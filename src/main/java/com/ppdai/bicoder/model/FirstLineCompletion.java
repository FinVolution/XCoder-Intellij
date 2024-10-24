package com.ppdai.bicoder.model;

/**
 * 记录渲染内容和渲染位置
 *
 */
public class FirstLineCompletion {
    private int startPosition;
    private String insertText;

    public FirstLineCompletion(int startPosition, String insertText) {
        this.startPosition = startPosition;
        this.insertText = insertText;
    }

    public int getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(int startPosition) {
        this.startPosition = startPosition;
    }

    public String getInsertText() {
        return insertText;
    }

    public void setInsertText(String insertText) {
        this.insertText = insertText;
    }
}