package com.ppdai.bicoder.chat.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * chat功能添加上下文
 *
 */
public class ChatContext {
    private final String id;
    private String type;
    private String content;
    private String filePath;
    private String fileName;
    private int startLine;
    private int endLine;

    private String systemFilePath;

    public static final String TYPE_FILE = "file";

    /**
     * 当前文件全文,也可以作为上下文传入
     */
    public static final String TYPE_FILE_LOCAL = "file@local";

    /**
     * 当前文件对应的单测文件
     */
    public static final String TYPE_FILE_LOCAL_TEST = "file@localTest";

    /**
     * 非当前文件对应的单测文件
     */
    public static final String TYPE_FILE_OTHER_TEST = "file@otherTest";

    /**
     * 前几个打开的文件
     */
    public static final String TYPE_FILE_OPENED = "file@opened";

    public ChatContext() {
        this.id = UUID.randomUUID().toString();
    }

    public ChatContext(@NotNull String type, String content, String filePath, @NotNull String systemFilePath, String fileName, int startLine, int endLine) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.content = content;
        this.filePath = filePath;
        this.systemFilePath = systemFilePath;
        this.fileName = fileName;
        this.startLine = startLine;
        this.endLine = endLine;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getStartLine() {
        return startLine;
    }

    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSystemFilePath() {
        return systemFilePath;
    }

    public void setSystemFilePath(String systemFilePath) {
        this.systemFilePath = systemFilePath;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ChatContext{");
        sb.append("id='").append(id).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", content='").append(content).append('\'');
        sb.append(", filePath='").append(filePath).append('\'');
        sb.append(", fileName='").append(fileName).append('\'');
        sb.append(", startLine=").append(startLine);
        sb.append(", endLine=").append(endLine);
        sb.append(", systemFilePath='").append(systemFilePath).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChatContext that = (ChatContext) o;
        return new EqualsBuilder().append(startLine, that.startLine).append(endLine, that.endLine).append(systemFilePath, that.systemFilePath).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(startLine).append(endLine).append(systemFilePath).toHashCode();
    }
}
