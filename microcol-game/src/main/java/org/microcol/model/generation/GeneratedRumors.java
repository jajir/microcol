package org.microcol.model.generation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.microcol.model.Location;
import org.microcol.model.TerrainType;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.NativeSettlementPo;
import org.microcol.model.store.UnitPo;

/**
 * Sparse lost-city rumor markers of a generated world. They are saved with the
 * map like any other square feature; the model resolves each one once, at a
 * legal scout entry.
 */
final class GeneratedRumors {

    /** One marker for about this many suitable land squares. */
    static final int LAND_PER_RUMOR = 50;

    /** Smallest Chebyshev distance between two markers. */
    static final int SPACING = 3;

    private GeneratedRumors() {
    }

    /**
     * Mark suitable land squares of a generated world with rumors. A square is
     * suitable when it is walkable land that is no mountain or arctic square
     * and holds neither a native settlement nor any saved unit.
     *
     * @param model  required model with terrain, natives and expedition
     * @param random required generator of this attempt; it is separate from the
     *               terrain and native generators, so those stay unchanged
     */
    static void place(final ModelPo model, final Random random) {
        final GeneratedGrid grid = new GeneratedGrid(
                model.getMap().getTerrainMap());
        final Set<Location> occupied = new HashSet<>();
        for (final NativeSettlementPo settlement : model
                .getNativeSettlements()) {
            occupied.add(settlement.getLocation());
        }
        for (final UnitPo unit : model.getUnits()) {
            if (unit.getPlaceMap() != null) {
                occupied.add(unit.getPlaceMap().getLocation());
            }
        }
        final List<Location> sites = new ArrayList<>(grid.squaresWith(
                terrain -> !terrain.isSea() && terrain != TerrainType.MOUNTAIN
                        && terrain != TerrainType.ARCTIC));
        sites.removeIf(occupied::contains);
        final int wanted = Math.max(1, sites.size() / LAND_PER_RUMOR);
        Collections.shuffle(sites, random);
        final List<Location> chosen = new ArrayList<>();
        for (final Location candidate : sites) {
            if (chosen.size() == wanted) {
                break;
            }
            if (chosen.stream().allMatch(other -> other
                    .getDistanceChebyshev(candidate) >= SPACING)) {
                chosen.add(candidate);
            }
        }
        model.getMap().setRumors(new HashSet<>(chosen));
    }

}
