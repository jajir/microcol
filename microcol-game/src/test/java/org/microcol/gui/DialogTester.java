package org.microcol.gui;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.easymock.classextension.EasyMock;
import org.microcol.gui.event.AnimationSpeedChangeController;
import org.microcol.gui.event.VolumeChangeController;
import org.microcol.gui.event.model.GameController;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.Text.Language;
import org.microcol.gui.util.ViewUtil;
import org.microcol.model.Player;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;

/**
 * Allows to display panel and dialog without creating game event.
 */
public class DialogTester {

	private static JFrame parentFrame;

	private static ViewUtil viewUtil;

	public static void main(String[] args) {

		SwingUtilities.invokeLater(() -> {
			parentFrame = new JFrame("main frame");
			viewUtil = new ViewUtil(parentFrame);
			// startWaitingDialog();
			// startNewGameDialog();
			startPreferencesVolume();
			// startPreferencesAnimationSpeed();
			// testDialogFight();
			// dialogWarning();
			// dialogSave();
			// dialogLoad();
		});
	}

	public final static void startNewGameDialog() {
		new NewGameDialog(new ViewUtil(parentFrame), new Text(Language.cz.getLocale()));
	}

	public final static void startWaitingDialog() {
		new WaitingDialog(new ViewUtil(parentFrame), new Text(Language.cz.getLocale()));
	}

	public final static void startPreferencesVolume() {
		final Text text = new Text(Text.Language.cz.getLocale());
		VolumeChangeController controller = new VolumeChangeController();
		int actualVolume = 10;
		new PreferencesVolume(viewUtil, text, controller, actualVolume);
	}

	public final static void startPreferencesAnimationSpeed() {
		final Text text = new Text(Text.Language.cz.getLocale());
		AnimationSpeedChangeController controller = new AnimationSpeedChangeController();
		int actualVolume = 10;
		PreferencesAnimationSpeed preferences = new PreferencesAnimationSpeed(text, viewUtil, controller, actualVolume);
		preferences.setVisible(true);
	}

	public final static void testDialogFight() {
		final Text text = new Text(Text.Language.cz.getLocale());
		final ImageProvider imageProvider = new ImageProvider();
		final LocalizationHelper localizationHelper = new LocalizationHelper(text);

		final Player playerAttacker = EasyMock.createMock(Player.class);
		final Player playerDefender = EasyMock.createMock(Player.class);

		final Unit unitAttacker = EasyMock.createMock(Unit.class);
		final Unit unitDefender = EasyMock.createMock(Unit.class);

		EasyMock.expect(unitAttacker.getType()).andReturn(UnitType.FRIGATE).times(2);
		EasyMock.expect(unitDefender.getType()).andReturn(UnitType.GALLEON).times(2);

		EasyMock.replay(playerAttacker, playerDefender, unitAttacker, unitDefender);

		DialogFigth preferences = new DialogFigth(text, viewUtil, imageProvider, localizationHelper, unitAttacker,
				unitDefender);
		System.out.println("User wants to fight: " + preferences.isUserChooseFight());
	}

	public final static void dialogWarning() {
		DialogWarning dialogWarning = new DialogWarning(new ViewUtil(parentFrame));
		dialogWarning.setVisible(true);
	}

	public final static void dialogSave() {
		final Text text = new Text(Text.Language.cz.getLocale());
		final GameController gameController = EasyMock.createMock(GameController.class);
		final PersistingDialog persistingDialog = new PersistingDialog(text, gameController);

		persistingDialog.saveModel();
	}

	public final static void dialogLoad() {
		final Text text = new Text(Text.Language.cz.getLocale());
		final GameController gameController = EasyMock.createMock(GameController.class);
		final PersistingDialog persistingDialog = new PersistingDialog(text, gameController);

		persistingDialog.loadModel();
	}

}
