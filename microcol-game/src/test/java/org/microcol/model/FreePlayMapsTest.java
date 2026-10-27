package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.microcol.model.campaign.FreePlayMap;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;

class FreePlayMapsTest {
    @TempDir
    Path directory;

    private final GameModelPoDao dao = new GameModelPoDao();

    @Test
    void everyBundledWorldKeepsRiverPlacementAndLakesSeparateFromTheSea() {
        for (final FreePlayMap choice : FreePlayMap.values()) {
            final GameModelPo data = dao
                    .loadFromClassPath(choice.getResource());
            final WorldMap map = new WorldMap(data.getModel());
            if (choice != FreePlayMap.TWO_ISLANDS) {
                assertEquals(TerrainType.LAKE,
                        map.getTerrainTypeAt(Location.of(15, 12)),
                        choice.name());
            }
            for (int x = 1; x <= map.getMaxLocationX(); x++) {
                for (int y = 1; y <= map.getMaxLocationY(); y++) {
                    final Location at = Location.of(x, y);
                    final TerrainType terrain = map.getTerrainTypeAt(at);
                    if (map.getRiverSizeAt(at) != RiverSize.NONE) {
                        assertFalse(
                                terrain.isSea() || terrain == TerrainType.HILL
                                        || terrain == TerrainType.MOUNTAIN,
                                choice + " " + at);
                        // R-0313 explicitly adds this coastal endpoint.
                        if (choice != FreePlayMap.ORIGINAL
                                || !at.equals(Location.of(25, 13))) {
                            assertTrue(at.getNeighbors().stream()
                                    .filter(map::isValid)
                                    .noneMatch(next -> map
                                            .getTerrainTypeAt(next).isSea()),
                                    choice + " " + at);
                        }
                    }
                    if (terrain == TerrainType.LAKE) {
                        assertTrue(at.getNeighbors().stream()
                                .filter(map::isValid)
                                .allMatch(next -> !map.getTerrainTypeAt(next)
                                        .isSea()
                                        || map.getTerrainTypeAt(
                                                next) == TerrainType.LAKE),
                                choice + " " + at);
                    }
                }
            }
        }
    }

    @Test
    void bothWorldsHaveLegalOwnedFrenchContentAndPreserveItOnDisk() {
        for (FreePlayMap map : List.of(FreePlayMap.ORIGINAL,
                FreePlayMap.TWO_ISLANDS)) {
            final GameModelPo data = dao.loadFromClassPath(map.getResource());
            final Model model = Model.make(data.getModel(),
                    org.microcol.model.campaign.MissionImpl
                            .getPredefinedGameOverEvaluators());
            model.startGame();
            verifyWorld(model, map == FreePlayMap.ORIGINAL ? 17 : 13);
            data.setModel(model.save());
            final Path save = directory.resolve(map.name() + ".microcol");
            dao.saveToFile(save.toString(), data);
            final Model restored = Model.make(
                    dao.loadFromFile(save.toFile()).getModel(),
                    org.microcol.model.campaign.MissionImpl
                            .getPredefinedGameOverEvaluators());
            restored.startGame();
            verifyWorld(restored, map == FreePlayMap.ORIGINAL ? 17 : 13);
        }
    }

    private void verifyWorld(final Model model, final int cityX) {
        assertEquals(50, model.getMap().getMaxLocationX());
        assertEquals(30, model.getMap().getMaxLocationY());
        final Player france = model.getPlayerByName("France");
        final Player dutch = model.getPlayerByName("Dutch");
        assertEquals(EuropeanRelationState.PEACE,
                model.getEuropeanRelation(dutch, france));
        assertEquals(1, model.save().getEuropeanRelations().size());
        assertTrue(france.isComputer());
        assertFalse(france.isKing());
        assertTrue(dutch.isHuman());
        assertEquals(2, france.getColonies().size());
        assertEquals(3, france.getAllUnits().size());
        assertEquals(4, dutch.getAllUnits().size());
        assertEquals(2, model.save().getNextColonyId());
        final Colony quebec = model.getColonyById(0).orElseThrow();
        final Colony montreal = model.getColonyById(1).orElseThrow();
        assertEquals("Quebec", quebec.getName());
        assertEquals("Montreal", montreal.getName());
        assertEquals(Location.of(cityX, 10), quebec.getLocation());
        assertEquals(Location.of(cityX, cityX == 17 ? 21 : 20),
                montreal.getLocation());
        for (Colony colony : france.getColonies()) {
            assertSame(france, colony.getOwner());
            assertFalse(model.getMap().getTerrainTypeAt(colony.getLocation())
                    .isSea());
            assertEquals(1, colony.getColonyFields().stream()
                    .filter(field -> field.getUnit().isPresent()).count());
            colony.getColonyFields().stream()
                    .filter(f -> f.getUnit().isPresent())
                    .forEach(f -> assertSame(france,
                            f.getUnit().get().getOwner()));
        }
        final Unit ship = model.getUnitById(3);
        assertSame(dutch, ship.getOwner());
        assertEquals(Location.of(45, 15), ship.getLocation());
        assertEquals(TerrainType.OCEAN,
                model.getMap().getTerrainTypeAt(ship.getLocation()));
        assertEquals(0, model.getKingsTaxForPlayer(france));
    }

    @Test
    void twoIslandWorldHasTwoMainLandmassesAndOpenSeaLanes() {
        final Model model = Model.make(
                dao.loadFromClassPath(FreePlayMap.TWO_ISLANDS.getResource())
                        .getModel(),
                org.microcol.model.campaign.MissionImpl
                        .getPredefinedGameOverEvaluators());
        final List<Continent> continents = new ContinentTool()
                .findContinents(model).getContinents();
        assertEquals(2, continents.stream()
                .filter(c -> c.getLocations().size() > 100).count());
        for (int y = 2; y < 30; y++) {
            assertTrue(model.getMap().getTerrainTypeAt(Location.of(23, y))
                    .isSea());
        }
        for (int x = 41; x <= 50; x++) {
            assertTrue(model.getMap().getTerrainTypeAt(Location.of(x, 15))
                    .isSea());
        }
    }
}
