package org.microcol.model.generation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

import org.microcol.model.Location;
import org.microcol.model.ResourceOverlay;
import org.microcol.model.TerrainType;

/**
 * Natural resources of a generated world. About {@link #RESOURCE_SHARE} of the
 * suitable squares receive one. Suitable squares are the land and the ocean
 * squares beside it. Each resource family takes its weighted share and keeps to
 * the terrain where it naturally occurs.
 */
final class GeneratedWorldResources {

    /** Share of suitable squares that receive a resource. */
    static final double RESOURCE_SHARE = 0.10;

    /** Chance that a mineral square yields both silver and ore. */
    private static final double MIXED_SHARE = 0.25;

    /** Chance that a grassland square is a tobacco rather than food field. */
    private static final double TOBACCO_SHARE = 0.5;

    /** Forest squares give timber, trapping or game in equal turns. */
    private static final ResourceOverlay[] FOREST_OVERLAYS = {
            ResourceOverlay.PRIME_TIMBER, ResourceOverlay.TRAPPING,
            ResourceOverlay.GAME };

    /** Resource families in their fixed placement order. */
    private enum Family {
        MINERAL(0.15), FOREST(0.30), OASIS(0.10), CROPS(0.25), FISHERY(0.20);

        private final double weight;

        Family(final double weight) {
            this.weight = weight;
        }
    }

    private GeneratedWorldResources() {
    }

    /**
     * @param grid   required terrain of the world
     * @param trees  required squares that carry a forest
     * @param random required generator of this attempt
     * @return resource of every square that receives one
     * @throws IllegalStateException when too few suitable squares exist
     */
    static Map<Location, ResourceOverlay> assign(final GeneratedGrid grid,
            final Set<Location> trees, final Set<Location> rivers,
            final Random random) {
        final List<Location> land = grid
                .squaresWith(terrain -> !terrain.isSea());
        land.removeAll(rivers);
        final List<Location> coast = new ArrayList<>();
        for (int y = 1; y <= GeneratedWorld.HEIGHT; y++) {
            for (int x = 1; x <= GeneratedWorld.WIDTH; x++) {
                final Location at = Location.of(x, y);
                if (grid.terrainAt(at) == TerrainType.OCEAN && grid
                        .neighbours(at).stream().anyMatch(grid::isLand)) {
                    coast.add(at);
                }
            }
        }
        final int target = (int) Math
                .round((land.size() + coast.size()) * RESOURCE_SHARE);
        final Map<Family, List<Location>> pools = new EnumMap<>(Family.class);
        pools.put(Family.MINERAL,
                filter(land, at -> grid.terrainAt(at) == TerrainType.HILL
                        || grid.terrainAt(at) == TerrainType.MOUNTAIN));
        pools.put(Family.FOREST, filter(land, trees::contains));
        pools.put(Family.OASIS,
                filter(land, at -> grid.terrainAt(at) == TerrainType.DESERT));
        pools.put(Family.CROPS,
                filter(land, at -> isCropLand(grid.terrainAt(at))));
        pools.put(Family.FISHERY, coast);
        final Map<Location, ResourceOverlay> out = new HashMap<>();
        for (final Family family : Family.values()) {
            final int quota = (int) Math.round(target * family.weight);
            place(out, shuffled(pools.get(family), random), family,
                    Math.min(quota, target - out.size()), grid, random);
        }
        // Any shortfall from rounding or an empty pool is taken in family
        // order.
        for (final Family family : Family.values()) {
            place(out, shuffled(pools.get(family), random), family,
                    target - out.size(), grid, random);
        }
        if (out.size() != target) {
            throw new IllegalStateException(
                    "Not enough suitable squares for resources");
        }
        for (final Location at : GeneratedGrid.sorted(rivers)) {
            final double roll = random.nextDouble();
            if (roll < 0.15) {
                out.put(at, ResourceOverlay.SILVER);
            } else if (roll < 0.20) {
                out.put(at, ResourceOverlay.ORE);
            }
        }
        return out;
    }

    private static boolean isCropLand(final TerrainType terrain) {
        return terrain == TerrainType.GRASSLAND
                || terrain == TerrainType.PRAIRIE
                || terrain == TerrainType.SAVANNAH
                || terrain == TerrainType.PLAINS;
    }

    private static List<Location> filter(final List<Location> squares,
            final Predicate<Location> test) {
        final List<Location> out = new ArrayList<>();
        for (final Location at : squares) {
            if (test.test(at)) {
                out.add(at);
            }
        }
        return out;
    }

    private static List<Location> shuffled(final List<Location> squares,
            final Random random) {
        final List<Location> out = new ArrayList<>(squares);
        Collections.shuffle(out, random);
        return out;
    }

    /** Add up to {@code limit} resources of one family to free squares. */
    private static void place(final Map<Location, ResourceOverlay> out,
            final List<Location> pool, final Family family, final int limit,
            final GeneratedGrid grid, final Random random) {
        int added = 0;
        for (final Location at : pool) {
            if (added >= limit) {
                break;
            }
            if (!out.containsKey(at)) {
                out.put(at, overlayFor(family, grid.terrainAt(at), random));
                added++;
            }
        }
    }

    /**
     * @return resource of a family on a square of a terrain in that family's
     *         pool; each choice is allowed on that terrain
     */
    private static ResourceOverlay overlayFor(final Family family,
            final TerrainType terrain, final Random random) {
        if (family == Family.MINERAL) {
            if (random.nextDouble() < MIXED_SHARE) {
                return ResourceOverlay.MIXED_MINERALS;
            }
            return terrain == TerrainType.MOUNTAIN ? ResourceOverlay.SILVER
                    : ResourceOverlay.ORE;
        }
        if (family == Family.FOREST) {
            return FOREST_OVERLAYS[random.nextInt(FOREST_OVERLAYS.length)];
        }
        if (family == Family.OASIS) {
            return ResourceOverlay.OASIS;
        }
        if (family == Family.FISHERY) {
            return ResourceOverlay.FISHERY;
        }
        if (terrain == TerrainType.SAVANNAH) {
            return ResourceOverlay.PRIME_SUGAR;
        }
        if (terrain == TerrainType.PLAINS) {
            return ResourceOverlay.PRIME_COTTON;
        }
        if (terrain == TerrainType.GRASSLAND
                && random.nextDouble() < TOBACCO_SHARE) {
            return ResourceOverlay.PRIME_TOBACCO;
        }
        return ResourceOverlay.PRIME_FOOD;
    }

}
