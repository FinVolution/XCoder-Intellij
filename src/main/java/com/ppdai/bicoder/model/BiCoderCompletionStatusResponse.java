package com.ppdai.bicoder.model;

/**
 * 补全响应对象
 */
public class BiCoderCompletionStatusResponse {

    private Integer code;
    private String message;
    private StatusData data;

    public BiCoderCompletionStatusResponse() {
    }

    public BiCoderCompletionStatusResponse(int code, String message, StatusData data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public StatusData getData() {
        return data;
    }

    public void setData(StatusData data) {
        this.data = data;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BiCoderCompletionStatusResponse{");
        sb.append("code=").append(code);
        sb.append(", message='").append(message).append('\'');
        sb.append(", data=").append(data);
        sb.append('}');
        return sb.toString();
    }
}

class StatusData {
    private String generateUUID;

    public StatusData() {
    }

    public StatusData(String generateUUID) {
        this.generateUUID = generateUUID;
    }

    public String getGenerateUUID() {
        return generateUUID;
    }

    public void setGenerateUUID(String generateUUID) {
        this.generateUUID = generateUUID;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("StatusData{");
        sb.append("generateUUID='").append(generateUUID).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
