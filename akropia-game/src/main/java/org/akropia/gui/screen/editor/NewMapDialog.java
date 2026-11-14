package org.akropia.gui.screen.editor;

import java.util.Optional;

import org.akropia.gui.image.ButtonIcon;
import org.akropia.gui.util.AbstractMessageWindow;
import org.akropia.gui.util.ModalActions;
import org.akropia.gui.util.ActionButtons;
import org.akropia.gui.util.ViewUtil;
import org.akropia.i18n.I18n;
import org.akropia.model.Location;
import org.akropia.model.generation.ScenarioDraft;
import org.akropia.model.generation.ScenarioDraftException;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 * Asks for the width and height of a new ocean map. Invalid sizes are explained
 * in the dialog, which stays open; Cancel and Escape choose nothing.
 */
final class NewMapDialog extends AbstractMessageWindow {

    static final String WIDTH_ID = "editorWidth";

    static final String HEIGHT_ID = "editorHeight";

    static final String ERROR_ID = "editorDimensionError";

    static final String CONFIRM_ID = "editorDimensionConfirm";

    static final String CANCEL_ID = "editorDimensionCancel";

    private final EditorMessages messages;

    private final TextField width = new TextField();

    private final TextField height = new TextField();

    private final Label error = new Label();

    private Location result;

    NewMapDialog(final ViewUtil viewUtil, final I18n i18n,
            final EditorMessages messages, final Location current) {
        super(viewUtil, i18n);
        this.messages = messages;
        setTitle(i18n.get(EditorText.newTitle));
        final Label title = new Label(i18n.get(EditorText.newTitle));
        title.getStyleClass().add("label-title");
        final Label bounds = new Label(i18n.get(EditorText.newBounds,
                ScenarioDraft.MIN_SIDE, ScenarioDraft.MAX_SIDE));
        bounds.setWrapText(true);
        bounds.setMaxWidth(420);
        width.setId(WIDTH_ID);
        height.setId(HEIGHT_ID);
        width.setText(String.valueOf(current.getX()));
        height.setText(String.valueOf(current.getY()));
        final Label widthLabel = new Label(i18n.get(EditorText.newWidth));
        widthLabel.setLabelFor(width);
        final Label heightLabel = new Label(i18n.get(EditorText.newHeight));
        heightLabel.setLabelFor(height);
        final GridPane fields = new GridPane();
        fields.setHgap(10);
        fields.setVgap(8);
        fields.addRow(0, widthLabel, width);
        fields.addRow(1, heightLabel, height);
        error.setId(ERROR_ID);
        error.setWrapText(true);
        error.setMaxWidth(420);
        final Button confirm = ActionButtons.button(ButtonIcon.CONFIRM,
                i18n.get(EditorText.newConfirm));
        confirm.setId(CONFIRM_ID);
        confirm.setDefaultButton(true);
        confirm.setOnAction(event -> confirm());
        final Button cancel = ActionButtons.button(ButtonIcon.CANCEL,
                i18n.get(EditorText.newCancel));
        cancel.setId(CANCEL_ID);
        cancel.setCancelButton(true);
        cancel.setOnAction(event -> close());
        final VBox content = new VBox(12, title, bounds, fields, error,
                new ModalActions(confirm, cancel));
        content.setPadding(new Insets(20));
        init(content);
    }

    private void confirm() {
        try {
            result = Location.of(ScenarioDraft.parseSide(width.getText()),
                    ScenarioDraft.parseSide(height.getText()));
            close();
        } catch (ScenarioDraftException rejected) {
            error.setText(messages.describe(rejected));
        }
    }

    /**
     * @return chosen width and height as x and y, empty when canceled
     */
    Optional<Location> choose() {
        result = null;
        showAndWait();
        return Optional.ofNullable(result);
    }

}
