package com.ppdai.bicoder.chat.model;

import com.intellij.openapi.vfs.VirtualFile;

/**
 * chat功能上下文dto
 *
 */
public class ChatContextFile {
    private String type;
    private VirtualFile file;

    public ChatContextFile() {
    }

    public ChatContextFile(String type, VirtualFile file) {
        this.type = type;
        this.file = file;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public VirtualFile getFile() {
        return file;
    }

    public void setFile(VirtualFile file) {
        this.file = file;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ChatContextFile{");
        sb.append("type='").append(type).append('\'');
        sb.append(", file=").append(file);
        sb.append('}');
        return sb.toString();
    }
}
