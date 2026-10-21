package org.microcol.model.store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.MineralReserve;
import org.microcol.model.ResourceOverlay;
import org.microcol.model.RiverSize;
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

    /**
     * Hold information where an unresolved lost-city rumor waits. A missing
     * block means no rumor is left.
     */
    private String[] rumors;

    /**
     * Hold size of the river on each land square. A missing block means no
     * river.
     */
    private String[] rivers;

    /**
     * Hold course segments joining neighbouring river squares. A missing block
     * means no course.
     */
    private List<RiverSegmentPo> riverSegments;

    /**
     * Hold the special resource on each square. A missing block means no square
     * has one.
     */
    private String[] resources;

    /**
     * Hold the remaining units of every finite deposit. A missing block means
     * no square has a finite deposit.
     */
    private List<MineralReservePo> mineralReserves;

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

    public void setRumors(final Set<Location> rumorSet) {
        rumors = generateString(loc -> rumorSet.contains(loc) ? "l" : "-", maxX,
                maxY);
    }

    public String[] getRumors() {
        return rumors;
    }

    public void setRumors(final String[] rumors) {
        this.rumors = rumors;
    }

    public Set<Location> getRumorSet() {
        final Set<Location> out = new HashSet<>();
        // A missing block means no rumor is left; a present one must be
        // complete and use only the known codes.
        if (rumors != null) {
            Preconditions.checkArgument(
                    rumors.length == maxY + 1 && rumors[0] != null,
                    "Invalid rumor row count");
            for (int y = 1; y <= maxY; y++) {
                final String row = rumors[y];
                Preconditions.checkArgument(
                        row != null
                                && row.startsWith(
                                        String.format("row-%04d:", y % 10000))
                                && row.substring("row-0001:".length())
                                        .split(",", -1).length == maxX,
                        "Invalid rumor row %s", y);
            }
            iterate(rumors, (location, charCode) -> {
                Preconditions.checkArgument(
                        charCode.equals("l") || charCode.equals("-"),
                        "Unknown rumor code '%s'", charCode);
                if (charCode.equals("l")) {
                    out.add(location);
                }
            });
        }
        return out;
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

    public void setRivers(final Map<Location, RiverSize> riverMap) {
        rivers = generateString(
                loc -> riverMap.getOrDefault(loc, RiverSize.NONE).getCode(),
                maxX, maxY);
    }

    /**
     * @return size of the river on every square that has one
     */
    public Map<Location, RiverSize> getRiverMap() {
        final Map<Location, RiverSize> out = new HashMap<>();
        // A missing block means no river; a present one must be complete and
        // use only the known codes.
        if (rivers != null) {
            Preconditions.checkArgument(
                    rivers.length == maxY + 1 && rivers[0] != null,
                    "Invalid river row count");
            for (int y = 1; y <= maxY; y++) {
                final String row = rivers[y];
                Preconditions.checkArgument(
                        row != null
                                && row.startsWith(
                                        String.format("row-%04d:", y % 10000))
                                && row.substring("row-0001:".length())
                                        .split(",", -1).length == maxX,
                        "Invalid river row %s", y);
            }
            iterate(rivers, (location, charCode) -> {
                final RiverSize size = RiverSize.valueOfCode(charCode);
                if (size != RiverSize.NONE) {
                    out.put(location, size);
                }
            });
        }
        return out;
    }

    /**
     * @return saved course segments, empty when the block is missing
     */
    public List<RiverSegmentPo> getRiverSegments() {
        return riverSegments != null ? riverSegments : ImmutableList.of();
    }

    public void setRiverSegments(final List<RiverSegmentPo> riverSegments) {
        this.riverSegments = riverSegments;
    }

    public void setResources(final Map<Location, ResourceOverlay> resourceMap) {
        resources = generateString(loc -> resourceMap
                .getOrDefault(loc, ResourceOverlay.NONE).getCode(), maxX, maxY);
    }

    /**
     * Save remaining units in row order, one entry for each finite deposit.
     *
     * @param reserveMap remaining units of every finite deposit
     */
    public void setMineralReserves(
            final Map<Location, MineralReserve> reserveMap) {
        final List<MineralReservePo> out = new ArrayList<>();
        for (int y = 1; y <= maxY; y++) {
            for (int x = 1; x <= maxX; x++) {
                final Location at = Location.of(x, y);
                final MineralReserve reserve = reserveMap.get(at);
                if (reserve != null) {
                    out.add(new MineralReservePo(at,
                            reserve.getRemaining(GoodsType.SILVER),
                            reserve.getRemaining(GoodsType.ORE)));
                }
            }
        }
        mineralReserves = out;
    }

    /**
     * Every saved entry must name its square and both counts. Its range is
     * checked against the deposit when the world map is built.
     *
     * @return remaining units of every saved deposit, empty when the block is
     *         missing
     */
    public Map<Location, MineralReserve> getMineralReserveMap() {
        final Map<Location, MineralReserve> out = new HashMap<>();
        if (mineralReserves != null) {
            for (final MineralReservePo saved : mineralReserves) {
                Preconditions.checkArgument(
                        saved.getLocation() != null && saved.getSilver() != null
                                && saved.getOre() != null,
                        "Mineral reserve must name its square, silver and ore.");
                Preconditions.checkArgument(
                        !out.containsKey(saved.getLocation()),
                        "Duplicate mineral reserve (%s).", saved.getLocation());
                out.put(saved.getLocation(),
                        MineralReserve.of(saved.getSilver(), saved.getOre()));
            }
        }
        return out;
    }

    /**
     * @return special resource of every square that has one
     */
    public Map<Location, ResourceOverlay> getResourceMap() {
        final Map<Location, ResourceOverlay> out = new HashMap<>();
        // A missing block means no resource; a present one must be complete and
        // use only the known codes.
        if (resources != null) {
            Preconditions.checkArgument(
                    resources.length == maxY + 1 && resources[0] != null,
                    "Invalid resource row count");
            for (int y = 1; y <= maxY; y++) {
                final String row = resources[y];
                Preconditions.checkArgument(row != null
                        && row.startsWith(String.format("row-%04d:", y % 10000))
                        && row.substring("row-0001:".length()).split(",",
                                -1).length == maxX,
                        "Invalid resource row %s", y);
            }
            iterate(resources, (location, charCode) -> {
                final ResourceOverlay overlay = ResourceOverlay
                        .valueOfCode(charCode);
                if (overlay != ResourceOverlay.NONE) {
                    out.put(location, overlay);
                }
            });
        }
        return out;
    }

}
