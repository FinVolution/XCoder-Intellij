package com.ppdai.bicoder.menu;

import com.intellij.execution.util.ListTableWithButtons;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import com.ppdai.bicoder.config.BiCoderBundle;
import com.ppdai.bicoder.config.PluginStaticConfig;
import com.ppdai.bicoder.config.UserSetting;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.*;
import java.util.stream.Collectors;

class LanguageTable extends ListTableWithButtons<LanguageChoice> {
    public LanguageTable() {
    }

    void initItems(@NotNull Set<String> disabledLanguages) {
        UserSetting userSetting = UserSetting.getInstance();
        if (CollectionUtils.isNotEmpty(userSetting.getFileWhitelist())) {
            List<LanguageChoice> items = userSetting.getFileWhitelist().stream()
                    .map(lang -> new LanguageChoice(lang, disabledLanguages.contains(lang)))
                    .sorted(Comparator.comparing(LanguageChoice::getLanguage, String::compareToIgnoreCase))
                    .sorted(Comparator.comparingInt(value -> value.isSelected() ? -1 : 1))
                    .collect(Collectors.toList());
            this.setValues(items);
        } else {
            this.setValues(new ArrayList<>());
        }
    }

    void setDisabledLanguages(@NotNull Set<String> languages) {
        List<LanguageChoice> items = this.getElements();

        for (LanguageChoice item : items) {
            item.setSelected(languages.contains(item.getLanguage()));
        }

        this.getTableView().getTableViewModel().setItems(items);
    }

    Set<String> getDisabledLanguages() {
        Set<String> languages = new HashSet();
        List<LanguageChoice> items = this.getElements();
        for (LanguageChoice element : items) {
            if (element.isSelected()) {
                languages.add(element.getLanguage());
            }
        }
        return languages;
    }

    @Override
    @Nullable
    protected AnActionButtonRunnable createAddAction() {
        return null;
    }

    @Override
    @Nullable
    protected AnActionButtonRunnable createRemoveAction() {
        return null;
    }

    @Override
    protected boolean isUpDownSupported() {
        return false;
    }

    @Override
    protected ListTableModel<LanguageChoice> createListModel() {
        LanguageTable.LanguageSelectedColumn checkboxColumn = new LanguageTable.LanguageSelectedColumn();
        LanguageTable.LanguageNameColumn nameColumn = new LanguageTable.LanguageNameColumn();
        ListTableModel<LanguageChoice> model = new ListTableModel(new ColumnInfo[]{checkboxColumn, nameColumn}, List.of(), 0, SortOrder.UNSORTED);
        model.setSortable(true);
        return model;
    }

    @Override
    protected LanguageChoice createElement() {
        throw new UnsupportedOperationException("unsupported");
    }

    @Override
    protected boolean isEmpty(LanguageChoice element) {
        return false;
    }

    @Override
    protected LanguageChoice cloneElement(LanguageChoice item) {
        return new LanguageChoice(item);
    }

    @Override
    protected boolean canDeleteElement(LanguageChoice selection) {
        return false;
    }

    private static class LanguageSelectedColumn extends ColumnInfo<LanguageChoice, Boolean> {
        private static final int CHECKBOX_COLUMN_WIDTH;

        public LanguageSelectedColumn() {
            super("");
        }

        @Override
        public int getWidth(JTable table) {
            return CHECKBOX_COLUMN_WIDTH;
        }

        @Override
        public Class<?> getColumnClass() {
            return Boolean.class;
        }

        @Override
        public boolean isCellEditable(LanguageChoice languageChoice) {
            return true;
        }

        @Override
        @Nullable
        public Boolean valueOf(LanguageChoice languageChoice) {
            return languageChoice.isSelected();
        }

        @Override
        public void setValue(LanguageChoice languageChoice, Boolean value) {
            languageChoice.setSelected(value);
        }

        @Override
        @Nullable
        public Comparator<LanguageChoice> getComparator() {
            return Comparator.comparing(LanguageChoice::isSelected);
        }

        static {
            CHECKBOX_COLUMN_WIDTH = (new JCheckBox()).getPreferredSize().width + 4;
        }
    }

    private static class LanguageNameColumn extends ColumnInfo<LanguageChoice, String> {
        public LanguageNameColumn() {
            super(BiCoderBundle.get("plugin.setting.language.table.title"));
        }

        @Override
        @Nullable
        public Comparator<LanguageChoice> getComparator() {
            return Comparator.comparing((lang) -> {
                return this.getLabel(lang).toLowerCase();
            });
        }

        @Override
        @Nullable
        public String valueOf(LanguageChoice languageChoice) {
            return this.getLabel(languageChoice);
        }

        @NotNull
        private String getLabel(LanguageChoice languageChoice) {
            return languageChoice.getLanguage();
        }
    }
}
