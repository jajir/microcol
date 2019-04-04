package org.microcol.gui.dock;

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
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
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
        toggleGroup.selectedToggleProperty().addListener(this::onToggleGroupWasChanged);

        panelShips = new HBox();
        panelShips.getStyleClass().add("ships");

        mainPanel = new VBox(panelShips, panelDockCratesPresenter.getContent());
        mainPanel.getStyleClass().add("panel-dock");
    }

    @SuppressWarnings("unused")
    private void onToggleGroupWasChanged(final ObservableValue<? extends Toggle> object,
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
     * Allows to hide ships add cargo slots entirely.
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
        final ToggleButton toggleButtonShip = new ToggleButton();
        final BackgroundImage myBI = new BackgroundImage(imageProvider.getUnitImage(ship),
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                BackgroundSize.DEFAULT);
        toggleButtonShip.getStyleClass().add(SHIP_IN_PORT_STYLE);
        toggleButtonShip.setBackground(new Background(myBI));
        toggleButtonShip.setToggleGroup(toggleGroup);
        toggleButtonShip.setUserData(ship);
        toggleButtonShip.setOnDragDetected(this::onDragDetected);
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
                final ToggleButton button = (ToggleButton) panelShips.getChildren().get(0);
                button.setSelected(true);
            }
        }
    }

    public Optional<UnitWithCargo> getSelectedShip() {
        if (toggleGroup.getSelectedToggle() == null) {
            return Optional.empty();
        }
        return Optional.of((UnitWithCargo) toggleGroup.getSelectedToggle().getUserData());
    }

    /**
     * Move to new behavior interface
     */
    private void onDragDetected(final MouseEvent event) {
        if (event.getSource() instanceof ToggleButton) {
            final ToggleButton butt = (ToggleButton) event.getSource();
            final Unit unit = (Unit) butt.getUserData();
            Preconditions.checkNotNull(unit);
            Preconditions.checkArgument(unit.getType().isShip(), "Unit (%s) have to be ship.");
            ClipboardWritter.make(butt.startDragAndDrop(TransferMode.MOVE))
                    .addImage(butt.getBackground().getImages().get(0).getImage()).addUnit(unit)
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
