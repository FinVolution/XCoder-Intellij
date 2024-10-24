package com.ppdai.bicoder.chat;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.ContentManagerEvent;
import com.intellij.ui.content.ContentManagerListener;
import com.ppdai.bicoder.chat.conversation.ConversationsHistoryToolWindow;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * 工具窗口工厂
 *
 */
public class BiCoderToolWindowFactory implements ToolWindowFactory, DumbAware {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        BiCoderChatToolWindowPanel chatToolWindowPanel = new BiCoderChatToolWindowPanel(project, toolWindow.getDisposable());
        ConversationsHistoryToolWindow conversationsHistoryToolWindow = new ConversationsHistoryToolWindow(project);

        addContent(toolWindow, chatToolWindowPanel, "Chat");
        addContent(toolWindow, conversationsHistoryToolWindow.getContent(), "Chat History");
        toolWindow.addContentManagerListener(new ContentManagerListener() {
            @Override
            public void selectionChanged(@NotNull ContentManagerEvent event) {
                var content = event.getContent();
                if ("Chat History".equals(content.getTabName()) && content.isSelected()) {
                    conversationsHistoryToolWindow.refresh();
                }
            }
        });
    }

    public void addContent(ToolWindow toolWindow, JComponent panel, String displayName) {
        var contentManager = toolWindow.getContentManager();
        contentManager.addContent(contentManager.getFactory().createContent(panel, displayName, false));
    }
}
