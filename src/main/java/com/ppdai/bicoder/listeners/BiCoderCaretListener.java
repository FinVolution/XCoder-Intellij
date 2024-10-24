package com.ppdai.bicoder.listeners;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.ppdai.bicoder.handler.BiCoderCompletionHandler;
import com.ppdai.bicoder.utils.BiCoderLoggerUtils;
import com.ppdai.bicoder.utils.EditorUtils;
import org.jetbrains.annotations.NotNull;


/**
 * 光标监听器
 *
 */
public class BiCoderCaretListener implements CaretListener {

    private final BiCoderCompletionHandler biCoderCompletionHandler = ApplicationManager.getApplication().getService(BiCoderCompletionHandler.class);

    @Override
    public void caretPositionChanged(@NotNull CaretEvent event) {
        Editor focusedEditor = event.getEditor();
        //未获取到编辑器实例或者当前焦点编辑器不是主编辑器,直接忽视,不做任何处理
        if (!EditorUtils.isEditorValidForAutocomplete(focusedEditor)) {
            return;
        }
        String currentCommandName = CommandProcessor.getInstance().getCurrentCommandName();
        //如果是插件自身的命令,则忽视
        if (EditorUtils.isPluginCommand(currentCommandName)) {
            return;
        }
        Caret caret = event.getCaret();
        if (caret == null) {
            return;
        }
        int newPosition = caret.getOffset();
        try {
            biCoderCompletionHandler.triggerAutoCompletion(focusedEditor, newPosition);
        } catch (Throwable e) {
            BiCoderLoggerUtils.getInstance(getClass()).warn("triggerAutoCompletion error", e);
        }
    }

}
