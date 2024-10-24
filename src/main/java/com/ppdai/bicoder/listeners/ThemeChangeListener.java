package com.ppdai.bicoder.listeners;

import com.intellij.openapi.editor.colors.EditorColorsListener;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import org.jetbrains.annotations.Nullable;

public abstract class ThemeChangeListener implements EditorColorsListener {
    @Override
    public abstract void globalSchemeChange(@Nullable EditorColorsScheme scheme);

}