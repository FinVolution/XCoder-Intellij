package com.ppdai.bicoder.chat.constant;

import com.ppdai.bicoder.config.BiCoderBundle;

/**
 * 消息类型
 *
 */
public enum MessageType {
    CHAT("chat", ""),
    TESTS("tests", BiCoderBundle.get("chat.action.generate.tests.action.title.Cn")),
    EXPLAIN("explain", BiCoderBundle.get("chat.action.start.inline.explain.code.action.title")),
    DOC("doc", BiCoderBundle.get("chat.action.start.inline.doc.code.action.title")),
    OPTIMIZE("optimize", BiCoderBundle.get("chat.action.start.inline.optimize.code.action.title")),
    EDIT("edit", BiCoderBundle.get("chat.action.edit.code.action.title.Cn"));

    private final String type;
    private final String cnName;

    MessageType(String type, String cnName) {
        this.type = type;
        this.cnName = cnName;
    }

    public String getType() {
        return type;
    }

    public String getCnName() {
        return cnName;
    }

    public static String getCnNameByType(String type) {
        for (MessageType messageType : MessageType.values()) {
            if (messageType.getType().equals(type)) {
                return messageType.getCnName();
            }
        }
        return "";
    }
}