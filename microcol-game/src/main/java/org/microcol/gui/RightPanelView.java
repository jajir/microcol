package org.microcol.gui;

import java.util.List;
import java.util.Optional;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.gamepanel.TileWasSelectedEvent;
import org.microcol.gui.image.ImageLoaderButtons;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.Text;
import org.microcol.model.Colony;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Player;
import org.microcol.model.Terrain;
import org.microcol.model.TerrainType;
import org.microcol.model.Unit;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.application.Platform;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;

/**
 * Draw right panel containing info about selected tile and selected unit.
 *
 */
public final class RightPanelView implements JavaFxComponent {

    private static final int RIGHT_PANEL_WIDTH = 170;

    private final ImageProvider imageProvider;

    private final Text text;

    private final TilePainter tileImage;

    private final Label labelOnMove;

    private final Label tileName;

    private final Label unitsLabel;

    private final UnitsPanel unitsPanel;

    private final Button nextTurnButton;

    private final LocalizationHelper localizationHelper;

    private final GridPane gridPane;

    private final GameModelController gameModelController;

    @Inject
    public RightPanelView(final ImageProvider imageProvider, final Text text,
            final UnitsPanel unitsPanel, final LocalizationHelper localizationHelper,
            final GameModelController gameModelController, final TilePainter tileImage) {
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.text = Preconditions.checkNotNull(text);
        this.unitsPanel = Preconditions.checkNotNull(unitsPanel);
        this.localizationHelper = Preconditions.checkNotNull(localizationHelper);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.tileImage = Preconditions.checkNotNull(tileImage);

        gridPane = new GridPane();
        gridPane.setId("rightPanel");
        gridPane.setPrefWidth(RIGHT_PANEL_WIDTH);
        gridPane.setMinWidth(RIGHT_PANEL_WIDTH);
        gridPane.getStylesheets().add(MainStageBuilder.STYLE_SHEET_RIGHT_PANEL_VIEW);

        // Y=0
        labelOnMove = new Label();
        gridPane.add(labelOnMove, 0, 0, 2, 1);

        // Y=1
        gridPane.add(tileImage.getCanvas(), 0, 1);

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
        final BackgroundImage nextButtonImage = new BackgroundImage(
                imageProvider.getImage(ImageLoaderButtons.BUTTON_NEXT_TURN),
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                new BackgroundPosition(Side.RIGHT, 0.5, true, Side.TOP, 0.5, true),
                BackgroundSize.DEFAULT);

        nextTurnButton = new Button();
        nextTurnButton.setId("nextTurnButton");
        nextTurnButton.setBackground(new Background(nextButtonImage));
//        gridPane.add(nextTurnButton, 0, 4, 2, 1);
        //FIXME remove next turn button
    }

    public void refreshView(final TileWasSelectedEvent event) {
        refreshView(event.getLocation());
    }

    void cleanView() {
        unitsPanel.clear();
        tileName.setText("");
        tileImage.clear();
    }

    public void refreshView(final Location location) {
        Preconditions.checkNotNull(location);
        StringBuilder sb = new StringBuilder(200);
        unitsPanel.clear();
        if (isDiscovered(location)) {
            tileImage.setTerrain(getTerrainAt(location), location);
            sb.append(localizationHelper.getTerrainName(getTerrainTypeAt(location)));
            sb.append("");
            sb.append("\n");
            sb.append("Move cost: 1");
            final Optional<Colony> oColony = getModel().getColonyAt(location);
            if (oColony.isPresent()) {
                tileImage.paintColony(oColony.get());
            }
            final List<Unit> units = getModel().getUnitsAt(location);
            if (units.isEmpty()) {
                unitsLabel.setText("");
            } else {
                tileImage.paintUnit(units);
                /**
                 * Current player is not same as human player. For purposes of
                 * this method it will be sufficient.
                 */
                unitsPanel.setUnits(getModel().getCurrentPlayer(), getModel().getUnitsAt(location));
            }
        } else {
            tileImage.setImage(imageProvider.getImage(ImageProvider.IMG_TILE_HIDDEN));
            sb.append(text.get("unitsPanel.unexplored"));
        }
        tileName.setText(sb.toString());
    }

    private Model getModel() {
        return gameModelController.getModel();
    }

    private TerrainType getTerrainTypeAt(final Location location) {
        return gameModelController.getModel().getMap().getTerrainAt(location).getTerrainType();
    }

    private Terrain getTerrainAt(final Location location) {
        return gameModelController.getModel().getMap().getTerrainAt(location);
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

    public void setOnMovePlayer(final Player player) {
        StringBuilder sb = new StringBuilder(200);
        sb.append(text.get("unitsPanel.currentUser"));
        sb.append(" ");
        sb.append(player.getName());
        Platform.runLater(() -> {
            labelOnMove.setText(sb.toString());
        });
    }

    public Button getNextTurnButton() {
        return nextTurnButton;
    }

    public void setNextTurnButtonDisable(final boolean disabled) {
        nextTurnButton.setDisable(disabled);
    }

    public void setNextTurnButtonLabel(final String text) {
        nextTurnButton.setText(text);
    }

    @Override
    public GridPane getContent() {
        return gridPane;
    }

}
