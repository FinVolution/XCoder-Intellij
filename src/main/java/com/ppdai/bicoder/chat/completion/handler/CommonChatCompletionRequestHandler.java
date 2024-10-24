package com.ppdai.bicoder.chat.completion.handler;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.ppdai.bicoder.chat.conversation.Conversation;
import com.ppdai.bicoder.chat.conversation.Message;
import com.ppdai.bicoder.chat.model.ChatContext;
import com.ppdai.bicoder.chat.model.ChatContextDto;
import com.ppdai.bicoder.chat.model.CommonChatMessageDto;
import com.ppdai.bicoder.chat.model.CommonChatRequest;
import com.ppdai.bicoder.config.PluginStaticConfig;
import com.ppdai.bicoder.config.UserSetting;
import com.ppdai.bicoder.utils.EditorUtils;
import com.ppdai.bicoder.utils.InfoUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CommonChatCompletionRequestHandler extends BaseChatCompletionRequestHandler {

    public CommonChatCompletionRequestHandler(Project project) {
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
        return buildCommonChatRequest(conversation, message, conversationUUID, gitRepo, gitBranch, codePath, codeLanguage, ideInfo, projectVersion, chatContextDtoList);
    }

    @Override
    public String getUrl() {
        return UserSetting.getInstance().getRequestHost() + PluginStaticConfig.COMMON_CHAT_URL;
    }


    private String buildCommonChatRequest(@NotNull Conversation conversation, @NotNull Message message, String conversationUUID, String gitRepo, String gitBranch, String codePath, String codeLanguage, String ideInfo, String projectVersion, List<ChatContextDto> chatContextDtoList) {
        List<CommonChatMessageDto> commonChatMessageDtos = new ArrayList<>();
        List<Message> messages = conversation.getMessages();
        commonChatMessageDtos.add(new CommonChatMessageDto(Message.USER_MESSAGE_TYPE, message.getUserMessage()));
        //reload时得判断reload的message位置
        int startIndex = messages.size() - 1;
        for (int i = messages.size() - 1; i >= 0; i--) {
            if (messages.get(i).getId().equals(message.getId())) {
                startIndex = i - 1;
                break;
            }
        }
        int num = 0;
        for (int i = startIndex; i >= 0; i--) {
            commonChatMessageDtos.add(new CommonChatMessageDto(Message.AI_MESSAGE_TYPE, messages.get(i).getResponse()));
            String userMessage = messages.get(i).getUserMessage();
            String selectedCode = messages.get(i).getSelectedCode();
            StringBuilder stringBuilder = new StringBuilder();
            if (StringUtils.isNotBlank(userMessage)) {
                stringBuilder.append(userMessage);
            }
            if (StringUtils.isNotBlank(selectedCode)) {
                if (stringBuilder.length() > 0) {
                    stringBuilder.append("\n");
                }
                stringBuilder.append(selectedCode);
            }
            commonChatMessageDtos.add(new CommonChatMessageDto(Message.USER_MESSAGE_TYPE, stringBuilder.toString()));
            num++;
            if (num >= 5) {
                break;
            }
        }
        Collections.reverse(commonChatMessageDtos);
        CommonChatRequest commonChatRequest =
                new CommonChatRequest.Builder()
                        .conversationUUID(conversationUUID)
                        .gitRepo(gitRepo)
                        .gitBranch(gitBranch)
                        .codePath(codePath)
                        .codeLanguage(codeLanguage)
                        .ideInfo(ideInfo)
                        .projectVersion(projectVersion)
                        .message(commonChatMessageDtos)
                        .userCode(message.getSelectedCode())
                        .context(chatContextDtoList)
                        .build();
        return gson.toJson(commonChatRequest);
    }
}
