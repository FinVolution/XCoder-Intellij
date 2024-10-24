package com.ppdai.bicoder.utils;


import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.InlayModel;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.ppdai.bicoder.config.UserSetting;
import com.ppdai.bicoder.handler.BiCoderCompletionHandler;
import com.ppdai.bicoder.model.BiCoderCodeCompletion;
import com.ppdai.bicoder.renderer.CodeGenHintRenderer;
import com.ppdai.bicoder.service.BiCoderService;
import org.apache.commons.text.similarity.LevenshteinDistance;

/**
 * 代码提示处理工具类
 *
 */
public class CompletionUtils {

    public static void cleanCodeCompletion(Editor focusedEditor) {
        if (focusedEditor != null) {
            Document document = focusedEditor.getDocument();
            VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
            if (virtualFile != null) {
                BiCoderCodeCompletion codeCompletion = virtualFile.getUserData(BiCoderCompletionHandler.BI_CODER_CODE_COMPLETION);
                if (codeCompletion != null && !codeCompletion.isCached()) {
                    BiCoderService biCoderService = ApplicationManager.getApplication().getService(BiCoderService.class);
                    virtualFile.putUserData(BiCoderCompletionHandler.BI_CODER_CODE_COMPLETION, null);
                }
            }
            InlayModel inlayModel = focusedEditor.getInlayModel();
            inlayModel.getInlineElementsInRange(0, focusedEditor.getDocument().getTextLength(), CodeGenHintRenderer.class).forEach(Disposable::dispose);
            inlayModel.getBlockElementsInRange(0, focusedEditor.getDocument().getTextLength(), CodeGenHintRenderer.class).forEach(Disposable::dispose);
        }
    }

    /**
     * 比较两个代码段的相似度,使用编辑距离算法
     *
     * @param sample      代码段样本
     * @param currentCode 当前代码段
     * @return 相似度是否达标
     */
    public static boolean similarityComparison(String sample, String currentCode) {
        float similarityPercentage = UserSetting.getInstance().getSimilarityPercentage();
        float currentSimilarity = getSimilarityPercentage(sample, currentCode);
        return currentSimilarity > similarityPercentage;
    }

    /**
     * 获取两个字符串的相似度
     *
     * @param sample      代码段样本
     * @param currentCode 当前代码段
     * @return 相似度
     */
    public static float getSimilarityPercentage(String sample, String currentCode) {
        int distance = LevenshteinDistance.getDefaultInstance().apply(sample, currentCode);
        return 1f - (float) distance / Math.max(sample.length(), currentCode.length());
    }

    /**
     * 判断相似度是否达标
     *
     * @param currentSimilarity 当前相似度
     * @return 相似度是否达标
     */
    public static boolean similarityComparison(float currentSimilarity) {
        float similarityPercentage = UserSetting.getInstance().getSimilarityPercentage();
        return currentSimilarity > similarityPercentage;
    }


}
