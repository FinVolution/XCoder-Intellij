package com.ppdai.bicoder.utils;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.extensions.PluginId;

public class EditorActionsUtil {

    public static void registerOrReplaceAction(AnAction action,String actionId) {
        ActionManager actionManager = ActionManager.getInstance();
        if (actionManager.getAction(actionId) != null) {
            actionManager.replaceAction(actionId, action);
        } else {
            actionManager.registerAction(actionId, action, PluginId.getId("com.ppdai.bicoder"));
        }
    }

}
