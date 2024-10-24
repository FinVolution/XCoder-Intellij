package com.ppdai.bicoder.chat.model;

public class ChatAcceptResponse {
    private Integer code;
    private String message;
    private ChatStatusData data;

    public ChatAcceptResponse() {
    }

    public ChatAcceptResponse(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ChatStatusData getData() {
        return data;
    }

    public void setData(ChatStatusData data) {
        this.data = data;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ChatAcceptResponse{");
        sb.append("code=").append(code);
        sb.append(", message='").append(message).append('\'');
        sb.append(", data=").append(data);
        sb.append('}');
        return sb.toString();
    }
}

class ChatStatusData {
    private String conversationUUID;

    public ChatStatusData() {
    }

    public ChatStatusData(String conversationUUID) {
        this.conversationUUID = conversationUUID;
    }

    public String getConversationUUID() {
        return conversationUUID;
    }

    public void setConversationUUID(String conversationUUID) {
        this.conversationUUID = conversationUUID;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ChatStatusData{");
        sb.append("conversationUUID='").append(conversationUUID).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
