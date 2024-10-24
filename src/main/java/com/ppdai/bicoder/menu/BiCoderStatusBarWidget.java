package com.ppdai.bicoder.menu;

import com.intellij.ide.DataManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.EditorBasedWidget;
import com.intellij.util.Consumer;
import com.ppdai.bicoder.actions.StatusBarActions;
import com.ppdai.bicoder.config.BiCoderBundle;
import com.ppdai.bicoder.config.PluginStaticConfig;
import com.ppdai.bicoder.config.UserSetting;
import com.ppdai.bicoder.listeners.BiCoderCaretListener;
import com.ppdai.bicoder.notifier.BinaryStateChangeNotifier;
import com.ppdai.bicoder.notifier.LanguageStateChangeNotifier;
import com.ppdai.bicoder.utils.BiCoderLoggerUtils;
import com.ppdai.bicoder.utils.CompletionUtils;
import com.ppdai.bicoder.utils.InfoUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.TimerTask;
import java.util.concurrent.*;

/**
 * 工具栏图标管理
 *
 */
public class BiCoderStatusBarWidget extends EditorBasedWidget
        implements StatusBarWidget, StatusBarWidget.MultipleTextValuesPresentation {
    private boolean isEnablePlugin;

    private boolean isLoading;

    private static final String EMPTY_SYMBOL = "\u0000";

    private ScheduledExecutorService executor;


    public BiCoderStatusBarWidget(@NotNull Project project) {
        super(project);
        //第一次创建时，初始化属性
        UserSetting userSetting = UserSetting.getInstance();
        isEnablePlugin = userSetting.getEnablePlugin();
        isLoading = false;

        //初始化绑定事件
        ApplicationManager.getApplication()
                .getMessageBus()
                .connect(this)
                .subscribe(
                        BinaryStateChangeNotifier.STATE_CHANGED_TOPIC,
                        (BinaryStateChangeNotifier) state -> {
                            this.isEnablePlugin = state.isEnablePlugin();
                            this.isLoading = state.isLoading();
                            if (!state.isEnablePlugin()) {
                                //如果插件被禁用，清除代码补全提示
                                Editor focusedEditor = FileEditorManager.getInstance(project).getSelectedTextEditor();
                                CompletionUtils.cleanCodeCompletion(focusedEditor);
                            }
                            update();
                        });
        ApplicationManager.getApplication()
                .getMessageBus()
                .connect(this)
                .subscribe(
                        LanguageStateChangeNotifier.LANGUAGE_STATE_CHANGED_TOPIC,
                        (LanguageStateChangeNotifier) state -> {
                            if (!state) {
                                //如果插件被禁用，清除代码补全提示
                                Editor focusedEditor = FileEditorManager.getInstance(project).getSelectedTextEditor();
                                CompletionUtils.cleanCodeCompletion(focusedEditor);
                            }
                            update();
                        });

        executor = new ScheduledThreadPoolExecutor(1, Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
        //更新图标状态
        executor.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (isEnablePlugin) {
                    update();
                }
            }
        }, 2, 2, TimeUnit.SECONDS);

        //绑定光标监听器
        EditorFactory.getInstance().getEventMulticaster().addCaretListener(new BiCoderCaretListener(), this);
    }

    @Override
    public Icon getIcon() {
        Project project = myStatusBar != null ? myStatusBar.getProject() : null;
        String language = getLanguage(project);
        if (!this.isEnablePlugin || UserSetting.getInstance().getDisableFileTypeList().contains(language)) {
            return PluginStaticConfig.DISABLED_ICON;
        }
        if (this.isLoading) {
            return PluginStaticConfig.LOADING_ICON;
        }
        return PluginStaticConfig.BI_CODER_ICON;
    }

    @Override
    public @Nullable("null means the widget is unable to show the popup")
    ListPopup getPopupStep() {
        return createPopup();
    }

    @Override
    public String getSelectedValue() {
        return EMPTY_SYMBOL;
    }

    @Override
    @Nullable
    public WidgetPresentation getPresentation() {
        return this;
    }

    @NotNull
    @Override
    public String ID() {
        return getClass().getName();
    }

    private ListPopup createPopup() {
        Project project = myStatusBar != null ? myStatusBar.getProject() : null;
        String language = getLanguage(project);
        return JBPopupFactory.getInstance()
                .createActionGroupPopup(
                        null,
                        StatusBarActions.buildStatusBarActionsGroup(project, isEnablePlugin, language),
                        DataManager.getInstance()
                                .getDataContext(myStatusBar != null ? myStatusBar.getComponent() : null),
                        JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                        true);
    }

    @Override
    @Nullable
    public String getTooltipText() {
        return String.format(BiCoderBundle.get("plugin.status.bar.open.setting.hint"), PluginStaticConfig.PLUGIN_NAME);
    }

    @Override
    @Nullable
    public Consumer<MouseEvent> getClickConsumer() {
        return null;
    }

    private void update() {
        if (myStatusBar == null) {
            BiCoderLoggerUtils.getInstance(getClass()).warn("Failed to update the status bar");
            return;
        }
        myStatusBar.updateWidget(ID());
    }

    @Nullable
    private String getLanguage(Project project) {
        String language = null;
        if (project != null) {
            Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
            if (editor != null) {
                Document document = editor.getDocument();
                VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
                if (virtualFile != null) {
                    language = InfoUtils.getLanguage(virtualFile);
                }
            }
        }
        return language;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

}