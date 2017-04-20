package org.microcol.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.microcol.gui.util.Localized;

import com.google.inject.Inject;

public class StartPanelView extends JPanel implements StartPanelPresenter.Display, Localized {

	/**
	 * Default serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	private final JButton buttonStartNewGame;

	@Inject
	public StartPanelView() {
		this.setLayout(new GridBagLayout());
		buttonStartNewGame = new JButton();
		add(buttonStartNewGame, new GridBagConstraints(0, 0, 1, 1, 1.0D, 1.0D, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		
		updateLanguage();
	}

	@Override
	public void updateLanguage() {
		buttonStartNewGame.setText(getText().get("startPanel.buttonNewGame"));
	}

	@Override
	public JButton getButtonStartNewGame() {
		return buttonStartNewGame;
	}

}
