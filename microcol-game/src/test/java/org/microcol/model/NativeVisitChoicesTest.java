package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.microcol.model.NativeRelationsScene.DRAGOON;
import static org.microcol.model.NativeRelationsScene.DUTCH;
import static org.microcol.model.NativeRelationsScene.FRANCE;
import static org.microcol.model.NativeRelationsScene.SCOUT;
import static org.microcol.model.NativeRelationsScene.SOLDIER;
import static org.microcol.model.NativeRelationsScene.UNARMED;
import static org.microcol.model.NativeRelationsScene.at;
import static org.microcol.model.NativeRelationsScene.island;
import static org.microcol.model.NativeRelationsScene.settlement;
import static org.microcol.model.NativeVisitChoice.Limit.AVAILABLE;
import static org.microcol.model.NativeVisitChoice.Limit.NOT_YET_AVAILABLE;
import static org.microcol.model.NativeVisitChoice.Limit.RELATION_REFUSED;
import static org.microcol.model.NativeVisitChoice.Limit.ROLE_REFUSED;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.store.CargoSlotPo;
import org.microcol.model.store.UnitPo;

import com.google.gson.Gson;

/**
 * Entry choices of a native settlement (R-0157): which roles the model admits
 * or refuses, the gates of current player, registered unit, range and learned
 * knowledge, the saved offered skill, and that asking changes nothing. Teaching
 * is available to an eligible visitor (R-0148); the other admitted actions are
 * still pending, since their owners are not integrated.
 */
class NativeVisitChoicesTest {

    private static final NativeVisitChoice.Limit OK = NOT_YET_AVAILABLE;

    private static final NativeVisitChoice.Limit TEACH = AVAILABLE;

    private static final NativeVisitChoice.Limit NO = ROLE_REFUSED;

    /**
     * Specialists, servants and criminals are colonists too, so their saved
     * units carry the colonist work history and missionary fields; the scene
     * adds them only for the plain colonist type.
     */
    private static final Consumer<UnitPo> COLONIST_FIELDS = unit -> {
        unit.setWorkUpdates(0);
        unit.setMissionary(false);
    };

    private static final Consumer<UnitPo> MISSIONARY = unit -> unit
            .setMissionary(true);

    private static final Consumer<UnitPo> LOADED = unit -> {
        final CargoSlotPo slot = new CargoSlotPo();
        slot.setGoodsType(GoodsType.CORN);
        slot.setAmount(50);
        unit.getCargo().getSlots().add(slot);
    };

    private static Map<NativeVisitAction, NativeVisitChoice.Limit> limits(
            final Model model, final Unit visitor) {
        final Map<NativeVisitAction, NativeVisitChoice.Limit> out = new EnumMap<>(
                NativeVisitAction.class);
        for (final NativeVisitChoice choice : model
                .getNativeVisitChoices(visitor, settlement(model))) {
            out.put(choice.getAction(), choice.getLimit());
        }
        return out;
    }

    private static Map<NativeVisitAction, NativeVisitChoice.Limit> limitsOf(
            final UnitType type, final Consumer<UnitPo> equipment) {
        final Model model = island(NativeNation.AZTEC)
                .unit(DUTCH, type, at(1, 0), equipment).toModel();
        return limits(model, model.getUnitById(200));
    }

    /**
     * Expected limits in the order chief, teaching, trade, tribute, mission,
     * attack.
     */
    private static void assertLimits(
            final Map<NativeVisitAction, NativeVisitChoice.Limit> actual,
            final NativeVisitChoice.Limit... expected) {
        assertEquals(NativeVisitAction.values().length, actual.size());
        final NativeVisitAction[] actions = NativeVisitAction.values();
        for (int i = 0; i < actions.length; i++) {
            assertEquals(expected[i], actual.get(actions[i]),
                    actions[i].name());
        }
    }

    @Test
    void ordinaryScoutMeetsChiefAndLearnsButCannotFight() {
        // A mounted colonist without guns is the ordinary scout.
        assertLimits(limitsOf(UnitType.COLONIST, SCOUT), OK, TEACH, NO, OK, NO,
                NO);
    }

    @Test
    void seasonedScoutMeetsChiefButIsNotAlreadyAPupil() {
        assertLimits(limitsOf(UnitType.SEASONED_SCOUT, COLONIST_FIELDS), OK, NO,
                NO, OK, NO, NO);
    }

    @Test
    void soldierAndDragoonDemandTributeAndAttackButAreNoScouts() {
        assertLimits(limitsOf(UnitType.COLONIST, SOLDIER), NO, TEACH, NO, OK,
                NO, OK);
        assertLimits(limitsOf(UnitType.COLONIST, DRAGOON), NO, TEACH, NO, OK,
                NO, OK);
    }

    @Test
    void unskilledFreeColonistAndServantMayLearnOnly() {
        assertLimits(limitsOf(UnitType.COLONIST, UNARMED), NO, TEACH, NO, NO,
                NO, NO);
        assertLimits(limitsOf(UnitType.INDENTURED_SERVANT, COLONIST_FIELDS), NO,
                TEACH, NO, NO, NO, NO);
    }

    @Test
    void criminalAndExistingExpertCannotLearn() {
        assertLimits(limitsOf(UnitType.PETTY_CRIMINAL, COLONIST_FIELDS), NO, NO,
                NO, NO, NO, NO);
        assertLimits(limitsOf(UnitType.EXPERT_FARMER, COLONIST_FIELDS), NO, NO,
                NO, NO, NO, NO);
    }

    @Test
    void equippedPioneerIsJudgedAsTheUnskilledPersonUnderneath() {
        assertLimits(limitsOf(UnitType.COLONIST, NativeRelationsScene.PIONEER),
                NO, TEACH, NO, NO, NO, NO);
    }

    @Test
    void missionaryMayEstablishAMission() {
        assertLimits(limitsOf(UnitType.COLONIST, MISSIONARY), NO, TEACH, NO, NO,
                OK, NO);
    }

    @Test
    void onlyALoadedWagonMayTrade() {
        assertLimits(limitsOf(UnitType.WAGON, UNARMED), NO, NO, NO, NO, NO, NO);
        assertLimits(limitsOf(UnitType.WAGON, LOADED), NO, NO, OK, NO, NO, NO);
    }

    @Test
    void artilleryAttacksOnlyThroughTheSharedAttackRule() {
        assertLimits(limitsOf(UnitType.ARTILLERY, UNARMED), NO, NO, NO, NO, NO,
                OK);
    }

    @Test
    void angryNativesRefuseTeachingAndTradeButNotOtherRoles() {
        final Model model = island(NativeNation.AZTEC)
                .unit(DUTCH, UnitType.COLONIST, at(1, 0), SCOUT)
                .unit(DUTCH, UnitType.WAGON, at(0, 1), LOADED)
                .tribal(NativeNation.AZTEC, EuropeanNation.DUTCH, 80).toModel();
        assertLimits(limits(model, model.getUnitById(200)), OK,
                RELATION_REFUSED, NO, OK, NO, NO);
        assertLimits(limits(model, model.getUnitById(201)), NO, NO,
                RELATION_REFUSED, NO, NO, NO);
    }

    @Test
    void uneasyNativesStillOfferTeaching() {
        final Model model = island(NativeNation.AZTEC)
                .unit(DUTCH, UnitType.COLONIST, at(1, 0), UNARMED)
                .tribal(NativeNation.AZTEC, EuropeanNation.DUTCH, 30).toModel();
        assertLimits(limits(model, model.getUnitById(200)), NO, TEACH, NO, NO,
                NO, NO);
    }

    @Test
    void nonAdjacentOrUnlearnedEntryOffersNothing() {
        // The scout sees two squares, so it knows the settlement from here but
        // is not beside it.
        final Model seen = island(NativeNation.AZTEC)
                .unit(DUTCH, UnitType.COLONIST, at(2, 0), SCOUT).toModel();
        final Player dutch = seen.getPlayerByName(DUTCH);
        assertTrue(seen.getNativeSettlementKnownTo(dutch,
                settlement(seen).getLocation()).isPresent());
        assertTrue(limits(seen, seen.getUnitById(200)).isEmpty());
        // An unarmed colonist far away has not learned the settlement.
        final Model unseen = island(NativeNation.AZTEC)
                .unit(DUTCH, UnitType.COLONIST, at(4, 0), UNARMED).toModel();
        assertTrue(
                unseen.getNativeSettlementKnownTo(unseen.getPlayerByName(DUTCH),
                        settlement(unseen).getLocation()).isEmpty());
        assertTrue(limits(unseen, unseen.getUnitById(200)).isEmpty());
    }

    @Test
    void foreignOrOutOfTurnVisitorOffersNothing() {
        final Model foreign = island(NativeNation.AZTEC)
                .unit(FRANCE, UnitType.COLONIST, at(1, 0), SCOUT).toModel();
        assertTrue(limits(foreign, foreign.getUnitById(200)).isEmpty());
        final Model outOfTurn = island(NativeNation.AZTEC)
                .unit(DUTCH, UnitType.COLONIST, at(1, 0), SCOUT)
                .currentPlayer(FRANCE).toModel();
        assertTrue(limits(outOfTurn, outOfTurn.getUnitById(200)).isEmpty());
    }

    @Test
    void unitOfAnotherModelIsNotRegisteredVisitor() {
        final Model live = island(NativeNation.AZTEC)
                .unit(DUTCH, UnitType.COLONIST, at(1, 0), SCOUT).toModel();
        final Model stale = island(NativeNation.AZTEC)
                .unit(DUTCH, UnitType.COLONIST, at(1, 0), SCOUT).toModel();
        assertTrue(live
                .getNativeVisitChoices(stale.getUnitById(200), settlement(live))
                .isEmpty());
    }

    @Test
    void askingChangesNothingAndTheChoicesCannotBeChanged() {
        final Model model = island(NativeNation.AZTEC)
                .unit(DUTCH, UnitType.COLONIST, at(1, 0), SCOUT).toModel();
        final Gson gson = new Gson();
        final String before = gson.toJson(model.save());
        final List<NativeVisitChoice> choices = model.getNativeVisitChoices(
                model.getUnitById(200), settlement(model));
        model.getNativeVisitChoices(model.getUnitById(200), settlement(model));
        assertEquals(before, gson.toJson(model.save()));
        assertThrows(UnsupportedOperationException.class,
                () -> choices.add(choices.get(0)));
    }

    @Test
    void europeanNeverStepsOntoOrRoutesThroughTheSettlement() {
        final Model model = island(NativeNation.AZTEC)
                .unit(DUTCH, UnitType.COLONIST, at(-1, 0), SOLDIER)
                .withMovesForLastUnit().toModel();
        final Unit soldier = model.getUnitById(200);
        final Location site = settlement(model).getLocation();
        assertFalse(soldier.isPossibleToMoveAt(site));
        assertFalse(soldier.isPossibleToMoveAt(site, true));
        final List<Location> path = soldier.getPath(at(1, 0)).orElseThrow();
        assertFalse(path.contains(site), path.toString());
        assertEquals(at(1, 0), path.get(path.size() - 1));
    }

    @Test
    void offeredSkillIsSavedAndNeverRedrawnByLoading() {
        final Model model = island(NativeNation.AZTEC).toModel();
        assertEquals(UnitType.EXPERT_FARMER,
                settlement(model).getTeachableSkill());
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertEquals(UnitType.EXPERT_FARMER,
                settlement(restored).getTeachableSkill());
        assertEquals("EXPERT_FARMER", restored.save().getNativeSettlements()
                .get(0).getTeachableSkill());
    }

}
