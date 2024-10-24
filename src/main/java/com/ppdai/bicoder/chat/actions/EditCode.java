package com.ppdai.bicoder.chat.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.ppdai.bicoder.chat.BiCoderChatToolWindowContentManager;
import com.ppdai.bicoder.chat.constant.EventUserDataKeyConstant;
import com.ppdai.bicoder.chat.constant.MessageType;
import com.ppdai.bicoder.chat.conversation.Message;
import com.ppdai.bicoder.config.BiCoderBundle;
import com.ppdai.bicoder.config.PluginStaticConfig;
import com.ppdai.bicoder.utils.BalloonUtils;
import com.ppdai.bicoder.utils.EditorUtils;
import com.ppdai.bicoder.utils.InfoUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.event.MouseEvent;


public class EditCode extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Editor editor = anActionEvent.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            return;
        }
        TextRange textRange = EditorUtils.getSelectLineTextRange(editor);
        Document document = editor.getDocument();
        String selectedText = document.getText(textRange);
        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
        assert virtualFile != null;
        String language = InfoUtils.getLanguage(virtualFile);
        String userMessage = anActionEvent.getRequiredData(CommonDataKeys.PROJECT).getUserData(EventUserDataKeyConstant.USER_MESSAGE);
        Message message = new Message(userMessage, MessageType.EDIT.getType());
        message.setSelectedCode(selectedText);
        message.setSelectedCodeLanguage(language);
        message.setSelectCodeFileFullPath(virtualFile.getPath());
        Project project = anActionEvent.getProject();
        if (project != null) {
            project.getService(BiCoderChatToolWindowContentManager.class).sendCommandMessage(message);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setText(BiCoderBundle.get("chat.action.edit.code.action.title.Cn"));
    }
}
