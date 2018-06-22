package org.microcol.model;

import java.util.Set;

import org.microcol.model.store.ModelPo;
import org.microcol.model.store.VisibilityPo;
import org.microcol.model.store.WorldMapPo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

public class WorldMap {

    private final int maxX;
    private final int maxY;

    private final ImmutableMap<Location, TerrainType> terrainMap;

    private final Set<Location> trees;

    private final Integer seed;

    public WorldMap(final ModelPo gamePo) {
        final WorldMapPo worldMapPo = gamePo.getMap();
        this.maxX = worldMapPo.getMaxX();
        this.maxY = worldMapPo.getMaxY();
        Preconditions.checkArgument(maxY >= 1, "Max Y (%s) must be positive.", maxY);
        Preconditions.checkArgument(maxX >= 1, "Max X (%s) must be positive.", maxX);
        this.terrainMap = ImmutableMap.copyOf(worldMapPo.getTerrainMap());
        this.trees = worldMapPo.getTreeSet();
        this.seed = Preconditions.checkNotNull(worldMapPo.getSeed(), "Seed value is null");
        verifyThatMapIsComplete();
    }

    private void verifyThatMapIsComplete() {
        for (int x = 1; x <= getMaxX(); x++) {
            for (int y = 1; y <= getMaxY(); y++) {
                Terrain terrain = getTerrainAt(Location.of(x, y));
                Preconditions.checkNotNull(terrain);
            }
        }
    }

    /**
     * Get max X location.
     * 
     * @return max X location
     */
    public int getMaxX() {
        return maxX;
    }

    /**
     * Get max Y location.
     * 
     * @return max Y location
     */
    public int getMaxY() {
        return maxY;
    }

    public TerrainType getTerrainTypeAt(final Location location) {
        Preconditions.checkArgument(isValid(location), "Location (%s) is not part of this map.",
                location);

        TerrainType terrain = terrainMap.get(location);

        return terrain != null ? terrain : TerrainType.OCEAN;
    }

    public Terrain getTerrainAt(final Location location) {
        Preconditions.checkArgument(isValid(location), "Location (%s) is not part of this map.",
                location);
        final Terrain out = new Terrain(location, getTerrainTypeAt(location));
        out.setHasTrees(trees.contains(location));
        return out;
    }

    public boolean isValid(final Location location) {
        Preconditions.checkNotNull(location);

        return location.getX() >= 1 && location.getX() <= getMaxX() && location.getY() >= 1
                && location.getY() <= getMaxY();
    }

    public boolean isValid(final Path path) {
        Preconditions.checkNotNull(path);

        return !path.getLocations().stream().anyMatch(location -> !isValid(location));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("maxX", maxX).add("maxY", maxY)
                .add("landmass", terrainMap.keySet().size()).toString();
    }

    void save(final ModelPo gamePo) {
        gamePo.getMap().setMaxX(maxX);
        gamePo.getMap().setMaxY(maxY);
        gamePo.getMap().setTerrainType(terrainMap);
        gamePo.getMap().setTrees(trees);
        gamePo.getMap().setSeed(seed);
        gamePo.getMap().setVisibility(new VisibilityPo());
    }

    /**
     * Seed number for pseudo random function that set game features in random
     * way. Numbers will look random but still two instances of map will be
     * same. All map location should have generated random number. This number
     * helps to determine which type of tree to show.
     * 
     * @return return map constant pseudo random seed number
     */
    public Integer getSeed() {
        return seed;
    }

}
