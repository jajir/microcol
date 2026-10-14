package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.microcol.model.campaign.FreePlayMap;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.unit.UnitWithCargo;

class RivalExpeditionWorldTest {
    @TempDir
    Path directory;
    private final GameModelPoDao dao = new GameModelPoDao();

    private void verifyExpeditions(final Model model) {
        final Player france = model.getPlayerByName("France");
        final Player dutch = model.getPlayerByName("Dutch");
        assertTrue(france.isComputer());
        assertFalse(france.isKing());
        assertTrue(dutch.isHuman());
        assertEquals(EuropeanRelationState.PEACE,
                model.getEuropeanRelation(france, dutch));
        assertEquals(6, model.getAllUnits().size());
        assertEquals(2, france.getAllUnits().size());
        assertEquals(4, dutch.getAllUnits().size());
        assertTrue(model.getColonies().isEmpty());
        assertEquals(0, model.save().getNextColonyId());
        final UnitWithCargo ship = (UnitWithCargo) model.getUnitById(5);
        final Unit founder = model.getUnitById(4);
        assertSame(france, ship.getOwner());
        assertSame(france, founder.getOwner());
        assertEquals(Location.of(35, 10), ship.getLocation());
        assertEquals(TerrainType.OCEAN,
                model.getMap().getTerrainTypeAt(ship.getLocation()));
        assertTrue(founder.getType().canBuildColony());
        assertSame(founder,
                ship.getCargo().getSlotByIndex(0).getUnit().orElseThrow());
        assertSame(ship, founder.getPlaceCargoSlot().getOwnerUnit());
        assertEquals(Location.of(29, 15), model.getUnitById(3).getLocation());
        assertEquals(TerrainType.OCEAN, model.getMap()
                .getTerrainTypeAt(model.getUnitById(3).getLocation()));
        assertTrue(france.isVisible(ship.getLocation()),
                "A rival knows the tile occupied by its own ship");
        assertFalse(france.isVisible(Location.of(26, 15)));
        assertTrue(dutch.isVisible(ship.getLocation()));
    }

    @Test
    void ordinaryBundledWorldStartsWithAnOwnedFleetAndUnexploredCoast() {
        final GameModelPo data = dao
                .loadFromClassPath(FreePlayMap.RIVAL_EXPEDITION.getResource());
        final Model model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        model.startGame();
        verifyExpeditions(model);
    }

    @Test
    void diskRoundTripPreservesFleetIdentitiesOwnershipAndKnowledge() {
        final GameModelPo data = dao
                .loadFromClassPath(FreePlayMap.RIVAL_EXPEDITION.getResource());
        final Model model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        model.startGame();
        data.setModel(model.save());
        final Path file = directory.resolve("expedition.microcol");
        dao.saveToFile(file.toString(), data);
        final Model restored = Model.make(
                dao.loadFromFile(file.toFile()).getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        restored.startGame();
        verifyExpeditions(restored);
    }
}
