package com.ppdai.bicoder.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * 插件状态
 *
 */
public class PluginState {
    private boolean isEnablePlugin;
    private boolean isLoading;

    public PluginState() {
    }

    public PluginState(boolean isEnablePlugin, boolean isLoading) {
        this.isEnablePlugin = isEnablePlugin;
        this.isLoading = isLoading;
    }

    public boolean isEnablePlugin() {
        return isEnablePlugin;
    }

    public void setEnablePlugin(boolean enablePlugin) {
        isEnablePlugin = enablePlugin;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PluginState that = (PluginState) o;

        return new EqualsBuilder().append(isEnablePlugin, that.isEnablePlugin).append(isLoading, that.isLoading).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(isEnablePlugin).append(isLoading).toHashCode();
    }
}