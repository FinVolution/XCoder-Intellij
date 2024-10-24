package com.ppdai.bicoder.listeners;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginStateListener;
import com.ppdai.bicoder.job.UpdatePluginConfigJob;
import org.jetbrains.annotations.NotNull;

/**
 * 插件安装卸载监听器
 *
 */
public class PluginInstallListener implements PluginStateListener {


    @Override
    public void install(@NotNull IdeaPluginDescriptor descriptor) {
    }

    @Override
    public void uninstall(@NotNull IdeaPluginDescriptor descriptor) {
        UpdatePluginConfigJob.getInstance().stopUpdateJob();
    }
}
