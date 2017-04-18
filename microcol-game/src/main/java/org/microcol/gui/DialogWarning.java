package org.microcol.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

public class DialogWarning extends AbstractDialog {

	/**
	 * Default serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor.
	 * 
	 *FIXME add required parameters. 
	 */
	public DialogWarning() {
		super();
		setUndecorated(true);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setTitle("Tato jednotka neumi bojovat");
		setLayout(new GridBagLayout());

		final JLabel label = new JLabel("Tato jednotka neumi bojovat");
		add(label, new GridBagConstraints(0, 0, 1, 1, 1.0D, 1.0D, GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE,
				new Insets(BORDER, BORDER_BIG, BORDER_BIG, BORDER_BIG), 0, 0));

		/**
		 * Buttons
		 */
		final JButton buttonFight = new JButton("Ok");
		buttonFight.addActionListener(e -> {
			setVisible(false);
			dispose();
		});
		add(buttonFight, new GridBagConstraints(2, 10, 1, 1, 0.0D, 0.0D, GridBagConstraints.SOUTHEAST,
				GridBagConstraints.NONE, new Insets(BORDER_BIG, BORDER, BORDER, BORDER), 0, 0));

		setResizable(false);
		pack();
		setLocationRelativeTo(null);
		setModal(true);
		buttonFight.requestFocus();
	}
}
