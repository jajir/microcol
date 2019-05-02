package org.microcol.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.microcol.gui.MicroColException;
import org.microcol.model.store.GsonConstructionTypeAdapter;
import org.microcol.model.store.GsonDirectionTypeAdapter;
import org.microcol.model.store.GsonGoodsAdapter;
import org.microcol.model.store.GsonGoodsTypeAdapter;
import org.microcol.model.store.GsonQueueItemTypeAdapter;
import org.microcol.model.store.GsonTerrainTypeAdapter;
import org.microcol.model.store.GsonUnitTypeAdapter;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.QueueItemType;
import org.microcol.model.store.UnitActionPo;
import org.microcol.model.store.UnitActionPoAdapter;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Predecessor for test that need to load world map from file.
 */
public abstract class AbstractMapTest {

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(ConstructionType.class, new GsonConstructionTypeAdapter())
            .registerTypeAdapter(GoodsType.class, new GsonGoodsTypeAdapter())
            .registerTypeAdapter(QueueItemType.class, new GsonQueueItemTypeAdapter())
            .registerTypeAdapter(TerrainType.class, new GsonTerrainTypeAdapter())
            .registerTypeAdapter(UnitActionPo.class, new UnitActionPoAdapter())
            .registerTypeAdapter(UnitType.class, new GsonUnitTypeAdapter())
            .registerTypeAdapter(Direction.class, new GsonDirectionTypeAdapter())
            .registerTypeAdapter(Goods.class, new GsonGoodsAdapter()).setPrettyPrinting().create();

    /**
     * Load predefined world map stored on class path.
     *
     * @param fileName
     *            required file name on class path
     * @return loaded world map
     */
    protected WorldMap loadPredefinedWorldMap(final String fileName) {
        return new WorldMap(loadFromClassPath(fileName));
    }

    /**
     * Load predefined model of scenario stored on class path.
     * 
     * @param fileName
     *            required file name on class path
     * @return loaded model persistent object
     */
    private ModelPo loadFromClassPath(final String fileName) {
        try (final InputStream is = WorldMap.class.getResourceAsStream(fileName)) {
            Preconditions.checkArgument(is != null, "input stream for file (%s) is null", fileName);
            return gson.fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), ModelPo.class);
        } catch (IOException e) {
            throw new MicroColException(e.getMessage(), e);
        }
    }

}
