package org.akropia.model.generation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.akropia.model.Location;
import org.akropia.model.MineralReserve;
import org.akropia.model.ResourceOverlay;
import org.akropia.model.RiverSize;
import org.akropia.model.TerrainType;
import org.akropia.model.store.RiverSegmentPo;
import org.akropia.model.store.WorldMapPo;

/**
 * Terrain of one generated world: land masses, islands, the eastern high-sea
 * lane, latitude-sensitive terrain, forests, rivers and resources. Every random
 * draw follows a fixed order, so one seed always gives the same map.
 */
final class GeneratedWorldLayout {

    /** Width in squares of the open high-sea lane on the east edge. */
    static final int SEA_LANE_WIDTH = 4;

    /** Land stays off the map edge and off the high-sea lane. */
    private static final int LAND_MIN = 2;

    private final int LAND_MAX_X;

    private final int LAND_MAX_Y;

    /** Share of the land that forms islands; the rest forms the main masses. */
    private static final double ISLAND_SHARE = 0.20;

    private static final int MIN_ISLAND = 3;

    private static final int MAX_ISLAND = 12;

    /** Random squares tried around one island before it is abandoned. */
    private static final int ISLAND_DRAWS = 400;

    private static final int ISLAND_ATTEMPTS = 4000;

    /** Main masses start at least this far apart. */
    private static final int SEED_SPACING = 6;

    /** Share of the land that is the highest ground, mountains first. */
    private static final double MOUNTAIN_SHARE = 0.04;

    private static final double HILL_SHARE = 0.10;

    /** Moisture from which forests are likely. */
    private static final double WET = 0.5;

    private static final double WET_FOREST_CHANCE = 0.55;

    private static final double DRY_FOREST_CHANCE = 0.20;

    /** Bound on random growth steps, so generation always ends. */
    private static final int GROWTH_DRAWS = 2_000_000;

    private final GeneratedWorldSettings settings;

    private final Random random;

    private final Map<Location, TerrainType> types = new HashMap<>();

    private final GeneratedGrid grid;

    private LatticeNoise elevation;

    private LatticeNoise moisture;

    private GeneratedWorldLayout(final GeneratedWorldSettings settings,
            final Random random) {
        this.settings = settings;
        this.random = random;
        LAND_MAX_X = settings.getWidth() - SEA_LANE_WIDTH - 2;
        LAND_MAX_Y = settings.getHeight() - 1;
        grid = new GeneratedGrid(types, settings.getWidth(),
                settings.getHeight());
    }

    /**
     * @param settings required generation settings
     * @param random   required generator of this attempt
     * @return terrain, forests, rivers and resources of the world, without
     *         units or native settlements
     * @throws IllegalStateException when this attempt cannot fit the land
     */
    static WorldMapPo generate(final GeneratedWorldSettings settings,
            final Random random) {
        return new GeneratedWorldLayout(settings, random).build();
    }

    private WorldMapPo build() {
        final int landTarget = settings.getLandShare()
                .landSquares(settings.getWidth() * settings.getHeight());
        final int islandTarget = (int) Math.round(landTarget * ISLAND_SHARE);
        final Set<Location> land = new HashSet<>();
        if (settings.getLayout() == null) {
            growMainMasses(landTarget - islandTarget, land);
            placeIslands(islandTarget, land);
        } else {
            growSeparatedMasses(landTarget, land);
        }
        elevation = new LatticeNoise(random, settings.getWidth(),
                settings.getHeight());
        moisture = new LatticeNoise(random, settings.getWidth(),
                settings.getHeight());
        assignTerrain(land);
        for (int x = settings.getWidth() - SEA_LANE_WIDTH + 1; x <= settings
                .getWidth(); x++) {
            for (int y = 1; y <= settings.getHeight(); y++) {
                types.put(Location.of(x, y), TerrainType.HIGH_SEA);
            }
        }
        final Set<Location> trees = assignTrees(land);
        final List<List<Location>> rivers = GeneratedWorldRivers.trace(grid,
                elevation, random);
        final Set<Location> riverSquares = new HashSet<>();
        rivers.forEach(riverSquares::addAll);
        trees.removeAll(riverSquares);
        final Map<Location, ResourceOverlay> resources = GeneratedWorldResources
                .assign(grid, trees, riverSquares, random);
        return toMap(trees, rivers, resources);
    }

    /**
     * Grow bounded connected groups; two water squares separate neighbouring
     * regions.
     */
    private void growSeparatedMasses(final int target,
            final Set<Location> land) {
        int columns = 1;
        int rows = 1;
        if (settings.getLayout() == GeneratedLandLayout.BIG_ISLANDS) {
            columns = 2;
            rows = 2;
        } else if (settings.getLayout() == GeneratedLandLayout.ARCHIPELAGO) {
            columns = Math.max(3, settings.getWidth() / 24);
            rows = Math.max(2, settings.getHeight() / 24);
        }
        final int count = columns * rows;
        final int width = LAND_MAX_X - LAND_MIN + 1;
        final int height = LAND_MAX_Y - LAND_MIN + 1;
        for (int index = 0; index < count; index++) {
            final int column = index % columns;
            final int row = index / columns;
            final int left = LAND_MIN + column * width / columns;
            final int right = LAND_MIN + (column + 1) * width / columns - 3;
            final int top = LAND_MIN + row * height / rows;
            final int bottom = LAND_MIN + (row + 1) * height / rows - 3;
            final int quota = target / count + (index < target % count ? 1 : 0);
            final List<Location> frontier = new ArrayList<>();
            final Set<Location> queued = new HashSet<>();
            frontier.add(Location.of((left + right) / 2 + random.nextInt(3) - 1,
                    (top + bottom) / 2 + random.nextInt(3) - 1));
            queued.addAll(frontier);
            for (int placed = 0; placed < quota; placed++) {
                if (frontier.isEmpty()) {
                    throw new IllegalStateException("Land region exhausted");
                }
                final int chosen = random.nextInt(frontier.size());
                final Location at = frontier.get(chosen);
                frontier.set(chosen, frontier.get(frontier.size() - 1));
                frontier.remove(frontier.size() - 1);
                land.add(at);
                for (final int[] step : GeneratedGrid.STEPS) {
                    final Location next = Location.of(at.getX() + step[0],
                            at.getY() + step[1]);
                    if (next.getX() >= left && next.getX() <= right
                            && next.getY() >= top && next.getY() <= bottom
                            && queued.add(next)) {
                        frontier.add(next);
                    }
                }
            }
        }
    }

    /**
     * Grow one to three connected main masses until they hold the target number
     * of land squares. Every step joins an existing square of a mass.
     */
    private void growMainMasses(final int target, final Set<Location> land) {
        final List<Location> candidates = new ArrayList<>();
        for (int x = LAND_MIN; x <= LAND_MAX_X; x++) {
            for (int y = LAND_MIN; y <= LAND_MAX_Y; y++) {
                candidates.add(Location.of(x, y));
            }
        }
        Collections.shuffle(candidates, random);
        final int masses = 1 + random.nextInt(3);
        final List<Location> cells = new ArrayList<>();
        for (final Location candidate : candidates) {
            if (cells.size() == masses) {
                break;
            }
            if (cells.stream().allMatch(seed -> seed
                    .getDistanceChebyshev(candidate) >= SEED_SPACING)) {
                cells.add(candidate);
            }
        }
        if (cells.size() < masses) {
            throw new IllegalStateException("No room for the main masses");
        }
        // Each mass grows from its own squares toward an equal share, so no
        // mass stays a stray patch.
        for (int i = 0; i < masses; i++) {
            final List<Location> mass = new ArrayList<>(List.of(cells.get(i)));
            land.add(cells.get(i));
            final int quota = target / masses + (i < target % masses ? 1 : 0);
            int draws = 0;
            while (mass.size() < quota) {
                if (++draws > GROWTH_DRAWS) {
                    throw new IllegalStateException(
                            "Main masses did not reach the land target");
                }
                final Location from = mass.get(random.nextInt(mass.size()));
                final int[] step = GeneratedGrid.STEPS[random
                        .nextInt(GeneratedGrid.STEPS.length)];
                final Location next = Location.of(from.getX() + step[0],
                        from.getY() + step[1]);
                if (isLandArea(next) && land.add(next)) {
                    mass.add(next);
                }
            }
        }
    }

    /**
     * Place islands of three to twelve squares, the last one taking the rest.
     * An island keeps at least one water square between it and any other land.
     */
    private void placeIslands(final int target, final Set<Location> land) {
        int remaining = target;
        int attempts = 0;
        while (remaining > 0) {
            if (++attempts > ISLAND_ATTEMPTS) {
                throw new IllegalStateException(
                        "Islands did not fit beside the main masses");
            }
            final int size = remaining <= MAX_ISLAND ? remaining
                    : MIN_ISLAND + random.nextInt(MAX_ISLAND - MIN_ISLAND + 1);
            final Set<Location> island = growIsland(size, land);
            if (island != null) {
                land.addAll(island);
                remaining -= size;
            }
        }
    }

    /**
     * @return exactly {@code size} new squares forming one island, or null when
     *         the random seed or growth does not fit
     */
    private Set<Location> growIsland(final int size, final Set<Location> land) {
        final Location seed = Location.of(
                LAND_MIN + random.nextInt(LAND_MAX_X - LAND_MIN + 1),
                LAND_MIN + random.nextInt(LAND_MAX_Y - LAND_MIN + 1));
        if (land.contains(seed) || !isMoatClear(seed, land)) {
            return null;
        }
        final Set<Location> island = new HashSet<>();
        island.add(seed);
        final List<Location> cells = new ArrayList<>(island);
        int draws = 0;
        while (island.size() < size && draws++ < ISLAND_DRAWS) {
            final Location from = cells.get(random.nextInt(cells.size()));
            final int[] step = GeneratedGrid.STEPS[random
                    .nextInt(GeneratedGrid.STEPS.length)];
            final Location next = Location.of(from.getX() + step[0],
                    from.getY() + step[1]);
            if (isLandArea(next) && !land.contains(next)
                    && !island.contains(next) && isMoatClear(next, land)) {
                island.add(next);
                cells.add(next);
            }
        }
        return island.size() == size ? island : null;
    }

    /** @return {@code true} when no other land touches the square */
    private static boolean isMoatClear(final Location at,
            final Set<Location> land) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if ((dx != 0 || dy != 0) && land.contains(
                        Location.of(at.getX() + dx, at.getY() + dy))) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isLandArea(final Location at) {
        return at.getX() >= LAND_MIN && at.getX() <= LAND_MAX_X
                && at.getY() >= LAND_MIN && at.getY() <= LAND_MAX_Y;
    }

    /**
     * Highest squares become mountains, the next band hills, and the rest
     * follows the latitude band and moisture of its region.
     */
    private void assignTerrain(final Set<Location> land) {
        final List<Location> ranked = GeneratedGrid.sorted(land);
        // Stable sort: equal heights keep the row-major order.
        ranked.sort(Comparator
                .comparingDouble((Location at) -> elevation.at(at)).reversed());
        final int mountains = (int) Math.ceil(ranked.size() * MOUNTAIN_SHARE);
        final int hills = (int) Math.ceil(ranked.size() * HILL_SHARE);
        for (int i = 0; i < ranked.size(); i++) {
            final Location at = ranked.get(i);
            if (i < mountains) {
                types.put(at, TerrainType.MOUNTAIN);
            } else if (i < mountains + hills) {
                types.put(at, TerrainType.HILL);
            } else {
                types.put(at, baseTerrain(coldness(at), moisture.at(at)));
            }
        }
    }

    /**
     * @return normalised coldness of the square: 0 at the equator, 1 at the
     *         poles, shifted by the climate
     */
    private double coldness(final Location at) {
        final double latitude = Math
                .abs(at.getY() - (settings.getHeight() + 1) / 2.0)
                / ((settings.getHeight() - 1) / 2.0);
        return Math.min(1,
                Math.max(0, latitude + settings.getClimate().getColdShift()));
    }

    /**
     * @param coldness normalised coldness, 0 to 1
     * @param wet      moisture, 0 to 1
     * @return terrain of a square that is neither upland nor water
     */
    static TerrainType baseTerrain(final double coldness, final double wet) {
        if (coldness >= 0.85) {
            return TerrainType.ARCTIC;
        }
        if (coldness >= 0.60) {
            return TerrainType.TUNDRA;
        }
        if (coldness < 0.35) {
            if (wet >= 0.70) {
                return TerrainType.SWAMP;
            }
            if (wet >= 0.45) {
                return TerrainType.SAVANNAH;
            }
            return TerrainType.DESERT;
        }
        if (wet >= 0.70) {
            return TerrainType.MARSH;
        }
        if (wet >= 0.50) {
            return TerrainType.GRASSLAND;
        }
        if (wet >= 0.30) {
            return TerrainType.PRAIRIE;
        }
        return TerrainType.PLAINS;
    }

    private Set<Location> assignTrees(final Set<Location> land) {
        final Set<Location> trees = new HashSet<>();
        for (final Location at : GeneratedGrid.sorted(land)) {
            if (types.get(at).isCanHaveTree()) {
                final double chance = moisture.at(at) >= WET ? WET_FOREST_CHANCE
                        : DRY_FOREST_CHANCE;
                if (random.nextDouble() < chance) {
                    trees.add(at);
                }
            }
        }
        return trees;
    }

    private WorldMapPo toMap(final Set<Location> trees,
            final List<List<Location>> rivers,
            final Map<Location, ResourceOverlay> resources) {
        final Map<Location, RiverSize> riverSizes = new HashMap<>();
        final List<RiverSegmentPo> segments = new ArrayList<>();
        for (int i = 0; i < rivers.size(); i++) {
            final List<Location> river = rivers.get(i);
            final RiverSize size = i == 0 ? RiverSize.LARGE : RiverSize.SMALL;
            for (int j = 0; j < river.size(); j++) {
                riverSizes.put(river.get(j), size);
                if (j > 0) {
                    segments.add(
                            new RiverSegmentPo(river.get(j - 1), river.get(j)));
                }
            }
        }
        final Map<Location, MineralReserve> reserves = new HashMap<>();
        resources.forEach((at, overlay) -> {
            if (overlay.isFinite()) {
                reserves.put(at, MineralReserve.initial(overlay));
            }
        });
        final WorldMapPo map = new WorldMapPo();
        map.setMaxX(settings.getWidth());
        map.setMaxY(settings.getHeight());
        map.setSeed(settings.getSeed());
        map.setTerrainType(types);
        map.setTrees(trees);
        map.setFields(new HashSet<>());
        map.setRivers(riverSizes);
        map.setRiverSegments(segments);
        map.setResources(resources);
        map.setMineralReserves(reserves);
        return map;
    }

}
