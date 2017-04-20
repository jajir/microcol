package org.microcol.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.microcol.gui.event.FocusedTileEvent;
import org.microcol.gui.util.Localized;
import org.microcol.model.Player;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Draw right panel containing info about selected tile and selected unit.
 *
 */
public class RightPanelView extends JPanel implements RightPanelPresenter.Display, Localized {

	/**
	 * Default serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	private static final int RIGHT_PANEL_WIDTH = 170;

	private final ImageProvider imageProvider;

	private final ImageIcon tileImage;

	private final JLabel labelOnMove;

	private final JLabel tileName;

	private final JLabel unitsLabel;

	private final JScrollPane scrollPaneGamePanel;

	private final UnitsPanel unitsPanel;

	private final JButton nextTurnButton;

	private final LocalizationHelper localizationHelper;

	@Inject
	public RightPanelView(final ImageProvider imageProvider, final UnitsPanel unitsPanel,
			final LocalizationHelper localizationHelper) {
		this.imageProvider = Preconditions.checkNotNull(imageProvider);
		this.unitsPanel = Preconditions.checkNotNull(unitsPanel);
		this.localizationHelper = Preconditions.checkNotNull(localizationHelper);
		this.setLayout(new GridBagLayout());

		// Y=0
		labelOnMove = new JLabel();
		add(labelOnMove, new GridBagConstraints(0, 0, 2, 1, 0D, 0D, GridBagConstraints.NORTH, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));

		// Y=1
		tileImage = new ImageIcon();
		add(new JLabel(tileImage), new GridBagConstraints(0, 1, 1, 1, 0D, 0D, GridBagConstraints.NORTHWEST,
				GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
		tileName = new JLabel();
		add(tileName, new GridBagConstraints(1, 1, 1, 1, 0D, 0D, GridBagConstraints.NORTH, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));

		// Y=2
		unitsLabel = new JLabel();
		add(unitsLabel, new GridBagConstraints(0, 2, 2, 1, 1D, 0D, GridBagConstraints.NORTHWEST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 0, 5, 0), 0, 0));

		// Y=3
		scrollPaneGamePanel = new JScrollPane(unitsPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPaneGamePanel.setBorder(BorderFactory.createEmptyBorder());
		add(scrollPaneGamePanel, new GridBagConstraints(0, 3, 2, 1, 1D, 1D, GridBagConstraints.NORTH,
				GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		// Y=10
		nextTurnButton = new JButton();
		nextTurnButton.setName("nextTurnButton");
		add(nextTurnButton, new GridBagConstraints(0, 10, 2, 1, 0D, 0D, GridBagConstraints.SOUTH,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		setPreferredSize(new Dimension(RIGHT_PANEL_WIDTH, 200));
		setMinimumSize(new Dimension(RIGHT_PANEL_WIDTH, 200));
		validate();
		unitsPanel.setMaximumSize(scrollPaneGamePanel.getViewport().getExtentSize());
		unitsPanel.setPreferredSize(scrollPaneGamePanel.getViewport().getExtentSize());
	}

	@Override
	public void showTile(final FocusedTileEvent event) {
		if (event == null) {
			return;
		}
		tileImage.setImage(imageProvider.getTerrainImage(event.getTerrain()));
		StringBuilder sb = new StringBuilder(200);
		sb.append("<html><div>");
		sb.append(localizationHelper.getTerrainName(event.getTerrain()));
		sb.append("");
		sb.append("</div><div>");
		sb.append("Move cost: 1");
		sb.append("</div></html>");
		tileName.setText(sb.toString());
		unitsPanel.clear();
		if (event.getModel().getUnitsAt(event.getLocation()).isEmpty()) {
			unitsLabel.setText("");
		} else {
			unitsLabel.setText(getText().get("unitsPanel.units"));
			unitsPanel.setUnits(event.getModel().getUnitsAt(event.getLocation()));
		}
		repaint();
	}

	@Override
	public void setOnMovePlayer(final Player player) {
		StringBuilder sb = new StringBuilder(200);
		sb.append("<html><div>");
		sb.append(getText().get("unitsPanel.currentUser"));
		sb.append(" ");
		sb.append(player.getName());
		sb.append("</div></html>");
		labelOnMove.setText(sb.toString());
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
