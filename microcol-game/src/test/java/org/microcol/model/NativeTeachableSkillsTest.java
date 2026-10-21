package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Initial teachable skill of a native settlement (R-0157): the accepted list,
 * preference for skills whose goods the surroundings produce naturally, the
 * whole-list fallback and reproduction from the same seed.
 */
class NativeTeachableSkillsTest {

    private static final Location AT = Location.of(10, 10);

    private static final List<Terrain> GRASSLAND = List
            .of(new Terrain(AT, TerrainType.GRASSLAND));

    @Test
    void acceptedListIsTheSkillsCardTeachableSet() {
        assertEquals(Set.of(UnitType.EXPERT_FARMER, UnitType.EXPERT_FISHERMAN,
                UnitType.EXPERT_FUR_TRAPPER, UnitType.EXPERT_SILVER_MINER,
                UnitType.MASTER_SUGAR_PLANTER, UnitType.MASTER_COTTON_PLANTER,
                UnitType.MASTER_TOBACCO_PLANTER, UnitType.SEASONED_SCOUT),
                Set.copyOf(UnitType.NATIVE_TEACHABLE_SKILLS));
        assertEquals(8, UnitType.NATIVE_TEACHABLE_SKILLS.size());
    }

    @Test
    void expertGoodsMatchTheirProduction() {
        assertEquals(GoodsType.CORN,
                UnitType.EXPERT_FARMER.getExpertInProducing());
        assertEquals(GoodsType.SUGAR,
                UnitType.MASTER_SUGAR_PLANTER.getExpertInProducing());
        assertNull(UnitType.SEASONED_SCOUT.getExpertInProducing());
    }

    @Test
    void naturalOutputIsTheTerrainBaseline() {
        assertEquals(4, GRASSLAND.get(0).getNaturalOutput(GoodsType.CORN));
        assertEquals(0, GRASSLAND.get(0).getNaturalOutput(GoodsType.SILVER));
    }

    @Test
    void localProductionPrefersMatchingSkillsOnly() {
        final Random random = new Random(7);
        final Set<UnitType> seen = new HashSet<>();
        for (int i = 0; i < 500; i++) {
            seen.add(NativeTeachableSkills.choose(GRASSLAND, random));
        }
        assertEquals(Set.of(UnitType.EXPERT_FARMER,
                UnitType.MASTER_SUGAR_PLANTER, UnitType.MASTER_COTTON_PLANTER,
                UnitType.MASTER_TOBACCO_PLANTER), seen);
    }

    @Test
    void waterFoodMatchesFishermanNotFarmer() {
        final List<Terrain> ocean = List.of(new Terrain(AT, TerrainType.OCEAN));
        final Random random = new Random(7);
        final Set<UnitType> seen = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            seen.add(NativeTeachableSkills.choose(ocean, random));
        }
        assertEquals(Set.of(UnitType.EXPERT_FISHERMAN), seen);
    }

    @Test
    void noLocalProductionFallsBackToWholeAcceptedList() {
        final Random random = new Random(7);
        final Set<UnitType> seen = new HashSet<>();
        for (int i = 0; i < 2000; i++) {
            seen.add(NativeTeachableSkills.choose(List.of(), random));
        }
        assertEquals(Set.copyOf(UnitType.NATIVE_TEACHABLE_SKILLS), seen);
    }

    @Test
    void sameSeedReproducesTheSkills() {
        final List<UnitType> first = new ArrayList<>();
        final List<UnitType> second = new ArrayList<>();
        final Random firstRandom = new Random(42);
        final Random secondRandom = new Random(42);
        for (int i = 0; i < 20; i++) {
            first.add(NativeTeachableSkills.choose(GRASSLAND, firstRandom));
            second.add(NativeTeachableSkills.choose(GRASSLAND, secondRandom));
        }
        assertEquals(first, second);
    }

}
