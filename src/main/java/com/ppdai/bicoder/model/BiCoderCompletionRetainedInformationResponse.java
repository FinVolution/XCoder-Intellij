package com.ppdai.bicoder.model;

/**
 * 补全响应对象
 */
public class BiCoderCompletionRetainedInformationResponse {

    private Integer code;
    private String message;
    private Data data;

    public BiCoderCompletionRetainedInformationResponse() {
    }

    public BiCoderCompletionRetainedInformationResponse(int code, String message, Data data) {
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

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BiCoderCompletionRetainedInformationResponse{");
        sb.append("code=").append(code);
        sb.append(", message='").append(message).append('\'');
        sb.append(", data=").append(data);
        sb.append('}');
        return sb.toString();
    }
}

class RetainedInformationData {
    private Integer codeRemainRecordsID;

    public RetainedInformationData() {
    }

    public RetainedInformationData(Integer codeRemainRecordsID) {
        this.codeRemainRecordsID = codeRemainRecordsID;
    }

    public Integer getCodeRemainRecordsID() {
        return codeRemainRecordsID;
    }

    public void setCodeRemainRecordsID(Integer codeRemainRecordsID) {
        this.codeRemainRecordsID = codeRemainRecordsID;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RetainedInformationData{");
        sb.append("codeRemainRecordsID=").append(codeRemainRecordsID);
        sb.append('}');
        return sb.toString();
    }
}
