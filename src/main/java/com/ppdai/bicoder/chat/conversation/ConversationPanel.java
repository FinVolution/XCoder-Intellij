package com.ppdai.bicoder.chat.conversation;

import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBFont;
import com.intellij.util.ui.JBUI;
import com.ppdai.bicoder.chat.BiCoderChatToolWindowContentManager;
import com.ppdai.bicoder.chat.actions.DeleteConversationAction;
import com.ppdai.bicoder.chat.components.IconActionButton;
import com.ppdai.bicoder.utils.ThemeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;

class ConversationPanel extends JPanel {

    private final Project project;

    ConversationPanel(
            @NotNull Project project,
            @NotNull Conversation conversation,
            @NotNull Runnable onDelete) {
        super(new BorderLayout());
        setBackground(JBColor.background());
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                project.getService(BiCoderChatToolWindowContentManager.class)
                        .displayConversation(conversation);
            }
        });
        this.project = project;
        addStyles(isSelected(conversation));
        addTextPanel(conversation, onDelete);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private boolean isSelected(Conversation conversation) {
        var currentConversation = ConversationsStorage.getInstance(project).getCurrentConversation();
        return currentConversation != null && currentConversation.getId().equals(conversation.getId());
    }

    private void addStyles(boolean isSelected) {
        var border = isSelected ?
                JBUI.Borders.customLine(JBUI.CurrentTheme.ActionButton.focusedBorder(), 2, 2, 2, 2) :
                JBUI.Borders.customLine(JBColor.border(), 1, 0, 1, 0);
        setBackground(ThemeUtils.getPanelBackgroundColor());
        setBorder(JBUI.Borders.compound(border, JBUI.Borders.empty(8)));
        setLayout(new GridBagLayout());
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void addTextPanel(Conversation conversation, Runnable onDelete) {
        var constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        add(createTextPanel(conversation, onDelete), constraints);
    }

    private JPanel createTextPanel(Conversation conversation, Runnable onDelete) {
        var headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setBorder(JBUI.Borders.emptyBottom(12));

        var gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        headerPanel.setBackground(ThemeUtils.getPanelBackgroundColor());
        headerPanel.add(new JBLabel(getFirstUserMessage(conversation))
                .withFont(JBFont.label().asBold()), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0;
        headerPanel.add(new IconActionButton(new DeleteConversationAction(onDelete)), gbc);

        var bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(ThemeUtils.getPanelBackgroundColor());
        bottomPanel.add(new JLabel(conversation.getUpdatedOn()
                .format(DateTimeFormatter.ofPattern("M/d/yyyy, h:mm:ss a"))), BorderLayout.WEST);
        var textPanel = new JPanel(new BorderLayout());
        textPanel.setBackground(ThemeUtils.getPanelBackgroundColor());
        textPanel.add(headerPanel, BorderLayout.NORTH);
        textPanel.add(bottomPanel, BorderLayout.SOUTH);
        return textPanel;
    }

    private String getFirstUserMessage(Conversation conversation) {
        var messages = conversation.getMessages();
        if (messages.isEmpty()) {
            return "";
        }
        String userMessage = messages.get(0).getUserMessage();
        String selectedCode = messages.get(0).getSelectedCode();
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
        return stringBuilder.toString();
    }
}
