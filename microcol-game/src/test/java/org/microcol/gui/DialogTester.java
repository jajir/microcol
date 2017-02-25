package org.microcol.gui;

import javax.swing.SwingUtilities;

import org.microcol.gui.Text.Language;

/**
 * Allows to display panel and dialog without creating game event.
 */
public class DialogTester {

	public static void main(String[] args) {

		SwingUtilities.invokeLater(() -> {
			WaitingDialog dialog = new WaitingDialog(new ViewUtil(), new Text(Language.cz));
			dialog.setVisible(true);
		});
	}

}
