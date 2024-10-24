package com.ppdai.bicoder.chat.actions;

import com.intellij.find.FindManager;
import com.intellij.find.FindModel;
import com.intellij.find.findInProject.FindInProjectManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;


public class SearchContext extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        var project = anActionEvent.getProject();
        if (project == null) {
            return;
        }
        FindManager findManager = FindManager.getInstance(project);
        FindModel findModel = findManager.getFindInFileModel().clone();
        findManager.showFindDialog(findModel, () -> FindInProjectManager.getInstance(project).findInPath(findModel));
    }
}
