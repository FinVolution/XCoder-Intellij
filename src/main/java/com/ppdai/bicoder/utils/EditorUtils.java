package com.ppdai.bicoder.utils;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.ide.DataManager;
import com.intellij.injected.editor.EditorWindow;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.impl.ImaginaryEditor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.testFramework.LightVirtualFile;
import com.ppdai.bicoder.chat.constant.TriggerCompleteCommand;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

import static java.lang.String.format;

public class EditorUtils {


    public static boolean isEditorValidForAutocomplete(Editor editor) {
        return editor != null && isMainEditor(editor) && isEditorInstanceSupported(editor) && isEditorWritable(editor);
    }

    public static boolean isEditorInstanceSupported(@NotNull Editor editor) {
        return editor.getProject() != null
                && !editor.isDisposed()
                && !editor.isViewer()
                && !editor.isOneLineMode()
                && !(editor instanceof EditorWindow)
                && !(editor instanceof ImaginaryEditor)
                && (!(editor instanceof EditorEx) || !((EditorEx) editor).isEmbeddedIntoDialogWrapper());
    }

    public static boolean isEditorWritable(@NotNull Editor editor) {
        return editor.getDocument().isWritable();
    }

    /**
     * 判断当前编辑器是否是主编辑器
     *
     * @param editor 文本编辑器实例
     * @return 是否是主编辑器
     */
    public static boolean isMainEditor(Editor editor) {
        return EditorKind.MAIN_EDITOR.equals(editor.getEditorKind()) || ApplicationManager.getApplication().isUnitTestMode();
    }


    /**
     * 获取当前活跃的文本编辑器实例
     *
     * @param document 文本内容对象
     * @return 当前活跃的文本编辑器实例
     */
    @Nullable
    public static Editor getActiveEditor(@NotNull Document document) {
        if (!ApplicationManager.getApplication().isDispatchThread()) {
            return null;
        }

        Component focusOwner = IdeFocusManager.getGlobalInstance().getFocusOwner();
        DataContext dataContext = DataManager.getInstance().getDataContext(focusOwner);
        // ignore caret placing when exiting
        Editor activeEditor =
                ApplicationManager.getApplication().isDisposed()
                        ? null
                        : CommonDataKeys.EDITOR.getData(dataContext);

        if (activeEditor != null && activeEditor.getDocument() != document) {
            activeEditor = null;
        }

        return activeEditor;
    }


    public static Editor createEditor(@NotNull Project project, String fileSuffix, String code) {
        var timestamp = System.currentTimeMillis();
        var fileName = "temp_" + timestamp + fileSuffix;
        var lightVirtualFile = new LightVirtualFile(
                format("%s/%s", PathManager.getTempPath(), fileName),
                code);
        var existingDocument = ReadAction.compute(() -> FileDocumentManager.getInstance().getDocument(lightVirtualFile));
        var document = existingDocument != null
                ? existingDocument
                : EditorFactory.getInstance().createDocument(code);

        disableHighlighting(project, document);

        return EditorFactory.getInstance().createEditor(
                document,
                project,
                lightVirtualFile,
                false,
                EditorKind.MAIN_EDITOR);
    }

    public static Editor copyEditorAndDeleteSelectedCode(@NotNull Project project, @NotNull Editor editor, int startOffset, int endOffset) {
        var document = editor.getDocument();
        var code = document.getText();
        var newCode = code.substring(0, startOffset) + code.substring(endOffset);
        return getNewEditor(project, editor, newCode);
    }

    public static Editor copyEditor(@NotNull Project project, @NotNull Editor editor) {
        var document = editor.getDocument();
        var code = document.getText();
        return getNewEditor(project, editor, code);
    }

    public static void closeEditor(@NotNull Project project, @NotNull Editor editor) {
        VirtualFile file = FileDocumentManager.getInstance().getFile(editor.getDocument());
        if (file != null) {
            FileEditorManager.getInstance(project).closeFile(file);
        }
    }

    private static Editor getNewEditor(@NotNull Project project, @NotNull Editor editor, String code) {
        var resultEditorFile = FileUtils.getEditorFile(editor);
        var fileName = resultEditorFile.getName();
        var lightVirtualFile = new LightVirtualFile(fileName, code);
        var existingDocument = FileDocumentManager.getInstance().getDocument(lightVirtualFile);
        var newDocument = existingDocument != null
                ? existingDocument
                : EditorFactory.getInstance().createDocument(code);
        return EditorFactory.getInstance().createEditor(
                newDocument,
                project,
                lightVirtualFile,
                false,
                EditorKind.DIFF);
    }

    public static boolean hasSelection(@Nullable Editor editor) {
        return editor != null && editor.getSelectionModel().hasSelection();
    }

    public static @Nullable
    Editor getSelectedEditor(@NotNull Project project) {
        FileEditorManager editorManager = FileEditorManager.getInstance(project);
        return editorManager != null ? editorManager.getSelectedTextEditor() : null;
    }

    public static boolean isMainEditorTextSelected(@NotNull Project project) {
        return hasSelection(getSelectedEditor(project));
    }

    public static void replaceMainEditorSelection(@NotNull Project project, @NotNull String text) {
        var application = ApplicationManager.getApplication();
        application.invokeLater(() ->
                application.runWriteAction(() -> WriteCommandAction.runWriteCommandAction(project, TriggerCompleteCommand.REPLACE_MAIN_EDITOR_SELECTION_COMMAND, null, () -> {
                    var editor = getSelectedEditor(project);
                    if (editor != null) {
                        var selectionModel = editor.getSelectionModel();
                        editor.getDocument().replaceString(selectionModel.getSelectionStart(), selectionModel.getSelectionEnd(), text);
                        editor.getContentComponent().requestFocus();
                        selectionModel.removeSelection();
                    }
                })));
    }

    public static void disableHighlighting(@NotNull Project project, Document document) {
        var psiFile = ReadAction.compute(() -> PsiDocumentManager.getInstance(project).getPsiFile(document));
        if (psiFile != null) {
            DaemonCodeAnalyzer.getInstance(project).setHighlightingEnabled(psiFile, false);
        }
    }

    public static void disableHighlighting(@NotNull Project project, VirtualFile virtualFile) {
        var existingDocument = FileDocumentManager.getInstance().getDocument(virtualFile);
        if (existingDocument != null) {
            disableHighlighting(project, existingDocument);
        }
    }

    /**
     * 获取editor中选中的文本的开始行到结束行的索引范围
     *
     * @param selectedCodeEditor 选中的文本编辑器
     * @return 选中的文本的开始行到结束行的索引范围
     */
    @NotNull
    public static TextRange getSelectLineTextRange(@NotNull Editor selectedCodeEditor) {
        SelectionModel selectionModel = selectedCodeEditor.getSelectionModel();
        Document document = selectedCodeEditor.getDocument();
        //根据索引获取开始结束行数索引
        int selectionStart = selectionModel.getSelectionStart();
        int selectionEnd = selectionModel.getSelectionEnd();
        int startLine = document.getLineNumber(selectionStart);
        int endLine = document.getLineNumber(selectionEnd);
        int lineStartOffset = document.getLineStartOffset(startLine);
        int lineEndOffset = document.getLineEndOffset(endLine);
        return new TextRange(lineStartOffset, lineEndOffset);
    }

    /**
     * 判断当前命令是否需要排除自动补全
     *
     * @param command 命令
     * @return 是否需要触发自动补全
     */
    public static boolean isCommandExcluded(@Nullable String command) {
        return (StringUtils.isEmpty(command) ||
                command.equals(TriggerCompleteCommand.ACCEPT_COMMAND) ||
                isPluginCommand(command) ||
                command.equals(TriggerCompleteCommand.UP_COMMAND) ||
                command.equals(TriggerCompleteCommand.DOWN_COMMAND) ||
                command.equals(TriggerCompleteCommand.LEFT_COMMAND) ||
                command.equals(TriggerCompleteCommand.RIGHT_COMMAND) ||
                command.contains(TriggerCompleteCommand.VIM_MOTION_COMMAND) ||
                command.contains(TriggerCompleteCommand.MOVE_CARET_COMMAND));
    }

    public static boolean isPluginCommand(@Nullable String command) {
        return TriggerCompleteCommand.REPLACE_MAIN_EDITOR_SELECTION_COMMAND.equals(command) ||
                TriggerCompleteCommand.UPDATE_CHAT_EDITOR_DOCUMENT_COMMAND.equals(command) ||
                TriggerCompleteCommand.HANDLE_CHAT_EDIT_CODE_COMMAND.equals(command) ||
                TriggerCompleteCommand.ACCEPT_CHAT_EDIT_CODE_COMMAND.equals(command) ||
                TriggerCompleteCommand.HANDLE_CHAT_BASE_DIFF_CODE_COMMAND.equals(command) ||
                TriggerCompleteCommand.ACCEPT_CHAT_BASE_DIFF_CODE_COMMAND.equals(command) ||
                TriggerCompleteCommand.HANDLE_TESTS_CODE_COMMAND.equals(command) ||
                TriggerCompleteCommand.ACCEPT_TESTS_CODE_COMMAND.equals(command) ||
                TriggerCompleteCommand.RENDER_COMPLETION_CODE_COMMAND.equals(command);
    }
}
