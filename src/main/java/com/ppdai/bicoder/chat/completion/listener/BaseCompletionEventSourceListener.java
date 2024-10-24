package com.ppdai.bicoder.chat.completion.listener;

import com.google.gson.Gson;
import com.ppdai.bicoder.chat.model.*;
import com.ppdai.bicoder.config.BiCoderBundle;
import com.ppdai.bicoder.utils.BiCoderLoggerUtils;
import okhttp3.*;
import okhttp3.internal.http2.StreamResetException;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaseCompletionEventSourceListener extends EventSourceListener {

    private CompletionEventListener listeners;
    private final StringBuilder messageBuilder = new StringBuilder();
    private  boolean retryOnReadTimeout;
    private  Consumer<String> onRetry;

    private final Gson gson = new Gson();

    public BaseCompletionEventSourceListener(CompletionEventListener listeners) {
        this(listeners, false, null);
    }

    public BaseCompletionEventSourceListener(CompletionEventListener listeners, boolean retryOnReadTimeout, Consumer<String> onRetry) {
        this.listeners = listeners;
        this.retryOnReadTimeout = retryOnReadTimeout;
        this.onRetry = onRetry;
    }

    public BaseCompletionEventSourceListener() {

    }


    @Override
    public void onOpen(@NotNull EventSource eventSource, @NotNull Response response) {
        BiCoderLoggerUtils.getInstance(getClass()).info("Request opened.");
    }

    @Override
    public void onClosed(@NotNull EventSource eventSource) {
        BiCoderLoggerUtils.getInstance(getClass()).info("Request closed.");
        listeners.onComplete(messageBuilder.toString());
    }

    @Override
    public void onEvent(
            @NotNull EventSource eventSource,
            String id,
            String type,
            @NotNull String data) {
        ChatCompletionResponse responseData = gson.fromJson(data, ChatCompletionResponse.class);
        Integer code = responseData.getCode();
        if (!Integer.valueOf(0).equals(code)) {
            BiCoderLoggerUtils.getInstance(getClass()).warn(responseData.getMessage());
            //主动申请断开,避免服务端没有释放时卡死
            listeners.onCancel();
            listeners.onError(BiCoderBundle.get("chat.error.request.failed"));
            return;
        }
        var dataString = responseData.getData();
        // 有可能不是关闭事件，而是给结束标识,联调后会触发complete
        if ("[DONE]".equals(dataString)) {
//                    ChatCompletionRequestHandler.this.cancel();
            BiCoderLoggerUtils.getInstance(getClass()).info("Text transfer complete.");
//                    listeners.onComplete(messageBuilder);
            return;
        }
        if (StringUtils.isNotEmpty(dataString)) {
            messageBuilder.append(dataString);
            listeners.onMessage(dataString);
        }
    }

    @Override
    public void onFailure(
            @NotNull EventSource eventSource,
            Throwable throwable,
            Response response) {
        if (throwable instanceof SocketTimeoutException) {
            if (retryOnReadTimeout) {
                BiCoderLoggerUtils.getInstance(getClass()).info("Retrying request.");
                onRetry.accept(messageBuilder.toString());
                return;
            }
            //主动申请断开,避免服务端没有释放时卡死
            listeners.onCancel();
            listeners.onError(BiCoderBundle.get("chat.error.request.timeout"));
            return;
        }
        if (throwable instanceof StreamResetException || throwable instanceof SocketException) {
            BiCoderLoggerUtils.getInstance(getClass()).info("Stream was cancelled");
            listeners.onComplete(messageBuilder.toString());
            return;
        }

        String returnErrMsg = BiCoderBundle.get("chat.error.request.failed");
        try {
            if (response == null) {
                BiCoderLoggerUtils.getInstance(getClass()).warn("chat Response is null");
            } else {
                var body = response.body();
                if (body != null) {
                    String jsonBody = body.string();
                    jsonBody = extractResponse(jsonBody);
                    ChatCompletionResponse responseData = gson.fromJson(jsonBody, ChatCompletionResponse.class);
                    BiCoderLoggerUtils.getInstance(getClass()).warn("Request failed,errorMsg:" + responseData.getMessage());
                    if (Integer.valueOf(400002).equals(responseData.getCode())) {
                        returnErrMsg = BiCoderBundle.get("chat.user.input.contains.sensitive.information");
                    }
                } else {
                    BiCoderLoggerUtils.getInstance(getClass()).warn("chat Response Body is null");
                }
            }
        } catch (Exception ex) {
            BiCoderLoggerUtils.getInstance(getClass()).warn("Error while reading chat response body", ex);
        } finally {
            //主动申请断开,避免服务端没有释放时卡死
            listeners.onCancel();
            listeners.onError(returnErrMsg);
        }
    }

    private String extractResponse(String data) {
        String pattern = "data: (.*?)(?=\n\n)";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(data);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    public void setCompletionEventListener(CompletionEventListener completionEventListener) {
        this.listeners = completionEventListener;
    }
}