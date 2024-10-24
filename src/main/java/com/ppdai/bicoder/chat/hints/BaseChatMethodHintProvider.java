package com.ppdai.bicoder.chat.hints;

import com.intellij.codeInsight.hints.ImmediateConfigurable;
import com.intellij.codeInsight.hints.InlayHintsProvider;
import com.intellij.codeInsight.hints.SettingsKey;
import com.intellij.ide.DataManager;
import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.ppdai.bicoder.chat.constant.ChatAction;
import com.ppdai.bicoder.config.BiCoderBundle;
import com.ppdai.bicoder.config.PluginStaticConfig;
import com.ppdai.bicoder.utils.PsiUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseChatMethodHintProvider implements InlayHintsProvider<ChatMethodHintSettings> {
    private static final SettingsKey<ChatMethodHintSettings> KEY = new SettingsKey(PluginStaticConfig.PLUGIN_NAME);

    @Override
    public boolean isVisibleInSettings() {
        return true;
    }

    @NotNull
    @Override
    public SettingsKey<ChatMethodHintSettings> getKey() {
        return KEY;
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getName() {
        return PluginStaticConfig.PLUGIN_NAME;
    }

    @Nullable
    @Override
    public String getPreviewText() {
        return null;
    }

    @NotNull
    @Override
    public ImmediateConfigurable createConfigurable(@NotNull ChatMethodHintSettings chatHintSettings) {
        return listener -> {
            JPanel panel = new JPanel();
            panel.setVisible(false);
            return panel;
        };
    }

    @NotNull
    @Override
    public ChatMethodHintSettings createSettings() {
        return new ChatMethodHintSettings();
    }

    protected void onClick(@NotNull final Editor editor, @NotNull final PsiElement element, @NotNull MouseEvent event) {
        if (editor.getProject() == null) {
            return;
        }
        List<MyActionInfo> actionInfos = new ArrayList<>();
        actionInfos.add(new MyActionInfo(ChatAction.GENERATE_TESTS.getActionId(), BiCoderBundle.get("chat.action.generate.tests.action.title")));
        actionInfos.add(new MyActionInfo(ChatAction.EXPLAIN_CODE.getActionId(), ChatAction.EXPLAIN_CODE.getHint()));
        actionInfos.add(new MyActionInfo(ChatAction.DOC_CODE.getActionId(), ChatAction.DOC_CODE.getHint()));
        actionInfos.add(new MyActionInfo(ChatAction.OPTIMIZE_CODE.getActionId(), ChatAction.OPTIMIZE_CODE.getHint()));
        actionInfos.add(new MyActionInfo(ChatAction.ADD_FILE_CHAT_CONTENT.getActionId(), BiCoderBundle.get("chat.action.add.selected.code.action.title")));
        actionInfos.add(new MyActionInfo(ChatAction.START_EDIT_CODE.getActionId(), ChatAction.START_EDIT_CODE.getHint()));
        //todo 添加后续动作
        ListPopup listPopup = JBPopupFactory.getInstance().createListPopup(new BaseListPopupStep<>("", actionInfos) {
            @Override
            @NotNull
            public String getTextFor(MyActionInfo actionInfo) {
                return actionInfo.getText();
            }

            @Override
            @Nullable
            public PopupStep<?> onChosen(MyActionInfo actionInfo, boolean finalChoice) {
                ActionManager actionManager = ActionManager.getInstance();
                AnAction action = actionManager.getAction(actionInfo.getActionId());
                TextRange range = element.getTextRange();
                editor.getSelectionModel().setSelection(range.getStartOffset(), range.getEndOffset());
                DataContext dataContext = DataManager.getInstance().getDataContext(editor.getComponent());
                AnActionEvent event = new AnActionEvent(null, dataContext, "MenuPopup", new Presentation(), actionManager, 0);
                action.actionPerformed(event);
                return FINAL_CHOICE;
            }
        });
        listPopup.showInScreenCoordinates(editor.getComponent(), event.getLocationOnScreen());
    }

    protected static int getAnchorOffset(@NotNull PsiElement element) {
        if (PsiUtils.instanceOf(element, "com.jetbrains.python.psi.PyFunction")) {
            return element.getTextRange().getStartOffset();
        }
        for (PsiElement child : element.getChildren()) {
            if (!(child instanceof com.intellij.psi.PsiComment) && !(child instanceof com.intellij.psi.PsiWhiteSpace)) {
                return child.getTextRange().getStartOffset();
            }
        }
        return element.getTextRange().getStartOffset();
    }

    protected int findRealOffsetBySpace(@NotNull Editor editor, String linePrefix) {
        int tabWidth = editor.getSettings().getTabSize(editor.getProject());
        int totalOffset = 0;
        for (int i = 0; i < linePrefix.length(); i++) {
            if (linePrefix.charAt(i) == '\t') {
                totalOffset += tabWidth;
            }
        }
        return totalOffset;
    }

    @Override
    public boolean isLanguageSupported(@NotNull Language language) {
        return true;
    }

    static class MyActionInfo {
        private final String actionId;
        private final String text;

        public MyActionInfo(String actionId, String text) {
            this.actionId = actionId;
            this.text = text;
        }

        public String getActionId() {
            return actionId;
        }

        public String getText() {
            return text;
        }
    }
}