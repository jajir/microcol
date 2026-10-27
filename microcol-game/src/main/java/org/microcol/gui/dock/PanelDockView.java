package org.microcol.gui.dock;

import org.microcol.gui.util.ActionButtons;
import org.microcol.gui.image.ButtonIcon;
import java.util.Optional;
import java.util.function.Consumer;

import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.ClipboardWritter;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.model.Unit;
import org.microcol.model.unit.UnitWithCargo;

import com.google.common.base.Preconditions;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class PanelDockView implements JavaFxComponent {

    public static final String SHIP_IN_PORT_STYLE = "paneShip";

    private final ImageProvider imageProvider;

    private final HBox panelShips;

    private final ToggleGroup toggleGroup;

    private final VBox mainPanel;

    private Consumer<Optional<UnitWithCargo>> onSelectedUnitWasChanged;

    public PanelDockView(final ImageProvider imageProvider,
            final PanelDockCratesPresenter panelDockCratesPresenter) {
        this.imageProvider = Preconditions.checkNotNull(imageProvider);

        toggleGroup = new ToggleGroup();
        toggleGroup.selectedToggleProperty()
                .addListener(this::onToggleGroupWasChanged);

        panelShips = new HBox();
        panelShips.getStyleClass().add("ships");

        mainPanel = new VBox(panelShips, panelDockCratesPresenter.getContent());
        // Empty dock space must not intercept the colony buildings below it.
        mainPanel.setPickOnBounds(false);
        mainPanel.getStyleClass().add("panel-dock");
    }

    @SuppressWarnings("unused")
    private void onToggleGroupWasChanged(
            final ObservableValue<? extends Toggle> object,
            final Toggle oldValue, final Toggle newValue) {
        if (onSelectedUnitWasChanged != null) {
            if (toggleGroup.getSelectedToggle() == null) {
                onSelectedUnitWasChanged.accept(Optional.empty());
            } else {
                onSelectedUnitWasChanged.accept(getSelectedShip());
            }
        }
    }

    /**
     * Set the visibility of ships and cargo slots.
     *
     * @param isVisible whether the dock is visible
     */
    void setVisible(final boolean isVisible) {
        mainPanel.setVisible(isVisible);
    }

    void cleanShips() {
        panelShips.getChildren().clear();
    }

    void unselectShip() {
        toggleGroup.selectToggle(null);
    }

    void addShip(final UnitWithCargo ship) {
        final ToggleButton toggleButtonShip = ActionButtons
                .toggle(ButtonIcon.SHIP);
        final javafx.scene.image.ImageView icon = new javafx.scene.image.ImageView(
                imageProvider.getUnitImage(ship));
        icon.setFitWidth(32);
        icon.setFitHeight(32);
        icon.setPreserveRatio(true);
        icon.setMouseTransparent(true);
        toggleButtonShip.getStyleClass().add(SHIP_IN_PORT_STYLE);
        toggleButtonShip.setGraphic(icon);
        toggleButtonShip.setToggleGroup(toggleGroup);
        toggleButtonShip.setUserData(ship);
        if (ship.isShip()) {
            toggleButtonShip.setOnDragDetected(this::onDragDetected);
        }
        panelShips.getChildren().add(toggleButtonShip);
    }

    public void selectShip(final Optional<UnitWithCargo> oSelectedShip) {
        if (oSelectedShip.isPresent()) {
            final UnitWithCargo selectedShip = oSelectedShip.get();
            panelShips.getChildren().forEach(node -> {
                final ToggleButton button = (ToggleButton) node;
                final UnitWithCargo ship = (UnitWithCargo) button.getUserData();
                button.setSelected(selectedShip.equals(ship));
            });
        } else {
            // select first
            if (panelShips.getChildren().size() > 0) {
                final ToggleButton button = (ToggleButton) panelShips
                        .getChildren().get(0);
                button.setSelected(true);
            }
        }
    }

    public Optional<UnitWithCargo> getSelectedShip() {
        if (toggleGroup.getSelectedToggle() == null) {
            return Optional.empty();
        }
        return Optional.of(
                (UnitWithCargo) toggleGroup.getSelectedToggle().getUserData());
    }

    // Move drag handling to the behavior interface.
    private void onDragDetected(final MouseEvent event) {
        if (event.getSource() instanceof ToggleButton) {
            final ToggleButton butt = (ToggleButton) event.getSource();
            final Unit unit = (Unit) butt.getUserData();
            Preconditions.checkNotNull(unit);
            Preconditions.checkArgument(unit.getType().isShip(),
                    "Unit (%s) have to be ship.");
            ClipboardWritter.make(butt.startDragAndDrop(TransferMode.MOVE))
                    .addImage(imageProvider.getUnitImage(unit)).addUnit(unit)
                    .build();
            event.consume();
        }
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

    void setOnSelectedUnitWasChanged(
            final Consumer<Optional<UnitWithCargo>> onSelectedUnitWasChanged) {
        this.onSelectedUnitWasChanged = onSelectedUnitWasChanged;
    }

}
