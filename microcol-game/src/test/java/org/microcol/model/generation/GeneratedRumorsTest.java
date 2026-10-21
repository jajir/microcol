package org.microcol.model.generation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.microcol.model.Difficulty;
import org.microcol.model.EuropeanNation;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.TerrainType;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.NativeSettlementPo;
import org.microcol.model.store.UnitPo;

/**
 * Lost-city rumor markers of generated worlds: few, apart from each other, only
 * on free walkable land, the same for the same settings and kept by a save.
 */
class GeneratedRumorsTest {

    private static final int[] SEEDS = { 1, 7, 2024 };

    private static ModelPo generate(final GeneratedClimate climate,
            final GeneratedLandShare share, final int seed) {
        return GeneratedWorld.create(
                new GeneratedWorldSettings(seed, climate, share),
                EuropeanNation.DUTCH, Difficulty.CONQUISTADOR);
    }

    private static Set<Location> occupied(final ModelPo po) {
        final Set<Location> out = new HashSet<>();
        for (final NativeSettlementPo settlement : po.getNativeSettlements()) {
            out.add(settlement.getLocation());
        }
        for (final UnitPo unit : po.getUnits()) {
            if (unit.getPlaceMap() != null) {
                out.add(unit.getPlaceMap().getLocation());
            }
        }
        return out;
    }

    /** Squares that could carry a marker: walkable land with nothing on it. */
    private static int suitableSquares(final ModelPo po) {
        final Set<Location> occupied = occupied(po);
        int count = 0;
        for (final Map.Entry<Location, TerrainType> entry : po.getMap()
                .getTerrainMap().entrySet()) {
            final TerrainType terrain = entry.getValue();
            if (!terrain.isSea() && terrain != TerrainType.MOUNTAIN
                    && terrain != TerrainType.ARCTIC
                    && !occupied.contains(entry.getKey())) {
                count++;
            }
        }
        return count;
    }

    @Test
    void markersAreSparseSpacedAndOnFreeWalkableLand() {
        for (final GeneratedClimate climate : GeneratedClimate.values()) {
            for (final GeneratedLandShare share : GeneratedLandShare.values()) {
                for (final int seed : SEEDS) {
                    final String label = climate + " " + share + " " + seed;
                    final ModelPo po = generate(climate, share, seed);
                    final Set<Location> markers = po.getMap().getRumorSet();
                    final int wanted = Math.max(1, suitableSquares(po)
                            / GeneratedRumors.LAND_PER_RUMOR);

                    assertFalse(markers.isEmpty(), label);
                    assertTrue(markers.size() <= wanted, label);
                    assertTrue(markers.size() >= Math.max(1, wanted / 2),
                            label + ": " + markers.size() + " of " + wanted);

                    final Map<Location, TerrainType> terrain = po.getMap()
                            .getTerrainMap();
                    final Set<Location> occupied = occupied(po);
                    for (final Location marker : markers) {
                        final TerrainType type = terrain.get(marker);
                        assertNotNull(type, label);
                        assertFalse(type.isSea(), label);
                        assertNotEquals(TerrainType.MOUNTAIN, type, label);
                        assertNotEquals(TerrainType.ARCTIC, type, label);
                        assertFalse(occupied.contains(marker),
                                label + ": " + marker + " is occupied");
                    }
                    final List<Location> list = List.copyOf(markers);
                    for (int first = 0; first < list.size(); first++) {
                        for (int second = first + 1; second < list
                                .size(); second++) {
                            assertTrue(list.get(first)
                                    .getDistanceChebyshev(list.get(
                                            second)) >= GeneratedRumors.SPACING,
                                    label + ": markers too close");
                        }
                    }
                }
            }
        }
    }

    @Test
    void sameSettingsGiveTheSameMarkersAndOtherSeedsGiveOthers() {
        final Set<Location> first = generate(GeneratedClimate.TEMPERATE,
                GeneratedLandShare.MEDIUM, 7).getMap().getRumorSet();
        final Set<Location> again = generate(GeneratedClimate.TEMPERATE,
                GeneratedLandShare.MEDIUM, 7).getMap().getRumorSet();
        final Set<Location> other = generate(GeneratedClimate.TEMPERATE,
                GeneratedLandShare.MEDIUM, 2024).getMap().getRumorSet();
        assertEquals(first, again);
        assertNotEquals(first, other);
    }

    @Test
    void markersLoadIntoTheModelAndSurviveASave() {
        final ModelPo po = generate(GeneratedClimate.COLD,
                GeneratedLandShare.HIGH, 2024);
        final Set<Location> markers = po.getMap().getRumorSet();
        final Model model = Model.make(po,
                MissionImpl.getPredefinedGameOverEvaluators());
        final ModelPo saved;
        try {
            for (final Location marker : markers) {
                assertTrue(model.getMap().hasLostCityRumorAt(marker),
                        "" + marker);
            }
            // A generated world is saved only once it has started, as the
            // ordinary new-game path does before its first save.
            model.startGame();
            for (final Location marker : markers) {
                assertTrue(model.getMap().hasLostCityRumorAt(marker),
                        "starting resolves nothing: " + marker);
            }
            saved = model.save();
        } finally {
            model.stop();
        }
        assertEquals(markers, saved.getMap().getRumorSet());

        // Continue loads the started game as saved and keeps every marker.
        final Model restored = Model.make(saved,
                MissionImpl.getPredefinedGameOverEvaluators());
        try {
            assertTrue(restored.isGameStarted());
            for (final Location marker : markers) {
                assertTrue(restored.getMap().hasLostCityRumorAt(marker),
                        "" + marker);
            }
            assertEquals(markers, restored.save().getMap().getRumorSet());
        } finally {
            restored.stop();
        }
    }

}
