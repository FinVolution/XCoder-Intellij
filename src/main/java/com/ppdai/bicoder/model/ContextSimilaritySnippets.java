package com.ppdai.bicoder.model;

/**
 * 上下文片段,及其相似度
 *
 */
public class ContextSimilaritySnippets {

    private String contextSnippetCode;
    private double similarity;

    public ContextSimilaritySnippets() {
    }

    public ContextSimilaritySnippets(String contextSnippetCode, double similarity) {
        this.contextSnippetCode = contextSnippetCode;
        this.similarity = similarity;
    }

    public String getContextSnippetCode() {
        return contextSnippetCode;
    }

    public void setContextSnippetCode(String contextSnippetCode) {
        this.contextSnippetCode = contextSnippetCode;
    }

    public double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ContextSimilaritySnippets{");
        sb.append("contextSnippetCode='").append(contextSnippetCode).append('\'');
        sb.append(", similarity=").append(similarity);
        sb.append('}');
        return sb.toString();
    }
}
