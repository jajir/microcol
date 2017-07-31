package org.microcol.gui.town;

import org.microcol.gui.ImageProvider;
import org.microcol.gui.europe.PanelGoods;
import org.microcol.gui.europe.PanelPortPier;
import org.microcol.gui.event.model.GameController;
import org.microcol.gui.panelview.PaintService;
import org.microcol.gui.util.AbstractDialog;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;
import org.microcol.model.Town;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Show Europe port.
 */
public class TownDialog extends AbstractDialog {

	private Town town;

	private final Label townName;
	
	private final PanelTownLayout colonyLayout ;
	
	private final PanelTownStructures colonyStructures;

	@Inject
	public TownDialog(final ViewUtil viewUtil, final Text text, final ImageProvider imageProvider,
			final GameController gameController, final PaintService paintService, final PanelTownLayout panelTownLayout) {
		super(viewUtil);
		Preconditions.checkNotNull(imageProvider);
		Preconditions.checkNotNull(gameController);
		getDialog().setTitle(text.get("europeDialog.caption"));

		/**
		 * Row 1
		 */
		townName = new Label("Colony: ");
		/**
		 * Row 1
		 */
		colonyLayout = Preconditions.checkNotNull(panelTownLayout);

		colonyStructures = new PanelTownStructures();

		/**
		 * Row 2
		 */
		final PanelPortPier pierShips = new PanelPortPier(imageProvider);
		/**
		 * Good row - 3
		 */
		final PanelGoods goods = new PanelGoods(imageProvider);

		/**
		 * Last row 10
		 */
		final Button buttonOk = new Button(text.get("dialog.ok"));
		buttonOk.setOnAction(e -> {
			getDialog().close();
		});
		buttonOk.requestFocus();
		final VBox mainPanel = new VBox();
		mainPanel.getChildren().addAll(townName, colonyLayout, colonyStructures, pierShips, goods, buttonOk);
		init(mainPanel);
		getScene().getStylesheets().add("gui/MicroCol.css");
	}

	public void showTown(final Town town) {
		this.town = Preconditions.checkNotNull(town);
		townName.setText("Colony: " + town.getName());
		colonyLayout.setTown(town);
		getDialog().showAndWait();
	}

}
