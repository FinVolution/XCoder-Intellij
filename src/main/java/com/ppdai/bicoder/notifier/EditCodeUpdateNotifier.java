package com.ppdai.bicoder.notifier;

import com.intellij.util.messages.Topic;

/**
 * 编辑代码更新通知器
 *
 */
public interface EditCodeUpdateNotifier {
    Topic<EditCodeUpdateNotifier> EDIT_CODE_UPDATE_TOPIC =
            Topic.create("Edit Code Update Notifier", EditCodeUpdateNotifier.class);

    /**
     * 代码更新
     *
     * @param code 代码
     */
    void codeUpdate(String code);

}