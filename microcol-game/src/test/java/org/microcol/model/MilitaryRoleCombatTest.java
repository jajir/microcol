package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.event.UnitAttackedEvent;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.UnitActionNoActionPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.UnitFreeColonist;

import com.google.gson.Gson;

/**
 * Continental and royal infantry/cavalry: chart bases 4/5 and 5/6 in the shared
 * preview and resolver, and destruction with equipment when defeated. The
 * ordinary veteran controls keep their old strength and equipment loss.
 */
class MilitaryRoleCombatTest {

    private static final String MAP = "/maps/default-campaign-mission-2-thrive.microcol";

    private static final Location ORIGIN = Location.of(27, 10);

    private static final Location TARGET = Location.of(28, 10);

    private static final int ATTACKER = 9001;

    private static final int DEFENDER = 9002;

    private static final double EPSILON = 1e-9;

    private UnitPo soldier(final int id, final String owner, final Location at,
            final boolean mounted, final boolean veteran,
            final MilitaryRole role) {
        final PlaceMapPo place = new PlaceMapPo();
        place.setLocation(at);
        place.setOrientation(Direction.east);
        final UnitPo out = new UnitPo();
        out.setId(id);
        out.setType(UnitType.COLONIST);
        out.setOwnerId(owner);
        out.setPlaceMap(place);
        out.setAction(new UnitActionNoActionPo());
        out.setAvailableMoves(1);
        out.setHoldingGuns(true);
        out.setMounted(mounted);
        out.setVeteran(veteran);
        out.setMilitaryRole(role);
        out.setWorkUpdates(0);
        out.setMissionary(false);
        return out;
    }

    /** A Dutch attacker beside a Crown defender on plain open ground. */
    private Model game(final long state, final UnitPo attacker,
            final UnitPo defender) {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(MAP);
        data.getModel().getCalendar().setEndYear(1900);
        data.getModel().setCombatRandomState(state);
        final Map<Location, TerrainType> terrain = data.getModel().getMap()
                .getTerrainMap();
        terrain.put(ORIGIN, TerrainType.GRASSLAND);
        terrain.put(TARGET, TerrainType.GRASSLAND);
        data.getModel().getMap().setTerrainType(terrain);
        final Set<Location> trees = data.getModel().getMap().getTreeSet();
        trees.remove(ORIGIN);
        trees.remove(TARGET);
        data.getModel().getMap().setTrees(trees);
        data.getModel().getGameManager().getPlayers().stream()
                .filter(player -> "Dutch".equals(player.getName()))
                .forEach(player -> RebellionFixtures.declare(data.getModel(),
                        player));
        data.getModel().getUnits().add(attacker);
        data.getModel().getUnits().add(defender);
        final Model model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Unit actor = model.getUnitById(ATTACKER);
        actor.getOwner().revealMapForUnit(actor);
        return model;
    }

    private AttackOrder order(final Model model) {
        return colonist(model, ATTACKER).prepareAttack(TARGET, true)
                .orElseThrow();
    }

    private UnitFreeColonist colonist(final Model model, final int id) {
        return (UnitFreeColonist) model.getUnitById(id);
    }

    /**
     * First saved random state whose single draw makes the attacker win or lose
     * against the quoted odds of the same battle.
     */
    private long stateWhere(final boolean attackerWins,
            final Function<Long, Model> factory) {
        for (long state = 0; state < 512; state++) {
            final double chance = order(factory.apply(state))
                    .getCombatStrengths().orElseThrow()
                    .getAttackerWinProbability();
            final double draw = factory.apply(state).nextCombatRandom();
            if (draw < chance == attackerWins) {
                return state;
            }
        }
        throw new AssertionError("No suitable saved draw");
    }

    @ParameterizedTest
    @CsvSource({ "false,4.0", "true,5.0" })
    void continentalInfantryAndCavalryUseTheChartBases(final boolean mounted,
            final double base) {
        final Model model = game(0L,
                soldier(ATTACKER, "Dutch", ORIGIN, mounted, true,
                        MilitaryRole.CONTINENTAL),
                soldier(DEFENDER, "Dutch's King", TARGET, false, false,
                        MilitaryRole.COLONIAL));
        final CombatStrengths strengths = order(model).getCombatStrengths()
                .orElseThrow();
        assertEquals(base, strengths.getAttacker().getBase(), EPSILON);
        // No second veteran bonus: the base is the role chart value.
        assertEquals(base * 1.5, strengths.getAttacker().getTotal(), EPSILON);
        assertEquals(base, colonist(model, ATTACKER).getMilitaryStrenght(),
                EPSILON);
    }

    @ParameterizedTest
    @CsvSource({ "false,5.0", "true,6.0" })
    void royalInfantryAndCavalryUseTheChartBases(final boolean mounted,
            final double base) {
        final Model model = game(0L,
                soldier(ATTACKER, "Dutch", ORIGIN, false, false,
                        MilitaryRole.COLONIAL),
                soldier(DEFENDER, "Dutch's King", TARGET, mounted, false,
                        MilitaryRole.ROYAL));
        final CombatStrengths strengths = order(model).getCombatStrengths()
                .orElseThrow();
        assertEquals(base, strengths.getDefender().getBase(), EPSILON);
        assertEquals(base, strengths.getDefender().getTotal(), EPSILON);
        assertEquals(base, colonist(model, DEFENDER).getMilitaryStrenght(),
                EPSILON);
    }

    @Test
    void ordinaryVeteransKeepTheirOwnStrength() {
        final Model model = game(0L,
                soldier(ATTACKER, "Dutch", ORIGIN, false, true,
                        MilitaryRole.COLONIAL),
                soldier(DEFENDER, "Dutch's King", TARGET, true, true,
                        MilitaryRole.COLONIAL));
        final CombatStrengths strengths = order(model).getCombatStrengths()
                .orElseThrow();
        assertEquals(3.0, strengths.getAttacker().getBase(), EPSILON);
        assertEquals(4.5, strengths.getDefender().getBase(), EPSILON);
    }

    @Test
    void aRoleChangeInvalidatesAStaleBattleSnapshot() {
        final Model model = game(0L,
                soldier(ATTACKER, "Dutch", ORIGIN, false, true,
                        MilitaryRole.COLONIAL),
                soldier(DEFENDER, "Dutch's King", TARGET, false, false,
                        MilitaryRole.COLONIAL));
        final AttackOrder stale = order(model);
        final String before = new Gson().toJson(model.save());
        colonist(model, ATTACKER).assignMilitaryRole(MilitaryRole.CONTINENTAL);
        final String changed = new Gson().toJson(model.save());
        assertNotEquals(before, changed);
        assertThrows(IllegalStateException.class, stale::execute);
        assertEquals(changed, new Gson().toJson(model.save()));
        assertNotEquals(stale.getCombatStrengths(),
                order(model).getCombatStrengths());
    }

    @Test
    void aDefeatedContinentalIsDestroyedWithItsEquipment() {
        final Function<Long, Model> factory = state -> game(state,
                soldier(ATTACKER, "Dutch", ORIGIN, true, true,
                        MilitaryRole.CONTINENTAL),
                soldier(DEFENDER, "Dutch's King", TARGET, false, false,
                        MilitaryRole.COLONIAL));
        final Model model = factory.apply(stateWhere(false, factory));
        final UnitFreeColonist continental = colonist(model, ATTACKER);
        final int unitsBefore = model.getAllUnits().size();
        final List<UnitAttackedEvent> events = new ArrayList<>();
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onUnitAttacked(final UnitAttackedEvent event) {
                events.add(event);
            }
        });
        order(model).execute();
        assertEquals(1, events.size());
        assertTrue(events.get(0).wasDestroyed());
        assertSame(continental, events.get(0).getDestroyed());
        assertEquals(unitsBefore - 1, model.getAllUnits().size());
        assertFalse(model.getAllUnits().contains(continental));
        // It neither survives as an ordinary colonist nor leaves anything.
        assertTrue(model.getUnitsAt(ORIGIN).isEmpty());
        assertTrue(model.getAllUnits().stream()
                .noneMatch(unit -> unit.getId() == ATTACKER));
        assertTrue(colonist(model, DEFENDER).isHoldingGuns());
    }

    @Test
    void aDefeatedRoyalIsDestroyedAndTheWinnerKeepsItsEquipment() {
        final Function<Long, Model> factory = state -> game(state,
                soldier(ATTACKER, "Dutch", ORIGIN, true, false,
                        MilitaryRole.COLONIAL),
                soldier(DEFENDER, "Dutch's King", TARGET, false, false,
                        MilitaryRole.ROYAL));
        final Model model = factory.apply(stateWhere(true, factory));
        final UnitFreeColonist winner = colonist(model, ATTACKER);
        final int unitsBefore = model.getAllUnits().size();
        order(model).execute();
        assertEquals(unitsBefore - 1, model.getAllUnits().size());
        assertTrue(model.getAllUnits().stream()
                .noneMatch(unit -> unit.getId() == DEFENDER));
        assertSame(winner, model.getUnitById(ATTACKER));
        assertTrue(winner.isHoldingGuns());
        assertTrue(winner.isMounted());
    }

    @Test
    void aVictoriousRoleKeepsEverythingAndGainsNoPromotion() {
        final Function<Long, Model> factory = state -> game(state,
                soldier(ATTACKER, "Dutch", ORIGIN, false, false,
                        MilitaryRole.COLONIAL),
                soldier(DEFENDER, "Dutch's King", TARGET, true, false,
                        MilitaryRole.ROYAL));
        final Model model = factory.apply(stateWhere(false, factory));
        final UnitFreeColonist royal = colonist(model, DEFENDER);
        final List<UnitAttackedEvent> events = new ArrayList<>();
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onUnitAttacked(final UnitAttackedEvent event) {
                events.add(event);
            }
        });
        order(model).execute();
        assertSame(royal, model.getUnitById(DEFENDER));
        assertTrue(royal.isHoldingGuns());
        assertTrue(royal.isMounted());
        assertFalse(royal.isVeteran());
        assertEquals(MilitaryRole.ROYAL, royal.getMilitaryRole());
        assertEquals(1, events.size());
        assertTrue(events.get(0).getPromotion().isEmpty());
        // The ordinary attacker is not destroyed: it only loses its muskets.
        assertFalse(colonist(model, ATTACKER).isHoldingGuns());
    }

    @Test
    void anOrdinaryDefeatedVeteranStillLosesOnlyItsEquipment() {
        final Function<Long, Model> factory = state -> game(state,
                soldier(ATTACKER, "Dutch", ORIGIN, true, false,
                        MilitaryRole.COLONIAL),
                soldier(DEFENDER, "Dutch's King", TARGET, false, true,
                        MilitaryRole.COLONIAL));
        final Model model = factory.apply(stateWhere(true, factory));
        final int unitsBefore = model.getAllUnits().size();
        order(model).execute();
        final UnitFreeColonist defeated = colonist(model, DEFENDER);
        assertNotNull(defeated);
        assertEquals(unitsBefore, model.getAllUnits().size());
        assertFalse(defeated.isHoldingGuns());
        assertTrue(defeated.isVeteran());
        assertEquals(MilitaryRole.COLONIAL, defeated.getMilitaryRole());
    }

    @Test
    void theRoleSurvivesSaveAndContinueAndAbsentRolesStayOrdinary() {
        final Model model = game(0L,
                soldier(ATTACKER, "Dutch", ORIGIN, false, true,
                        MilitaryRole.CONTINENTAL),
                soldier(DEFENDER, "Dutch's King", TARGET, false, false,
                        MilitaryRole.ROYAL));
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertEquals(MilitaryRole.CONTINENTAL,
                colonist(restored, ATTACKER).getMilitaryRole());
        assertEquals(MilitaryRole.ROYAL,
                colonist(restored, DEFENDER).getMilitaryRole());
        assertEquals(new Gson().toJson(model.save()),
                new Gson().toJson(restored.save()));
        // A resident worker never carries a role.
        assertEquals(MilitaryRole.COLONIAL,
                colonist(restored, 0).getMilitaryRole());
    }

    @Test
    void aCapturedPersonReturnsToTheOrdinaryRole() {
        final Model model = game(0L,
                soldier(ATTACKER, "Dutch", ORIGIN, false, true,
                        MilitaryRole.CONTINENTAL),
                soldier(DEFENDER, "Dutch's King", TARGET, false, false,
                        MilitaryRole.ROYAL));
        final UnitFreeColonist continental = colonist(model, ATTACKER);
        // A rejected call changes neither owner nor role.
        assertThrows(NullPointerException.class,
                () -> continental.takeOver(null));
        assertEquals(MilitaryRole.CONTINENTAL, continental.getMilitaryRole());
        assertEquals("Dutch", continental.getOwner().getName());
        continental.takeOver(model.getPlayerByName("Dutch's King"));
        assertEquals(MilitaryRole.COLONIAL, continental.getMilitaryRole());
    }
}
