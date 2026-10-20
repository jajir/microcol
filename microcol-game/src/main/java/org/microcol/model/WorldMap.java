package org.microcol.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.microcol.model.store.ModelPo;
import org.microcol.model.store.RiverSegmentPo;
import org.microcol.model.store.VisibilityPo;
import org.microcol.model.store.WorldMapPo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class WorldMap {

    private final int maxX;
    private final int maxY;

    private final ImmutableMap<Location, TerrainType> terrainMap;

    private final Set<Location> trees;

    private final Set<Location> fields;

    private final Set<Location> cleared;

    private final Set<Location> roads;

    private final ImmutableMap<Location, RiverSize> rivers;

    /**
     * Course segments; each one is the unordered pair of its two squares.
     */
    private final Set<Set<Location>> riverSegments;

    private final ImmutableMap<Location, ResourceOverlay> resources;

    /**
     * Remaining units of every finite deposit. Only the owner of the map
     * changes them, through {@link #extractMinerals(Map)}.
     */
    private final Map<Location, MineralReserve> mineralReserves;

    private final Integer seed;

    public WorldMap(final ModelPo gamePo) {
        final WorldMapPo worldMapPo = gamePo.getMap();
        this.maxX = worldMapPo.getMaxX();
        this.maxY = worldMapPo.getMaxY();
        Preconditions.checkArgument(maxY >= 1, "Max Y (%s) must be positive.",
                maxY);
        Preconditions.checkArgument(maxX >= 1, "Max X (%s) must be positive.",
                maxX);
        this.terrainMap = ImmutableMap.copyOf(worldMapPo.getTerrainMap());
        this.trees = worldMapPo.getTreeSet();
        this.fields = worldMapPo.getFieldSet();
        this.cleared = worldMapPo.getClearedSet();
        this.roads = worldMapPo.getRoadSet();
        this.rivers = ImmutableMap.copyOf(worldMapPo.getRiverMap());
        this.riverSegments = new HashSet<>();
        this.seed = Preconditions.checkNotNull(worldMapPo.getSeed(),
                "Seed value is null");
        for (final Location at : cleared) {
            Preconditions.checkArgument(!trees.contains(at),
                    "Cleared square (%s) still has a forest.", at);
        }
        for (final Location at : roads) {
            Preconditions.checkArgument(getTerrainTypeAt(at).isLand(),
                    "Road square (%s) must be land.", at);
        }
        for (final Location at : rivers.keySet()) {
            Preconditions.checkArgument(getTerrainTypeAt(at).isLand(),
                    "River square (%s) must be land.", at);
        }
        for (final RiverSegmentPo segment : worldMapPo.getRiverSegments()) {
            verifyRiverSegment(segment);
        }
        this.resources = ImmutableMap.copyOf(worldMapPo.getResourceMap());
        for (final Map.Entry<Location, ResourceOverlay> entry : resources
                .entrySet()) {
            Preconditions.checkArgument(
                    entry.getValue().canBeOn(getTerrainTypeAt(entry.getKey())),
                    "Resource square (%s) can't carry %s.", entry.getKey(),
                    entry.getValue());
        }
        this.mineralReserves = new HashMap<>(worldMapPo.getMineralReserveMap());
        verifyMineralReserves();
        verifyThatMapIsComplete();
    }

    /**
     * Every finite deposit needs exactly one saved reserve, and every saved
     * reserve must belong to a finite deposit and fit its initial units.
     */
    private void verifyMineralReserves() {
        for (final Map.Entry<Location, MineralReserve> entry : mineralReserves
                .entrySet()) {
            final ResourceOverlay overlay = getResourceOverlayAt(
                    entry.getKey());
            Preconditions.checkArgument(
                    overlay.isFinite()
                            && entry.getValue().isAllowedFor(overlay),
                    "Mineral reserve of square (%s) does not fit its deposit.",
                    entry.getKey());
        }
        for (final Map.Entry<Location, ResourceOverlay> entry : resources
                .entrySet()) {
            Preconditions.checkArgument(
                    !entry.getValue().isFinite()
                            || mineralReserves.containsKey(entry.getKey()),
                    "Deposit square (%s) needs a saved mineral reserve.",
                    entry.getKey());
        }
    }

    /**
     * Check one saved course segment and remember it. Both ends must be known
     * river squares on this map and must be distinct neighbours.
     *
     * @param segment required saved segment
     */
    private void verifyRiverSegment(final RiverSegmentPo segment) {
        final Location from = segment.getFrom();
        final Location to = segment.getTo();
        Preconditions.checkArgument(from != null && to != null,
                "River segment must name two squares.");
        Preconditions.checkArgument(isValid(from) && isValid(to),
                "River segment (%s, %s) must be part of this map.", from, to);
        Preconditions.checkArgument(from.isNeighbor(to),
                "River segment (%s, %s) must join neighbour squares.", from,
                to);
        Preconditions.checkArgument(
                rivers.containsKey(from) && rivers.containsKey(to),
                "River segment (%s, %s) must join squares with a river.", from,
                to);
        Preconditions.checkArgument(
                riverSegments.add(ImmutableSet.of(from, to)),
                "Duplicate river segment (%s, %s).", from, to);
    }

    private void verifyThatMapIsComplete() {
        for (int x = 1; x <= getMaxLocationX(); x++) {
            for (int y = 1; y <= getMaxLocationY(); y++) {
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
    public int getMaxLocationX() {
        return maxX;
    }

    /**
     * Get max Y location.
     *
     * @return max Y location
     */
    public int getMaxLocationY() {
        return maxY;
    }

    /**
     * Return map size. Valid map location will be from 1 up to getMapSize()
     * Including x and y values of {@link #getMapSize()}
     *
     * @return return map size
     */
    public Location getMapSize() {
        return Location.of(maxX, maxY);
    }

    public TerrainType getTerrainTypeAt(final Location location) {
        Preconditions.checkArgument(isValid(location),
                "Location (%s) is not part of this map.", location);

        TerrainType terrain = terrainMap.get(location);

        return terrain != null ? terrain : TerrainType.OCEAN;
    }

    public Terrain getTerrainAt(final Location location) {
        Preconditions.checkArgument(isValid(location),
                "Location (%s) is not part of this map.", location);
        final Terrain out = new Terrain(location, getTerrainTypeAt(location));
        out.setHasTrees(trees.contains(location));
        out.setHasField(fields.contains(location));
        out.setCleared(cleared.contains(location));
        out.setHasRoad(roads.contains(location));
        out.setRiverSize(getRiverSizeAt(location));
        out.setResourceOverlay(getResourceOverlayAt(location));
        out.setMineralReserve(
                mineralReserves.getOrDefault(location, MineralReserve.EMPTY));
        return out;
    }

    /**
     * @param location required location
     * @return {@code true} when the square carries a forest, which changes its
     *         movement cost
     */
    public boolean hasTreesAt(final Location location) {
        Preconditions.checkNotNull(location);
        return trees.contains(location);
    }

    /**
     * @param location required location
     * @return {@code true} when a road has been built on the square
     */
    public boolean hasRoadAt(final Location location) {
        Preconditions.checkNotNull(location);
        return roads.contains(location);
    }

    /**
     * @param location required location
     * @return size of the river on the square, {@link RiverSize#NONE} when none
     */
    public RiverSize getRiverSizeAt(final Location location) {
        Preconditions.checkNotNull(location);
        return rivers.getOrDefault(location, RiverSize.NONE);
    }

    /**
     * @param location required location
     * @return special resource on the square, {@link ResourceOverlay#NONE} when
     *         there is none
     */
    public ResourceOverlay getResourceOverlayAt(final Location location) {
        Preconditions.checkNotNull(location);
        return resources.getOrDefault(location, ResourceOverlay.NONE);
    }

    /**
     * @param location  required location
     * @param goodsType required goods type
     * @return bonus units of that goods type left in the deposit on the square,
     *         zero when there is no finite deposit
     */
    public int getMineralRemainingAt(final Location location,
            final GoodsType goodsType) {
        Preconditions.checkNotNull(location);
        Preconditions.checkNotNull(goodsType);
        return mineralReserves.getOrDefault(location, MineralReserve.EMPTY)
                .getRemaining(goodsType);
    }

    /**
     * Spend the finite deposit output of one update. Every extraction is
     * checked before any reserve changes.
     *
     * @param extractions required map from deposit square to the goods taken
     *                    from its reserve
     */
    void extractMinerals(final Map<Location, Goods> extractions) {
        Preconditions.checkNotNull(extractions);
        for (final Map.Entry<Location, Goods> entry : extractions.entrySet()) {
            final Location at = entry.getKey();
            final Goods goods = entry.getValue();
            Preconditions.checkArgument(
                    mineralReserves.containsKey(at)
                            && getResourceOverlayAt(at).getOutputs()
                                    .contains(goods.getType())
                            && mineralReserves.get(at).canExtract(
                                    goods.getType(), goods.getAmount()),
                    "Square (%s) can't give %s from its mineral reserve.", at,
                    goods);
        }
        for (final Map.Entry<Location, Goods> entry : extractions.entrySet()) {
            final MineralReserve reserve = mineralReserves.get(entry.getKey());
            mineralReserves.put(entry.getKey(), reserve.extract(
                    entry.getValue().getType(), entry.getValue().getAmount()));
        }
    }

    /**
     * @param from required square
     * @param to   required neighbouring square
     * @return {@code true} when a river course joins the two squares, in either
     *         direction
     */
    public boolean hasRiverSegment(final Location from, final Location to) {
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(to);
        return riverSegments.contains(ImmutableSet.of(from, to));
    }

    /**
     * @param location required location
     * @return squares joined to this one by a river course
     */
    public Set<Location> getRiverLinksAt(final Location location) {
        Preconditions.checkNotNull(location);
        return riverSegments.stream()
                .filter(segment -> segment.contains(location))
                .flatMap(Set::stream).filter(other -> !other.equals(location))
                .collect(ImmutableSet.toImmutableSet());
    }

    public boolean isValid(final Location location) {
        Preconditions.checkNotNull(location);

        return location.getX() >= 1 && location.getX() <= getMaxLocationX()
                && location.getY() >= 1 && location.getY() <= getMaxLocationY();
    }

    public boolean isValid(final Path path) {
        Preconditions.checkNotNull(path);

        return !path.getLocations().stream()
                .anyMatch(location -> !isValid(location));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("maxX", maxX)
                .add("maxY", maxY).add("landmass", terrainMap.keySet().size())
                .toString();
    }

    void save(final ModelPo gamePo) {
        gamePo.getMap().setMaxX(maxX);
        gamePo.getMap().setMaxY(maxY);
        gamePo.getMap().setTerrainType(terrainMap);
        gamePo.getMap().setTrees(trees);
        gamePo.getMap().setFields(fields);
        gamePo.getMap().setCleared(cleared);
        gamePo.getMap().setRoads(roads);
        gamePo.getMap().setRivers(rivers);
        gamePo.getMap().setRiverSegments(riverSegmentsToSave());
        gamePo.getMap().setResources(resources);
        gamePo.getMap().setMineralReserves(mineralReserves);
        gamePo.getMap().setSeed(seed);
        gamePo.getMap().setVisibility(new VisibilityPo());
    }

    /**
     * Every course once, so a shared segment is not saved twice.
     */
    private List<RiverSegmentPo> riverSegmentsToSave() {
        return riverSegments.stream().map(segment -> {
            final List<Location> ends = ImmutableList.copyOf(segment);
            return new RiverSegmentPo(ends.get(0), ends.get(1));
        }).collect(ImmutableList.toImmutableList());
    }

    public void plowFiled(final Location at) {
        Preconditions.checkArgument(getTerrainTypeAt(at).isCanHaveField(),
                "Terrain '%s' at '%s' can have field.", getTerrainTypeAt(at),
                at);
        fields.add(at);
    }

    /**
     * Remove the forest at a square for good. The square keeps its bare terrain
     * yields except that it never again produces lumber or fur.
     *
     * @param at forested square
     */
    public void clearForest(final Location at) {
        Preconditions.checkArgument(trees.contains(at),
                "There is no forest to clear at '%s'.", at);
        trees.remove(at);
        cleared.add(at);
    }

    /**
     * Record a finished road. Sea squares never take a road.
     *
     * @param at land square without a road
     */
    public void buildRoad(final Location at) {
        Preconditions.checkArgument(getTerrainTypeAt(at).isLand(),
                "Terrain '%s' at '%s' cannot have road.", getTerrainTypeAt(at),
                at);
        Preconditions.checkArgument(!roads.contains(at),
                "There is already a road at '%s'.", at);
        roads.add(at);
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
