package com.ppdai.bicoder.model;

/**
 * class信息
 *
 */
public class ClassInfo {

    private String filePath;

    private String className;
    private String classText;

    public ClassInfo() {
    }

    public ClassInfo(String filePath, String className, String classText) {
        this.filePath = filePath;
        this.className = className;
        this.classText = classText;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassText() {
        return classText;
    }

    public void setClassText(String classText) {
        this.classText = classText;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ClassInfo{");
        sb.append("filePath='").append(filePath).append('\'');
        sb.append(", className='").append(className).append('\'');
        sb.append(", classText='").append(classText).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
