package org.microcol.gui.town;

import org.microcol.gui.ImageProvider;
import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.europe.EuropeDialog;
import org.microcol.gui.europe.PanelEuropeDock;
import org.microcol.gui.event.model.GameController;
import org.microcol.gui.util.AbstractDialog;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;
import org.microcol.model.Town;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Show Europe port.
 */
public class TownDialog extends AbstractDialog {

	private final Label townName;

	private final PanelTownLayout colonyLayout;

	private final PanelTownStructures colonyStructures;

	private final PanelTownGoods goods;

	private final GameController gameController;

	private final PanelEuropeDock europeDock;

	@Inject
	public TownDialog(final ViewUtil viewUtil, final Text text, final ImageProvider imageProvider,
			final GameController gameController, final LocalizationHelper localizationHelper,
			final PanelTownLayout panelTownLayout) {
		super(viewUtil);
		Preconditions.checkNotNull(imageProvider);
		this.gameController = Preconditions.checkNotNull(gameController);
		getDialog().setTitle(text.get("europeDialog.caption"));

		/**
		 * Row 0
		 */
		townName = new Label("Colony: ");

		/**
		 * Row 1
		 */
		colonyLayout = Preconditions.checkNotNull(panelTownLayout);

		colonyStructures = new PanelTownStructures(localizationHelper, imageProvider);

		final HBox mapAndBuildings = new HBox();
		mapAndBuildings.getChildren().addAll(colonyStructures, colonyLayout);

		/**
		 * Row 2
		 */
		final PanelProductionSummary panelProductionSummary = new PanelProductionSummary();

		europeDock = new PanelEuropeDock(viewUtil, text, gameController, imageProvider,
				new EuropeDialog(viewUtil, text, imageProvider, gameController, new LocalizationHelper(text)));

		final PanelOutsideColony panelOutsideColony = new PanelOutsideColony();

		final HBox managementRow = new HBox();
		managementRow.getChildren().addAll(panelProductionSummary, europeDock, panelOutsideColony);

		/**
		 * Good row - 3
		 */
		goods = new PanelTownGoods(imageProvider);

		/**
		 * Last row 4
		 */
		final Button buttonOk = new Button(text.get("dialog.ok"));
		buttonOk.setOnAction(e -> {
			getDialog().close();
		});
		buttonOk.requestFocus();

		final VBox mainPanel = new VBox();
		mainPanel.getChildren().addAll(townName, mapAndBuildings, managementRow, goods, buttonOk);
		init(mainPanel);
		getScene().getStylesheets().add("gui/MicroCol.css");
	}

	public void showTown(final Town town) {
		townName.setText("Colony: " + town.getName());
		colonyLayout.setTown(town);
		goods.setEurope(gameController.getModel().getEurope());
		colonyStructures.repaint(town);
		// pierShips.setPort(null);
		getDialog().showAndWait();
	}

}
