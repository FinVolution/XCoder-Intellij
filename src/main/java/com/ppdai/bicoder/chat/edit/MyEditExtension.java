package com.ppdai.bicoder.chat.edit;

import com.intellij.diff.DiffContext;
import com.intellij.diff.DiffExtension;
import com.intellij.diff.FrameDiffTool;
import com.intellij.diff.requests.DiffRequest;
import com.intellij.diff.tools.util.side.TwosideTextDiffViewer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class MyEditExtension extends DiffExtension {

    @Override
    public void onViewerCreated(FrameDiffTool.@NotNull DiffViewer viewer, @NotNull DiffContext context, @NotNull DiffRequest request) {
        Boolean isNeed = request.getUserData(MyEditConfig.NEED_ACCEPT_AND_REJECT);
        if (isNeed != null && isNeed) {
            if (viewer instanceof TwosideTextDiffViewer) {
                TwosideTextDiffViewer twosideTextDiffViewer = (TwosideTextDiffViewer) viewer;
                JComponent component = twosideTextDiffViewer.getComponent();
                Consumer<JComponent> setRenderComponent = request.getUserData(MyEditConfig.SET_RENDER_COMPONENT);
                if (setRenderComponent != null) {
                    setRenderComponent.accept(component);
                }
            }
        }

    }
}