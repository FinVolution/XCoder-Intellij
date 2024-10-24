package com.ppdai.bicoder.listeners;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.BulkAwareDocumentListener;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.ppdai.bicoder.chat.constant.TriggerCompleteCommand;
import com.ppdai.bicoder.handler.BiCoderCompletionHandler;
import com.ppdai.bicoder.utils.BiCoderLoggerUtils;
import com.ppdai.bicoder.utils.CompletionUtils;
import com.ppdai.bicoder.utils.EditorUtils;
import org.jetbrains.annotations.NotNull;

public class BiCoderDocumentListener implements BulkAwareDocumentListener {

    private final BiCoderCompletionHandler biCoderCompletionHandler = ApplicationManager.getApplication().getService(BiCoderCompletionHandler.class);


    @Override
    public void documentChangedNonBulk(@NotNull DocumentEvent event) {
        Document document = event.getDocument();
        Editor editor = EditorUtils.getActiveEditor(document);
        //未获取到编辑器实例或者当前焦点编辑器不是主编辑器,直接忽视,不做任何处理
        if (!EditorUtils.isEditorValidForAutocomplete(editor)) {
            return;
        }
        String currentCommandName = CommandProcessor.getInstance().getCurrentCommandName();
        //如果是插件自身的命令,则忽视
        if (EditorUtils.isPluginCommand(currentCommandName)) {
            return;
        }
        //获取输入后预期光标位置
        int exceptCursorPosition = event.getOffset() + event.getNewLength();
        int caretPosition = editor.getCaretModel().getOffset();
        //整体catch,避免错误被idea捕获抛出到用户界面
        try {
            if (exceptCursorPosition == caretPosition) {
                biCoderCompletionHandler.triggerAutoCompletion(editor, exceptCursorPosition);
            }else if(TriggerCompleteCommand.UNDO_BACKSPACE_COMMAND.equals(currentCommandName)){
                //如果是撤销操作,则清除渲染
                CompletionUtils.cleanCodeCompletion(editor);
            }
        } catch (Throwable e) {
            BiCoderLoggerUtils.getInstance(getClass()).warn("triggerAutoCompletion error", e);
        }

    }


}
