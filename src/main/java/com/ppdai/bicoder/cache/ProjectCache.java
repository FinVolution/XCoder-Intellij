package com.ppdai.bicoder.cache;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.ppdai.bicoder.chat.model.ChatContext;
import com.ppdai.bicoder.config.PluginStaticConfig;
import com.ppdai.bicoder.config.UserSetting;
import com.ppdai.bicoder.model.ClassInfo;
import com.ppdai.bicoder.utils.InfoUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 项目级缓存
 *
 */
public class ProjectCache {

    private String gitUrl;

    private List<ChatContext> chatContextList;

    private Map<String, Map<String, ClassInfo>> classDefineMapCache;

    private LinkedHashMap<String, ChatContext> currentOpenedEditorContexts = new LinkedHashMap<>(PluginStaticConfig.MAX_CURRENT_OPENED_EDITOR_CACHE_SIZE, 0.75f, true) {
        //如果超过最大缓存数量，则移除最早一个
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, ChatContext> eldest) {
            return size() > PluginStaticConfig.MAX_CURRENT_OPENED_EDITOR_CACHE_SIZE;
        }
    };

    public String getGitUrl() {
        return gitUrl;
    }

    public void setGitUrl(String gitUrl) {
        this.gitUrl = gitUrl;
    }

    public void clearCache() {
        gitUrl = null;
        clearChatContext();
        clearClassDefineMap();
    }

    public List<ChatContext> getChatContextList() {
        return chatContextList;
    }

    public void setChatContextList(List<ChatContext> chatContextList) {
        this.chatContextList = chatContextList;
    }

    public void addChatContext(ChatContext chatContext) {
        if (this.chatContextList == null) {
            this.chatContextList = new ArrayList<>();
        }
        this.chatContextList.add(chatContext);
    }

    public void removeChatContext(String id) {
        if (this.chatContextList != null) {
            this.chatContextList.removeIf(chatContent -> chatContent.getId().equals(id));
        }
    }

    public void clearChatContext() {
        if (this.chatContextList != null) {
            this.chatContextList.clear();
        }
    }

    /**
     * 获取用户最近聚焦并且还打开着的editor，工程刚打开时
     *
     * @param fileFullPath 文件全路径
     * @return 上下文列表
     */
    public LinkedHashMap<String, ChatContext> getCurrentOpenedEditorContextList(@NotNull String fileFullPath) {
        //todo 工程打开时，需要把所有打开的editor放到队列中
        //todo 查询当前打开的editor，如果不在，需要过滤掉，需要额外注意chat中的editor是否影响
        List<Map.Entry<String, ChatContext>> list = currentOpenedEditorContexts.entrySet().stream()
                .filter(entry -> {
                    //如果是当前文件,则忽略
                    if (entry.getKey().equals(fileFullPath)) {
                        return false;
                    }
                    //只保留指定后缀的文件
                    String key = entry.getKey();
                    String afterLastDot = key.substring(key.lastIndexOf(".") + 1);
                    String fileExtension = fileFullPath.substring(fileFullPath.lastIndexOf(".") + 1);
                    return fileExtension.equals(afterLastDot);
                })
                .collect(Collectors.toList());
        //取最近打开的文件
        Collections.reverse(list);

        return list.stream()
                .limit(UserSetting.getInstance().getMaxCurrentOpenedEditorCacheUseSize())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

    public void setCurrentOpenedEditorContextList(LinkedHashMap<String, ChatContext> currentOpenedEditorContextList) {
        this.currentOpenedEditorContexts = currentOpenedEditorContextList;
    }

    public void addCurrentOpenedEditorContext(String fileFullPath, ChatContext chatContext) {
        this.currentOpenedEditorContexts.put(fileFullPath, chatContext);
    }

    /**
     * 初始化当前打开的editor上下文,主要用于工程打开时提前将所有打开的文件加入缓存
     */
    public void initCurrentOpenedEditorContext(@NotNull Project project) {
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        VirtualFile[] files = fileEditorManager.getOpenFiles();
        for (VirtualFile file : files) {
            Document document = ReadAction.compute(()->FileDocumentManager.getInstance().getDocument(file));
            if (document != null) {
                int lineCount = document.getLineCount();
                //如果文件行数小于最小行数或者大于最大行数，则排除
                if (lineCount < UserSetting.getInstance().getOpenedEditorCacheFileMinLine() || lineCount > UserSetting.getInstance().getOpenedEditorCacheFileMaxLine()) {
                    continue;
                }
                String codePath = InfoUtils.getRelativePathNoContainFileName(project, file);
                ChatContext chatContext = new ChatContext(ChatContext.TYPE_FILE_OPENED, document.getText(), codePath, file.getPath(), file.getName(), 1, lineCount);
                this.currentOpenedEditorContexts.put(file.getPath(), chatContext);
            }
        }
    }

    public void removeCurrentOpenedEditorContext(String fileFullPath) {
        this.currentOpenedEditorContexts.remove(fileFullPath);
    }

    public void clearCurrentOpenedChatContext() {
        if (this.currentOpenedEditorContexts != null) {
            this.currentOpenedEditorContexts.clear();
        }
    }

    public void addClassDefineMap(String editorFileName, Map<String, ClassInfo> classDefineMap) {
        if (this.classDefineMapCache == null) {
            this.classDefineMapCache = new ConcurrentHashMap<>(1);
        }
        clearClassDefineMap();
        this.classDefineMapCache.put(editorFileName, classDefineMap);
    }

    public Map<String, ClassInfo> getClassDefineMap(String editorFileName) {
        if (this.classDefineMapCache == null) {
            return null;
        }
        return this.classDefineMapCache.get(editorFileName);
    }

    public void clearClassDefineMap() {
        if (this.classDefineMapCache != null) {
            this.classDefineMapCache.clear();
        }
    }

}
