package com.ppdai.bicoder.chat.tests.handler;

import com.intellij.ide.util.EditorHelper;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiManager;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;
import com.ppdai.bicoder.chat.constant.TriggerCompleteCommand;
import com.ppdai.bicoder.chat.model.bo.MessageContextBo;
import com.ppdai.bicoder.config.BiCoderBundle;
import com.ppdai.bicoder.config.PluginStaticConfig;
import com.ppdai.bicoder.config.UserProjectSetting;
import com.ppdai.bicoder.utils.BiCoderLoggerUtils;
import com.ppdai.bicoder.utils.EditorUtils;
import com.ppdai.bicoder.utils.FileUtils;
import com.vladsch.flexmark.ast.FencedCodeBlock;
import com.vladsch.flexmark.parser.Parser;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

import static java.lang.String.format;

public class NewTestsFileHandler {


    public static final String GO = "GO";
    public static final String PYTHON = "PYTHON";
    public static final String JAVA = "JAVA";
    public static final String OTHER = "OTHER";
    public static final String SLASH = "/";
    public static final String SRC_MAIN_JAVA = "/src/main/java";
    private final Project project;
    private final String language;

    private UserProjectSetting userProjectSetting;

    private final MessageContextBo messageContextBo;

    private String currentRequestId;

    private boolean hasInit;

    private String renderCode;

    private Editor suggestTestsEditor;

    private int lastCodeLength;
    private String suggestTestsFileName;

    private String suggestTestPath;

    public NewTestsFileHandler(Project project, MessageContextBo messageContextBo) {
        this.project = project;
        this.userProjectSetting = UserProjectSetting.getInstance(project);
        this.messageContextBo = messageContextBo;
        this.currentRequestId = messageContextBo.getMessageId();
        this.language = messageContextBo.getLanguage().toUpperCase();
    }

    public void processNewTestsCode(String markdownCode) {
        if (!PluginStaticConfig.TESTS_SCHEMA_WHITELIST.contains(this.language)) {
            // 不支持的语言，直接返回
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

    private void handleCode(String finalCode) {
        if (suggestTestsEditor == null) {
            return;
        }
        var currentDocument = suggestTestsEditor.getDocument();
        Application application = ApplicationManager.getApplication();
        application.invokeLater(
                () -> application.runWriteAction(() ->
                        WriteCommandAction.runWriteCommandAction(project, TriggerCompleteCommand.HANDLE_TESTS_CODE_COMMAND, null, () -> {
                            currentDocument.replaceString(0, lastCodeLength, finalCode);
                            lastCodeLength = finalCode.length();
                            suggestTestsEditor.getCaretModel().moveToOffset(finalCode.length());
                            suggestTestsEditor.getComponent().revalidate();
                            suggestTestsEditor.getComponent().repaint();
                        })));
    }

    public void init() {
        this.suggestTestsFileName = getTestsFileName();
        String selectCodeFileFullPath = messageContextBo.getSelectCodeFileFullPath();
        //去除selectCodeFileFullPath路径中文件名
        String selectCodeFileFullPathNoFileName = selectCodeFileFullPath.substring(0, selectCodeFileFullPath.lastIndexOf(SLASH) + 1);
        String language = this.language;
        if (StringUtils.isBlank(language)) {
            language = OTHER;
        }
        this.suggestTestPath = getSuggestTestPath(project, language, selectCodeFileFullPathNoFileName);
        LightVirtualFile virtualFile = new LightVirtualFile(
                format("%s%s", suggestTestPath, suggestTestsFileName),
                "");
        openNewFileWindow(virtualFile);
        hasInit = true;
    }

    private void openNewFileWindow(LightVirtualFile virtualFile) {
        ApplicationManager.getApplication().invokeLaterOnWriteThread(() -> {
            this.suggestTestsEditor = FileEditorManager.getInstance(project).openTextEditor(new OpenFileDescriptor(project, virtualFile), true);
        });
    }

    @NotNull
    private String getTestsFileName() {
        String language = this.language;
        String selectCodeFileFullPath = messageContextBo.getSelectCodeFileFullPath();
        String selectCodeFileName;
        if (selectCodeFileFullPath != null) {
            selectCodeFileName = selectCodeFileFullPath.substring(selectCodeFileFullPath.lastIndexOf(SLASH) + 1);
        } else {
            var timestamp = System.currentTimeMillis();
            selectCodeFileName = "temp_" + timestamp + ".txt";

        }
        String testFileName;
        switch (language) {
            case JAVA:
                testFileName = selectCodeFileName.substring(0, selectCodeFileName.lastIndexOf(".")) + "Test" + selectCodeFileName.substring(selectCodeFileName.lastIndexOf("."));
                break;
            case GO:
                testFileName = selectCodeFileName.substring(0, selectCodeFileName.lastIndexOf(".")) + "_test" + selectCodeFileName.substring(selectCodeFileName.lastIndexOf("."));
                break;
            case PYTHON:
                //python的单元测试文件使用test_加原文件名
            default:
                // 其他语言全部使用test_加原文件名
                testFileName = "test_" + selectCodeFileName;
        }
        return testFileName;
    }

    @NotNull
    private String getSuggestTestPath(@NotNull Project project, @NotNull String language, @NotNull String currentSelectCodeFileFullPath) {
        //读取配置的测试文件根目录，没有配置则使用默认
        String testRootPath = getConfigTestRootPath(language);
        if (StringUtils.isNotBlank(testRootPath) && !testRootPath.startsWith(SLASH)) {
            testRootPath = SLASH + testRootPath;
        }
        String basePath = project.getBasePath();
        String testFilePath;
        switch (language) {
            case JAVA:
                //java使用test根目录下的原路径,如果有根目录配置,优先使用
                if (currentSelectCodeFileFullPath.contains(SRC_MAIN_JAVA)) {
                    testFilePath = currentSelectCodeFileFullPath.replace(SRC_MAIN_JAVA, testRootPath);
                } else {
                    VirtualFile vf = VirtualFileManager.getInstance().findFileByUrl(currentSelectCodeFileFullPath);
                    if (vf != null) {
                        com.intellij.openapi.module.Module module = ModuleUtilCore.findModuleForFile(vf, project);
                        if (module != null) {
                            VirtualFile moduleFile = ProjectUtil.guessModuleDir(module);
                            if (moduleFile != null) {
                                testFilePath = moduleFile.getPath() + testRootPath;
                                break;
                            }
                        }
                    }
                    testFilePath = basePath + testRootPath;
                }
                break;
            case PYTHON:
                //python使用test根目录
                testFilePath = basePath + testRootPath;
                break;
            case GO:
            default:
                //go和其他语言使用原路径,如果有根目录配置,优先使用
                if (StringUtils.isNotBlank(testRootPath)) {
                    testFilePath = basePath + testRootPath;
                } else {
                    testFilePath = currentSelectCodeFileFullPath;
                }

        }
        if (!testFilePath.endsWith(SLASH)) {
            testFilePath = testFilePath + SLASH;
        }
        return testFilePath;
    }

    @NotNull
    private String getConfigTestRootPath(String language) {
        String testFileRootPath = userProjectSetting.getTestFileRootPath();
        if (StringUtils.isBlank(testFileRootPath)) {
            testFileRootPath = PluginStaticConfig.TEST_FILE_ROOT_PATH.get(language);
            if (StringUtils.isNotBlank(testFileRootPath)) {
                userProjectSetting.setTestFileRootPath(testFileRootPath);
            }
        }
        return testFileRootPath;
    }

    public void setCurrentRequestId(String currentRequestId) {
        this.currentRequestId = currentRequestId;
    }


    public void complete() {
        if (suggestTestsEditor != null) {
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
            JComponent renderComponent = suggestTestsEditor.getComponent();
            renderComponent.add(myButton, BorderLayout.NORTH);
        }
    }

    private void rejectTests() {
        //关闭临时editor窗口
        closeTemporaryEditorWindow();

    }

    private void acceptTests() {
        var file = FileUtils.createFile(
                suggestTestPath,
                suggestTestsFileName,
                renderCode);
        var virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
        if (virtualFile == null) {
            BiCoderLoggerUtils.getInstance(getClass()).warn("Couldn't find the saved virtual file");
            return;
        }
        var psiFile = PsiManager.getInstance(project).findFile(virtualFile);
        if (psiFile == null) {
            BiCoderLoggerUtils.getInstance(getClass()).warn("Couldn't find the saved virtual file");
            return;
        }
        EditorHelper.openInEditor(psiFile);
        int lineNum = 0;
        if (renderCode != null) {
            lineNum = renderCode.split("\n").length;
        }
        //关闭临时editor窗口
        closeTemporaryEditorWindow();
    }

    private void closeTemporaryEditorWindow() {
        EditorUtils.closeEditor(project, suggestTestsEditor);
        clear();
    }

    public void clear() {
        lastCodeLength = 0;
        hasInit = false;
        suggestTestsEditor = null;
        renderCode = null;
        suggestTestsFileName = null;
        suggestTestPath = null;
    }
}
