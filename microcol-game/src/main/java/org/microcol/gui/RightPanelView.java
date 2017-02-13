package org.microcol.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.google.inject.Inject;

public class RightPanelView extends JPanel implements RightPanelPresenter.Display {

	/**
	 * Default serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	private final JButton nextTurnButton;

	private final JLabel textLabel;

	@Inject
	public RightPanelView() {
		this.setLayout(new GridBagLayout());

		textLabel = new JLabel("...");
		add(textLabel, new GridBagConstraints(0, 0, 1, 1, 0D, 1D, GridBagConstraints.NORTH, GridBagConstraints.VERTICAL,
				new Insets(0, 0, 0, 0), 0, 0));

		nextTurnButton = new JButton("Next turn");
		add(nextTurnButton, new GridBagConstraints(0, 1, 1, 1, 0D, 0D, GridBagConstraints.SOUTH,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

	}

	@Override
	public JButton getNextTurnButton() {
		return nextTurnButton;
	}

	@Override
	public JLabel getTextLabel() {
		return textLabel;
	}

}
