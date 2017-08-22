package org.microcol.gui.europe;

import org.microcol.gui.ImageProvider;
import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.event.model.GameController;
import org.microcol.gui.util.AbstractDialog;
import org.microcol.gui.util.ButtonsBar;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;
import org.microcol.model.UnitType;

import com.google.common.base.Preconditions;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Show Europe port.
 */
public class RecruiteUnitsDialog extends AbstractDialog {

	public RecruiteUnitsDialog(final ViewUtil viewUtil, final Text text, final ImageProvider imageProvider,
			final GameController gameController, final LocalizationHelper localizationHelper) {
		super(viewUtil);
		Preconditions.checkNotNull(imageProvider);
		Preconditions.checkNotNull(gameController);
		getDialog().setTitle(text.get("recruitUnitDialog.title"));
		final Label labelCaption = new Label(text.get("recruitUnitDialog.title"));

		final VBox root = new VBox();
		root.setId("mainVbox");
		init(root);

		final HBox gridWithUnits = new HBox();

		gameController.getModel();
		//TODO unit to recruit should be here from some 
		UnitType.UNIT_TYPES.stream().filter(unitType -> unitType.getEuropePrice() > 0).forEach(unitType -> {
			final RecruiteUnitPanel buyUnitPanel = new RecruiteUnitPanel(unitType, viewUtil, imageProvider,
					gameController, localizationHelper, text, this);
			HBox.setMargin(buyUnitPanel, new Insets(10, 10, 10, 10));
			gridWithUnits.getChildren().add(buyUnitPanel);
		});

		final ButtonsBar buttonBar = new ButtonsBar(text);
		buttonBar.getButtonOk().setOnAction(e -> {
			getDialog().close();
		});

		root.getChildren().addAll(labelCaption, gridWithUnits, buttonBar);

		getDialog().showAndWait();
	}

}
