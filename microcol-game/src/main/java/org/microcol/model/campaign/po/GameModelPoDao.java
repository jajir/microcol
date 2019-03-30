package org.microcol.model.campaign.po;

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
import org.microcol.model.store.GsonConstructionTypeAdapter;
import org.microcol.model.store.GsonDirectionTypeAdapter;
import org.microcol.model.store.GsonGoodsAdapter;
import org.microcol.model.store.GsonGoodsTypeAdapter;
import org.microcol.model.store.GsonQueueItemTypeAdapter;
import org.microcol.model.store.GsonTerrainTypeAdapter;
import org.microcol.model.store.GsonUnitTypeAdapter;
import org.microcol.model.store.QueueItemType;
import org.microcol.model.store.UnitActionPo;
import org.microcol.model.store.UnitActionPoAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Read whole game model from file.
 */
public final class GameModelPoDao {

    private Logger logger = LoggerFactory.getLogger(GameModelPoDao.class);

    private final Gson gson;

    public GameModelPoDao() {
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
    public GameModelPo loadFromClassPath(final String fileName) {
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
    public GameModelPo loadFromFile(final File file) {
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

    private GameModelPo internalLoadModel(final InputStream is) {
        Preconditions.checkArgument(is != null, "input stream is null");
        final GameModelPo loaded = gson.fromJson(new InputStreamReader(is, StandardCharsets.UTF_8),
                GameModelPo.class);
        return loaded;
    }

    public void saveToFile(final String fileName, final GameModelPo gameModelPo) {
        final String str = gson.toJson(gameModelPo);
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
