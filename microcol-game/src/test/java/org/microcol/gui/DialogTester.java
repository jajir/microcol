package org.microcol.gui;

import javax.swing.JFrame;

import org.easymock.classextension.EasyMock;
import org.microcol.gui.colony.ColonyDialog;
import org.microcol.gui.europe.EuropeDialog;
import org.microcol.gui.event.AnimationSpeedChangeController;
import org.microcol.gui.event.VolumeChangeController;
import org.microcol.gui.event.model.GameController;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.Text.Language;
import org.microcol.gui.util.ViewUtil;
import org.microcol.model.Player;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Allows to display panel and dialog without creating game event.
 */
public class DialogTester extends Application {

	private static JFrame parentFrame;

	private static ViewUtil viewUtil;

	private static ImageProvider imageProvider;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(final Stage primaryStage) throws Exception {
		Platform.runLater(() -> {
			parentFrame = new JFrame("main frame");
			viewUtil = new ViewUtil(primaryStage);
			imageProvider = new ImageProvider();

			// startPreferencesVolume();
			// startPreferencesAnimationSpeed();

			startNewGameDialog();
			// testDialogFight();
			// dialogWarning();
			// dialogSave();
			// dialogLoad();
			// europeDialog();
			// dialogColony();
			// primaryStage.show();
		});
	}

	public final static void startNewGameDialog() {
		new NewGameDialog(new ViewUtil(parentFrame), new Text(Language.cz.getLocale()));
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
		new PreferencesAnimationSpeed(text, viewUtil, controller, actualVolume);
	}

	public final static void testDialogFight() {
		final Text text = new Text(Text.Language.cz.getLocale());
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

	public final static void europeDialog() {
		final Text text = new Text(Text.Language.cz.getLocale());
		final GameController gameController = EasyMock.createMock(GameController.class);
		EasyMock.replay(gameController);
		new EuropeDialog(viewUtil, text, imageProvider, gameController);
	}

	public final static void dialogColony() {
		final Text text = new Text(Text.Language.cz.getLocale());
		final GameController gameController = EasyMock.createMock(GameController.class);
		EasyMock.replay(gameController);
		new ColonyDialog(viewUtil, text, imageProvider, gameController);
	}

}
