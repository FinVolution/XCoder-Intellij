package com.ppdai.bicoder.job;

import com.intellij.ide.plugins.CustomPluginRepositoryService;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.notification.impl.NotificationFullContent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.updateSettings.impl.PluginDownloader;
import com.intellij.openapi.updateSettings.impl.UpdateChecker;
import com.intellij.openapi.util.BuildNumber;
import com.ppdai.bicoder.config.BiCoderBundle;
import com.ppdai.bicoder.config.PluginStaticConfig;
import com.ppdai.bicoder.utils.BiCoderLoggerUtils;
import com.ppdai.bicoder.utils.ReflectUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;

public class PluginUpdateCheckTask extends Task.Backgroundable {
    private static final PluginId PLUGIN_ID = PluginId.getId("com.ppdai.bicoder");

    public PluginUpdateCheckTask(Project project) {
        super(project, String.format(BiCoderBundle.get("plugin.update.checking.title"), PluginStaticConfig.PLUGIN_NAME), false);
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        PluginDownloader availableUpdate = getAvailablePluginDownLoaders(indicator)
                .stream()
                .filter(downloader -> PLUGIN_ID.equals(downloader.getId()))
                .findFirst()
                .orElse(null);
        if (availableUpdate != null) {
            CustomPluginRepositoryService.getInstance().clearCache();
            // 直接安装更新
            installUpdateRightNow(getProject(), availableUpdate);
        }
    }

    public static Collection<PluginDownloader> getAvailablePluginDownLoaders(ProgressIndicator indicator) {
        try {
            var getInternalPluginUpdatesMethod = UpdateChecker.class.getMethod(
                    "getInternalPluginUpdates", BuildNumber.class, ProgressIndicator.class);
            var internalPluginUpdates = getInternalPluginUpdatesMethod.invoke(null, null, indicator);
            var getPluginUpdatesMethod = internalPluginUpdates.getClass().getMethod("getPluginUpdates");
            var pluginUpdates = getPluginUpdatesMethod.invoke(internalPluginUpdates);
            var getAllEnabledMethod = pluginUpdates.getClass().getMethod("getAllEnabled");
            var allEnabled = getAllEnabledMethod.invoke(pluginUpdates);

            return allEnabled != null ? (Collection<PluginDownloader>) allEnabled : Collections.emptyList();
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | ClassCastException e) {
            BiCoderLoggerUtils.getInstance(PluginUpdateCheckTask.class).info("failed to obtain the plugin update", e);
        }

        return Collections.emptyList();
    }

    public static void notifyAboutTheUpdate(Project project, PluginDownloader pluginDownloader) {
        var notification = new FullContent(
                PluginStaticConfig.PLUGIN_NAME + BiCoderBundle.get("plugin.update.notify.group.id"),
                PluginStaticConfig.PLUGIN_NAME + BiCoderBundle.get("plugin.update.notify.title"),
                String.format(BiCoderBundle.get("plugin.update.notify.content"), PluginStaticConfig.PLUGIN_NAME),
                NotificationType.IDE_UPDATE);

        notification.addAction(NotificationAction.createSimpleExpiring(
                BiCoderBundle.get("plugin.update.now"),
                () -> installUpdateRightNow(project, pluginDownloader)));

        notification.notify(project);
    }

    private static void installUpdateRightNow(final Project project, final PluginDownloader pluginDownloader) {
        if (project == null) {
            doInstallUpdate(project, pluginDownloader, new EmptyProgressIndicator());
        } else {
            (new Task.Backgroundable(project, BiCoderBundle.get("plugin.update.installing"), true) {
                public void run(@NotNull ProgressIndicator indicator) {
                    doInstallUpdate(project, pluginDownloader, indicator);
                }
            }).queue();
        }
    }

    private static void doInstallUpdate(Project project, PluginDownloader pluginDownloader, ProgressIndicator indicator) {
        try {
            BiCoderLoggerUtils.getInstance(PluginUpdateCheckTask.class).info("start installing:" + pluginDownloader.getPluginVersion());
            Class<?> clazz = ReflectUtil.classForName("com.intellij.openapi.updateSettings.impl.UpdateInstaller");
            Method method = ReflectUtil.getMethod(clazz, "installPluginUpdates", Collection.class, ProgressIndicator.class);
            if (method == null) {
                BiCoderLoggerUtils.getInstance(PluginUpdateCheckTask.class).warn("install update failed:" + pluginDownloader.getPluginVersion());
                return;
            }
            Boolean installResult = (Boolean) method.invoke(null, new Object[]{Collections.singletonList(pluginDownloader), indicator});
            BiCoderLoggerUtils.getInstance(PluginUpdateCheckTask.class).info("finished installing:" + installResult);
            if (project != null) {
                notifyUpdateFinished(project);
            } else {
                Project[] projects = ProjectManager.getInstance().getOpenProjects();
                for (Project p : projects) {
                    if (p.isOpen()) {
                        notifyUpdateFinished(p);
                    }
                }
            }
        } catch (Exception e) {
            BiCoderLoggerUtils.getInstance(PluginUpdateCheckTask.class).info("install update failed:" + pluginDownloader.getPluginVersion(), e);
        }
    }

    private static void notifyUpdateFinished(@NotNull Project project) {
        var notification = new FullContent(
                PluginStaticConfig.PLUGIN_NAME + BiCoderBundle.get("plugin.update.notify.group.id"),
                PluginStaticConfig.PLUGIN_NAME + BiCoderBundle.get("plugin.update.complete.title"),
                BiCoderBundle.get("plugin.update.complete.content"),
                NotificationType.INFORMATION);
        notification.addAction(NotificationAction.createSimpleExpiring(
                BiCoderBundle.get("plugin.restart.now"),
                () -> ApplicationManager.getApplication().restart()));
        notification.notify(project);
    }

    private static class FullContent extends Notification implements NotificationFullContent {
        public FullContent(String groupId, String notificationTitle, String content, NotificationType type) {
            super(groupId, notificationTitle, content, type);
        }
    }
}
