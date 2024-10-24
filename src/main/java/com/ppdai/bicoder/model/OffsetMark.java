package com.ppdai.bicoder.model;

/**
 * 代码索引标记
 *
 */
public class OffsetMark {

    private String id;

    private int startOffset;

    private int endOffset;

    private String completionId;

    private String code;

    public OffsetMark() {
    }

    public OffsetMark(String id, int startOffset, int endOffset, String completionId, String code) {
        this.id = id;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.completionId = completionId;
        this.code = code;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getCompletionId() {
        return completionId;
    }

    public void setCompletionId(String completionId) {
        this.completionId = completionId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
