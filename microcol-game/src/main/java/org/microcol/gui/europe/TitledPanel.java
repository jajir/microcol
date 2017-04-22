package org.microcol.gui.europe;

import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;

public class TitledPanel extends JPanel {

	/**
	 * Default serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	public TitledPanel(final String title) {
		setLayout(new GridBagLayout());
		Border border = BorderFactory.createTitledBorder(title);
		setBorder(border);
	}
}
