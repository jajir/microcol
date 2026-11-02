package org.akropia.ai;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.akropia.gui.screen.game.gamepanel.AnimationLock;
import org.akropia.model.*;
import org.akropia.model.campaign.MissionImpl;
import org.akropia.model.campaign.po.GameModelPoDao;
import com.google.gson.Gson;

class FortifiedUnitAiTest {
    @Test
    void resumedFortifiedUnitKeepsItsPositionOrderAndMovementDuringAiAction() {
        final var data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.akropia");
        final var savedSoldier = data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == 16).findFirst().orElseThrow();
        savedSoldier.setHoldingWeapons(true);
        savedSoldier.setTools(0);
        savedSoldier.setAvailableMoves(1);
        final var terrain = data.getModel().getMap().getTerrainMap();
        terrain.put(Location.of(48, 15), TerrainType.GRASSLAND);
        data.getModel().getMap().setTerrainType(terrain);
        final Model initial = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Unit soldier = initial.getUnitById(16);
        soldier.placeToLocation(Location.of(48, 15));
        initial.fortify(soldier);
        final Player owner = soldier.getOwner();
        do {
            initial.endTurn();
        } while (initial.getCurrentPlayer() != owner);
        final Model model = Model.make(initial.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Unit restored = model.getUnitById(16);
        assertTrue(model.isFortified(restored));
        assertTrue(restored.getActionPoints() > 0);
        final String before = new Gson().toJson(model.save());
        final SimpleAiPlayer ai = new SimpleAiPlayer(model, restored.getOwner(),
                mock(AnimationLock.class));
        try {
            ai.moveUnit(restored);
            assertEquals(before, new Gson().toJson(model.save()));
        } finally {
            ai.stop();
        }
    }
}
