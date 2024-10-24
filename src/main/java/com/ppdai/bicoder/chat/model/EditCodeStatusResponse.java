package com.ppdai.bicoder.chat.model;

public class EditCodeStatusResponse {
    private Integer code;
    private String message;
    private EditCodeStatusData data;

    public EditCodeStatusResponse() {
    }

    public EditCodeStatusResponse(Integer code) {
        this.code = code;
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

    public EditCodeStatusData getData() {
        return data;
    }

    public void setData(EditCodeStatusData data) {
        this.data = data;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EditCodeStatusResponse{");
        sb.append("code=").append(code);
        sb.append(", message='").append(message).append('\'');
        sb.append(", data=").append(data);
        sb.append('}');
        return sb.toString();
    }
}

class EditCodeStatusData {
    private String conversationUUID;

    public EditCodeStatusData() {
    }

    public EditCodeStatusData(String conversationUUID) {
        this.conversationUUID = conversationUUID;
    }

    public String getConversationUUID() {
        return conversationUUID;
    }

    public void setConversationUUID(String conversationUUID) {
        this.conversationUUID = conversationUUID;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EditCodeStatusData{");
        sb.append("conversationUUID='").append(conversationUUID).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
