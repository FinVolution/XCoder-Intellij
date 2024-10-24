package com.ppdai.bicoder.model;

/**
 * 补全请求状态对象
 */
public class BiCoderCompletionStatusRequest {

    /**
     * 唯一id
     */
    private String generateUUID;

    /**
     * 接收状态
     */
    private String acceptStatus;

    public BiCoderCompletionStatusRequest() {
    }

    public BiCoderCompletionStatusRequest(String generateUUID, String acceptStatus) {
        this.generateUUID = generateUUID;
        this.acceptStatus = acceptStatus;
    }

    public String getGenerateUUID() {
        return generateUUID;
    }

    public void setGenerateUUID(String generateUUID) {
        this.generateUUID = generateUUID;
    }

    public String getAcceptStatus() {
        return acceptStatus;
    }

    public void setAcceptStatus(String acceptStatus) {
        this.acceptStatus = acceptStatus;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BiCoderCompletionStatusRequest{");
        sb.append("generateUUID='").append(generateUUID).append('\'');
        sb.append(", acceptStatus='").append(acceptStatus).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
