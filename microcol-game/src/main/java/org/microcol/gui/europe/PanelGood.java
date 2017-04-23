package org.microcol.gui.europe;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Contains image of of type of good.
 */
public class PanelGood extends JPanel {

	/**
	 * Default serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	public PanelGood(final Image image, final int sellPrice, final int buyPrice) {
		setLayout(new GridBagLayout());
		final ImageIcon imageIcon = new ImageIcon(image);
		final JLabel labelImage = new JLabel(imageIcon);
		add(labelImage, new GridBagConstraints(0, 0, 1, 1, 0.0D, 0.0D, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

		final JLabel labelPrice = new JLabel(sellPrice + "/" + buyPrice);
		add(labelPrice, new GridBagConstraints(0, 1, 1, 1, 0.0D, 0.0D, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
	}

}
