package com.ppdai.bicoder.chat.conversation;

import com.intellij.openapi.project.Project;
import com.ppdai.bicoder.config.PluginStaticConfig;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.*;

import static java.util.stream.Collectors.toList;

public final class ConversationService {

    private ConversationsStorage conversationsStorage;

    private ConversationService() {
    }

    public static ConversationService getInstance(Project project) {
        ConversationService service = project.getService(ConversationService.class);
        if (service.conversationsStorage == null) {
            service.conversationsStorage = ConversationsStorage.getInstance(project);
        }
        return service;
    }

    public List<Conversation> getSortedConversations() {
        return conversationsStorage.getAllStoreConversations()
                .stream()
                .sorted(Comparator.comparing(Conversation::getUpdatedOn).reversed())
                .collect(toList());
    }

    public Conversation createConversation() {
        Conversation conversation = new Conversation();
        conversation.setId(UUID.randomUUID().toString());
        conversation.setCreatedOn(LocalDateTime.now());
        conversation.setUpdatedOn(LocalDateTime.now());
        return conversation;
    }

    public void addConversation(Conversation conversation) {
        //消息不能为空
        if (conversation != null && CollectionUtils.isNotEmpty(conversation.getMessages())) {
            List<Conversation> conversations = conversationsStorage.getAllStoreConversations();
            if (conversations == null) {
                conversations = new ArrayList<>();
            }
            conversations.add(conversation);
            //只保留固定个数的会话
            if (conversations.size() > PluginStaticConfig.MAX_CHAT_CONVERSATION_SIZE) {
                //删除最早一条会话
                conversations.remove(0);
            }
            conversationsStorage.saveAllConversations(conversations);
        }
    }

    public void saveMessage(String response, Message message, Conversation conversation, boolean isRetry) {
        List<Message> messages = conversation.getMessages();
        if (isRetry && !messages.isEmpty()) {
            Message messageToBeSaved = messages.stream()
                    .filter(item -> item.getId().equals(message.getId()))
                    .findFirst().orElseThrow();
            messageToBeSaved.setResponse(response);
            saveConversation(conversation);
            return;
        }
        message.setResponse(response);
        conversation.addMessage(message);
        //只保留固定个数的消息
        if (messages.size() > PluginStaticConfig.MAX_CHAT_MESSAGE_SIZE) {
            //删除最早一条消息
            messages.remove(0);
        }
        saveConversation(conversation);
    }

    public void saveMessage(@NotNull Conversation conversation, @NotNull Message message) {
        conversation.setUpdatedOn(LocalDateTime.now());
        List<Conversation> conversations = conversationsStorage.getAllStoreConversations();
        for (Conversation savedConversation : conversations) {
            savedConversation.setMessages(
                    savedConversation.getMessages().stream().map(item -> {
                        if (item.getId().equals(message.getId())) {
                            return message;
                        }
                        return item;
                    }).collect(toList()));
            if (savedConversation.getId().equals(conversation.getId())) {
                savedConversation.setMessages(conversation.getMessages());
            }
        }
    }

    public void saveConversation(Conversation conversation) {
        conversation.setUpdatedOn(LocalDateTime.now());
        ListIterator<Conversation> iterator = conversationsStorage.getAllStoreConversations().listIterator();
        while (iterator.hasNext()) {
            var next = iterator.next();
            if (next.getId().equals(conversation.getId())) {
                iterator.set(conversation);
                conversationsStorage.setCurrentConversation(conversation);
                return;
            }
        }
        addConversation(conversation);
        conversationsStorage.setCurrentConversation(conversation);
    }

    public Conversation startConversation() {
        Conversation conversation = createConversation();
        conversationsStorage.setCurrentConversation(conversation);
        addConversation(conversation);
        return conversation;
    }

    public void clearAll() {
        conversationsStorage.getAllStoreConversations().clear();
        conversationsStorage.setCurrentConversation(null);
    }

    public Optional<Conversation> getPreviousConversation() {
        return tryGetNextOrPreviousConversation(true);
    }

    public Optional<Conversation> getNextConversation() {
        return tryGetNextOrPreviousConversation(false);
    }

    private Optional<Conversation> tryGetNextOrPreviousConversation(boolean isPrevious) {
        Conversation currentConversation = conversationsStorage.getCurrentConversation();
        if (currentConversation != null) {
            List<Conversation> sortedConversations = getSortedConversations();
            for (int i = 0; i < sortedConversations.size(); i++) {
                Conversation conversation = sortedConversations.get(i);
                if (conversation != null && conversation.getId().equals(currentConversation.getId())) {
                    int previousIndex = isPrevious ? i + 1 : i - 1;
                    if (isPrevious ? previousIndex < sortedConversations.size() : previousIndex != -1) {
                        return Optional.of(sortedConversations.get(previousIndex));
                    }
                }
            }
        }
        return Optional.empty();
    }

    public void deleteConversation(Conversation conversation) {
        ListIterator<Conversation> iterator = conversationsStorage.getAllStoreConversations().listIterator();
        while (iterator.hasNext()) {
            var next = iterator.next();
            if (next.getId().equals(conversation.getId())) {
                iterator.remove();
                break;
            }
        }
    }

    public void deleteSelectedConversation() {
        Optional<Conversation> nextConversation = getPreviousConversation();
        if (nextConversation.isEmpty()) {
            nextConversation = getNextConversation();
        }
        Conversation currentConversation = conversationsStorage.getCurrentConversation();
        if (currentConversation != null) {
            deleteConversation(currentConversation);
            nextConversation.ifPresent(conversationsStorage::setCurrentConversation);
        } else {
            throw new RuntimeException("Tried to delete a conversation that hasn't been set");
        }
    }
}