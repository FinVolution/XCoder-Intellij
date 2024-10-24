package com.ppdai.bicoder.model;

import java.util.Map;

/**
 * 类定义信息
 *
 */
public class ClassDefineInfo {
    /**
     * 文件路径名
     */
    private String filePath;

    /**
     * 类定义map
     */
    private Map<String, String> classNamesWithBody;

    public ClassDefineInfo() {
    }

    public ClassDefineInfo(String filePath, Map<String, String> classNamesWithBody) {
        this.filePath = filePath;
        this.classNamesWithBody = classNamesWithBody;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Map<String, String> getClassNamesWithBody() {
        return classNamesWithBody;
    }

    public void setClassNamesWithBody(Map<String, String> classNamesWithBody) {
        this.classNamesWithBody = classNamesWithBody;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ClassDefineInfo{");
        sb.append("filePath='").append(filePath).append('\'');
        sb.append(", classNamesWithBody=").append(classNamesWithBody);
        sb.append('}');
        return sb.toString();
    }
}
