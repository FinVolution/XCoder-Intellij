package com.ppdai.bicoder.chat.conversation;

import java.util.ArrayList;
import java.util.List;

/**
 * 会话容器
 */
public class ConversationsContainer {

    private List<Conversation> conversations = new ArrayList<>();

    public List<Conversation> getConversations() {
        return conversations;
    }

    public void setConversations(List<Conversation> conversations) {
        this.conversations = conversations;
    }
}
