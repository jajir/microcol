package org.microcol.gui.colony;

import org.microcol.gui.ImageProvider;
import org.microcol.gui.util.TitledPanel;
import org.microcol.model.Europe;
import org.microcol.model.GoodType;

import com.google.common.base.Preconditions;

import javafx.scene.layout.HBox;

/**
 * Show list of all available goods.
 */
public class PanelColonyGoods extends TitledPanel {

	private final HBox hBox;

	private final ImageProvider imageProvider;

	public PanelColonyGoods(final ImageProvider imageProvider) {
		super("zbozi");
		this.imageProvider = Preconditions.checkNotNull(imageProvider);
		hBox = new HBox();
		getContentPane().getChildren().add(hBox);
	}

	public void setEurope(final Europe europe) {
		Preconditions.checkNotNull(europe);
		hBox.getChildren().clear();
		GoodType.BUYABLE_GOOD_TYPES.forEach(goodType -> {
			hBox.getChildren().add(
					new PanelColonyGood(imageProvider.getGoodTypeImage(goodType), europe.getGoodTradeForType(goodType)));
		});
	}

}
