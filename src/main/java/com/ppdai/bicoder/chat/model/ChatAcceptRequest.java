package com.ppdai.bicoder.chat.model;

/**
 * chat接收请求对象
 *
 */
public class ChatAcceptRequest {
    private String acceptStatus;
    private String conversationUUID;
    private int completionCodeLines;

    public ChatAcceptRequest() {
    }

    public ChatAcceptRequest(String acceptStatus, String conversationUUID, int completionCodeLines) {
        this.acceptStatus = acceptStatus;
        this.conversationUUID = conversationUUID;
        this.completionCodeLines = completionCodeLines;
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

    public int getCompletionCodeLines() {
        return completionCodeLines;
    }

    public void setCompletionCodeLines(int completionCodeLines) {
        this.completionCodeLines = completionCodeLines;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ChatAcceptRequest{");
        sb.append("acceptStatus='").append(acceptStatus).append('\'');
        sb.append(", conversationUUID='").append(conversationUUID).append('\'');
        sb.append(", completionCodeLines=").append(completionCodeLines);
        sb.append('}');
        return sb.toString();
    }
}
