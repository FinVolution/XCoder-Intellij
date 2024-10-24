package com.ppdai.bicoder.chat.components.markdown;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.JBUI;
import com.ppdai.bicoder.chat.ResponseEditor;
import com.ppdai.bicoder.chat.ResponseNodeRenderer;
import com.ppdai.bicoder.chat.StreamParser;
import com.ppdai.bicoder.chat.StreamResponseType;
import com.ppdai.bicoder.chat.constant.MessageType;
import com.ppdai.bicoder.chat.constant.TestSchema;
import com.ppdai.bicoder.chat.constant.TriggerCompleteCommand;
import com.ppdai.bicoder.chat.diff.handler.BaseDiffHandler;
import com.ppdai.bicoder.chat.edit.handler.EditorCodeHandler;
import com.ppdai.bicoder.chat.model.bo.MessageContextBo;
import com.ppdai.bicoder.chat.tests.handler.LocalTestsFileHandler;
import com.ppdai.bicoder.chat.tests.handler.NewTestsFileHandler;
import com.ppdai.bicoder.config.BiCoderBundle;
import com.ppdai.bicoder.utils.MarkdownUtils;
import com.ppdai.bicoder.utils.SwingUtils;
import com.ppdai.bicoder.utils.ThemeUtils;
import com.vladsch.flexmark.ast.FencedCodeBlock;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

import static java.lang.String.format;
import static javax.swing.event.HyperlinkEvent.EventType.ACTIVATED;

public class MarkdownPanel extends JPanel {

    private final Project project;
    private final Disposable parentDisposable;
    private final StreamParser streamParser;
    private JPanel currentlyProcessedElement;
    private ResponseEditor currentlyProcessedEditor;
    private JTextPane currentlyProcessedTextPane;
    private boolean responseReceived;
    private final boolean canCopy;
    private final boolean canInsert;
    private final boolean canNewFile;
    private final boolean canDiff;

    private final MessageContextBo messageContextBo;

    private String currentRequestId;

    private EditorCodeHandler editorCodeHandler;
    private BaseDiffHandler baseDiffHandler;

    private LocalTestsFileHandler localTestsFileHandler;
    private NewTestsFileHandler newTestsFileHandler;


    public MarkdownPanel(Project project, MessageContextBo messageContextBo, Disposable parentDisposable) {
        this(project, ThemeUtils.getPanelBackgroundColor(), messageContextBo, false, parentDisposable);
    }

    public MarkdownPanel(
            Project project,
            MessageContextBo messageContextBo,
            boolean withGhostText,
            Disposable parentDisposable) {
        this(project, ThemeUtils.getPanelBackgroundColor(), messageContextBo, withGhostText, parentDisposable);
    }

    public MarkdownPanel(
            Project project,
            Color backgroundColor,
            MessageContextBo messageContextBo,
            boolean withGhostText,
            Disposable parentDisposable) {
        this(project, backgroundColor, messageContextBo, withGhostText, parentDisposable, true, true, true, true);

    }

    public MarkdownPanel(
            Project project,
            Color backgroundColor,
            MessageContextBo messageContextBo,
            boolean withGhostText,
            Disposable parentDisposable, boolean canCopy, boolean canInsert, boolean canNewFile, boolean canDiff) {
        super(new BorderLayout());
        this.canCopy = canCopy;
        this.canInsert = canInsert;
        this.canNewFile = canNewFile;
        this.canDiff = canDiff;
        this.project = project;
        this.parentDisposable = parentDisposable;
        this.streamParser = new StreamParser();
        this.messageContextBo = messageContextBo;
        this.currentRequestId = messageContextBo.getMessageId();
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setBackground(backgroundColor);

        if (withGhostText) {
            prepareProcessingTextResponse();
            currentlyProcessedTextPane.setText(
                    "<html><p style=\"margin-top: 4px; margin-bottom: 8px;\">&#8205;</p></html>");
        }

        UIManager.addPropertyChangeListener(propertyChangeEvent -> setBackground(backgroundColor));
    }


    public MarkdownPanel withResponse(String response) {
        for (var message : MarkdownUtils.splitCodeBlocks(response)) {
            boolean isCodeResponse = message.startsWith("```");
            if (isCodeResponse) {
                currentlyProcessedEditor = null;
            }
            processResponse(message, isCodeResponse);
        }
        hideCarets();

        return this;
    }

    public void append(String partialMessage) {
        boolean isEditCode = MessageType.EDIT.getType().equals(messageContextBo.getMessageType());
        boolean isTestsCode = MessageType.TESTS.getType().equals(messageContextBo.getMessageType());
        boolean isBaseDiffCode = MessageType.DOC.getType().equals(messageContextBo.getMessageType())
                || MessageType.OPTIMIZE.getType().equals(messageContextBo.getMessageType());
        boolean hasLocalTestFile = TestSchema.LOCAL.equals(messageContextBo.getTestSchema());
        for (var item : streamParser.parse(partialMessage)) {
            processResponse(item.getResponse(), StreamResponseType.CODE.equals(item.getType()), isEditCode, isTestsCode, isBaseDiffCode, hasLocalTestFile, item.isTwiceProcessingCode());
        }
    }

    public void complete() {
        boolean isEditCode = MessageType.EDIT.getType().equals(messageContextBo.getMessageType());
        if (isEditCode && editorCodeHandler != null) {
            editorCodeHandler.complete();
        }
        boolean isBaseDiffCode = MessageType.DOC.getType().equals(messageContextBo.getMessageType())
                || MessageType.OPTIMIZE.getType().equals(messageContextBo.getMessageType());
        if (isBaseDiffCode && baseDiffHandler != null) {
            baseDiffHandler.complete();
        }
        boolean isTestsCode = MessageType.TESTS.getType().equals(messageContextBo.getMessageType());
        boolean hasLocalTestFile = TestSchema.LOCAL.equals(messageContextBo.getTestSchema());
        if (isTestsCode) {
            if (hasLocalTestFile) {
                processLocalTestsCode(streamParser.getFirstProcessCode());
            }
            if (!hasLocalTestFile && newTestsFileHandler != null) {
                newTestsFileHandler.complete();
            }
        }
    }

    public void hideCarets() {
        if (currentlyProcessedEditor != null) {
            ((EditorEx) currentlyProcessedEditor.getEditor()).setCaretVisible(false);
            ((EditorEx) currentlyProcessedEditor.getEditor()).setCaretEnabled(false);
        }
        if (currentlyProcessedTextPane != null && currentlyProcessedTextPane.getCaret().isVisible()) {
            currentlyProcessedTextPane.getCaret().setVisible(false);
        }
    }

    public void displayError(String message) {
        var errorText = format(
                "<html><p style=\"margin-top: 4px; margin-bottom: 8px;\">%s</p></html>",
                message);
        if (responseReceived) {
            var errorPane = createTextPane();
            errorPane.setText(errorText);
            add(new ResponseWrapper().add(errorPane));
        } else {
            currentlyProcessedTextPane.setText(errorText);
        }
    }

    public void displayDefaultError() {
        displayError(BiCoderBundle.get("chat.error.something.wrong"));
    }


    public void clear() {
        removeAll();
        if (editorCodeHandler != null) {
            editorCodeHandler.clear();
        }

        streamParser.clear();
        prepareProcessingTextResponse();
        currentlyProcessedTextPane.setText(
                "<html><p style=\"margin-top: 4px; margin-bottom: 8px;\">&#8205;</p></html>");

        repaint();
        revalidate();
    }

    private void processResponse(String markdownInput, boolean isCodeResponse) {
        processResponse(markdownInput, isCodeResponse, false, false, false, false, false);
    }

    private void processResponse(String markdownInput, boolean isCodeResponse, boolean isEditCode, boolean isTestsCode, boolean isBaseDiffCode, boolean hasLocalTestFile, boolean isTwiceProcessingCode) {
        responseReceived = true;
        if (isCodeResponse) {
            processCode(markdownInput);
            if (isEditCode && !isTwiceProcessingCode) {
                processEditCode(markdownInput);
            }
            if (isBaseDiffCode && !isTwiceProcessingCode) {
                processBaseDiffCode(markdownInput);
            }
            //如果是单测,并且当前文件没有找到本地测试文件，并且不是第二段代码，就处理新增文件
            if (isTestsCode && !isTwiceProcessingCode && !hasLocalTestFile) {
                processNewTestsCode(markdownInput);
            }
        } else {
            processText(markdownInput);
        }
    }


    private void processCode(String markdownCode) {
        var document = Parser.builder().build().parse(markdownCode);
        var codeBlock = document.getChildOfType(FencedCodeBlock.class);
        if (codeBlock != null) {
            var code = ((FencedCodeBlock) codeBlock).getContentChars().unescape();
            var language = ((FencedCodeBlock) codeBlock).getInfo();
            if (!code.isEmpty()) {
                //如果代码块以换行符结尾，就去掉换行符
                if (codeBlock.getChars().endsWith("\n```") && code.endsWith("\n")) {
                    code = code.substring(0, code.length() - 1);
                }
                if (currentlyProcessedEditor == null) {
                    prepareProcessingCodeResponse(code, language.unescape());
                }
                updateEditorDocument(currentlyProcessedEditor.getEditor(), code);
            }
        }
    }

    private void processEditCode(String markdownCode) {
        if (editorCodeHandler == null) {
            editorCodeHandler = new EditorCodeHandler(project, messageContextBo.getMessageId());
        }
        editorCodeHandler.processEditCode(markdownCode);
    }

    private void processBaseDiffCode(String markdownCode) {
        if (baseDiffHandler == null) {
            baseDiffHandler = new BaseDiffHandler(project, messageContextBo.getMessageId());
        }
        baseDiffHandler.processEditCode(markdownCode);

    }

    /**
     * 处理本地测试代码,只在结束时处理
     *
     * @param firstProcessCode 第一个处理的代码块
     */
    private void processLocalTestsCode(String firstProcessCode) {
        localTestsFileHandler = new LocalTestsFileHandler(project, messageContextBo, firstProcessCode);
    }

    private void processNewTestsCode(String markdownCode) {
        if (newTestsFileHandler == null) {
            newTestsFileHandler = new NewTestsFileHandler(project, messageContextBo);
        }
        newTestsFileHandler.processNewTestsCode(markdownCode);
    }

    public void setCurrentRequestId(String currentRequestId) {
        this.currentRequestId = currentRequestId;
        if (editorCodeHandler != null) {
            editorCodeHandler.setCurrentRequestId(currentRequestId);
        }
        if (localTestsFileHandler != null) {
            localTestsFileHandler.setCurrentRequestId(currentRequestId);
        }
        if (newTestsFileHandler != null) {
            newTestsFileHandler.setCurrentRequestId(currentRequestId);
        }
    }

    private void processText(String markdownText) {
        if (currentlyProcessedTextPane == null) {
            prepareProcessingTextResponse();
        }
        currentlyProcessedTextPane.setText(convertMdToHtml(markdownText));
    }

    private void prepareProcessingTextResponse() {
        hideCarets();
        currentlyProcessedEditor = null;
        currentlyProcessedTextPane = createTextPane();
        currentlyProcessedElement = new ResponseWrapper();
        currentlyProcessedElement.add(currentlyProcessedTextPane);
        add(currentlyProcessedElement);
    }

    private void prepareProcessingCodeResponse(String code, String language) {
        hideCarets();
        currentlyProcessedTextPane = null;
        currentlyProcessedEditor = new ResponseEditor(
                project,
                currentRequestId,
                messageContextBo,
                code,
                language,
                parentDisposable,
                canCopy, canInsert, canNewFile, canDiff);
        currentlyProcessedElement = new ResponseWrapper();
        currentlyProcessedElement.add(currentlyProcessedEditor);
        add(currentlyProcessedElement);
    }

    private void updateEditorDocument(Editor editor, String code) {
        var document = editor.getDocument();
        var application = ApplicationManager.getApplication();
        Runnable updateDocumentRunnable = () -> {
            application.runWriteAction(() ->
                    WriteCommandAction.runWriteCommandAction(project, TriggerCompleteCommand.UPDATE_CHAT_EDITOR_DOCUMENT_COMMAND, null, () -> {
                        try {
                            document.setReadOnly(false);
                            document.replaceString(0, document.getTextLength(), code);
                            editor.getCaretModel().moveToOffset(code.length());
                            editor.getComponent().revalidate();
                            editor.getComponent().repaint();
                            document.setReadOnly(true);
                        } catch (Exception ignore) {
                            //由于\n换行符在字符串中计算长度会存在问题，会抛出索引越界问题，直接忽视
                        }
                    }));
        };

        if (application.isUnitTestMode()) {
            application.invokeAndWait(updateDocumentRunnable);
        } else {
            application.invokeLater(updateDocumentRunnable);
        }
    }

    private JTextPane createTextPane() {
        var textPane = new JTextPane();
        textPane.addHyperlinkListener(event -> {
            if (FileUtil.exists(event.getDescription()) && ACTIVATED.equals(event.getEventType())) {
                VirtualFile file = LocalFileSystem.getInstance().findFileByPath(event.getDescription());
                FileEditorManager.getInstance(project).openFile(Objects.requireNonNull(file), true);
                return;
            }

            SwingUtils.handleHyperlinkClicked(event);
        });
        textPane.setContentType("text/html");
        textPane.putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, true);
        textPane.setCaretPosition(textPane.getDocument().getLength());
        textPane.setBackground(getBackground());
        textPane.setFocusable(true);
        textPane.getCaret().setVisible(true);
        textPane.setEditable(false);
        textPane.setBorder(JBUI.Borders.empty());
        return textPane;
    }

    private String convertMdToHtml(String message) {
        MutableDataSet options = new MutableDataSet();
        var document = Parser.builder(options).build().parse(message);
        return HtmlRenderer.builder(options)
                .nodeRendererFactory(new ResponseNodeRenderer.Factory())
                .build()
                .render(document);
    }

    private static class ResponseWrapper extends JPanel {

        ResponseWrapper() {
            super(new BorderLayout());
            setBorder(JBUI.Borders.empty());
            setBackground(getBackground());

            UIManager.addPropertyChangeListener(propertyChangeEvent -> setBackground(getBackground()));
        }
    }
}
