package com.ppdai.bicoder.chat;

import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.project.Project;
import com.ppdai.bicoder.chat.components.markdown.MarkdownPanel;
import com.ppdai.bicoder.chat.components.ResponsePanel;
import com.ppdai.bicoder.chat.components.UserMessagePanel;
import com.ppdai.bicoder.chat.conversation.Conversation;
import com.ppdai.bicoder.chat.conversation.Message;
import com.ppdai.bicoder.chat.model.bo.MessageContextBo;
import com.ppdai.bicoder.utils.EditorUtils;
import com.ppdai.bicoder.utils.FileUtils;
import com.ppdai.bicoder.utils.BalloonUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;


public class BiCoderChatToolWindowTabPanel extends BaseChatToolWindowTabPanel {

    public BiCoderChatToolWindowTabPanel(@NotNull Project project) {
        this(project, null);
    }

    public BiCoderChatToolWindowTabPanel(@NotNull Project project,
                                         @Nullable Conversation conversation) {
        super(project);
        if (conversation == null) {
            displayLandingView();
        } else {
            displayConversation(conversation);
        }
    }

    @Override
    protected JComponent getLandingView() {
        return new StandardChatToolWindowLandingPanel();
    }

    @Override
    public void displayConversation(@NotNull Conversation conversation) {
        clearWindow();
        conversation.getMessages().forEach(message -> {
            MarkdownPanel messageResponseBody = new MarkdownPanel(project, new MessageContextBo(message.getId(), message.getType()), this)
                    .withResponse(message.getResponse());
            JPanel messageWrapper = createNewMessageWrapper(message.getId());
            messageWrapper.add(new UserMessagePanel(project, message, this));
            messageWrapper.add(new ResponsePanel()
                    .withReloadAction(() -> reloadMessage(message, conversation))
                    .withDeleteAction(() -> deleteMessage(message.getId(), messageWrapper, conversation))
                    .addContent(messageResponseBody));
        });
        setConversation(conversation);
    }
}
