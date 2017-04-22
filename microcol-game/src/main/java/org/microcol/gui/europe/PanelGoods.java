package org.microcol.gui.europe;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JLabel;

/**
 * Show list of all available goods.
 */
public class PanelGoods extends TitledPanel {

	/**
	 * Default serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	public PanelGoods() {
		super("Good buy & sell");
		final JLabel label = new JLabel("Goods");
		add(label, new GridBagConstraints(0, 0, 1, 1, 0.0D, 0.0D, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(0, 0, 10, 10), 0, 0));
	}

}
