package com.ppdai.bicoder.chat;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.componentsList.components.ScrollablePanel;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI.Borders;
import com.ppdai.bicoder.chat.completion.handler.BaseChatCompletionRequestHandler;
import com.ppdai.bicoder.chat.completion.handler.ChatCompletionRequestHandlerFactory;
import com.ppdai.bicoder.chat.components.*;
import com.ppdai.bicoder.chat.components.markdown.MarkdownPanel;
import com.ppdai.bicoder.chat.constant.ChatAction;
import com.ppdai.bicoder.chat.conversation.Conversation;
import com.ppdai.bicoder.chat.conversation.ConversationService;
import com.ppdai.bicoder.chat.conversation.Message;
import com.ppdai.bicoder.chat.model.ChatContext;
import com.ppdai.bicoder.chat.model.bo.MessageContextBo;
import com.ppdai.bicoder.utils.ThemeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

public abstract class BaseChatToolWindowTabPanel implements ChatToolWindowTabPanel {

    private final JPanel rootPanel;
    private final ScrollablePanel scrollablePanel;
    private final Map<String, JPanel> visibleMessagePanels = new HashMap<>();

    protected final Project project;
    protected final UserPromptTextArea userPromptTextArea;
    protected final ChatContextArea chatContextArea;

    protected final ConversationService conversationService;


    protected @Nullable
    Conversation conversation;

    protected abstract JComponent getLandingView();

    public BaseChatToolWindowTabPanel(@NotNull Project project) {

        this.project = project;
        this.conversationService = ConversationService.getInstance(project);
        this.rootPanel = new JPanel(new GridBagLayout());
        this.scrollablePanel = new ScrollablePanel();
        this.userPromptTextArea = new UserPromptTextArea(this::sendMessage, project);
        this.chatContextArea = new ChatContextArea(project);
        init();
    }

    @Override
    public void requestFocusForTextArea() {
        userPromptTextArea.focus();
    }

    @Override
    public JPanel getContent() {
        return rootPanel;
    }

    @Override
    public @Nullable
    Conversation getConversation() {
        return conversation;
    }

    @Override
    public void setConversation(@Nullable Conversation conversation) {
        this.conversation = conversation;
    }

    @Override
    public void displayLandingView() {
        scrollablePanel.removeAll();
        scrollablePanel.add(getLandingView());
        scrollablePanel.repaint();
        scrollablePanel.revalidate();
        userPromptTextArea.stopCall();
    }

    @Override
    public void startNewConversation(Message message) {
        conversation = conversationService.startConversation();
        sendMessage(message);
    }

    @Override
    public void addChatContext(ChatContext chatContext) {
        chatContextArea.addChatContext(chatContext);
    }

    @Override
    public List<ChatContext> getChatContextListInArea() {
        return chatContextArea.getChatContextListInArea();
    }

    @Override
    public void startEditCode() {
        this.userPromptTextArea.setText(ChatAction.EDIT_CODE.getCommandText() + " ");
        this.requestFocusForTextArea();
    }


    @Override
    public void sendMessage(Message message) {
        if (conversation == null) {
            conversation = conversationService.startConversation();
        }
        String messageId = message.getId();
        JPanel messageWrapper = createNewMessageWrapper(messageId);
        messageWrapper.add(new UserMessagePanel(
                project,
                message,
                this));
        MarkdownPanel markdownPanel = new MarkdownPanel(project, new MessageContextBo(message), true, this);
        ResponsePanel responsePanel = new ResponsePanel()
                .withReloadAction(() -> reloadMessage(message, conversation))
                .withDeleteAction(() -> deleteMessage(messageId, messageWrapper, conversation))
                .addContent(markdownPanel);
        responsePanel.enableActions(false);
        messageWrapper.add(responsePanel);
        call(conversation, message, responsePanel, false);
    }

    @Override
    public void dispose() {
        chatContextArea.clearChatContext();
    }


    private void call(
            Conversation conversation,
            Message message,
            ResponsePanel responsePanel,
            boolean isRetry) {
        MarkdownPanel responseContainer = (MarkdownPanel) responsePanel.getContent();

        BaseChatCompletionRequestHandler requestHandler = ChatCompletionRequestHandlerFactory.getChatCompletionRequestHandler(project, message.getType());
        requestHandler.setMessageListener(partialMessage -> {
            try {
                responseContainer.append(partialMessage);
            } catch (Exception e) {
                responseContainer.displayDefaultError();
                throw new RuntimeException("Error while updating the content", e);
            }
        });
        requestHandler.setCompletedListener(completeMessage -> {
            responseContainer.complete();
            responsePanel.enableActions(true);
            conversationService.saveMessage(completeMessage, message, conversation, isRetry);
            chatContextArea.setLastAdd(true);
            stopStreaming(responseContainer);
        });
        requestHandler.setErrorListener((errMsg) -> {
            try {
                responseContainer.displayError(errMsg);
            } finally {
                responsePanel.enableActions(true);
                stopStreaming(responseContainer);
            }
        });
        userPromptTextArea.setRequestHandler(requestHandler);
        userPromptTextArea.setSubmitEnabled(false);
        requestHandler.call(conversation, message, isRetry);
    }

    protected void reloadMessage(Message message, Conversation conversation) {
        ResponsePanel responsePanel = null;
        try {
            responsePanel = (ResponsePanel) Arrays.stream(
                            visibleMessagePanels.get(message.getId()).getComponents())
                    .filter(component -> component instanceof ResponsePanel)
                    .findFirst().orElseThrow();
            ((MarkdownPanel) responsePanel.getContent()).clear();
            scrollablePanel.revalidate();
            scrollablePanel.repaint();
        } catch (Exception e) {
            throw new RuntimeException("Couldn't reload message", e);
        } finally {
            if (responsePanel != null) {
                message.setResponse("");
                message.setCurrentRequestId(UUID.randomUUID().toString());
                ((MarkdownPanel) responsePanel.getContent()).setCurrentRequestId(message.getCurrentRequestId());
                conversationService.saveMessage(conversation, message);
                call(conversation, message, responsePanel, true);
            }
        }
    }

    protected void deleteMessage(String messageId, JPanel messageWrapper, Conversation conversation) {
        scrollablePanel.remove(messageWrapper);
        scrollablePanel.repaint();
        scrollablePanel.revalidate();


        visibleMessagePanels.remove(messageId);
        conversation.removeMessage(messageId);
        conversationService.saveConversation(conversation);

        if (conversation.getMessages().isEmpty()) {
            conversationService.deleteConversation(conversation);
            setConversation(null);
            displayLandingView();
        }
    }

    protected JPanel createNewMessageWrapper(String messageId) {
        var messageWrapper = new JPanel();
        messageWrapper.setLayout(new BoxLayout(messageWrapper, BoxLayout.PAGE_AXIS));
        scrollablePanel.add(messageWrapper);
        scrollablePanel.repaint();
        scrollablePanel.revalidate();
        visibleMessagePanels.put(messageId, messageWrapper);
        return messageWrapper;
    }

    protected void clearWindow() {
        scrollablePanel.removeAll();
        scrollablePanel.revalidate();
        scrollablePanel.repaint();
    }

    private void stopStreaming(MarkdownPanel responseContainer) {
        SwingUtilities.invokeLater(() -> {
            userPromptTextArea.setSubmitEnabled(true);
            responseContainer.hideCarets();
        });
    }

    private void init() {
        var gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;

        scrollablePanel.setLayout(new BoxLayout(scrollablePanel, BoxLayout.Y_AXIS));
        JBScrollPane scrollPane = new JBScrollPane(scrollablePanel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.setViewportBorder(null);
        rootPanel.add(scrollPane, gbc);
        new SmartScroller(scrollPane);

        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 1;

        var wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(Borders.compound(
                Borders.customLine(JBColor.border(), 1, 0, 0, 0),
                Borders.empty(8)));
        wrapper.setBackground(ThemeUtils.getPanelBackgroundColor());
        wrapper.add(userPromptTextArea, BorderLayout.SOUTH);
        wrapper.add(chatContextArea, BorderLayout.NORTH);


        var header = new JPanel(new BorderLayout());
        header.setBackground(ThemeUtils.getPanelBackgroundColor());
        header.setBorder(Borders.emptyBottom(8));
        wrapper.add(header);
        rootPanel.add(wrapper, gbc);
        userPromptTextArea.requestFocusInWindow();
        userPromptTextArea.requestFocus();
    }


}
