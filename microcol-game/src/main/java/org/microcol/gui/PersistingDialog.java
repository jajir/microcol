package org.microcol.gui;

import java.io.File;
import java.nio.file.Path;

import org.microcol.gui.event.model.GameController;
import org.microcol.gui.util.AbstractDialog;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;
import org.microcol.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.stage.FileChooser;

/**
 * Provide load and save operations.
 */
public class PersistingDialog extends AbstractDialog {

	public static final String SAVE_FILE_EXTENSION = "microcol";

	private final Logger logger = LoggerFactory.getLogger(PersistingDialog.class);

	private final Text text;

	private final GameController gameController;

	@Inject
	public PersistingDialog(final ViewUtil viewUtil, final Text text, final GameController gameController) {
		super(viewUtil);
		this.text = Preconditions.checkNotNull(text);
		this.gameController = Preconditions.checkNotNull(gameController);
	}

	public void saveModel() {
		final FileChooser fileChooser = prepareFileChooser("saveGameDialog.title");
		File saveFile = fileChooser.showSaveDialog(getViewUtil().getPrimaryStage());
		if (saveFile == null) {
			logger.debug("User didn't select any file to save game");
		} else {
			saveModelToFile(saveFile);
		}
	}

	public void loadModel() {
		final FileChooser fileChooser = prepareFileChooser("loadGameDialog.title");
		File saveFile = fileChooser.showOpenDialog(getViewUtil().getPrimaryStage());
		if (saveFile == null) {
			logger.debug("User didn't select any file to load game");
		} else {
			loadFromFile(saveFile);
		}
	}

	private FileChooser prepareFileChooser(final String caption) {
		final FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(text.get(caption));
		fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
		fileChooser.setInitialFileName("gamesave-01.microcol");
		fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("MicroCol data files", "microcol"));
		return fileChooser;
	}

	private void saveModelToFile(final File targetFile) {
		final File file = correctFileNameExtension(targetFile);
		logger.debug("Saving game to file '{}' ", file.getAbsolutePath());
		writeModelToFile(gameController.getModel(), file);
	}

	private void loadFromFile(final File sourceFile) {
		final File file = correctFileNameExtension(sourceFile);
		logger.debug("Loading game from file '{}' ", file.getAbsolutePath());
		gameController.setModel(loadModelFromFile(file));
	}

	private File correctFileNameExtension(final File targetFile) {
		Path path = targetFile.toPath();
		if (path.getFileName().toString().toLowerCase().endsWith(SAVE_FILE_EXTENSION)) {
			return targetFile;
		} else {
			final Path parent = path.getParent();
			Path out = parent.resolve(path.getFileName().toString() + "." + SAVE_FILE_EXTENSION);
			return out.toFile();
		}
	}

	@SuppressWarnings("unused")
	private void writeModelToFile(final Model model, final File targetFile) {
		//TODO JJ perform saving of model
	}

	@SuppressWarnings("unused")
	private Model loadModelFromFile(final File sourceFile) {
		//TODO JJ perform model loading
		return null;
	}

}
