package org.microcol.gui.europe;

import org.microcol.gui.ImageProvider;

import javafx.scene.control.Label;

/**
 * Show list of all available goods.
 */
public class PanelGoods extends TitledPanel {

	public PanelGoods(final ImageProvider imageProvider) {
		super("zbozi", new Label("zbozi"));
		//FIXME JJ NYI
//		final PanelGood pgGrain = new PanelGood(imageProvider.getImage(ImageProvider.IMG_GOOD_CORN), 1, 8);
//		add(pgGrain, new GridBagConstraints(0, 0, 1, 1, 0.0D, 0.0D, GridBagConstraints.CENTER, GridBagConstraints.NONE,
//				new Insets(0, 0, 0, 0), 0, 0));
//
//		final PanelGood pgSugar = new PanelGood(imageProvider.getImage(ImageProvider.IMG_GOOD_SUGAR), 3, 5);
//		add(pgSugar, new GridBagConstraints(1, 0, 1, 1, 0.0D, 0.0D, GridBagConstraints.CENTER, GridBagConstraints.NONE,
//				new Insets(0, 0, 0, 0), 0, 0));
//
//		final PanelGood pgTobacco = new PanelGood(imageProvider.getImage(ImageProvider.IMG_GOOD_TOBACCO), 3, 6);
//		add(pgTobacco, new GridBagConstraints(2, 0, 1, 1, 0.0D, 0.0D, GridBagConstraints.CENTER,
//				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
	}

}
