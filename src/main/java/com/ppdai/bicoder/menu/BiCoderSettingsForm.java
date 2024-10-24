package com.ppdai.bicoder.menu;

import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBDimension;
import com.ppdai.bicoder.config.BiCoderBundle;
import com.ppdai.bicoder.config.PluginStaticConfig;
import com.ppdai.bicoder.config.UserSetting;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

/**
 * 插件配置页
 *
 */
public class BiCoderSettingsForm {

    private JPanel panel;

    private JPanel commonSettings;

    private JPanel languagesGroup;

    private JBLabel disabledLanguagesLabel;

    private LanguageTable languageTable;

    private JCheckBox isEnableCache;

    private JTextField requestHost;

    public BiCoderSettingsForm() {
        this.initCommonSetting();
        this.initLanguageGroup();
        this.panel = FormBuilder.createFormBuilder()
                .addComponent(this.commonSettings)
                .addComponent(this.languagesGroup)
                .getPanel();
        this.panel.setLayout(new BoxLayout(this.panel, BoxLayout.Y_AXIS));
    }

    public JPanel getPanel() {
        return this.panel;
    }

    private void applyGroupLayout(JPanel group) {
        group.setBorder(IdeBorderFactory.createTitledBorder(BiCoderBundle.get("plugin.setting.language.title"), false));
    }

    public void setDisabledLanguages(@NotNull Set<String> languages) {
        this.languageTable.setDisabledLanguages(languages);
    }


    private void initLanguageGroup() {
        this.languageTable = new LanguageTable();
        this.languageTable.initItems(UserSetting.getInstance().getDisableFileTypeList());
        this.disabledLanguagesLabel = new JBLabel();
        this.disabledLanguagesLabel.setText(BiCoderBundle.get("plugin.setting.language.group.title"));
        this.disabledLanguagesLabel.setMaximumSize(new JBDimension(Integer.MAX_VALUE, disabledLanguagesLabel.getPreferredSize().height));
        this.disabledLanguagesLabel.setMinimumSize(new JBDimension(Integer.MAX_VALUE, disabledLanguagesLabel.getPreferredSize().height));
        this.disabledLanguagesLabel.setHorizontalTextPosition(SwingConstants.LEFT);
        this.disabledLanguagesLabel.setHorizontalAlignment(SwingConstants.LEFT);

        this.languagesGroup = new JPanel();
        this.applyGroupLayout(this.languagesGroup);
        languagesGroup.add(this.disabledLanguagesLabel);
        languagesGroup.add(this.languageTable.getComponent());

        languagesGroup.setLayout(new BoxLayout(this.languagesGroup, BoxLayout.Y_AXIS));
        languagesGroup.setMinimumSize(new JBDimension(languagesGroup.getMaximumSize().width, 400));
        languagesGroup.setMaximumSize(new JBDimension(languagesGroup.getMaximumSize().width, 400));
    }

    private void initCommonSetting() {
        this.commonSettings = new JPanel();
        this.commonSettings.setBorder(IdeBorderFactory.createTitledBorder(BiCoderBundle.get("plugin.setting.common.group.title"), false));
        this.commonSettings.add(createEnableCachePanel());
        this.commonSettings.add(createRequestHostPanel());
        this.commonSettings.setLayout(new BoxLayout(this.commonSettings, BoxLayout.Y_AXIS));
        this.commonSettings.setMinimumSize(new JBDimension(commonSettings.getMaximumSize().width, 150));
        this.commonSettings.setMaximumSize(new JBDimension(commonSettings.getMaximumSize().width, 150));
    }

    private JPanel  createEnableCachePanel() {
        FormBuilder panelBuilder = FormBuilder.createFormBuilder();
        this.isEnableCache = new JCheckBox(BiCoderBundle.get("plugin.setting.common.enable.cache.title"));
        isEnableCache.setSelected(false);
        panelBuilder.addComponent(isEnableCache);
        JPanel enableCachePanel = panelBuilder.getPanel();
        enableCachePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        enableCachePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, enableCachePanel.getPreferredSize().height));
        enableCachePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return enableCachePanel;
    }

    private JPanel  createRequestHostPanel() {
        FormBuilder panelBuilder = FormBuilder.createFormBuilder();
        this.requestHost = new JTextField();
        requestHost.setText(UserSetting.getInstance().getRequestHost());
        this.requestHost.setMinimumSize(new JBDimension(100, requestHost.getMaximumSize().height));
        this.requestHost.setMaximumSize(new JBDimension(100, requestHost.getMaximumSize().height));
        panelBuilder.addLabeledComponent(BiCoderBundle.get("plugin.setting.common.server.address.title"), requestHost);
        JPanel requestHostPanel = panelBuilder.getPanel();
        requestHostPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        requestHostPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, requestHostPanel.getPreferredSize().height));
        requestHostPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return requestHostPanel;
    }

    public Set<String> getDisabledLanguages() {
        return this.languageTable.getDisabledLanguages();
    }

    public void setEnableCache(boolean enableCache) {
        this.isEnableCache.setSelected(enableCache);
    }

    public boolean getEnableCache() {
        return this.isEnableCache.isSelected();
    }

    public void setRequestHost(String requestHost) {
        this.requestHost.setText(requestHost);
    }

    public String getRequestHost() {
        return this.requestHost.getText();
    }


}
