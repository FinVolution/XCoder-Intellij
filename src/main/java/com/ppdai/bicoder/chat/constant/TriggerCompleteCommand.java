package com.ppdai.bicoder.chat.constant;

/**
 * 触发完成命令
 *
 */
public class TriggerCompleteCommand {
    public static final String ACCEPT_COMMAND = "BiCoder Accept";
    public static final String REPLACE_MAIN_EDITOR_SELECTION_COMMAND = "BiCoder ReplaceMainEditorSelection";
    public static final String UPDATE_CHAT_EDITOR_DOCUMENT_COMMAND = "BiCoder UpdateChatEditorDocument";
    public static final String HANDLE_CHAT_EDIT_CODE_COMMAND = "BiCoder HandleChatEditCode";
    public static final String ACCEPT_CHAT_EDIT_CODE_COMMAND = "BiCoder AcceptChatEditCode";
    public static final String HANDLE_CHAT_BASE_DIFF_CODE_COMMAND = "BiCoder HandleBaseDiffCode";
    public static final String ACCEPT_CHAT_BASE_DIFF_CODE_COMMAND = "BiCoder AcceptBaseDiffCode";

    public static final String HANDLE_TESTS_CODE_COMMAND = "BiCoder HandleTestsCode";
    public static final String ACCEPT_TESTS_CODE_COMMAND = "BiCoder AcceptTestsCode";
    public static final String RENDER_COMPLETION_CODE_COMMAND = "BiCoder RenderCompletionCode";

    public static final String VIM_MOTION_COMMAND = "Motion";
    public static final String UP_COMMAND = "Up";
    public static final String DOWN_COMMAND = "Down";
    public static final String LEFT_COMMAND = "Left";
    public static final String RIGHT_COMMAND = "Right";
    public static final String BACKSPACE_COMMAND = "Backspace";
    public static final String UNDO_BACKSPACE_COMMAND = "Undo Backspace";
    public static final String MOVE_CARET_COMMAND = "Move Caret";
}
