package com.ppdai.bicoder.chat;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StreamParser {

    private static final String CODE_BLOCK_STARTING_REGEX = "```[a-zA-Z]*\n";
    private final StringBuilder messageBuilder = new StringBuilder();
    private boolean isProcessingCode;
    /**
     * 是否有正在处理的代码块
     */
    private boolean hasProcessingCode;

    /**
     * 是否是第二次以上处理代码块
     */
    private boolean isTwiceProcessingCode;

    /**
     * 第一个处理的代码块
     */
    private String firstProcessCode;


    public List<StreamParseResponse> parse(String message) {
        messageBuilder.append(message);

        Pattern pattern = Pattern.compile(CODE_BLOCK_STARTING_REGEX);
        Matcher matcher = pattern.matcher(messageBuilder.toString());
        if (!isProcessingCode && matcher.find()) {
            isProcessingCode = true;
            if (hasProcessingCode) {
                isTwiceProcessingCode = true;
            }
            hasProcessingCode = true;

            var startingIndex = messageBuilder.indexOf(matcher.group());
            var prevMessage = messageBuilder.substring(0, startingIndex);
            messageBuilder.delete(0, messageBuilder.indexOf(matcher.group()));

            return List.of(
                    new StreamParseResponse(StreamResponseType.TEXT, prevMessage, isTwiceProcessingCode),
                    new StreamParseResponse(StreamResponseType.CODE, messageBuilder.toString(), isTwiceProcessingCode));
        }

        var endingIndex = messageBuilder.indexOf("```\n", 1);
        if (isProcessingCode && endingIndex > 0) {
            isProcessingCode = false;

            var codeResponse = messageBuilder.substring(0, endingIndex + 3);
            if (!isTwiceProcessingCode) {
                firstProcessCode = codeResponse;
            }
            messageBuilder.delete(0, endingIndex + 3);

            return List.of(
                    new StreamParseResponse(StreamResponseType.CODE, codeResponse, isTwiceProcessingCode),
                    new StreamParseResponse(StreamResponseType.TEXT, messageBuilder.toString(), isTwiceProcessingCode));
        }

        return List.of(new StreamParseResponse(isProcessingCode ? StreamResponseType.CODE : StreamResponseType.TEXT, messageBuilder.toString(), isTwiceProcessingCode));
    }

    public void clear() {
        messageBuilder.setLength(0);
        isProcessingCode = false;
        hasProcessingCode = false;
        isTwiceProcessingCode = false;
    }

    public String getFirstProcessCode() {
        return firstProcessCode;
    }
}
