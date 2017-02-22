package org.microcol.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import com.google.inject.Inject;

public class StatusBarView extends JPanel implements StatusBarPresenter.Display {

	/**
	 * Default serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	private final JLabel statusBarDescription;

	private final JLabel labelEra;

	@Inject
	public StatusBarView() {
		this.setLayout(new GridBagLayout());

		statusBarDescription = new JLabel();
		add(statusBarDescription, new GridBagConstraints(0, 0, 1, 1, 1D, 0D, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL, new Insets(0, 10, 0, 3), 0, 0));

		add(new JSeparator(SwingConstants.VERTICAL), new GridBagConstraints(1, 0, 1, 1, 0D, 1D,
				GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(0, 3, 0, 15), 0, 0));

		labelEra = new JLabel();
		add(labelEra, new GridBagConstraints(2, 0, 1, 1, 0D, 0D, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(0, 3, 0, 15), 0, 0));

		Border border = BorderFactory.createBevelBorder(EtchedBorder.LOWERED);
		setBorder(border);
	}

	@Override
	public JLabel getStatusBarDescription() {
		return statusBarDescription;
	}

	@Override
	public JLabel getLabelEra() {
		return labelEra;
	}
}
