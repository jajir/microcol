package org.microcol.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.microcol.gui.model.Tile;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Draw right panel containing info about selected tile and selected unit.
 *
 */
public class RightPanelView extends JPanel implements RightPanelPresenter.Display {

	/**
	 * Default serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	private final static int RIGHT_PANEL_WIDTH = 150;

	private final ImageProvider imageProvider;

	private final ImageIcon tileImage;

	private final JLabel tileName;

	private final JLabel tileDescription;

	private final JScrollPane scrollPaneGamePanel;

	private final UnitsPanel unitsPanel;

	private final JButton nextTurnButton;

	@Inject
	public RightPanelView(final ImageProvider imageProvider, final UnitsPanel unitsPanel) {
		this.imageProvider = Preconditions.checkNotNull(imageProvider);
		this.unitsPanel = Preconditions.checkNotNull(unitsPanel);
		this.setLayout(new GridBagLayout());

		// Y=0
		tileImage = new ImageIcon();
		add(new JLabel(tileImage), new GridBagConstraints(0, 0, 1, 1, 0D, 0D, GridBagConstraints.NORTH,
				GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
		tileName = new JLabel();
		add(tileName, new GridBagConstraints(1, 0, 1, 1, 0D, 0D, GridBagConstraints.NORTH, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));

		// Y=1
		tileDescription = new JLabel();
		add(tileDescription, new GridBagConstraints(0, 1, 2, 1, 1D, 0D, GridBagConstraints.NORTH,
				GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		// Y=2
		scrollPaneGamePanel = new JScrollPane(unitsPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(scrollPaneGamePanel, new GridBagConstraints(0, 2, 2, 1, 1D, 1D, GridBagConstraints.NORTH,
				GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		// Y=10
		nextTurnButton = new JButton();
		add(nextTurnButton, new GridBagConstraints(0, 10, 2, 1, 0D, 0D, GridBagConstraints.SOUTH,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		setPreferredSize(new Dimension(RIGHT_PANEL_WIDTH, 200));
		setMinimumSize(new Dimension(RIGHT_PANEL_WIDTH, 200));
		validate();
		unitsPanel.setMaximumSize(scrollPaneGamePanel.getViewport().getExtentSize());
		unitsPanel.setPreferredSize(scrollPaneGamePanel.getViewport().getExtentSize());
	}

	@Override
	public void showTile(final Tile tile) {
		System.out.println(tile);
		tileImage.setImage(imageProvider.getImage(ImageProvider.IMG_TILE_OCEAN));
		tileName.setText(tile.getName());
		tileDescription.setText("<html><div>" + tile.getDescription() + "</div></html>");
		if (tile.getUnits().isEmpty()) {
			unitsPanel.clear();
			unitsPanel.repaint();
		} else {
			unitsPanel.setUnits(tile.getUnits());
		}

	}

	@Override
	public JButton getNextTurnButton() {
		return nextTurnButton;
	}

	@Override
	public JPanel getRightPanel() {
		return this;
	}

}
