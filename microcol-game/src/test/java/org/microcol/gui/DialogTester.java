package org.microcol.gui;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import org.microcol.gui.event.AnimationSpeedChangeController;
import org.microcol.gui.event.VolumeChangeController;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.Text.Language;

/**
 * Allows to display panel and dialog without creating game event.
 */
public class DialogTester {

	public static void main(String[] args) {

		SwingUtilities.invokeLater(() -> {
			// startWaitingDialog();
			// startNewGameDialog();
//			startPreferencesVolume();
			startPreferencesAnimationSpeed();
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

	public final static void startPreferencesVolume() {
		ViewUtil viewUtil = new ViewUtil();
		Text text = new Text(Text.Language.cz);
		VolumeChangeController controller = new VolumeChangeController();
		int actualVolume = 10;
		PreferencesVolume preferences = new PreferencesVolume(viewUtil, text, controller, actualVolume);
		preferences.setVisible(true);
	}

	public final static void startPreferencesAnimationSpeed() {
		ViewUtil viewUtil = new ViewUtil();
		Text text = new Text(Text.Language.cz);
		AnimationSpeedChangeController controller = new AnimationSpeedChangeController();
		int actualVolume = 10;
		PreferencesAnimationSpeed preferences = new PreferencesAnimationSpeed(viewUtil, text, controller, actualVolume);
		preferences.setVisible(true);
	}

}
