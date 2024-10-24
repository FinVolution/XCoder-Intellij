package com.ppdai.bicoder.chat.completion.handler;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.ppdai.bicoder.chat.conversation.Conversation;
import com.ppdai.bicoder.chat.conversation.Message;
import com.ppdai.bicoder.chat.model.ChatContext;
import com.ppdai.bicoder.chat.model.ChatContextDto;
import com.ppdai.bicoder.chat.model.GenerateTestsRequest;
import com.ppdai.bicoder.config.PluginStaticConfig;
import com.ppdai.bicoder.config.UserProjectSetting;
import com.ppdai.bicoder.config.UserSetting;
import com.ppdai.bicoder.utils.EditorUtils;
import com.ppdai.bicoder.utils.InfoUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class GenerateTestCompletionRequestHandler extends BaseChatCompletionRequestHandler {

    public GenerateTestCompletionRequestHandler(Project project) {
        super(project);
    }

    @Override
    public String getRequestBody(Conversation conversation, Message message) {
        Editor editor = EditorUtils.getSelectedEditor(project);
        VirtualFile virtualFile;
        if (editor != null) {
            virtualFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
        } else {
            virtualFile = null;
        }
        //此处因为后端每次需要不同id来关联对话,所以使用每次变化的messageId
        String conversationUUID = message.getCurrentRequestId();
        String gitRepo = InfoUtils.getGitUrl(project);
        String gitBranch = InfoUtils.getGitBranch(project);
        String codePath;
        String codeLanguage;
        if (virtualFile != null) {
            codePath = InfoUtils.getRelativePath(virtualFile, project);
            codeLanguage = InfoUtils.getLanguage(virtualFile).toUpperCase();
        } else {
            codePath = null;
            codeLanguage = null;
        }
        String ideInfo = InfoUtils.getIdeaInfo();
        String projectVersion = InfoUtils.getVersion();
        List<ChatContext> chatContexts = message.getChatContexts();
        List<ChatContextDto> chatContextDtoList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(chatContexts)) {
            chatContextDtoList = chatContexts.stream()
                    .map(chatContext -> new ChatContextDto(chatContext.getType(), chatContext.getContent(), chatContext.getFilePath() + "/" + chatContext.getFileName()))
                    .collect(Collectors.toList());
        }
        return buildGenerateTestsRequest(message, conversationUUID, gitRepo, gitBranch, codePath, codeLanguage, ideInfo, projectVersion, chatContextDtoList);
    }

    @Override
    public String getUrl() {
        return UserSetting.getInstance().getRequestHost() + PluginStaticConfig.GENERATE_TESTS_URL;
    }

    private String buildGenerateTestsRequest(@NotNull Message message, String conversationUUID, String gitRepo, String gitBranch, String codePath, String codeLanguage, String ideInfo, String projectVersion, List<ChatContextDto> chatContextDtoList) {
        UserProjectSetting userProjectSetting = UserProjectSetting.getInstance(project);
        String unitTestFramework;
        String customUintTestFramework = userProjectSetting.getCustomUintTestFramework();
        if (StringUtils.isNotBlank(customUintTestFramework)) {
            unitTestFramework = customUintTestFramework;
        } else {
            unitTestFramework = userProjectSetting.getSelectedUintTestFramework();
        }
        GenerateTestsRequest generateTestsRequest =
                new GenerateTestsRequest.Builder()
                        .conversationUUID(conversationUUID)
                        .gitRepo(gitRepo)
                        .gitBranch(gitBranch)
                        .codePath(codePath)
                        .codeLanguage(codeLanguage)
                        .ideInfo(ideInfo)
                        .projectVersion(projectVersion)
                        .unitTestFramework(unitTestFramework)
                        .userText(message.getUserMessage())
                        .userCode(message.getSelectedCode())
                        .userContext(chatContextDtoList)
                        .build();
        return gson.toJson(generateTestsRequest);
    }

}
