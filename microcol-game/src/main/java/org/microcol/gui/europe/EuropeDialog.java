package org.microcol.gui.europe;

import org.microcol.gui.ImageProvider;
import org.microcol.gui.event.model.GameController;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Show Europe port.
 */
public class EuropeDialog {

	private final Stage dialog;

	@Inject
	public EuropeDialog(final ViewUtil viewUtil, final Text text, final ImageProvider imageProvider,
			final GameController gameController) {
		Preconditions.checkNotNull(imageProvider);
		Preconditions.checkNotNull(gameController);
		dialog = new Stage();
		dialog.initModality(Modality.APPLICATION_MODAL);
		dialog.initOwner(viewUtil.getParentFrame());
		dialog.setTitle(text.get("europe.title"));

		final Label label = new Label("European port");

		final PanelShips outgoingShips = new PanelShips("Ships travelling to New World");
		final PanelShips incomingShips = new PanelShips("Ships travelling to Europe");
		final PanelPortPier pierShips = new PanelPortPier();
		final VBox panelSips = new VBox();
		panelSips.getChildren().addAll(outgoingShips, incomingShips, pierShips);

		final Button recruiteButton = new Button("Recruite");
		final Button buyButton = new Button("Buy");
		final Button buttonOk = new Button(text.get("dialog.ok"));
		buttonOk.setOnAction(e -> {
			dialog.close();
		});
		buttonOk.requestFocus();
		final VBox panelButtons = new VBox();
		panelButtons.getChildren().addAll(recruiteButton, buyButton, buttonOk);

		final HBox panelMiddle = new HBox();
		panelMiddle.getChildren().addAll(panelSips, panelButtons);

		final PanelGoods goods = new PanelGoods(imageProvider);

		VBox mainPanel = new VBox();
		mainPanel.getChildren().addAll(label, panelMiddle, goods);
		Scene scene = new Scene(mainPanel);
		scene.getStylesheets().add("gui/MicroCol.css");
		dialog.setScene(scene);
		dialog.showAndWait();
	}

}
