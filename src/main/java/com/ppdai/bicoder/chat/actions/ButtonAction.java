package com.ppdai.bicoder.chat.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.ppdai.bicoder.utils.BiCoderLoggerUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public abstract class ButtonAction extends AnAction {

    private final String actionType;
    protected final Editor editor;

    public ButtonAction(
            @NotNull Editor editor,
            String text,
            String description,
            Icon icon,
            String actionType) {
        super(text, description, icon);
        this.editor = editor;
        this.actionType = actionType;
    }

    public abstract void handleAction(@NotNull AnActionEvent e);

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        try {
            handleAction(e);
        } catch (Exception ex) {
            BiCoderLoggerUtils.getInstance(getClass()).warn(actionType + " action execute failure!", ex);
        }
    }
}