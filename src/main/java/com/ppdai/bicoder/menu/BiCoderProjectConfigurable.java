package com.ppdai.bicoder.menu;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.ppdai.bicoder.config.BiCoderBundle;
import com.ppdai.bicoder.config.UserProjectSetting;
import com.ppdai.bicoder.config.UserSetting;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * 插件配置页(项目级生效)
 *
 */
public class BiCoderProjectConfigurable implements Configurable {

    private final BiCoderProjectSettingsForm form;

    private UserProjectSetting userProjectSetting;

    public BiCoderProjectConfigurable(Project project) {
        userProjectSetting = UserProjectSetting.getInstance(project);
        form = new BiCoderProjectSettingsForm(userProjectSetting.getSelectedUintTestFrameworkLanguage(),
                userProjectSetting.getSelectedUintTestFramework(),
                userProjectSetting.getCustomUintTestFramework(),
                userProjectSetting.getTestFileRootPath());
    }

    @Override
    public String getDisplayName() {
        return BiCoderBundle.get("plugin.setting.project.configurable.title");
    }

    @Override
    public void reset() {
        this.form.resetUintTestFramework(userProjectSetting.getSelectedUintTestFrameworkLanguage(),
                userProjectSetting.getSelectedUintTestFramework(),
                userProjectSetting.getCustomUintTestFramework(),
                userProjectSetting.getTestFileRootPath());
    }

    @Override
    public boolean isModified() {
        String currentSelectedUintTestFramework = this.form.getSelectedUintTestFramework();
        String currentSelectedLanguage = this.form.getSelectedLanguage();
        String currentCustomUintTestFramework = this.form.getCustomUintTestFramework();
        String currentTestFileRootPath = this.form.getTestFileRootPath();
        String selectedUintTestFrameworkSetting = userProjectSetting.getSelectedUintTestFramework();
        String selectedLanguageSetting = userProjectSetting.getSelectedUintTestFrameworkLanguage();
        String customUintTestFrameworkSetting = userProjectSetting.getCustomUintTestFramework();
        String testFileRootPathSetting = userProjectSetting.getTestFileRootPath();
        return !StringUtils.equals(currentSelectedLanguage, selectedLanguageSetting)
                || !StringUtils.equals(currentSelectedUintTestFramework, selectedUintTestFrameworkSetting)
                || !StringUtils.equals(currentCustomUintTestFramework, customUintTestFrameworkSetting)
                || !StringUtils.equals(currentTestFileRootPath, testFileRootPathSetting);

    }

    @Override
    public void apply() {
        String currentSelectedUintTestFramework = this.form.getSelectedUintTestFramework();
        String currentSelectedLanguage = this.form.getSelectedLanguage();
        String currentCustomUintTestFramework = this.form.getCustomUintTestFramework();
        String currentTestFileRootPath = this.form.getTestFileRootPath();
        userProjectSetting.setTestFileRootPath(currentTestFileRootPath);
        userProjectSetting.setSelectedUintTestFramework(currentSelectedUintTestFramework);
        userProjectSetting.setSelectedUintTestFrameworkLanguage(currentSelectedLanguage);
        userProjectSetting.setCustomUintTestFramework(currentCustomUintTestFramework);
    }


    BiCoderProjectSettingsForm getForm() {
        return this.form;
    }

    @Override
    public @Nullable
    JComponent createComponent() {
        return this.form.getPanel();
    }

}