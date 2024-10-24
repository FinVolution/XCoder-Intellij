package com.ppdai.bicoder.notifier;

import com.intellij.util.messages.Topic;
import com.ppdai.bicoder.model.PluginState;

/**
 * 状态变更通知器
 *
 */
public interface BinaryStateChangeNotifier {
    Topic<BinaryStateChangeNotifier> STATE_CHANGED_TOPIC =
            Topic.create("State Changed Notifier", BinaryStateChangeNotifier.class);

    void stateChanged(PluginState state);

}