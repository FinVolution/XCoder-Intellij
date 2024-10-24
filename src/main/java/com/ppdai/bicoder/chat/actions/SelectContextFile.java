package com.ppdai.bicoder.chat.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.awt.RelativePoint;
import com.ppdai.bicoder.chat.BiCoderChatToolWindowContentManager;
import com.ppdai.bicoder.chat.model.ChatContext;
import com.ppdai.bicoder.config.BiCoderBundle;
import com.ppdai.bicoder.config.PluginStaticConfig;
import com.ppdai.bicoder.utils.BalloonUtils;
import com.ppdai.bicoder.utils.InfoUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;


public class SelectContextFile extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        var project = anActionEvent.getProject();
        if (project == null) {
            return;
        }
        var fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFileDescriptor();
        Editor editor = anActionEvent.getData(CommonDataKeys.EDITOR);
        VirtualFile toSelect;
        if (editor != null) {
            toSelect = FileDocumentManager.getInstance().getFile(editor.getDocument());
        } else {
            toSelect = ProjectUtil.guessProjectDir(project);
        }
        var virtualFile = FileChooser.chooseFile(fileChooserDescriptor, project, toSelect);
        if (virtualFile != null) {
            long length = virtualFile.getLength();
            JComponent component = project.getService(BiCoderChatToolWindowContentManager.class).getToolWindow().getComponent();
            final Dimension size = component.getSize();
            RelativePoint relativePoint = new RelativePoint(component, new Point(20, size.height - 50));
            if (length > PluginStaticConfig.MAX_CONTEXT_FILE_SIZE) {
                BalloonUtils.showWarnIconBalloonRelative(BiCoderBundle.get("chat.action.select.file.context.file.size.too.big.hint"), PluginStaticConfig.WARNING_ICON, relativePoint);
                return;
            }
            String codePath = InfoUtils.getRelativePathNoContainFileName(project, virtualFile);
            Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
            if (document == null) {
                BalloonUtils.showWarnIconBalloonRelative(BiCoderBundle.get("chat.action.select.file.context.file.cannot.analysis.hint"), PluginStaticConfig.WARNING_ICON, relativePoint);
                return;
            }
            ChatContext chatContext = new ChatContext(ChatContext.TYPE_FILE, document.getText(), codePath,virtualFile.getPath(), virtualFile.getName(), 1, document.getLineCount());
            project.getService(BiCoderChatToolWindowContentManager.class).addChatContext(chatContext);
        }
    }
}
