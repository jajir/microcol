package org.microcol.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.microcol.model.Direction;
import org.microcol.model.Location;
import org.microcol.model.LostCityRumorRules;
import org.microcol.model.NativeNation;
import org.microcol.model.NativeRelations;
import org.microcol.model.TerrainType;
import org.microcol.model.UnitType;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.NativeSettlementPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.UnitActionNoActionPo;
import org.microcol.model.store.UnitPo;

/**
 * Deterministic current-schema saves for the lost-city rumor scenarios. Each is
 * the bundled free-play map with one nine by nine island, an Aztec capital, one
 * Dutch scout and two rumor markers. The saved random state is chosen with an
 * independent copy of the persisted generator, so the result of the first entry
 * is known before the game starts and nothing of the gameplay is mocked.
 */
final class LostCityRumorFixture {

    /** Aztec capital of the island. */
    static final Location SITE = Location.of(36, 8);

    /** Land square where the scout starts; two squares from the marker. */
    static final Location START = Location.of(33, 8);

    /** The marker the scenarios enter; the capital is two squares east. */
    static final Location RUMOR = Location.of(34, 8);

    /** A second marker that no scenario enters. */
    static final Location OTHER_RUMOR = Location.of(33, 6);

    /** Square east of {@link #RUMOR}. */
    static final Location BEYOND = Location.of(35, 8);

    static final int SCOUT_ID = 200;

    private static final long GOLDEN = 0x9E3779B97F4A7C15L;

    private static final int HALF_ISLAND = 4;

    private LostCityRumorFixture() {
    }

    /** A written save and the facts the scenario may expect from it. */
    static final class Fixture {
        private final File file;
        private final int treasury;
        private final int gold;

        private Fixture(final File file, final int treasury, final int gold) {
            this.file = file;
            this.treasury = treasury;
            this.gold = gold;
        }

        /** @return the save the Load command opens */
        File getFile() {
            return file;
        }

        /** @return Dutch gold before any entry */
        int getTreasury() {
            return treasury;
        }

        /** @return gold of the first entry, zero unless it is a gold result */
        int getGold() {
            return gold;
        }
    }

    /**
     * @param name    required file name stem
     * @param outcome required result of the ordinary scout's first entry
     * @return the written save
     * @throws IOException if the save cannot be written
     */
    static Fixture create(final String name,
            final LostCityRumorRules.Outcome outcome) throws IOException {
        final long seed = seedFor(outcome);
        final GameModelPoDao dao = new GameModelPoDao();
        final GameModelPo data = dao
                .loadFromClassPath("/maps/free-play.microcol");
        final ModelPo po = data.getModel();
        po.getGameManager().setGameStarted(true);
        po.getGameManager().setCurrentPlayer("Dutch");
        po.setCombatRandomState(seed);

        final Map<Location, TerrainType> terrain = po.getMap().getTerrainMap();
        for (int dx = -HALF_ISLAND; dx <= HALF_ISLAND; dx++) {
            for (int dy = -HALF_ISLAND; dy <= HALF_ISLAND; dy++) {
                terrain.put(Location.of(SITE.getX() + dx, SITE.getY() + dy),
                        TerrainType.GRASSLAND);
            }
        }
        po.getMap().setTerrainType(terrain);
        po.getMap().setRumors(Set.of(RUMOR, OTHER_RUMOR));
        po.setFocusedField(START);

        final NativeSettlementPo settlement = new NativeSettlementPo();
        settlement.setId(0);
        settlement.setNation(NativeNation.AZTEC);
        settlement.setSettlementClass(NativeNation.AZTEC.getSettlementClass());
        settlement.setCapital(true);
        settlement.setTreasury(2000);
        settlement.setTeachableSkill(UnitType.EXPERT_FARMER.name());
        settlement.setOfferedGoods(List.of());
        settlement.setWantedGoods(List.of());
        settlement.setTrade(List.of());
        settlement.setLocation(SITE);
        final List<NativeSettlementPo> natives = new ArrayList<>();
        natives.add(settlement);
        po.setNativeSettlements(natives);
        po.setNativeSettlementKnowledge(new ArrayList<>());
        po.setNativeTribalAnger(NativeRelations.freshTribalAnger());
        po.setNativeLocalAlarm(NativeRelations.freshLocalAlarm(natives));
        po.setNativeLandRights(new ArrayList<>());
        po.setNativeLandTakings(new ArrayList<>());

        po.getUnits().add(scout());

        final int treasury = po.getGameManager().getPlayers().stream()
                .filter(player -> "Dutch".equals(player.getName())).findFirst()
                .orElseThrow().getGold();
        final int gold = outcome == LostCityRumorRules.Outcome.GOLD
                ? LostCityRumorRules.gold(roll(seed, 2))
                : 0;
        final Path file = Files.createDirectories(Path.of("target", "fixtures"))
                .resolve("F114-lost-city-rumor-" + name + ".microcol");
        dao.saveToFile(file.toString(), data);
        return new Fixture(file.toFile(), treasury, gold);
    }

    /** The ordinary scout: a mounted colonist without muskets. */
    private static UnitPo scout() {
        final UnitPo unit = new UnitPo();
        unit.setId(SCOUT_ID);
        unit.setType(UnitType.COLONIST);
        unit.setOwnerId("Dutch");
        unit.setWorkUpdates(0);
        unit.setMissionary(false);
        unit.setAction(new UnitActionNoActionPo());
        unit.setMovementThirds(15);
        unit.setMounted(true);
        final PlaceMapPo place = new PlaceMapPo();
        place.setLocation(START);
        place.setOrientation(Direction.west);
        unit.setPlaceMap(place);
        return unit;
    }

    private static long seedFor(final LostCityRumorRules.Outcome outcome) {
        for (long seed = 0; seed < 5_000_000L; seed++) {
            if (LostCityRumorRules.draw(false, roll(seed, 1)) == outcome) {
                return seed;
            }
        }
        throw new IllegalStateException("No seed gives " + outcome);
    }

    /** The persisted generator: the draw-th output for a saved state. */
    private static double roll(final long seed, final int draw) {
        long state = seed;
        long z = 0;
        for (int index = 0; index < draw; index++) {
            state += GOLDEN;
            z = state;
            z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
            z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
            z ^= z >>> 31;
        }
        return (z >>> 11) * 0x1.0p-53;
    }

}
