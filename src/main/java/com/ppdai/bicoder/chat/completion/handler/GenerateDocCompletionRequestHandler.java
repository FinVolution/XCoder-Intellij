package com.ppdai.bicoder.chat.completion.handler;

import com.intellij.openapi.project.Project;
import com.ppdai.bicoder.config.PluginStaticConfig;
import com.ppdai.bicoder.config.UserSetting;


public class GenerateDocCompletionRequestHandler extends EditCodeCompletionRequestHandler {


    public GenerateDocCompletionRequestHandler(Project project) {
        super(project);
    }

    @Override
    public String getUrl() {
        return UserSetting.getInstance().getRequestHost() + PluginStaticConfig.GENERATE_DOC_URL;
    }


}
