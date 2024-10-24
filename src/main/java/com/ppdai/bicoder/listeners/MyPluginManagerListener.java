package com.ppdai.bicoder.listeners;

import com.intellij.ide.plugins.PluginInstaller;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.util.concurrency.annotations.RequiresBackgroundThread;
import com.ppdai.bicoder.cache.ProjectCache;
import com.ppdai.bicoder.job.PluginUpdateCheckTask;
import com.ppdai.bicoder.job.UpdatePluginConfigJob;
import com.ppdai.bicoder.utils.BiCoderLoggerUtils;
import org.jetbrains.annotations.NotNull;

public class MyPluginManagerListener implements StartupActivity.Background {

    private boolean pluginInit = false;

    @RequiresBackgroundThread
    @Override
    public void runActivity(@NotNull Project project) {
        if (!pluginInit) {
            initPlugin();
        }
        new PluginUpdateCheckTask(project).queue();
        //初始化当前打开的编辑器上下文
        project.getService(ProjectCache.class).initCurrentOpenedEditorContext(project);
    }

    private void initPlugin() {
        BiCoderLoggerUtils.getInstance(getClass()).info("init plugin");
        UpdatePluginConfigJob.getInstance().startUpdateJob();
        PluginInstaller.addStateListener(new PluginInstallListener());
        pluginInit = true;
    }


}
