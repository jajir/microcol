package org.microcol.gui.europe;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JLabel;

/**
 * Panels shows ships in seas. Ships are incoming to port or are going to new
 * world.
 */
public class PanelShips extends TitledPanel {

	/**
	 * Default serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	public PanelShips(final String title) {
		super(title);
		final JLabel label = new JLabel("Ships");
		add(label, new GridBagConstraints(0, 0, 1, 1, 0.0D, 0.0D, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(0, 0, 10, 10), 0, 0));
	}

}
