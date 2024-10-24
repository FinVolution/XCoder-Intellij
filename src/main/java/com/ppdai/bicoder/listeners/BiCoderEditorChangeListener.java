package com.ppdai.bicoder.listeners;

import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.ppdai.bicoder.cache.ProjectCache;
import com.ppdai.bicoder.chat.model.ChatContext;
import com.ppdai.bicoder.config.PluginStaticConfig;
import com.ppdai.bicoder.config.UserSetting;
import com.ppdai.bicoder.utils.EditorUtils;
import com.ppdai.bicoder.utils.InfoUtils;
import org.jetbrains.annotations.NotNull;

/**
 * 光标监听器
 *
 */
public class BiCoderEditorChangeListener implements FileEditorManagerListener {

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        var commandName = CommandProcessor.getInstance().getCurrentCommandName();
        //自身plugin生成的editor排除
        if (EditorUtils.isPluginCommand(commandName)) {
            return;
        }
        FileEditor newEditor = event.getNewEditor();
        //排除非文本编辑器和非主界面的编辑器界面
        if (newEditor instanceof TextEditor && EditorUtils.isEditorInstanceSupported(((TextEditor) newEditor).getEditor())) {
            VirtualFile newFile = newEditor.getFile();
            Project project = event.getManager().getProject();
            //如果不是项目内文件，则排除
            ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(project).getFileIndex();
            if (!projectFileIndex.isInContent(newFile)) {
                return;
            }
            //如果文件大小大于最大文件大小，则排除
//            long size = newFile.getLength();
//            if (size > PluginStaticConfig.OPENED_EDITOR_CACHE_FILE_MAX_SIZE) {
//                return;
//            }
            Document document = FileDocumentManager.getInstance().getDocument(newFile);
            if (document != null) {
                int lineCount = document.getLineCount();
                //如果文件行数小于最小行数或者大于最大行数，则排除
                if (lineCount < UserSetting.getInstance().getOpenedEditorCacheFileMinLine() || lineCount > UserSetting.getInstance().getOpenedEditorCacheFileMaxLine()) {
                    return;
                }
                String codePath = InfoUtils.getRelativePathNoContainFileName(project, newFile);
                ChatContext chatContext = new ChatContext(ChatContext.TYPE_FILE_OPENED, document.getText(), codePath, newFile.getPath(), newFile.getName(), 1, lineCount);
                project.getService(ProjectCache.class).addCurrentOpenedEditorContext(newFile.getPath(), chatContext);
            }
        }
    }


    @Override
    public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        Project project = source.getProject();
        project.getService(ProjectCache.class).removeCurrentOpenedEditorContext(file.getPath());
    }
}
