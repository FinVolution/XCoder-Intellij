package com.ppdai.bicoder.chat.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.ppdai.bicoder.chat.BiCoderChatToolWindowContentManager;
import com.ppdai.bicoder.chat.model.ChatContext;
import com.ppdai.bicoder.config.BiCoderBundle;
import com.ppdai.bicoder.config.PluginStaticConfig;
import com.ppdai.bicoder.utils.BalloonUtils;
import com.ppdai.bicoder.utils.EditorUtils;
import com.ppdai.bicoder.utils.InfoUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;


public class AddFileChatContext extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Editor editor = anActionEvent.getData(CommonDataKeys.EDITOR);
        if (!EditorUtils.hasSelection(editor)) {
            InputEvent inputEvent = anActionEvent.getInputEvent();
            Point locationOnScreen;
            if (inputEvent instanceof MouseEvent) {
                locationOnScreen = ((MouseEvent) inputEvent).getLocationOnScreen();
                locationOnScreen.y = locationOnScreen.y - 16;
                BalloonUtils.showWarnIconBalloon(BiCoderBundle.get("chat.action.generate.message.need.selected.code"), PluginStaticConfig.WARNING_ICON, locationOnScreen);
            } else {
                assert editor != null;
                BalloonUtils.showWarnIconBalloonRelative(BiCoderBundle.get("chat.action.generate.message.need.selected.code"), PluginStaticConfig.WARNING_ICON, JBPopupFactory.getInstance().guessBestPopupLocation(editor));
            }
            return;
        }
        String selectedText = editor.getSelectionModel().getSelectedText();
        int selectionStart = editor.getSelectionModel().getSelectionStart();
        int selectionEnd = editor.getSelectionModel().getSelectionEnd();
        int startLine = editor.getDocument().getLineNumber(selectionStart) + 1;
        int endLine = editor.getDocument().getLineNumber(selectionEnd) + 1;
        VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
        assert virtualFile != null;
        Project project = anActionEvent.getProject();
        assert project != null;
        String codePath = InfoUtils.getRelativePathNoContainFileName(project, virtualFile);
        ChatContext chatContext = new ChatContext(ChatContext.TYPE_FILE, selectedText, codePath, virtualFile.getPath(), virtualFile.getName(), startLine, endLine);
        project.getService(BiCoderChatToolWindowContentManager.class).addChatContext(chatContext);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setText(BiCoderBundle.get("chat.action.add.selected.code.action.title"));
    }
}
