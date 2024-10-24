package com.ppdai.bicoder.chat.completion.handler;

import com.intellij.openapi.project.Project;
import com.ppdai.bicoder.chat.constant.MessageType;


public class ChatCompletionRequestHandlerFactory {

    public static BaseChatCompletionRequestHandler getChatCompletionRequestHandler(Project project, String type) {
        if (MessageType.TESTS.getType().equals(type)) {
            return new GenerateTestCompletionRequestHandler(project);
        } else if (MessageType.EDIT.getType().equals(type)) {
            return new EditCodeCompletionRequestHandler(project);
        } else if (MessageType.DOC.getType().equals(type)) {
            return new GenerateDocCompletionRequestHandler(project);
        } else if (MessageType.EXPLAIN.getType().equals(type)) {
            return new ExplainCodeCompletionRequestHandler(project);
        } else if (MessageType.OPTIMIZE.getType().equals(type)) {
            return new OptimizeCodeCompletionRequestHandler(project);
        }
        return new CommonChatCompletionRequestHandler(project);
    }
}
