package com.ppdai.bicoder.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.ppdai.bicoder.chat.constant.TriggerCompleteCommand;
import com.ppdai.bicoder.config.UserSetting;
import com.ppdai.bicoder.handler.BiCoderCompletionHandler;
import com.ppdai.bicoder.model.BiCoderCodeCompletion;
import com.ppdai.bicoder.model.FirstLineCompletion;
import com.ppdai.bicoder.service.BiCoderService;
import com.ppdai.bicoder.utils.BiCoderLoggerUtils;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.StringJoiner;


/**
 * 用户按下Tab键时，触发接受代码补全
 *
 */
public class AcceptCompletionAction extends AnAction {

    private final BiCoderService biCoderService = ApplicationManager.getApplication().getService(BiCoderService.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        Caret caret = e.getData(CommonDataKeys.CARET);
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (!performAction(editor, caret, file)) {
            BiCoderLoggerUtils.getInstance(getClass()).warn("insert completion failed!");
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        UserSetting userSetting = UserSetting.getInstance();
        //用户必须插件不能处于禁用状态
        if (!userSetting.getEnablePlugin()) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }
        // 只有当前页面存在代码提示时才做插入操作
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (file == null) {
            return;
        }
        BiCoderCodeCompletion biCoderCodeCompletion = file.getUserData(BiCoderCompletionHandler.BI_CODER_CODE_COMPLETION);
        String[] hints = biCoderCodeCompletion == null ? null : biCoderCodeCompletion.getCodeCompletion();
        e.getPresentation().setEnabledAndVisible(hints != null && hints.length > 0);
    }

    public boolean performAction(Editor editor, Caret caret, VirtualFile file) {
        if (file == null) {
            return false;
        }
        BiCoderCodeCompletion biCoderCodeCompletion = file.getUserData(BiCoderCompletionHandler.BI_CODER_CODE_COMPLETION);
        String[] hints = biCoderCodeCompletion == null ? null : biCoderCodeCompletion.getCodeCompletion();
        if ((hints == null) || (hints.length == 0)) {
            return false;
        }
        file.putUserData(BiCoderCompletionHandler.BI_CODER_CODE_COMPLETION, null);
        Project project = editor.getProject();
        if (project == null) {
            return false;
        }
        WriteCommandAction.runWriteCommandAction(project, TriggerCompleteCommand.ACCEPT_COMMAND, null, () -> {
            int startCaretPosition = caret.getOffset();
            int currentCaretPosition = startCaretPosition;
            List<FirstLineCompletion> firstLineCompletionList = biCoderCodeCompletion.getFirstLineCompletionList();
            StringJoiner insertTextJoiner = new StringJoiner("");
            if (CollectionUtils.isNotEmpty(firstLineCompletionList)) {
                int existInsertPosition = 0;
                for (FirstLineCompletion firstLineCompletion : firstLineCompletionList) {
                    String insertText = firstLineCompletion.getInsertText();
                    int startPosition = firstLineCompletion.getStartPosition();
                    int insertPosition = startPosition - existInsertPosition;
                    editor.getDocument().insertString(currentCaretPosition + insertPosition, insertText);
                    editor.getCaretModel().moveToOffset(currentCaretPosition + insertPosition + insertText.length());
                    existInsertPosition = startPosition;
                    currentCaretPosition = caret.getOffset();
                }
                for (int i = 1; i < hints.length; i++) {
                    insertTextJoiner.add(hints[i]);
                }
            } else {
                for (String hint : hints) {
                    insertTextJoiner.add(hint);
                }
            }
            String otherInsertText = insertTextJoiner.toString();
            editor.getDocument().insertString(currentCaretPosition, otherInsertText);
            editor.getCaretModel().moveToOffset(currentCaretPosition + otherInsertText.length());
        });
        return true;
    }
}
