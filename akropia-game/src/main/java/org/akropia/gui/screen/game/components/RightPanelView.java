package org.akropia.gui.screen.game.components;

import org.akropia.model.GoodsType;
import org.akropia.gui.FoodText;
import com.google.common.eventbus.EventBus;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.akropia.gui.GoodsTypeName;
import org.akropia.gui.Loc;
import org.akropia.gui.MovementText;
import org.akropia.gui.screen.game.gamepanel.SelectedUnitManager;
import org.akropia.gui.LocalizationHelper;
import org.akropia.gui.MainStageBuilder;
import org.akropia.gui.UnitsPanel;
import org.akropia.gui.event.model.GameModelController;
import org.akropia.gui.image.ImageProvider;
import org.akropia.gui.screen.game.gamepanel.TileWasSelectedEvent;
import org.akropia.gui.util.JavaFxComponent;
import org.akropia.gui.util.ActionButtons;
import org.akropia.i18n.I18n;
import org.akropia.model.ColonySighting;
import org.akropia.model.Location;
import org.akropia.model.Model;
import org.akropia.model.Player;
import org.akropia.model.ResourceOverlay;
import org.akropia.model.Terrain;
import org.akropia.model.TerrainProduction;
import org.akropia.model.Unit;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * Draw right panel containing info about selected tile and selected unit.
 *
 */
public final class RightPanelView implements JavaFxComponent {

    private final I18n i18n;

    private final EventBus eventBus;

    private final TilePainter tilePainter;
    private final SelectedUnitManager selectedUnitManager;

    private final Label tileName;
    private final Label tileFacts = new Label();
    private final javafx.scene.shape.SVGPath terrainArrow = new javafx.scene.shape.SVGPath();
    private final StackPane terrainArrowSpace = new StackPane(terrainArrow);
    private final Button terrainToggle = ActionButtons
            .disclosure(terrainArrowSpace);
    private final VBox terrainDetails = new VBox(6);
    private final VBox terrainYields = new VBox();
    private final ImageProvider imageProvider;
    private boolean terrainExpanded;

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
            final SelectedUnitManager selectedUnitManager,
            final ImageProvider imageProvider, final EventBus eventBus) {
        this.eventBus = eventBus;
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
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

        tileName = new Label();
        tileName.setId("tileName");
        terrainToggle.setId("terrainToggle");
        terrainToggle.getStyleClass().add("terrain-toggle");
        terrainToggle.setMaxWidth(Double.MAX_VALUE);
        terrainToggle.setMinWidth(0);
        terrainToggle.setWrapText(true);
        // Keep the graphic bounds stable when the arrow changes direction.
        terrainArrowSpace.setMinSize(14, 14);
        terrainArrowSpace.setPrefSize(14, 14);
        terrainArrowSpace.setMaxSize(14, 14);
        terrainArrow.setId("terrainArrow");
        terrainArrow.setFill(null);
        terrainArrow.setStroke(javafx.scene.paint.Color.BLACK);
        terrainArrow.setStrokeWidth(2);
        terrainToggle.setOnAction(event -> {
            terrainExpanded = !terrainExpanded;
            updateDisclosure();
        });
        terrainDetails.setId("terrainDetails");
        terrainYields.setId("terrainYields");
        terrainDetails.getChildren().addAll(tileName, terrainYields);
        terrainDetails.managedProperty().bind(terrainDetails.visibleProperty());
        tileFacts.setId("tileFacts");
        tileFacts.setWrapText(true);
        tileFacts.setMinHeight(Region.USE_PREF_SIZE);
        tileFacts.managedProperty().bind(tileFacts.visibleProperty());
        final javafx.scene.shape.SVGPath previewFrame = new javafx.scene.shape.SVGPath();
        previewFrame.setId("tilePreviewFrame");
        previewFrame.setContent(
                "M1 1 Q11 0 22 1 T44 1 Q45 12 44 22 T44 44 Q33 45 22 44 T1 44 Q0 33 1 22 T1 1 Z");
        previewFrame.setFill(null);
        previewFrame.setStroke(javafx.scene.paint.Color.BLACK);
        previewFrame.setStrokeWidth(2);
        previewFrame.setMouseTransparent(true);
        previewFrame.visibleProperty()
                .bind(terrainToggle.textProperty().isNotEmpty());
        tileImagePane.getChildren().add(previewFrame);

        tileName.setMinHeight(Region.USE_PREF_SIZE);
        final HBox heading = new HBox(tileImagePane, terrainToggle);
        HBox.setHgrow(terrainToggle, Priority.ALWAYS);
        final VBox summary = new VBox(8, heading, terrainDetails, tileFacts);
        summary.setId("terrainSummary");
        VBox.setMargin(tileFacts, new javafx.geometry.Insets(0, 0, 0, 55));
        gridPane.add(summary, 0, 0, 2, 1);

        gridPane.add(unitsPanel.getNode(), 0, 1, 2, 1);
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
        updateDisclosure();

    }

    void cleanView() {
        Platform.runLater(() -> {
            unitsPanel.clear();
            tileName.setText("");
            terrainToggle.setText("");
            tileFacts.setText("");
            tileFacts.setVisible(false);
            terrainYields.getChildren().clear();
            terrainToggle.setDisable(true);
            updateDisclosure();
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
                refreshTerrain(location, terrain);
                final Player player = gameModelController.getHumanPlayer();
                getModel().getNativeSettlementKnownTo(player, location)
                        .ifPresent(settlement -> {
                            sb.append("\n").append(
                                    settlement.getNation().getDisplayName());
                            if (player.isColonialNation()) {
                                sb.append("\n").append(i18n.get(
                                        Loc.nativeFavour,
                                        player.getDisplayName(),
                                        getModel().getNativeFavour(
                                                settlement.getNation(),
                                                org.akropia.model.EuropeanNation
                                                        .fromPlayerName(player
                                                                .getName()))));
                            }
                            sb.append("\n").append(getModel()
                                    .getNativeFriend(settlement.getNation())
                                    .map(friend -> i18n.get(Loc.nativeFriend,
                                            friend.getDisplayName()))
                                    .orElseGet(() -> i18n
                                            .get(Loc.nativeNoFriend)));
                        });
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
                if (!units.isEmpty()) {
                    tilePainter.paintUnit(units);
                    /**
                     * Current player is not same as human player. For purposes
                     * of this method it will be sufficient.
                     */
                    unitsPanel.setUnits(getModel().getCurrentPlayer(), units);
                }
            } else {
                tilePainter.setHiddenTerrain(location);
                terrainToggle.setText(i18n.get(Loc.unitsPanel_unexplored));
                terrainToggle.setDisable(true);
                tileName.setText(i18n.get(Loc.unitsPanel_unexplored));
                terrainYields.getChildren().clear();
            }
            tileFacts.setText(sb.toString().strip());
            tileFacts.setVisible(!tileFacts.getText().isEmpty());
            updateDisclosure();
        });
    }

    private void updateDisclosure() {
        final boolean expanded = terrainExpanded && !terrainToggle.isDisabled();
        terrainDetails.setVisible(expanded);
        terrainArrow.setContent(expanded ? "M1 1 L5 5 L9 1" : "M1 1 L5 5 L1 9");
        terrainArrow.setVisible(!terrainToggle.isDisabled());
        terrainToggle.setAccessibleHelp(
                i18n.get(expanded ? Loc.terrainCollapse : Loc.terrainExpand));
    }

    private void refreshTerrain(final Location location,
            final Terrain terrain) {
        final String name = localizationHelper
                .getTerrainName(terrain.getTerrainType());
        terrainToggle.setText(name);
        terrainToggle.setDisable(false);
        String description = movementDescription(location, terrain);
        if (!terrain.getTerrainType().isSea()) {
            description += "\n" + getModel().getLandOwner(location)
                    .map(owner -> i18n.get(Loc.landOwner,
                            owner instanceof org.akropia.model.PlayerNative
                                    ? ((org.akropia.model.PlayerNative) owner)
                                            .getNativeNation().getDisplayName()
                                    : owner.getDisplayName()))
                    .orElseGet(() -> i18n.get(Loc.landUnowned));
        }
        if (terrain.getResourceOverlay() != ResourceOverlay.NONE) {
            final String bonuses = terrain.getResourceOverlay().getOutputs()
                    .stream()
                    .map(goods -> i18n.get(Loc.terrainBonusAmount,
                            terrain.getResourceProductionBonus(goods),
                            i18n.get(GoodsTypeName.getNameForGoodsType(goods))))
                    .collect(Collectors.joining(", "));
            description += "\n" + i18n.get(Loc.terrainResourceBonus,
                    localizationHelper.getResourceOverlayName(
                            terrain.getResourceOverlay()),
                    bonuses);
        }
        tileName.setText(description);
        terrainYields.getChildren().clear();
        final Label title = new Label(i18n.get(Loc.terrainPotentialYields));
        terrainYields.getChildren().add(title);
        for (final TerrainProduction production : terrain.getProduction()) {
            final ImageView icon = new ImageView(
                    imageProvider.getProductionImage(production.getGoodsType(),
                            terrain, 45));
            icon.setFitWidth(45);
            icon.setFitHeight(45);
            final Label product = new Label(i18n.get(GoodsTypeName
                    .getNameForGoodsType(production.getGoodsType())));
            product.setWrapText(true);
            product.setMinWidth(0);
            product.setMinHeight(Region.USE_PREF_SIZE);
            final Region space = new Region();
            HBox.setHgrow(space, Priority.ALWAYS);
            final Label amount = new Label(
                    Integer.toString(production.getProduction()));
            amount.setMinWidth(Region.USE_PREF_SIZE);
            final HBox row = new HBox(6, icon, product, space, amount);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            row.setId("terrainYield-" + production.getGoodsType().name());
            row.getStyleClass().add("terrain-yield-row");
            if (production.getGoodsType() == GoodsType.GRAIN) {
                final String help = i18n.get(FoodText.source(terrain),
                        production.getProduction());
                row.setAccessibleHelp(help);
                row.setOnMouseEntered(
                        event -> eventBus.post(new StatusBarMessageEvent(help,
                                StatusBarMessageEvent.Source.GAME)));
                row.setOnMouseExited(
                        event -> eventBus.post(new StatusBarMessageEvent(
                                StatusBarMessageEvent.Source.GAME)));
            }
            terrainYields.getChildren().add(row);
        }
        if (terrain.getProduction().isEmpty()) {
            terrainYields.getChildren()
                    .add(new Label(i18n.get(Loc.terrainNoYield)));
        }
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
