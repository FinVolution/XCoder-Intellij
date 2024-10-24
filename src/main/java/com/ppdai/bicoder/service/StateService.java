package com.ppdai.bicoder.service;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.messages.MessageBus;
import com.ppdai.bicoder.config.UserSetting;
import com.ppdai.bicoder.model.PluginState;
import com.ppdai.bicoder.notifier.BinaryStateChangeNotifier;
import com.ppdai.bicoder.notifier.LanguageStateChangeNotifier;

/**
 * 状态服务
 * 1.是否启用插件
 * 2。是否请求中
 *
 */
public class StateService {
    private final MessageBus messageBus;

    private PluginState lastState = new PluginState(UserSetting.getInstance().getEnablePlugin(), false);

    public StateService() {
        this.messageBus = ApplicationManager.getApplication().getMessageBus();
    }

    /**
     * 更新状态,通知消息总线状态变化
     *
     * @param state 状态
     */
    public void updateState(PluginState state) {
        if (state != null) {
            if (!state.equals(this.lastState)) {
                this.messageBus
                        .syncPublisher(BinaryStateChangeNotifier.STATE_CHANGED_TOPIC)
                        .stateChanged(state);
            }
            this.lastState = state;
        }
    }


    public void updateLanguageState(boolean state) {
        this.messageBus
                .syncPublisher(LanguageStateChangeNotifier.LANGUAGE_STATE_CHANGED_TOPIC)
                .languageStateChanged(state);
    }


    public void updateEnableState(boolean isEnabled) {
        PluginState state = new PluginState(getLastState().isEnablePlugin(), getLastState().isLoading());
        state.setEnablePlugin(isEnabled);
        updateState(state);
    }

    public void updateLoadingState(boolean isLoading) {
        PluginState state = new PluginState(getLastState().isEnablePlugin(), getLastState().isLoading());
        state.setLoading(isLoading);
        updateState(state);
    }

    /**
     * 获取最后一次状态
     *
     * @return 最后一次状态
     */
    public PluginState getLastState() {
        return this.lastState;
    }


}
