package org.microcol.gui.screen.game.components;

import java.util.List;
import java.util.Optional;

import org.microcol.gui.Loc;
import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.MainStageBuilder;
import org.microcol.gui.UnitsPanel;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageLoaderTerrain;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.game.gamepanel.TileWasSelectedEvent;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.i18n.I18n;
import org.microcol.model.Colony;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Player;
import org.microcol.model.Terrain;
import org.microcol.model.Unit;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;

/**
 * Draw right panel containing info about selected tile and selected unit.
 *
 */
public final class RightPanelView implements JavaFxComponent {

    private final ImageProvider imageProvider;

    private final I18n i18n;

    private final TilePainter tilePainter;

    private final Label labelOnMove;

    private final Label tileName;

    private final Label unitsLabel;

    private final UnitsPanel unitsPanel;

    private final LocalizationHelper localizationHelper;

    private final GridPane gridPane;

    private final GameModelController gameModelController;

    @Inject
    public RightPanelView(final ImageProvider imageProvider, final I18n i18n,
            final UnitsPanel unitsPanel, final LocalizationHelper localizationHelper,
            final GameModelController gameModelController, final TilePainter tilePainter) {
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.i18n = Preconditions.checkNotNull(i18n);
        this.unitsPanel = Preconditions.checkNotNull(unitsPanel);
        this.localizationHelper = Preconditions.checkNotNull(localizationHelper);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.tilePainter = Preconditions.checkNotNull(tilePainter);

        gridPane = new GridPane();
        gridPane.setId("rightPanel");
        gridPane.getStylesheets().add(MainStageBuilder.STYLE_SHEET_RIGHT_PANEL);

        // Y=0
        labelOnMove = new Label();
        gridPane.add(labelOnMove, 0, 0, 2, 1);

        // Y=1
        final Pane tileImagePane = new Pane(tilePainter.getCanvas());
        tileImagePane.setId("tileImage");
        gridPane.add(tileImagePane, 0, 1);

        tileName = new Label();
        gridPane.add(tileName, 1, 1);

        // Y=2
        unitsLabel = new Label();
        gridPane.add(unitsLabel, 0, 2);

        // Y=3

        final ScrollPane scrollPaneGamePanel = new ScrollPane(unitsPanel.getNode());
        RowConstraints scrollPaneRow = new RowConstraints();
        scrollPaneRow.setVgrow(Priority.ALWAYS);
        scrollPaneRow.fillHeightProperty().set(true);
        gridPane.getRowConstraints().addAll(new RowConstraints(), new RowConstraints(),
                new RowConstraints(), scrollPaneRow);
        gridPane.add(scrollPaneGamePanel, 0, 3, 2, 1);

        // Y=4
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
                sb.append(localizationHelper.getTerrainName(terrain.getTerrainType()));
                sb.append("");
                sb.append("\n");
                sb.append(i18n.get(Loc.moveCost) + terrain.getMoveCost());
                final Optional<Colony> oColony = getModel().getColonyAt(location);
                if (oColony.isPresent()) {
                    tilePainter.paintColony(oColony.get());
                }
                final List<Unit> units = getModel().getUnitsAt(location);
                if (units.isEmpty()) {
                    unitsLabel.setText("");
                } else {
                    tilePainter.paintUnit(units);
                    /**
                     * Current player is not same as human player. For purposes
                     * of this method it will be sufficient.
                     */
                    unitsPanel.setUnits(getModel().getCurrentPlayer(),
                            getModel().getUnitsAt(location));
                }
            } else {
                tilePainter.setImage(imageProvider.getImage(ImageLoaderTerrain.IMG_TILE_HIDDEN));
                sb.append(i18n.get(Loc.unitsPanel_unexplored));
            }
            tileName.setText(sb.toString());
        });
    }

    public void setOnMovePlayer(final Player player) {
        StringBuilder sb = new StringBuilder(200);
        sb.append(i18n.get(Loc.unitsPanel_currentUser));
        sb.append(" ");
        sb.append(player.getName());
        Platform.runLater(() -> {
            labelOnMove.setText(sb.toString());
        });
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
     * @param location
     *            required location
     * @return return <code>true</code> when selected location was already
     *         discovered otherwise return <code>false</code>.
     */
    private boolean isDiscovered(final Location location) {
        return gameModelController.getHumanPlayer().isVisible(location);
    }

    @Override
    public GridPane getContent() {
        return gridPane;
    }

}
