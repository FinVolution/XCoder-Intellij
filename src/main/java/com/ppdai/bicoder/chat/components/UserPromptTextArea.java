package com.ppdai.bicoder.chat.components;

import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.*;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.JBColor;
import com.intellij.ui.ScrollingUtil;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.speedSearch.ListWithFilter;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.ppdai.bicoder.cache.ProjectCache;
import com.ppdai.bicoder.chat.BiCoderChatToolWindowContentManager;
import com.ppdai.bicoder.chat.InputSuggestCommandListCellRender;
import com.ppdai.bicoder.chat.completion.handler.BaseChatCompletionRequestHandler;
import com.ppdai.bicoder.chat.constant.ChatAction;
import com.ppdai.bicoder.chat.constant.EventUserDataKeyConstant;
import com.ppdai.bicoder.chat.constant.MessageType;
import com.ppdai.bicoder.chat.conversation.Message;
import com.ppdai.bicoder.chat.model.ChatContext;
import com.ppdai.bicoder.chat.model.SuggestCommand;
import com.ppdai.bicoder.config.BiCoderBundle;
import com.ppdai.bicoder.config.PluginStaticConfig;
import com.ppdai.bicoder.utils.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class UserPromptTextArea extends JPanel implements Disposable {

    private static final String TEXT_SUBMIT = "text-submit";
    private static final String INSERT_BREAK = "insert-break";
    private static final JBColor BACKGROUND_COLOR = JBColor.namedColor("Editor.SearchField.background", UIUtil.getTextFieldBackground());

    private static final List<SuggestCommand> COMMANDS =
            List.of(new SuggestCommand(ChatAction.CLEAR_CHAT)
                    , new SuggestCommand(ChatAction.EXPLAIN_CODE)
                    , new SuggestCommand(ChatAction.DOC_CODE)
                    , new SuggestCommand(ChatAction.OPTIMIZE_CODE)
                    , new SuggestCommand(ChatAction.GENERATE_TESTS)
                    , new SuggestCommand(ChatAction.EDIT_CODE)
                    , new SuggestCommand(ChatAction.SELECT_CONTEXT_FILE)
//                    , new SuggestCommand(ChatAction.SEARCH_CONTEXT)
            );

    public final JBTextArea textArea;

    private final int textAreaRadius = 16;
    private final Consumer<Message> onMessage;
    private final Project project;
    private JButton stopButton;
    private JButton sendButton;
    private boolean submitEnabled = true;
    private boolean isCommandPopupOpened;

    private BaseChatCompletionRequestHandler chatCompletionRequestHandler;

    private JBPopup commandPopup;
    private PopupChooserBuilder<SuggestCommand> commandPopupBuilder;
    private JBList<SuggestCommand> suggestCommandJBList;
    private ListWithFilter<String> suggestCommandListComponent;
    private JScrollBar suggestCommandListComponentScrollBar;

    private final boolean isInline;

    private JBPopup inlinePopup;

    public UserPromptTextArea(Consumer<Message> onMessage, Project project) {
        this(onMessage, project, false);
    }

    public UserPromptTextArea(Consumer<Message> onMessage, Project project, boolean isInline) {
        this.onMessage = onMessage;
        this.project = project;
        this.isInline = isInline;
        textArea = new JBTextArea();
        textArea.setOpaque(false);
        textArea.setBackground(BACKGROUND_COLOR);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.getEmptyText().setText(BiCoderBundle.get("chat.user.message.hint"));
        textArea.setBorder(JBUI.Borders.empty(8, 4));
        var input = textArea.getInputMap();
        input.put(KeyStroke.getKeyStroke("ENTER"), TEXT_SUBMIT);
        input.put(KeyStroke.getKeyStroke("shift ENTER"), INSERT_BREAK);
        textArea.getActionMap().put(TEXT_SUBMIT, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSubmit();
            }

            @Override
            public boolean isEnabled() {
                return true;
            }
        });
        textArea.addFocusListener(
                new FocusListener() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        UserPromptTextArea.super.paintBorder(UserPromptTextArea.super.getGraphics());
                    }

                    @Override
                    public void focusLost(FocusEvent e) {
                        UserPromptTextArea.super.paintBorder(UserPromptTextArea.super.getGraphics());
                    }
                });
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                if (e.getDocument().getLength() == 0) {
                    sendButton.setEnabled(false);
                }
                handleCommand();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                if (e.getDocument().getLength() >= 1) {
                    sendButton.setEnabled(true);
                }
                handleCommand();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
        updateFont();
        init();
    }

    /**
     * 处理生成当前可见的命令
     */
    private void handleCommand() {
        String input = textArea.getText().toLowerCase();
        List<SuggestCommand> visibleCommands = new ArrayList<>();
        if (input.startsWith("/") || input.startsWith("@")) {
            for (SuggestCommand command : COMMANDS) {
                if (command.getCommandText().startsWith(input)) {
                    visibleCommands.add(command);
                }
            }
        }
        initCommand(visibleCommands);
    }


    public void focus() {
        textArea.requestFocus();
        textArea.requestFocusInWindow();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, textAreaRadius, textAreaRadius);
        super.paintComponent(g);
    }

    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(JBUI.CurrentTheme.ActionButton.focusedBorder());
        if (textArea.isFocusOwner()) {
            g2.setStroke(new BasicStroke(1.5F));
        }
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, textAreaRadius, textAreaRadius);
    }

    @Override
    public Insets getInsets() {
        return JBUI.insets(6, 12, 6, 6);
    }

    public void setSubmitEnabled(boolean submitEnabled) {
        this.submitEnabled = submitEnabled;
        sendButton.setVisible(submitEnabled);
        stopButton.setVisible(!submitEnabled);
    }

    public void setTextAreaEnabled(boolean textAreaEnabled) {
        textArea.setEnabled(textAreaEnabled);
    }

    public void setCommandPopupOpened(boolean commandPopupOpened) {
        UserPromptTextArea.this.isCommandPopupOpened = commandPopupOpened;
    }

    private void handleSubmit() {
        if (UserPromptTextArea.this.isCommandPopupOpened) {
            triggerSuggestCommandSelection(null);
        } else {
            var text = textArea.getText();
            if (submitEnabled && !text.isEmpty()) {
                startChat(text);
            }
        }
    }


    public void setText(String text) {
        textArea.setText(text);
    }

    private void init() {
        setOpaque(false);
        setLayout(new BorderLayout());
        add(textArea, BorderLayout.CENTER);

        stopButton = createIconButton(AllIcons.Actions.Suspend, true, false, null);
        sendButton = createIconButton(PluginStaticConfig.SEND_ICON, false, true, this::handleSubmit);
        var flowLayout = new FlowLayout(FlowLayout.RIGHT);
        flowLayout.setHgap(8);
        JPanel iconsPanel = new JPanel(flowLayout);
        iconsPanel.add(sendButton);
        iconsPanel.add(stopButton);
        add(JBUI.Panels.simplePanel().addToBottom(iconsPanel), BorderLayout.EAST);
        initCommands();
        initListeners();
    }

    private void updateFont() {
        if (Registry.is("ide.find.use.editor.font", false)) {
            textArea.setFont(EditorUtil.getEditorFont());
        } else {
            textArea.setFont(UIManager.getFont("TextField.font"));
        }
    }

    private JButton createIconButton(Icon icon, boolean isEnable, boolean isVisible, @Nullable Runnable submitListener) {
        var button = SwingUtils.createIconButton(icon);
        if (submitListener != null) {
            button.addActionListener((e) -> handleSubmit());
        }
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setEnabled(isEnable);
        button.setVisible(isVisible);
        return button;
    }

    public void setRequestHandler(@NotNull BaseChatCompletionRequestHandler requestService) {
        this.chatCompletionRequestHandler = requestService;
        stopButton.addActionListener(e -> {
            stopCall();
        });
    }

    public void stopCall() {
        if (chatCompletionRequestHandler != null) {
            chatCompletionRequestHandler.cancel();
        }
        if (textArea.getDocument().getLength() >= 1) {
            sendButton.setEnabled(true);
        }
    }

    public void addListener(KeyListener listener) {
        if (this.textArea != null) {
            this.textArea.addKeyListener(listener);
        }
    }

    private void initCommands() {
        this.commandPopupBuilder = (PopupChooserBuilder<SuggestCommand>) JBPopupFactory.getInstance().createPopupChooserBuilder(new ArrayList<SuggestCommand>());
        this.suggestCommandJBList = (JBList<SuggestCommand>) this.commandPopupBuilder.getChooserComponent();
        this.suggestCommandJBList.setBackground(UIUtil.getListBackground());
        this.commandPopupBuilder.setRequestFocus(false);
        this.commandPopupBuilder.setMovable(false);
        this.commandPopupBuilder.setRenderer(new InputSuggestCommandListCellRender(this));
        this.suggestCommandJBList.getEmptyText().setText("");
        this.commandPopupBuilder.setVisibleRowCount(11);
        this.commandPopupBuilder.setItemChosenCallback(this::triggerSuggestCommandSelection);
        //增加回车监听
        suggestCommandJBList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    triggerSuggestCommandSelection(suggestCommandJBList.getSelectedValue());
                }
            }
        });
    }

    public void triggerSuggestCommandSelection(SuggestCommand command) {
        if (command == null) {
            command = this.suggestCommandJBList.getSelectedValue();
        }
        if (command == null) {
            BiCoderLoggerUtils.getInstance(getClass()).warn("can not find command");
            return;
        }
        String actionId = command.getActionId();
        if (StringUtils.isNotBlank(actionId)) {
            if (ChatAction.CLEAR_CHAT.getActionId().equals(actionId)) {
                project.getService(BiCoderChatToolWindowContentManager.class).clearCurrentChat();
            } else if (ChatAction.SELECT_CONTEXT_FILE.getActionId().equals(actionId) || ChatAction.SEARCH_CONTEXT.getActionId().equals(actionId)) {
                performAction(actionId, false);
            } else if (ChatAction.EDIT_CODE.getActionId().equals(actionId)) {
                this.setText(command.getCommandText() + " ");
                this.focus();
                return;
            } else {
                performAction(actionId, true);
            }
            this.setText("");
            this.exitInline();
        }
    }

    private void initListeners() {
        this.addListener(new KeyAdapter() {
            @Override
            public void keyPressed(@NotNull KeyEvent e) {
                String text = textArea.getText();
                if (StringUtils.isBlank(text)) {
                    return;
                }
                KeyStroke stroke = KeyStroke.getKeyStrokeForEvent(e);
                if (KeyStroke.getKeyStroke("pressed UP").equals(stroke)) {
                    ScrollingUtil.moveUp(suggestCommandJBList, e.getModifiersEx());
                } else if (KeyStroke.getKeyStroke("pressed DOWN").equals(stroke)) {
                    ScrollingUtil.moveDown(suggestCommandJBList, e.getModifiersEx());
                }
            }
        });
    }


    public void initCommand(@NotNull List<SuggestCommand> visibleCommands) {
        if (!this.textArea.isFocusOwner()) {
            return;
        }
        if (CollectionUtils.isNotEmpty(visibleCommands)) {
            if (PopUtil.isPopUsable(this.commandPopup)) {
                this.suggestCommandJBList.setModel(new CollectionListModel<>(visibleCommands));
                this.suggestCommandJBList.setSelectedIndex(0);
                ScrollingUtil.ensureIndexIsVisible(this.suggestCommandJBList, 0, 1);
                this.suggestCommandJBList.getEmptyText().setText("");
                adjustJbPopSize();
                Point point = this.getLocationOnScreen();
                int currentHeight = (int) this.commandPopup.getContent().getPreferredSize().getHeight();
                this.commandPopup.setLocation(new Point(point.x, point.y - currentHeight - 2));
                return;
            }
            if (this.commandPopup != null) {
                this.commandPopup.cancel();
            }
            this.suggestCommandJBList.setModel(new CollectionListModel<>(visibleCommands));
            this.commandPopup = this.commandPopupBuilder.createPopup();
            this.commandPopup.addListener(new JBPopupListener() {
                @Override
                public void onClosed(@NotNull LightweightWindowEvent event) {
                    setCommandPopupOpened(false);
                }

                @Override
                public void beforeShown(@NotNull LightweightWindowEvent event) {
                    setCommandPopupOpened(true);
                }

            });
            this.suggestCommandJBList.setSelectedIndex(0);
            this.suggestCommandJBList.getEmptyText().setText("");
            adjustJbPopSize();
            Point point = this.getLocationOnScreen();
            int currentHeight = (int) this.commandPopup.getContent().getPreferredSize().getHeight();
            this.commandPopup.showInScreenCoordinates(this, new Point(point.x, point.y - currentHeight - 18));
        } else {
            if (this.commandPopup != null) {
                this.commandPopup.cancel();
            }
        }

    }

    private void adjustJbPopSize() {
        this.suggestCommandListComponent = (ListWithFilter<String>) ((JPanel) this.commandPopup.getContent().getComponent(0)).getComponent(0);
        this.suggestCommandListComponent.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        this.suggestCommandListComponentScrollBar = this.suggestCommandListComponent.getScrollPane().getHorizontalScrollBar();
        int width = Math.max((this.getSize()).width, (this.getPreferredSize()).width);
        Dimension dimension = new Dimension(width, (this.suggestCommandListComponent.getPreferredSize()).height);
        dimension.height += (this.suggestCommandListComponentScrollBar.getPreferredSize()).height;
        this.commandPopup.setSize(dimension);
    }

    public void startChat(String text) {
        if (StringUtils.isNotBlank(text)) {
            if (text.startsWith(ChatAction.EDIT_CODE.getCommandText())) {
                String userMessage = text.substring(ChatAction.EDIT_CODE.getCommandText().length()).trim();
                performAction(ChatAction.EDIT_CODE.getActionId(), true, userMessage);
            } else {
                startCommonChat(text);
            }
        } else {
            startCommonChat(text);
        }
        this.exitInline();
    }

    /**
     * 开始通用聊天
     *
     * @param text 用户输入的文本
     */
    private void startCommonChat(String text) {
        Message message = new Message(text, MessageType.CHAT.getType());
        removeChatContextNotInConTextArea();
        List<ChatContext> currentChatContextList = project.getService(ProjectCache.class).getChatContextList();
        if (currentChatContextList == null) {
            currentChatContextList = new ArrayList<>();
        }
        this.addCurrentFileContextToMessage(currentChatContextList);
        message.setChatContexts(currentChatContextList);
        this.addCurrentSelectTextToMessage(message);
        project.getService(BiCoderChatToolWindowContentManager.class).showToolWindow();
        onMessage.accept(message);
        this.setText("");
    }

    /**
     * 移除不在聊天上下文区的上下文
     */
    private void removeChatContextNotInConTextArea() {
        List<ChatContext> chatContextList = project.getService(ProjectCache.class).getChatContextList();
        if (CollectionUtils.isEmpty(chatContextList)) {
            return;
        }
        List<ChatContext> chatContextListInArea = project.getService(BiCoderChatToolWindowContentManager.class).getChatContextListInArea();
        if (CollectionUtils.isEmpty(chatContextListInArea)) {
            project.getService(ProjectCache.class).clearChatContext();
            return;
        }
        List<ChatContext> chatContextListNotInArea = chatContextList.stream()
                .filter(chatContext -> !chatContextListInArea.contains(chatContext))
                .collect(Collectors.toList());
        chatContextListNotInArea.forEach(chatContext -> project.getService(ProjectCache.class).removeChatContext(chatContext.getId()));
    }

    /**
     * 将当前文件的上下文添加到消息中
     *
     * @param chatContextList 聊天上下文列表
     */
    private void addCurrentFileContextToMessage(@NotNull List<ChatContext> chatContextList) {
        ChatContext currentFileContext = this.getCurrentFileContext();
        addContextToMessage(chatContextList, currentFileContext);
    }

    /**
     * 将当前选中的文本添加到消息中
     *
     * @param message 消息
     */
    private void addCurrentSelectTextToMessage(@NotNull Message message) {
        Editor editor = EditorUtils.getSelectedEditor(project);
        //如果有选中内容，则将选中内容作为聊天上下文
        if (EditorUtils.hasSelection(editor)) {
            SelectionModel selectionModel = editor.getSelectionModel();
            VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
            assert virtualFile != null;
            String language = InfoUtils.getLanguage(virtualFile);
            String selectedText = selectionModel.getSelectedText();
            message.setSelectedCode(selectedText);
            message.setSelectedCodeLanguage(language);
        }
    }

    /**
     * 将上下文添加到消息中
     *
     * @param chatContextList 聊天上下文列表
     * @param context         上下文
     */
    private void addContextToMessage(@NotNull List<ChatContext> chatContextList, ChatContext context) {
        if (context != null) {
            //判断chatContextList中是否存在和currentSelectTextContext对象equals的对象
            for (ChatContext chatContext : chatContextList) {
                if (chatContext.equals(context)) {
                    return;
                }
            }
            chatContextList.add(context);
        }
    }

    /**
     * 获取当前文件的上下文
     */
    @Nullable
    private ChatContext getCurrentFileContext() {
        Editor selectedEditor = EditorUtils.getSelectedEditor(project);
        if (selectedEditor != null) {
            Document selectedEditorDocument = selectedEditor.getDocument();
            VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(selectedEditorDocument);
            if (virtualFile != null) {
                //增加当前文件到上下文中
                String codePath = InfoUtils.getRelativePathNoContainFileName(project, virtualFile);
                return new ChatContext(ChatContext.TYPE_FILE_LOCAL, selectedEditorDocument.getText(), codePath, virtualFile.getPath(), virtualFile.getName(), 1, selectedEditorDocument.getLineCount());
            }
        }
        return null;
    }


    /**
     * 执行action
     *
     * @param actionId           actionId
     * @param isNeedSelectedCode 是否必须选中的代码
     * @param userMessage        用户输入的消息
     */
    private void performAction(String actionId, boolean isNeedSelectedCode, String userMessage) {
        performAction(actionId, isNeedSelectedCode, false, userMessage);
    }

    private void performAction(String actionId, boolean isNeedSelectedCode, boolean isNeedUserMessage, String userMessage) {
        FileEditorManager editorManager = FileEditorManager.getInstance(project);
        ActionManager actionManager = ActionManager.getInstance();
        AnAction action = actionManager.getAction(actionId);
        Editor selectedEditor = editorManager.getSelectedTextEditor();
        DataContext dataContext;
        if (isNeedUserMessage) {
            if (StringUtils.isBlank(userMessage)) {
                var locationOnScreen = getLocationOnScreen();
                locationOnScreen.y = locationOnScreen.y - 16;
                BalloonUtils.showWarnIconBalloon(BiCoderBundle.get("chat.action.generate.message.need.userMessage"), PluginStaticConfig.WARNING_ICON, locationOnScreen);
                return;
            }
        }
        if (isNeedSelectedCode) {
            if (!EditorUtils.hasSelection(selectedEditor)) {
                var locationOnScreen = getLocationOnScreen();
                locationOnScreen.y = locationOnScreen.y - 16;
                BalloonUtils.showWarnIconBalloon(BiCoderBundle.get("chat.action.generate.message.need.selected.code"), PluginStaticConfig.WARNING_ICON, locationOnScreen);
                return;
            } else {
                dataContext = DataManager.getInstance().getDataContext(selectedEditor.getComponent());
            }
        } else {
            Component component = Optional.ofNullable(selectedEditor)
                    .map(Editor::getComponent)
                    .orElse(this);
            dataContext = DataManager.getInstance().getDataContext(component);
        }
        AnActionEvent event = new AnActionEvent(null, dataContext, "SuggestCommand", new Presentation(), actionManager, 0);
        if (StringUtils.isNotBlank(userMessage)) {
            event.getRequiredData(CommonDataKeys.PROJECT).putUserData(EventUserDataKeyConstant.USER_MESSAGE, userMessage);
        }
        action.actionPerformed(event);
        this.setText("");
    }

    private void exitInline() {
        if (isInline) {
            if (inlinePopup != null) {
                this.inlinePopup.dispose();
            }
            dispose();
        }
    }

    private void performAction(String actionId, boolean isNeedSelectedCode) {
        performAction(actionId, isNeedSelectedCode, null);
    }

    public JBPopup getCommandPopup() {
        return commandPopup;
    }

    public PopupChooserBuilder<SuggestCommand> getCommandPopupBuilder() {
        return commandPopupBuilder;
    }

    public JBList<SuggestCommand> getSuggestCommandJBList() {
        return suggestCommandJBList;
    }

    public ListWithFilter<String> getSuggestCommandListComponent() {
        return suggestCommandListComponent;
    }

    public JScrollBar getSuggestCommandListComponentScrollBar() {
        return suggestCommandListComponentScrollBar;
    }

    public void setInlinePopup(JBPopup inlinePopup) {
        this.inlinePopup = inlinePopup;
    }

    @Override
    public void dispose() {
        Disposer.dispose(this);
    }
}
