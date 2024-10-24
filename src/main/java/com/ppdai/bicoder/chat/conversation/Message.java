package com.ppdai.bicoder.chat.conversation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ppdai.bicoder.chat.model.ChatContext;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 消息对象
 *
 */
public class Message {

    public static final String USER_MESSAGE_TYPE = "user";

    public static final String AI_MESSAGE_TYPE = "assistant";

    private final String id;

    /**
     * 当前请求id,主要为了reload时后台不同请求区分
     */
    private String currentRequestId;
    private final String userMessage;
    private String response;
    private String type;
    public String selectedCode;
    public String selectedCodeLanguage;
    public String selectCodeFileFullPath;
    private List<ChatContext> chatContexts;


    public Message(String userMessage, String type, String response) {
        this(userMessage, type);
        this.response = response;
    }

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public Message(@JsonProperty("userMessage") String userMessage,@JsonProperty("type") String type) {
        this.id = UUID.randomUUID().toString();
        this.currentRequestId = this.id;
        this.userMessage = userMessage;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCurrentRequestId() {
        return currentRequestId;
    }

    public void setCurrentRequestId(String currentRequestId) {
        this.currentRequestId = currentRequestId;
    }

    public String getSelectedCode() {
        return selectedCode;
    }

    public void setSelectedCode(String selectedCode) {
        this.selectedCode = selectedCode;
    }

    public String getSelectedCodeLanguage() {
        return selectedCodeLanguage;
    }

    public void setSelectedCodeLanguage(String selectedCodeLanguage) {
        this.selectedCodeLanguage = selectedCodeLanguage;
    }

    public String getSelectCodeFileFullPath() {
        return selectCodeFileFullPath;
    }

    public void setSelectCodeFileFullPath(String selectCodeFileFullPath) {
        this.selectCodeFileFullPath = selectCodeFileFullPath;
    }

    public List<ChatContext> getChatContexts() {
        return chatContexts;
    }

    public void setChatContexts(List<ChatContext> chatContexts) {
        this.chatContexts = chatContexts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Message message = (Message) o;

        return new EqualsBuilder().append(id, message.id).isEquals();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userMessage,selectedCode);
    }
}
