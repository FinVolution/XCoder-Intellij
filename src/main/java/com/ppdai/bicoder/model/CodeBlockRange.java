package com.ppdai.bicoder.model;

/**
 * 代码块范围
 */
public class CodeBlockRange {
    private int startOffset;

    private int endOffset;

    public CodeBlockRange() {
    }

    public CodeBlockRange(int startOffset, int endOffset) {
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    public int getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(int startOffset) {
        this.startOffset = startOffset;
    }

    public int getEndOffset() {
        return endOffset;
    }

    public void setEndOffset(int endOffset) {
        this.endOffset = endOffset;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CodeBlockRange{");
        sb.append("startOffset=").append(startOffset);
        sb.append(", endOffset=").append(endOffset);
        sb.append('}');
        return sb.toString();
    }
}
