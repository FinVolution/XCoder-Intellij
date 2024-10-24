package com.ppdai.bicoder.chat;

import com.intellij.openapi.Disposable;
import com.ppdai.bicoder.chat.conversation.Conversation;
import com.ppdai.bicoder.chat.conversation.Message;
import com.ppdai.bicoder.chat.model.ChatContext;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public interface ChatToolWindowTabPanel extends Disposable {

  JPanel getContent();

  @Nullable Conversation getConversation();

  void setConversation(@Nullable Conversation conversation);

  void displayLandingView();

  void displayConversation(Conversation conversation);

  void startNewConversation(Message message);

  void sendMessage(Message message);

  void requestFocusForTextArea();

  void addChatContext(ChatContext chatContext);

  List<ChatContext> getChatContextListInArea();

  /**
   * 开始edit功能
   */
  void startEditCode();
}
