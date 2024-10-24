package com.ppdai.bicoder.actions;

import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffDialogHints;
import com.intellij.diff.DiffManager;
import com.intellij.diff.contents.DiffContent;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.LightVirtualFile;
import com.ppdai.bicoder.config.BiCoderBundle;
import com.ppdai.bicoder.config.PluginStaticConfig;
import com.ppdai.bicoder.config.UserSetting;
import com.ppdai.bicoder.service.StateService;
import com.ppdai.bicoder.utils.EditorUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

/**
 * 工具栏图标生成按钮操作
 *
 */
public class StatusBarActions {
    private static final String DISABLED_BICODER = BiCoderBundle.get("plugin.status.bar.completion.disable");
    private static final String DISABLED_BICODER_FOR_LANGUAGE = BiCoderBundle.get("plugin.status.bar.completion.disable.for.language");
    private static final String ENABLE_BICODER = BiCoderBundle.get("plugin.status.bar.completion.enable");

    private static final String DIFF_FILE = BiCoderBundle.get("plugin.status.bar.tools.diff.file.title");

    private static final String OPEN_SETTING = BiCoderBundle.get("plugin.status.bar.open.completion.setting.title");

    private static final String TOOL_NAME = BiCoderBundle.get("plugin.status.bar.tools.setting.title");

    private static final StateService stateService = ApplicationManager.getApplication().getService(StateService.class);


    public static DefaultActionGroup buildStatusBarActionsGroup(Project project, boolean isEnablePlugin, String language) {
        List<AnAction> actions = new ArrayList<>();
        if (project != null) {

            if (isEnablePlugin && !UserSetting.getInstance().getDisableFileTypeList().contains(language)) {
                actions.add(createDisableAction());
                if (StringUtils.isNotBlank(language)) {
                    actions.add(createLanguageDisableAction(language));
                }
            } else {
                actions.add(createEnableAction(language));
            }
            addTools(project, actions);
            actions.add(createOpenSettingAction(project));
        }
        return new DefaultActionGroup(actions);
    }

    private static void addTools(Project project, List<AnAction> actions) {
        //增加一个小工具栏
        AnAction diffAction = createDiffFileAction(project);
        DefaultActionGroup tools = new DefaultActionGroup(TOOL_NAME, true);
        tools.add(diffAction);
        actions.add(tools);
    }

    private static DumbAwareAction createDiffFileAction(Project project) {
        return DumbAwareAction.create(DIFF_FILE, anActionEvent -> {
            ActionManager actionManager = ActionManager.getInstance();
            AnAction action = actionManager.getAction("ShowBlankDiffWindow");
            boolean processed = actionManager.tryToExecute(action, anActionEvent.getInputEvent(), null, anActionEvent.getPlace(), true).isProcessed();
            if (!processed) {
                //没有执行成功系统的diff,则使用自己的diff
                //创建并打开一个文本比对窗口
                var diffContentFactory = DiffContentFactory.getInstance();
                LightVirtualFile leftSelectedTextFile = new LightVirtualFile(
                        format("%s/%s", PathManager.getTempPath(), "left.txt"),
                        "");
                LightVirtualFile rightSelectedTextFile = new LightVirtualFile(
                        format("%s/%s", PathManager.getTempPath(), "right.txt"),
                        "");
                EditorUtils.disableHighlighting(project, leftSelectedTextFile);
                EditorUtils.disableHighlighting(project, rightSelectedTextFile);
                DiffContent leftDiffContent = diffContentFactory.create(project, leftSelectedTextFile);
                DiffContent rightDiffContent = diffContentFactory.create(project, rightSelectedTextFile);
                var request = new SimpleDiffRequest(
                        BiCoderBundle.get("plugin.tools.diff.file.title"),
                        leftDiffContent,
                        rightDiffContent,
                        BiCoderBundle.get("plugin.tools.diff.file.left.title"),
                        BiCoderBundle.get("plugin.tools.diff.file.right.title"));
                DiffManager.getInstance().showDiff(project, request, DiffDialogHints.DEFAULT);
            }
        });
    }

    private static DumbAwareAction createOpenSettingAction(Project project) {
        return DumbAwareAction.create(OPEN_SETTING, anActionEvent -> {
            ShowSettingsUtil.getInstance().showSettingsDialog(project, PluginStaticConfig.PLUGIN_NAME);
        });
    }

    private static DumbAwareAction createDisableAction() {
        return DumbAwareAction.create(DISABLED_BICODER, anActionEvent -> {
            UserSetting.getInstance().setEnablePlugin(false);
            stateService.updateEnableState(false);
        });
    }

    private static DumbAwareAction createLanguageDisableAction(String language) {
        return DumbAwareAction.create(String.format(DISABLED_BICODER_FOR_LANGUAGE, language), anActionEvent -> {
            UserSetting.getInstance().getDisableFileTypeList().add(language);
            stateService.updateLanguageState(false);
        });
    }

    private static DumbAwareAction createEnableAction(String language) {
        return DumbAwareAction.create(ENABLE_BICODER, anActionEvent -> {
            if (UserSetting.getInstance().getDisableFileTypeList().contains(language)) {
                UserSetting.getInstance().getDisableFileTypeList().remove(language);
                stateService.updateLanguageState(true);
            }
            UserSetting.getInstance().setEnablePlugin(true);
            stateService.updateEnableState(true);
        });
    }

}