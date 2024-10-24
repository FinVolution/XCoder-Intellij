package com.ppdai.bicoder.chat.completion.handler;

import com.intellij.openapi.project.Project;
import com.ppdai.bicoder.config.PluginStaticConfig;
import com.ppdai.bicoder.config.UserSetting;


public class ExplainCodeCompletionRequestHandler extends EditCodeCompletionRequestHandler {


    public ExplainCodeCompletionRequestHandler(Project project) {
        super(project);
    }

    @Override
    public String getUrl() {
        return UserSetting.getInstance().getRequestHost() + PluginStaticConfig.EXPLAIN_CODE_URL;
    }


}
