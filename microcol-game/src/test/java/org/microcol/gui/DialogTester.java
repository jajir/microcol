package org.microcol.gui;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import org.microcol.gui.Text.Language;

/**
 * Allows to display panel and dialog without creating game event.
 */
public class DialogTester {

	public static void main(String[] args) {

		SwingUtilities.invokeLater(() -> {
			JDialog dialog = new WaitingDialog(new ViewUtil(), new Text(Language.cz));
			dialog.setResizable(false);
			dialog.setVisible(true);
		});
	}

}
