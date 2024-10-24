package com.ppdai.bicoder.model;

/**
 * 补全响应对象
 */
public class BiCoderCompletionResponse {

    private Integer code;
    private String message;
    private Data data;

    public BiCoderCompletionResponse() {
    }

    public BiCoderCompletionResponse(int code, String message, Data data) {
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

    public String getText() {
        return data.getCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BiCoderCompletionResponse{");
        sb.append("code=").append(code);
        sb.append(", message='").append(message).append('\'');
        sb.append(", data=").append(data);
        sb.append('}');
        return sb.toString();
    }
}

class Data {
    private String code;

    public Data() {
    }

    public Data(String code) {
        this.code = code;
    }


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Data{");
        sb.append("code='").append(code).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
