package com.ppdai.bicoder.chat.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;
import com.ppdai.bicoder.chat.BiCoderChatToolWindowContentManager;
import com.ppdai.bicoder.chat.BiCoderChatToolWindowTabPanel;
import com.ppdai.bicoder.chat.components.UserPromptTextArea;
import com.ppdai.bicoder.chat.constant.ChatAction;
import com.ppdai.bicoder.config.BiCoderBundle;
import com.ppdai.bicoder.config.PluginStaticConfig;
import com.ppdai.bicoder.utils.BalloonUtils;
import com.ppdai.bicoder.utils.EditorUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;


public class StartEditCode extends AnAction {

    private static final int INLINE_WIDTH = 400;

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Editor editor = anActionEvent.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            return;
        }
        if (!EditorUtils.hasSelection(editor)) {
            InputEvent inputEvent = anActionEvent.getInputEvent();
            Point locationOnScreen;
            if (inputEvent instanceof MouseEvent) {
                locationOnScreen = ((MouseEvent) inputEvent).getLocationOnScreen();
                locationOnScreen.y = locationOnScreen.y - 16;
                BalloonUtils.showWarnIconBalloon(BiCoderBundle.get("chat.action.generate.message.need.selected.code"), PluginStaticConfig.WARNING_ICON, locationOnScreen);
            } else {
                BalloonUtils.showWarnIconBalloonRelative(BiCoderBundle.get("chat.action.generate.message.need.selected.code"), PluginStaticConfig.WARNING_ICON, JBPopupFactory.getInstance().guessBestPopupLocation(editor));
            }
            return;
        }
        Project project = anActionEvent.getProject();
        assert project != null;
        BiCoderChatToolWindowTabPanel chatTab = project.getService(BiCoderChatToolWindowContentManager.class).findOrNewChatTab();
        UserPromptTextArea userPromptTextArea = new UserPromptTextArea(chatTab::sendMessage, project, true);
        int height = userPromptTextArea.getPreferredSize().height;
        JBPopup popup = JBPopupFactory.getInstance().createComponentPopupBuilder(userPromptTextArea, userPromptTextArea)
                .setTitle(null)
                .setMovable(true)
                .setFocusable(true)
                .setRequestFocus(true)
                .setMinSize(new Dimension(INLINE_WIDTH, height)
                ).createPopup();
        userPromptTextArea.setInlinePopup(popup);
        //监听内容去自适应大小
        userPromptTextArea.textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                resize();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                resize();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }

            private void resize() {
                SwingUtilities.invokeLater(() -> {
                    popup.setSize(new Dimension(INLINE_WIDTH, userPromptTextArea.getPreferredSize().height));
                });
            }
        });
        //显示位置在选中代码上方
        SelectionModel selectionModel = editor.getSelectionModel();
        int start = selectionModel.getSelectionStart();
        LogicalPosition startPos = editor.offsetToLogicalPosition(start);
        Point point = editor.logicalPositionToXY(new LogicalPosition(startPos.line, startPos.column));
        popup.show(new RelativePoint(editor.getContentComponent(), new Point(point.x, point.y - height - 5)));
        userPromptTextArea.setText(ChatAction.EDIT_CODE.getCommandText() + " ");
        userPromptTextArea.focus();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setText(BiCoderBundle.get("chat.action.start.edit.code.action.title"));
    }
}