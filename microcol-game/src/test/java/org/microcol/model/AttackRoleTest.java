package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.unit.UnitFreeColonist;

import com.google.gson.Gson;

class AttackRoleTest {
    private final Gson gson = new Gson();

    private Model border(final UnitType type) {
        final GameModelPo saved = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.microcol");
        saved.getModel().getUnits().stream().filter(unit -> unit.getId() == 0)
                .findFirst().orElseThrow().setType(type);
        final Model model = Model.make(saved.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        model.startGame();
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
    void musketsPermitLandAttackForColonistsAndSpecialists(
            final boolean specialist, final boolean guns,
            final boolean horses) {
        final Model model = border(
                specialist ? UnitType.MASTER_BLACKSMITH : UnitType.COLONIST);
        final UnitFreeColonist attacker = (UnitFreeColonist) model
                .getUnitById(0);
        attacker.setHoldingGuns(guns);
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

    @ParameterizedTest
    @CsvSource({ "false", "true" })
    void removingMusketsClosesAnEarlierLegalAttackWithoutEffects(
            final boolean specialist) {
        final Model model = border(
                specialist ? UnitType.MASTER_BLACKSMITH : UnitType.COLONIST);
        final UnitFreeColonist attacker = (UnitFreeColonist) model
                .getUnitById(0);
        attacker.setHoldingGuns(true);
        final Location target = model.getUnitById(6).getLocation();
        assertTrue(attacker.isPossibleToAttackAt(target));
        attacker.setHoldingGuns(false);
        final String before = gson.toJson(model.save());
        assertFalse(attacker.isPossibleToAttackAt(target));
        assertThrows(IllegalStateException.class,
                () -> attacker.attack(target));
        assertEquals(before, gson.toJson(model.save()));
    }
}
