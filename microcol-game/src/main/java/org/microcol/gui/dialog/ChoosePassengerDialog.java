package org.microcol.gui.dialog;

import com.google.inject.Inject;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ButtonsBarYesNo;
import org.microcol.gui.util.ViewUtil;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.LocalizationHelper;
import org.microcol.i18n.I18n;
import org.microcol.model.Unit;
import org.microcol.model.Location;

import java.util.List;
import java.util.Optional;

/**
 * Dialog for choosing a passenger from a list.
 *
 * <p>
 * The dialog displays a caption describing the destination, a list of radio
 * buttons for each available passenger, and a {@link ButtonsBarYesNo} for
 * confirmation or cancellation. The dialog keeps its own {@link Optional}
 * result that is returned after the user closes the window.
 * </p>
 */
public final class ChoosePassengerDialog extends AbstractMessageWindow {

    private final LocalizationHelper localizationHelper;
    private final ImageProvider imageProvider;
    private final I18n i18n;

    private Optional<Unit> result;

    @Inject
    public ChoosePassengerDialog(ViewUtil viewUtil, I18n i18n,
            LocalizationHelper localizationHelper,
            ImageProvider imageProvider) {
        super(viewUtil, i18n);
        this.localizationHelper = localizationHelper;
        this.imageProvider = imageProvider;
        this.i18n = i18n;
    }

    /**
     * Opens the dialog and returns the chosen passenger.
     *
     * @param passengers  the list of passengers that can be chosen
     * @param destination the destination to display in the caption
     * @return an {@link Optional} containing the chosen unit, or empty if
     *         cancelled
     */
    public Optional<Unit> choose(List<Unit> passengers, Location destination) {
        result = Optional.empty();

        Label caption = new Label(i18n.get(Dialog.passenger_destination,
                destination.getX() + ", " + destination.getY()));
        caption.setWrapText(true);
        caption.setMaxWidth(420);

        ToggleGroup group = new ToggleGroup();

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        vbox.getChildren().add(caption);

        for (Unit unit : passengers) {
            RadioButton rb = new RadioButton();
            rb.setToggleGroup(group);
            if (group.getToggles().size() == 1) {
                rb.setSelected(true);
            }
            rb.setUserData(unit);
            rb.setId("passenger-choice-" + unit.getId());

            int slotIndex = unit.getPlaceCargoSlot().getCargoSlot().getIndex();
            rb.setText(i18n.get(Dialog.passenger_hold, slotIndex + 1,
                    localizationHelper.getUnitName(unit.getType())));

            ImageView icon = new ImageView(imageProvider.getUnitImage(unit));
            rb.setGraphic(icon);

            vbox.getChildren().add(rb);
        }

        ButtonsBarYesNo buttons = new ButtonsBarYesNo(
                i18n.get(Dialog.passenger_confirm), i18n.get(Dialog.cancel));
        Button confirmBtn = buttons.getButtonYes();
        Button cancelBtn = buttons.getButtonNo();
        confirmBtn.setId("passenger-confirm");
        cancelBtn.setId("passenger-cancel");

        confirmBtn.disableProperty()
                .bind(group.selectedToggleProperty().isNull());

        confirmBtn.setOnAction(e -> {
            result = Optional
                    .ofNullable((Unit) group.getSelectedToggle().getUserData());
            close();
        });

        cancelBtn.setOnAction(e -> {
            result = Optional.empty();
            close();
        });

        vbox.getChildren().add(buttons.getContent());
        init(vbox);
        setTitle(i18n.get(Dialog.passenger_title));

        showAndWait();

        return result;
    }

    @Override
    protected void onCancelDialog() {
        result = Optional.empty();
        super.onCancelDialog();
    }
}
