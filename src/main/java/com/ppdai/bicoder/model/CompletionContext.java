package com.ppdai.bicoder.model;

/**
 * 代码补全上下文
 */
public class CompletionContext {
    private String type;
    private String path;
    private String content;

    public CompletionContext() {
    }

    public CompletionContext(String type, String path, String content) {
        this.type = type;
        this.path = path;
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CompletionContext{");
        sb.append("type='").append(type).append('\'');
        sb.append(", path='").append(path).append('\'');
        sb.append(", content='").append(content).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
