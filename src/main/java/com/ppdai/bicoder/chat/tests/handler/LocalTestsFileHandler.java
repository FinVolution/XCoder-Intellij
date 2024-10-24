package com.ppdai.bicoder.chat.tests.handler;

import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffDialogHints;
import com.intellij.diff.DiffManager;
import com.intellij.diff.contents.DiffContent;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.diff.util.DiffUserDataKeys;
import com.intellij.diff.util.Side;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;
import com.ppdai.bicoder.chat.BiCoderChatToolWindowContentManager;
import com.ppdai.bicoder.chat.constant.TriggerCompleteCommand;
import com.ppdai.bicoder.chat.model.bo.MessageContextBo;
import com.ppdai.bicoder.chat.tests.MyTestsSchemaConfig;
import com.ppdai.bicoder.config.BiCoderBundle;
import com.ppdai.bicoder.config.PluginStaticConfig;
import com.ppdai.bicoder.utils.EditorUtils;
import com.ppdai.bicoder.utils.FileUtils;
import com.vladsch.flexmark.ast.FencedCodeBlock;
import com.vladsch.flexmark.parser.Parser;

import javax.swing.*;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocalTestsFileHandler {

    private final Project project;

    private final MessageContextBo messageContextBo;

    private String currentRequestId;
    private Editor suggestTestsEditor;

    private Editor localTestsEditor;

    private JComponent renderComponent;

    private int startOffset;

    private int lastCodeLength;

    private String renderCode;

    private boolean hasInit;

    private static final Pattern BLANK_START_PATTERN = Pattern.compile("^\\s*");

    private int firstLineBlankLength;
    private String language;


    public LocalTestsFileHandler(Project project, MessageContextBo messageContextBo, String firstProcessCode) {
        this.project = project;
        this.language = messageContextBo.getLanguage().toUpperCase();
        this.messageContextBo = messageContextBo;
        this.currentRequestId = messageContextBo.getMessageId();
        // 根据路径获得本地测试文件editor
        // 获取想打开的文件的VirtualFile对象
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(messageContextBo.getTestFilePath());
        // 获取Editor对象
        if (virtualFile != null) {
            //需要在EDT写线程中执行,不然会报错
            ApplicationManager.getApplication().invokeAndWait(() -> {
                this.localTestsEditor = WriteAction.compute(() ->
                        FileEditorManager.getInstance(project).openTextEditor(new OpenFileDescriptor(project, virtualFile), true));
                processLocalTestsCode(firstProcessCode);
            });
        }
    }


    public void processLocalTestsCode(String markdownCode) {
        if (!PluginStaticConfig.TESTS_SCHEMA_WHITELIST.contains(language)) {
            // 不支持的语言，直接返回
            return;
        }
        if (localTestsEditor == null) {
            // 本地测试文件没找到,直接返回
            return;
        }
        var document = Parser.builder().build().parse(markdownCode);
        var codeBlock = document.getChildOfType(FencedCodeBlock.class);
        if (codeBlock != null) {
            var code = ((FencedCodeBlock) codeBlock).getContentChars().unescape();
            if (!code.isEmpty()) {
                //如果代码块以换行符结尾，就去掉换行符
                if (codeBlock.getChars().endsWith("\n```") && code.endsWith("\n")) {
                    code = code.substring(0, code.length() - 1);
                }
                if (!hasInit) {
                    init();
                }
                renderCode = code;
                handleCode(code);
            }
        }
    }

    private void handleCode(String code) {
        var currentDocument = suggestTestsEditor.getDocument();
        Application application = ApplicationManager.getApplication();
        String finalCode = handleCodeIndent(code);
        application.invokeLater(
                () -> application.runWriteAction(() ->
                        WriteCommandAction.runWriteCommandAction(project, TriggerCompleteCommand.HANDLE_TESTS_CODE_COMMAND, null, () -> {
                            currentDocument.replaceString(startOffset, startOffset + lastCodeLength, finalCode);
                            lastCodeLength = finalCode.length();
                            suggestTestsEditor.getCaretModel().moveToOffset(startOffset + finalCode.length());
                            suggestTestsEditor.getComponent().revalidate();
                            suggestTestsEditor.getComponent().repaint();
                            complete();
                        })));
    }

    public void init() {
        // 根据不同语言计算开始索引位置和首行缩进
        Document document = localTestsEditor.getDocument();
        switch (this.language) {
            case "JAVA":
                //获取最后一个}闭区间的位置
                String content = document.getText();
                int lastBracePos = content.lastIndexOf('}');
                if (lastBracePos != -1) {
                    startOffset = lastBracePos;
                } else {
                    startOffset = document.getTextLength();
                }
                firstLineBlankLength = 4;
                break;
            case "PYTHON":
                startOffset = document.getTextLength();
                //获取最后一个方法的缩进
                firstLineBlankLength = getPythonLastMethodIndent(document.getText());
                break;
            case "GO":
                startOffset = document.getTextLength();
                firstLineBlankLength = 0;
                break;
            default:
                // 其他语言暂时不支持
                break;
        }
        suggestTestsEditor = EditorUtils.copyEditor(project, localTestsEditor);
        openDiffWindow();
        hasInit = true;
    }

    public void setCurrentRequestId(String currentRequestId) {
        this.currentRequestId = currentRequestId;
    }

    private void openDiffWindow() {
        var resultEditorFile = FileUtils.getEditorFile(localTestsEditor);
        var diffContentFactory = DiffContentFactory.getInstance();
        Application application = ApplicationManager.getApplication();
        DiffContent originalDiffContent = diffContentFactory.create(project, resultEditorFile);
        DiffContent suggestDiffContent = diffContentFactory.create(project, FileUtils.getEditorFile(suggestTestsEditor));
        originalDiffContent.putUserData(DiffUserDataKeys.FORCE_READ_ONLY, true);
        var request = new SimpleDiffRequest(
                BiCoderBundle.get("chat.diff.title"),
                originalDiffContent,
                suggestDiffContent,
                resultEditorFile.getName(),
                BiCoderBundle.get("chat.diff.suggest.content.title"));
        request.putUserData(
                DiffUserDataKeys.SCROLL_TO_LINE,
                Pair.create(Side.RIGHT, localTestsEditor.getDocument().getLineNumber(startOffset)));
        request.putUserData(MyTestsSchemaConfig.NEED_ACCEPT_AND_REJECT, true);
        request.putUserData(MyTestsSchemaConfig.SET_RENDER_COMPONENT, this::setRenderComponent);
        application.invokeLater(
                () -> application.runWriteAction(() ->
                        DiffManager.getInstance().showDiff(project, request, DiffDialogHints.DEFAULT)
                ));
    }

    private void setRenderComponent(JComponent renderComponent) {
        this.renderComponent = renderComponent;
    }


    private void acceptTests() {
        Application application = ApplicationManager.getApplication();
        application.invokeLater(
                () -> application.runWriteAction(() ->
                        WriteCommandAction.runWriteCommandAction(project, TriggerCompleteCommand.ACCEPT_TESTS_CODE_COMMAND, null, () -> {
                            //替换文本
                            Document selectedTextDocument = localTestsEditor.getDocument();
                            Document editCodeDocument = suggestTestsEditor.getDocument();
                            String text = editCodeDocument.getText();
                            selectedTextDocument.replaceString(0, selectedTextDocument.getTextLength(), text);
                            //关闭diff窗口
                            closeDiffWindow();
                        })));
    }

    private void rejectTests() {
        //关闭diff窗口
        closeDiffWindow();
    }

    private void closeDiffWindow() {
        //关闭diff窗口
        ActionManager actionManager = ActionManager.getInstance();
        AnAction closeAction = actionManager.getAction(IdeActions.ACTION_CLOSE);
        DataManager.getInstance().getDataContextFromFocusAsync().onSuccess(dataContext -> {
            AnActionEvent actionEvent = AnActionEvent.createFromAnAction(closeAction, null, ActionPlaces.EDITOR_TAB, dataContext);
            closeAction.actionPerformed(actionEvent);
            clear();
        });
    }

    public void complete() {
        if (renderComponent != null) {
            JButton acceptButton = new JButton(BiCoderBundle.get("plugin.common.accept"));
            acceptButton.setForeground(new JBColor(new Color(100, 200, 100), new Color(100, 200, 100)));
            acceptButton.setFont(UIUtil.getLabelFont().deriveFont(Font.BOLD));
            acceptButton.addActionListener(e -> {
                acceptTests();
            });
            JButton rejectButton = new JButton(BiCoderBundle.get("plugin.common.reject"));
            rejectButton.setForeground(new JBColor(new Color(200, 100, 100), new Color(200, 100, 100)));
            rejectButton.setFont(UIUtil.getLabelFont().deriveFont(Font.BOLD));
            rejectButton.addActionListener(e -> {
                rejectTests();
            });
            JPanel myButton = new JPanel();
            myButton.setLayout(new BoxLayout(myButton, BoxLayout.X_AXIS));
            myButton.add(Box.createHorizontalGlue());
            myButton.add(acceptButton);
            myButton.add(rejectButton);
            renderComponent.add(myButton, BorderLayout.NORTH);
            //关闭toolwindow
            project.getService(BiCoderChatToolWindowContentManager.class).hideToolWindow();
        }
    }

    public void clear() {
        lastCodeLength = 0;
        hasInit = false;
        startOffset = 0;
        suggestTestsEditor = null;
        renderComponent = null;
        renderCode = null;
        firstLineBlankLength = 0;
    }


    /**
     * 处理代码每行增加缩进,并处理第一行注释和开始结束换行
     *
     * @param code 代码
     * @return 处理后的代码
     */
    private String handleCodeIndent(String code) {

        //查看code第一行是不是#或//开头,如果是就去掉这一行
        if (code.startsWith("#") || code.startsWith("//")) {
            code = code.substring(code.indexOf("\n") + 1);
        }
        //查看code前10行是不是有import或class关键字,如果有,缩进为0
        boolean isClass = false;
        String[] lines = code.split("\n");
        for (int i = 0; i < lines.length && i < 10; i++) {
            if (lines[i].trim().startsWith("import ") || lines[i].trim().startsWith("class ")) {
                isClass = true;
                break;
            }
        }
        if (!isClass) {
            int firstLineIndent = getFirstLineIndent(code);
            int indentLength = firstLineBlankLength - firstLineIndent;
            if (indentLength > 0) {
                String indent = " ".repeat(indentLength);
                for (int i = 0; i < lines.length; i++) {
                    lines[i] = indent + lines[i];
                }
                code = String.join("\n", lines);
            }
        }
        //如果code开始不是换行,增加换行
        if (!code.startsWith("\n")) {
            code = "\n" + code;
        }
        //如果code结尾不是换行,增加换行
        if (!code.endsWith("\n")) {
            code = code + "\n";
        }
        return code;

    }

    /**
     * 获取代码第一行缩进长度
     *
     * @param code 代码
     * @return 缩进长度
     */
    private int getFirstLineIndent(String code) {
        Matcher matcher = BLANK_START_PATTERN.matcher(code);
        if (matcher.find()) {
            return matcher.group(0).length();
        }
        return 0;
    }

    /**
     * 获取python代码最后一个方法的缩进
     *
     * @param fileCode python代码
     * @return 缩进长度
     */
    public int getPythonLastMethodIndent(String fileCode) {
        String[] lines = fileCode.split("\n");
        int indent = 0;
        for (int i = lines.length - 1; i >= 0; i--) {
            if (lines[i].trim().startsWith("def ") || lines[i].trim().startsWith("async def ")) {
                indent = lines[i].length() - lines[i].trim().length();
                break;
            }
        }
        return indent;
    }
}
