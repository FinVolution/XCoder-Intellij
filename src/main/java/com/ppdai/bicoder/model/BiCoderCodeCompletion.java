package com.ppdai.bicoder.model;

import java.util.Arrays;
import java.util.List;

/**
 * 代码建议
 */
public class BiCoderCodeCompletion {

    private String completionId;

    private String[] codeCompletion;

    private List<FirstLineCompletion> firstLineCompletionList;

    private boolean isCached;

    public BiCoderCodeCompletion() {
    }

    public BiCoderCodeCompletion(String completionId, String[] codeCompletion, boolean isCached) {
        this.completionId = completionId;
        this.codeCompletion = codeCompletion;
        this.isCached = isCached;
    }

    public String getCompletionId() {
        return completionId;
    }

    public void setCompletionId(String completionId) {
        this.completionId = completionId;
    }

    public String[] getCodeCompletion() {
        return codeCompletion;
    }

    public void setCodeCompletion(String[] codeCompletion) {
        this.codeCompletion = codeCompletion;
    }

    public List<FirstLineCompletion> getFirstLineCompletionList() {
        return firstLineCompletionList;
    }

    public void setFirstLineCompletionList(List<FirstLineCompletion> firstLineCompletionList) {
        this.firstLineCompletionList = firstLineCompletionList;
    }

    public boolean isCached() {
        return isCached;
    }

    public void setCached(boolean cached) {
        isCached = cached;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BiCoderCodeCompletion{");
        sb.append("completionId='").append(completionId).append('\'');
        sb.append(", codeCompletion=").append(Arrays.toString(codeCompletion));
        sb.append(", isCached=").append(isCached);
        sb.append('}');
        return sb.toString();
    }
}
