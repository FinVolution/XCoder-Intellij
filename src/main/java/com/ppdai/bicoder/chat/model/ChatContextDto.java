package com.ppdai.bicoder.chat.model;

/**
 * chat功能上下文dto
 *
 */
public class ChatContextDto {
    private String type;
    private String content;
    private String path;

    public ChatContextDto() {
    }

    public ChatContextDto(String type, String content, String path) {
        this.type = type;
        this.content = content;
        this.path = path;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ChatContextDto{");
        sb.append("type='").append(type).append('\'');
        sb.append(", content='").append(content).append('\'');
        sb.append(", path='").append(path).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
