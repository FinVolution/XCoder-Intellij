package com.ppdai.bicoder.chat;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.impl.ContextMenuPopupHandler;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import com.ppdai.bicoder.chat.actions.CopyAction;
import com.ppdai.bicoder.chat.actions.DiffAction;
import com.ppdai.bicoder.chat.actions.InsertAction;
import com.ppdai.bicoder.chat.actions.NewFileAction;
import com.ppdai.bicoder.chat.components.IconActionButton;
import com.ppdai.bicoder.chat.model.bo.MessageContextBo;
import com.ppdai.bicoder.utils.EditorUtils;
import com.ppdai.bicoder.utils.FileUtils;

import javax.swing.*;
import java.awt.*;

public class ResponseEditor extends JPanel implements Disposable {

    private final Editor editor;
    private final String language;
    private final String fileSuffix;
    private final String currentRequestId;
    private final MessageContextBo messageContextBo;
    private final boolean canCopy;
    private final boolean canInsert;
    private final boolean canNewFile;
    private final boolean canDiff;


    public ResponseEditor(
            Project project,
            String currentRequestId,
            MessageContextBo messageContextBo,
            String code,
            String markdownLanguage,
            Disposable disposableParent) {
        this(project, currentRequestId, messageContextBo, code, markdownLanguage, disposableParent, true, true, true, true);
    }

    public ResponseEditor(
            Project project,
            String currentRequestId,
            MessageContextBo messageContextBo,
            String code,
            String markdownLanguage,
            Disposable disposableParent, boolean canCopy, boolean canInsert, boolean canNewFile, boolean canDiff) {
        super(new BorderLayout());
        this.canCopy = canCopy;
        this.canInsert = canInsert;
        this.canNewFile = canNewFile;
        this.canDiff = canDiff;
        this.currentRequestId = currentRequestId;
        this.messageContextBo = messageContextBo;
        var extensionMapping = FileUtils.findLanguageExtensionMapping(markdownLanguage);
        language = extensionMapping.getKey();
        fileSuffix = extensionMapping.getValue();
        editor = EditorUtils.createEditor(project, fileSuffix, code);
        DefaultActionGroup group = new DefaultActionGroup();
        var editorEx = ((EditorEx) editor);
        editorEx.installPopupHandler(new ContextMenuPopupHandler.Simple(group));
        editorEx.setColorsScheme(EditorColorsManager.getInstance().getSchemeForCurrentUITheme());

        var settings = editorEx.getSettings();
        settings.setAdditionalColumnsCount(0);
        settings.setAdditionalLinesCount(0);
        settings.setAdditionalPageAtBottom(false);
        settings.setVirtualSpace(false);
        settings.setUseSoftWraps(false);
        settings.setLineMarkerAreaShown(true);
        settings.setGutterIconsShown(true);
        add(createHeaderComponent(), BorderLayout.NORTH);
        editor.getDocument().setReadOnly(true);
        add(editor.getComponent(), BorderLayout.SOUTH);

        Disposer.register(disposableParent, this);

    }

    @Override
    public void dispose() {
        EditorFactory.getInstance().releaseEditor(editor);
    }

    public Editor getEditor() {
        return editor;
    }

    private JPanel createHeaderComponent() {
        var headerComponent = new JPanel(new BorderLayout());
        headerComponent.setBorder(JBUI.Borders.compound(
                JBUI.Borders.customLine(JBColor.border(), 1, 1, 1, 1),
                JBUI.Borders.empty(8)));
        JBLabel jbLabel = new JBLabel(language);
        jbLabel.setFont(new Font("JetBrains Mono", Font.PLAIN, 14));
        headerComponent.add(jbLabel, BorderLayout.LINE_START);
        headerComponent.add(createHeaderActions(), BorderLayout.LINE_END);
        return headerComponent;
    }

    private JPanel createHeaderActions() {
        var wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        if (canDiff) {
            wrapper.add(Box.createHorizontalStrut(8));
            wrapper.add(new IconActionButton(new DiffAction(editor)));
        }
        if (canCopy) {
            wrapper.add(Box.createHorizontalStrut(8));
            wrapper.add(new IconActionButton(new CopyAction(editor, currentRequestId, messageContextBo.getMessageType())));
        }
        if (canInsert) {
            wrapper.add(Box.createHorizontalStrut(8));
            wrapper.add(new IconActionButton(new InsertAction(editor, currentRequestId, messageContextBo.getMessageType())));
        }
        if (canNewFile) {
            wrapper.add(Box.createHorizontalStrut(8));
            wrapper.add(new IconActionButton(new NewFileAction(editor, fileSuffix, currentRequestId, messageContextBo.getMessageType())));
        }
        return wrapper;
    }
}
