package com.ppdai.bicoder.chat.components;

import com.intellij.icons.AllIcons.Actions;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBFont;
import com.intellij.util.ui.JBUI;
import com.ppdai.bicoder.config.BiCoderBundle;
import com.ppdai.bicoder.config.PluginStaticConfig;
import com.ppdai.bicoder.utils.ThemeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class ResponsePanel extends JPanel {

    private final Header header;
    private final Body body;

    public ResponsePanel() {
        super(new BorderLayout());
        header = new Header();
        body = new Body();
        add(header, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
    }

    public void enableActions(boolean enabled){
        header.enableActions(enabled);
    }

    public ResponsePanel withReloadAction(Runnable onReload) {
        header.addReloadAction(onReload);
        return this;
    }

    public ResponsePanel withDeleteAction(Runnable onDelete) {
        header.addDeleteAction(onDelete);
        return this;
    }

    public ResponsePanel addContent(JComponent content) {
        body.addContent(content);
        return this;
    }

    public void updateContent(JComponent content) {
        body.updateContent(content);
    }

    public JComponent getContent() {
        return body.getContent();
    }

    static class Header extends JPanel {

        private final JPanel iconsWrapper;

        Header() {
            super(new BorderLayout());
            setBackground(ThemeUtils.getPanelBackgroundColor());
            setBorder(JBUI.Borders.empty(12, 8, 4, 8));
            add(getIconLabel(), BorderLayout.LINE_START);

            iconsWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            iconsWrapper.setBackground(getBackground());
            add(iconsWrapper, BorderLayout.LINE_END);
        }

        public void enableActions(boolean enabled) {
            for (Component iconButton : iconsWrapper.getComponents()) {
                iconButton.setEnabled(enabled);
            }
        }

        public void addReloadAction(Runnable onReload) {
            addIconActionButton(new IconActionButton(
                    new AnAction(BiCoderBundle.get("chat.action.icon.response.reload.tooltip"), "Reload response description", Actions.Refresh) {
                        @Override
                        public void actionPerformed(@NotNull AnActionEvent e) {
                            enableActions(false);
                            onReload.run();
                        }
                    }));
        }

        public void addDeleteAction(Runnable onDelete) {
            addIconActionButton(new IconActionButton(
                    new AnAction(BiCoderBundle.get("chat.action.icon.response.delete.tooltip"), "Delete response description", Actions.GC) {
                        @Override
                        public void actionPerformed(@NotNull AnActionEvent e) {
                            onDelete.run();
                        }
                    }));
        }

        private void addIconActionButton(IconActionButton iconActionButton) {
            if (iconsWrapper.getComponents() != null && iconsWrapper.getComponents().length > 0) {
                iconsWrapper.add(Box.createHorizontalStrut(8));
            }
            iconsWrapper.add(iconActionButton);
        }

        private JBLabel getIconLabel() {
            return new JBLabel(PluginStaticConfig.PLUGIN_NAME, PluginStaticConfig.BI_CODER_ICON, SwingConstants.LEADING)
                    .setAllowAutoWrapping(true)
                    .withFont(JBFont.label().asBold());
        }
    }

    static class Body extends JPanel {

        private @Nullable
        JComponent content;

        Body() {
            super(new BorderLayout());
            setBackground(ThemeUtils.getPanelBackgroundColor());
            setBorder(JBUI.Borders.compound(
                    JBUI.Borders.customLine(JBColor.border(), 0, 0, 1, 0),
                    JBUI.Borders.empty(4, 8, 8, 8)));
        }

        public void addContent(JComponent content) {
            this.content = content;
            add(content);
        }

        public void updateContent(JComponent content) {
            removeAll();
            revalidate();
            repaint();
            addContent(content);
        }

        public @Nullable
        JComponent getContent() {
            return content;
        }
    }
}
