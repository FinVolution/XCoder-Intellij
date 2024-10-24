package com.ppdai.bicoder.chat.actions;

import com.intellij.ide.util.EditorHelper;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.ppdai.bicoder.chat.constant.BiCoderActionType;
import com.ppdai.bicoder.config.BiCoderBundle;
import com.ppdai.bicoder.config.PluginStaticConfig;
import com.ppdai.bicoder.utils.BalloonUtils;
import com.ppdai.bicoder.utils.BiCoderLoggerUtils;
import com.ppdai.bicoder.utils.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.event.MouseEvent;
import java.util.Objects;

import static com.intellij.openapi.ui.DialogWrapper.OK_EXIT_CODE;

public class NewFileAction extends ButtonAction {
    private final String fileSuffix;
    private final String messageId;
    private final String messageType;


    public NewFileAction(@NotNull Editor editor, String fileSuffix, String messageId, String messageType) {
        super(
                editor,
                BiCoderBundle.get("chat.action.new.file.title"),
                BiCoderBundle.get("chat.action.new.file.description"),
                PluginStaticConfig.NEW_FILE_ICON,
                BiCoderActionType.NEW_FILE);
        this.fileSuffix = fileSuffix;
        this.messageId = messageId;
        this.messageType = messageType;
    }

    @Override
    public void handleAction(@NotNull AnActionEvent event) {
        var project = Objects.requireNonNull(event.getProject());
        var fileChooserDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        fileChooserDescriptor.setForcedToUseIdeaFileChooser(true);
        var textFieldWithBrowseButton = new TextFieldWithBrowseButton();
        var selectedTextEditor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        String path = project.getBasePath();
        if (selectedTextEditor != null) {
            var document = selectedTextEditor.getDocument();
            VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
            if (virtualFile != null) {
                path = virtualFile.getParent().getPath();
            }
        }
        textFieldWithBrowseButton.setText(path);
        textFieldWithBrowseButton.addBrowseFolderListener(
                new TextBrowseFolderListener(fileChooserDescriptor, project));
        var timestamp = System.currentTimeMillis();
        var fileNameTextField = new JBTextField("temp" + timestamp + fileSuffix);
        fileNameTextField.setColumns(30);

        if (showDialog(project, textFieldWithBrowseButton, fileNameTextField) == OK_EXIT_CODE) {
            var file = FileUtils.createFile(
                    textFieldWithBrowseButton.getText(),
                    fileNameTextField.getText(),
                    editor.getDocument().getText());
            var virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
            var locationOnScreen = ((MouseEvent) event.getInputEvent()).getLocationOnScreen();
            locationOnScreen.y = locationOnScreen.y - 16;
            if (virtualFile == null) {
                BiCoderLoggerUtils.getInstance(getClass()).warn("Couldn't find the saved virtual file");
                BalloonUtils.showWarnIconBalloon(BiCoderBundle.get("chat.action.new.file.message.failure"), PluginStaticConfig.WARNING_ICON, locationOnScreen);
                return;
            }
            var psiFile = PsiManager.getInstance(project).findFile(virtualFile);
            if (psiFile == null) {
                BiCoderLoggerUtils.getInstance(getClass()).warn("Couldn't find the saved virtual file");
                BalloonUtils.showWarnIconBalloon(BiCoderBundle.get("chat.action.new.file.message.failure"), PluginStaticConfig.WARNING_ICON, locationOnScreen);
                return;
            }

            EditorHelper.openInEditor(psiFile);

            BalloonUtils.showSuccessIconBalloon(BiCoderBundle.get("chat.action.new.file.message.success"), PluginStaticConfig.ACCEPT_ICON, locationOnScreen);
        }
    }


    private int showDialog(
            Project project,
            TextFieldWithBrowseButton textFieldWithBrowseButton,
            JBTextField fileNameTextField) {
        var dialogBuilder = new DialogBuilder(project)
                .title(BiCoderBundle.get("chat.action.new.file.dialog.title"))
                .centerPanel(FormBuilder.createFormBuilder()
                        .addLabeledComponent(BiCoderBundle.get("chat.action.new.file.dialog.label.text.filename"), fileNameTextField)
                        .addLabeledComponent(BiCoderBundle.get("chat.action.new.file.dialog.label.text.save.path"), textFieldWithBrowseButton)
                        .getPanel());
        dialogBuilder.addOkAction();
        dialogBuilder.addCancelAction();
        return dialogBuilder.show();
    }
}
