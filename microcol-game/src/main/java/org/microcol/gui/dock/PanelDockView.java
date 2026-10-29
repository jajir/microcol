package org.microcol.gui.dock;

import org.microcol.gui.util.ActionButtons;
import org.microcol.gui.image.ButtonIcon;
import java.util.Optional;
import java.util.function.Consumer;

import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.UnitTypes;
import org.microcol.gui.Loc;
import org.microcol.i18n.I18n;
import org.microcol.gui.util.ClipboardWritter;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.model.Unit;
import org.microcol.model.unit.UnitWithCargo;

import com.google.common.base.Preconditions;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Label;
import javafx.scene.control.ContentDisplay;
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

    private boolean harborLayout;
    private I18n i18n;
    private final Label cargoTitle = new Label();

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
        updateCargoTitle();
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
        mainPanel.setVisible(isVisible || harborLayout);
    }

    void cleanShips() {
        toggleGroup.selectToggle(null);
        toggleGroup.getToggles().clear();
        panelShips.getChildren().clear();
    }

    void enableHarborLayout() {
        harborLayout = true;
        final Region crates = (Region) mainPanel.getChildren().remove(1);
        final ScrollPane scroll = new ScrollPane(panelShips);
        scroll.setId("europe-dock-scroll");
        scroll.getStyleClass().add("harbor-scroll");
        scroll.setFitToHeight(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setMinHeight(96);
        scroll.setPrefHeight(96);
        scroll.setMaxHeight(96);
        panelShips.setMinWidth(Region.USE_PREF_SIZE);
        cargoTitle.setId("europe-cargo-title");
        final VBox cargo = new VBox(cargoTitle, crates);
        cargo.getStyleClass().add("harbor-cargo");
        mainPanel.getChildren().setAll(scroll, cargo);
    }

    void updateLanguage(final I18n i18n) {
        this.i18n = i18n;
        updateCargoTitle();
    }

    private String shipLabel(final UnitWithCargo ship) {
        return (i18n == null ? ship.getType().name()
                : i18n.get(UnitTypes.getUnitName(ship.getType()))) + " #"
                + ship.getId();
    }

    private void updateCargoTitle() {
        if (harborLayout && i18n != null) {
            cargoTitle.setText(getSelectedShip()
                    .map(ship -> i18n.get(Loc.selectedShipCargo, ship.getId()))
                    .orElseGet(() -> i18n.get(Loc.selectShipCargo)));
        }
    }

    void addShip(final UnitWithCargo ship) {
        final ToggleButton toggleButtonShip = ActionButtons
                .toggle(ButtonIcon.SHIP);
        final javafx.scene.image.ImageView icon = new javafx.scene.image.ImageView(
                imageProvider.getUnitImage(ship));
        icon.setFitWidth(harborLayout ? 45 : 32);
        icon.setFitHeight(harborLayout ? 45 : 32);
        icon.setPreserveRatio(true);
        icon.setMouseTransparent(true);
        toggleButtonShip.getStyleClass().add(SHIP_IN_PORT_STYLE);
        toggleButtonShip.setGraphic(icon);
        toggleButtonShip.setToggleGroup(toggleGroup);
        toggleButtonShip.setUserData(ship);
        if (harborLayout) {
            toggleButtonShip.setId("europe-dock-ship-" + ship.getId());
            toggleButtonShip.getStyleClass().add("harbor-unit");
            toggleButtonShip.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            toggleButtonShip.setAccessibleText(shipLabel(ship));
            toggleButtonShip.setMinWidth(Region.USE_PREF_SIZE);
        }
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
        }
        if (toggleGroup.getSelectedToggle() == null) {
            if (!panelShips.getChildren().isEmpty()) {
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
