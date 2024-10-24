package com.ppdai.bicoder.model;

/**
 * 补全请求状态对象
 */
public class BiCoderCompletionRetainedInformationRequest {

    /**
     * 唯一id
     */
    private String generateUUID;

    /**
     * 留存状态
     */
    private String latestRemainStatus;

    /**
     * 留存时间
     */
    private long remainCheckTimeInterval;

    /**
     * 相似度
     */
    private float remainSimilarity;

    /**
     * 当前索引
     */
    private int remainCursorIdx;

    public BiCoderCompletionRetainedInformationRequest() {
    }

    public BiCoderCompletionRetainedInformationRequest(String generateUUID, String latestRemainStatus, long remainCheckTimeInterval, float remainSimilarity, int remainCursorIdx) {
        this.generateUUID = generateUUID;
        this.latestRemainStatus = latestRemainStatus;
        this.remainCheckTimeInterval = remainCheckTimeInterval;
        this.remainSimilarity = remainSimilarity;
        this.remainCursorIdx = remainCursorIdx;
    }

    public String getGenerateUUID() {
        return generateUUID;
    }

    public void setGenerateUUID(String generateUUID) {
        this.generateUUID = generateUUID;
    }

    public String getLatestRemainStatus() {
        return latestRemainStatus;
    }

    public void setLatestRemainStatus(String latestRemainStatus) {
        this.latestRemainStatus = latestRemainStatus;
    }

    public long getRemainCheckTimeInterval() {
        return remainCheckTimeInterval;
    }

    public void setRemainCheckTimeInterval(long remainCheckTimeInterval) {
        this.remainCheckTimeInterval = remainCheckTimeInterval;
    }

    public float getRemainSimilarity() {
        return remainSimilarity;
    }

    public void setRemainSimilarity(float remainSimilarity) {
        this.remainSimilarity = remainSimilarity;
    }

    public int getRemainCursorIdx() {
        return remainCursorIdx;
    }

    public void setRemainCursorIdx(int remainCursorIdx) {
        this.remainCursorIdx = remainCursorIdx;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BiCoderCompletionRetainedInformationRequest{");
        sb.append("generateUUID='").append(generateUUID).append('\'');
        sb.append(", latestRemainStatus='").append(latestRemainStatus).append('\'');
        sb.append(", remainCheckTimeInterval=").append(remainCheckTimeInterval);
        sb.append(", remainSimilarity=").append(remainSimilarity);
        sb.append(", remainCursorIdx=").append(remainCursorIdx);
        sb.append('}');
        return sb.toString();
    }
}
