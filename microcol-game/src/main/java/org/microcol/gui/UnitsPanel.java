package org.microcol.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.microcol.gui.model.Ship;
import org.microcol.gui.model.Unit;

/**
 * Display one unit description.
 *
 */
public class UnitsPanel extends JPanel {

	/**
	 * Default serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	public UnitsPanel(final List<Unit> units, final ImageProvider imageProvider) {
		this.setLayout(new GridBagLayout());
		add(new JLabel("Units:"), new GridBagConstraints(0, 0, 2, 1, 0D, 0D, GridBagConstraints.NORTHWEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		int i = 1;
		for (final Unit u : units) {
			Ship s = (Ship) u;
			add(new JLabel(new ImageIcon(imageProvider.getImage(ImageProvider.IMG_TILE_SHIP1))),
					new GridBagConstraints(0, i, 1, 2, 0D, 0D, GridBagConstraints.NORTH, GridBagConstraints.NONE,
							new Insets(0, 0, 0, 0), 0, 0));
			add(new JLabel("Galeon"), new GridBagConstraints(1, i, 1, 1, 0D, 0D, GridBagConstraints.NORTH,
					GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			add(new JLabel("<html><div>Available moves: " + s.getAvailableSteps() + "</div></html>"),
					new GridBagConstraints(1, i + 1, 1, 1, 1D, 0D, GridBagConstraints.NORTH, GridBagConstraints.VERTICAL,
							new Insets(0, 0, 0, 0), 0, 0));
			i += 2;
		}

	}
}
