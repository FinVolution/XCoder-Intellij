package com.ppdai.bicoder.chat.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.ppdai.bicoder.chat.constant.BiCoderActionType;
import com.ppdai.bicoder.config.BiCoderBundle;
import com.ppdai.bicoder.config.PluginStaticConfig;
import com.ppdai.bicoder.utils.BalloonUtils;
import com.ppdai.bicoder.utils.EditorUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.event.MouseEvent;

import static java.util.Objects.requireNonNull;

public class InsertAction extends ButtonAction {

    private final String messageId;
    private final String messageType;

    public InsertAction(@NotNull Editor editor, String messageId, String messageType) {
        super(
                editor,
                BiCoderBundle.get("chat.action.insert.title"),
                BiCoderBundle.get("chat.action.insert.description"),
                PluginStaticConfig.INSERT_ICON,
                BiCoderActionType.INSERT_CODE);
        this.messageId = messageId;
        this.messageType = messageType;
    }

    @Override
    public void handleAction(@NotNull AnActionEvent event) {
        var project = requireNonNull(event.getProject());
        var locationOnScreen = ((MouseEvent) event.getInputEvent()).getLocationOnScreen();
        locationOnScreen.y = locationOnScreen.y - 16;
        if (EditorUtils.getSelectedEditor(project) != null) {
            EditorUtils.replaceMainEditorSelection(project, editor.getDocument().getText());
            BalloonUtils.showSuccessIconBalloon(BiCoderBundle.get("chat.action.insert.message.success"), PluginStaticConfig.ACCEPT_ICON, locationOnScreen);
        } else {
            BalloonUtils.showWarnIconBalloon(BiCoderBundle.get("chat.action.insert.message.failure"), PluginStaticConfig.WARNING_ICON, locationOnScreen);
        }
    }

}
