package com.ppdai.bicoder.menu;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import com.ppdai.bicoder.config.PluginStaticConfig;
import com.ppdai.bicoder.utils.BiCoderLoggerUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * 工具栏图标管理工厂类
 *
 */
public class BiCoderStatusBarWidgetFactory implements StatusBarWidgetFactory {
    @Override
    public @NotNull
    String getId() {
        return getClass().getName();
    }

    @Override
    public @Nls
    @NotNull
    String getDisplayName() {
        return PluginStaticConfig.PLUGIN_NAME;
    }

    @Override
    public boolean isAvailable(@NotNull Project project) {
        return true;
    }

    @Override
    public @NotNull
    StatusBarWidget createWidget(@NotNull Project project) {
        BiCoderLoggerUtils.getInstance(getClass()).info("creating status bar widget");
        return new BiCoderStatusBarWidget(project);
    }

    @Override
    public void disposeWidget(@NotNull StatusBarWidget widget) {
        BiCoderLoggerUtils.getInstance(getClass()).info("disposing status bar widget");
        Disposer.dispose(widget);
    }

    @Override
    public boolean canBeEnabledOn(@NotNull StatusBar statusBar) {
        return true;
    }
}