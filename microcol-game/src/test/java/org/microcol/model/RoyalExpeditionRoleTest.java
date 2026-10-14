package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.unit.UnitFreeColonist;
import org.microcol.model.unit.UnitWithCargo;

class RoyalExpeditionRoleTest {
    @Test
    void royalSoldiersCarryTheirAttackEquipmentThroughCargoAndSave() {
        final Model model = Model.make(new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.microcol").getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        model.startGame();
        final Player king = model.getPlayerByName("Dutch's King");
        final UnitWithCargo ship = model.createCargoShipForKing(king);
        final UnitFreeColonist soldier = (UnitFreeColonist) model
                .createRoyalExpeditionForceUnit(king, ship);
        assertTrue(soldier.isHoldingGuns());
        assertTrue(soldier.canAttack());
        assertTrue(soldier.isAtCargoSlot());
        assertSame(king, soldier.getOwner());
        assertEquals(1,
                ship.getCargo().getSlots().stream()
                        .filter(slot -> slot.getUnit()
                                .filter(unit -> unit == soldier).isPresent())
                        .count());

        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final UnitFreeColonist saved = (UnitFreeColonist) restored
                .getUnitById(soldier.getId());
        assertTrue(saved.isHoldingGuns());
        assertTrue(saved.canAttack());
        assertTrue(saved.isAtCargoSlot());
        assertEquals(ship.getId(),
                saved.getPlaceCargoSlot().getOwnerUnit().getId());
    }
}
