package org.microcol.gui.screen.editor;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.microcol.gui.screen.game.gamepanel.VisibleAreaService;
import org.microcol.model.Location;
import org.microcol.model.TerrainType;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.WorldMapPo;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * Holds edited game model and provide basic operations with this model.
 */
@Singleton
public class ModelService {

    private final GameModelPoDao gameModelPoDao;

    private final VisibleAreaService visibleAreaService;

    private Map<Location, TerrainType> mapCache;
    private Set<Location> treesCache;
    private Set<Location> fieldsCache;
    private Set<Location> colonyCache;

    private GameModelPo gameModelPo;

    private File file;

    @Inject
    ModelService(final GameModelPoDao gameModelPoDao,
            final @Named("editor") VisibleAreaService visibleAreaService) {
        this.gameModelPoDao = Preconditions.checkNotNull(gameModelPoDao);
        this.visibleAreaService = Preconditions.checkNotNull(visibleAreaService);
    }

    /**
     * Load game model from given file.
     * 
     * @param file
     *            required file
     */
    void load(final File file) {
        this.file = Preconditions.checkNotNull(file);
        this.gameModelPo = gameModelPoDao.loadFromFile(file);
        visibleAreaService.setMapSize(getMapSize());
        initCache();
    }

    void initCache() {
        mapCache = getMap().getTerrainMap();
        treesCache = getMap().getTreeSet();
        fieldsCache = getMap().getFieldSet();
        colonyCache = getModelPo().getColonies().stream().map(colonyPo -> colonyPo.getLocation())
                .collect(Collectors.toSet());
    }

    WorldMapPo getMap() {
        return getModelPo().getMap();
    }

    Location getMapSize() {
        return Location.of(getMap().getMaxX(), getMap().getMaxY());
    }

    /**
     * Save currently loaded game model.
     */
    public void save() {
        gameModelPoDao.saveToFile(file.getAbsolutePath(), gameModelPo);
    }

    public ModelPo getModelPo() {
        return gameModelPo.getModel();
    }

    TerrainType getTerrainTypeAt(final Location location) {
        return mapCache.get(location);
    }

    boolean hasTrees(final Location location) {
        return treesCache.contains(location);
    }

    boolean hasField(final Location location) {
        return fieldsCache.contains(location);
    }

    boolean hasColony(final Location location) {
        return colonyCache.contains(location);
    }

}
