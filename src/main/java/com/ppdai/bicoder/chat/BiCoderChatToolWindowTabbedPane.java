package com.ppdai.bicoder.chat;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.JBMenuItem;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.util.ui.JBUI;
import com.ppdai.bicoder.chat.conversation.Conversation;
import com.ppdai.bicoder.chat.conversation.ConversationService;
import com.ppdai.bicoder.chat.conversation.ConversationsStorage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;


public class BiCoderChatToolWindowTabbedPane extends JBTabbedPane {

    private final Map<String, BiCoderChatToolWindowTabPanel> activeTabMapping = new TreeMap<>((o1, o2) -> {
        int n1 = Integer.parseInt(o1.replaceAll("\\D", "0"));
        int n2 = Integer.parseInt(o2.replaceAll("\\D", "0"));
        return Integer.compare(n1, n2);
    });
    private final Disposable parentDisposable;
    private final Project project;

    public BiCoderChatToolWindowTabbedPane(Project project, Disposable parentDisposable) {
        this.project = project;
        this.parentDisposable = parentDisposable;
        setTabComponentInsets(null);
        setComponentPopupMenu(new TabPopupMenu());
        addChangeListener(e -> refreshTabState());
    }

    public Map<String, BiCoderChatToolWindowTabPanel> getActiveTabMapping() {
        return activeTabMapping;
    }

    public void addNewTab(BiCoderChatToolWindowTabPanel toolWindowPanel) {
        var tabIndices = activeTabMapping.keySet().toArray(new String[0]);
        var nextIndex = 0;
        for (String title : tabIndices) {
            int tabNum = Integer.parseInt(title.replaceAll("\\D+", ""));
            if ((tabNum - 1) == nextIndex) {
                nextIndex++;
            } else {
                break;
            }
        }
        var title = "Chat";
        if (nextIndex > 0) {
            title = "Chat " + (nextIndex + 1);
        }
        super.insertTab(title, null, toolWindowPanel.getContent(), null, nextIndex);
        activeTabMapping.put(title, toolWindowPanel);
        super.setSelectedIndex(nextIndex);

        setTabComponentAt(nextIndex, createCloseableTabButtonPanel(title));
        SwingUtilities.invokeLater(toolWindowPanel::requestFocusForTextArea);
        Disposer.register(parentDisposable, toolWindowPanel);
    }

    /**
     * 替换当前的tab,保持只有一个tab
     *
     * @param toolWindowPanel
     */
    public void replaceNewTab(BiCoderChatToolWindowTabPanel toolWindowPanel) {
        clearAll();
        var title = "Chat";
        super.insertTab(title, null, toolWindowPanel.getContent(), null, 0);
        activeTabMapping.put(title, toolWindowPanel);
        super.setSelectedIndex(0);

        setTabComponentAt(0, createCloseableTabButtonPanel(title));
        SwingUtilities.invokeLater(toolWindowPanel::requestFocusForTextArea);
        Disposer.register(parentDisposable, toolWindowPanel);
    }


    public Optional<String> tryFindActiveConversationTitle(String conversationId) {
        return activeTabMapping.entrySet().stream()
                .filter(entry -> {
                    Conversation panelConversation = entry.getValue().getConversation();
                    return panelConversation != null && conversationId.equals(panelConversation.getId());
                })
                .findFirst()
                .map(Map.Entry::getKey);
    }

    public Optional<BiCoderChatToolWindowTabPanel> tryFindActiveTabPanel() {
        var selectedIndex = getSelectedIndex();
        if (selectedIndex == -1) {
            return Optional.empty();
        }

        return Optional.ofNullable(activeTabMapping.get(getTitleAt(selectedIndex)));
    }

    public void clearAll() {
        activeTabMapping.values().forEach(Disposer::dispose);
        removeAll();
        activeTabMapping.clear();
    }

    private void refreshTabState() {
        var selectedIndex = getSelectedIndex();
        if (selectedIndex == -1) {
            return;
        }

        var toolWindowPanel = activeTabMapping.get(getTitleAt(selectedIndex));
        if (toolWindowPanel != null) {
            var conversation = toolWindowPanel.getConversation();
            if (conversation != null) {
                ConversationsStorage.getInstance(project).setCurrentConversation(conversation);
            }
        }
    }

    public void resetCurrentlyActiveTabPanel() {
        tryFindActiveTabPanel().ifPresent(tabPanel -> {
            tabPanel.displayLandingView();
            tabPanel.setConversation(null);
        });
        Conversation currentConversation = ConversationsStorage.getInstance(project).getCurrentConversation();
        if (currentConversation != null) {
            ConversationService.getInstance(project).deleteConversation(currentConversation);
            ConversationsStorage.getInstance(project).setCurrentConversation(null);
        }
    }

    public void startGenerateTests(String promptText) {
        //todo

        tryFindActiveTabPanel().ifPresent(tabPanel -> {
        });
    }

    private JPanel createCloseableTabButtonPanel(String title) {
        var closeIcon = AllIcons.Actions.Close;
        var button = new JButton(closeIcon);
        button.addActionListener(new CloseActionListener(title));
        button.setPreferredSize(new Dimension(closeIcon.getIconWidth(), closeIcon.getIconHeight()));
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setContentAreaFilled(false);
        button.setToolTipText("Close Chat");
        button.setRolloverIcon(AllIcons.Actions.CloseHovered);

        var panel = JBUI.Panels.simplePanel(4, 0)
                .addToLeft(new JBLabel(title))
                .addToRight(button);
        panel.setOpaque(false);
        return panel;
    }

    class CloseActionListener implements ActionListener {

        private final String title;

        public CloseActionListener(String title) {
            this.title = title;
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            var tabIndex = indexOfTab(title);
            if (tabIndex >= 0) {
                Disposer.dispose(activeTabMapping.get(title));
                removeTabAt(tabIndex);
                activeTabMapping.remove(title);
            }
            if (activeTabMapping.isEmpty()) {
                addNewTab(new BiCoderChatToolWindowTabPanel(project));
            }
        }
    }

    class TabPopupMenu extends JPopupMenu {

        private int selectedPopupTabIndex = -1;

        TabPopupMenu() {
            add(createPopupMenuItem("Close", e -> {
                if (selectedPopupTabIndex > 0) {
                    activeTabMapping.remove(getTitleAt(selectedPopupTabIndex));
                    removeTabAt(selectedPopupTabIndex);
                }
            }));
            add(createPopupMenuItem("Close Other Tabs", e -> {
                var selectedPopupTabTitle = getTitleAt(selectedPopupTabIndex);
                var tabPanel = activeTabMapping.get(selectedPopupTabTitle);

                clearAll();
                addNewTab(tabPanel);
            }));
        }

        @Override
        public void show(Component invoker, int x, int y) {
            selectedPopupTabIndex = BiCoderChatToolWindowTabbedPane.this.getUI().tabForCoordinate(BiCoderChatToolWindowTabbedPane.this, x, y);
            if (selectedPopupTabIndex > 0) {
                super.show(invoker, x, y);
            }
        }

        private JBMenuItem createPopupMenuItem(String label, ActionListener listener) {
            var menuItem = new JBMenuItem(label);
            menuItem.addActionListener(listener);
            return menuItem;
        }
    }
}
