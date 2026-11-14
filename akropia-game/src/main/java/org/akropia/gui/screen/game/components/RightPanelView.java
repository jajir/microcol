package org.akropia.gui.screen.game.components;

import java.util.List;
import java.util.Optional;

import org.akropia.gui.Loc;
import org.akropia.gui.MovementText;
import org.akropia.gui.screen.game.gamepanel.SelectedUnitManager;
import org.akropia.gui.LocalizationHelper;
import org.akropia.gui.MainStageBuilder;
import org.akropia.gui.UnitsPanel;
import org.akropia.gui.event.model.GameModelController;
import org.akropia.gui.screen.game.gamepanel.TileWasSelectedEvent;
import org.akropia.gui.util.JavaFxComponent;
import org.akropia.i18n.I18n;
import org.akropia.model.ColonySighting;
import org.akropia.model.Location;
import org.akropia.model.Model;
import org.akropia.model.Player;
import org.akropia.model.ResourceOverlay;
import org.akropia.model.Terrain;
import org.akropia.model.Unit;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

/**
 * Draw right panel containing info about selected tile and selected unit.
 *
 */
public final class RightPanelView implements JavaFxComponent {

    private final I18n i18n;

    private final TilePainter tilePainter;
    private final SelectedUnitManager selectedUnitManager;

    private final Label tileName;

    private final Label unitsLabel;

    private final UnitsPanel unitsPanel;

    private final LocalizationHelper localizationHelper;

    private final GridPane gridPane;
    private final ScrollPane scrollPaneGamePanel;

    private final GameModelController gameModelController;

    @Inject
    public RightPanelView(final I18n i18n, final UnitsPanel unitsPanel,
            final LocalizationHelper localizationHelper,
            final GameModelController gameModelController,
            final TilePainter tilePainter,
            final SelectedUnitManager selectedUnitManager) {
        this.selectedUnitManager = selectedUnitManager;
        this.i18n = Preconditions.checkNotNull(i18n);
        this.unitsPanel = Preconditions.checkNotNull(unitsPanel);
        this.localizationHelper = Preconditions
                .checkNotNull(localizationHelper);
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
        this.tilePainter = Preconditions.checkNotNull(tilePainter);

        gridPane = new GridPane();
        gridPane.setId("rightPanel");
        gridPane.getStylesheets().add(MainStageBuilder.STYLE_SHEET_RIGHT_PANEL);

        final Pane tileImagePane = new Pane(tilePainter.getCanvas());
        tileImagePane.setId("tileImage");
        gridPane.add(tileImagePane, 0, 0);

        tileName = new Label();
        tileName.setId("tileName");
        final javafx.scene.shape.SVGPath previewFrame = new javafx.scene.shape.SVGPath();
        previewFrame.setId("tilePreviewFrame");
        previewFrame.setContent(
                "M1 1 Q11 0 22 1 T44 1 Q45 12 44 22 T44 44 Q33 45 22 44 T1 44 Q0 33 1 22 T1 1 Z");
        previewFrame.setFill(null);
        previewFrame.setStroke(javafx.scene.paint.Color.BLACK);
        previewFrame.setStrokeWidth(2);
        previewFrame.setMouseTransparent(true);
        previewFrame.visibleProperty()
                .bind(tileName.textProperty().isNotEmpty());
        tileImagePane.getChildren().add(previewFrame);

        tileName.setMinHeight(Region.USE_PREF_SIZE);
        gridPane.add(tileName, 1, 0);

        unitsLabel = new Label();
        gridPane.add(unitsLabel, 0, 1);

        gridPane.add(unitsPanel.getNode(), 0, 2, 2, 1);
        tileName.setWrapText(true);
        gridPane.setMinWidth(0);
        scrollPaneGamePanel = new ScrollPane(gridPane) {
            @Override
            protected double computePrefHeight(final double width) {
                return Math.max(120,
                        gridPane.prefHeight(width > 20 ? width - 20 : 208)
                                + snappedTopInset() + snappedBottomInset());
            }
        };
        scrollPaneGamePanel.setId("mapDetailsScroll");
        scrollPaneGamePanel.setFitToWidth(false);
        gridPane.prefWidthProperty()
                .bind(scrollPaneGamePanel.widthProperty().subtract(20));
        gridPane.setMaxWidth(Region.USE_PREF_SIZE);
        scrollPaneGamePanel.setMinHeight(120);
        scrollPaneGamePanel.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        // Keep the content width stable when the scrollbar appears.
        scrollPaneGamePanel.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

    }

    void cleanView() {
        Platform.runLater(() -> {
            unitsPanel.clear();
            tileName.setText("");
        });
        tilePainter.clear();
    }

    public void refreshView(final TileWasSelectedEvent event) {
        if (event != null) {
            refreshView(event.getLocation());
        }
    }

    public void refreshView(final Location location) {
        Preconditions.checkNotNull(location);
        Platform.runLater(() -> {
            final StringBuilder sb = new StringBuilder(200);
            unitsPanel.clear();
            if (isDiscovered(location)) {
                final Terrain terrain = getTerrainAt(location);
                tilePainter.setTerrain(terrain, location);
                sb.append(localizationHelper
                        .getTerrainName(terrain.getTerrainType()));
                sb.append("");
                sb.append("\n");
                sb.append(movementDescription(location, terrain));
                // Only a discovered square names its special resource.
                if (terrain.getResourceOverlay() != ResourceOverlay.NONE) {
                    sb.append("\n");
                    sb.append(localizationHelper.getResourceOverlayName(
                            terrain.getResourceOverlay()));
                }
                final Player player = gameModelController.getHumanPlayer();
                // Objects come from the player's permitted view: live inside
                // present sight, remembered snapshot (marked stale) outside.
                final Optional<ColonySighting> oColony = player
                        .getKnownColonyAt(location);
                if (oColony.isPresent()) {
                    tilePainter.paintColony(oColony.get());
                    if (!player.isCurrentlyVisible(location)) {
                        sb.append("\n");
                        sb.append(i18n.get(Loc.colonyLastSeen,
                                oColony.get().getColonyName(),
                                oColony.get().getOwner().getDisplayName(),
                                oColony.get().getObservedYear()));
                    }
                }
                final List<Unit> units = player.getObservedUnitsAt(location);
                if (units.isEmpty()) {
                    unitsLabel.setText("");
                } else {
                    tilePainter.paintUnit(units);
                    /**
                     * Current player is not same as human player. For purposes
                     * of this method it will be sufficient.
                     */
                    unitsPanel.setUnits(getModel().getCurrentPlayer(), units);
                }
            } else {
                tilePainter.setHiddenTerrain(location);
                sb.append(i18n.get(Loc.unitsPanel_unexplored));
            }
            tileName.setText(sb.toString());
        });
    }

    private String movementDescription(final Location location,
            final Terrain terrain) {
        final Optional<Unit> selected = selectedUnitManager.getSelectedUnit();
        if (selected.isEmpty()
                || selected.get().getOwner() != gameModelController
                        .getHumanPlayer()
                || !selected.get().isAtPlaceLocation()
                || !selected.get().getLocation().isNeighbor(location)) {
            return i18n.get(Loc.baseTerrainCost, terrain.getMoveCost());
        }
        final Unit unit = selected.get();
        if (!unit.isPossibleToMoveAt(location) || unit.isFishing()) {
            return i18n.get(Loc.stepUnavailable);
        }
        final int cost = unit.getEntryCostThirds(unit.getLocation(), location);
        String text = i18n.get(Loc.stepCost, MovementText.format(cost));
        if (!unit.isShip() && cost == 1) {
            if (getModel().getMap().hasRoadAt(unit.getLocation())
                    && getModel().getMap().hasRoadAt(location)) {
                text += "\n" + i18n.get(Loc.stepAlongRoad);
            } else if (getModel().getMap().hasRiverSegment(unit.getLocation(),
                    location)) {
                text += "\n" + i18n.get(Loc.stepAlongRiver);
            }
        }
        if (!unit.canMoveStepTo(location)) {
            text += "\n" + i18n.get(Loc.stepUnavailable);
            if (!unit.canAffordStepTo(location)) {
                text += "\n" + i18n.get(Loc.stepInsufficientMovement);
            }
        }
        return text;
    }

    private Model getModel() {
        return gameModelController.getModel();
    }

    private Terrain getTerrainAt(final Location location) {
        return getModel().getMap().getTerrainAt(location);
    }

    /**
     * If user already explored selected tile.
     *
     * @param location required location
     * @return return <code>true</code> when selected location was already
     *         discovered otherwise return <code>false</code>.
     */
    private boolean isDiscovered(final Location location) {
        return gameModelController.getHumanPlayer().isVisible(location);
    }

    @Override
    public ScrollPane getContent() {
        return scrollPaneGamePanel;
    }

}
