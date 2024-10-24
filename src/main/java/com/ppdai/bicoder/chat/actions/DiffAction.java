package com.ppdai.bicoder.chat.actions;

import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffDialogHints;
import com.intellij.diff.DiffManager;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.diff.util.DiffUserDataKeys;
import com.intellij.diff.util.DiffUtil;
import com.intellij.diff.util.Side;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.util.Pair;
import com.intellij.testFramework.LightVirtualFile;
import com.ppdai.bicoder.chat.constant.BiCoderActionType;
import com.ppdai.bicoder.config.BiCoderBundle;
import com.ppdai.bicoder.config.PluginStaticConfig;
import com.ppdai.bicoder.utils.BalloonUtils;
import com.ppdai.bicoder.utils.EditorUtils;
import com.ppdai.bicoder.utils.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.event.MouseEvent;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class DiffAction extends ButtonAction {

    public DiffAction(@NotNull Editor editor) {
        super(
                editor,
                BiCoderBundle.get("chat.action.diff.title"),
                BiCoderBundle.get("chat.action.diff.description"),
                PluginStaticConfig.DIFF_CODE_ICON,
                BiCoderActionType.DIFF_CODE);
    }

    @Override
    public void handleAction(@NotNull AnActionEvent event) {
        var project = requireNonNull(event.getProject());
        var selectedTextEditor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (!EditorUtils.hasSelection(selectedTextEditor)) {
            var locationOnScreen = ((MouseEvent) event.getInputEvent()).getLocationOnScreen();
            locationOnScreen.y = locationOnScreen.y - 16;
            BalloonUtils.showWarnIconBalloon(BiCoderBundle.get("chat.action.diff.message.failure"), PluginStaticConfig.WARNING_ICON, locationOnScreen);
            return;
        }

        var resultEditorFile = FileUtils.getEditorFile(selectedTextEditor);
        LightVirtualFile tempSelectedTextFile = new LightVirtualFile(
                format("%s/%s", PathManager.getTempPath(), resultEditorFile.getName()),
                requireNonNull(selectedTextEditor.getSelectionModel().getSelectedText()));
        EditorUtils.disableHighlighting(project, tempSelectedTextFile);
        var diffContentFactory = DiffContentFactory.getInstance();
        var request = new SimpleDiffRequest(
                BiCoderBundle.get("chat.diff.title"),
                diffContentFactory.create(project, tempSelectedTextFile),
                diffContentFactory.create(project, FileUtils.getEditorFile(this.editor)),
                BiCoderBundle.get("chat.diff.select.content.title"),
                BiCoderBundle.get("chat.diff.suggest.content.title"));
        request.putUserData(
                DiffUserDataKeys.SCROLL_TO_LINE,
                Pair.create(Side.RIGHT, DiffUtil.getCaretPosition(selectedTextEditor).line));
        DiffManager.getInstance().showDiff(project, request, DiffDialogHints.DEFAULT);
    }
}
