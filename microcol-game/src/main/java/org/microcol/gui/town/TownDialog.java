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
			final GameController gameController,
			final PanelTownLayout panelTownLayout) {
		super(viewUtil);
		Preconditions.checkNotNull(imageProvider);
		this.gameController = Preconditions.checkNotNull(gameController);
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
		// TODO EuropeDock should be one instance in app
		europeDock = new PanelEuropeDock(viewUtil, text, gameController, imageProvider,
				new EuropeDialog(viewUtil, text, imageProvider, gameController, new LocalizationHelper(text)));

		/**
		 * Good row - 3
		 */
		goods = new PanelTownGoods(imageProvider);

		/**
		 * Last row 10
		 */
		final Button buttonOk = new Button(text.get("dialog.ok"));
		buttonOk.setOnAction(e -> {
			getDialog().close();
		});
		buttonOk.requestFocus();
		final VBox mainPanel = new VBox();
		mainPanel.getChildren().addAll(townName, colonyLayout, colonyStructures, europeDock, goods, buttonOk);
		init(mainPanel);
		getScene().getStylesheets().add("gui/MicroCol.css");
	}

	public void showTown(final Town town) {
		townName.setText("Colony: " + town.getName());
		colonyLayout.setTown(town);
		goods.setEurope(gameController.getModel().getEurope());
		// pierShips.setPort(null);
		getDialog().showAndWait();
	}

}
