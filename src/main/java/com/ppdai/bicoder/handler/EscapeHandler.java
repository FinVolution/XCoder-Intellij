package com.ppdai.bicoder.handler;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.ppdai.bicoder.config.UserSetting;
import com.ppdai.bicoder.utils.CompletionUtils;
import org.jetbrains.annotations.NotNull;

/**
 * 用户按下ESC键取消显示补全结果
 *
 */
public class EscapeHandler extends EditorActionHandler {

    public static final String ACTION_ID = "EditorEscape";

    @Override
    public void doExecute(@NotNull Editor editor, Caret caret, DataContext dataContext) {
        CompletionUtils.cleanCodeCompletion(editor);
        super.doExecute(editor, caret, dataContext);
    }

    @Override
    public boolean isEnabledForCaret(@NotNull Editor editor, @NotNull Caret caret, DataContext dataContext) {
        UserSetting userSetting = UserSetting.getInstance();
        //用户必须插件不能处于禁用状态
        return userSetting.getEnablePlugin();
    }

}
