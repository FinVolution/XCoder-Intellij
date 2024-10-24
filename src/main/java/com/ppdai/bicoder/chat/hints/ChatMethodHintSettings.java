package com.ppdai.bicoder.chat.hints;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ChatMethodHintSettings {
    private String setting;

    public ChatMethodHintSettings() {
    }

    public ChatMethodHintSettings(String setting) {
        this.setting = setting;
    }

    public String getSetting() {
        return setting;
    }

    public void setSetting(String setting) {
        this.setting = setting;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChatMethodHintSettings that = (ChatMethodHintSettings) o;
        return new EqualsBuilder().append(setting, that.setting).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(setting).toHashCode();
    }
}