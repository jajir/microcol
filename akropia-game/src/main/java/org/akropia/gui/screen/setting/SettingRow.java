package org.akropia.gui.screen.setting;

import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

/** Shared caption axis for the Settings form in both screen hosts. */
final class SettingRow {
    private SettingRow() {
    }

    static void align(final HBox row, final Label caption) {
        row.getStyleClass().add("setting-row");
        row.setMinHeight(Region.USE_PREF_SIZE);
        caption.getStyleClass().add("setting-caption");
        caption.setWrapText(true);
        caption.setMinHeight(Region.USE_PREF_SIZE);
    }

    static HBox check(final CheckBox control) {
        final Label caption = new Label();
        caption.setId(control.getId() + "Caption");
        caption.textProperty().bind(control.textProperty());
        caption.setLabelFor(control);
        caption.disableProperty().bind(control.disableProperty());
        caption.setOnMouseClicked(event -> {
            control.requestFocus();
            control.fire();
        });
        // Retain the native accessible name and keyboard behavior.
        control.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        final HBox row = new HBox(caption, control);
        align(row, caption);
        return row;
    }
}
