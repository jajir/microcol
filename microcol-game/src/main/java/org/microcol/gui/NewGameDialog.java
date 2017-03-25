package org.microcol.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;

public class NewGameDialog extends JDialog {

	/**
	 * Default serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	private final static int BORDER_SPAN = 20;

	/**
	 * Constructor when parentFrame is not available.
	 * 
	 * @param viewUtil
	 *            required tool for centering window on screen
	 * @param text
	 *            required localization helper class
	 */
	public NewGameDialog(final ViewUtil viewUtil, final Text text) {
		super();
		setTitle(text.get("newGameDialog.title"));
		setLayout(new GridBagLayout());

		final JLabel labelSelectMap = new JLabel(text.get("newGameDialog.selectMap"));
		add(labelSelectMap, new GridBagConstraints(0, 0, 1, 1, 0.0D, 0.0D, GridBagConstraints.SOUTHEAST,
				GridBagConstraints.NONE, new Insets(BORDER_SPAN, BORDER_SPAN, 15, 5), 0, 0));

		final JComboBox<String> comboBoxSelectMap = new JComboBox<>(getMaps());
		comboBoxSelectMap.setSelectedIndex(0);
		comboBoxSelectMap.setEditable(false);
		add(comboBoxSelectMap, new GridBagConstraints(1, 0, 1, 1, 0.0D, 0.0D, GridBagConstraints.SOUTHWEST,
				GridBagConstraints.NONE, new Insets(BORDER_SPAN, 5, 15, BORDER_SPAN), 0, 0));

		final JButton buttonCancel = new JButton(text.get("newGameDialog.cancel"));
		buttonCancel.addActionListener(e -> {
			setVisible(false);
		});
		buttonCancel.requestFocus();
		add(buttonCancel, new GridBagConstraints(0, 10, 1, 1, 1.0D, 1.0D, GridBagConstraints.SOUTHWEST,
				GridBagConstraints.NONE, new Insets(0, BORDER_SPAN, BORDER_SPAN, 10), 0, 0));

		final JButton buttonStartGame = new JButton(text.get("newGameDialog.startGame"));
		buttonStartGame.addActionListener(e -> {
			//TODO JJ selected map should be used.
//			final String selectedMap = (String) comboBoxSelectMap.getSelectedItem();
			setVisible(false);
		});
		buttonStartGame.requestFocus();
		add(buttonStartGame, new GridBagConstraints(1, 10, 1, 1, 1.0D, 1.0D, GridBagConstraints.SOUTHEAST,
				GridBagConstraints.NONE, new Insets(0, 0, BORDER_SPAN, BORDER_SPAN), 0, 0));

		setResizable(false);
		pack();
		setLocation(viewUtil.centerWindow(this));
		setModal(true);
	}

	private String[] getMaps() {
		try {
			final URL resource = ClassLoader.getSystemClassLoader().getResource("maps");
			File directory = new File(resource.toURI());
			String[] out = new String[directory.listFiles().length];
			int i = 0;
			for (final File f : directory.listFiles()) {
				out[i] = f.getName();
				i++;
			}
			return out;
		} catch (URISyntaxException e) {
			throw new MicroColException(e.getMessage(), e);
		}
	}

}
