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

	private final GameController gameController;

	private final PanelEuropeDock europeDock;

	private final PanelHighSeas shipsTravelingToNewWorld;

	private final PanelHighSeas shipsTravelingToEurope;

	private final PanelPortPier panelPortPier;

	@Inject
	public EuropeDialog(final ViewUtil viewUtil, final Text text, final ImageProvider imageProvider,
			final GameController gameController, final LocalizationHelper localizationHelper) {
		super(viewUtil);
		Preconditions.checkNotNull(imageProvider);
		this.gameController = Preconditions.checkNotNull(gameController);
		getDialog().setTitle(text.get("europe.title"));

		final Label label = new Label("European port");

		shipsTravelingToNewWorld = new PanelHighSeas(this, imageProvider, "Ships travelling to New World",
				gameController.getModel(), false);
		shipsTravelingToEurope = new PanelHighSeas(this, imageProvider, "Ships travelling to Europe",
				gameController.getModel(), true);
		europeDock = new PanelEuropeDock(gameController, imageProvider, this);
		europeDock.repaint();
		final VBox panelShips = new VBox();
		panelShips.getChildren().addAll(shipsTravelingToNewWorld, shipsTravelingToEurope, europeDock);

		panelPortPier = new PanelPortPier(gameController, this, "Pier", imageProvider, localizationHelper);
		panelPortPier.setEurope(gameController.getModel());

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
		panelMiddle.getChildren().addAll(panelShips, panelPortPier, panelButtons);

		final PanelGoods goods = new PanelGoods(imageProvider);
		goods.setEurope(gameController.getModel().getEurope());

		final VBox mainPanel = new VBox();
		mainPanel.getChildren().addAll(label, panelMiddle, goods);
		init(mainPanel);
		// TODO call one repaint here
		getScene().getStylesheets().add("gui/MicroCol.css");
		getDialog().showAndWait();
	}

	public void repaint() {
		europeDock.repaint();
		shipsTravelingToEurope.repaint();
		shipsTravelingToNewWorld.repaint();
		panelPortPier.setEurope(gameController.getModel());
	}

	public void repaintAfterGoodMoving() {
		europeDock.repaintCurrectShipsCrates();
		panelPortPier.setEurope(gameController.getModel());
	}

}
