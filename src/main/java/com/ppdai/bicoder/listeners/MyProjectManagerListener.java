package com.ppdai.bicoder.listeners;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.ppdai.bicoder.cache.ProjectCache;
import org.jetbrains.annotations.NotNull;

/**
 * 项目处理监听器
 *
 */
public class MyProjectManagerListener implements ProjectManagerListener {

    @Override
    public void projectClosing(@NotNull Project project) {
        //关闭项目时清除一些项目级别的缓存
        ProjectCache service = project.getService(ProjectCache.class);
        service.clearCache();
    }

}