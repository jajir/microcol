package org.microcol.model.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.microcol.gui.MicroColException;
import org.microcol.model.ConstructionType;
import org.microcol.model.GoodType;
import org.microcol.model.TerrainType;
import org.microcol.model.UnitType;
import org.microcol.model.WorldMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Read whole model from file.
 */
public class ModelDao {

	private Logger logger = LoggerFactory.getLogger(ModelDao.class);

	private final Gson gson;

	public ModelDao() {
		gson = new GsonBuilder().registerTypeAdapter(ConstructionType.class, new GsonConstructionTypeAdapter())
				.registerTypeAdapter(GoodType.class, new GsonGoodTypeAdapter())
				.registerTypeAdapter(TerrainType.class, new GsonTerrainTypeAdapter())
				.registerTypeAdapter(UnitType.class, new GsonUnitTypeAdapter()).setPrettyPrinting().create();
	}

	/**
	 * Load predefined model of scenario stored on class path.
	 * 
	 * @param fileName
	 *            required file name on class path
	 * @return loaded model persistent object
	 */
	public ModelPo loadPredefinedModel(final String fileName) {
		logger.debug("Starting to read from class path ({})", fileName);
		try {
			return internalLoadPredefinedModel(fileName);
		} catch (FileNotFoundException e) {
			throw new MicroColException(e.getMessage(), e);
		}
	}

	/**
	 * Load model or scenario stored on class path.
	 * 
	 * @param file
	 *            required file object
	 * @return loaded model persistent object
	 */
	public ModelPo loadModel(final File file) {
		Preconditions.checkNotNull(file);
		Preconditions.checkArgument(file.exists());
		Preconditions.checkArgument(file.isFile());
		logger.debug("Starting to read from class path ({})", file.getAbsolutePath());
		try {
			return internalLoadModel(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			throw new MicroColException(e.getMessage(), e);
		}
	}

	/**
	 * Load predefined world map stored on class path.
	 * 
	 * @param fileName
	 *            required file name on class path
	 * @return loaded world map
	 */
	public WorldMap loadPredefinedWorldMap(final String fileName) {
		try {
			return new WorldMap(internalLoadPredefinedModel(fileName));
		} catch (FileNotFoundException e) {
			throw new MicroColException(e.getMessage(), e);
		}
	}

	private ModelPo internalLoadPredefinedModel(final String fileName) throws FileNotFoundException {
		final InputStream is = WorldMap.class.getResourceAsStream(fileName);
		Preconditions.checkArgument(is != null, "input stream for file (%s) is null", fileName);
		return internalLoadModel(is);
	}

	private ModelPo internalLoadModel(final InputStream is) throws FileNotFoundException {
		Preconditions.checkArgument(is != null, "input stream is null");

		ModelPo loaded = gson.fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), ModelPo.class);

		return loaded;
	}

	public void saveToFile(final String fileName, final ModelPo modelPo) {
		try {
			internalSaveModel(fileName, modelPo);
		} catch (IOException e) {
			throw new MicroColException(e.getMessage(), e);
		}
	}

	private void internalSaveModel(final String fileName, final ModelPo modelPo) throws IOException {
		final String str = gson.toJson(modelPo);
		if (logger.isDebugEnabled()) {
			logger.debug(str);
		}
		final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(fileName),
				StandardCharsets.UTF_8);
		outputStreamWriter.write(str);
		outputStreamWriter.close();
	}

}
