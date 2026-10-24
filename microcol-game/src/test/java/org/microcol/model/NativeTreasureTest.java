package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import java.util.function.LongPredicate;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.microcol.model.store.CongressPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.UnitActionType;
import org.microcol.model.unit.UnitTreasure;

/**
 * Treasure train of a native conquest (R-0175). The single reward draw follows
 * the battles, so the saved random state shows exactly how many draws happened:
 * a won battle of the soldier takes two draws (the battle and its promotion
 * roll), the treasure draw adds one more, and Cortes adds none. A lost attack
 * takes only its battle draw. The face value belongs to the train, never to the
 * owner's gold, and a saved train is validated on load and never rolled again.
 */
class NativeTreasureTest {

    private static final NativeNation NATION = NativeNation.ARAWAK;

    private static final String DUTCH = "Dutch";

    private static final int TARGET_ID = 1;

    private static final int CAPITAL_ID = 2;

    private static final int ATTACKER_ID = 9000;

    private static final int TREASURE_ID = 9100;

    private static final Location TARGET_AT = NativeTurnScene.at(0, 0);

    private static final Location ATTACKER_AT = NativeTurnScene.at(1, 0);

    private static final Location OPEN_AT = NativeTurnScene.at(2, 0);

    private static final long GOLDEN = 0x9E3779B97F4A7C15L;

    /** Draw of the battle, then the soldier's promotion roll. */
    private static final int BATTLE_AND_PROMOTION = 2;

    /** Draw of the treasure roll after one battle and one promotion roll. */
    private static final int TREASURE_DRAW = 3;

    // ----- rewards without Cortes -----

    @Test
    void ordinarySettlementLeavesA500TrainOnADrawBelowHalf() {
        final double chance = chance(false, false);
        final long seed = seedWhere(s -> roll(s, 1) < chance / 2
                && roll(s, TREASURE_DRAW) < NativeTreasureRules.CHANCE);
        final Model model = model(world(false, false, seed));
        final int gold = dutch(model).getGold();

        final NativeConquestResult result = attack(model);

        assertTrue(result.isConquered());
        assertEquals(Optional.of(500), result.getTreasureValue());
        final List<UnitTreasure> trains = treasures(model);
        assertEquals(1, trains.size());
        assertEquals(500, trains.get(0).getValue());
        assertEquals(DUTCH, trains.get(0).getOwner().getName());
        assertEquals(TARGET_AT, trains.get(0).getLocation());
        // The train is the value carrier: no gold is credited at creation.
        assertEquals(gold, dutch(model).getGold());
        assertEquals(seed + (BATTLE_AND_PROMOTION + 1) * GOLDEN, random(model));
    }

    @Test
    void ordinarySettlementLeavesNothingOnADrawAtOrAboveHalf() {
        final double chance = chance(false, false);
        final long seed = seedWhere(s -> roll(s, 1) < chance / 2
                && roll(s, TREASURE_DRAW) >= NativeTreasureRules.CHANCE);
        final Model model = model(world(false, false, seed));
        final int gold = dutch(model).getGold();

        final NativeConquestResult result = attack(model);

        assertTrue(result.isConquered());
        assertTrue(result.getTreasureValue().isEmpty());
        assertTrue(treasures(model).isEmpty());
        assertEquals(gold, dutch(model).getGold());
        assertEquals(seed + (BATTLE_AND_PROMOTION + 1) * GOLDEN, random(model));
    }

    @Test
    void capitalWithoutCortesLeaves1000OnADrawBelowHalf() {
        final double chance = chance(true, false);
        final long seed = seedWhere(s -> roll(s, 1) < chance / 2
                && roll(s, TREASURE_DRAW) < NativeTreasureRules.CHANCE);
        final Model model = model(world(true, false, seed));

        assertEquals(Optional.of(1000), attack(model).getTreasureValue());
        assertEquals(1000, treasures(model).get(0).getValue());
    }

    // ----- Cortes -----

    @Test
    void cortesGuaranteesADoubleOrdinaryTrainWithoutAnyRewardDraw() {
        final double chance = chance(false, true);
        final long seed = seedWhere(s -> roll(s, 1) < chance / 2);
        final Model model = model(world(false, true, seed));

        assertEquals(Optional.of(1000), attack(model).getTreasureValue());
        final List<UnitTreasure> trains = treasures(model);
        assertEquals(1, trains.size());
        assertEquals(1000, trains.get(0).getValue());
        // Only the battle and its promotion roll were drawn.
        assertEquals(seed + BATTLE_AND_PROMOTION * GOLDEN, random(model));
    }

    @Test
    void cortesGuaranteesADoubleCapitalTrainEvenWhenTheDrawWouldFail() {
        final double chance = chance(true, true);
        final long seed = seedWhere(s -> roll(s, 1) < chance / 2
                && roll(s, TREASURE_DRAW) >= NativeTreasureRules.CHANCE);
        final Model model = model(world(true, true, seed));

        assertEquals(Optional.of(2000), attack(model).getTreasureValue());
        assertEquals(2000, treasures(model).get(0).getValue());
        assertEquals(seed + BATTLE_AND_PROMOTION * GOLDEN, random(model));
    }

    @Test
    void cortesJoiningThroughOwnerUpdateDoublesTheSameSeedsOrdinaryTrain() {
        final double chance = chance(false, false);
        final long seed = seedWhere(s -> roll(s, 1) < chance / 2
                && roll(s, TREASURE_DRAW) < NativeTreasureRules.CHANCE);
        assertEquals(Optional.of(500),
                attack(model(world(false, false, seed))).getTreasureValue());

        final ModelPo po = world(false, false, seed);
        RealFounderJoin.readyFor(RealFounderJoin.congressOf(po, DUTCH),
                Founder.HERNAN_CORTES);
        final Model model = model(po);
        RealFounderJoin.joinThroughOwnerUpdate(model, Founder.HERNAN_CORTES);
        assertEquals(Optional.of(1000), attack(model).getTreasureValue());
        assertEquals(1000, treasures(model).get(0).getValue());
    }

    // ----- failed attacks -----

    @Test
    void lostAttackLeavesNoTrainNoGoldAndNoRewardDraw() {
        assertLostAttackRewardsNothing(false);
        assertLostAttackRewardsNothing(true);
    }

    private static void assertLostAttackRewardsNothing(final boolean cortes) {
        final double chance = chance(false, cortes);
        final long seed = seedWhere(s -> roll(s, 1) > chance);
        final Model model = model(world(false, cortes, seed));
        final int gold = dutch(model).getGold();

        final NativeConquestResult result = attack(model);

        assertFalse(result.isConquered());
        assertTrue(result.getTreasureValue().isEmpty());
        assertTrue(treasures(model).isEmpty());
        assertEquals(gold, dutch(model).getGold());
        // Only the losing battle drew: no promotion and no reward roll.
        assertEquals(seed + GOLDEN, random(model));
    }

    // ----- persistence -----

    @Test
    void savedTrainIsRestoredOnceAndNeverRolledAgain() {
        final double chance = chance(false, false);
        final long seed = seedWhere(s -> roll(s, 1) < chance / 2
                && roll(s, TREASURE_DRAW) < NativeTreasureRules.CHANCE);
        final Model model = model(world(false, false, seed));
        assertTrue(attack(model).getTreasureValue().isPresent());
        final int trainId = treasures(model).get(0).getId();
        final long afterConquest = random(model);

        final Model reloaded = NativeTurnScene.make(model.save());

        final List<UnitTreasure> trains = treasures(reloaded);
        assertEquals(1, trains.size());
        assertEquals(trainId, trains.get(0).getId());
        assertEquals(500, trains.get(0).getValue());
        assertEquals(afterConquest, random(reloaded));
        // A second save and load repeats neither the train nor its draw.
        final Model again = NativeTurnScene.make(reloaded.save());
        assertEquals(1, treasures(again).size());
        assertEquals(afterConquest, random(again));
    }

    @Test
    void invalidSavedTrainIdentityIsRefusedOnLoad() {
        assertInvalidTrain(UnitType.TREASURE_TRAIN, null);
        assertInvalidTrain(UnitType.TREASURE_TRAIN, 0);
        assertInvalidTrain(UnitType.TREASURE_TRAIN, -5);
        // Only a treasure train carries a face value.
        assertInvalidTrain(UnitType.COLONIST, 500);
    }

    private static void assertInvalidTrain(final UnitType type,
            final Integer value) {
        final ModelPo po = world(false, false, 1L);
        addUnit(po, type, value);
        assertThrows(IllegalArgumentException.class,
                () -> NativeTurnScene.make(po), type + " " + value);
    }

    // ----- what the train can and cannot do -----

    @Test
    void trainHasNoStrengthNoOrdersAndNoHoldOfItsOwnButReservesSixInAShip() {
        final ModelPo po = world(false, false, 1L);
        addUnit(po, UnitType.TREASURE_TRAIN, 500);
        final Model model = model(po);
        final UnitTreasure train = (UnitTreasure) model
                .getUnitById(TREASURE_ID);

        assertFalse(train.canAttack());
        assertEquals(0.0, train.getMilitaryStrenght(), 1e-9);
        assertTrue(train.prepareAttack(ATTACKER_AT, true).isEmpty());
        // Storable only as a six-hold reservation in a ship (R-0134).
        assertTrue(train.isStorable());
        assertEquals(6, train.getType().getRequiredHolds());
        assertFalse(train.canHoldCargo());
        assertFalse(train.getType().canBuildColony());
        assertEquals(0, train.getType().getEuropePrice());
        assertThrows(IllegalStateException.class,
                () -> train.setActionType(UnitActionType.sentry));
        assertThrows(IllegalStateException.class,
                () -> train.setActionType(UnitActionType.plowField));
    }

    @Test
    void onlyAnArmedUnitTakesAForeignTrainAndTheDefeatPaysNothing() {
        // An unarmed colonist has no legal attack on the train.
        final ModelPo unarmed = world(false, false, 1L);
        addUnit(unarmed, "France", UnitType.TREASURE_TRAIN, 500);
        for (final UnitPo unit : unarmed.getUnits()) {
            if (unit.getId() == ATTACKER_ID) {
                unit.setHoldingGuns(false);
            }
        }
        assertTrue(model(unarmed).getUnitById(ATTACKER_ID)
                .prepareAttack(OPEN_AT, true).isEmpty());

        // The armed soldier defeats it with the saved strengths: the train is
        // destroyed, not captured, and no gold changes hands.
        final ModelPo armed = world(false, false, 1L);
        addUnit(armed, "France", UnitType.TREASURE_TRAIN, 500);
        final Model model = model(armed);
        final int dutchGold = dutch(model).getGold();
        final int frenchGold = model.getPlayerByName("France").getGold();

        model.getUnitById(ATTACKER_ID).prepareAttack(OPEN_AT, true)
                .orElseThrow().execute();

        assertTrue(model.tryGetUnitById(TREASURE_ID).isEmpty());
        assertTrue(treasures(model).isEmpty());
        assertEquals(dutchGold, dutch(model).getGold());
        assertEquals(frenchGold, model.getPlayerByName("France").getGold());
        assertEquals(OPEN_AT, model.getUnitById(ATTACKER_ID).getLocation());
    }

    @Test
    void trainIsAMovableLandUnit() {
        assertTrue(UnitType.TREASURE_TRAIN
                .canMoveAtTerrain(TerrainType.GRASSLAND));
        assertEquals(1, UnitType.TREASURE_TRAIN.getSpeed());
    }

    @Test
    void armedAttackerAlwaysTakesTheTrainWithZeroDefense() {
        final ModelPo po = world(false, false, 1L);
        addUnit(po, UnitType.TREASURE_TRAIN, 500);
        final Model model = model(po);
        final UnitTreasure train = (UnitTreasure) model
                .getUnitById(TREASURE_ID);

        final CombatStrengths battle = CombatStrengths
                .forBattle(model, attacker(model), train).orElseThrow();

        assertEquals(0.0, battle.getDefender().getTotal(), 1e-9);
        assertEquals(1.0, battle.getAttackerWinProbability(), 1e-9);
        assertTrue(battle.attackerWins(0.999));
    }

    // ----- helpers -----

    /**
     * The first battle chance of the attacker against the single native brave,
     * read from a world that is identical except for the saved draw.
     */
    private static double chance(final boolean capital, final boolean cortes) {
        return quote(model(world(capital, cortes, 0L))).getFirstBattle()
                .getAttackerWinProbability();
    }

    /**
     * A Dutch soldier beside a native settlement with one brave, the settlement
     * on the island at the centre and, for an ordinary settlement, a capital
     * elsewhere. The soldier is armed with a movement point, the saved draws
     * start at the given seed, and Cortes is optionally a member of Dutch
     * Congress.
     */
    private static ModelPo world(final boolean capital, final boolean cortes,
            final long seed) {
        NativeTurnScene scene = NativeTurnScene.island().nativePlayer(NATION)
                .settlement(TARGET_ID, NATION, capital, 0, 0)
                .brave(NATION, TARGET_ID, 0, 0).rawUnit(ATTACKER_ID,
                        UnitType.COLONIST, DUTCH, null, null, ATTACKER_AT);
        if (!capital) {
            scene = scene.settlement(CAPITAL_ID, NATION, true, -3, -3);
        }
        final ModelPo po = scene.po();
        po.getGameManager().setGameStarted(true);
        po.getGameManager().setCurrentPlayer(DUTCH);
        for (final UnitPo unit : po.getUnits()) {
            if (unit.getId() == ATTACKER_ID) {
                unit.setHoldingGuns(true);
                unit.setMovementThirds(MovementPoints.ofPoints(1));
            }
        }
        if (cortes) {
            for (final PlayerPo player : po.getGameManager().getPlayers()) {
                if (DUTCH.equals(player.getName())) {
                    player.setCongress(cortesCongress());
                }
            }
        }
        po.setCombatRandomState(seed);
        return po;
    }

    /**
     * An active Congress with Hernan Cortes as its only member and one offer of
     * each remaining category, as a saved state that the model accepts.
     */
    private static CongressPo cortesCongress() {
        final CongressPo congress = new CongressPo();
        congress.setActivated(true);
        congress.setBells(0);
        congress.setMembers(List.of(Founder.HERNAN_CORTES));
        congress.setCandidates(List.of(Founder.FRANCIS_DRAKE,
                Founder.SIMON_BOLIVAR, Founder.ADAM_SMITH,
                Founder.JEAN_DE_BREBEUF, Founder.HENRY_HUDSON));
        congress.setSelected(null);
        congress.setGeneration(1);
        congress.setJonesDeliveryPending(false);
        return congress;
    }

    /** Add a saved unit of the given type, as a treasure with the value. */
    private static void addUnit(final ModelPo po, final UnitType type,
            final Integer value) {
        addUnit(po, DUTCH, type, value);
    }

    /** Add a saved unit of the given owner, type and value, on open ground. */
    private static void addUnit(final ModelPo po, final String owner,
            final UnitType type, final Integer value) {
        final ModelPo source = NativeTurnScene.island()
                .rawUnit(TREASURE_ID, type, owner, null, null, OPEN_AT).po();
        final UnitPo unit = source.getUnits().stream()
                .filter(candidate -> candidate.getId() == TREASURE_ID)
                .findFirst().orElseThrow();
        unit.setTreasureValue(value);
        po.getUnits().add(unit);
    }

    private static Model model(final ModelPo po) {
        return NativeTurnScene.make(po);
    }

    private static Unit attacker(final Model model) {
        return model.getUnitById(ATTACKER_ID);
    }

    private static NativeSettlement site(final Model model) {
        return model.getNativeSettlements().stream().filter(
                settlement -> settlement.getLocation().equals(TARGET_AT))
                .findAny().orElseThrow();
    }

    private static NativeConquestQuote quote(final Model model) {
        return model.getNativeConquestQuote(attacker(model), site(model))
                .orElseThrow();
    }

    private static NativeConquestResult attack(final Model model) {
        return model.attackNativeSettlement(attacker(model), site(model),
                quote(model));
    }

    private static Player dutch(final Model model) {
        return model.getPlayerByName(DUTCH);
    }

    private static List<UnitTreasure> treasures(final Model model) {
        return model.getAllUnits().stream()
                .filter(UnitTreasure.class::isInstance)
                .map(UnitTreasure.class::cast).collect(Collectors.toList());
    }

    /** Saved combat random state, which only a draw can change. */
    private static long random(final Model model) {
        return model.save().getCombatRandomState();
    }

    /** The draw number of a saved random state, counted from one, as drawn. */
    private static double roll(final long seed, final int draw) {
        return Model.combatRandomAfter(seed + (draw - 1) * GOLDEN);
    }

    private static long seedWhere(final LongPredicate wanted) {
        for (long seed = 0; seed < 2_000_000L; seed++) {
            if (wanted.test(seed)) {
                return seed;
            }
        }
        throw new IllegalStateException("No seed with the wanted draws");
    }

}
