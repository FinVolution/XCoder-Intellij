package com.ppdai.bicoder.chat.components;

import com.intellij.openapi.project.Project;
import com.ppdai.bicoder.cache.ProjectCache;
import com.ppdai.bicoder.chat.model.ChatContext;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 上下文内容展示区域
 *
 */
public class ChatContextArea extends JPanel {

    private final Project project;

    /**
     * 用于判断添加上下文时,遗留的上下文是本次添加,还是上次提问添加
     */
    private boolean isLastAdd = false;

    public ChatContextArea(@NotNull Project project) {
        this.project = project;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    public void addChatContext(ChatContext chatContext) {
        if (isLastAdd) {
            clearChatContext();
        } else {
            List<ChatContext> chatContextList = project.getService(ProjectCache.class).getChatContextList();
            if (CollectionUtils.isNotEmpty(chatContextList)) {
                for (ChatContext context : chatContextList) {
                    //存在相同的上下文,不添加
                    if (context.equals(chatContext)) {
                        return;
                    }
                }
            }
        }
        project.getService(ProjectCache.class).addChatContext(chatContext);
        add(new SingleChatContextJPanel(chatContext, this::removeChatContext));
    }

    public void removeChatContext(String id) {
        project.getService(ProjectCache.class).removeChatContext(id);
        Component[] components = getComponents();
        for (Component component : components) {
            if (component instanceof SingleChatContextJPanel) {
                SingleChatContextJPanel singleChatContextJPanel = (SingleChatContextJPanel) component;
                if (singleChatContextJPanel.getChatContext().getId().equals(id)) {
                    remove(singleChatContextJPanel);
                    break;
                }
            }
        }
    }

    public List<ChatContext> getChatContextListInArea() {
        List<ChatContext> chatContextList = new ArrayList<>();
        Component[] components = getComponents();
        for (Component component : components) {
            if (component instanceof SingleChatContextJPanel) {
                SingleChatContextJPanel singleChatContextJPanel = (SingleChatContextJPanel) component;
                chatContextList.add(singleChatContextJPanel.getChatContext());
            }
        }
        return chatContextList;
    }

    public void clearChatContext() {
        project.getService(ProjectCache.class).clearChatContext();
        removeAll();
        setLastAdd(false);
    }

    public void setLastAdd(boolean lastAdd) {
        isLastAdd = lastAdd;
    }
}
