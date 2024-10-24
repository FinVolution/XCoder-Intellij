package com.ppdai.bicoder.utils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.ppdai.bicoder.config.PluginStaticConfig;
import org.apache.commons.lang3.StringUtils;

/**
 * 格式化代码工具类
 *
 */
public class FormatUtils {
    public static String reformatText(Project project, VirtualFile virtualFile, String text) {
        // 创建临时PsiFile,用于格式化代码
        PsiFile tempPsiFile = PsiFileFactory.getInstance(project).createFileFromText("temp", virtualFile.getFileType(), text);
        CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(project);
        codeStyleManager.reformat(tempPsiFile);
        PsiDocumentManager.getInstance(project).commitDocument(tempPsiFile.getViewProvider().getDocument());
        // 获取格式化后的代码
        String text1 = tempPsiFile.getText();
        return text1;
    }

    /**
     * 处理重复代码
     *
     * @param text 代码
     * @return 处理后的代码
     */
    public static String handleRepetitiveCode(String text) {
        if (StringUtils.isBlank(text) || text.length() <= 2) {
            return text;
        }
        String judgeText = text + text;
        //去除首尾如果能够匹配,说明存在重复代码
        if (!judgeText.substring(1, judgeText.length() - 1).contains(text)) {
            return text;
        }
        for (int i = 1; i <= text.length(); i++) {
            String subText = judgeText.substring(i, text.length() + i);
            if (text.equals(subText)) {
                return text.substring(0, i);
            }
        }
        return text;
    }

    /**
     * 处理最后一个有效字符,如果在白名单中,并且索引后第一个有效字符与该字符相同,则删除该字符后内容
     *
     * @param text   代码
     * @param editor 编辑器
     * @return 处理后的代码
     */
    public static String handleLastChar(String text, Editor editor) {
        if (StringUtils.isNotBlank(text)) {
            String lastChar = "";
            int charIndex = -1;
            int length = text.length();
            for (int i = length - 1; i >= 0; i--) {
                char c = text.charAt(i);
                if (c != ' ' && c != '\n' && c != '\t') {
                    lastChar = String.valueOf(c);
                    charIndex = i;
                    break;
                }
            }
            if (PluginStaticConfig.END_SYMBOL_WHITELIST.contains(lastChar)) {
                Document document = editor.getDocument();
                Integer offset = ApplicationManager.getApplication().runReadAction((Computable<Integer>) () -> editor.getCaretModel().getOffset());
                //如果光标在最后,则不处理,否则会导致索引越界
                if (offset >= document.getTextLength() - 1) {
                    return text;
                }
                String suffixText = document.getText(new TextRange(offset, document.getTextLength() - 1));
                if (StringUtils.isNotBlank(suffixText)) {
                    String firstChar = "";
                    int suffixTextLength = suffixText.length();
                    for (int i = 0; i < suffixTextLength; i++) {
                        char c = suffixText.charAt(i);
                        if (c != ' ' && c != '\n' && c != '\t') {
                            firstChar = String.valueOf(c);
                            break;
                        }
                    }
                    if (firstChar.equals(lastChar)) {
                        text = text.substring(0, charIndex);
                    }
                }
            }
        }
        return text;


    }
}
