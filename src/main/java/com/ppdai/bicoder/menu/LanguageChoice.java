package com.ppdai.bicoder.menu;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jetbrains.annotations.NotNull;

class LanguageChoice {
    @NotNull
    private final String language;
    private boolean selected;

    public LanguageChoice(@NotNull String language) {
        super();
        this.language = language;
    }

    LanguageChoice(@NotNull LanguageChoice item) {
        this(item.language, item.selected);
    }

    public LanguageChoice(@NotNull String language, boolean selected) {
        super();
        this.language = language;
        this.selected = selected;
    }

    @NotNull
    public String getLanguage() {
        return language;
    }

    public boolean isSelected() {
        return this.selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LanguageChoice that = (LanguageChoice) o;

        return new EqualsBuilder().append(selected, that.selected).append(language, that.language).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(language).append(selected).toHashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LanguageChoice{");
        sb.append("language='").append(language).append('\'');
        sb.append(", selected=").append(selected);
        sb.append('}');
        return sb.toString();
    }


}
