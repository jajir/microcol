package org.microcol.gui.util;

import java.util.Optional;

import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.colony.TmpPanel;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.microcol.i18n.I18n;
import org.microcol.model.Unit;
import org.microcol.model.unit.UnitWithCargo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
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

    // FIXME make it abstract, create two instances for Colony and Europe and
    // inject them with guice

    private final Logger logger = LoggerFactory.getLogger(PanelDock.class);

    public static final String SHIP_IN_PORT_STYLE = "paneShip";

    private final ImageProvider imageProvider;

    private final PanelDockCratesController panelCratesController;

    private final HBox panelShips;

    private final ToggleGroup toggleGroup;

    private final PanelDockBehavior panelDockBehavior;

    private final VBox mainPanel;

    private final TmpPanel titledPanel;

    @Inject
    public PanelDock(final ImageProvider imageProvider, final PanelDockBehavior panelDockBehavior,
            final I18n i18n, final EventBus eventBus, final Source source) {
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.panelDockBehavior = Preconditions.checkNotNull(panelDockBehavior);
        panelCratesController = new PanelDockCratesController(imageProvider, panelDockBehavior,
                i18n, eventBus, source);

        toggleGroup = new ToggleGroup();
        toggleGroup.selectedToggleProperty().addListener((object, oldValue, newValue) -> {
            if (toggleGroup.getSelectedToggle() == null) {
                panelCratesController.closeAllCrates();
            } else {
                panelCratesController.setCratesForShip(getSelectedShip().get());
            }
        });

        panelShips = new HBox();
        panelShips.getStyleClass().add("ships");

        mainPanel = new VBox(panelShips, panelCratesController.getContent());

        titledPanel = new TmpPanel();
        titledPanel.getStyleClass().add("panel-dock");
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
            toggleButtonShip.getStyleClass().add(SHIP_IN_PORT_STYLE);
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
