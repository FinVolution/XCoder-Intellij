package com.ppdai.bicoder.chat.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.ppdai.bicoder.chat.constant.ChatAction;
import com.ppdai.bicoder.chat.conversation.ConversationsStorage;
import com.ppdai.bicoder.config.BiCoderBundle;
import com.ppdai.bicoder.utils.EditorActionsUtil;
import org.jetbrains.annotations.NotNull;

public class DeleteConversationAction extends AnAction {

    private final Runnable onDelete;

    public DeleteConversationAction(Runnable onDelete) {
        super(BiCoderBundle.get("chat.action.icon.conversation.delete.tooltip"), "Delete single conversation", AllIcons.Actions.GC);
        this.onDelete = onDelete;
        EditorActionsUtil.registerOrReplaceAction(this, ChatAction.DELETE_CONVERSATION.getActionId());
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) {
            return;
        }
        event.getPresentation().setEnabled(ConversationsStorage.getInstance(project).getCurrentConversation() != null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
//    if (OverlayUtils.showDeleteConversationDialog() == Messages.YES) {
        var project = event.getProject();
        if (project != null) {
            onDelete.run();
        }
    }
}
