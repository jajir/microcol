package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.UnitFreeColonist;

import com.google.gson.Gson;

class AttackOrderTest {
    private final Gson gson = new Gson();
    private Model model;
    private UnitFreeColonist attacker;
    private UnitFreeColonist defender;

    @BeforeEach
    void prepareAdjacentArmedOpponentsAtPeace() {
        model = Model.make(new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.microcol").getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        model.startGame();
        while (!model.getCurrentPlayer().getName().equals("Dutch")) {
            model.endTurn();
        }
        attacker = (UnitFreeColonist) model.getUnitById(0);
        defender = (UnitFreeColonist) model.getUnitById(6);
        attacker.placeToLocation(Location.of(18, 10));
        defender.placeToLocation(Location.of(17, 11));
        attacker.setHoldingGuns(true);
        defender.setHoldingGuns(true);
        attacker.getOwner().revealMapForUnit(attacker);
    }

    private String snapshot() {
        return gson.toJson(model.save());
    }

    private AttackOrder proposal() {
        return attacker.prepareAttack(defender.getLocation(), true)
                .orElseThrow();
    }

    @Test
    void peacefulPreparationIsReadOnlyAndDoesNotAuthorizeTheAiCommand() {
        final String before = snapshot();
        assertFalse(attacker.isPossibleToAttackAt(defender.getLocation()));
        assertTrue(attacker.prepareAttack(defender.getLocation(), false)
                .isEmpty());
        final AttackOrder order = proposal();
        assertTrue(order.isDeclarationRequired());
        assertSame(model, order.getModel());
        assertSame(attacker, order.getAttacker());
        assertSame(defender, order.getDefender());
        assertThrows(IllegalStateException.class,
                () -> attacker.attack(defender.getLocation()));
        assertEquals(before, snapshot());
    }

    @ParameterizedTest
    @ValueSource(strings = { "attacker-moved", "defender-moved", "disarmed",
            "spent", "attacker-owner", "defender-owner", "attacker-removed",
            "defender-removed", "defender-replaced", "relation-changed",
            "wrong-turn", "stopped" })
    void changedConfirmationRejectsWithoutAnyFurtherMutation(
            final String change) {
        final AttackOrder order = proposal();
        switch (change) {
        case "attacker-moved":
            attacker.placeToLocation(Location.of(18, 11));
            break;
        case "defender-moved":
            defender.placeToLocation(Location.of(17, 12));
            break;
        case "disarmed":
            attacker.setHoldingGuns(false);
            break;
        case "spent":
            attacker.setActionPoints(0);
            break;
        case "attacker-owner":
            attacker.takeOver(defender.getOwner());
            break;
        case "defender-owner":
            defender.takeOver(attacker.getOwner());
            break;
        case "attacker-removed":
            model.destroyUnit(attacker);
            break;
        case "defender-removed":
            model.destroyUnit(defender);
            break;
        case "defender-replaced":
            final UnitPo replacement = defender.save();
            model.destroyUnit(defender);
            model.createUnit(model.save(), replacement);
            break;
        case "relation-changed":
            model.declareHostilities(attacker.getOwner(), defender.getOwner());
            break;
        case "wrong-turn":
            model.endTurn();
            break;
        case "stopped":
            model.stop();
            break;
        default:
            fail("Unknown test change: " + change);
        }
        final String before = snapshot();
        assertThrows(IllegalStateException.class, order::execute);
        assertEquals(before, snapshot());
    }

    @Test
    void targetFromAnotherGameWithTheSameIdCannotReplaceTheChosenDefender() {
        final Model other = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final String before = snapshot();
        assertThrows(IllegalArgumentException.class,
                () -> new AttackOrder(attacker,
                        other.getUnitById(defender.getId())));
        assertEquals(before, snapshot());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void acceptedBattleDeclaresOnceAndExhaustsEitherSurvivorThroughRestore(
            final boolean attackerWins) throws Exception {
        // Control both existing outcome branches without changing the
        // algorithm.
        final Field field = AbstractUnit.class.getDeclaredField("random");
        field.setAccessible(true);
        ((Random) field.get(attacker)).setSeed(attackerWins ? 4096L : 0L);
        final AttackOrder order = proposal();
        final int units = model.getAllUnits().size();
        order.execute();
        assertEquals(EuropeanRelationState.WAR, model
                .getEuropeanRelation(attacker.getOwner(), defender.getOwner()));
        assertEquals(units - 1, model.getAllUnits().size());
        final Unit survivor = attackerWins ? attacker : defender;
        final Unit lost = attackerWins ? defender : attacker;
        assertSame(survivor, model.getUnitById(survivor.getId()));
        assertTrue(model.tryGetUnitById(lost.getId()).isEmpty());
        assertEquals(0, survivor.getActionPoints());
        final String after = snapshot();
        assertThrows(IllegalStateException.class, order::execute);
        assertEquals(after, snapshot());

        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Unit continued = restored.getUnitById(survivor.getId());
        assertEquals(0, continued.getActionPoints());
        assertEquals(EuropeanRelationState.WAR,
                restored.getEuropeanRelation(restored.getPlayerByName("Dutch"),
                        restored.getPlayerByName("France")));
        // Resume the saved phase, then advance real player turns to the owner's
        // next turn.
        restored.startGame();
        assertEquals(0, continued.getActionPoints());
        int turns = 0;
        do {
            restored.endTurn();
            assertTrue(++turns <= restored.getPlayers().size());
            if (restored.getCurrentPlayer() != continued.getOwner()) {
                assertEquals(0, continued.getActionPoints());
            }
        } while (restored.getCurrentPlayer() != continued.getOwner());
        assertEquals(1, continued.getActionPoints());
    }
}
