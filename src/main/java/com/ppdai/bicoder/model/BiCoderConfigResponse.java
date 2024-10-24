package com.ppdai.bicoder.model;

/**
 * 配置响应对象
 *
 */
public class BiCoderConfigResponse {

    private Integer code;
    private String message;
    private ConfigData data;

    public BiCoderConfigResponse() {
    }

    public BiCoderConfigResponse(int code, String message, ConfigData data) {
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

    public ConfigData getData() {
        return data;
    }

    public void setData(ConfigData data) {
        this.data = data;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BiCoderConfigResponse{");
        sb.append("code=").append(code);
        sb.append(", message='").append(message).append('\'');
        sb.append(", data=").append(data);
        sb.append('}');
        return sb.toString();
    }
}


