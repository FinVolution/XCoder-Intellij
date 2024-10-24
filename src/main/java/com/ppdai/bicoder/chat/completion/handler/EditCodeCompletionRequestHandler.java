package com.ppdai.bicoder.chat.completion.handler;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.ppdai.bicoder.chat.conversation.Conversation;
import com.ppdai.bicoder.chat.conversation.Message;
import com.ppdai.bicoder.chat.model.*;
import com.ppdai.bicoder.config.PluginStaticConfig;
import com.ppdai.bicoder.config.UserSetting;
import com.ppdai.bicoder.utils.EditorUtils;
import com.ppdai.bicoder.utils.InfoUtils;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


public class EditCodeCompletionRequestHandler extends BaseChatCompletionRequestHandler {


    public EditCodeCompletionRequestHandler(Project project) {
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

        return buildEditCodeRequest(message, conversationUUID, gitRepo, gitBranch, codePath, codeLanguage, ideInfo, projectVersion, chatContextDtoList);
    }

    private String buildEditCodeRequest(Message message, String conversationUUID, String gitRepo, String gitBranch, String codePath, String codeLanguage, String ideInfo, String projectVersion, List<ChatContextDto> chatContextDtoList) {
        EditCodeRequest editCodeRequest =
                new EditCodeRequest.Builder()
                        .conversationUUID(conversationUUID)
                        .gitRepo(gitRepo)
                        .gitBranch(gitBranch)
                        .codePath(codePath)
                        .codeLanguage(codeLanguage)
                        .ideInfo(ideInfo)
                        .projectVersion(projectVersion)
                        .userText(message.getUserMessage())
                        .userCode(message.getSelectedCode())
                        .userContext(chatContextDtoList)
                        .build();
        return gson.toJson(editCodeRequest);
    }

    @Override
    public String getUrl() {
        return UserSetting.getInstance().getRequestHost() + PluginStaticConfig.EDIT_CODE_URL;
    }


}
