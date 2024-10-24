package com.ppdai.bicoder.chat.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.ppdai.bicoder.chat.constant.ChatAction;
import com.ppdai.bicoder.chat.conversation.Conversation;
import com.ppdai.bicoder.chat.conversation.ConversationsStorage;
import com.ppdai.bicoder.config.BiCoderBundle;
import com.ppdai.bicoder.utils.EditorActionsUtil;
import org.jetbrains.annotations.NotNull;

/**
 * 清除当前对话
 */
public class ClearChatAction extends AnAction {

    private Runnable onActionPerformed;

    public ClearChatAction() {
        super();
    }

    public ClearChatAction(Runnable onActionPerformed) {
        super(BiCoderBundle.get("chat.action.icon.chat.clear.tooltip"), "Clears this chat", AllIcons.Actions.GC);
        this.onActionPerformed = onActionPerformed;
        EditorActionsUtil.registerOrReplaceAction(this, ChatAction.CLEAR_CHAT.getActionId());
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        super.update(event);
        Project project = event.getProject();
        if (project == null) {
            return;
        }
        Conversation currentConversation = ConversationsStorage.getInstance(project).getCurrentConversation();
        boolean isEnabled = currentConversation != null;
        event.getPresentation().setEnabled(isEnabled);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        onActionPerformed.run();
    }
}