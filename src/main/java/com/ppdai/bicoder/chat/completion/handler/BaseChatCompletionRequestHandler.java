package com.ppdai.bicoder.chat.completion.handler;

import com.google.gson.Gson;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.ppdai.bicoder.chat.completion.listener.BaseCompletionEventSourceListener;
import com.ppdai.bicoder.chat.completion.listener.CompletionEventListener;
import com.ppdai.bicoder.chat.conversation.Conversation;
import com.ppdai.bicoder.chat.conversation.Message;
import com.ppdai.bicoder.config.BiCoderBundle;
import com.ppdai.bicoder.config.PluginStaticConfig;
import com.ppdai.bicoder.utils.BiCoderLoggerUtils;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSources;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


public abstract class BaseChatCompletionRequestHandler {

    private final StringBuilder messageBuilder = new StringBuilder();
    private SwingWorker<Void, String> swingWorker;
    private EventSource eventSource;
    protected final Project project;
    protected Consumer<String> messageListener;
    protected Consumer<String> errorListener;
    protected Consumer<String> completedListener;

    protected BaseCompletionEventSourceListener completionEventSourceListener;

    private final Runnable cancelListener;

    protected OkHttpClient okHttpClient;

    protected final Gson gson = new Gson();


    public BaseChatCompletionRequestHandler(Project project, BaseCompletionEventSourceListener completionEventSourceListener) {
        okHttpClient();
        this.project = project;
        this.cancelListener = this::cancel;
        this.completionEventSourceListener = completionEventSourceListener;
    }

    public BaseChatCompletionRequestHandler(Project project) {
        this(project, new BaseCompletionEventSourceListener());
    }

    public void setCompletionEventListener(CompletionEventListener completionEventListener) {
        this.completionEventSourceListener.setCompletionEventListener(completionEventListener);
    }

    public void setMessageListener(Consumer<String> messageListener) {
        this.messageListener = messageListener;
    }

    public void setErrorListener(Consumer<String> errorListener) {
        this.errorListener = errorListener;
    }

    public void setCompletedListener(Consumer<String> completedListener) {
        this.completedListener = completedListener;
    }

    private void okHttpClient() {
        OkHttpClient.Builder client = new OkHttpClient.Builder();
        client.connectTimeout(PluginStaticConfig.REQUEST_CONNECT_TIMEOUT, TimeUnit.SECONDS);
        client.writeTimeout(PluginStaticConfig.REQUEST_WRITE_TIMEOUT, TimeUnit.SECONDS);
        client.readTimeout(PluginStaticConfig.REQUEST_READ_TIMEOUT, TimeUnit.SECONDS);
        this.okHttpClient = client.build();
    }


    public void call(Conversation conversation, Message message, boolean isRetry) {
        swingWorker = new CompletionRequestWorker(conversation, message, isRetry);
        swingWorker.execute();
    }

    public void cancel() {
        if (eventSource != null) {
            eventSource.cancel();
        }
        swingWorker.cancel(true);
    }

    private EventSource startCall(
            @NotNull Conversation conversation,
            @NotNull Message message,
            boolean isRetry,
            BaseCompletionEventSourceListener completionEventListener) {
        try {
            String url = getUrl();
            String requestJson = getRequestBody(conversation, message);
            BiCoderLoggerUtils.getInstance(getClass()).info("start sse call, url: " + url + ", requestJson: " + requestJson);
            RequestBody body = RequestBody.create(requestJson, MediaType.parse("application/json; charset=utf-8"));
            return EventSources.createFactory(okHttpClient).newEventSource(
                    new Request.Builder()
                            .url(url)
                            .post(body)
                            .addHeader("Accept", "text/event-stream")
                            .build(),
                    completionEventListener);
        } catch (Throwable t) {
            if (errorListener != null) {
                errorListener.accept(BiCoderBundle.get("chat.error.something.wrong"));
            }
            throw t;
        }
    }

    /**
     * 获取请求body
     *
     * @param conversation 会话
     * @param message      消息
     * @return 请求body
     */
    public abstract String getRequestBody(Conversation conversation, Message message);

    /**
     * 获取请求url
     *
     * @return 请求url
     */
    public abstract String getUrl();

    private String handleCommandMessage(@NotNull Message message) {
        var userMessage = message.getUserMessage();
        var selectedCode = message.getSelectedCode();
        var wholeMessage = new StringBuilder();
        if (StringUtils.isNotBlank(userMessage)) {
            wholeMessage.append(userMessage).append("\n");
        }
        if (StringUtils.isNotBlank(selectedCode)) {
            wholeMessage.append(selectedCode);
        }
        return wholeMessage.toString();
    }

    private class CompletionRequestWorker extends SwingWorker<Void, String> {

        private final Conversation conversation;
        private final Message message;
        private final boolean isRetry;

        private final Consumer<String> publishListener = this::publish;

        public CompletionRequestWorker(Conversation conversation, Message message, boolean isRetry) {
            this.conversation = conversation;
            this.message = message;
            this.isRetry = isRetry;
        }

        @Override
        protected Void doInBackground() {

            try {
                ApplicationManager.getApplication().invokeLater(
                        () -> {
                            completionEventSourceListener.setCompletionEventListener(
                                    new CompletionEventListener(
                                            completedListener,
                                            errorListener,
                                            publishListener,
                                            cancelListener));
                            eventSource = startCall(
                                    conversation,
                                    message,
                                    isRetry, completionEventSourceListener);
                        });
            } catch (Exception e) {
                if (errorListener != null) {
                    errorListener.accept(BiCoderBundle.get("chat.error.something.wrong"));
                }
                BiCoderLoggerUtils.getInstance(getClass()).warn("CompletionRequestWorker doInBackground error", e);
            } finally {
                BiCoderLoggerUtils.getInstance(getClass()).info("CompletionRequestWorker.doInBackground");
            }
            return null;
        }

        @Override
        protected void process(List<String> chunks) {
            message.setResponse(messageBuilder.toString());
            for (String text : chunks) {
                messageBuilder.append(text);
                if (messageListener != null) {
                    messageListener.accept(text);
                }
            }
        }

    }

}
