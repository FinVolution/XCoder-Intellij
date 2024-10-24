package com.ppdai.bicoder.chat.model;


public class CommonChatMessageDto {

    private String content;

    private String role;

    public CommonChatMessageDto() {
    }

    public CommonChatMessageDto(String role, String content) {
        this.content = content;
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MessageDto{");
        sb.append("content='").append(content).append('\'');
        sb.append(", role='").append(role).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
