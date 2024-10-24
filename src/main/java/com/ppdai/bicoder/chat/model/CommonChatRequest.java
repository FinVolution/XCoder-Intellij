package com.ppdai.bicoder.chat.model;

import com.ppdai.bicoder.config.UserSetting;

import java.util.List;


public class CommonChatRequest {

    private String createUser;

    private String conversationUUID;

    private String gitRepo;

    private String gitBranch;

    private String codePath;

    private String codeLanguage;

    private String ideInfo;

    private String projectVersion;

    private List<CommonChatMessageDto> message;

    private List<ChatContextDto> context;

    private String userCode;

    private CommonChatRequest(CommonChatRequest.Builder builder) {
        this.createUser = builder.createUser;
        this.conversationUUID = builder.conversationUUID;
        this.gitRepo = builder.gitRepo;
        this.gitBranch = builder.gitBranch;
        this.codePath = builder.codePath;
        this.codeLanguage = builder.codeLanguage;
        this.ideInfo = builder.ideInfo;
        this.projectVersion = builder.projectVersion;
        this.message = builder.message;
        this.context = builder.context;
        this.userCode = builder.userCode;
    }

    public static class Builder {
        private String createUser;
        private String conversationUUID;
        private String gitRepo;
        private String gitBranch;
        private String codePath;
        private String codeLanguage;
        private String ideInfo;
        private String projectVersion;
        private List<CommonChatMessageDto> message;
        private List<ChatContextDto> context;

        private String userCode;

        public Builder() {
            UserSetting userSetting = UserSetting.getInstance();
            this.createUser = userSetting.getUsername();
        }

        public Builder conversationUUID(String conversationUUID) {
            this.conversationUUID = conversationUUID;
            return this;
        }

        public Builder gitRepo(String gitRepo) {
            this.gitRepo = gitRepo;
            return this;
        }

        public Builder gitBranch(String gitBranch) {
            this.gitBranch = gitBranch;
            return this;
        }

        public Builder codePath(String codePath) {
            this.codePath = codePath;
            return this;
        }

        public Builder codeLanguage(String codeLanguage) {
            this.codeLanguage = codeLanguage;
            return this;
        }

        public Builder ideInfo(String ideInfo) {
            this.ideInfo = ideInfo;
            return this;
        }

        public Builder projectVersion(String projectVersion) {
            this.projectVersion = projectVersion;
            return this;
        }

        public Builder message(List<CommonChatMessageDto> message) {
            this.message = message;
            return this;
        }

        public Builder context(List<ChatContextDto> context) {
            this.context = context;
            return this;
        }

        public Builder userCode(String userCode) {
            this.userCode = userCode;
            return this;
        }

        public CommonChatRequest build() {
            return new CommonChatRequest(this);
        }
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public String getConversationUUID() {
        return conversationUUID;
    }

    public void setConversationUUID(String conversationUUID) {
        this.conversationUUID = conversationUUID;
    }

    public String getGitRepo() {
        return gitRepo;
    }

    public void setGitRepo(String gitRepo) {
        this.gitRepo = gitRepo;
    }

    public String getGitBranch() {
        return gitBranch;
    }

    public void setGitBranch(String gitBranch) {
        this.gitBranch = gitBranch;
    }

    public String getCodePath() {
        return codePath;
    }

    public void setCodePath(String codePath) {
        this.codePath = codePath;
    }

    public String getCodeLanguage() {
        return codeLanguage;
    }

    public void setCodeLanguage(String codeLanguage) {
        this.codeLanguage = codeLanguage;
    }

    public String getIdeInfo() {
        return ideInfo;
    }

    public void setIdeInfo(String ideInfo) {
        this.ideInfo = ideInfo;
    }

    public String getProjectVersion() {
        return projectVersion;
    }

    public void setProjectVersion(String projectVersion) {
        this.projectVersion = projectVersion;
    }

    public List<CommonChatMessageDto> getMessage() {
        return message;
    }

    public void setMessage(List<CommonChatMessageDto> message) {
        this.message = message;
    }

    public List<ChatContextDto> getContext() {
        return context;
    }

    public void setContext(List<ChatContextDto> context) {
        this.context = context;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CommonChatRequest{");
        sb.append("createUser='").append(createUser).append('\'');
        sb.append(", conversationUUID='").append(conversationUUID).append('\'');
        sb.append(", gitRepo='").append(gitRepo).append('\'');
        sb.append(", gitBranch='").append(gitBranch).append('\'');
        sb.append(", codePath='").append(codePath).append('\'');
        sb.append(", codeLanguage='").append(codeLanguage).append('\'');
        sb.append(", ideInfo='").append(ideInfo).append('\'');
        sb.append(", projectVersion='").append(projectVersion).append('\'');
        sb.append(", message=").append(message);
        sb.append(", context=").append(context);
        sb.append(", userCode='").append(userCode).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
