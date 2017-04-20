package org.microcol.gui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import javax.json.stream.JsonParser;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.filechooser.FileView;

import org.microcol.gui.event.model.GameController;
import org.microcol.gui.util.Text;
import org.microcol.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.google.inject.Inject;

/**
 * Provide load and save operations.
 */
public class PersistingDialog {

	private final Logger logger = LoggerFactory.getLogger(PersistingDialog.class);

	private final Text text;

	private final GameController gameController;

	@Inject
	public PersistingDialog(final Text text, final GameController gameController) {
		this.text = Preconditions.checkNotNull(text);
		this.gameController = Preconditions.checkNotNull(gameController);
	}

	public void saveModel() {
		final JFileChooser saveFile = makeFileChooser();
		saveFile.setDialogTitle(text.get("saveGameDialog.title"));
		final int rVal = saveFile.showSaveDialog(null);
		if (rVal == JFileChooser.APPROVE_OPTION) {
			saveModelToFile(saveFile.getSelectedFile());
		}
	}

	public void loadModel() {
		final JFileChooser loadFile = makeFileChooser();
		loadFile.setDialogTitle(text.get("loadGameDialog.title"));
		final int rVal = loadFile.showOpenDialog(null);
		if (rVal == JFileChooser.APPROVE_OPTION) {
			loadFromFile(loadFile.getSelectedFile());
		}
	}

	private void saveModelToFile(final File targetFile) {
		logger.debug("Saving game to file '{}' ", targetFile.getAbsolutePath());
		writeModelToFile(gameController.getModel(), targetFile);

	}

	private void loadFromFile(final File sourceFile) {
		logger.debug("Loading game from file '{}' ", sourceFile.getAbsolutePath());
		gameController.setModel(loadModelFromFile(sourceFile));
	}

	private JFileChooser makeFileChooser() {
		final Path path = Paths.get(System.getProperty("user.home"), ".microcol", "saves");
		try {
			Files.createParentDirs(
					Paths.get(System.getProperty("user.home"), ".microcol", "saves", "pok.microcol").toFile());
		} catch (IOException e) {
			throw new MicroColException(e.getMessage(), e);
		}
		final JFileChooser fileChooser = new JFileChooser(path.toFile());
		fileChooser.setFileFilter(new FileNameExtensionFilter("MicroCol game saves", "microcol"));
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setFileView(new FileView() {
			@Override
			public Icon getIcon(File f) {
				return FileSystemView.getFileSystemView().getSystemIcon(f);
			}
		});
		return fileChooser;
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
