package com.ppdai.bicoder.utils;

import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;

public class BalloonUtils {

    public static void showWarningBalloon(String content, Point locationOnScreen) {
        showBalloon(content, MessageType.WARNING, locationOnScreen);
    }

    public static void showInfoBalloon(String content, Point locationOnScreen) {
        showBalloon(content, MessageType.INFO, locationOnScreen);
    }

    private static void showBalloon(String content, MessageType messageType, Point locationOnScreen) {
        JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder(content, messageType, null)
                .setFadeoutTime(2500)
                .createBalloon()
                .show(RelativePoint.fromScreen(locationOnScreen), Balloon.Position.above);
    }

    public static void showSuccessIconBalloon(String content, Icon icon, Point locationOnScreen) {
        JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder(content, icon, JBUI.CurrentTheme.NotificationInfo.backgroundColor(), null)
                .setFadeoutTime(2500)
                .createBalloon()
                .show(RelativePoint.fromScreen(locationOnScreen), Balloon.Position.above);
    }

    public static void showWarnIconBalloon(String content, Icon icon, Point locationOnScreen) {
        JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder(content, icon, JBUI.CurrentTheme.NotificationWarning.backgroundColor(), null)
                .setFadeoutTime(2500)
                .createBalloon()
                .show(RelativePoint.fromScreen(locationOnScreen), Balloon.Position.above);
    }

    public static void showWarnIconBalloonRelative(String content, Icon icon, RelativePoint relativePoint) {
        JBPopupFactory.getInstance()
                .createHtmlTextBalloonBuilder(content, icon, JBUI.CurrentTheme.NotificationWarning.backgroundColor(), null)
                .setFadeoutTime(2500)
                .createBalloon()
                .show(relativePoint, Balloon.Position.above);
    }

}
