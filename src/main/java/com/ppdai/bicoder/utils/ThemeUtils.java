package com.ppdai.bicoder.utils;

import com.intellij.ui.JBColor;
import com.intellij.util.ui.UIUtil;

import java.awt.*;

import static java.lang.String.format;

/**
 * 主题工具类
 */
public class ThemeUtils {

    /**
     * 获取面板背景色
     *
     * @return 面板背景色
     */
    public static Color getPanelBackgroundColor() {
        return UIUtil.getPanelBackground();
    }

    private static Color toDarker(Color color) {
        var factor = 0.9;
        return new Color(
                Math.max((int) (color.getRed() * factor), 0),
                Math.max((int) (color.getGreen() * factor), 0),
                Math.max((int) (color.getBlue() * factor), 0),
                color.getAlpha());
    }

    public static String getRGB(Color color) {
        return format("rgb(%d, %d, %d)", color.getRed(), color.getGreen(), color.getBlue());
    }


}
