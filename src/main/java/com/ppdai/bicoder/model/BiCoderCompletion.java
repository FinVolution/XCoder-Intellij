package com.ppdai.bicoder.model;

/**
 * 补全结果对象
 */
public class BiCoderCompletion {

    private String completionId;

    private String text;

    public BiCoderCompletion() {
    }

    public BiCoderCompletion(String completionId, String text) {
        this.completionId = completionId;
        this.text = text;
    }

    public String getCompletionId() {
        return completionId;
    }

    public void setCompletionId(String completionId) {
        this.completionId = completionId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BiCoderCompletion{");
        sb.append("completionId='").append(completionId).append('\'');
        sb.append(", text='").append(text).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
