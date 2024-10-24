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
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.ppdai.bicoder.chat.BiCoderChatToolWindowContentManager;
import com.ppdai.bicoder.chat.constant.EventUserDataKeyConstant;
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


public class GenerateDoc extends AnAction {
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
        Message message = new Message("", MessageType.DOC.getType());
        message.setSelectedCode(selectedText);
        message.setSelectedCodeLanguage(language);
        message.setSelectCodeFileFullPath(virtualFile.getPath());
        Project project = anActionEvent.getProject();
        if (project != null) {
            //增加当前文件到上下文中
            String codePath = InfoUtils.getRelativePathNoContainFileName(project, virtualFile);
            ChatContext chatContext = new ChatContext(ChatContext.TYPE_FILE_LOCAL, editor.getDocument().getText(), codePath, virtualFile.getPath(), virtualFile.getName(), 1, editor.getDocument().getLineCount());
            project.getService(BiCoderChatToolWindowContentManager.class).addChatContext(chatContext);
            project.getService(BiCoderChatToolWindowContentManager.class).sendCommandMessage(message);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setText(BiCoderBundle.get("chat.action.start.inline.doc.code.action.title"));
    }


}
