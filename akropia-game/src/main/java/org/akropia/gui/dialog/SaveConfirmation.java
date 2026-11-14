package org.akropia.gui.dialog;

import org.akropia.gui.util.ActionButtons;
import org.akropia.gui.image.ButtonIcon;
import org.akropia.gui.util.AbstractMessageWindow;
import org.akropia.gui.util.ModalActions;
import org.akropia.gui.util.ViewUtil;
import org.akropia.i18n.I18n;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/** Escape and window closure always cancel the pending action. */
public final class SaveConfirmation extends AbstractMessageWindow {
    private int result;
    private final I18n texts;

    public SaveConfirmation(final ViewUtil view, final I18n texts) {
        super(view, texts);
        this.texts = texts;
    }

    public int choose(final String message, final boolean leaving) {
        return choose(message, leaving, SavedGameText.overwriteAction);
    }

    public int choose(final String message, final boolean leaving,
            final SavedGameText confirmText) {
        result = 0;
        final Label label = new Label(message);
        label.setWrapText(true);
        label.setMaxWidth(520);
        final Button confirm = button(
                leaving ? SavedGameText.saveContinue : confirmText,
                "saveConfirm", 1);
        final Button cancel = button(SavedGameText.cancel, "saveCancel", 0);
        cancel.setCancelButton(true);
        final ModalActions buttons = leaving
                ? new ModalActions(confirm,
                        button(SavedGameText.discard, "saveDiscard", 2), cancel)
                : new ModalActions(confirm, cancel);
        final VBox content = new VBox(16, label, buttons);
        content.setPadding(new Insets(20));
        content.getStyleClass().add("save-confirmation");
        init(content);
        getScene().getStylesheets().add(
                getClass().getResource("/gui/SavedGames.css").toExternalForm());
        cancel.requestFocus();
        showAndWait();
        return result;
    }

    private Button button(final SavedGameText text, final String id,
            final int value) {
        final Button button = ActionButtons
                .button((value == 0 ? ButtonIcon.CANCEL
                        : text == SavedGameText.discard ? ButtonIcon.EXIT
                                : text == SavedGameText.saveContinue
                                        ? ButtonIcon.SAVE
                                        : ButtonIcon.CONFIRM),
                        texts.get(text));
        button.setId(id);
        button.setOnAction(event -> {
            result = value;
            close();
        });
        return button;
    }
}
