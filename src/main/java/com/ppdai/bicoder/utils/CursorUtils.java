package com.ppdai.bicoder.utils;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.ppdai.bicoder.config.PluginStaticConfig;
import com.ppdai.bicoder.config.UserSetting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 光标工具类
 *
 */
public class CursorUtils {

    /**
     * 获取当前行的光标前代码
     *
     * @param document       文本内容对象
     * @param cursorPosition 光标位置
     * @return 当前行的光标前代码
     */
    @Nullable
    public static String getCurrentLineCursorPrefix(@NotNull Document document, int cursorPosition) {
        try {
            if (cursorPosition > document.getTextLength()) {
                cursorPosition = document.getTextLength();
            }
            int lineNumber = document.getLineNumber(cursorPosition);
            int lineStart = document.getLineStartOffset(lineNumber);

            return document.getText(TextRange.create(lineStart, cursorPosition));
        } catch (Exception e) {
            BiCoderLoggerUtils.getInstance(CursorUtils.class).warn("Failed to get current line cursor prefix code: ", e);
            return null;
        }
    }

    /**
     * 获取当前行的光标后代码
     *
     * @param document       文本内容对象
     * @param cursorPosition 光标位置
     * @return 当前行的光标后代码
     */
    @Nullable
    public static String getCurrentLineCursorSuffix(@NotNull Document document, int cursorPosition) {
        try {
            if (cursorPosition > document.getTextLength()) {
                return "";
            }
            int lineNumber = document.getLineNumber(cursorPosition);
            int lineEnd = document.getLineEndOffset(lineNumber);

            return document.getText(TextRange.create(cursorPosition, lineEnd));
        } catch (Exception e) {
            BiCoderLoggerUtils.getInstance(CursorUtils.class).warn("Failed to get current line cursor suffix code: ", e);
            return null;
        }
    }


    /**
     * 获取设置允许的最大光标前代码
     * 如果第一行不完整,则取第二行的起始位置
     *
     * @param document       文本内容对象
     * @param cursorPosition 光标位置
     * @return 光标前代码
     */
    public static String getMaxCursorPrefix(@NotNull Document document, int cursorPosition) {
        try {
            int begin = Integer.max(0, cursorPosition - UserSetting.getInstance().getMaxPrefixOffset());
            // 取的第一行的行号
            int firstLine = document.getLineNumber(begin);
            // 取的第一行的开始位置
            int firstLineStartOffset = document.getLineStartOffset(firstLine);
            if (begin > firstLineStartOffset) {
                // 取的第二行的起始位置
                begin = document.getLineStartOffset(firstLine + 1);
            }
            //如果预期的光标位置大于文档结束位置,可能是因为做了大批量删除或撤销动作,导致文档长度变短,此时直接采集整个文档
            if (cursorPosition > document.getTextLength()) {
                cursorPosition = document.getTextLength();
            }
            return document.getText(TextRange.create(begin, cursorPosition));
        } catch (Exception e) {
            BiCoderLoggerUtils.getInstance(CursorUtils.class).warn("Failed to get max cursor prefix code: ", e);
            return "";
        }
    }

    /**
     * 获取设置允许的最大光标后代码
     * 如果最后一行不完整,则取最后第二行的结束位置
     *
     * @param document       文本内容对象
     * @param cursorPosition 光标位置
     * @return 光标后代码
     */
    public static String getMaxCursorSuffix(@NotNull Document document, int cursorPosition) {
        try {
            int end = Integer.min(document.getTextLength(), cursorPosition + UserSetting.getInstance().getMaxSuffixOffset());
            // 取的最后一行的行号
            int lastLine = document.getLineNumber(end);
            // 取的最后一行的结束位置
            int lastLineEndOffset = document.getLineEndOffset(lastLine);
            if (end < lastLineEndOffset) {
                // 取的最后第二行的结束位置
                end = document.getLineEndOffset(lastLine - 1);
            }
            //如果预期的结束位置小于文档结束位置,可能是因为做了大批量撤销动作,导致文档长度变短,此时直接不做后续采集
            if (end < cursorPosition) {
                return "";
            }
            return document.getText(TextRange.create(cursorPosition, end));
        } catch (Exception e) {
            BiCoderLoggerUtils.getInstance(CursorUtils.class).warn("Failed to get max cursor suffix code: ", e);
            return "";
        }
    }

}
