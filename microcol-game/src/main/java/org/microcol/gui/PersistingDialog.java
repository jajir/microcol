package org.microcol.gui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import javax.json.stream.JsonParser;

import org.microcol.gui.event.model.GameController;
import org.microcol.gui.util.AbstractDialog;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;
import org.microcol.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
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
		final FileChooser fileChooser = prepareFileChooser("saveGameDialog.title");
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

	private void writeModelToFile(final Model model, final File targetFile) {
		final Map<String, ?> config = ImmutableMap.of(JsonGenerator.PRETTY_PRINTING, Boolean.TRUE);
		final JsonGeneratorFactory factory = Json.createGeneratorFactory(config);
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile))) {
			final JsonGenerator generator = factory.createGenerator(writer);
			generator.writeStartObject();
			model.save("model", generator);
			generator.writeEnd();
			generator.close();
		} catch (IOException e) {
			throw new MicroColException(e.getMessage(), e);
		}
	}

	private Model loadModelFromFile(final File sourceFile) {
		try (BufferedReader reader = new BufferedReader(new FileReader(sourceFile))) {
			final JsonParser parser = Json.createParser(reader);
			parser.next(); // START_OBJECT
			parser.next(); // KEY_NAME
			final Model model = Model.load(parser);
			parser.next(); // END_OBJECT
			parser.close();
			return model;
		} catch (IOException e) {
			throw new MicroColException(e.getMessage(), e);
		}
	}

}
