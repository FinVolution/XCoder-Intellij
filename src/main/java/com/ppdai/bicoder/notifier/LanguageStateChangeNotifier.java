package com.ppdai.bicoder.notifier;

import com.intellij.util.messages.Topic;

/**
 * 仅语言禁用状态变更通知器
 *
 */
public interface LanguageStateChangeNotifier {
    Topic<LanguageStateChangeNotifier> LANGUAGE_STATE_CHANGED_TOPIC =
            Topic.create("Language State Changed Notifier", LanguageStateChangeNotifier.class);

    void languageStateChanged(boolean state);

}