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
//			startWaitingDialog();
			startNewGameDialog();
		});
	}

	public final static void startNewGameDialog() {
		JDialog dialog = new NewGameDialog(new ViewUtil(), new Text(Language.cz));
		dialog.setResizable(false);
		dialog.setVisible(true);
	}

	public final static void startWaitingDialog() {
		JDialog dialog = new WaitingDialog(new ViewUtil(), new Text(Language.cz));
		dialog.setResizable(false);
		dialog.setVisible(true);
	}

}
