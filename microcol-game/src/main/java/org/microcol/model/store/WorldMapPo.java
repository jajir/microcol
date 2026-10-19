package org.microcol.model.store;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;

import org.microcol.model.Location;
import org.microcol.model.TerrainType;

public final class WorldMapPo extends AbstractMapStore {

    /**
     * Hold tile definitions rows.
     */
    private String[] tiles;

    /**
     * Hold information where are trees.
     */
    private String[] trees;

    /**
     * Hold information where are fields to grow crop.
     */
    private String[] fields;

    /**
     * Hold information where pioneers permanently cleared a forest. A missing
     * block means no square was cleared.
     */
    private String[] cleared;

    /**
     * Hold information where pioneers built a road. A missing block means no
     * square has a road.
     */
    private String[] roads;

    private VisibilityPo visibility;

    private int maxX;

    private int maxY;

    private Integer seed;

    public void setTerrainType(final Map<Location, TerrainType> terrainMap) {
        tiles = generateString(loc -> {
            final TerrainType terrainType = terrainMap.get(loc);
            if (terrainType == null) {
                return TerrainType.OCEAN.getCode();
            } else {
                return terrainType.getCode();
            }
        }, maxX, maxY);
    }

    public void setTrees(final Set<Location> treesSet) {
        trees = generateString(loc -> {
            final boolean hasTree = treesSet.contains(loc);
            if (hasTree) {
                return "t";
            } else {
                return "-";
            }
        }, maxX, maxY);
    }

    public void setFields(final Set<Location> fieldSet) {
        fields = generateString(loc -> {
            final boolean hasField = fieldSet.contains(loc);
            if (hasField) {
                return "f";
            } else {
                return "-";
            }
        }, maxX, maxY);
    }

    public void setCleared(final Set<Location> clearedSet) {
        cleared = generateString(loc -> clearedSet.contains(loc) ? "c" : "-",
                maxX, maxY);
    }

    public void setRoads(final Set<Location> roadSet) {
        roads = generateString(loc -> roadSet.contains(loc) ? "r" : "-", maxX,
                maxY);
    }

    public Map<Location, TerrainType> getTerrainMap() {
        Preconditions.checkArgument(
                tiles != null && tiles.length == maxY + 1 && tiles[0] != null,
                "Invalid terrain row count");
        for (int y = 1; y <= maxY; y++) {
            final String row = tiles[y];
            Preconditions.checkArgument(
                    row != null
                            && row.startsWith(
                                    String.format("row-%04d:", y % 10000))
                            && row.substring("row-0001:".length()).split(",",
                                    -1).length == maxX,
                    "Invalid terrain row %s", y);
        }
        final Map<Location, TerrainType> out = new HashMap<>();
        iterate(tiles, (location, charCode) -> {
            final TerrainType terrainType = TerrainType.valueOfCode(charCode);
            if (!terrainType.equals(TerrainType.OCEAN)) {
                out.put(location, terrainType);
            }
        });
        return out;
    }

    public Set<Location> getTreeSet() {
        final Set<Location> out = new HashSet<>();
        if (trees != null) {
            iterate(trees, (location, charCode) -> {
                if (charCode.equals("t")) {
                    out.add(location);
                }
            });
        }
        return out;
    }

    public Set<Location> getFieldSet() {
        final Set<Location> out = new HashSet<>();
        if (fields != null) {
            iterate(fields, (location, charCode) -> {
                if (charCode.equals("f")) {
                    out.add(location);
                }
            });
        }
        return out;
    }

    public Set<Location> getClearedSet() {
        final Set<Location> out = new HashSet<>();
        // A missing block means nothing was cleared; a present one must be
        // complete and use only the known codes.
        if (cleared != null) {
            Preconditions.checkArgument(
                    cleared.length == maxY + 1 && cleared[0] != null,
                    "Invalid cleared row count");
            for (int y = 1; y <= maxY; y++) {
                final String row = cleared[y];
                Preconditions.checkArgument(row != null
                        && row.startsWith(String.format("row-%04d:", y % 10000))
                        && row.substring("row-0001:".length()).split(",",
                                -1).length == maxX,
                        "Invalid cleared row %s", y);
            }
            iterate(cleared, (location, charCode) -> {
                Preconditions.checkArgument(
                        charCode.equals("c") || charCode.equals("-"),
                        "Unknown cleared code '%s'", charCode);
                if (charCode.equals("c")) {
                    out.add(location);
                }
            });
        }
        return out;
    }

    public int getMaxX() {
        return maxX;
    }

    public void setMaxX(final int maxX) {
        this.maxX = maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    public void setMaxY(int maxY) {
        this.maxY = maxY;
    }

    public String[] getTiles() {
        return tiles;
    }

    public String[] getTrees() {
        return trees;
    }

    public void setTrees(final String[] trees) {
        this.trees = trees;
    }

    /**
     * @return the seed
     */
    public Integer getSeed() {
        return seed;
    }

    /**
     * @param seed the seed to set
     */
    public void setSeed(Integer seed) {
        this.seed = seed;
    }

    /**
     * @return the visibility
     */
    public VisibilityPo getVisibility() {
        return visibility;
    }

    /**
     * @param visibility the visibility to set
     */
    public void setVisibility(VisibilityPo visibility) {
        this.visibility = visibility;
    }

    public String[] getFields() {
        return fields;
    }

    public void setFields(final String[] fields) {
        this.fields = fields;
    }

    public String[] getCleared() {
        return cleared;
    }

    public void setCleared(final String[] cleared) {
        this.cleared = cleared;
    }

    public Set<Location> getRoadSet() {
        final Set<Location> out = new HashSet<>();
        // A missing block means no road; a present one must be complete and
        // use only the known codes.
        if (roads != null) {
            Preconditions.checkArgument(
                    roads.length == maxY + 1 && roads[0] != null,
                    "Invalid road row count");
            for (int y = 1; y <= maxY; y++) {
                final String row = roads[y];
                Preconditions.checkArgument(
                        row != null
                                && row.startsWith(
                                        String.format("row-%04d:", y % 10000))
                                && row.substring("row-0001:".length())
                                        .split(",", -1).length == maxX,
                        "Invalid road row %s", y);
            }
            iterate(roads, (location, charCode) -> {
                Preconditions.checkArgument(
                        charCode.equals("r") || charCode.equals("-"),
                        "Unknown road code '%s'", charCode);
                if (charCode.equals("r")) {
                    out.add(location);
                }
            });
        }
        return out;
    }

    public String[] getRoads() {
        return roads;
    }

    public void setRoads(final String[] roads) {
        this.roads = roads;
    }

}
