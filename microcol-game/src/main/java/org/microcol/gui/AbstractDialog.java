package org.microcol.gui;

import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 * Contain size definition common for all dialogs.
 */
public class AbstractDialog extends JDialog {

	/**
	 * Default serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	protected static final int BORDER = 10;

	protected static final int BORDER_BIG = 20;

	public AbstractDialog() {

	}

	public AbstractDialog(final JFrame parentFrame) {
		super(parentFrame);
	}

}
