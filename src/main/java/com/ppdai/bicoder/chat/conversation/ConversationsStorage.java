package com.ppdai.bicoder.chat.conversation;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.OptionTag;
import com.ppdai.bicoder.chat.conversation.converter.ConversationConverter;
import com.ppdai.bicoder.chat.conversation.converter.ConversationsConverter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 对话存储
 *
 */
@State(name = "com.ppdai.bicoder.chat.conversation.ConversationsStorage", storages = @Storage("BiCoderChatConversations.xml"))
public class ConversationsStorage implements PersistentStateComponent<ConversationsStorage> {

    @OptionTag(converter = ConversationsConverter.class)
    public ConversationsContainer conversationsContainer = new ConversationsContainer();

    @OptionTag(converter = ConversationConverter.class)
    public Conversation currentConversation;

    public static ConversationsStorage getInstance(Project project) {
        return project.getService(ConversationsStorage.class);
    }

    @Nullable
    @Override
    public ConversationsStorage getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull ConversationsStorage state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public void setCurrentConversation(@Nullable Conversation conversation) {
        this.currentConversation = conversation;
    }

    public @Nullable
    Conversation getCurrentConversation() {
        return this.currentConversation;
    }

    public List<Conversation> getAllStoreConversations() {
        return conversationsContainer.getConversations();
    }

    public void saveAllConversations(List<Conversation> conversations) {
        conversationsContainer.setConversations(conversations);
    }
}
