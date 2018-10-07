package org.microcol.gui.util;

import java.util.Optional;

import org.microcol.gui.gamepanel.GamePanelView;
import org.microcol.gui.image.ImageProvider;
import org.microcol.i18n.I18n;
import org.microcol.model.Unit;
import org.microcol.model.unit.UnitWithCargo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

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

/**
 * Contains ships in port. Good from ships could be loaded/unloaded.
 * <p>
 * Class should not be defined as singleton because is used in Europe port and
 * in colonies.
 * </p>
 */
public final class PanelDock implements JavaFxComponent, UpdatableLanguage, Repaintable {

    private final Logger logger = LoggerFactory.getLogger(PanelDock.class);

    private final ImageProvider imageProvider;

    private final PanelDockCratesController panelCratesController;

    private final HBox panelShips;

    private final ToggleGroup toggleGroup;

    private final PanelDockBehavior panelDockBehavior;

    private final VBox mainPanel;

    private final TitledPanel titledPanel;

    @Inject
    public PanelDock(final ImageProvider imageProvider, final PanelDockBehavior panelDockBehavior) {
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.panelDockBehavior = Preconditions.checkNotNull(panelDockBehavior);
        panelCratesController = new PanelDockCratesController(imageProvider, panelDockBehavior);

        toggleGroup = new ToggleGroup();
        toggleGroup.selectedToggleProperty().addListener((object, oldValue, newValue) -> {
            if (toggleGroup.getSelectedToggle() == null) {
                panelCratesController.closeAllCrates();
            } else {
                panelCratesController.setCratesForShip(getSelectedShip().get());
            }
        });

        panelShips = new HBox();
        panelShips.setMinHeight(GamePanelView.TILE_WIDTH_IN_PX);
        
        mainPanel = new VBox(panelShips, panelCratesController.getPanelCratesView());
        mainPanel.getStyleClass().add("panel-dock");
        
        titledPanel = new TitledPanel();
        titledPanel.getContentPane().getChildren().add(mainPanel);
    }

    @Override
    public void repaint() {
        panelShips.getChildren().clear();
        Optional<UnitWithCargo> selectedUnit = getSelectedShip();
        toggleGroup.selectToggle(null);
        panelDockBehavior.getUnitsInPort().forEach(unit -> {
            final ToggleButton toggleButtonShip = new ToggleButton();
            final BackgroundImage myBI = new BackgroundImage(imageProvider.getUnitImage(unit),
                    BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER, BackgroundSize.DEFAULT);
            toggleButtonShip.getStyleClass().add("paneShip");
            toggleButtonShip.setBackground(new Background(myBI));
            toggleButtonShip.setToggleGroup(toggleGroup);
            toggleButtonShip.setUserData(unit);
            toggleButtonShip.setOnDragDetected(this::onDragDetected);
            toggleButtonShip
                    .setSelected(selectedUnit.isPresent() && unit.equals(selectedUnit.get()));
            panelShips.getChildren().add(toggleButtonShip);
        });
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        titledPanel.setTitle(i18n.get(Util.panelDockTitle));
    }

    public void repaintCurrectShipsCrates() {
        panelCratesController.setCratesForShip(getSelectedShip().get());
    }

    private Optional<UnitWithCargo> getSelectedShip() {
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
            logger.debug("Start dragging unit: " + unit);
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
        return titledPanel;
    }

}
