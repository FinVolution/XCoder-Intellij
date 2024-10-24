package com.ppdai.bicoder.chat.model;


public class CodeFuncStatusRequest {

    private String acceptStatus;
    private String conversationUUID;

    public CodeFuncStatusRequest() {
    }

    public CodeFuncStatusRequest(String acceptStatus, String conversationUUID) {
        this.acceptStatus = acceptStatus;
        this.conversationUUID = conversationUUID;
    }

    public String getAcceptStatus() {
        return acceptStatus;
    }

    public void setAcceptStatus(String acceptStatus) {
        this.acceptStatus = acceptStatus;
    }

    public String getConversationUUID() {
        return conversationUUID;
    }

    public void setConversationUUID(String conversationUUID) {
        this.conversationUUID = conversationUUID;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CodeFuncStatusRequest{");
        sb.append("acceptStatus='").append(acceptStatus).append('\'');
        sb.append(", conversationUUID='").append(conversationUUID).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
