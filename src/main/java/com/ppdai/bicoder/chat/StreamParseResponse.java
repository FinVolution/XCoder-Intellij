package com.ppdai.bicoder.chat;

public class StreamParseResponse {

    private final StreamResponseType type;
    private final String response;
    private final boolean isTwiceProcessingCode;


    public StreamParseResponse(StreamResponseType type, String response, boolean isTwiceProcessingCode) {
        this.type = type;
        this.response = response;
        this.isTwiceProcessingCode = isTwiceProcessingCode;

    }

    public StreamResponseType getType() {
        return type;
    }

    public String getResponse() {
        return response;
    }

    public boolean isTwiceProcessingCode() {
        return isTwiceProcessingCode;
    }
}
