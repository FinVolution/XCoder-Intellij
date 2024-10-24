package com.ppdai.bicoder.chat.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.ppdai.bicoder.chat.BiCoderChatToolWindowContentManager;
import com.ppdai.bicoder.chat.constant.MessageType;
import com.ppdai.bicoder.chat.conversation.Message;
import com.ppdai.bicoder.chat.model.ChatContext;
import com.ppdai.bicoder.chat.model.ChatContextFile;
import com.ppdai.bicoder.config.BiCoderBundle;
import com.ppdai.bicoder.config.PluginStaticConfig;
import com.ppdai.bicoder.utils.BalloonUtils;
import com.ppdai.bicoder.utils.ContextUtils;
import com.ppdai.bicoder.utils.EditorUtils;
import com.ppdai.bicoder.utils.InfoUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.event.MouseEvent;


public class GenerateTests extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Editor editor = anActionEvent.getData(CommonDataKeys.EDITOR);
        if (!EditorUtils.hasSelection(editor)) {
            var locationOnScreen = ((MouseEvent) anActionEvent.getInputEvent()).getLocationOnScreen();
            locationOnScreen.y = locationOnScreen.y - 16;
            BalloonUtils.showWarnIconBalloon(BiCoderBundle.get("chat.action.generate.message.need.selected.code"), PluginStaticConfig.WARNING_ICON, locationOnScreen);
            return;
        }
        String selectedText = editor.getSelectionModel().getSelectedText();
        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
        assert virtualFile != null;
        String language = InfoUtils.getLanguage(virtualFile);
        //todo 是否需要支持用户输入内容
        Message message = new Message("", MessageType.TESTS.getType());
        message.setSelectedCode(selectedText);
        message.setSelectedCodeLanguage(language);
        message.setSelectCodeFileFullPath(virtualFile.getPath());
        Project project = anActionEvent.getProject();
        if (project != null) {
            //增加当前文件到上下文中
            String codePath = InfoUtils.getRelativePathNoContainFileName(project, virtualFile);
            String codeLanguage = InfoUtils.getLanguage(virtualFile).toUpperCase();
            ChatContext chatContext = new ChatContext(ChatContext.TYPE_FILE_LOCAL, editor.getDocument().getText(), codePath, virtualFile.getPath(), virtualFile.getName(), 1, editor.getDocument().getLineCount());
            project.getService(BiCoderChatToolWindowContentManager.class).addChatContext(chatContext);
            //增加单测文件到上下文中,开启异步线程,避免阻塞UI线程
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                ChatContextFile chatContextTestFile = ContextUtils.getTestFile(editor, codeLanguage, virtualFile, project);
                if (chatContextTestFile != null) {
                    VirtualFile testVirtualFile = chatContextTestFile.getFile();
                    String testFilePath = InfoUtils.getRelativePathNoContainFileName(project, testVirtualFile);
                    Document document = ReadAction.compute(()->FileDocumentManager.getInstance().getDocument(testVirtualFile));
                    if (document != null) {
                        ChatContext testFileChatContext = new ChatContext(chatContextTestFile.getType(), document.getText(), testFilePath, testVirtualFile.getPath(), testVirtualFile.getName(), 1, document.getLineCount());
                        project.getService(BiCoderChatToolWindowContentManager.class).addChatContext(testFileChatContext);
                    }
                }
                ApplicationManager.getApplication().invokeLater(() -> {
                    project.getService(BiCoderChatToolWindowContentManager.class).sendCommandMessage(message);
                });
            });
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setText(BiCoderBundle.get("chat.action.generate.tests.action.title"));
    }
}
