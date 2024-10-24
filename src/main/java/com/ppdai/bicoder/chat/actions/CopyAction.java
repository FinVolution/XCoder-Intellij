package com.ppdai.bicoder.chat.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.ppdai.bicoder.chat.constant.BiCoderActionType;
import com.ppdai.bicoder.config.BiCoderBundle;
import com.ppdai.bicoder.config.PluginStaticConfig;
import com.ppdai.bicoder.utils.BalloonUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;

public class CopyAction extends ButtonAction {

    private final String messageId;
    private final String messageType;


    public CopyAction(@NotNull Editor editor, String messageId, String messageType) {
        super(
                editor,
                BiCoderBundle.get("chat.action.copy.title"),
                BiCoderBundle.get("chat.action.copy.description"),
                PluginStaticConfig.COPY_ICON,
                BiCoderActionType.COPY_CODE);
        this.messageId = messageId;
        this.messageType = messageType;
    }

    @Override
    public void handleAction(@NotNull AnActionEvent event) {
        StringSelection stringSelection = new StringSelection(editor.getDocument().getText());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);

        var locationOnScreen = ((MouseEvent) event.getInputEvent()).getLocationOnScreen();
        locationOnScreen.y = locationOnScreen.y - 16;

        BalloonUtils.showSuccessIconBalloon(BiCoderBundle.get("chat.action.copy.message.success"), PluginStaticConfig.ACCEPT_ICON, locationOnScreen);
    }
}
