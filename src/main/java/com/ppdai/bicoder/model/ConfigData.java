package com.ppdai.bicoder.model;

import java.util.List;
import java.util.Map;

public class ConfigData {
    private List<String> allowVersions;
    private Integer maxConsecutiveInputIntervalTime;
    private Integer maxPrefixOffset;
    private Float maxPrefixOffsetPercent;
    private Integer maxSuffixOffset;
    private Float maxSuffixOffsetPercent;
    private Float similarityPercentage;
    private Integer validPrefixLength;
    private String validSuffixReg;
    private Map<String, List<String>> unitTestLanguageFrameworkMap;

    private List<String> fileWhitelist;

    public Integer maxCurrentOpenedEditorCacheUseSize;

    public ConfigData() {
    }

    public ConfigData(List<String> allowVersions, Integer maxConsecutiveInputIntervalTime, Integer maxPrefixOffset, Float maxPrefixOffsetPercent, Integer maxSuffixOffset, Float maxSuffixOffsetPercent, Float similarityPercentage, Integer validPrefixLength, String validSuffixReg, Map<String, List<String>> unitTestLanguageFrameworkMap, List<String> fileWhitelist, Integer maxCurrentOpenedEditorCacheUseSize) {
        this.allowVersions = allowVersions;
        this.maxConsecutiveInputIntervalTime = maxConsecutiveInputIntervalTime;
        this.maxPrefixOffset = maxPrefixOffset;
        this.maxPrefixOffsetPercent = maxPrefixOffsetPercent;
        this.maxSuffixOffset = maxSuffixOffset;
        this.maxSuffixOffsetPercent = maxSuffixOffsetPercent;
        this.similarityPercentage = similarityPercentage;
        this.validPrefixLength = validPrefixLength;
        this.validSuffixReg = validSuffixReg;
        this.unitTestLanguageFrameworkMap = unitTestLanguageFrameworkMap;
        this.fileWhitelist = fileWhitelist;
        this.maxCurrentOpenedEditorCacheUseSize = maxCurrentOpenedEditorCacheUseSize;
    }

    public List<String> getAllowVersions() {
        return allowVersions;
    }

    public void setAllowVersions(List<String> allowVersions) {
        this.allowVersions = allowVersions;
    }

    public Integer getMaxConsecutiveInputIntervalTime() {
        return maxConsecutiveInputIntervalTime;
    }

    public void setMaxConsecutiveInputIntervalTime(Integer maxConsecutiveInputIntervalTime) {
        this.maxConsecutiveInputIntervalTime = maxConsecutiveInputIntervalTime;
    }

    public Integer getMaxPrefixOffset() {
        return maxPrefixOffset;
    }

    public void setMaxPrefixOffset(Integer maxPrefixOffset) {
        this.maxPrefixOffset = maxPrefixOffset;
    }

    public Float getMaxPrefixOffsetPercent() {
        return maxPrefixOffsetPercent;
    }

    public void setMaxPrefixOffsetPercent(Float maxPrefixOffsetPercent) {
        this.maxPrefixOffsetPercent = maxPrefixOffsetPercent;
    }

    public Integer getMaxSuffixOffset() {
        return maxSuffixOffset;
    }

    public void setMaxSuffixOffset(Integer maxSuffixOffset) {
        this.maxSuffixOffset = maxSuffixOffset;
    }

    public Float getMaxSuffixOffsetPercent() {
        return maxSuffixOffsetPercent;
    }

    public void setMaxSuffixOffsetPercent(Float maxSuffixOffsetPercent) {
        this.maxSuffixOffsetPercent = maxSuffixOffsetPercent;
    }

    public Float getSimilarityPercentage() {
        return similarityPercentage;
    }

    public void setSimilarityPercentage(Float similarityPercentage) {
        this.similarityPercentage = similarityPercentage;
    }

    public Integer getValidPrefixLength() {
        return validPrefixLength;
    }

    public void setValidPrefixLength(Integer validPrefixLength) {
        this.validPrefixLength = validPrefixLength;
    }

    public String getValidSuffixReg() {
        return validSuffixReg;
    }

    public void setValidSuffixReg(String validSuffixReg) {
        this.validSuffixReg = validSuffixReg;
    }

    public Map<String, List<String>> getUnitTestLanguageFrameworkMap() {
        return unitTestLanguageFrameworkMap;
    }

    public void setUnitTestLanguageFrameworkMap(Map<String, List<String>> unitTestLanguageFrameworkMap) {
        this.unitTestLanguageFrameworkMap = unitTestLanguageFrameworkMap;
    }

    public List<String> getFileWhitelist() {
        return fileWhitelist;
    }

    public void setFileWhitelist(List<String> fileWhitelist) {
        this.fileWhitelist = fileWhitelist;
    }

    public Integer getMaxCurrentOpenedEditorCacheUseSize() {
        return maxCurrentOpenedEditorCacheUseSize;
    }

    public void setMaxCurrentOpenedEditorCacheUseSize(Integer maxCurrentOpenedEditorCacheUseSize) {
        this.maxCurrentOpenedEditorCacheUseSize = maxCurrentOpenedEditorCacheUseSize;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ConfigData{");
        sb.append("allowVersions=").append(allowVersions);
        sb.append(", maxConsecutiveInputIntervalTime=").append(maxConsecutiveInputIntervalTime);
        sb.append(", maxPrefixOffset=").append(maxPrefixOffset);
        sb.append(", maxPrefixOffsetPercent=").append(maxPrefixOffsetPercent);
        sb.append(", maxSuffixOffset=").append(maxSuffixOffset);
        sb.append(", maxSuffixOffsetPercent=").append(maxSuffixOffsetPercent);
        sb.append(", similarityPercentage=").append(similarityPercentage);
        sb.append(", validPrefixLength=").append(validPrefixLength);
        sb.append(", validSuffixReg='").append(validSuffixReg).append('\'');
        sb.append(", unitTestLanguageFrameworkMap=").append(unitTestLanguageFrameworkMap);
        sb.append(", fileWhitelist=").append(fileWhitelist);
        sb.append(", maxCurrentOpenedEditorCacheUseSize=").append(maxCurrentOpenedEditorCacheUseSize);
        sb.append('}');
        return sb.toString();
    }
}