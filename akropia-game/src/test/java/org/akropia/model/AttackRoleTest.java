package org.akropia.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.akropia.model.campaign.MissionImpl;
import org.akropia.model.campaign.po.GameModelPo;
import org.akropia.model.campaign.po.GameModelPoDao;
import org.akropia.model.unit.UnitFreeColonist;

import com.google.gson.Gson;

class AttackRoleTest {
    private final Gson gson = new Gson();

    private Model border(final UnitType type) {
        final GameModelPo saved = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.akropia");
        saved.getModel().getUnits().stream().filter(unit -> unit.getId() == 0)
                .findFirst().orElseThrow().setType(type);
        final Model model = Model.make(saved.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        model.startGame();
        while (!model.getCurrentPlayer().getName().equals("Dutch")) {
            model.endTurn();
        }
        model.getUnitById(0).placeToLocation(Location.of(18, 10));
        model.getUnitById(6).placeToLocation(Location.of(17, 11));
        model.getUnitById(0).getOwner().revealMapForUnit(model.getUnitById(0));
        model.declareHostilities(model.getPlayerByName("Dutch"),
                model.getPlayerByName("France"));
        return model;
    }

    @ParameterizedTest
    @CsvSource({ "false,false,false", "false,false,true", "false,true,false",
            "false,true,true", "true,false,false", "true,false,true",
            "true,true,false", "true,true,true" })
    void weaponsPermitLandAttackForColonistsAndSpecialists(
            final boolean specialist, final boolean guns,
            final boolean horses) {
        final Model model = border(
                specialist ? UnitType.MASTER_BLACKSMITH : UnitType.COLONIST);
        final UnitFreeColonist attacker = (UnitFreeColonist) model
                .getUnitById(0);
        attacker.setHoldingWeapons(guns);
        attacker.setMounted(horses);
        attacker.setTools(guns ? 0 : 100);
        final Location target = model.getUnitById(6).getLocation();
        final String before = gson.toJson(model.save());
        assertEquals(guns, attacker.isPossibleToAttackAt(target));
        assertEquals(before, gson.toJson(model.save()));
        if (!guns) {
            assertThrows(IllegalStateException.class,
                    () -> attacker.attack(target));
            assertEquals(before, gson.toJson(model.save()));
        }
    }

    /**
     * R-0028 chapter 4: a purchased Seasoned Scout is a supported specialist.
     * Weapons confer the soldier role and horses then the dragoon base 3, so
     * there is no unarmed open-field scout attack and no separate specialty.
     */
    @Test
    void seasonedScoutAttacksOnlyWithWeaponsAndThenFightsAsAnOrdinaryDragoon() {
        final Model model = border(UnitType.SEASONED_SCOUT);
        final UnitFreeColonist scout = (UnitFreeColonist) model.getUnitById(0);
        final Location target = model.getUnitById(6).getLocation();
        scout.setTools(0);
        scout.setMounted(true);
        scout.setHoldingWeapons(false);
        assertFalse(scout.isPossibleToAttackAt(target));
        scout.setHoldingWeapons(true);
        assertTrue(scout.isPossibleToAttackAt(target));
        final CombatStrengths strengths = scout.prepareAttack(target, true)
                .orElseThrow().getCombatStrengths().orElseThrow();
        assertEquals(3.0, strengths.getAttacker().getBase(), 1e-9);
        assertEquals(4.5, strengths.getAttacker().getTotal(), 1e-9);
        assertSame(UnitType.SEASONED_SCOUT, scout.getType());
    }

    @ParameterizedTest
    @CsvSource({ "false", "true" })
    void removingWeaponsClosesAnEarlierLegalAttackWithoutEffects(
            final boolean specialist) {
        final Model model = border(
                specialist ? UnitType.MASTER_BLACKSMITH : UnitType.COLONIST);
        final UnitFreeColonist attacker = (UnitFreeColonist) model
                .getUnitById(0);
        attacker.setHoldingWeapons(true);
        final Location target = model.getUnitById(6).getLocation();
        assertTrue(attacker.isPossibleToAttackAt(target));
        attacker.setHoldingWeapons(false);
        final String before = gson.toJson(model.save());
        assertFalse(attacker.isPossibleToAttackAt(target));
        assertThrows(IllegalStateException.class,
                () -> attacker.attack(target));
        assertEquals(before, gson.toJson(model.save()));
    }
}
