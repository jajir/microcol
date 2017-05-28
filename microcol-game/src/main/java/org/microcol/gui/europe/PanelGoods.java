package org.microcol.gui.europe;

import org.microcol.gui.ImageProvider;

import javafx.scene.layout.HBox;

/**
 * Show list of all available goods.
 */
public class PanelGoods extends TitledPanel {

	public PanelGoods(final ImageProvider imageProvider) {
		super("zbozi");
		HBox hBox = new HBox();
		hBox.getChildren().addAll(

				new PanelGood(imageProvider.getImage(ImageProvider.IMG_GOOD_CORN), 1, 8),
				new PanelGood(imageProvider.getImage(ImageProvider.IMG_GOOD_SUGAR), 3, 5),

				new PanelGood(imageProvider.getImage(ImageProvider.IMG_GOOD_TOBACCO), 1, 8),
				new PanelGood(imageProvider.getImage(ImageProvider.IMG_GOOD_COTTON), 1, 8),
				new PanelGood(imageProvider.getImage(ImageProvider.IMG_GOOD_FUR), 1, 8),
				new PanelGood(imageProvider.getImage(ImageProvider.IMG_GOOD_LUMBER), 1, 8),
				new PanelGood(imageProvider.getImage(ImageProvider.IMG_GOOD_ORE), 1, 8),
				new PanelGood(imageProvider.getImage(ImageProvider.IMG_GOOD_SILVER), 1, 8),
				new PanelGood(imageProvider.getImage(ImageProvider.IMG_GOOD_HORSE), 1, 8),
				new PanelGood(imageProvider.getImage(ImageProvider.IMG_GOOD_RUM), 1, 8),
				new PanelGood(imageProvider.getImage(ImageProvider.IMG_GOOD_CIGARS), 1, 8),
				new PanelGood(imageProvider.getImage(ImageProvider.IMG_GOOD_SILK), 1, 8),
				new PanelGood(imageProvider.getImage(ImageProvider.IMG_GOOD_COAT), 1, 8),
				new PanelGood(imageProvider.getImage(ImageProvider.IMG_GOOD_GOODS), 1, 8),
				new PanelGood(imageProvider.getImage(ImageProvider.IMG_GOOD_TOOLS), 1, 8),
				new PanelGood(imageProvider.getImage(ImageProvider.IMG_GOOD_MUSKET), 1, 8)

		);
		getContentPane().getChildren().add(hBox);
	}

}
