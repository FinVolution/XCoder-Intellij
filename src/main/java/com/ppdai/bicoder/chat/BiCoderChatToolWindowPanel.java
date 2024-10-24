package com.ppdai.bicoder.chat;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultCompactActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.Disposer;
import com.ppdai.bicoder.chat.actions.ClearChatAction;
import com.ppdai.bicoder.chat.actions.CreateNewChatAction;
import com.ppdai.bicoder.chat.conversation.Conversation;
import com.ppdai.bicoder.chat.conversation.ConversationsStorage;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;


public class BiCoderChatToolWindowPanel extends SimpleToolWindowPanel {

    public BiCoderChatToolWindowPanel(@NotNull Project project, @NotNull Disposable parentDisposable) {
        super(true);
        initialize(project, parentDisposable);
    }

    private void initialize(Project project, Disposable parentDisposable) {
        BiCoderChatToolWindowTabPanel tabPanel = new BiCoderChatToolWindowTabPanel(project);
        BiCoderChatToolWindowTabbedPane tabbedPane = createTabbedPanel(project, tabPanel, parentDisposable);
        JComponent toolbarComponent = createActionToolbar(project, tabbedPane).getComponent();
        toolbarComponent.setLayout(new FlowLayout());

        setToolbar(toolbarComponent);
        setContent(tabbedPane);

        Disposer.register(parentDisposable, tabPanel);
    }

    private ActionToolbar createActionToolbar(Project project, BiCoderChatToolWindowTabbedPane tabbedPane) {
        DefaultCompactActionGroup actionGroup = new DefaultCompactActionGroup("TOOLBAR_ACTION_GROUP", false);
        actionGroup.add(new CreateNewChatAction(() -> {
            tabbedPane.replaceNewTab(new BiCoderChatToolWindowTabPanel(project));
            repaint();
            revalidate();
        }));
        actionGroup.add(new ClearChatAction(tabbedPane::resetCurrentlyActiveTabPanel));

        ActionToolbar toolbar = ActionManager.getInstance()
                .createActionToolbar("NAVIGATION_BAR_TOOLBAR", actionGroup, true);
        toolbar.setTargetComponent(this);
        return toolbar;
    }

    private BiCoderChatToolWindowTabbedPane createTabbedPanel(Project project, BiCoderChatToolWindowTabPanel tabPanel, Disposable parentDisposable) {
        BiCoderChatToolWindowTabbedPane tabbedPane = new BiCoderChatToolWindowTabbedPane(project, parentDisposable);
        tabbedPane.replaceNewTab(tabPanel);
        return tabbedPane;
    }
}