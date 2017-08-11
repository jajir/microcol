package org.microcol.gui.europe;

import org.microcol.gui.ImageProvider;
import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.event.model.GameController;
import org.microcol.gui.util.AbstractDialog;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Show Europe port.
 */
public class EuropeDialog extends AbstractDialog {

	@Inject
	public EuropeDialog(final ViewUtil viewUtil, final Text text, final ImageProvider imageProvider,
			final GameController gameController, final LocalizationHelper localizationHelper) {
		super(viewUtil);
		Preconditions.checkNotNull(imageProvider);
		Preconditions.checkNotNull(gameController);
		getDialog().setTitle(text.get("europe.title"));

		final Label label = new Label("European port");

		final PanelShips outgoingShips = new PanelShips(imageProvider, "Ships travelling to New World",
				gameController.getModel(), false);
		final PanelShips incomingShips = new PanelShips(imageProvider, "Ships travelling to Europe",
				gameController.getModel(), true);
		final PanelPortPier pierShips = new PanelPortPier(imageProvider);
		pierShips.setPort(gameController, gameController.getModel().getEurope().getPort());
		final VBox panelShips = new VBox();
		panelShips.getChildren().addAll(outgoingShips, incomingShips, pierShips);

		final PanelRecruits panelRecruits = new PanelRecruits("Recruits", imageProvider, localizationHelper);

		final Button recruiteButton = new Button("Recruite");
		final Button buyButton = new Button("Buy");
		final Button buttonOk = new Button(text.get("dialog.ok"));
		buttonOk.setOnAction(e -> {
			getDialog().close();
		});
		buttonOk.requestFocus();
		final VBox panelButtons = new VBox();
		panelButtons.getChildren().addAll(recruiteButton, buyButton, buttonOk);

		final HBox panelMiddle = new HBox();
		panelMiddle.getChildren().addAll(panelShips, panelRecruits, panelButtons);

		final PanelGoods goods = new PanelGoods(imageProvider);
		goods.setEurope(gameController.getModel().getEurope());

		final VBox mainPanel = new VBox();
		mainPanel.getChildren().addAll(label, panelMiddle, goods);
		init(mainPanel);
		getScene().getStylesheets().add("gui/MicroCol.css");
		getDialog().showAndWait();
	}

}
