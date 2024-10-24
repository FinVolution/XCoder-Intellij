package com.ppdai.bicoder.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;


/**
 * 用户配置,project级生效
 *
 */
@State(name = "com.ppdai.bicoder.config.UserProjectSetting", storages = @Storage("BiCoderUserProjectSettings.xml"))
public class UserProjectSetting implements PersistentStateComponent<UserProjectSetting> {

    /**
     * 已经选择的单测框架语言
     */
    private String selectedUintTestFrameworkLanguage = "";

    /**
     * 已经选择的单测框架
     */
    private String selectedUintTestFramework = "";

    /**
     * 自定义的单测框架
     */
    private String customUintTestFramework = "";

    /**
     * 已经选择的编译语言
     */
    private String testFileRootPath = "";


    public String getSelectedUintTestFrameworkLanguage() {
        return selectedUintTestFrameworkLanguage;
    }

    public void setSelectedUintTestFrameworkLanguage(String selectedUintTestFrameworkLanguage) {
        this.selectedUintTestFrameworkLanguage = selectedUintTestFrameworkLanguage;
    }

    public String getSelectedUintTestFramework() {
        return selectedUintTestFramework;
    }

    public void setSelectedUintTestFramework(String selectedUintTestFramework) {
        this.selectedUintTestFramework = selectedUintTestFramework;
    }


    public String getCustomUintTestFramework() {
        return customUintTestFramework;
    }

    public void setCustomUintTestFramework(String customUintTestFramework) {
        this.customUintTestFramework = customUintTestFramework;
    }

    public String getTestFileRootPath() {
        return testFileRootPath;
    }

    public void setTestFileRootPath(String testFileRootPath) {
        this.testFileRootPath = testFileRootPath;
    }

    @Override
    public UserProjectSetting getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull UserProjectSetting state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public static UserProjectSetting getInstance(Project project) {
        return project.getService(UserProjectSetting.class);
    }


}
