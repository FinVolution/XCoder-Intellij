package com.ppdai.bicoder.model;

import com.intellij.openapi.application.ApplicationInfo;
import com.ppdai.bicoder.config.PluginStaticConfig;
import com.ppdai.bicoder.config.UserSetting;
import com.ppdai.bicoder.utils.InfoUtils;

/**
 * 配置请求对象
 *
 */
public class BiCoderConfigRequest {

    /**
     * 用户域账号
     */
    private String userName;

    /**
     * 插件名称
     */
    private String projectName;

    /**
     * 插件版本
     */
    private String projectVersion;

    /**
     * 编译器信息
     */
    private String projectType;

    /**
     * 插件禁用状态
     */
    private boolean isDisable;


    private BiCoderConfigRequest(Builder builder) {
        this.userName = builder.username;
        this.projectName = builder.projectName;
        this.projectVersion = builder.projectVersion;
        this.projectType = builder.projectType;
        this.isDisable = builder.isDisable;
    }

    /**
     * 静态内部类 Builder
     */

    public static class Builder {

        private String username;
        private String projectName;
        private String projectVersion;
        private String projectType;
        private boolean isDisable;

        /**
         * 必填参数构造,仅暴露此构造
         */
        public Builder() {
            UserSetting userSetting = UserSetting.getInstance();
            this.username = userSetting.getUsername();
            this.projectName = PluginStaticConfig.PLUGIN_NAME;
            this.projectVersion = InfoUtils.getVersion();
            this.projectType = ApplicationInfo.getInstance().getVersionName();
            this.isDisable = !userSetting.getEnablePlugin();
        }

        public BiCoderConfigRequest build() {
            return new BiCoderConfigRequest(this);
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getProjectName() {
            return projectName;
        }

        public void setProjectName(String projectName) {
            this.projectName = projectName;
        }

        public String getProjectVersion() {
            return projectVersion;
        }

        public void setProjectVersion(String projectVersion) {
            this.projectVersion = projectVersion;
        }

        public String getProjectType() {
            return projectType;
        }

        public void setProjectType(String projectType) {
            this.projectType = projectType;
        }

        public boolean isDisable() {
            return isDisable;
        }

        public void setDisable(boolean disable) {
            isDisable = disable;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Builder{");
            sb.append("username='").append(username).append('\'');
            sb.append(", projectName='").append(projectName).append('\'');
            sb.append(", projectVersion='").append(projectVersion).append('\'');
            sb.append(", projectType='").append(projectType).append('\'');
            sb.append(", isDisable=").append(isDisable);
            sb.append('}');
            return sb.toString();
        }
    }
}
