package com.ppdai.bicoder.chat.conversation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * 对话对象
 *
 */
public class Conversation {

    private String id;
    private List<Message> messages = new ArrayList<>();
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public LocalDateTime getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(LocalDateTime updatedOn) {
        this.updatedOn = updatedOn;
    }

    public void removeMessage(String messageId) {
        setMessages(messages.stream()
                .filter(message -> !message.getId().equals(messageId))
                .collect(toList()));
    }
}
