package org.akropia.gui.dialog;

import java.util.List;
import java.util.Optional;

import org.akropia.gui.buttonpanel.Buttons;
import org.akropia.gui.shortcut.KeyboardShortcuts;
import org.akropia.gui.shortcut.ShortcutAction;
import org.akropia.gui.util.AbstractMessageWindow;
import org.akropia.gui.util.ButtonsBarYesNo;
import org.akropia.gui.util.ViewUtil;
import org.akropia.i18n.I18n;
import org.akropia.model.Colony;
import org.akropia.model.TradeRouteStop;

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

    private Optional<TradeRouteStop> result = Optional.empty();

    private final KeyboardShortcuts shortcuts;

    @Inject
    GoToColonyDialog(final ViewUtil viewUtil, final I18n i18n,
            final KeyboardShortcuts shortcuts) {
        super(viewUtil, i18n);
        this.i18n = i18n;
        this.shortcuts = shortcuts;
    }

    /**
     * Ask the player and wait for the answer.
     *
     * @param colonies colonies offered by the model, never empty
     * @return the chosen colony, or empty when the dialog was canceled or
     *         closed
     */
    public Optional<TradeRouteStop> choose(final List<Colony> colonies,
            final boolean europe) {
        result = Optional.empty();
        final Label caption = new Label(i18n.get(Buttons.goToColonyCaption));
        caption.setId("goToColonyCaption");
        caption.setWrapText(true);
        caption.setMaxWidth(420);
        final ToggleGroup group = new ToggleGroup();
        final VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.getChildren().add(caption);
        if (europe) {
            final RadioButton choice = new RadioButton(
                    i18n.get(Buttons.goToEurope));
            choice.setToggleGroup(group);
            choice.setUserData(TradeRouteStop.europe());
            choice.setId("goto-europe");
            choice.setSelected(true);
            root.getChildren().add(choice);
        }
        for (final Colony colony : colonies) {
            final RadioButton choice = new RadioButton(colony.getName());
            choice.setToggleGroup(group);
            choice.setUserData(TradeRouteStop.ofColony(colony.getId()));
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
                    (TradeRouteStop) group.getSelectedToggle().getUserData());
            close();
        });
        buttons.getButtonNo().setOnAction(event -> onCancelDialog());
        root.getChildren().add(buttons.getContent());
        init(root);
        setTitle(shortcuts.withHint(i18n.get(Buttons.goToColonyTitle),
                ShortcutAction.GO_TO));
        showAndWait();
        return result;
    }

    @Override
    protected void onCancelDialog() {
        result = Optional.empty();
        super.onCancelDialog();
    }
}
