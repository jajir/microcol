package org.microcol.gui;

import org.easymock.EasyMock;
import org.microcol.gui.colonizopedia.Colonizopedia;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Allows to display panel and dialog without creating game event.
 */
public class DialogTester extends Application {

	private static final Logger logger = LoggerFactory.getLogger(DialogTester.class);

	private static ViewUtil viewUtil;

	private static ImageProvider imageProvider;

	private static Text text;

	private static LocalizationHelper localizationHelper;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(final Stage primaryStage) throws Exception {
		Platform.runLater(() -> {
			viewUtil = new ViewUtil(primaryStage);
			imageProvider = new ImageProvider();
			text = new Text(Language.cz.getLocale());
			localizationHelper = new LocalizationHelper(text);

			// startPreferencesVolume();
			// startPreferencesAnimationSpeed();
			// dialogWarning();
			// testDialogFight();
			// startNewGameDialog();
			// startAboutDialog();
			dialogEurope();
			// dialogColonizopedia();

			// dialogSave();
			// dialogLoad();
			// dialogColony();
			// primaryStage.show();
		});
	}

	public final static void startAboutDialog() {
		new AboutDialog(viewUtil, text);
	}

	public final static void startNewGameDialog() {
		new NewGameDialog(viewUtil, text);
	}

	public final static void startPreferencesVolume() {
		VolumeChangeController controller = new VolumeChangeController();
		int actualVolume = 10;
		new PreferencesVolume(viewUtil, text, controller, actualVolume);
	}

	public final static void startPreferencesAnimationSpeed() {
		AnimationSpeedChangeController controller = new AnimationSpeedChangeController();
		int actualVolume = 10;
		new PreferencesAnimationSpeed(text, viewUtil, controller, actualVolume);
	}

	public final static void testDialogFight() {
		final LocalizationHelper localizationHelper = new LocalizationHelper(text);

		final Player playerAttacker = EasyMock.createMock(Player.class);
		final Player playerDefender = EasyMock.createMock(Player.class);

		final Unit unitAttacker = EasyMock.createMock(Unit.class);
		final Unit unitDefender = EasyMock.createMock(Unit.class);

		EasyMock.expect(unitAttacker.getType()).andReturn(UnitType.FRIGATE).times(2);
		EasyMock.expect(unitDefender.getType()).andReturn(UnitType.GALLEON).times(2);

		EasyMock.replay(playerAttacker, playerDefender, unitAttacker, unitDefender);

		DialogFigth preferences = new DialogFigth(text, viewUtil, imageProvider, localizationHelper, null, unitAttacker,
				unitDefender);
		logger.debug("User wants to fight: " + preferences.isUserChooseFight());
	}

	public final static void dialogWarning() {
		new DialogWarning(viewUtil);
	}

	public final static void dialogSave() {
		final GameController gameController = EasyMock.createMock(GameController.class);
		final PersistingDialog persistingDialog = new PersistingDialog(viewUtil, text, gameController);

		persistingDialog.saveModel();
	}

	public final static void dialogLoad() {
		final GameController gameController = EasyMock.createMock(GameController.class);
		final PersistingDialog persistingDialog = new PersistingDialog(viewUtil, text, gameController);

		persistingDialog.loadModel();
	}

	public final static void dialogEurope() {
		final GameController gameController = EasyMock.createMock(GameController.class);
		EasyMock.replay(gameController);
		new EuropeDialog(viewUtil, text, imageProvider, gameController, localizationHelper);
	}

	public final static void dialogColonizopedia() {
		final GameController gameController = EasyMock.createMock(GameController.class);
		EasyMock.replay(gameController);
		new Colonizopedia(text, viewUtil, imageProvider, localizationHelper);
	}

}
