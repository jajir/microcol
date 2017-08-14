package org.microcol.gui.europe;

import org.microcol.gui.ImageProvider;
import org.microcol.gui.event.model.GameController;
import org.microcol.model.Europe;
import org.microcol.model.GoodType;

import com.google.common.base.Preconditions;

import javafx.scene.layout.HBox;

/**
 * Show list of all available goods.
 */
public class PanelGoods extends TitledPanel {

	private final HBox hBox;

	private final ImageProvider imageProvider;

	private final GameController gameController;

	public PanelGoods(final GameController gameController, final ImageProvider imageProvider) {
		super("zbozi");
		this.gameController = Preconditions.checkNotNull(gameController);
		this.imageProvider = Preconditions.checkNotNull(imageProvider);
		hBox = new HBox();
	}

	public void repaint() {
		final Europe europe = gameController.getModel().getEurope();
		getContentPane().getChildren().clear();
		GoodType.getGoodTypes().forEach(goodType -> {
			hBox.getChildren()
					.add(new PanelGood(imageProvider.getGoodTypeImage(goodType), europe.getGoodTradeForType(goodType)));
		});
		getContentPane().getChildren().add(hBox);
	}

}
