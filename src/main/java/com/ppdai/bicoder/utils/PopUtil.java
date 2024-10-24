package com.ppdai.bicoder.utils;

import com.intellij.openapi.ui.popup.JBPopup;

import javax.swing.*;

public class PopUtil {
    public static boolean isPopUsable(JBPopup jbPopup) {
        if (jbPopup == null || jbPopup.isDisposed() || !jbPopup.isVisible()) {
            return false;
        }
        return true;
    }

    public static boolean isNotNullJList(JList jList) {
        if (jList == null || jList.getModel().getSize() == 0 || !jList.isVisible()) {
            return false;
        }
        return true;
    }

}
