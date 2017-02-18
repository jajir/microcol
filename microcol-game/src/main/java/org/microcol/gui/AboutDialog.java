package org.microcol.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

public class AboutDialog extends JDialog {

	/**
	 * Default serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor when parentFrame is not available.
	 * 
	 * @param viewUtil
	 *            required tool for centering window on screen
	 */
	public AboutDialog(final ViewUtil viewUtil) {
		super();
		setTitle("About game");
		setLayout(new GridBagLayout());

		final JLabel text = new JLabel("<html>Version:<br/>It's funniest game in the world<br/>...</html>");
		add(text, new GridBagConstraints(0, 0, 1, 1, 1.0D, 1.0D, GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
				new Insets(0, 0, 10, 10), 0, 0));

		final JButton buttonOk = new JButton("OK");
		buttonOk.addActionListener(e -> {
			setVisible(false);
		});
		buttonOk.requestFocus();
		add(buttonOk, new GridBagConstraints(0, 10, 1, 1, 1.0D, 1.0D, GridBagConstraints.SOUTHEAST,
				GridBagConstraints.NONE, new Insets(0, 0, 10, 10), 0, 0));

		pack();
		setLocation(viewUtil.centerWindow(this));
		setModal(true);
	}

}
