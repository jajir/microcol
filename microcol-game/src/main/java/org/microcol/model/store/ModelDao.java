package org.microcol.model.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.microcol.gui.MicroColException;
import org.microcol.model.ConstructionType;
import org.microcol.model.Direction;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
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
public final class ModelDao {

    private Logger logger = LoggerFactory.getLogger(ModelDao.class);

    private final Gson gson;

    public ModelDao() {
        gson = new GsonBuilder()
                .registerTypeAdapter(ConstructionType.class, new GsonConstructionTypeAdapter())
                .registerTypeAdapter(GoodsType.class, new GsonGoodsTypeAdapter())
                .registerTypeAdapter(QueueItemType.class, new GsonQueueItemTypeAdapter())
                .registerTypeAdapter(TerrainType.class, new GsonTerrainTypeAdapter())
                .registerTypeAdapter(UnitActionPo.class, new UnitActionPoAdapter())
                .registerTypeAdapter(UnitType.class, new GsonUnitTypeAdapter())
                .registerTypeAdapter(Direction.class, new GsonDirectionTypeAdapter())
                .registerTypeAdapter(Goods.class, new GsonGoodsAdapter()).setPrettyPrinting()
                .create();
    }

    /**
     * Load predefined model of scenario stored on class path.
     * 
     * @param fileName
     *            required file name on class path
     * @return loaded model persistent object
     */
    public ModelPo loadFromClassPath(final String fileName) {
        logger.debug("Starting to read from class path ({})", fileName);
        try (final InputStream is = WorldMap.class.getResourceAsStream(fileName)) {
            Preconditions.checkArgument(is != null, "input stream for file (%s) is null", fileName);
            return internalLoadModel(is);
        } catch (IOException e) {
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
    public ModelPo loadFromFile(final File file) {
        Preconditions.checkNotNull(file, "File is null");
        Preconditions.checkArgument(file.exists(), "File '%s' doesn't exists.",
                file.getAbsolutePath());
        Preconditions.checkArgument(file.isFile());
        logger.debug("Starting to read from class path ({})", file.getAbsolutePath());
        try (final FileInputStream fis = new FileInputStream(file)) {
            return internalLoadModel(fis);
        } catch (IOException e) {
            throw new MicroColException(e.getMessage(), e);
        }
    }

    private ModelPo internalLoadModel(final InputStream is) {
        Preconditions.checkArgument(is != null, "input stream is null");
        final ModelPo loaded = gson.fromJson(new InputStreamReader(is, StandardCharsets.UTF_8),
                ModelPo.class);
        return loaded;
    }

    public void saveToFile(final String fileName, final ModelPo modelPo) {
        final String str = gson.toJson(modelPo);
        if (logger.isTraceEnabled()) {
            logger.trace(str);
        }
        try (final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                new FileOutputStream(fileName), StandardCharsets.UTF_8);) {
            outputStreamWriter.write(str);
        } catch (IOException e) {
            throw new MicroColException(e.getMessage(), e);
        }
    }

}
