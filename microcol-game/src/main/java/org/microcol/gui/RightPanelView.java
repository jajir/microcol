package org.microcol.gui;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.gamepanel.TileWasSelectedEvent;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.Text;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Player;
import org.microcol.model.TerrainType;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;

/**
 * Draw right panel containing info about selected tile and selected unit.
 *
 */
public class RightPanelView {

    private static final int RIGHT_PANEL_WIDTH = 170;

    private final ImageProvider imageProvider;

    private final Text text;

    private final ImageView tileImage;

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
            final GameModelController gameModelController) {
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.text = Preconditions.checkNotNull(text);
        this.unitsPanel = Preconditions.checkNotNull(unitsPanel);
        this.localizationHelper = Preconditions.checkNotNull(localizationHelper);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);

        gridPane = new GridPane();
        gridPane.setId("rightPanel");
        gridPane.setPrefWidth(RIGHT_PANEL_WIDTH);
        gridPane.setMinWidth(RIGHT_PANEL_WIDTH);
        gridPane.getStylesheets().add("gui/rightPanelView.css");

        // Y=0
        labelOnMove = new Label();
        gridPane.add(labelOnMove, 0, 0, 2, 1);

        // Y=1
        tileImage = new ImageView();
        gridPane.add(tileImage, 0, 1);

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
        nextTurnButton = new Button();
        nextTurnButton.setId("nextTurnButton");
        gridPane.add(nextTurnButton, 0, 4, 2, 1);
    }

    public void refreshView(final TileWasSelectedEvent event) {
        refreshView(event.getLocation());
    }

    void cleanView() {
        unitsPanel.clear();
        tileName.setText("");
        tileImage.setImage(null);
    }

    public void refreshView(final Location location) {
        Preconditions.checkNotNull(location);
        StringBuilder sb = new StringBuilder(200);
        unitsPanel.clear();
        if (isDiscovered(location)) {
            tileImage.setImage(imageProvider.getTerrainImage(getTerrainTypeAt(location)));
            sb.append(localizationHelper.getTerrainName(getTerrainTypeAt(location)));
            sb.append("");
            sb.append("\n");
            sb.append("Move cost: 1");
            if (getModel().getUnitsAt(location).isEmpty()) {
                unitsLabel.setText("");
            } else {
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

    /**
     * If user already explored selected tile.
     * 
     * @param location
     *            required locaton
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
        labelOnMove.setText(sb.toString());
    }

    public Button getNextTurnButton() {
        return nextTurnButton;
    }

    public GridPane getBox() {
        return gridPane;
    }

}
