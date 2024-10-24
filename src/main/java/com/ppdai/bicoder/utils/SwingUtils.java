package com.ppdai.bicoder.utils;

import com.intellij.ide.BrowserUtil;
import com.intellij.util.ui.UI;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.net.URISyntaxException;

import static javax.swing.event.HyperlinkEvent.EventType.ACTIVATED;

public class SwingUtils {

    public static JButton createIconButton(Icon icon) {
        var button = new JButton(icon);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setContentAreaFilled(false);
        button.setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
        return button;
    }

    public static Box justifyLeft(Component component) {
        Box box = Box.createHorizontalBox();
        box.add(component);
        box.add(Box.createHorizontalGlue());
        return box;
    }

    public static void setEqualLabelWidths(JPanel firstPanel, JPanel secondPanel) {
        var firstLabel = firstPanel.getComponents()[0];
        var secondLabel = secondPanel.getComponents()[0];
        if (firstLabel instanceof JLabel && secondLabel instanceof JLabel) {
            firstLabel.setPreferredSize(secondLabel.getPreferredSize());
        }
    }

    public static JPanel createPanel(JComponent component, String label) {
        return createPanel(component, label, false);
    }

    public static JPanel createPanel(JComponent component, String label, boolean resizeX) {
        return UI.PanelFactory.panel(component)
                .withLabel(label)
                .resizeX(resizeX)
                .createPanel();
    }

    public static void handleHyperlinkClicked(HyperlinkEvent event) {
        if (ACTIVATED.equals(event.getEventType()) && event.getURL() != null) {
            try {
                BrowserUtil.browse(event.getURL().toURI());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void addShiftEnterInputMap(JTextArea textArea, Runnable onSubmit) {
        var enterStroke = KeyStroke.getKeyStroke("ENTER");
        var shiftEnterStroke = KeyStroke.getKeyStroke("shift ENTER");
        textArea.getInputMap().put(shiftEnterStroke, "insert-break");
        textArea.getInputMap().put(enterStroke, "text-submit");
        textArea.getActionMap().put("text-submit", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                onSubmit.run();
            }
        });
    }

    public static Rectangle2D getStringFontSize(String text, Font font) {
        if ("".equals(text)) {
            return new Rectangle2D.Float(0.0F, 0.0F, 0.0F, 0.0F);
        }
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform, true, true);
        return font.getStringBounds(text, frc);
    }
}

