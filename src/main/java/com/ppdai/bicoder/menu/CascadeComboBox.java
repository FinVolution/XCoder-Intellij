package com.ppdai.bicoder.menu;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.util.ui.FormBuilder;
import com.ppdai.bicoder.config.BiCoderBundle;
import com.ppdai.bicoder.config.UserSetting;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class CascadeComboBox extends JPanel {
    private ComboBox<String> parentComboBox;
    private ComboBox<String> childComboBox;
    private JTextField customText;

    private Map<String, List<String>> allUintTestFramework = UserSetting.getInstance().getAllUintTestFramework();


    public CascadeComboBox(String language, String selectedUintTestFramework, String customUintTestFramework) {
        setBorder(IdeBorderFactory.createTitledBorder(BiCoderBundle.get("chat.action.generate.tests.action.uint.test.framework.title"), false));
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));

        FormBuilder panelBuilder = FormBuilder.createFormBuilder();


        initParentCombobox(language);
        panelBuilder.addLabeledComponent(BiCoderBundle.get("chat.action.generate.tests.action.uint.test.framework.language.combobox.title"), parentComboBox);

        childComboBox = new ComboBox<>();
        updateChildComboBox();
        childComboBox.setItem(selectedUintTestFramework);

        panelBuilder.addLabeledComponent(BiCoderBundle.get("chat.action.generate.tests.action.uint.test.framework.framework.combobox.title"), childComboBox);

        customText = new JTextField();
        customText.setText(customUintTestFramework);
        panelBuilder.addLabeledComponent(BiCoderBundle.get("chat.action.generate.tests.action.uint.test.framework.custom.setting.title"), customText);

        JPanel group = panelBuilder.getPanel();
        add(group);
    }

    private void initParentCombobox(String language) {
        List<String> parentList = new ArrayList<>(allUintTestFramework.keySet());
        //添加一个空选择
        parentList.add(0, "");
        parentComboBox = new ComboBox<>(new CollectionComboBoxModel<>(parentList));
        parentComboBox.setItem(language);
        parentComboBox.addItemListener(e -> {
            String selected = e.getItem().toString();
            updateChildComboBox();
        });
    }

    private void updateChildComboBox() {
        String parent = parentComboBox.getItem();
        updateChildComboBox(parent);
    }

    private void updateChildComboBox(String parent) {
        List<String> childComboBoxList = allUintTestFramework.get(parent);
        childComboBox.setModel(new CollectionComboBoxModel<>(Objects.requireNonNullElseGet(childComboBoxList, List::of)));
    }

    public void resetSelectedUintTestFramework(String language, String selectedUintTestFramework, String customUintTestFramework) {
        parentComboBox.setItem(language);
        childComboBox.setItem(selectedUintTestFramework);
        customText.setText(customUintTestFramework);
    }

    public String getSelectedLanguage() {
        return parentComboBox.getItem();
    }

    public String getSelectedUintTestFramework() {
        return childComboBox.getItem();
    }

    public String getCustomUintTestFramework() {
        return customText.getText();
    }

}
