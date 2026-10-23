package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.InterventionPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.unit.UnitFreeColonist;

/**
 * R-0190: an earned intervention is delivered once, by the owner update that
 * finds it earned, to the lowest-ID owned coastal colony. Reloading never
 * delivers, and the delivered flag is kept apart from the earning.
 */
class InterventionDeliveryTest {

    private static final String MAP = "/maps/default-campaign-mission-2-thrive.microcol";

    private static final int TARGET = 500;

    private static GameModelPo data() {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(MAP);
        for (final PlayerPo player : data.getModel().getGameManager()
                .getPlayers()) {
            if (player.getWhosKingThisPlayerIs() != null) {
                player.setRoyalRoster(
                        RoyalRoster.initial(Difficulty.CONQUISTADOR));
            } else if (player.getName().equals("Dutch")) {
                RebellionFixtures.declare(data.getModel(), player);
            }
        }
        return data;
    }

    private static Model load(final ModelPo po) {
        return Model.make(po, MissionImpl.getPredefinedGameOverEvaluators());
    }

    private static Player dutch(final Model model) {
        return model.getPlayers().stream()
                .filter(player -> player.getName().equals("Dutch")).findFirst()
                .orElseThrow();
    }

    private static InterventionPo po(final int target, final long bells,
            final boolean earned, final boolean delivered) {
        final InterventionPo po = new InterventionPo();
        po.setTarget(target);
        po.setBells(bells);
        po.setEarned(earned);
        po.setDelivered(delivered);
        return po;
    }

    /**
     * The rule of R-0190 as the test states it: lowest-ID owned coastal port.
     */
    private static Colony expectedPort(final Model model, final Player owner) {
        return model.getColonies(owner).stream().filter(
                colony -> model.isOwnedCoastalPort(owner, colony.getLocation()))
                .min(Comparator.comparingInt(Colony::getId)).orElseThrow();
    }

    private static long delivered(final Player owner) {
        return owner.getAllUnits().stream()
                .filter(unit -> unit instanceof UnitFreeColonist)
                .filter(unit -> ((UnitFreeColonist) unit)
                        .isInterventionDelivered())
                .count();
    }

    private static long menOfWarAt(final Model model, final Colony port,
            final Player owner) {
        return model.getUnitsAt(port.getLocation()).stream()
                .filter(unit -> unit.getOwner() == owner)
                .filter(unit -> unit.getType() == UnitType.MAN_OF_WAR).count();
    }

    @Test
    void earnedInterventionIsDeliveredOnceAtTheLowestOwnedCoastalPort() {
        final Model model = load(data().getModel());
        final Player dutch = dutch(model);
        assertTrue(dutch.startTurn());
        final InterventionTarget state = dutch.getIntervention().orElseThrow();
        final Colony port = expectedPort(model, dutch);
        final int unitsBefore = dutch.getAllUnits().size();
        final long shipsBefore = menOfWarAt(model, port, dutch);
        dutch.creditProducedBells(
                Math.toIntExact(state.getTarget() - state.getBells()));
        assertTrue(state.isEarned());
        assertFalse(state.isDelivered());
        // The earning alone delivers nothing; the next owner update does.
        assertEquals(unitsBefore, dutch.getAllUnits().size());
        assertTrue(dutch.startTurn());
        assertTrue(state.isDelivered());
        assertEquals(unitsBefore + 8, dutch.getAllUnits().size());
        assertEquals(shipsBefore + 2, menOfWarAt(model, port, dutch));
        assertEquals(6L, delivered(dutch));
        final List<UnitFreeColonist> soldiers = dutch.getAllUnits().stream()
                .filter(unit -> unit instanceof UnitFreeColonist)
                .map(unit -> (UnitFreeColonist) unit)
                .filter(UnitFreeColonist::isInterventionDelivered)
                .collect(Collectors.toList());
        assertEquals(2L,
                soldiers.stream().filter(UnitFreeColonist::isMounted).count());
        assertTrue(soldiers.stream().allMatch(
                soldier -> soldier.getMilitaryRole() == MilitaryRole.CONTINENTAL
                        && soldier.isHoldingGuns()
                        && soldier.getLocation().equals(port.getLocation())));
        // A later owner update never delivers a second time.
        assertTrue(dutch.startTurn());
        assertEquals(unitsBefore + 8, dutch.getAllUnits().size());
        assertEquals(6L, delivered(dutch));
    }

    @Test
    void deliveredStateSurvivesReloadWithoutANewDelivery() {
        final Model model = load(data().getModel());
        final Player dutch = dutch(model);
        assertTrue(dutch.startTurn());
        final InterventionTarget state = dutch.getIntervention().orElseThrow();
        dutch.creditProducedBells(
                Math.toIntExact(state.getTarget() - state.getBells()));
        assertTrue(dutch.startTurn());
        final int unitsAfterDelivery = dutch.getAllUnits().size();
        final Model restored = load(model.save());
        final Player restoredDutch = dutch(restored);
        assertTrue(restoredDutch.getIntervention().orElseThrow().isDelivered());
        assertEquals(unitsAfterDelivery, restoredDutch.getAllUnits().size());
        assertEquals(6L, delivered(restoredDutch));
        assertTrue(restoredDutch.startTurn());
        assertEquals(unitsAfterDelivery, restoredDutch.getAllUnits().size());
        assertEquals(6L, delivered(restoredDutch));
    }

    @Test
    void savedEarnedButUndeliveredStateWaitsForTheNextOwnerUpdate() {
        final GameModelPo data = data();
        for (final PlayerPo player : data.getModel().getGameManager()
                .getPlayers()) {
            if (player.getName().equals("Dutch")) {
                player.setIntervention(po(TARGET, TARGET, true, false));
            }
        }
        final Model loaded = load(data.getModel());
        final Player dutch = dutch(loaded);
        final int unitsBeforeUpdate = dutch.getAllUnits().size();
        // Loading never delivers; only the owner update does.
        assertFalse(dutch.getIntervention().orElseThrow().isDelivered());
        assertEquals(unitsBeforeUpdate, dutch.getAllUnits().size());
        assertTrue(dutch.startTurn());
        assertTrue(dutch.getIntervention().orElseThrow().isDelivered());
        assertEquals(unitsBeforeUpdate + 8, dutch.getAllUnits().size());
    }

    @Test
    void deliveryNeedsAnEarnedTargetAndIncoherentSavesAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new InterventionTarget(po(TARGET, 10L, false, true)));
        assertThrows(IllegalArgumentException.class,
                () -> new InterventionTarget(new InterventionPo()));
    }
}
