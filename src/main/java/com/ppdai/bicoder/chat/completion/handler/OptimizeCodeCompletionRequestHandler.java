package com.ppdai.bicoder.chat.completion.handler;

import com.intellij.openapi.project.Project;
import com.ppdai.bicoder.config.PluginStaticConfig;
import com.ppdai.bicoder.config.UserSetting;


public class OptimizeCodeCompletionRequestHandler extends EditCodeCompletionRequestHandler {


    public OptimizeCodeCompletionRequestHandler(Project project) {
        super(project);
    }

    @Override
    public String getUrl() {
        return UserSetting.getInstance().getRequestHost() + PluginStaticConfig.OPTIMIZE_CODE_URL;
    }


}
