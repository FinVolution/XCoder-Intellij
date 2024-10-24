package com.ppdai.bicoder.chat;

import com.ppdai.bicoder.chat.components.ResponsePanel;
import com.ppdai.bicoder.config.BiCoderBundle;
import com.ppdai.bicoder.config.PluginStaticConfig;
import com.ppdai.bicoder.config.UserSetting;
import com.ppdai.bicoder.utils.ThemeUtils;

import javax.swing.*;

class StandardChatToolWindowLandingPanel extends ResponsePanel {


    StandardChatToolWindowLandingPanel() {
        addContent(createContent());
    }

    private JTextPane createContent() {
        var description = createTextPane();
        description.setText("<html>" +
                String.format(
                        BiCoderBundle.get("chat.welcome.message"),
                        UserSetting.getInstance().getUsername(),
                        PluginStaticConfig.PLUGIN_NAME)
        );
        return description;
    }

    private JTextPane createTextPane() {
        var textPane = new JTextPane();
        textPane.setBackground(ThemeUtils.getPanelBackgroundColor());
        textPane.setContentType("text/html");
        textPane.putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, true);
        textPane.setFocusable(false);
        textPane.setEditable(false);
        return textPane;
    }
}
