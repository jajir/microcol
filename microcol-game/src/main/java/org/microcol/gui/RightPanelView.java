package org.microcol.gui;

import org.microcol.gui.event.FocusedTileEvent;
import org.microcol.gui.util.Localized;
import org.microcol.model.Player;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

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

	private final GridPane box;

	@Inject
	public RightPanelView(final ImageProvider imageProvider, final UnitsPanel unitsPanel,
			final LocalizationHelper localizationHelper) {
		this.imageProvider = Preconditions.checkNotNull(imageProvider);
		this.unitsPanel = Preconditions.checkNotNull(unitsPanel);
		this.localizationHelper = Preconditions.checkNotNull(localizationHelper);

		box = new GridPane();

		// Y=0
		labelOnMove = new Label();

		// Y=1
		tileImage = new ImageView();
		box.add(new Label("", tileImage),0,1);

		tileName = new Label();
		box.add(tileName,1,1);

		// Y=2
		unitsLabel = new Label();
		box.add(unitsLabel, 0,2);

		// Y=3
		scrollPaneGamePanel = new ScrollPane(unitsPanel.getNode());
		scrollPaneGamePanel.setVbarPolicy(ScrollBarPolicy.ALWAYS);
		scrollPaneGamePanel.setHbarPolicy(ScrollBarPolicy.ALWAYS);
//		scrollPaneGamePanel.setBorder(BorderFactory.createEmptyBorder());
		box.add(scrollPaneGamePanel,0,3);

		// Y=10
		nextTurnButton = new Button();
		nextTurnButton.setId("nextTurnButton");
		box.add(nextTurnButton,0,10);
//		setPreferredSize(new Dimension(RIGHT_PANEL_WIDTH, 200));
//		setMinimumSize(new Dimension(RIGHT_PANEL_WIDTH, 200));
		
//		unitsPanel.setMaximumSize(scrollPaneGamePanel.getViewport().getExtentSize());
//		unitsPanel.setPreferredSize(scrollPaneGamePanel.getViewport().getExtentSize());
	}

	@Override
	public void showTile(final FocusedTileEvent event) {
		if (event == null) {
			return;
		}
		tileImage.setImage(imageProvider.getTerrainImage(event.getTerrain()));
		StringBuilder sb = new StringBuilder(200);
		sb.append("<html><div>");
		sb.append(localizationHelper.getTerrainName(event.getTerrain()));
		sb.append("");
		sb.append("</div><div>");
		sb.append("Move cost: 1");
		sb.append("</div></html>");
		tileName.setText(sb.toString());
		unitsPanel.clear();
		if (event.getModel().getUnitsAt(event.getLocation()).isEmpty()) {
			unitsLabel.setText("");
		} else {
			unitsLabel.setText(getText().get("unitsPanel.units"));
			unitsPanel.setUnits(event.getModel().getUnitsAt(event.getLocation()));
		}
	}

	@Override
	public void setOnMovePlayer(final Player player) {
		StringBuilder sb = new StringBuilder(200);
		sb.append("<html><div>");
		sb.append(getText().get("unitsPanel.currentUser"));
		sb.append(" ");
		sb.append(player.getName());
		sb.append("</div></html>");
		labelOnMove.setText(sb.toString());
	}

	@Override
	public Button getNextTurnButton() {
		return nextTurnButton;
	}

	@Override
	public GridPane getBox() {
		return box;
	}

}
