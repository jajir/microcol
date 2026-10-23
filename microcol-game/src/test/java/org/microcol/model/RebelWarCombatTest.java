package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ColonyPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.UnitActionNoActionPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.UnitFreeColonist;

import com.google.gson.Gson;

/**
 * Revolution battle modifiers (R-0178): a royal regular bombards a rebel colony
 * only for its own Crown, intervention soldiers bombard a colony held by their
 * own Crown, and a rebel gains ambush in the open only against its own Crown's
 * regulars. Preview and resolver read the same snapshot. A revolutionary colony
 * attack also adds its cause support (R-0188): the exact rebel share of the
 * colony to its rebel attacker, or the complementary Tory share to the Crown.
 *
 * <p>
 * The world is the free-play map: France holds Quebec (17,10) and Montreal, its
 * armed unit 6 stands on Quebec's tile and its only resident, unit 4, works a
 * field. The fields (18,10) east and (17,11) south of Quebec are idle, so the
 * tests may reshape them.
 */
class RebelWarCombatTest {

    private static final String MAP = "/maps/free-play.microcol";

    /** Colonial nation declared independent in these tests. */
    private static final String REBEL = "France";

    /** Crown ruling the rebel nation. */
    private static final String OWN_CROWN = "France's King";

    /** Crown of the other nation, with no claim on the rebel colony. */
    private static final String OTHER_CROWN = "Dutch's King";

    /** Ordinary colonial nation, neither rebel nor Crown. */
    private static final String RIVAL = "Dutch";

    private static final Location QUEBEC = Location.of(17, 10);

    private static final Location ORIGIN = Location.of(18, 10);

    private static final Location TARGET = Location.of(17, 11);

    /** Armed France soldier standing on Quebec's tile. */
    private static final int GARRISON = 6;

    /** The only resident of Quebec, working a field. */
    private static final int RESIDENT = 4;

    private static final int ATTACKER = 9001;

    private static final int DEFENDER = 9002;

    /** Second rebel soldier, which captures Quebec in the capture test. */
    private static final int CAPTURER = 9003;

    private static final double EPSILON = 1e-9;

    private UnitPo soldier(final int id, final String owner, final Location at,
            final boolean mounted, final MilitaryRole role) {
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
        out.setVeteran(false);
        out.setMilitaryRole(role);
        out.setWorkUpdates(0);
        out.setMissionary(false);
        return out;
    }

    private static GameModelPo load() {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(MAP);
        data.getModel().getCalendar().setEndYear(1900);
        data.getModel().setCombatRandomState(0L);
        return data;
    }

    private static void declare(final ModelPo data, final String nation,
            final boolean declared) {
        data.getGameManager().getPlayers().stream()
                .filter(player -> nation.equals(player.getName()))
                .forEach(player -> RebellionFixtures.set(data, player,
                        declared));
    }

    private static void declare(final ModelPo data, final boolean declared) {
        declare(data, REBEL, declared);
    }

    /** Make a square open grassland without trees. */
    private static void flatten(final ModelPo data, final Location square) {
        final Map<Location, TerrainType> types = data.getMap().getTerrainMap();
        types.put(square, TerrainType.GRASSLAND);
        data.getMap().setTerrainType(types);
        final Set<Location> forest = data.getMap().getTreeSet();
        forest.remove(square);
        data.getMap().setTrees(forest);
    }

    /**
     * Model with the attacker's owner to move, the target tile set to the given
     * terrain and trees, and both units placed on it.
     */
    private Model field(final TerrainType terrain, final boolean trees,
            final boolean declared, final UnitPo attacker,
            final UnitPo defender) {
        final GameModelPo data = load();
        flatten(data.getModel(), ORIGIN);
        final Map<Location, TerrainType> types = data.getModel().getMap()
                .getTerrainMap();
        types.put(TARGET, terrain);
        data.getModel().getMap().setTerrainType(types);
        final Set<Location> forest = data.getModel().getMap().getTreeSet();
        forest.remove(TARGET);
        if (trees) {
            forest.add(TARGET);
        }
        data.getModel().getMap().setTrees(forest);
        declare(data.getModel(), declared);
        data.getModel().getUnits().add(attacker);
        data.getModel().getUnits().add(defender);
        return start(data, attacker);
    }

    /**
     * Model in which the attacker stands on the open field east of Quebec. The
     * colony is France's own, or, when a Crown name is given, held by that
     * Crown as a real occupation leaves it: the colony, its only resident and
     * its garrison belong to the Crown while the colonial affiliation stays
     * France. A Continental attacker's own nation is declared as well.
     */
    private Model colonyWorld(final String colonyOwner, final boolean declared,
            final UnitPo attacker) {
        return colonyWorld(colonyOwner, declared, attacker, 0L);
    }

    /**
     * As {@link #colonyWorld(String, boolean, UnitPo)}, with Quebec's stored
     * liberty. Its one resident makes the rebel share exactly liberty / 200.
     */
    private Model colonyWorld(final String colonyOwner, final boolean declared,
            final UnitPo attacker, final long liberty) {
        final GameModelPo data = load();
        declare(data.getModel(), declared);
        if (declared
                && attacker.getMilitaryRole() == MilitaryRole.CONTINENTAL) {
            declare(data.getModel(), attacker.getOwnerId(), true);
        }
        flatten(data.getModel(), ORIGIN);
        final ColonyPo quebec = data.getModel().getColonies().stream()
                .filter(colony -> colony.getId() == 0).findFirst()
                .orElseThrow();
        quebec.setLiberty(liberty);
        if (!REBEL.equals(colonyOwner)) {
            quebec.setOwnerName(colonyOwner);
            data.getModel().getUnits().stream()
                    .filter(unit -> unit.getId() == RESIDENT
                            || unit.getId() == GARRISON)
                    .forEach(unit -> unit.setOwnerId(colonyOwner));
        }
        data.getModel().getUnits().add(attacker);
        return start(data, attacker);
    }

    private Model start(final GameModelPo data, final UnitPo attacker) {
        data.getModel().getGameManager().setGameStarted(true);
        data.getModel().getGameManager()
                .setCurrentPlayer(attacker.getOwnerId());
        final Model model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Unit actor = model.getUnitById(ATTACKER);
        actor.getOwner().revealMapForUnit(actor);
        return model;
    }

    private UnitFreeColonist colonist(final Model model, final int id) {
        return (UnitFreeColonist) model.getUnitById(id);
    }

    private CombatStrengths quote(final Model model, final Location target) {
        return model.getUnitById(ATTACKER).prepareAttack(target, true)
                .orElseThrow().getCombatStrengths().orElseThrow();
    }

    private CombatStrengths quote(final Model model) {
        return quote(model, TARGET);
    }

    @ParameterizedTest
    @CsvSource({ "COLONIAL", "CONTINENTAL" })
    void rebelsAttackingTheirOwnCrownsRegularsGainTheForestAmbush(
            final MilitaryRole role) {
        final Model model = field(TerrainType.GRASSLAND, true, true,
                soldier(ATTACKER, REBEL, ORIGIN, false, role), soldier(DEFENDER,
                        OWN_CROWN, TARGET, false, MilitaryRole.ROYAL));
        final CombatStrengths values = quote(model);
        final CombatStrength attacker = values.getAttacker();
        assertEquals(50, attacker.getRebelAmbushBonus());
        assertEquals(0, attacker.getTerrainBonus());
        // Attack 50 and ambush 50 add once: base 2 x 2 for a colonial soldier.
        final double base = role == MilitaryRole.CONTINENTAL ? 4.0 : 2.0;
        assertEquals(base * 2.0, attacker.getTotal(), EPSILON);
        // The Crown regular keeps its ordinary forest defense: the ambush
        // offsets it, and nothing is taken away or added on the defender side.
        final CombatStrength defender = values.getDefender();
        assertEquals(50, defender.getTerrainBonus());
        assertEquals(0, defender.getRebelAmbushBonus());
        assertEquals(5.0 * 1.5, defender.getTotal(), EPSILON);
    }

    @ParameterizedTest
    @CsvSource({ "GRASSLAND,true,50", "SWAMP,true,75", "HILL,false,100",
            "MOUNTAIN,false,150", "SWAMP,false,0", "MARSH,false,0",
            "PLAINS,false,0" })
    void ambushUsesOnlyTheCardValuesOfTheDefenderTile(final String name,
            final boolean trees, final int bonus) {
        final Model model = field(TerrainType.valueOf(name), trees, true,
                soldier(ATTACKER, REBEL, ORIGIN, false, MilitaryRole.COLONIAL),
                soldier(DEFENDER, OWN_CROWN, TARGET, false,
                        MilitaryRole.ROYAL));
        assertEquals(bonus, quote(model).getAttacker().getRebelAmbushBonus(),
                name);
    }

    @Test
    void rebelAmbushIsNotGrantedBeforeDeclaration() {
        final Model model = field(TerrainType.GRASSLAND, true, false,
                soldier(ATTACKER, REBEL, ORIGIN, false, MilitaryRole.COLONIAL),
                soldier(DEFENDER, OWN_CROWN, TARGET, false,
                        MilitaryRole.ROYAL));
        assertEquals(0, quote(model).getAttacker().getRebelAmbushBonus());
    }

    @Test
    void rebelAmbushIsNotGrantedForAnotherCrownsRegularsOrOrdinarySoldiers() {
        final Model other = field(TerrainType.GRASSLAND, true, true,
                soldier(ATTACKER, REBEL, ORIGIN, false, MilitaryRole.COLONIAL),
                soldier(DEFENDER, OTHER_CROWN, TARGET, false,
                        MilitaryRole.ROYAL));
        assertEquals(0, quote(other).getAttacker().getRebelAmbushBonus());
        final Model ordinary = field(TerrainType.GRASSLAND, true, true,
                soldier(ATTACKER, REBEL, ORIGIN, false, MilitaryRole.COLONIAL),
                soldier(DEFENDER, OWN_CROWN, TARGET, false,
                        MilitaryRole.COLONIAL));
        assertEquals(0, quote(ordinary).getAttacker().getRebelAmbushBonus());
        final Model rival = field(TerrainType.GRASSLAND, true, true,
                soldier(ATTACKER, REBEL, ORIGIN, false, MilitaryRole.COLONIAL),
                soldier(DEFENDER, RIVAL, TARGET, false, MilitaryRole.COLONIAL));
        assertEquals(0, quote(rival).getAttacker().getRebelAmbushBonus());
    }

    @Test
    void aDefendingRebelKeepsItsOrdinaryTerrainWithoutASecondTerm() {
        final Model model = field(TerrainType.HILL, false, true,
                soldier(ATTACKER, OWN_CROWN, ORIGIN, false, MilitaryRole.ROYAL),
                soldier(DEFENDER, REBEL, TARGET, false, MilitaryRole.COLONIAL));
        final CombatStrengths values = quote(model);
        assertEquals(0, values.getAttacker().getRebelAmbushBonus());
        // The defender's ordinary hill terrain is the only defensive term:
        // base 2 x (1 + 1.0) = 4, with no second ambush term.
        assertEquals(100, values.getDefender().getTerrainBonus());
        assertEquals(0, values.getDefender().getRebelAmbushBonus());
        assertEquals(4.0, values.getDefender().getTotal(), EPSILON);
    }

    @Test
    void rebelAmbushStacksOnceWithTheOrdinaryAttackBonus() {
        final Model model = field(TerrainType.HILL, false, true,
                soldier(ATTACKER, REBEL, ORIGIN, false, MilitaryRole.COLONIAL),
                soldier(DEFENDER, OWN_CROWN, TARGET, false,
                        MilitaryRole.ROYAL));
        final CombatStrengths values = quote(model);
        // Base 2 x (1 + 0.5 attack + 1.0 hill ambush) = 5, exactly.
        assertEquals(5.0, values.getAttacker().getTotal(), EPSILON);
        assertEquals(10.0, values.getDefender().getTotal(), EPSILON);
        assertEquals(5.0 / 15.0, values.getAttackerWinProbability(), EPSILON);
    }

    @Test
    void royalBombardmentIsNotGrantedInTheOpenField() {
        final Model model = field(TerrainType.GRASSLAND, false, true,
                soldier(ATTACKER, OWN_CROWN, ORIGIN, false, MilitaryRole.ROYAL),
                soldier(DEFENDER, REBEL, TARGET, false, MilitaryRole.COLONIAL));
        assertEquals(0, quote(model).getAttacker().getBombardmentBonus());
    }

    @Test
    void royalRegularOfItsOwnCrownBombardsARebelColony() {
        final Model model = colonyWorld(REBEL, true, soldier(ATTACKER,
                OWN_CROWN, ORIGIN, false, MilitaryRole.ROYAL));
        final CombatStrength attack = quote(model, QUEBEC).getAttacker();
        assertEquals(50, attack.getBombardmentBonus());
        assertEquals(0, attack.getRebelAmbushBonus());
        // Quebec has no rebel share, so the Crown takes the whole Tory share.
        assertEquals(100.0, attack.getCauseSupportBonus(), EPSILON);
        // Base 5 x (1 + 0.5 attack + 0.5 bombardment + 1.0 Tory support) = 15.
        assertEquals(15.0, attack.getTotal(), EPSILON);
    }

    @Test
    void royalRegularOfAnotherCrownOrARivalGetsNoBombardment() {
        final Model otherCrown = colonyWorld(REBEL, true, soldier(ATTACKER,
                OTHER_CROWN, ORIGIN, false, MilitaryRole.ROYAL));
        assertEquals(0,
                quote(otherCrown, QUEBEC).getAttacker().getBombardmentBonus());
        final Model rival = colonyWorld(REBEL, true,
                soldier(ATTACKER, RIVAL, ORIGIN, false, MilitaryRole.COLONIAL));
        assertEquals(0,
                quote(rival, QUEBEC).getAttacker().getBombardmentBonus());
    }

    @Test
    void royalBombardmentNeedsTheRebelDeclaration() {
        final Model undeclared = colonyWorld(REBEL, false, soldier(ATTACKER,
                OWN_CROWN, ORIGIN, false, MilitaryRole.ROYAL));
        assertEquals(0,
                quote(undeclared, QUEBEC).getAttacker().getBombardmentBonus());
    }

    @Test
    void interventionBombardsAColonyHeldByItsOwnCrown() {
        final Model crown = colonyWorld(OWN_CROWN, true, soldier(ATTACKER,
                REBEL, ORIGIN, false, MilitaryRole.CONTINENTAL));
        colonist(crown, ATTACKER).markInterventionDelivered();
        final CombatStrength attack = quote(crown, QUEBEC).getAttacker();
        assertEquals(50, attack.getBombardmentBonus());
        // Base 4 x (1 + 0.5 attack + 0.5 bombardment) = 8, exactly.
        assertEquals(8.0, attack.getTotal(), EPSILON);
    }

    @Test
    void interventionIgnoresAColonyHeldByAnotherNationsCrown() {
        // Dutch rebels with delivered troops attack Quebec, which is held by
        // France's King, not by the Crown ruling the Dutch.
        final Model other = colonyWorld(OWN_CROWN, true, soldier(ATTACKER,
                RIVAL, ORIGIN, false, MilitaryRole.CONTINENTAL));
        colonist(other, ATTACKER).markInterventionDelivered();
        assertEquals(0,
                quote(other, QUEBEC).getAttacker().getBombardmentBonus());
    }

    @Test
    void anOrdinaryContinentalGetsNoBombardmentWithoutIntervention() {
        final Model model = colonyWorld(OWN_CROWN, true, soldier(ATTACKER,
                REBEL, ORIGIN, false, MilitaryRole.CONTINENTAL));
        assertFalse(colonist(model, ATTACKER).isInterventionDelivered());
        assertEquals(0,
                quote(model, QUEBEC).getAttacker().getBombardmentBonus());
    }

    @Test
    void theOriginMarkIsOneWayAndOnlyForARebelContinental() {
        final Model model = field(TerrainType.GRASSLAND, false, true,
                soldier(ATTACKER, REBEL, ORIGIN, false, MilitaryRole.COLONIAL),
                soldier(DEFENDER, OWN_CROWN, TARGET, false,
                        MilitaryRole.ROYAL));
        final String before = new Gson().toJson(model.save());
        assertThrows(IllegalStateException.class,
                () -> colonist(model, ATTACKER).markInterventionDelivered());
        assertThrows(IllegalStateException.class,
                () -> colonist(model, DEFENDER).markInterventionDelivered());
        assertEquals(before, new Gson().toJson(model.save()));
        colonist(model, ATTACKER).assignMilitaryRole(MilitaryRole.CONTINENTAL);
        colonist(model, ATTACKER).markInterventionDelivered();
        assertTrue(colonist(model, ATTACKER).isInterventionDelivered());
        assertThrows(IllegalStateException.class,
                () -> colonist(model, ATTACKER).markInterventionDelivered());
    }

    @Test
    void aStaleQuoteAfterMarkingIsRejectedWithoutMutation() {
        final Model model = colonyWorld(OWN_CROWN, true, soldier(ATTACKER,
                REBEL, ORIGIN, false, MilitaryRole.CONTINENTAL));
        final AttackOrder stale = model.getUnitById(ATTACKER)
                .prepareAttack(QUEBEC, true).orElseThrow();
        assertEquals(0, stale.getCombatStrengths().orElseThrow().getAttacker()
                .getBombardmentBonus());
        colonist(model, ATTACKER).markInterventionDelivered();
        final String before = new Gson().toJson(model.save());
        assertThrows(IllegalStateException.class, stale::execute);
        assertEquals(before, new Gson().toJson(model.save()));
        assertNotEquals(stale.getCombatStrengths(),
                model.getUnitById(ATTACKER).prepareAttack(QUEBEC, true)
                        .orElseThrow().getCombatStrengths());
    }

    @Test
    void theOriginSurvivesSaveAndContinueAndOrdinaryUnitsStayUnmarked() {
        final Model model = field(TerrainType.GRASSLAND, false, true,
                soldier(ATTACKER, REBEL, ORIGIN, false,
                        MilitaryRole.CONTINENTAL),
                soldier(DEFENDER, OWN_CROWN, TARGET, false,
                        MilitaryRole.ROYAL));
        colonist(model, ATTACKER).markInterventionDelivered();
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertTrue(colonist(restored, ATTACKER).isInterventionDelivered());
        assertFalse(colonist(restored, DEFENDER).isInterventionDelivered());
        assertEquals(new Gson().toJson(model.save()),
                new Gson().toJson(restored.save()));
    }

    @Test
    void aRebelAttackGetsItsExactRebelShareOfAnOccupiedColony() {
        // Stored liberty 120 is 60% of the one resident's 200 points; the Crown
        // occupies Quebec, but its colonial affiliation is still France.
        final Model model = colonyWorld(OWN_CROWN, true,
                soldier(ATTACKER, REBEL, ORIGIN, false, MilitaryRole.COLONIAL),
                120L);
        final CombatStrength attack = quote(model, QUEBEC).getAttacker();
        assertEquals(60.0, attack.getCauseSupportBonus(), EPSILON);
        assertEquals(0, attack.getBombardmentBonus());
        // Base 2 x (1 + 0.5 attack + 0.6 rebel support) = 4.2, exactly.
        assertEquals(4.2, attack.getTotal(), EPSILON);
    }

    @Test
    void aCrownAttackGetsTheComplementaryToryShareBesideItsBombardment() {
        final Model model = colonyWorld(REBEL, true,
                soldier(ATTACKER, OWN_CROWN, ORIGIN, false, MilitaryRole.ROYAL),
                120L);
        final CombatStrength attack = quote(model, QUEBEC).getAttacker();
        assertEquals(40.0, attack.getCauseSupportBonus(), EPSILON);
        assertEquals(50, attack.getBombardmentBonus());
        // Base 5 x (1 + 0.5 attack + 0.5 bombardment + 0.4 Tory support) = 12.
        assertEquals(12.0, attack.getTotal(), EPSILON);
    }

    @Test
    void zeroAndFullSharesAreTheExactBoundaries() {
        final Model noRebels = colonyWorld(OWN_CROWN, true,
                soldier(ATTACKER, REBEL, ORIGIN, false, MilitaryRole.COLONIAL),
                0L);
        assertEquals(0.0,
                quote(noRebels, QUEBEC).getAttacker().getCauseSupportBonus(),
                EPSILON);
        final Model allRebels = colonyWorld(OWN_CROWN, true,
                soldier(ATTACKER, REBEL, ORIGIN, false, MilitaryRole.COLONIAL),
                200L);
        assertEquals(100.0,
                quote(allRebels, QUEBEC).getAttacker().getCauseSupportBonus(),
                EPSILON);
        final Model crownOnly = colonyWorld(REBEL, true,
                soldier(ATTACKER, OWN_CROWN, ORIGIN, false, MilitaryRole.ROYAL),
                0L);
        assertEquals(100.0,
                quote(crownOnly, QUEBEC).getAttacker().getCauseSupportBonus(),
                EPSILON);
        final Model crownNone = colonyWorld(REBEL, true,
                soldier(ATTACKER, OWN_CROWN, ORIGIN, false, MilitaryRole.ROYAL),
                200L);
        assertEquals(0.0,
                quote(crownNone, QUEBEC).getAttacker().getCauseSupportBonus(),
                EPSILON);
    }

    @Test
    void aFractionalShareIsKeptExactlyNotAsItsRoundedPercent() {
        // One point of liberty is 0.5% of the resident's 200 points.
        final Model model = colonyWorld(OWN_CROWN, true,
                soldier(ATTACKER, REBEL, ORIGIN, false, MilitaryRole.COLONIAL),
                1L);
        assertEquals(0, model.getColonyById(0).orElseThrow().getRebelSupport()
                .getDisplayPercent());
        final CombatStrength attack = quote(model, QUEBEC).getAttacker();
        assertEquals(0.5, attack.getCauseSupportBonus(), EPSILON);
        // Base 2 x (1 + 0.5 attack + 0.005 rebel support) = 3.01, exactly.
        assertEquals(3.01, attack.getTotal(), EPSILON);
    }

    @Test
    void theCauseSupportAddsOnceBesideTheBombardmentAndTheAttackBonus() {
        final Model model = colonyWorld(OWN_CROWN, true, soldier(ATTACKER,
                REBEL, ORIGIN, false, MilitaryRole.CONTINENTAL), 120L);
        colonist(model, ATTACKER).markInterventionDelivered();
        final CombatStrength attack = quote(model, QUEBEC).getAttacker();
        assertEquals(50, attack.getBombardmentBonus());
        assertEquals(60.0, attack.getCauseSupportBonus(), EPSILON);
        assertEquals(0, attack.getTerrainBonus());
        assertEquals(0, attack.getRebelAmbushBonus());
        // Base 4 x (1 + 0.5 attack + 0.5 bombardment + 0.6 support) = 10.4.
        assertEquals(10.4, attack.getTotal(), EPSILON);
    }

    @Test
    void anotherCrownOrARivalGetsNoCauseSupportForTheColony() {
        final Model otherCrown = colonyWorld(REBEL, true, soldier(ATTACKER,
                OTHER_CROWN, ORIGIN, false, MilitaryRole.ROYAL), 120L);
        assertEquals(0.0,
                quote(otherCrown, QUEBEC).getAttacker().getCauseSupportBonus(),
                EPSILON);
        final Model rival = colonyWorld(REBEL, true,
                soldier(ATTACKER, RIVAL, ORIGIN, false, MilitaryRole.COLONIAL),
                120L);
        assertEquals(0.0,
                quote(rival, QUEBEC).getAttacker().getCauseSupportBonus(),
                EPSILON);
    }

    @Test
    void noCauseSupportBeforeTheDeclarationOrInTheCountryside() {
        final Model crownBeforeDeclaration = colonyWorld(REBEL, false,
                soldier(ATTACKER, OWN_CROWN, ORIGIN, false, MilitaryRole.ROYAL),
                120L);
        assertEquals(0.0, quote(crownBeforeDeclaration, QUEBEC).getAttacker()
                .getCauseSupportBonus(), EPSILON);
        final Model countryside = field(TerrainType.GRASSLAND, false, true,
                soldier(ATTACKER, REBEL, ORIGIN, false, MilitaryRole.COLONIAL),
                soldier(DEFENDER, OWN_CROWN, TARGET, false,
                        MilitaryRole.ROYAL));
        assertEquals(0.0,
                quote(countryside).getAttacker().getCauseSupportBonus(),
                EPSILON);
    }

    @Test
    void aStaleSupportIsRejectedWithoutMutationEvenWhenItsPercentIsUnchanged() {
        final Model model = colonyWorld(OWN_CROWN, true,
                soldier(ATTACKER, REBEL, ORIGIN, false, MilitaryRole.COLONIAL),
                120L);
        final AttackOrder stale = model.getUnitById(ATTACKER)
                .prepareAttack(QUEBEC, true).orElseThrow();
        assertEquals(60.0, stale.getCombatStrengths().orElseThrow()
                .getAttacker().getCauseSupportBonus(), EPSILON);
        // 60.5% still displays as 60%, yet the confirmed quote is no longer
        // true.
        model.getColonyById(0).orElseThrow().addProtestLiberty(1);
        assertEquals(60, model.getColonyById(0).orElseThrow().getRebelSupport()
                .getDisplayPercent());
        final String before = new Gson().toJson(model.save());
        assertThrows(IllegalStateException.class, stale::execute);
        assertEquals(before, new Gson().toJson(model.save()));
        final CombatStrengths fresh = quote(model, QUEBEC);
        assertEquals(60.5, fresh.getAttacker().getCauseSupportBonus(), EPSILON);
        assertNotEquals(stale.getCombatStrengths(), Optional.of(fresh));
    }

    @Test
    void theCauseSupportSurvivesSaveAndContinueFromThePersistedLiberty() {
        final Model model = colonyWorld(OWN_CROWN, true,
                soldier(ATTACKER, REBEL, ORIGIN, false, MilitaryRole.COLONIAL),
                120L);
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertEquals(60.0,
                quote(restored, QUEBEC).getAttacker().getCauseSupportBonus(),
                EPSILON);
        assertEquals(quote(model, QUEBEC), quote(restored, QUEBEC));
    }

    /**
     * Quebec held by the Dutch with the Dutch as its colonial affiliation, both
     * nations declared, and the given attacker added. The rebel France is then
     * an unrelated attacker of this colony.
     */
    private Model rivalColonyWorld(final UnitPo attacker, final long liberty) {
        final GameModelPo data = load();
        declare(data.getModel(), true);
        declare(data.getModel(), RIVAL, true);
        flatten(data.getModel(), ORIGIN);
        final ColonyPo quebec = data.getModel().getColonies().stream()
                .filter(colony -> colony.getId() == 0).findFirst()
                .orElseThrow();
        quebec.setOwnerName(RIVAL);
        quebec.setLastColonialAffiliation(RIVAL);
        quebec.setLiberty(liberty);
        data.getModel().getUnits().stream().filter(
                unit -> unit.getId() == RESIDENT || unit.getId() == GARRISON)
                .forEach(unit -> unit.setOwnerId(RIVAL));
        data.getModel().getUnits().add(attacker);
        return start(data, attacker);
    }

    @Test
    void aColonyOfARivalNationGivesTheRebelNoCauseSupport() {
        // A non-Crown conqueror becomes the colony's affiliation, so a colony
        // of the Dutch is held and affiliated by the Dutch: the France rebel
        // neither is its affiliation nor attacks its Crown's occupation.
        final Model model = rivalColonyWorld(
                soldier(ATTACKER, REBEL, ORIGIN, false, MilitaryRole.COLONIAL),
                120L);
        final CombatStrength attack = quote(model, QUEBEC).getAttacker();
        assertEquals(0.0, attack.getCauseSupportBonus(), EPSILON);
        // Base 2 x (1 + 0.5 attack) = 3, with no support term at all.
        assertEquals(3.0, attack.getTotal(), EPSILON);
        final Colony quebec = model.getColonyById(0).orElseThrow();
        assertEquals(RIVAL, quebec.getOwner().getName());
        assertEquals(RIVAL, quebec.getLastColonialAffiliation().getName());
    }

    @Test
    void theRebelShareNeedsItsOwnCrownAsTheHolderOfTheColony() {
        // France is the affiliation in both worlds. The loader accepts any
        // Crown
        // as holder, because a Crown leaves the affiliation untouched.
        final Model ownCrown = colonyWorld(OWN_CROWN, true,
                soldier(ATTACKER, REBEL, ORIGIN, false, MilitaryRole.COLONIAL),
                120L);
        final Model otherCrown = colonyWorld(OTHER_CROWN, true,
                soldier(ATTACKER, REBEL, ORIGIN, false, MilitaryRole.COLONIAL),
                120L);
        assertEquals(REBEL, ownCrown.getColonyById(0).orElseThrow()
                .getLastColonialAffiliation().getName());
        assertEquals(REBEL, otherCrown.getColonyById(0).orElseThrow()
                .getLastColonialAffiliation().getName());
        assertEquals(60.0,
                quote(ownCrown, QUEBEC).getAttacker().getCauseSupportBonus(),
                EPSILON);
        assertEquals(0.0,
                quote(otherCrown, QUEBEC).getAttacker().getCauseSupportBonus(),
                EPSILON);
    }

    @Test
    void theToryShareNeedsTheAffiliationAsTheHolderOfTheColony() {
        // The Crown of France attacks Quebec, affiliated with France in both
        // worlds: once as a colony the affiliation holds, once as one that
        // another Crown holds, which is no revolutionary attack of this cause.
        final Model held = colonyWorld(REBEL, true,
                soldier(ATTACKER, OWN_CROWN, ORIGIN, false, MilitaryRole.ROYAL),
                120L);
        final Model otherCrown = colonyWorld(OTHER_CROWN, true,
                soldier(ATTACKER, OWN_CROWN, ORIGIN, false, MilitaryRole.ROYAL),
                120L);
        assertEquals(REBEL, otherCrown.getColonyById(0).orElseThrow()
                .getLastColonialAffiliation().getName());
        assertEquals(40.0,
                quote(held, QUEBEC).getAttacker().getCauseSupportBonus(),
                EPSILON);
        assertEquals(0.0,
                quote(otherCrown, QUEBEC).getAttacker().getCauseSupportBonus(),
                EPSILON);
    }

    @Test
    void anotherCrownGetsNoCauseSupportAgainstTheCrownHeldColony() {
        final Model model = colonyWorld(OWN_CROWN, true, soldier(ATTACKER,
                OTHER_CROWN, ORIGIN, false, MilitaryRole.ROYAL), 120L);
        assertEquals(0.0,
                quote(model, QUEBEC).getAttacker().getCauseSupportBonus(),
                EPSILON);
    }

    @Test
    void aRebelGetsNoCauseSupportBeforeTheDeclarationEvenAgainstItsCrown() {
        final Model model = colonyWorld(OWN_CROWN, false,
                soldier(ATTACKER, REBEL, ORIGIN, false, MilitaryRole.COLONIAL),
                120L);
        assertEquals(0.0,
                quote(model, QUEBEC).getAttacker().getCauseSupportBonus(),
                EPSILON);
    }

    /**
     * Crown-held Quebec that a won battle can capture: its garrison soldier is
     * disarmed, so the garrison and the one resident defend as civilians. Two
     * France rebels stand beside it, the first at {@link #ORIGIN} and the
     * second, the capturer, at {@link #TARGET}. The saved combat state decides
     * the capturer's draw.
     */
    private Model captureWorld(final long state) {
        final GameModelPo data = load();
        data.getModel().setCombatRandomState(state);
        declare(data.getModel(), true);
        flatten(data.getModel(), ORIGIN);
        flatten(data.getModel(), TARGET);
        final ColonyPo quebec = data.getModel().getColonies().stream()
                .filter(colony -> colony.getId() == 0).findFirst()
                .orElseThrow();
        quebec.setOwnerName(OWN_CROWN);
        quebec.setLiberty(120L);
        data.getModel().getUnits().stream().filter(
                unit -> unit.getId() == RESIDENT || unit.getId() == GARRISON)
                .forEach(unit -> unit.setOwnerId(OWN_CROWN));
        data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == GARRISON)
                .forEach(unit -> unit.setHoldingGuns(false));
        final UnitPo attacker = soldier(ATTACKER, REBEL, ORIGIN, false,
                MilitaryRole.COLONIAL);
        data.getModel().getUnits().add(attacker);
        data.getModel().getUnits().add(
                soldier(CAPTURER, REBEL, TARGET, false, MilitaryRole.COLONIAL));
        return start(data, attacker);
    }

    /**
     * Saved combat state under which the capturer wins its battle for Quebec.
     * The quote does not depend on the state, so a probe world gives the odds.
     */
    private long winningCaptureState() {
        final double chance = captureWorld(0L).getUnitById(CAPTURER)
                .prepareAttack(QUEBEC, true).orElseThrow().getCombatStrengths()
                .orElseThrow().getAttackerWinProbability();
        return CombatDraws.stateFor(chance, true);
    }

    @Test
    void aRealCaptureOfTheColonyInvalidatesTheRebelQuoteWithoutMutation() {
        final Model model = captureWorld(winningCaptureState());
        final AttackOrder stale = model.getUnitById(ATTACKER)
                .prepareAttack(QUEBEC, true).orElseThrow();
        assertEquals(60.0, stale.getCombatStrengths().orElseThrow()
                .getAttacker().getCauseSupportBonus(), EPSILON);

        // The second rebel wins the real battle and takes the whole colony.
        model.getUnitById(CAPTURER).prepareAttack(QUEBEC, true).orElseThrow()
                .execute();
        final Colony quebec = model.getColonyById(0).orElseThrow();
        final Player france = model.getPlayerByName(REBEL);
        assertSame(france, quebec.getOwner());
        assertSame(france, quebec.getLastColonialAffiliation());
        assertEquals(1, quebec.getPopulation());
        quebec.getUnitsInsideColony()
                .forEach(resident -> assertSame(france, resident.getOwner()));
        assertSame(france, model.getUnitById(GARRISON).getOwner());
        assertEquals(QUEBEC, model.getUnitById(CAPTURER).getLocation());

        // The captured world is coherent: Save and Continue reproduce it.
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertEquals(new Gson().toJson(model.save()),
                new Gson().toJson(restored.save()));

        // The first rebel's confirmation is stale: the colony changed hands.
        // It is rejected before any draw, movement or other change, and the
        // saved state, which holds the combat random state, is untouched.
        final String before = new Gson().toJson(model.save());
        assertThrows(IllegalStateException.class, stale::execute);
        assertEquals(before, new Gson().toJson(model.save()));
    }

    /** France to move in a running world, with Quebec's east field open. */
    private Model rebelQuebec() {
        final GameModelPo data = load();
        declare(data.getModel(), true);
        flatten(data.getModel(), ORIGIN);
        data.getModel().getGameManager().setGameStarted(true);
        data.getModel().getGameManager().setCurrentPlayer(REBEL);
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    /** Armed Continental on Quebec's tile, delivered by the intervention. */
    private UnitFreeColonist deliveredContinental(final Model model) {
        final Colony quebec = model.getColonyById(0).orElseThrow();
        final UnitFreeColonist unit = (UnitFreeColonist) model
                .addUnitOutSideColony(quebec);
        unit.setHoldingGuns(true);
        unit.assignMilitaryRole(MilitaryRole.CONTINENTAL);
        unit.markInterventionDelivered();
        assertTrue(unit.isInterventionDelivered());
        return unit;
    }

    @Test
    void takingAColonyJobMakesTheDeliveredContinentalACivilianAgain() {
        final Model model = rebelQuebec();
        final UnitFreeColonist unit = deliveredContinental(model);
        final ColonyField field = model.getColonyById(0).orElseThrow()
                .getColonyFieldInDirection(Direction.east);
        unit.placeToColonyField(field, GoodsType.CORN);
        assertTrue(unit.isAtPlaceColonyField());
        assertEquals(MilitaryRole.COLONIAL, unit.getMilitaryRole());
        assertFalse(unit.isInterventionDelivered());
        // The saved unit carries neither the role nor the origin.
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertEquals(MilitaryRole.COLONIAL,
                colonist(restored, unit.getId()).getMilitaryRole());
        assertFalse(colonist(restored, unit.getId()).isInterventionDelivered());
        assertEquals(new Gson().toJson(model.save()),
                new Gson().toJson(restored.save()));
    }

    @Test
    void aRejectedColonyJobKeepsTheRoleAndTheOriginWithoutAnyEffect() {
        final Model model = rebelQuebec();
        final UnitFreeColonist unit = deliveredContinental(model);
        final ColonyField field = model.getColonyById(0).orElseThrow()
                .getColonyFieldInDirection(Direction.east);
        final String before = new Gson().toJson(model.save());
        assertThrows(IllegalArgumentException.class,
                () -> unit.placeToColonyField(field, GoodsType.MUSKET));
        assertEquals(MilitaryRole.CONTINENTAL, unit.getMilitaryRole());
        assertTrue(unit.isInterventionDelivered());
        assertTrue(unit.isHoldingGuns());
        assertTrue(unit.isAtPlaceLocation());
        assertEquals(before, new Gson().toJson(model.save()));
    }

    /** A saved world holding only the unit, with France declared or not. */
    private ModelPo savedWorld(final boolean declared, final UnitPo unit) {
        final GameModelPo data = load();
        declare(data.getModel(), declared);
        data.getModel().getUnits().add(unit);
        return data.getModel();
    }

    @Test
    void aSavedOriginOnAnOrdinarySoldierIsRejectedWithoutAnyEffect() {
        final UnitPo unit = soldier(ATTACKER, REBEL, ORIGIN, false,
                MilitaryRole.COLONIAL);
        unit.setInterventionDelivered(true);
        final ModelPo data = savedWorld(true, unit);
        final String before = new Gson().toJson(data);
        assertThrows(IllegalArgumentException.class, () -> Model.make(data,
                MissionImpl.getPredefinedGameOverEvaluators()));
        assertEquals(before, new Gson().toJson(data));
    }

    @Test
    void aSavedOriginOnARoyalOrUndeclaredSoldierIsRejected() {
        final UnitPo royal = soldier(ATTACKER, OWN_CROWN, ORIGIN, false,
                MilitaryRole.ROYAL);
        royal.setInterventionDelivered(true);
        assertThrows(IllegalArgumentException.class,
                () -> Model.make(savedWorld(true, royal),
                        MissionImpl.getPredefinedGameOverEvaluators()));
        final UnitPo continental = soldier(ATTACKER, REBEL, ORIGIN, false,
                MilitaryRole.CONTINENTAL);
        continental.setInterventionDelivered(true);
        assertThrows(IllegalArgumentException.class,
                () -> Model.make(savedWorld(false, continental),
                        MissionImpl.getPredefinedGameOverEvaluators()));
    }

    @Test
    void aSavedOriginOnANonColonistIsRejectedWithoutAnyEffect() {
        final UnitPo wagon = soldier(ATTACKER, REBEL, ORIGIN, false,
                MilitaryRole.COLONIAL);
        wagon.setType(UnitType.WAGON);
        wagon.setMissionary(null);
        wagon.setInterventionDelivered(true);
        final ModelPo data = savedWorld(true, wagon);
        final String before = new Gson().toJson(data);
        assertThrows(IllegalArgumentException.class, () -> Model.make(data,
                MissionImpl.getPredefinedGameOverEvaluators()));
        assertEquals(before, new Gson().toJson(data));
    }

    @Test
    void aSavedRebelContinentalOriginLoads() {
        final UnitPo continental = soldier(ATTACKER, REBEL, ORIGIN, false,
                MilitaryRole.CONTINENTAL);
        continental.setInterventionDelivered(true);
        final Model restored = Model.make(savedWorld(true, continental),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertTrue(colonist(restored, ATTACKER).isInterventionDelivered());
    }
}
