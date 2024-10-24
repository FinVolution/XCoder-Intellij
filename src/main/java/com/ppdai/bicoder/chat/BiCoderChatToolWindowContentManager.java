package com.ppdai.bicoder.chat;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.ppdai.bicoder.cache.ProjectCache;
import com.ppdai.bicoder.chat.conversation.Conversation;
import com.ppdai.bicoder.chat.conversation.ConversationsStorage;
import com.ppdai.bicoder.chat.conversation.Message;
import com.ppdai.bicoder.chat.model.ChatContext;
import com.ppdai.bicoder.config.PluginStaticConfig;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Service(Service.Level.PROJECT)
public final class BiCoderChatToolWindowContentManager {

    private final Project project;

    public BiCoderChatToolWindowContentManager(Project project) {
        this.project = project;
    }

    public void sendMessage(Message message) {
        var chatTabbedPane = tryFindChatTabbedPane();
        if (chatTabbedPane.isPresent()) {
            var activeTabPanel = chatTabbedPane.get().tryFindActiveTabPanel();
            if (activeTabPanel.isPresent()) {
                sendMessage(activeTabPanel.get(), message);
                return;
            }
        }
        sendMessage(createNewTabPanel(), message);
    }

    public void addChatContext(ChatContext chatContext) {
        var chatTabbedPane = tryFindChatTabbedPane();
        if (chatTabbedPane.isPresent()) {
            var activeTabPanel = chatTabbedPane.get().tryFindActiveTabPanel();
            if (activeTabPanel.isPresent()) {
                addChatContext(activeTabPanel.get(), chatContext);
                return;
            }
        }
        addChatContext(createNewTabPanel(), chatContext);
    }

    public void startEditCode() {
        var chatTabbedPane = tryFindChatTabbedPane();
        if (chatTabbedPane.isPresent()) {
            var activeTabPanel = chatTabbedPane.get().tryFindActiveTabPanel();
            if (activeTabPanel.isPresent()) {
                startEditCode(activeTabPanel.get());
                return;
            }
        }
        startEditCode(createNewTabPanel());
    }

    public void startEditCode(ChatToolWindowTabPanel toolWindowTabPanel) {
        showToolWindow();
        toolWindowTabPanel.startEditCode();
    }

    public void addChatContext(ChatToolWindowTabPanel toolWindowTabPanel, ChatContext chatContext) {
        toolWindowTabPanel.addChatContext(chatContext);
    }

    @Nullable
    public List<ChatContext> getChatContextListInArea() {
        var chatTabbedPane = tryFindChatTabbedPane();
        if (chatTabbedPane.isPresent()) {
            var activeTabPanel = chatTabbedPane.get().tryFindActiveTabPanel();
            if (activeTabPanel.isPresent()) {
                return activeTabPanel.get().getChatContextListInArea();
            }
        }
        return null;
    }


    /**
     * 发送command特殊消息
     *
     * @param message 消息
     */
    public void sendCommandMessage(Message message) {
        List<ChatContext> chatContextList = project.getService(ProjectCache.class).getChatContextList();
        if (CollectionUtils.isNotEmpty(chatContextList)) {
            message.setChatContexts(new ArrayList<>(chatContextList));
        }
        //每个command问题都需要新开conversation
        sendMessage(replaceNewTabPanel(), message);
    }

    public void sendMessage(ChatToolWindowTabPanel toolWindowTabPanel, Message message) {
        showToolWindow();
        if (ConversationsStorage.getInstance(project).getCurrentConversation() == null) {
            toolWindowTabPanel.startNewConversation(message);
        } else {
            toolWindowTabPanel.sendMessage(message);
        }
    }

    public void displayConversation(Conversation conversation) {
        displayChatTab();
        tryFindChatTabbedPane()
                .ifPresent(tabbedPane -> tabbedPane.tryFindActiveConversationTitle(conversation.getId())
                        .ifPresentOrElse(
                                title -> tabbedPane.setSelectedIndex(tabbedPane.indexOfTab(title)),
                                () -> tabbedPane.replaceNewTab(new BiCoderChatToolWindowTabPanel(project, conversation))));
    }

    public void clearCurrentChat() {
        tryFindChatTabbedPane()
                .ifPresent(BiCoderChatToolWindowTabbedPane::resetCurrentlyActiveTabPanel);
    }

    public BiCoderChatToolWindowTabPanel replaceNewTabPanel() {
        displayChatTab();
        var tabbedPane = tryFindChatTabbedPane();
        if (tabbedPane.isPresent()) {
            var panel = new BiCoderChatToolWindowTabPanel(project);
            tabbedPane.get().replaceNewTab(panel);
            return panel;
        }
        return null;
    }

    public BiCoderChatToolWindowTabPanel createNewTabPanel() {
        displayChatTab();
        var tabbedPane = tryFindChatTabbedPane();
        if (tabbedPane.isPresent()) {
            var panel = new BiCoderChatToolWindowTabPanel(project);
            tabbedPane.get().addNewTab(panel);
            return panel;
        }
        return null;
    }

    public void displayChatTab() {
        var toolWindow = showToolWindow();

        var contentManager = toolWindow.getContentManager();
        tryFindChatTabContent().ifPresentOrElse(
                contentManager::setSelectedContent,
                () -> contentManager.setSelectedContent(requireNonNull(contentManager.getContent(0)))
        );
    }

    @NotNull
    public ToolWindow showToolWindow() {
        var toolWindow = getToolWindow();
        toolWindow.show();
        return toolWindow;
    }

    public ToolWindow hideToolWindow() {
        var toolWindow = getToolWindow();
        toolWindow.hide();
        return toolWindow;
    }

    public Optional<BiCoderChatToolWindowTabbedPane> tryFindChatTabbedPane() {
        var chatTabContent = tryFindChatTabContent();
        if (chatTabContent.isPresent()) {
            var tabbedPane = Arrays.stream(chatTabContent.get().getComponent().getComponents())
                    .filter(BiCoderChatToolWindowTabbedPane.class::isInstance)
                    .findFirst();
            if (tabbedPane.isPresent()) {
                return Optional.of((BiCoderChatToolWindowTabbedPane) tabbedPane.get());
            }
        }
        return Optional.empty();
    }

    public void resetActiveTab() {
        tryFindChatTabbedPane().ifPresent(tabbedPane -> {
            tabbedPane.clearAll();
            tabbedPane.addNewTab(new BiCoderChatToolWindowTabPanel(project));
        });
    }

    public ToolWindow getToolWindow() {
        return requireNonNull(ToolWindowManager.getInstance(project).getToolWindow(PluginStaticConfig.PLUGIN_NAME + " Chat"));
    }

    private Optional<Content> tryFindChatTabContent() {
        return Arrays.stream(getToolWindow().getContentManager().getContents())
                .filter(content -> "Chat".equals(content.getTabName()))
                .findFirst();
    }

    public BiCoderChatToolWindowTabPanel findOrNewChatTab() {
        var chatTabbedPane = tryFindChatTabbedPane();
        if (chatTabbedPane.isPresent()) {
            var activeTabPanel = chatTabbedPane.get().tryFindActiveTabPanel();
            if (activeTabPanel.isPresent()) {
                return activeTabPanel.get();
            }
        }
        return createNewTabPanel();
    }
}
