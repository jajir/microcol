package org.microcol.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JLabel;

import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;

/**
 * About MicroCol game dialog.
 */
public class AboutDialog extends AbstractDialog {

	/**
	 * Default serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor when parentFrame is not available.
	 * 
	 * @param viewUtil
	 *            required tool for centering window on screen
	 * @param text
	 *            required localization helper class
	 */
	public AboutDialog(final ViewUtil viewUtil, final Text text) {
		super(viewUtil.getParentFrame());
		setTitle(text.get("aboutDialog.caption"));
		setLayout(new GridBagLayout());

		final JLabel label = new JLabel("<html>About MicroCol<br/>Simple Colonization remake.<br/>...</html>");
		add(label, new GridBagConstraints(0, 0, 1, 1, 1.0D, 1.0D, GridBagConstraints.SOUTHEAST, GridBagConstraints.NONE,
				new Insets(BORDER_BIG, BORDER_BIG, BORDER_BIG, BORDER_BIG), 0, 0));

		final JButton buttonOk = new JButton(text.get("dialog.ok"));
		buttonOk.addActionListener(e -> {
			setVisible(false);
		});
		buttonOk.requestFocus();
		add(buttonOk, new GridBagConstraints(0, 10, 1, 1, 1.0D, 1.0D, GridBagConstraints.SOUTHEAST,
				GridBagConstraints.NONE, new Insets(0, 0, 10, 10), 0, 0));

		viewUtil.showDialog(this);
	}

}
