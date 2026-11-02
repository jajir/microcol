package org.akropia.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.akropia.model.campaign.MissionImpl;
import org.akropia.model.campaign.po.GameModelPo;
import org.akropia.model.campaign.po.GameModelPoDao;
import org.akropia.model.store.PlayerPo;
import org.akropia.model.unit.UnitFreeColonist;
import org.akropia.model.unit.UnitWithCargo;

class RoyalExpeditionRoleTest {
    @Test
    void royalSoldiersCarryTheirAttackEquipmentThroughCargoAndSave() {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.akropia");
        for (final PlayerPo player : data.getModel().getGameManager()
                .getPlayers()) {
            if (player.getWhosKingThisPlayerIs() != null) {
                player.setRoyalRoster(
                        RoyalRoster.initial(Difficulty.CONQUISTADOR));
            } else if (player.getName().equals("Dutch")) {
                RebellionFixtures.declare(data.getModel(), player);
            }
        }
        final Model model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        model.startGame();
        model.endTurn();
        final Player king = model.getPlayerByName("Dutch's King");
        final UnitWithCargo ship = model.dispatchRoyalShip(king,
                model.getRoyalRosterOf(king).getMembers().stream()
                        .filter(member -> member
                                .getFamily() == RoyalRoster.Family.MAN_OF_WAR)
                        .findFirst().orElseThrow().getId());
        final UnitFreeColonist soldier = ship.getCargo().getSlots().stream()
                .flatMap(slot -> slot.getUnit().stream())
                .filter(unit -> unit instanceof UnitFreeColonist
                        && !((UnitFreeColonist) unit).isMounted())
                .map(unit -> (UnitFreeColonist) unit).findFirst().orElseThrow();
        assertTrue(soldier.isHoldingWeapons());
        assertTrue(soldier.canAttack());
        assertTrue(soldier.isAtCargoSlot());
        assertSame(king, soldier.getOwner());
        assertEquals(MilitaryRole.ROYAL, soldier.getMilitaryRole());

        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final UnitFreeColonist saved = (UnitFreeColonist) restored
                .getUnitById(soldier.getId());
        assertTrue(saved.isHoldingWeapons());
        assertTrue(saved.canAttack());
        assertTrue(saved.isAtCargoSlot());
        assertEquals(MilitaryRole.ROYAL, saved.getMilitaryRole());
        assertEquals(ship.getId(),
                saved.getPlaceCargoSlot().getOwnerUnit().getId());
    }
}
