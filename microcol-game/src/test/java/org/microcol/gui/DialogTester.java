package org.microcol.gui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.filechooser.FileView;

import org.easymock.classextension.EasyMock;
import org.microcol.gui.event.AnimationSpeedChangeController;
import org.microcol.gui.event.VolumeChangeController;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.Text.Language;
import org.microcol.model.Player;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;

import com.google.common.io.Files;

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
			// testDialogFight();
			// dialogWarning();
			dialogSave();
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

	public final static void dialogWarning() {
		DialogWarning dialogWarning = new DialogWarning();
		dialogWarning.setVisible(true);
	}

	public final static void dialogSave() {
		Path path = Paths.get(System.getProperty("user.home"), ".microcol", "saves");
		try {
			Files.createParentDirs(
					Paths.get(System.getProperty("user.home"), ".microcol", "saves", "pok.microcol").toFile());
		} catch (IOException e) {
			e.printStackTrace();
		}
		JFileChooser saveFile = new JFileChooser(path.toFile());
		FileNameExtensionFilter filter = new FileNameExtensionFilter("MicroCol game saves", "microcol");
		saveFile.setFileFilter(filter);
		saveFile.setMultiSelectionEnabled(false);
		saveFile.setFileSelectionMode(JFileChooser.FILES_ONLY);
		saveFile.setFileView(new FileView() {
			@Override
			public Icon getIcon(File f) {
				return FileSystemView.getFileSystemView().getSystemIcon(f);
			}
		});
		int rVal = saveFile.showSaveDialog(null);
		if (rVal == JFileChooser.APPROVE_OPTION) {
			System.out.println(saveFile.getSelectedFile().getName());
			System.out.println(saveFile.getCurrentDirectory().toString());
		}
		if (rVal == JFileChooser.CANCEL_OPTION) {
			System.out.println("You pressed cancel");
			System.out.println("");
		}
	}

}
