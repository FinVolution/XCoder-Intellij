package com.ppdai.bicoder.chat.components;

import com.intellij.icons.AllIcons;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.ppdai.bicoder.chat.model.ChatContext;
import com.ppdai.bicoder.config.PluginStaticConfig;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * 单个上下文内容展示区域
 *
 */
public class SingleChatContextJPanel extends JPanel {

    private final ChatContext chatContext;

    private final Consumer<String> onRemove;

    public SingleChatContextJPanel(@NotNull ChatContext chatContext, @NotNull Consumer<String> onRemove) {
        this.chatContext = chatContext;
        this.onRemove = onRemove;
        renderChatContext(chatContext, true);
    }

    public SingleChatContextJPanel(@NotNull ChatContext chatContext) {
        this.chatContext = chatContext;
        this.onRemove = null;
        renderChatContext(chatContext, false);
    }

    private void renderChatContext(@NotNull ChatContext chatContext, boolean showCloseButton) {
        setOpaque(false);
        setLayout(new GridBagLayout());
        setBackground(getBackground());
        var gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        Icon jLabelIcon = null;
        if (chatContext.getType().startsWith(ChatContext.TYPE_FILE)) {
            jLabelIcon = PluginStaticConfig.FILE_TYPE_ICON;
        }
        StringBuilder label = new StringBuilder();
        if (jLabelIcon == null) {
            label.append(chatContext.getType());
            label.append(" ");
        }
        label.append(chatContext.getFileName());
        label.append(":");
        int startLine = chatContext.getStartLine();
        int endLine = chatContext.getEndLine();
        if (startLine == endLine) {
            label.append(startLine);
        } else {
            label.append(startLine);
            label.append("-");
            label.append(endLine);
        }
        label.append(" ");
        label.append(chatContext.getFilePath());
        JLabel jLabel = new JLabel(label.toString(), jLabelIcon, JLabel.LEADING);
        jLabel.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));
        jLabel.setToolTipText(label.toString());
        add(jLabel, gbc);
        if (showCloseButton) {
            Icon closeIcon = AllIcons.Actions.Close;
            var removeButton = new JButton(closeIcon);
            removeButton.setMinimumSize(new Dimension(closeIcon.getIconWidth(), closeIcon.getIconHeight()));
            removeButton.setPreferredSize(new Dimension(closeIcon.getIconWidth(), closeIcon.getIconHeight()));
            removeButton.setBorder(BorderFactory.createEmptyBorder());
            removeButton.setContentAreaFilled(false);
            removeButton.setToolTipText("remove content");
            removeButton.setRolloverIcon(AllIcons.Actions.CloseHovered);
            removeButton.addActionListener(
                    e -> {
                        onRemove.accept(chatContext.getId());
                    }
            );
            gbc.gridx = 1;
            gbc.weightx = 0;
            add(removeButton, gbc);
        }
        setBorder(JBUI.Borders.empty(4, 2));
    }


    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
        super.paintComponent(g);
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(JBUI.CurrentTheme.ActionButton.pressedBorder());
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
    }

    public ChatContext getChatContext() {
        return chatContext;
    }
}
