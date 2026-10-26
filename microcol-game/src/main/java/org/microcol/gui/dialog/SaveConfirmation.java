package org.microcol.gui.dialog;

import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Escape and window closure always cancel the pending action. */
final class SaveConfirmation extends AbstractMessageWindow {
    private int result;
    private final I18n texts;

    SaveConfirmation(final ViewUtil view, final I18n texts) {
        super(view, texts);
        this.texts = texts;
    }

    int choose(final String message, final boolean leaving) {
        result = 0;
        final Label label = new Label(message);
        label.setWrapText(true);
        label.setMaxWidth(520);
        final HBox buttons = new HBox(10);
        button(buttons,
                leaving ? SavedGameText.saveContinue : SavedGameText.confirm,
                "saveConfirm", 1);
        if (leaving) {
            button(buttons, SavedGameText.discard, "saveDiscard", 2);
        }
        button(buttons, SavedGameText.cancel, "saveCancel", 0);
        final VBox content = new VBox(16, label, buttons);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("save-confirmation");
        init(content);
        getScene().getStylesheets().add(
                getClass().getResource("/gui/SavedGames.css").toExternalForm());
        showAndWait();
        return result;
    }

    private void button(final HBox row, final SavedGameText text,
            final String id, final int value) {
        final Button button = new Button(texts.get(text));
        button.setId(id);
        button.setOnAction(event -> {
            result = value;
            close();
        });
        row.getChildren().add(button);
    }
}
