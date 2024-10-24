package com.ppdai.bicoder.chat.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.ppdai.bicoder.chat.BiCoderChatToolWindowContentManager;
import com.ppdai.bicoder.chat.constant.ChatAction;
import com.ppdai.bicoder.chat.conversation.ConversationService;
import com.ppdai.bicoder.config.BiCoderBundle;
import com.ppdai.bicoder.config.PluginStaticConfig;
import com.ppdai.bicoder.utils.EditorActionsUtil;
import org.jetbrains.annotations.NotNull;

public class DeleteAllConversationsAction extends AnAction {

    private final Runnable onRefresh;

    public DeleteAllConversationsAction(Runnable onRefresh) {
        super(BiCoderBundle.get("chat.action.icon.conversation.delete.all.tooltip"), "Delete all conversations", AllIcons.Actions.GC);
        this.onRefresh = onRefresh;
        EditorActionsUtil.registerOrReplaceAction(this, ChatAction.DELETE_ALL_CONVERSATION.getActionId());
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        var project = event.getProject();
        if (project != null) {
            var sortedConversations = ConversationService.getInstance(project).getSortedConversations();
            event.getPresentation().setEnabled(!sortedConversations.isEmpty());
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        int answer = Messages.showYesNoDialog(
                BiCoderBundle.get("chat.action.delete.history.conversation.message"),
                BiCoderBundle.get("chat.action.delete.history.conversation.title"),
                PluginStaticConfig.BI_CODER_ICON);
        if (answer == Messages.YES) {
            var project = event.getProject();
            if (project != null) {
                ConversationService.getInstance(project).clearAll();
                project.getService(BiCoderChatToolWindowContentManager.class).resetActiveTab();
            }
            this.onRefresh.run();
        }
    }
}
