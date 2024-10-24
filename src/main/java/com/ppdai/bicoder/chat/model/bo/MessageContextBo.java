package com.ppdai.bicoder.chat.model.bo;

import com.ppdai.bicoder.chat.constant.MessageType;
import com.ppdai.bicoder.chat.constant.TestSchema;
import com.ppdai.bicoder.chat.conversation.Message;
import com.ppdai.bicoder.chat.model.ChatContext;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;

/**
 * chat消息上下文,用作上下问信息整体传递,而不是分散的一个个属性
 *
 */
public class MessageContextBo {

    /**
     * 消息id
     */
    private final String messageId;

    /**
     * 消息类型
     *
     * @see com.ppdai.bicoder.chat.constant.MessageType
     */
    private final String messageType;

    /**
     * 当messageType为MessageType.TESTS时,表示测试模式,即生成的单测是原文件续写,还是新文件
     *
     * @see com.ppdai.bicoder.chat.constant.TestSchema
     */
    private String testSchema;

    /**
     * 测试文件路径
     */
    private String testFilePath;

    /**
     * 当前语言
     */
    private String language;

    /**
     * 当前选择的代码文件路径
     */
    private String selectCodeFileFullPath;

    public MessageContextBo(String messageId, String messageType) {
        this.messageId = messageId;
        this.messageType = messageType;
    }

    public MessageContextBo(Message message) {
        this.messageId = message.getId();
        this.messageType = message.getType();
        this.language = message.getSelectedCodeLanguage();
        this.selectCodeFileFullPath = message.getSelectCodeFileFullPath();
        // 如果是单测生成，则判断测试生成模式
        if (StringUtils.isNotBlank(this.messageType) && MessageType.TESTS.getType().equals(this.messageType)) {
            List<ChatContext> chatContexts = message.getChatContexts();
            if (CollectionUtils.isNotEmpty(chatContexts)) {
                for (ChatContext chatContext : chatContexts) {
                    //如果是单测文件路径，则记录被测试文件路径
                    if (Objects.equals(chatContext.getType(), ChatContext.TYPE_FILE_LOCAL_TEST)) {
                        this.testSchema = TestSchema.LOCAL;
                        this.testFilePath = chatContext.getSystemFilePath();
                        break;
                    }
                    if (Objects.equals(chatContext.getType(), ChatContext.TYPE_FILE_OTHER_TEST)) {
                        this.testSchema = TestSchema.OTHER;
                        this.testFilePath = chatContext.getSystemFilePath();
                        break;
                    }
                }
            }
        }
    }

    public String getMessageId() {
        return messageId;
    }

    public String getMessageType() {
        return messageType;
    }

    public String getTestSchema() {
        return testSchema;
    }

    public void setTestSchema(String testSchema) {
        this.testSchema = testSchema;
    }

    public String getTestFilePath() {
        return testFilePath;
    }

    public void setTestFilePath(String testFilePath) {
        this.testFilePath = testFilePath;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getSelectCodeFileFullPath() {
        return selectCodeFileFullPath;
    }

    public void setSelectCodeFileFullPath(String selectCodeFileFullPath) {
        this.selectCodeFileFullPath = selectCodeFileFullPath;
    }
}
