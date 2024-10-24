package com.ppdai.bicoder.chat.edit.handler;

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
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;
import com.ppdai.bicoder.chat.constant.TriggerCompleteCommand;
import com.ppdai.bicoder.chat.edit.MyEditConfig;
import com.ppdai.bicoder.config.BiCoderBundle;
import com.ppdai.bicoder.utils.EditorUtils;
import com.ppdai.bicoder.utils.FileUtils;
import com.vladsch.flexmark.ast.FencedCodeBlock;
import com.vladsch.flexmark.parser.Parser;

import javax.swing.*;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditorCodeHandler {

    private final Project project;

    private final String messageId;

    private String currentRequestId;
    private Editor editCodeEditor;

    private final Editor selectedTextEditor;

    private JComponent renderComponent;

    private int startOffset;

    private int lastCodeLength;

    private boolean hasInit;

    private static final Pattern BLANK_START_PATTERN = Pattern.compile("^\\s*");

    private int firstLineBlankLength;

    public EditorCodeHandler(Project project, String messageId) {
        this.project = project;
        this.messageId = messageId;
        this.currentRequestId = messageId;
        this.selectedTextEditor = FileEditorManager.getInstance(project).getSelectedTextEditor();
    }

    public void processEditCode(String markdownCode) {
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
                handleCode(code);
            }
        }

    }

    private void handleCode(String code) {
        var currentDocument = editCodeEditor.getDocument();
        Application application = ApplicationManager.getApplication();
        String finalCode = handleCodeIndent(code);
        application.invokeLater(
                () -> application.runWriteAction(() ->
                        WriteCommandAction.runWriteCommandAction(project, TriggerCompleteCommand.HANDLE_CHAT_EDIT_CODE_COMMAND, null, () -> {
                            currentDocument.replaceString(startOffset, startOffset + lastCodeLength, finalCode);
                            lastCodeLength = finalCode.length();
                            editCodeEditor.getCaretModel().moveToOffset(startOffset + finalCode.length());
                            editCodeEditor.getComponent().revalidate();
                            editCodeEditor.getComponent().repaint();
                        })));
    }

    public void init() {
        TextRange textRange = EditorUtils.getSelectLineTextRange(selectedTextEditor);
        startOffset = textRange.getStartOffset();
        Document document = selectedTextEditor.getDocument();
        int lineNumber = document.getLineNumber(startOffset);
        String text = document.getText(new TextRange(document.getLineStartOffset(lineNumber), document.getLineEndOffset(lineNumber)));
        firstLineBlankLength = getFirstLineIndent(text);
        editCodeEditor = EditorUtils.copyEditorAndDeleteSelectedCode(project, selectedTextEditor, startOffset, textRange.getEndOffset());
        hasInit = true;
        openDiffWindow();
    }

    public void setCurrentRequestId(String currentRequestId) {
        this.currentRequestId = currentRequestId;
    }

    private void openDiffWindow() {
        var resultEditorFile = FileUtils.getEditorFile(selectedTextEditor);
        var diffContentFactory = DiffContentFactory.getInstance();
        Application application = ApplicationManager.getApplication();
        DiffContent originalDiffContent = diffContentFactory.create(project, resultEditorFile);
        DiffContent suggestDiffContent = diffContentFactory.create(project, FileUtils.getEditorFile(editCodeEditor));
        originalDiffContent.putUserData(DiffUserDataKeys.FORCE_READ_ONLY, true);
        var request = new SimpleDiffRequest(
                BiCoderBundle.get("chat.diff.title"),
                originalDiffContent,
                suggestDiffContent,
                resultEditorFile.getName(),
                BiCoderBundle.get("chat.diff.suggest.content.title"));
        request.putUserData(
                DiffUserDataKeys.SCROLL_TO_LINE,
                Pair.create(Side.RIGHT, selectedTextEditor.getDocument().getLineNumber(startOffset)));
        request.putUserData(MyEditConfig.NEED_ACCEPT_AND_REJECT, true);
        request.putUserData(MyEditConfig.SET_RENDER_COMPONENT, this::setRenderComponent);
        application.invokeLater(
                () -> application.runWriteAction(() ->
                        DiffManager.getInstance().showDiff(project, request, DiffDialogHints.DEFAULT)
                ));
    }

    private void setRenderComponent(JComponent renderComponent) {
        this.renderComponent = renderComponent;
    }


    private void acceptEdit() {
        Application application = ApplicationManager.getApplication();
        application.invokeLater(
                () -> application.runWriteAction(() ->
                        WriteCommandAction.runWriteCommandAction(project, TriggerCompleteCommand.ACCEPT_CHAT_EDIT_CODE_COMMAND, null, () -> {
                            //替换文本
                            Document selectedTextDocument = selectedTextEditor.getDocument();
                            Document editCodeDocument = editCodeEditor.getDocument();
                            String text = editCodeDocument.getText();
                            selectedTextDocument.replaceString(0, selectedTextDocument.getTextLength(), text);
                            //关闭diff窗口
                            closeDiffWindow();
                        })));
    }

    private void rejectEdit() {
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
                acceptEdit();
            });
            JButton rejectButton = new JButton(BiCoderBundle.get("plugin.common.reject"));
            rejectButton.setForeground(new JBColor(new Color(200, 100, 100), new Color(200, 100, 100)));
            rejectButton.setFont(UIUtil.getLabelFont().deriveFont(Font.BOLD));
            rejectButton.addActionListener(e -> {
                rejectEdit();
            });
            JPanel myButton = new JPanel();
            myButton.setLayout(new BoxLayout(myButton, BoxLayout.X_AXIS));
            myButton.add(Box.createHorizontalGlue());
            myButton.add(acceptButton);
            myButton.add(rejectButton);
            renderComponent.add(myButton, BorderLayout.NORTH);
        }
    }

    public void clear() {
        lastCodeLength = 0;
        hasInit = false;
        startOffset = 0;
        editCodeEditor = null;
        renderComponent = null;
    }


    /**
     * 处理代码每行增加缩进
     *
     * @param code 代码
     * @return 处理后的代码
     */
    private String handleCodeIndent(String code) {
        int firstLineIndent = getFirstLineIndent(code);
        int indentLength = firstLineBlankLength - firstLineIndent;
        if (indentLength > 0) {
            String indent = " ".repeat(indentLength);
            String[] lines = code.split("\n");
            for (int i = 0; i < lines.length; i++) {
                lines[i] = indent + lines[i];
            }
            return String.join("\n", lines);
        } else {
            return code;
        }
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
}
