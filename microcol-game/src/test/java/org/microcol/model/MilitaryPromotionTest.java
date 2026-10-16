package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.event.UnitAttackedEvent;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.UnitFreeColonist;

import com.google.gson.Gson;

/** Earned military rank is a saved result of ordinary land victory. */
class MilitaryPromotionTest {
    private static final Location ORIGIN = Location.of(18, 10);
    private static final Location TARGET = Location.of(17, 11);
    private static final long STEP = 0x9E3779B97F4A7C15L;

    private Model game(final long state, final UnitType attackerType,
            final UnitType defenderType, final boolean washington) {
        final ModelPo data = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.microcol").getModel();
        data.setCombatRandomState(state);
        data.getUnits().stream().filter(unit -> unit.getId() == 0).findFirst()
                .orElseThrow().setType(attackerType);
        data.getUnits().stream().filter(unit -> unit.getId() == 6).findFirst()
                .orElseThrow().setType(defenderType);
        if (washington) {
            data.getGameManager().getPlayers().stream()
                    .filter(player -> player.getName().equals("France"))
                    .findFirst().orElseThrow().getCongress().getMembers()
                    .add(Founder.GEORGE_WASHINGTON);
        }
        final Model model = Model.make(data,
                MissionImpl.getPredefinedGameOverEvaluators());
        model.startGame();
        for (int i = 0; i < 8
                && !model.getCurrentPlayer().getName().equals("Dutch"); i++) {
            model.endTurn();
        }
        final UnitFreeColonist attacker = (UnitFreeColonist) model
                .getUnitById(0);
        final UnitFreeColonist defender = (UnitFreeColonist) model
                .getUnitById(6);
        attacker.placeToLocation(ORIGIN);
        defender.placeToLocation(TARGET);
        attacker.setHoldingGuns(true);
        defender.setHoldingGuns(true);
        attacker.getOwner().revealMapForUnit(attacker);
        return model;
    }

    private List<UnitAttackedEvent> listen(final Model model) {
        final List<UnitAttackedEvent> events = new ArrayList<>();
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onUnitAttacked(final UnitAttackedEvent event) {
                events.add(event);
            }
        });
        return events;
    }

    @Test
    void chanceDrawPromotesOnlyBelowQuarterAndPersistsWithoutReplay() {
        final Model promoted = game(7L, UnitType.COLONIST, UnitType.COLONIST,
                false);
        final UnitFreeColonist soldier = (UnitFreeColonist) promoted
                .getUnitById(0);
        final List<UnitAttackedEvent> events = listen(promoted);
        soldier.prepareAttack(TARGET, true).orElseThrow().execute();
        final MilitaryPromotion step = events.get(0).getPromotion()
                .orElseThrow();
        assertSame(soldier, promoted.getUnitById(0));
        assertEquals(0, step.getUnitId());
        assertEquals(UnitType.COLONIST, step.getFromType());
        assertEquals(UnitType.COLONIST, step.getToType());
        assertFalse(step.wasVeteran());
        assertTrue(step.isVeteran());
        assertTrue(soldier.isVeteran());
        assertTrue(soldier.isHoldingGuns());
        assertEquals(7L + 2 * STEP, promoted.save().getCombatRandomState());
        final Model restored = Model.make(promoted.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertTrue(((UnitFreeColonist) restored.getUnitById(0)).isVeteran());
        assertEquals(promoted.save().getCombatRandomState(),
                restored.save().getCombatRandomState());

        final Model missed = game(3L, UnitType.COLONIST, UnitType.COLONIST,
                false);
        final List<UnitAttackedEvent> missedEvents = listen(missed);
        ((UnitFreeColonist) missed.getUnitById(0)).prepareAttack(TARGET, true)
                .orElseThrow().execute();
        assertTrue(missedEvents.get(0).getPromotion().isEmpty());
        assertFalse(((UnitFreeColonist) missed.getUnitById(0)).isVeteran());
        assertEquals(3L + 2 * STEP, missed.save().getCombatRandomState());
    }

    @Test
    void exactQuarterDrawDoesNotPromoteButJustBelowItDoes() {
        final long belowQuarter = 1505139683090647059L;
        final long exactQuarter = -2363065069095756174L;
        for (final long seed : new long[] { belowQuarter, exactQuarter }) {
            final Model model = game(seed, UnitType.COLONIST, UnitType.COLONIST,
                    false);
            final List<UnitAttackedEvent> events = listen(model);
            final UnitFreeColonist winner = (UnitFreeColonist) model
                    .getUnitById(0);
            winner.prepareAttack(TARGET, true).orElseThrow().execute();
            final boolean expectedPromotion = seed == belowQuarter;
            assertEquals(expectedPromotion, winner.isVeteran());
            assertEquals(expectedPromotion,
                    events.get(0).getPromotion().isPresent());
            assertEquals(seed + 2 * STEP, model.save().getCombatRandomState());
        }
    }

    @Test
    void defenderExpertKeepsSpecialtyWhenPromoted() {
        final Model model = game(19L, UnitType.COLONIST,
                UnitType.MASTER_BLACKSMITH, false);
        final UnitFreeColonist expert = (UnitFreeColonist) model.getUnitById(6);
        final List<UnitAttackedEvent> events = listen(model);
        model.getUnitById(0).prepareAttack(TARGET, true).orElseThrow()
                .execute();
        assertSame(expert, model.getUnitById(6));
        assertEquals(UnitType.MASTER_BLACKSMITH, expert.getType());
        assertTrue(expert.isVeteran());
        assertEquals(6, events.get(0).getPromotion().orElseThrow().getUnitId());
        assertEquals(UnitType.MASTER_BLACKSMITH,
                events.get(0).getPromotion().orElseThrow().getToType());
        assertEquals(19L + 2 * STEP, model.save().getCombatRandomState());
        expert.setHoldingGuns(false);
        assertTrue(expert.isVeteran());
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final UnitFreeColonist continued = (UnitFreeColonist) restored
                .getUnitById(6);
        assertEquals(UnitType.MASTER_BLACKSMITH, continued.getType());
        assertTrue(continued.isVeteran());
        assertFalse(continued.isHoldingGuns());
    }

    @Test
    void washingtonPromotesDefenderWithoutExtraDraw() {
        final Model model = game(0L, UnitType.COLONIST, UnitType.COLONIST,
                true);
        final UnitFreeColonist defender = (UnitFreeColonist) model
                .getUnitById(6);
        defender.setMounted(true);
        final List<UnitAttackedEvent> events = listen(model);
        model.getUnitById(0).prepareAttack(TARGET, true).orElseThrow()
                .execute();
        assertTrue(defender.isVeteran());
        assertEquals(4.5, defender.getMilitaryStrenght());
        assertTrue(events.get(0).getPromotion().isPresent());
        assertEquals(0L + STEP, model.save().getCombatRandomState());
    }

    @Test
    void criminalServantFreeVeteranStepsKeepIdentityAndEquipment() {
        Model model = game(7L, UnitType.PETTY_CRIMINAL, UnitType.COLONIST,
                false);
        final UnitType[] before = { UnitType.PETTY_CRIMINAL,
                UnitType.INDENTURED_SERVANT, UnitType.COLONIST };
        final UnitType[] after = { UnitType.INDENTURED_SERVANT,
                UnitType.COLONIST, UnitType.COLONIST };
        for (int i = 0; i < before.length; i++) {
            final UnitFreeColonist winner = (UnitFreeColonist) model
                    .getUnitById(0);
            final List<UnitAttackedEvent> events = listen(model);
            winner.prepareAttack(TARGET, true).orElseThrow().execute();
            final MilitaryPromotion promotion = events.get(0).getPromotion()
                    .orElseThrow();
            assertSame(winner, model.getUnitById(0));
            assertEquals(before[i], promotion.getFromType());
            assertEquals(after[i], promotion.getToType());
            assertTrue(winner.isHoldingGuns());
            assertEquals(i == 2, winner.isVeteran());
            if (i < before.length - 1) {
                final ModelPo next = model.save();
                next.setCombatRandomState(7L);
                for (final UnitPo unit : next.getUnits()) {
                    if (unit.getId() == 0 || unit.getId() == 6) {
                        unit.setAvailableMoves(1);
                        unit.setHoldingGuns(true);
                    }
                }
                model = Model.make(next,
                        MissionImpl.getPredefinedGameOverEvaluators());
            }
        }
    }

    @Test
    void purchasedVeteranHasOneBaseBonusEvenAfterDisarming() {
        final Model model = game(7L, UnitType.VETERAN_SOLDIER,
                UnitType.COLONIST, false);
        final UnitFreeColonist veteran = (UnitFreeColonist) model
                .getUnitById(0);
        assertTrue(veteran.isVeteran());
        assertEquals(3, veteran.prepareAttack(TARGET, true).orElseThrow()
                .getCombatStrengths().orElseThrow().getAttacker().getBase());
        final List<UnitAttackedEvent> events = listen(model);
        veteran.prepareAttack(TARGET, true).orElseThrow().execute();
        assertTrue(events.get(0).getPromotion().isEmpty());
        assertEquals(7L + STEP, model.save().getCombatRandomState());
        veteran.setHoldingGuns(false);
        assertTrue(veteran.isVeteran());
        veteran.setMounted(true);
        veteran.setHoldingGuns(true);
        ((UnitFreeColonist) model.getUnitById(6)).setHoldingGuns(true);
        assertEquals(4.5,
                CombatStrengths.forBattle(model, veteran, model.getUnitById(6))
                        .orElseThrow().getAttacker().getBase());
    }

    @Test
    void staleVeteranOrFounderInputsRejectBeforeRandomAndMutation()
            throws ReflectiveOperationException {
        final Model model = game(3L, UnitType.COLONIST, UnitType.COLONIST,
                false);
        final UnitFreeColonist attacker = (UnitFreeColonist) model
                .getUnitById(0);
        final AttackOrder veteranOffer = attacker.prepareAttack(TARGET, true)
                .orElseThrow();
        attacker.promoteAfterCombatVictory();
        final String changedVeteran = new Gson().toJson(model.save());
        assertThrows(IllegalStateException.class, veteranOffer::execute);
        assertEquals(changedVeteran, new Gson().toJson(model.save()));

        final Model founderModel = game(3L, UnitType.COLONIST,
                UnitType.COLONIST, false);
        final AttackOrder founderOffer = founderModel.getUnitById(0)
                .prepareAttack(TARGET, true).orElseThrow();
        final Player france = founderModel.getUnitById(6).getOwner();
        final Field congressField = Player.class.getDeclaredField("congress");
        congressField.setAccessible(true);
        final Object congress = congressField.get(france);
        final Field membersField = congress.getClass()
                .getDeclaredField("members");
        membersField.setAccessible(true);
        @SuppressWarnings("unchecked")
        final List<Founder> members = (List<Founder>) membersField
                .get(congress);
        members.add(Founder.GEORGE_WASHINGTON);
        final String changedFounder = new Gson().toJson(founderModel.save());
        assertThrows(IllegalStateException.class, founderOffer::execute);
        assertEquals(changedFounder, new Gson().toJson(founderModel.save()));
    }

    @Test
    void savedNonColonistVeteranFlagIsRejected() {
        final ModelPo data = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.microcol").getModel();
        data.getUnits().stream().filter(unit -> unit.getId() == 3).findFirst()
                .orElseThrow().setVeteran(true);
        assertThrows(IllegalArgumentException.class, () -> Model.make(data,
                MissionImpl.getPredefinedGameOverEvaluators()));
    }
}
