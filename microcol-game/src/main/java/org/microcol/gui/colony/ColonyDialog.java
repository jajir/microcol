package org.microcol.gui.colony;

import org.microcol.gui.ImageProvider;
import org.microcol.gui.europe.PanelGoods;
import org.microcol.gui.europe.PanelPortPier;
import org.microcol.gui.event.model.GameController;
import org.microcol.gui.util.AbstractDialog;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Show Europe port.
 */
public class ColonyDialog extends AbstractDialog {

	@Inject
	public ColonyDialog(final ViewUtil viewUtil, final Text text, final ImageProvider imageProvider,
			final GameController gameController) {
		super(viewUtil);
		Preconditions.checkNotNull(imageProvider);
		Preconditions.checkNotNull(gameController);
		getDialog().setTitle(text.get("europeDialog.caption"));
		
		/**
		 * Row 1
		 */
		final Label label = new Label("Colony: First nice colony");
		/**
		 * Row 1
		 */
		final PanelColonyLayout colonyLayout = new PanelColonyLayout();

		final PanelColonyStructures colonyStructures = new PanelColonyStructures();

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
		mainPanel.getChildren().addAll(label,colonyLayout, colonyStructures, pierShips,goods,buttonOk);
		init(mainPanel);
		getScene().getStylesheets().add("gui/MicroCol.css");
		
		getDialog().showAndWait();
	}

}
