package org.microcol.gui.europe;

import org.microcol.gui.ImageProvider;
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

	public PanelGoods(final ImageProvider imageProvider) {
		super("zbozi");
		this.imageProvider = Preconditions.checkNotNull(imageProvider);
		hBox = new HBox();
	}

	public void setEurope(final Europe europe) {
		Preconditions.checkNotNull(europe);
		getContentPane().getChildren().clear();
		GoodType.getGoodTypes().forEach(goodType -> {
			hBox.getChildren()
					.add(new PanelGood(imageProvider.getGoodTypeImage(goodType), europe.getGoodTradeForType(goodType)));
		});
		getContentPane().getChildren().add(hBox);
	}

}
