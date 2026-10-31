package org.microcol.gui.dialog;

import com.google.inject.Inject;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ButtonsBarYesNo;
import org.microcol.gui.util.ViewUtil;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.LocalizationHelper;
import org.microcol.i18n.I18n;
import org.microcol.model.Unit;
import org.microcol.model.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dialog for choosing passengers to land from a list (R-0379).
 *
 * <p>
 * The dialog displays a caption describing the destination, an independent
 * check box for each eligible passenger, all unchecked on every opening, and a
 * {@link ButtonsBarYesNo} for confirmation or cancellation. Confirmation is
 * disabled while nothing is checked. The dialog returns the checked passengers
 * after the user closes the window; cancel and Escape return none.
 * </p>
 */
public final class ChoosePassengerDialog extends AbstractMessageWindow {

    private final LocalizationHelper localizationHelper;
    private final ImageProvider imageProvider;
    private final I18n i18n;

    private List<Unit> result;

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
     * Opens the dialog and returns the checked passengers.
     *
     * @param passengers  the list of passengers that can be chosen
     * @param destination the destination to display in the caption
     * @return the checked passengers in list order, or an empty list if
     *         cancelled
     */
    public List<Unit> choose(List<Unit> passengers, Location destination) {
        result = List.of();

        Label heading = new Label(i18n.get(Dialog.passenger_heading));
        heading.setId("passenger-heading");
        heading.setWrapText(true);
        heading.setMaxWidth(420);

        Label caption = new Label(i18n.get(Dialog.passenger_destination,
                destination.getX() + ", " + destination.getY()));
        caption.setWrapText(true);
        caption.setMaxWidth(420);

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        vbox.getChildren().addAll(heading, caption);

        final List<CheckBox> choices = new ArrayList<>();
        for (Unit unit : passengers) {
            CheckBox cb = new CheckBox();
            cb.setUserData(unit);
            cb.setId("passenger-choice-" + unit.getId());

            int slotIndex = unit.getPlaceCargoSlot().getCargoSlot().getIndex();
            cb.setText(i18n.get(Dialog.passenger_hold, slotIndex + 1,
                    localizationHelper.getUnitName(unit.getType())));

            ImageView icon = new ImageView(imageProvider.getUnitImage(unit));
            cb.setGraphic(icon);
            // The frame's contour padding must not truncate a passenger name.
            cb.setMinWidth(Region.USE_PREF_SIZE);

            choices.add(cb);
            vbox.getChildren().add(cb);
        }

        ButtonsBarYesNo buttons = new ButtonsBarYesNo(
                i18n.get(Dialog.passenger_confirm), i18n.get(Dialog.cancel));
        Button confirmBtn = buttons.getButtonYes();
        Button cancelBtn = buttons.getButtonNo();
        confirmBtn.setId("passenger-confirm");
        cancelBtn.setId("passenger-cancel");
        confirmBtn.setMinWidth(Region.USE_PREF_SIZE);
        cancelBtn.setMinWidth(Region.USE_PREF_SIZE);

        confirmBtn.disableProperty()
                .bind(Bindings.createBooleanBinding(
                        () -> choices.stream().noneMatch(CheckBox::isSelected),
                        choices.stream().map(CheckBox::selectedProperty)
                                .toArray(Observable[]::new)));

        confirmBtn.setOnAction(e -> {
            result = choices.stream().filter(CheckBox::isSelected)
                    .map(cb -> (Unit) cb.getUserData())
                    .collect(Collectors.toList());
            close();
        });

        cancelBtn.setOnAction(e -> {
            result = List.of();
            close();
        });

        vbox.getChildren().add(buttons.getContent());
        init(vbox);
        setTitle(i18n.get(Dialog.passenger_heading));

        showAndWait();

        return result;
    }

    @Override
    protected void onCancelDialog() {
        result = List.of();
        super.onCancelDialog();
    }
}
