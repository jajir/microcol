package org.microcol.gui.europe;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JLabel;

/**
 * Contains ships in port. Good from ships could be loaded/unloaded.
 */
public class PanelPortPier extends TitledPanel {

	/**
	 * Default serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	public PanelPortPier() {
		super("Pier");
		final JLabel label = new JLabel("Here will be ships to load and unload cargo");
		add(label, new GridBagConstraints(0, 0, 1, 1, 0.0D, 0.0D, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(0, 0, 10, 10), 0, 0));
	}

}
