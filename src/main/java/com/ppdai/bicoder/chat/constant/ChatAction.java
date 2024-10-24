package com.ppdai.bicoder.chat.constant;

import com.ppdai.bicoder.config.BiCoderBundle;

/**
 * actionId常量
 *
 */
public enum ChatAction {

    CLEAR_CHAT("BiCoder.ClearChat", "/clear", BiCoderBundle.get("chat.action.icon.chat.clear.tooltip")),
    CREATE_NEW_CHAT("BiCoder.CreateNewChat", "/newChat", BiCoderBundle.get("chat.action.icon.chat.new.tooltip")),
    DELETE_CONVERSATION("BiCoder.DeleteConversation", "/deleteChat", BiCoderBundle.get("chat.action.icon.conversation.delete.tooltip")),
    DELETE_ALL_CONVERSATION("BiCoder.DeleteAllConversation", "/deleteAllChat", BiCoderBundle.get("chat.action.icon.conversation.delete.all.tooltip")),
    GENERATE_TESTS("BiCoder.GenerateTests", "/tests", BiCoderBundle.get("chat.action.generate.tests.action.title")),

    START_EDIT_CODE("BiCoder.StartEditCode", "/startEdit", BiCoderBundle.get("chat.action.start.edit.code.action.title")),
    EDIT_CODE("BiCoder.EditCode", "/edit", BiCoderBundle.get("chat.action.start.edit.code.action.title")),

    EXPLAIN_CODE("BiCoder.ExplainCode", "/explain", BiCoderBundle.get("chat.action.start.inline.explain.code.action.title")),

    DOC_CODE("BiCoder.GenerateDoc", "/doc", BiCoderBundle.get("chat.action.start.inline.doc.code.action.title")),

    OPTIMIZE_CODE("BiCoder.OptimizeCode", "/optimize", BiCoderBundle.get("chat.action.start.inline.optimize.code.action.title")),

    SELECT_CONTEXT_FILE("BiCoder.SelectContextFile", "@file", BiCoderBundle.get("chat.action.select.context.file.title")),
    ADD_FILE_CHAT_CONTENT("BiCoder.AddFileChatContent", "@code", BiCoderBundle.get("chat.action.add.selected.code.action.title")),
    SEARCH_CONTEXT("BiCoder.SearchContext", "@search", BiCoderBundle.get("chat.action.search.context.title"));


    private String actionId;
    private String commandText;

    private String hint;

    ChatAction(String actionId, String commandText, String hint) {
        this.actionId = actionId;
        this.commandText = commandText;
        this.hint = hint;
    }

    public String getActionId() {
        return actionId;
    }

    public String getCommandText() {
        return commandText;
    }

    public String getHint() {
        return hint;
    }
}
