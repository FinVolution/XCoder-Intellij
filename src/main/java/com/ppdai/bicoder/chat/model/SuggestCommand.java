package com.ppdai.bicoder.chat.model;


import com.ppdai.bicoder.chat.constant.ChatAction;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * 推荐命令
 *
 */
public class SuggestCommand {
    private String commandText;
    private String actionId;

    private String hint;

    public SuggestCommand() {
    }

    public SuggestCommand(ChatAction chatAction) {
        this.commandText = chatAction.getCommandText();
        this.actionId = chatAction.getActionId();
        this.hint = chatAction.getHint();
    }

    public String getCommandText() {
        return commandText;
    }

    public void setCommandText(String commandText) {
        this.commandText = commandText;
    }

    public String getActionId() {
        return actionId;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
    }


    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SuggestCommand that = (SuggestCommand) o;

        return new EqualsBuilder().append(commandText, that.commandText).append(actionId, that.actionId).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(commandText).append(actionId).toHashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SuggestCommand{");
        sb.append("commandText='").append(commandText).append('\'');
        sb.append(", actionId='").append(actionId).append('\'');
        sb.append(", hint='").append(hint).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
