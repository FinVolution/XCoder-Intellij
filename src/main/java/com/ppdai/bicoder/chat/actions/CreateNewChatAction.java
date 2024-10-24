package com.ppdai.bicoder.chat.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.ppdai.bicoder.chat.constant.ChatAction;
import com.ppdai.bicoder.config.BiCoderBundle;
import com.ppdai.bicoder.utils.EditorActionsUtil;
import org.jetbrains.annotations.NotNull;

/**
 * 创建新对话
 *
 */
public class CreateNewChatAction extends AnAction {

    private final Runnable onCreate;

    public CreateNewChatAction(Runnable onCreate) {
        super(BiCoderBundle.get("chat.action.icon.chat.new.tooltip"), "Create new chat", AllIcons.General.Add);
        this.onCreate = onCreate;
        EditorActionsUtil.registerOrReplaceAction(this, ChatAction.CREATE_NEW_CHAT.getActionId());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        var project = event.getProject();
        if (project != null) {
            onCreate.run();
        }
    }
}
