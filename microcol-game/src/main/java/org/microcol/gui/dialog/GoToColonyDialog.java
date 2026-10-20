package org.microcol.gui.dialog;

import java.util.List;
import java.util.Optional;

import org.microcol.gui.buttonpanel.Buttons;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ButtonsBarYesNo;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.Colony;

import com.google.inject.Inject;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

/**
 * Dialog for choosing the named friendly colony a unit should go to. The
 * colonies are the model's current offer; the caller still asks the model to
 * accept the choice, because the state can change while the dialog is open.
 */
public final class GoToColonyDialog extends AbstractMessageWindow {

    private final I18n i18n;

    private Optional<Colony> result = Optional.empty();

    @Inject
    GoToColonyDialog(final ViewUtil viewUtil, final I18n i18n) {
        super(viewUtil, i18n);
        this.i18n = i18n;
    }

    /**
     * Ask the player and wait for the answer.
     *
     * @param colonies colonies offered by the model, never empty
     * @return the chosen colony, or empty when the dialog was canceled or
     *         closed
     */
    public Optional<Colony> choose(final List<Colony> colonies) {
        result = Optional.empty();
        final Label caption = new Label(i18n.get(Buttons.goToColonyCaption));
        caption.setId("goToColonyCaption");
        caption.setWrapText(true);
        caption.setMaxWidth(420);
        final ToggleGroup group = new ToggleGroup();
        final VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.getChildren().add(caption);
        for (final Colony colony : colonies) {
            final RadioButton choice = new RadioButton(colony.getName());
            choice.setToggleGroup(group);
            choice.setUserData(colony);
            choice.setId("goto-colony-" + colony.getId());
            if (group.getToggles().size() == 1) {
                choice.setSelected(true);
            }
            root.getChildren().add(choice);
        }
        final ButtonsBarYesNo buttons = new ButtonsBarYesNo(
                i18n.get(Buttons.goToColonyConfirm),
                i18n.get(Buttons.goToColonyCancel));
        final Button confirm = buttons.getButtonYes();
        confirm.setId("goto-colony-confirm");
        buttons.getButtonNo().setId("goto-colony-cancel");
        confirm.disableProperty().bind(group.selectedToggleProperty().isNull());
        confirm.setOnAction(event -> {
            result = Optional.ofNullable(
                    (Colony) group.getSelectedToggle().getUserData());
            close();
        });
        buttons.getButtonNo().setOnAction(event -> onCancelDialog());
        root.getChildren().add(buttons.getContent());
        init(root);
        setTitle(i18n.get(Buttons.goToColonyTitle));
        showAndWait();
        return result;
    }

    @Override
    protected void onCancelDialog() {
        result = Optional.empty();
        super.onCancelDialog();
    }
}
