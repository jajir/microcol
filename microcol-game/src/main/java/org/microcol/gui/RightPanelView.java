package org.microcol.gui;

import org.microcol.gui.gamepanel.TileWasSelectedEvent;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.Localized;
import org.microcol.model.Player;

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
public class RightPanelView implements RightPanelPresenter.Display, Localized {

	private static final int RIGHT_PANEL_WIDTH = 170;

	private final ImageProvider imageProvider;

	private final ImageView tileImage;

	private final Label labelOnMove;

	private final Label tileName;

	private final Label unitsLabel;

	private final ScrollPane scrollPaneGamePanel;

	private final UnitsPanel unitsPanel;

	private final Button nextTurnButton;

	private final LocalizationHelper localizationHelper;

	private final GridPane gridPane;

	@Inject
	public RightPanelView(final ImageProvider imageProvider, final UnitsPanel unitsPanel,
			final LocalizationHelper localizationHelper) {
		this.imageProvider = Preconditions.checkNotNull(imageProvider);
		this.unitsPanel = Preconditions.checkNotNull(unitsPanel);
		this.localizationHelper = Preconditions.checkNotNull(localizationHelper);

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
		scrollPaneGamePanel = new ScrollPane(unitsPanel.getNode());
		RowConstraints scrollPaneRow = new RowConstraints();
		scrollPaneRow.setVgrow(Priority.ALWAYS);
		scrollPaneRow.fillHeightProperty().set(true);
		gridPane.getRowConstraints().addAll(new RowConstraints(), new RowConstraints(), new RowConstraints(),
				scrollPaneRow);
		gridPane.add(scrollPaneGamePanel, 0, 3, 2, 1);

		// Y=4
		nextTurnButton = new Button();
		nextTurnButton.setId("nextTurnButton");
		gridPane.add(nextTurnButton, 0, 4, 2, 1);
	}

	@Override
	public void showTile(final TileWasSelectedEvent event) {
		if (event == null) {
			return;
		}
		tileImage.setImage(imageProvider.getTerrainImage(event.getTerrain()));
		StringBuilder sb = new StringBuilder(200);
		sb.append(localizationHelper.getTerrainName(event.getTerrain()));
		sb.append("");
		sb.append("\n");
		sb.append("Move cost: 1");
		tileName.setText(sb.toString());
		unitsPanel.clear();
		if (event.getModel().getUnitsAt(event.getLocation()).isEmpty()) {
			unitsLabel.setText("");
		} else {
			// unitsLabel.setText(getText().get("unitsPanel.units"));
			/**
			 * Current player is not same as human player. For purposes of this
			 * method it will be sufficient.
			 */
			unitsPanel.setUnits(event.getModel().getCurrentPlayer(), event.getModel().getUnitsAt(event.getLocation()));
		}
	}

	@Override
	public void setOnMovePlayer(final Player player) {
		StringBuilder sb = new StringBuilder(200);
		sb.append(getText().get("unitsPanel.currentUser"));
		sb.append(" ");
		sb.append(player.getName());
		labelOnMove.setText(sb.toString());
	}

	@Override
	public Button getNextTurnButton() {
		return nextTurnButton;
	}

	@Override
	public GridPane getBox() {
		return gridPane;
	}

}
