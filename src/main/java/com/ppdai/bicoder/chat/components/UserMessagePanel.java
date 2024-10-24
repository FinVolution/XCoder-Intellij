package com.ppdai.bicoder.chat.components;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBFont;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.ppdai.bicoder.chat.components.markdown.MarkdownPanel;
import com.ppdai.bicoder.chat.constant.MessageType;
import com.ppdai.bicoder.chat.conversation.Message;
import com.ppdai.bicoder.chat.model.ChatContext;
import com.ppdai.bicoder.chat.model.bo.MessageContextBo;
import com.ppdai.bicoder.config.UserSetting;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class UserMessagePanel extends JPanel {

    public UserMessagePanel(Project project, Message message, Disposable parentDisposable) {
        super(new BorderLayout());
        setBorder(JBUI.Borders.compound(
                JBUI.Borders.customLine(JBColor.border(), 0, 0, 1, 0),
                JBUI.Borders.empty(12, 8, 8, 8)));
        add(createDisplayNameWrapper(), BorderLayout.NORTH);
        if (CollectionUtils.isNotEmpty(message.getChatContexts())) {
            add(createChatContexts(message.getChatContexts()), BorderLayout.CENTER);
        }
        add(createUserMessageTextPane(project, message, parentDisposable), BorderLayout.SOUTH);
    }

    private JPanel createDisplayNameWrapper() {
        return JBUI.Panels.simplePanel()
                .withBorder(JBUI.Borders.emptyBottom(6))
                .addToLeft(new JBLabel(UserSetting.getInstance().getUsername())
                        .setAllowAutoWrapping(true)
                        .withFont(JBFont.label().asBold()));
    }

    private JPanel createChatContexts(@NotNull List<ChatContext> chatContexts) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        for (ChatContext chatContext : chatContexts) {
            panel.add(new SingleChatContextJPanel(chatContext));
        }
        return panel;
    }

    private JPanel createUserMessageTextPane(Project project, Message message, Disposable parentDisposable) {
        var userMessage = message.getUserMessage();
        var selectedCode = message.getSelectedCode();
        var selectedCodeLanguage = message.getSelectedCodeLanguage();
        var prompt = new StringBuilder();
        if (StringUtils.isNotBlank(userMessage)) {
            prompt.append(userMessage);
        }
        if (StringUtils.isNotBlank(selectedCode)) {
            prompt.append("\n\n");
            prompt.append("```");
            if (StringUtils.isNotBlank(selectedCodeLanguage)) {
                prompt.append(selectedCodeLanguage);
            }
            prompt.append("\n");
            prompt.append(selectedCode);
            prompt.append("\n");
            prompt.append("```");
        }
        if (StringUtils.isNotBlank(MessageType.getCnNameByType(message.getType()))) {
            prompt.append("\n");
            prompt.append(MessageType.getCnNameByType(message.getType()));
        }
        return new MarkdownPanel(project, UIUtil.getPanelBackground(), new MessageContextBo(message.getId(),message.getType()), false, parentDisposable, false, false, false, false).withResponse(prompt.toString());
    }
}
