package com.ppdai.bicoder.menu;

import com.intellij.util.ui.FormBuilder;
import com.ppdai.bicoder.config.BiCoderBundle;

import javax.swing.*;
import java.awt.*;

/**
 * 插件配置页
 *
 */
public class BiCoderProjectSettingsForm {

    private JPanel panel;

    private CascadeComboBox cascadeComboBox;

    private JTextField testFileRootPathJTest;

    public BiCoderProjectSettingsForm(String language, String selectedUintTestFramework, String customUintTestFramework, String testFileRootPath) {
        cascadeComboBox = new CascadeComboBox(language, selectedUintTestFramework, customUintTestFramework);
        this.panel = FormBuilder.createFormBuilder().getPanel();
        this.panel.setLayout(new BoxLayout(this.panel, BoxLayout.Y_AXIS));
        FormBuilder panelBuilder = FormBuilder.createFormBuilder();
        this.testFileRootPathJTest = new JTextField();
        this.testFileRootPathJTest.setText(testFileRootPath);
        panelBuilder.addLabeledComponent(BiCoderBundle.get("chat.action.generate.tests.action.uint.test.file.root.path.setting.title"), testFileRootPathJTest);
        JPanel testFileRootPathPanel = panelBuilder.getPanel();
        testFileRootPathPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));

        cascadeComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, cascadeComboBox.getPreferredSize().height));
        testFileRootPathPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, testFileRootPathPanel.getPreferredSize().height));
        cascadeComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        testFileRootPathPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.panel.add(cascadeComboBox);
        this.panel.add(Box.createHorizontalGlue());
        this.panel.add(testFileRootPathPanel);
        this.panel.add(Box.createHorizontalGlue());
    }

    public JPanel getPanel() {
        return this.panel;
    }


    public void resetUintTestFramework(String language, String selectedUintTestFramework, String customUintTestFramework, String testFileRootPath) {
        this.cascadeComboBox.resetSelectedUintTestFramework(language, selectedUintTestFramework, customUintTestFramework);
        this.testFileRootPathJTest.setText(testFileRootPath);
    }

    public String getSelectedLanguage() {
        return this.cascadeComboBox.getSelectedLanguage();
    }

    public String getSelectedUintTestFramework() {
        return this.cascadeComboBox.getSelectedUintTestFramework();
    }

    public String getCustomUintTestFramework() {
        return this.cascadeComboBox.getCustomUintTestFramework();
    }

    public String getTestFileRootPath() {
        return this.testFileRootPathJTest.getText();
    }
}
