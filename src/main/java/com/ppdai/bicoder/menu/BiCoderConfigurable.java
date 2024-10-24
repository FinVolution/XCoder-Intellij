package com.ppdai.bicoder.menu;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.util.NlsContexts.ConfigurableName;
import com.ppdai.bicoder.config.PluginStaticConfig;
import com.ppdai.bicoder.config.UserSetting;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Set;

/**
 * 插件配置页
 *
 */
public class BiCoderConfigurable implements Configurable {

    private final BiCoderSettingsForm form = new BiCoderSettingsForm();

    public BiCoderConfigurable() {
    }

    @Override
    @ConfigurableName
    public String getDisplayName() {
        return PluginStaticConfig.PLUGIN_NAME;
    }

    @Override
    public void reset() {
        this.form.setDisabledLanguages(UserSetting.getInstance().getDisableFileTypeList());
    }

    @Override
    public boolean isModified() {
        UserSetting settings = UserSetting.getInstance();
        return !requiresSettingsNotification(settings);

    }

    @Override
    public void apply() {
        UserSetting settings = UserSetting.getInstance();
        settings.setDisableFileTypeList(this.form.getDisabledLanguages());
        settings.setRequestHost(this.form.getRequestHost());
        settings.setEnableCache(this.form.getEnableCache());
    }


    private boolean requiresSettingsNotification(UserSetting settings) {
        String requestHost = settings.getRequestHost();
        boolean enableCache = settings.getEnableCache();
        Set<String> disableFileTypeList = settings.getDisableFileTypeList();
        return this.form.getDisabledLanguages().equals(disableFileTypeList)
                && StringUtils.equals(requestHost, this.form.getRequestHost())
                && enableCache == this.form.getEnableCache();
    }

    BiCoderSettingsForm getForm() {
        return this.form;
    }

    @Override
    public @Nullable
    JComponent createComponent() {
        return this.form.getPanel();
    }

}