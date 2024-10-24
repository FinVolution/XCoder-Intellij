package com.ppdai.bicoder.chat.model;

import java.util.List;

public class ReferenceLinkResponse {
    private Integer code;
    private String message;
    private ReferenceLinkData data;

    public ReferenceLinkResponse() {
    }

    public ReferenceLinkResponse(Integer code) {
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

    public ReferenceLinkData getData() {
        return data;
    }

    public void setData(ReferenceLinkData data) {
        this.data = data;
    }

    public List<String> getReferenceUrls() {
        return data.getReferenceUrls();
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ReferenceLinkResponse{");
        sb.append("code=").append(code);
        sb.append(", message='").append(message).append('\'');
        sb.append(", data=").append(data);
        sb.append('}');
        return sb.toString();
    }
}

class ReferenceLinkData {
    private String conversationUUID;

    private List<String> referenceUrls;

    public ReferenceLinkData() {
    }

    public ReferenceLinkData(String conversationUUID) {
        this.conversationUUID = conversationUUID;
    }

    public String getConversationUUID() {
        return conversationUUID;
    }

    public void setConversationUUID(String conversationUUID) {
        this.conversationUUID = conversationUUID;
    }


    public List<String> getReferenceUrls() {
        return referenceUrls;
    }

    public void setReferenceUrls(List<String> referenceUrls) {
        this.referenceUrls = referenceUrls;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ReferenceLinkData{");
        sb.append("conversationUUID='").append(conversationUUID).append('\'');
        sb.append(", referenceUrls=").append(referenceUrls);
        sb.append('}');
        return sb.toString();
    }
}
