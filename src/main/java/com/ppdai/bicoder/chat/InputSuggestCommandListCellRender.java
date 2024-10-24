package com.ppdai.bicoder.chat;

import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.panels.OpaquePanel;
import com.intellij.ui.speedSearch.SpeedSearchUtil;
import com.intellij.util.text.Matcher;
import com.intellij.util.text.MatcherHolder;
import com.intellij.util.ui.UIUtil;
import com.ppdai.bicoder.chat.components.UserPromptTextArea;
import com.ppdai.bicoder.chat.model.SuggestCommand;
import com.ppdai.bicoder.utils.SwingUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class InputSuggestCommandListCellRender
        extends DefaultListCellRenderer {
    private static final int MIN_RESERVED_TEXT_WIDTH = 30;

    private static final int MIN_COMMAND_TEXT_WIDTH = 15;
    UserPromptTextArea inputPanel;

    public InputSuggestCommandListCellRender(UserPromptTextArea inputPanel) {
        this.inputPanel = inputPanel;
    }

    @Override
    public Component getListCellRendererComponent(JList<?> paramJList, Object paramObject, int paramInt, boolean isSelected, boolean cellHasFocus) {
        if (!(paramObject instanceof SuggestCommand)) {
            return super.getListCellRendererComponent(paramJList, paramObject, paramInt, isSelected, cellHasFocus);
        }
        OpaquePanel opaquePanel = new OpaquePanel(new BorderLayout());
        opaquePanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 0));
        Component component = (new SuggestPromptCellRender(this.inputPanel)).getListCellRendererComponent(paramJList, paramObject, paramInt, isSelected, cellHasFocus);
        Color bgColor = component.getBackground();
        opaquePanel.add(component, "West");
        opaquePanel.setBackground(isSelected ? UIUtil.getListSelectionBackground(true) : bgColor);
        return opaquePanel;
    }

    class SuggestPromptCellRender
            extends ColoredListCellRenderer {
        private static final String PROMPT_ELLIPSIS = "...";
        private static final int NLP_ITEM_SUB_PADDING = 60;
        UserPromptTextArea inputPanel;

        private SuggestPromptCellRender(UserPromptTextArea inputPanel) {
            this.inputPanel = inputPanel;
        }

        @Override
        protected void customizeCellRenderer(@NotNull JList paramJList, Object paramObject, int paramInt, boolean isSelected, boolean cellHasFocus) {
            Matcher matcher = MatcherHolder.getAssociatedMatcher(paramJList);
            Color color2 = UIUtil.getListBackground();
            Color color1 = UIUtil.getListForeground();
            setPaintFocusBorder((cellHasFocus && UIUtil.isToUseDottedCellBorder()));
            if (paramObject instanceof SuggestCommand) {
                SuggestCommand suggestPrompt = (SuggestCommand) paramObject;
                String command = suggestPrompt.getCommandText();
                String hint = suggestPrompt.getHint();
                if (command.length() < MIN_COMMAND_TEXT_WIDTH) {
                    //command长度不够,补齐空格到MIN_COMMAND_TEXT_WIDTH长度,以便对齐
                    command = command + " ".repeat(MIN_COMMAND_TEXT_WIDTH - command.length());
                }
                SimpleTextAttributes commandAttributes = new SimpleTextAttributes(0, color1);
                SimpleTextAttributes hintAttributes = new SimpleTextAttributes(0, JBColor.GRAY);
                SpeedSearchUtil.appendColoredFragmentForMatcher(command, this, commandAttributes, matcher, color2, isSelected);
                SpeedSearchUtil.appendColoredFragmentForMatcher(hint, this, hintAttributes, matcher, color2, isSelected);
            }
            setBackground(isSelected ? UIUtil.getListSelectionBackground(true) : color2);
        }

        /**
         * 截断过长的文本,显示省略,暂无用,已改成scroll
         *
         * @param text
         * @param isSelected
         * @return
         */
        private String truncateText(String text, boolean isSelected) {
            if (isSelected) {
                int searchBarWidth = Math.max((this.inputPanel.getSize()).width, (this.inputPanel.getPreferredSize()).width);
                int popupWidth = (InputSuggestCommandListCellRender.this.inputPanel.getSuggestCommandListComponent() == null) ? 0 : (InputSuggestCommandListCellRender.this.inputPanel.getSuggestCommandListComponent().getPreferredSize()).width;
                int reservedWidth = (int) SwingUtils.getStringFontSize(PROMPT_ELLIPSIS, getFont()).getWidth();
                int remainWidth = Math.max(popupWidth, searchBarWidth) - reservedWidth - NLP_ITEM_SUB_PADDING;
                remainWidth = Math.max(MIN_RESERVED_TEXT_WIDTH, remainWidth);
                StringBuilder sb = new StringBuilder();
                int bufferWidth = 0;
                for (char ch : text.toCharArray()) {
                    int charWidth = (int) SwingUtils.getStringFontSize(String.valueOf(ch), getFont()).getWidth();
                    bufferWidth += charWidth;
                    if (bufferWidth < remainWidth) {
                        sb.append(ch);
                    }
                }
                if (!sb.toString().equals(text)) {
                    sb.append(PROMPT_ELLIPSIS);
                }
                return sb.toString();
            }
            return text;
        }
    }
}
