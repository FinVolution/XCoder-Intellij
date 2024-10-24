package com.ppdai.bicoder.handler;

import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.InlayModel;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.update.MergingUpdateQueue;
import com.intellij.util.ui.update.Update;
import com.ppdai.bicoder.cache.CompletionCache;
import com.ppdai.bicoder.cache.ProjectCache;
import com.ppdai.bicoder.chat.constant.TriggerCompleteCommand;
import com.ppdai.bicoder.chat.model.ChatContext;
import com.ppdai.bicoder.config.PluginStaticConfig;
import com.ppdai.bicoder.config.UserSetting;
import com.ppdai.bicoder.model.*;
import com.ppdai.bicoder.renderer.CodeGenHintRenderer;
import com.ppdai.bicoder.service.BiCoderService;
import com.ppdai.bicoder.utils.*;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 补全结果处理器
 *
 */
public class BiCoderCompletionHandler {

    public static final Key<BiCoderCodeCompletion> BI_CODER_CODE_COMPLETION = new Key<>("BiCoder Code Completion");

    public static final Key<Integer> BI_CODER_POSITION = new Key<>("BiCoder Position");

    private final MergingUpdateQueue serviceQueue;

    private final MergingUpdateQueue getClassDefineQueue;

    private final BiCoderService biCoderService = ApplicationManager.getApplication().getService(BiCoderService.class);

    {
        serviceQueue = new MergingUpdateQueue("BiCoderServiceQueue", UserSetting.getInstance().getMaxConsecutiveInputIntervalTime(), true, null, ApplicationManager.getApplication(), null, false);
        serviceQueue.setRestartTimerOnAdd(true);
        getClassDefineQueue = new MergingUpdateQueue("BiCoderGetClassDefineQueue", PluginStaticConfig.MAX_CONSECUTIVE_GET_CLASS_DEFINE_INTERVAL_TIME, true, null, ApplicationManager.getApplication(), null, false);
        getClassDefineQueue.setRestartTimerOnAdd(true);
    }


    /**
     * 触发代码补全提示
     *
     * @param focusedEditor         当前焦点编辑器
     * @param currentCursorPosition 当前光标位置
     */
    public void triggerAutoCompletion(@NotNull Editor focusedEditor, int currentCursorPosition) {
        if (forbidPlugin()) {
            return;
        }
        //未获取到编辑器实例或者当前焦点编辑器不是主编辑器,直接忽视,不做任何处理
        if (!EditorUtils.isEditorValidForAutocomplete(focusedEditor)) {
            return;
        }
        var commandName = CommandProcessor.getInstance().getCurrentCommandName();
        //回退，清除提示，然后重新请求
        if (TriggerCompleteCommand.BACKSPACE_COMMAND.equals(commandName)) {
            CompletionUtils.cleanCodeCompletion(focusedEditor);
        }
        Project project = focusedEditor.getProject();
        if (project == null) {
            BiCoderLoggerUtils.getInstance(getClass()).warn("can not find project");
            return;
        }
        Document document = focusedEditor.getDocument();
        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
        if (virtualFile == null) {
            BiCoderLoggerUtils.getInstance(getClass()).warn("can not find virtualFile");
            return;
        }

        int lastPosition = getLastPosition(virtualFile);
        // 更新位置
        virtualFile.putUserData(BI_CODER_POSITION, currentCursorPosition);

        //如果是command黑名单,不做任何处理
        if (EditorUtils.isCommandExcluded(commandName) || judgeSelection(focusedEditor)) {
            CompletionUtils.cleanCodeCompletion(focusedEditor);
            return;
        }
        ApplicationManager.getApplication().invokeLater(() -> {
            //整体catch,避免错误被idea捕获抛出到用户界面
            try {
                InlayModel inlayModel = focusedEditor.getInlayModel();
                if (judgeUserInput(focusedEditor, virtualFile, currentCursorPosition, lastPosition, inlayModel)) {
                    return;
                }
                CompletionUtils.cleanCodeCompletion(focusedEditor);

                String ideaInfo = InfoUtils.getIdeaInfo();
                String gitUrl = InfoUtils.getGitUrl(project);
                String gitBranch = InfoUtils.getGitBranch(project);
                String relativePath = InfoUtils.getRelativePath(virtualFile, project);
                String language = InfoUtils.getLanguage(virtualFile);
                String beforeCursor = CursorUtils.getMaxCursorPrefix(document, currentCursorPosition);
                String afterCursor = CursorUtils.getMaxCursorSuffix(document, currentCursorPosition);
                String generateScheme = InfoUtils.getGenerateScheme(beforeCursor, afterCursor);
                Boolean isSubMultiLine = InfoUtils.judgeMultiline(focusedEditor, currentCursorPosition, virtualFile, generateScheme);
                if (shouldIgnore(language, generateScheme, document, currentCursorPosition)) {
                    return;
                }
                getClassDefineQueue.queue(Update.create(focusedEditor, () -> ApplicationManager.getApplication().runReadAction(() -> {
                    try {
                        InfoUtils.getClassMethodDefineInCurrentFileRange(language, relativePath, virtualFile, document, focusedEditor, currentCursorPosition, project);
                    } catch (Throwable e) {
                        BiCoderLoggerUtils.getInstance(getClass()).warn("getClassMethodDefineInCurrentFileRange error", e);
                    }
                })));
                serviceQueue.queue(Update.create(focusedEditor, () -> {
                    //整体catch,避免错误被idea捕获抛出到用户界面
                    try {
                        getAndRenderCompletion(project, focusedEditor, currentCursorPosition, virtualFile, ideaInfo, gitUrl, gitBranch, relativePath, language, beforeCursor, afterCursor, generateScheme, isSubMultiLine);
                    } catch (Throwable e) {
                        BiCoderLoggerUtils.getInstance(getClass()).warn("getAndRenderCompletion error", e);
                    }
                }));
            } catch (Throwable e) {
                BiCoderLoggerUtils.getInstance(getClass()).warn("updateInlayHints inner error", e);

            }
        });
    }

    /**
     * 获取并渲染代码补全提示
     *
     * @param project               当前项目
     * @param focusedEditor         当前焦点编辑器
     * @param currentCursorPosition 当前光标位置
     * @param virtualFile           当前编辑器文件
     * @param ideaInfo              idea信息
     * @param gitUrl                git地址
     * @param gitBranch             git分支
     * @param relativePath          相对路径
     * @param language              语言
     * @param beforeCursor          前置内容
     * @param afterCursor           后置内容
     * @param generateScheme        生成方案
     * @param isSubMultiLine        是否多行
     */
    private void getAndRenderCompletion(@NotNull Project project, @NotNull Editor focusedEditor, int currentCursorPosition, VirtualFile virtualFile, String ideaInfo, String gitUrl, String gitBranch, String relativePath, String language, String beforeCursor, String afterCursor, String generateScheme, Boolean isSubMultiLine) {

        //请求前做判定是否应该继续,减轻服务器压力
        boolean shouldContinue = ApplicationManager.getApplication().runReadAction((Computable<Boolean>) () -> {
            // 如果位置已更改或存在选中文本,或存在活跃弹出窗口,则放弃显示这个提示
            return currentCursorPosition == focusedEditor.getCaretModel().getOffset() && focusedEditor.getSelectionModel().getSelectedText() == null && LookupManager.getActiveLookup(focusedEditor) == null;
        });
        if (!shouldContinue) {
            return;
        }

        BiCoderCompletion biCoderCompletion = null;
        boolean enableCache = UserSetting.getInstance().getEnableCache();
        //处理去掉后置内容中最前置的空格和换行来作为缓存key,这些内容不影响补全结果
        String suffixCacheKey = getSuffixCacheKey(afterCursor);
        boolean isCached = false;
        if (enableCache) {
            //尝试从缓存中获取补全结果
            biCoderCompletion = CompletionCache.get(beforeCursor, suffixCacheKey);
            isCached = true;
        }
        //缓存中不存在补全结果
        if (biCoderCompletion == null) {
            BiCoderCompletionRequest.Builder builder = new BiCoderCompletionRequest.Builder().ideaInfo(ideaInfo).gitUrl(gitUrl).gitBranch(gitBranch).path(relativePath).language(language.toUpperCase()).beforeCursor(beforeCursor).afterCursor(afterCursor).cursorOffset(currentCursorPosition).generateScheme(generateScheme);
            if (isSubMultiLine != null) {
                builder = builder.addExtra("isSubMultiLine", isSubMultiLine);
            }
            long start = System.currentTimeMillis();
            //记录行数
            int lineCount = focusedEditor.getDocument().getLineCount();
            builder.codeTotalLines(lineCount);
            //获取上下文
            LinkedHashMap<String, ChatContext> currentOpenedEditorContextList = project.getService(ProjectCache.class).getCurrentOpenedEditorContextList(virtualFile.getPath());
            List<CompletionContext> completionContextList = currentOpenedEditorContextList.values().stream()
                    .map(chatContext -> new CompletionContext(chatContext.getType(), chatContext.getFilePath() + "/" + chatContext.getFileName(), chatContext.getContent())).collect(Collectors.toList());
            BiCoderLoggerUtils.getInstance(getClass()).info("CompletionContext:" + completionContextList);
            builder.completionContexts(completionContextList);
            BiCoderLoggerUtils.getInstance(getClass()).info("CompletionContext cost:" + (System.currentTimeMillis() - start));
            BiCoderCompletionRequest request = builder.build();
            biCoderCompletion = biCoderService.getCompletion(request);
            //todo: 计算补全结果评分,如果评分低于阈值,则不做任何处理

            //存储补全结果到缓存中
            if (enableCache) {
                CompletionCache.put(beforeCursor, suffixCacheKey, biCoderCompletion);
            }
            isCached = false;
        }

        if (biCoderCompletion != null) {
            //目前默认只有一条
            String text = biCoderCompletion.getText();
            //过滤处理重复性代码,例:将foo=foo=foo=处理成foo=
            text = FormatUtils.handleRepetitiveCode(text);
            String[] hintList = biCoderService.buildCompletionList(text);
            this.addCodeCompletion(focusedEditor, virtualFile, currentCursorPosition, hintList, biCoderCompletion.getCompletionId(), isCached);
        }
    }


    /**
     * 渲染代码补全提示
     *
     * @param focusedEditor      当前编辑器
     * @param file               当前文件
     * @param completionPosition 补全提示位置
     * @param hintList           补全提示列表
     * @param completionId       补全id
     * @param isCached           是否缓存
     */
    private void addCodeCompletion(Editor focusedEditor, VirtualFile file, int completionPosition, String[] hintList, String completionId, boolean isCached) {
        WriteCommandAction.runWriteCommandAction(focusedEditor.getProject(), TriggerCompleteCommand.RENDER_COMPLETION_CODE_COMMAND, null, () -> {
            file.putUserData(BI_CODER_CODE_COMPLETION, new BiCoderCodeCompletion(completionId, hintList, isCached));

            // 请求后最后一次判定，如果位置已更改或存在文本现在已被选中，则放弃显示这个提示
            if (completionPosition != focusedEditor.getCaretModel().getOffset() || focusedEditor.getSelectionModel().getSelectedText() != null) {
                file.putUserData(BI_CODER_CODE_COMPLETION, null);
                return;
            }
            InlayModel inlayModel = focusedEditor.getInlayModel();
            inlayModel.getInlineElementsInRange(0, focusedEditor.getDocument().getTextLength(), CodeGenHintRenderer.class).forEach(Disposable::dispose);
            inlayModel.getBlockElementsInRange(0, focusedEditor.getDocument().getTextLength(), CodeGenHintRenderer.class).forEach(Disposable::dispose);
            if (hintList != null && hintList.length > 0) {
                // 第一行做光标后行内渲染
                if (!hintList[0].trim().isEmpty()) {
                    List<FirstLineCompletion> firstLineCompletionList = renderInline(focusedEditor, completionPosition, inlayModel, hintList[0], file.getUserData(BI_CODER_CODE_COMPLETION));
                    if (CollectionUtils.isEmpty(firstLineCompletionList) && hintList.length == 1) {
                        //如果当前只有一行，而第一行没有渲染内容，则直接触发cancel
                        file.putUserData(BI_CODER_CODE_COMPLETION, null);
                        return;
                    }
                }
                // 后续每一行直接做块渲染
                // 渲染存在问题,如果用户把第一行都输入完,按下回车,显示会错乱,目前修复设置回车会去除渲染重新请求临时解决，需要后续优化
                for (int i = 1; i < hintList.length; i++) {
                    inlayModel.addBlockElement(completionPosition, false, false, 0, new CodeGenHintRenderer(hintList[i]));
                }
            }
        });
    }

    /**
     * 是否需要忽视当前补全
     *
     * @param language              语言
     * @param generateScheme        生成模式
     * @param document              文档
     * @param currentCursorPosition 光标位置
     * @return 是否需要忽视
     */
    private boolean shouldIgnore(String language, String generateScheme, Document document, int currentCursorPosition) {
        UserSetting userSetting = UserSetting.getInstance();

        //语言为空或者默认语言名单不包含或黑名单包含当前语言
        if (language == null || (userSetting.getFileWhitelist() != null && !userSetting.getFileWhitelist().contains(language)) || userSetting.getDisableFileTypeList().contains(language)) {
            BiCoderLoggerUtils.getInstance(getClass()).info("ignore language: " + language);
            return true;
        }
        //生成模式为空或者黑名单包含当前模式
        if (generateScheme == null || PluginStaticConfig.INVALID_GENERATE_SCHEME.contains(generateScheme)) {
            return true;
        }
        String currentLineCursorPrefix = CursorUtils.getCurrentLineCursorPrefix(document, currentCursorPosition);
        String currentLineCursorSuffix = CursorUtils.getCurrentLineCursorSuffix(document, currentCursorPosition);
        //?Y:  所有这种情况要看光标后的字符是否正则验证通过
        if (generateScheme.contains("?Y")) {
            assert currentLineCursorSuffix != null;
            if (!currentLineCursorSuffix.matches(userSetting.getValidSuffixReg())) {
                return true;
            }
        }
        //Y?N: 要求Y的末尾字符不能在黑名单中
        if (generateScheme.contains("Y?N")) {
            assert currentLineCursorPrefix != null;
            String lastChar = currentLineCursorPrefix.substring(currentLineCursorPrefix.length() - 1);
            if (PluginStaticConfig.PRE_SYMBOL_BLACKLIST.contains(lastChar)) {
                return true;
            }
        }

        //NY?: 要求Y的字数超过阈值
        if (generateScheme.contains("NY?")) {
            assert currentLineCursorPrefix != null;
            String trim = currentLineCursorPrefix.trim();
            return trim.length() < userSetting.getValidPrefixLength();
        }

        return false;
    }


    /**
     * 判断用户输入是否与补全一致,如果一致,则不做任何处理,如果不一致,则取消补全提示
     *
     * @param focusedEditor         当前编辑器
     * @param file                  当前文件
     * @param currentCursorPosition 当前光标位置
     * @param lastPosition          上次光标位置
     * @param inlayModel            当前提示模型
     * @return true:用户输入与补全一致, false:用户输入与补全不一致,或者补全代码被完全输入
     */
    private boolean judgeUserInput(@NotNull Editor focusedEditor, @NotNull VirtualFile file, int currentCursorPosition, int lastPosition, InlayModel inlayModel) {

        //检查当前提示是否存在
        BiCoderCodeCompletion biCoderCodeCompletion = file.getUserData(BI_CODER_CODE_COMPLETION);
        if (biCoderCodeCompletion == null) {
            return false;
        }
        // 根据用户输入做相应处理
        if (currentCursorPosition > lastPosition) {
            String[] existingHints = biCoderCodeCompletion.getCodeCompletion();
            if (existingHints != null && existingHints.length > 0) {
                String inlineHint = existingHints[0];
                String modifiedText = focusedEditor.getDocument().getCharsSequence().subSequence(lastPosition, currentCursorPosition).toString();
                if (modifiedText.startsWith("\n")) {
                    // 如果是回车,idea有可能会补充缩进,导致处理不正确,去除后置空格
                    modifiedText = modifiedText.replace(" ", "");
                }
                //如果是回车,目前没有好方法处理idea自动缩进问题,忽视,重新生成提示
                if ("\n".equals(modifiedText)) {
                    return false;
                }
                //检测用户输入是否与补全补全一致,
                if (inlineHint.startsWith(modifiedText)) {
                    // 如果是,则更新提示删除已输入内容来代替再次发起请求
                    inlineHint = inlineHint.substring(modifiedText.length());
                    if (!inlineHint.isEmpty()) {
                        // 如果当前行还有内容,只需更新并移动
                        inlayModel.getInlineElementsInRange(0, focusedEditor.getDocument().getTextLength(), CodeGenHintRenderer.class).forEach(Disposable::dispose);
                        List<FirstLineCompletion> firstLineCompletionList = renderInline(focusedEditor, currentCursorPosition, inlayModel, inlineHint, biCoderCodeCompletion);
                        if (CollectionUtils.isEmpty(firstLineCompletionList) && existingHints.length == 1) {
                            //如果当前只有一行，而第一行没有渲染内容，则直接触发cancel
                            file.putUserData(BI_CODER_CODE_COMPLETION, null);
                            return false;
                        }
                        existingHints[0] = inlineHint;
                        //更新光标位置缓存和补全结果缓存
                        biCoderCodeCompletion.setCodeCompletion(existingHints);
                        file.putUserData(BI_CODER_CODE_COMPLETION, biCoderCodeCompletion);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private List<FirstLineCompletion> renderInline(@NotNull Editor focusedEditor, int currentCursorPosition, InlayModel inlayModel, String inlineHint, BiCoderCodeCompletion biCoderCodeCompletion) {
        Document document = focusedEditor.getDocument();
        int lineNumber = document.getLineNumber(currentCursorPosition);
        int lineEndOffset = document.getLineEndOffset(lineNumber);
        String originalText = document.getText(new TextRange(currentCursorPosition, lineEndOffset));
        Patch<String> patch = DiffUtils.diff(this.characterList(originalText), this.characterList(inlineHint));
        List<Delta<String>> deltas = patch.getDeltas();
        if (deltas.size() > 1 && !deltas.stream().allMatch(delta -> delta.getType() == Delta.TYPE.INSERT)) {
            BiCoderLoggerUtils.getInstance(getClass()).info("Skipping autocomplete with non-insert deltas: " + patch);
            return Collections.emptyList();
        }
        List<FirstLineCompletion> firstLineCompletionList = new ArrayList<>();
        for (Delta<String> delta : deltas) {
            String text = String.join("", delta.getRevised().getLines());
            int position = delta.getOriginal().getPosition();
            inlayModel.addInlineElement(
                    currentCursorPosition + position,
                    true,
                    new CodeGenHintRenderer(text));
            firstLineCompletionList.add(new FirstLineCompletion(position, text));
        }
        biCoderCodeCompletion.setFirstLineCompletionList(firstLineCompletionList);
        return firstLineCompletionList;
    }

    private List<String> characterList(String value) {
        return value.chars()
                .mapToObj(c -> String.valueOf((char) c))
                .collect(Collectors.toList());
    }

    /**
     * 计算后置缓存key
     *
     * @param afterCursor 后置内容
     * @return 后置缓存key
     */
    private String getSuffixCacheKey(String afterCursor) {
        if (afterCursor != null) {
            afterCursor = afterCursor.replaceAll("^[\\s\\t\\r\\n]+", "");
        }
        return afterCursor;
    }


    /**
     * 获取上次索引位置
     *
     * @param file 当前文件
     * @return 上次索引位置
     */
    private Integer getLastPosition(@NotNull VirtualFile file) {
        Integer biCoderPos = file.getUserData(BI_CODER_POSITION);
        //取不到上次索引位置,默认置为0
        return (biCoderPos == null) ? 0 : biCoderPos;
    }


    /**
     * 判断当前是否是在idea里有选择内容,是的话,清除所有的提示
     *
     * @param focusedEditor 当前编辑器
     * @return 是否是选择
     */
    private boolean judgeSelection(@NotNull Editor focusedEditor) {
        String selection = focusedEditor.getCaretModel().getCurrentCaret().getSelectedText();
        return selection != null && !selection.isEmpty();
    }


    /**
     * 判断是否禁用插件
     *
     * @return 是否禁用插件
     */
    private boolean forbidPlugin() {
        UserSetting userSetting = UserSetting.getInstance();
        boolean enablePlugin = userSetting.getEnablePlugin();
        //用户插件未启用
        return !enablePlugin;
    }


}
