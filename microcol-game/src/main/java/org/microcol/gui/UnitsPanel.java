package org.microcol.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.microcol.gui.event.StatusBarMessageController;
import org.microcol.gui.model.Ship;
import org.microcol.gui.model.Unit;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Display one unit description.
 *
 */
public class UnitsPanel extends JPanel implements Localized {

	/**
	 * Default serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	private final ImageProvider imageProvider;
	
	@Inject
	public UnitsPanel(final ImageProvider imageProvider, final StatusBarMessageController statusBarMessageController) {
		this.imageProvider = Preconditions.checkNotNull(imageProvider);
		this.setLayout(new GridBagLayout());
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(final MouseEvent e) {
				statusBarMessageController.fireStatusMessageWasChangedEvent(getText().get("unitsPanel.description"));
			}
		});
	}

	public void clear() {
		removeAll();
	}

	public void setUnits(final List<Unit> units) {
		add(new JLabel(getText().get("unitsPanel.units")), new GridBagConstraints(0, 0, 2, 1, 0D, 0D, GridBagConstraints.NORTHWEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		int i = 1;
		for (final Unit u : units) {
			Ship s = (Ship) u;
			add(new JLabel(new ImageIcon(imageProvider.getImage(ImageProvider.IMG_TILE_SHIP1))),
					new GridBagConstraints(0, i, 1, 2, 0D, 0D, GridBagConstraints.NORTH, GridBagConstraints.NONE,
							new Insets(0, 0, 0, 0), 0, 0));
			add(new JLabel("Galeon"), new GridBagConstraints(1, i, 1, 1, 0D, 0D, GridBagConstraints.NORTH,
					GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			add(new JLabel("<html><div>" + getText().get("unitsPanel.availableMoved") + s.getAvailableSteps() + "</div></html>"),
					new GridBagConstraints(1, i + 1, 1, 1, 1D, 0D, GridBagConstraints.NORTH,
							GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
			i += 2;
		}
		validate();
	}

}
