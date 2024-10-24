package com.ppdai.bicoder.chat.completion.listener;

import com.ppdai.bicoder.utils.BiCoderLoggerUtils;

import java.util.function.Consumer;

/**
 * chat处理listener整合
 *
 */
public class CompletionEventListener {

    private Consumer<String> completedListener;

    private Consumer<String> errorListener;

    private final Runnable cancelListener;

    private final Consumer<String> publishListener;

    public CompletionEventListener(Consumer<String> completedListener, Consumer<String> errorListener, Consumer<String> publishListener, Runnable cancelListener) {
        this.completedListener = completedListener;
        this.errorListener = errorListener;
        this.publishListener = publishListener;
        this.cancelListener = cancelListener;
    }

    public void onMessage(String message) {
        if (publishListener != null) {
            publishListener.accept(message);
        }
    }

    public void onComplete(String completeMessage) {
        if (completedListener != null) {
            completedListener.accept(completeMessage);
        }
    }

    public void onError(String errorMsg) {
        try {
            if (errorListener != null) {
                errorListener.accept(errorMsg);
            }
        } finally {
            BiCoderLoggerUtils.getInstance(getClass()).warn("get chat completion failure,errorMsg:" + errorMsg);
        }
    }

    public void onCancel() {
        if (cancelListener != null) {
            cancelListener.run();
        }
    }


    public void setErrorListener(Consumer<String> errorListener) {
        this.errorListener = errorListener;
    }

    public void setCompletedListener(Consumer<String> completedListener) {
        this.completedListener = completedListener;
    }
}