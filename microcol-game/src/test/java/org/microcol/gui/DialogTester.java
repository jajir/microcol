package org.microcol.gui;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import org.easymock.classextension.EasyMock;
import org.microcol.gui.event.AnimationSpeedChangeController;
import org.microcol.gui.event.VolumeChangeController;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.Text.Language;
import org.microcol.model.Player;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;

/**
 * Allows to display panel and dialog without creating game event.
 */
public class DialogTester {

	public static void main(String[] args) {

		SwingUtilities.invokeLater(() -> {
			// startWaitingDialog();
			// startNewGameDialog();
			// startPreferencesVolume();
			// startPreferencesAnimationSpeed();
			testDialogFight();
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
		final ViewUtil viewUtil = new ViewUtil();
		final Text text = new Text(Text.Language.cz);
		VolumeChangeController controller = new VolumeChangeController();
		int actualVolume = 10;
		PreferencesVolume preferences = new PreferencesVolume(viewUtil, text, controller, actualVolume);
		preferences.setVisible(true);
	}

	public final static void startPreferencesAnimationSpeed() {
		final Text text = new Text(Text.Language.cz);
		AnimationSpeedChangeController controller = new AnimationSpeedChangeController();
		int actualVolume = 10;
		PreferencesAnimationSpeed preferences = new PreferencesAnimationSpeed(text, controller, actualVolume);
		preferences.setVisible(true);
	}

	public final static void testDialogFight() {
		final Text text = new Text(Text.Language.cz);
		final ImageProvider imageProvider = new ImageProvider();
		final LocalizationHelper localizationHelper = new LocalizationHelper(text);

		final Player playerAttacker = EasyMock.createMock(Player.class);
		final Player playerDefender = EasyMock.createMock(Player.class);

		final Unit unitAttacker = EasyMock.createMock(Unit.class);
		final Unit unitDefender = EasyMock.createMock(Unit.class);

		EasyMock.expect(unitAttacker.getType()).andReturn(UnitType.FRIGATE).times(2);
		EasyMock.expect(unitDefender.getType()).andReturn(UnitType.GALLEON).times(2);

		EasyMock.replay(playerAttacker, playerDefender, unitAttacker, unitDefender);

		DialogFigth preferences = new DialogFigth(text, imageProvider, localizationHelper, unitAttacker, unitDefender);
		preferences.setVisible(true);
		System.out.println("User wants to fight: " + preferences.isUserChooseFight());
	}

}
