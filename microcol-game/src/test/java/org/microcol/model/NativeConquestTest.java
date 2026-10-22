package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.LongPredicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.microcol.model.store.EuropeanRelationPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.NativeLandRecordPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.turnevent.TurnEventNativeConquest;
import org.microcol.model.turnevent.TurnEventNativeConquestLoss;
import org.microcol.model.unit.UnitFreeColonist;
import org.microcol.model.unit.UnitNativeBrave;

/**
 * Attack and conquest of a native settlement (R-0174): the actual defenders
 * decide the outcome battle by battle, a settlement is removed only when every
 * defender is overcome, refused or stale terms change nothing, the aftermath of
 * braves, claims, pupils and missionaries follows the agreed rules, and the
 * conquest is recorded exactly once. Battle draws are chosen through the saved
 * combat random state: the first draw is the same splitmix draw that
 * {@link NativeChiefTest} uses, and a won battle of a promotable soldier uses
 * one more draw for its ordinary promotion roll before the next battle.
 */
class NativeConquestTest {

    private static final NativeNation NATION = NativeNation.ARAWAK;

    private static final int TARGET_ID = 1;

    private static final int CAPITAL_ID = 2;

    private static final int ATTACKER_ID = 9000;

    private static final int PUPIL_ID = 9001;

    private static final int MISSIONARY_ID = 9002;

    private static final int FRENCH_BLOCKER_ID = 9003;

    private static final Location TARGET_AT = NativeTurnScene.at(0, 0);

    private static final Consumer<UnitPo> SOLDIER = unit -> unit
            .setHoldingGuns(true);

    private static final Consumer<UnitPo> DRAGOON = unit -> {
        unit.setHoldingGuns(true);
        unit.setMounted(true);
    };

    private static final Consumer<UnitPo> SCOUT = unit -> unit.setMounted(true);

    private static final Consumer<UnitPo> UNARMED = unit -> {
    };

    private static final Consumer<UnitPo> SEASONED = unit -> {
        unit.setWorkUpdates(0);
        unit.setMissionary(false);
    };

    private static final Consumer<UnitPo> INTACT_GUN = unit -> unit
            .setArtilleryDamaged(false);

    private static final long GOLDEN = 0x9E3779B97F4A7C15L;

    private static double roll(final long seed, final int draw) {
        long state = seed;
        long z = 0;
        for (int index = 0; index < draw; index++) {
            state += GOLDEN;
            z = state;
            z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
            z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
            z ^= z >>> 31;
        }
        return (z >>> 11) * 0x1.0p-53;
    }

    private static long seedWhere(final LongPredicate wanted) {
        for (long seed = 0; seed < 2_000_000L; seed++) {
            if (wanted.test(seed)) {
                return seed;
            }
        }
        throw new IllegalStateException("No seed with the wanted draws");
    }

    /**
     * The island world of one attack: a target settlement at the centre, its
     * braves on its square, and a Dutch attacker beside it. Everything else is
     * added through {@link #extras} and {@link #tweak}.
     */
    private static final class World {
        private NativeNation nation = NATION;
        private boolean capital;
        private int braves = 1;
        private UnitType attackerType = UnitType.COLONIST;
        private Consumer<UnitPo> equip = SOLDIER;
        private Location attackerAt = NativeTurnScene.at(1, 0);
        private int companionDx = -3;
        private int companionDy = -3;
        private String current = "Dutch";
        private long seed;
        private UnaryOperator<NativeTurnScene> extras = scene -> scene;
        private Consumer<ModelPo> tweak = po -> {
        };

        World nation(final NativeNation value) {
            nation = value;
            return this;
        }

        World capital(final boolean value) {
            capital = value;
            return this;
        }

        World braves(final int value) {
            braves = value;
            return this;
        }

        World type(final UnitType value) {
            attackerType = value;
            return this;
        }

        World equip(final Consumer<UnitPo> value) {
            equip = value;
            return this;
        }

        World attackerAt(final int dx, final int dy) {
            attackerAt = NativeTurnScene.at(dx, dy);
            return this;
        }

        World companion(final int dx, final int dy) {
            companionDx = dx;
            companionDy = dy;
            return this;
        }

        World current(final String value) {
            current = value;
            return this;
        }

        World seed(final long value) {
            seed = value;
            return this;
        }

        World extras(final UnaryOperator<NativeTurnScene> value) {
            extras = value;
            return this;
        }

        World tweak(final Consumer<ModelPo> value) {
            tweak = value;
            return this;
        }

        ModelPo po() {
            NativeTurnScene scene = NativeTurnScene.island()
                    .nativePlayer(nation)
                    .settlement(TARGET_ID, nation, capital, 0, 0);
            if (!capital) {
                // The nation needs exactly one capital, kept elsewhere.
                scene = scene.settlement(CAPITAL_ID, nation, true, companionDx,
                        companionDy);
            }
            for (int index = 0; index < braves; index++) {
                scene = scene.brave(nation, TARGET_ID, 0, 0);
            }
            scene = scene.rawUnit(ATTACKER_ID, attackerType, "Dutch", null,
                    null, attackerAt);
            scene = extras.apply(scene);
            final ModelPo po = scene.po();
            po.getGameManager().setGameStarted(true);
            po.getGameManager().setCurrentPlayer(current);
            for (final UnitPo unit : po.getUnits()) {
                if (unit.getId() == ATTACKER_ID) {
                    equip.accept(unit);
                    unit.setMovementThirds(MovementPoints.ofPoints(1));
                }
            }
            tweak.accept(po);
            po.setCombatRandomState(seed);
            return po;
        }

        Model model() {
            return NativeTurnScene.make(po());
        }
    }

    private static Unit attacker(final Model model) {
        return model.getUnitById(ATTACKER_ID);
    }

    private static NativeSettlement site(final Model model) {
        return settlementAt(model, TARGET_AT);
    }

    private static NativeSettlement settlementAt(final Model model,
            final Location at) {
        return model.getNativeSettlements().stream()
                .filter(settlement -> settlement.getLocation().equals(at))
                .findAny().orElse(null);
    }

    private static NativeConquestQuote quote(final Model model) {
        return model.getNativeConquestQuote(attacker(model), site(model))
                .orElseThrow();
    }

    /** Chance of the attacker's battle against the first defender. */
    private static double chance(final World world) {
        return quote(world.model()).getFirstBattle()
                .getAttackerWinProbability();
    }

    private static NativeConquestResult attack(final Model model) {
        return model.attackNativeSettlement(attacker(model), site(model),
                quote(model));
    }

    private static Player dutch(final Model model) {
        return model.getPlayerByName("Dutch");
    }

    private static long bravesOnTarget(final Model model) {
        return model.getUnitsAt(TARGET_AT).stream()
                .filter(unit -> unit instanceof UnitNativeBrave).count();
    }

    private static List<TurnEventNativeConquest> conquests(final Model model) {
        return model.getTurnEventStore().getForPlayer(dutch(model)).stream()
                .filter(TurnEventNativeConquest.class::isInstance)
                .map(TurnEventNativeConquest.class::cast)
                .collect(Collectors.toList());
    }

    private static List<TurnEventNativeConquestLoss> losses(final Model model,
            final String owner) {
        return model.getTurnEventStore()
                .getForPlayer(model.getPlayerByName(owner)).stream()
                .filter(TurnEventNativeConquestLoss.class::isInstance)
                .map(TurnEventNativeConquestLoss.class::cast)
                .collect(Collectors.toList());
    }

    /** Saved combat random state, which only a draw can change. */
    private static long random(final Model model) {
        return model.save().getCombatRandomState();
    }

    private static double anger(final Model model, final NativeNation nation) {
        return model.getNativeTribalAnger(dutch(model), nation);
    }

    private static Unit unitAt(final Model model, final Location at) {
        return model.getUnitsAt(at).stream()
                .filter(unit -> unit instanceof UnitNativeBrave).findFirst()
                .orElseThrow();
    }

    private static int homeOf(final Model model, final Location braveAt) {
        return ((UnitNativeBrave) unitAt(model, braveAt)).getHomeSettlementId()
                .orElseThrow();
    }

    /** Seed whose first battle is won and whose second battle is won. */
    private static long winWinSeed(final double chance) {
        // The second battle uses the third draw: the second is the promotion
        // roll of the first victory.
        return seedWhere(
                s -> roll(s, 1) < chance / 2 && roll(s, 3) < chance / 2);
    }

    // ----- roles and strengths -----

    @Test
    void roleMatrixDecidesEligibility() {
        assertEligible(true, UnitType.COLONIST, SOLDIER);
        assertEligible(true, UnitType.COLONIST, DRAGOON);
        assertEligible(true, UnitType.COLONIST, SCOUT);
        assertEligible(true, UnitType.SEASONED_SCOUT, SEASONED);
        assertEligible(true, UnitType.ARTILLERY, INTACT_GUN);
        assertEligible(false, UnitType.COLONIST, UNARMED);
        assertEligible(false, UnitType.WAGON, UNARMED);
    }

    private static void assertEligible(final boolean expected,
            final UnitType type, final Consumer<UnitPo> equip) {
        final Model model = new World().type(type).equip(equip).model();
        assertEquals(expected,
                model.getNativeConquestQuote(attacker(model), site(model))
                        .isPresent(),
                type.name());
    }

    @Test
    void scoutUsesTheOrdinaryMountedBaseOnlyForTheSettlement() {
        // Base 2 for a soldier, 3 for a mounted colonist, then +50% attack.
        assertEquals(2.0, quote(new World().equip(SOLDIER).model())
                .getFirstBattle().getAttacker().getBase(), 1e-9);
        final Model scout = new World().equip(SCOUT).model();
        assertEquals(3.0, quote(scout).getFirstBattle().getAttacker().getBase(),
                1e-9);
        assertEquals(4.5,
                quote(scout).getFirstBattle().getAttacker().getTotal(), 1e-9);
        // The ordinary fight of the same scout against an open brave is still
        // refused: only the settlement attack admits it.
        final Model open = new World().equip(SCOUT)
                .extras(scene -> scene.brave(NATION, null, 2, 0)).model();
        assertTrue(
                CombatStrengths
                        .forBattle(open, attacker(open),
                                unitAt(open, NativeTurnScene.at(2, 0)))
                        .isEmpty());
    }

    @Test
    void defenseTableUsesClassAndCapitalBonusOnTheActualDefender() {
        for (final NativeNation nation : NativeNation.values()) {
            final int classBonus = classBonus(nation);
            for (final boolean capital : new boolean[] { false, true }) {
                final Model model = new World().nation(nation).capital(capital)
                        .model();
                final int expected = classBonus + (capital ? 25 : 0);
                assertEquals(expected, quote(model).getDefenseBonusPercent(),
                        nation + " capital=" + capital);
                assertEquals(expected,
                        quote(model).getFirstBattle().getDefender()
                                .getColonyBonus(),
                        nation + " capital=" + capital);
            }
        }
    }

    /** Camp 0, village 25, city 50, written out from the agreed table. */
    private static int classBonus(final NativeNation nation) {
        switch (nation) {
        case TUPI:
        case SIOUX:
        case APACHE:
            return 0;
        case ARAWAK:
        case CHEROKEE:
        case IROQUOIS:
            return 25;
        case AZTEC:
        case INCA:
            return 50;
        default:
            throw new IllegalStateException(nation.name());
        }
    }

    @Test
    void spanishBonusBelongsToSpainAndTheSettlementAttackOnly() {
        final Model mock = mock(Model.class);
        final Player spain = new PlayerHuman("Spain", false, 0, mock, false,
                Map.of(), Set.of());
        final Player dutch = new PlayerHuman("Dutch", false, 0, mock, false,
                Map.of(), Set.of());
        final Player france = new PlayerHuman("France", false, 0, mock, false,
                Map.of(), Set.of());
        final Player crown = new PlayerKing("Crown", true, 0, mock, false,
                dutch, Map.of(), Set.of());
        assertEquals(50, Model.settlementAttackBonus(spain));
        assertEquals(0, Model.settlementAttackBonus(dutch));
        assertEquals(0, Model.settlementAttackBonus(france));
        assertEquals(0, Model.settlementAttackBonus(crown));
    }

    @Test
    void spanishBonusEntersOnlyTheFounderSlotOfASettlementBattle() {
        final Model model = new World()
                .extras(scene -> scene.brave(NATION, null, 2, 0)).model();
        final Unit defender = quote(model).getDefenders().get(0);
        final CombatStrengths without = CombatStrengths
                .forSettlementBattle(model, attacker(model), defender, 25, 0)
                .orElseThrow();
        final CombatStrengths with = CombatStrengths
                .forSettlementBattle(model, attacker(model), defender, 25, 50)
                .orElseThrow();
        assertEquals(0, without.getAttacker().getFounderBonus());
        assertEquals(50, with.getAttacker().getFounderBonus());
        // Base 2, +50% attack, +50% Spain; no second bonus from equipment.
        assertEquals(3.0, without.getAttacker().getTotal(), 1e-9);
        assertEquals(4.0, with.getAttacker().getTotal(), 1e-9);
        // The Dutch quote carries no Spanish bonus, and a brave on open ground
        // gets neither the settlement defense nor any founder bonus.
        assertEquals(0, quote(model).getSpanishBonusPercent());
        final CombatStrengths open = CombatStrengths.forBattle(model,
                attacker(model), unitAt(model, NativeTurnScene.at(2, 0)))
                .orElseThrow();
        assertEquals(0, open.getAttacker().getFounderBonus());
        assertEquals(0, open.getDefender().getColonyBonus());
    }

    /** French colony Quebec of the free-play world. */
    private static final Location QUEBEC = Location.of(17, 10);

    /** Spanish soldier beside Quebec. */
    private static final int COLONY_ATTACKER_ID = 9020;

    /**
     * Turn the saved Dutch nation into Spain throughout the save: player,
     * Crown, units, treaty rows and the current actor. The free-play world has
     * no Spanish player, so this is the valid Spanish owner of the fixture.
     */
    private static void spanish(final ModelPo po) {
        for (final PlayerPo player : po.getGameManager().getPlayers()) {
            if ("Dutch".equals(player.getName())) {
                player.setName("Spain");
            }
            if ("Dutch".equals(player.getWhosKingThisPlayerIs())) {
                player.setWhosKingThisPlayerIs("Spain");
            }
        }
        for (final UnitPo unit : po.getUnits()) {
            if ("Dutch".equals(unit.getOwnerId())) {
                unit.setOwnerId("Spain");
            }
        }
        for (final EuropeanRelationPo row : po.getEuropeanRelations()) {
            if ("Dutch".equals(row.getFirstPlayer())) {
                row.setFirstPlayer("Spain");
            }
            if ("Dutch".equals(row.getSecondPlayer())) {
                row.setSecondPlayer("Spain");
            }
        }
        if ("Dutch".equals(po.getGameManager().getCurrentPlayer())) {
            po.getGameManager().setCurrentPlayer("Spain");
        }
    }

    /** First land square beside Quebec in the saved free-play terrain. */
    private static Location besideQuebec() {
        final Map<Location, TerrainType> land = NativeTurnScene.island().po()
                .getMap().getTerrainMap();
        return QUEBEC.getNeighbors().stream().filter(land::containsKey)
                .sorted(java.util.Comparator.comparingInt(Location::getX)
                        .thenComparingInt(Location::getY))
                .findFirst().orElseThrow();
    }

    /** Arm the extra colony attacker and give it a movement point. */
    private static void armColonyAttacker(final ModelPo po) {
        for (final UnitPo unit : po.getUnits()) {
            if (unit.getId() == COLONY_ATTACKER_ID) {
                unit.setHoldingGuns(true);
                unit.setMovementThirds(MovementPoints.ofPoints(1));
            }
        }
    }

    /** One world for both nations: a settlement, an open brave and Quebec. */
    private static World threeTargets(final boolean spain) {
        final Location colonyFront = besideQuebec();
        return new World().extras(scene -> scene.brave(NATION, null, 2, 0)
                .rawUnit(COLONY_ATTACKER_ID, UnitType.COLONIST, "Dutch", null,
                        null, colonyFront))
                .tweak(po -> {
                    armColonyAttacker(po);
                    if (spain) {
                        spanish(po);
                    }
                });
    }

    @Test
    void spanishNationalityAddsFiftyToTheSettlementAttackOnly() {
        final Model spain = threeTargets(true).model();
        final Model dutch = threeTargets(false).model();
        assertEquals(EuropeanNation.SPANISH, EuropeanNation
                .fromPlayerName(attacker(spain).getOwner().getName()));

        // Settlement: the real quote names +50 in the founder slot only.
        final NativeConquestQuote spanishQuote = quote(spain);
        final NativeConquestQuote dutchQuote = quote(dutch);
        assertEquals(50, spanishQuote.getSpanishBonusPercent());
        assertEquals(0, dutchQuote.getSpanishBonusPercent());
        assertEquals(50,
                spanishQuote.getFirstBattle().getAttacker().getFounderBonus());
        // Base 2, +50% attack, +50% Spain; nothing else is added.
        assertEquals(4.0,
                spanishQuote.getFirstBattle().getAttacker().getTotal(), 1e-9);
        assertEquals(3.0, dutchQuote.getFirstBattle().getAttacker().getTotal(),
                1e-9);
        assertEquals(dutchQuote.getFirstBattle().getDefender(),
                spanishQuote.getFirstBattle().getDefender());

        // A brave on open ground: the shared attack gives Spain nothing more.
        final AttackOrder spanishBrave = attacker(spain)
                .prepareAttack(NativeTurnScene.at(2, 0), true).orElseThrow();
        final AttackOrder dutchBrave = attacker(dutch)
                .prepareAttack(NativeTurnScene.at(2, 0), true).orElseThrow();
        assertEquals(0, spanishBrave.getCombatStrengths().orElseThrow()
                .getAttacker().getFounderBonus());
        assertEquals(3.0, spanishBrave.getCombatStrengths().orElseThrow()
                .getAttacker().getTotal(), 1e-9);
        assertEquals(dutchBrave.getCombatStrengths(),
                spanishBrave.getCombatStrengths());

        // A European colony: the colony battle of the same soldier role is
        // the same for Spain as for the Dutch.
        final AttackOrder spanishColony = spain.getUnitById(COLONY_ATTACKER_ID)
                .prepareAttack(QUEBEC, true).orElseThrow();
        final AttackOrder dutchColony = dutch.getUnitById(COLONY_ATTACKER_ID)
                .prepareAttack(QUEBEC, true).orElseThrow();
        assertTrue(spanishColony.getColonyDefense().isPresent());
        assertEquals(0, spanishColony.getCombatStrengths().orElseThrow()
                .getAttacker().getFounderBonus());
        assertEquals(dutchColony.getCombatStrengths(),
                spanishColony.getCombatStrengths());
    }

    @Test
    void spanishBonusDecidesTheActualBattleAndIsSaved() {
        final World spanishWorld = new World()
                .tweak(NativeConquestTest::spanish);
        final double spanishChance = chance(spanishWorld);
        final double dutchChance = chance(new World());
        assertTrue(spanishChance > dutchChance);
        // A draw that only the Spanish strength wins.
        final long seed = seedWhere(
                s -> roll(s, 1) > dutchChance && roll(s, 1) < spanishChance);

        final Model spain = spanishWorld.seed(seed).model();
        final Model dutch = new World().seed(seed).model();

        assertTrue(attack(spain).isConquered());
        assertFalse(attack(dutch).isConquered());
        final Model reloaded = NativeTurnScene.make(spain.save());
        assertNull(site(reloaded));
        assertEquals(1, reloaded.getTurnEventStore()
                .getForPlayer(reloaded.getPlayerByName("Spain")).stream()
                .filter(TurnEventNativeConquest.class::isInstance).count());
    }

    // ----- battles -----

    @Test
    void winningTheOnlyDefenderConquersOnceAndRecordsIt() {
        final double chance = chance(new World());
        final long seed = seedWhere(s -> roll(s, 1) < chance / 2);
        final Model model = new World().seed(seed).model();
        assertEquals(0.0, anger(model, NATION), 1e-9);

        final NativeConquestResult result = attack(model);

        assertTrue(result.isConquered());
        assertEquals(1, result.getDefendersDefeated());
        assertFalse(result.isAttackerLost());
        assertTrue(result.getLosses().isEmpty());
        assertNull(site(model));
        assertEquals(0, bravesOnTarget(model));
        assertEquals(TARGET_AT, attacker(model).getLocation());
        assertEquals(0, attacker(model).getActionPoints());
        assertEquals(1, conquests(model).size());
        final TurnEventNativeConquest event = conquests(model).get(0);
        assertEquals(TARGET_ID, event.getSettlementId());
        assertEquals(NATION, event.getNation());
        assertEquals(TARGET_AT, event.getLocation());
        assertFalse(event.isCapital());
        assertEquals(1, event.getDefendersDefeated());
        // Settlement destroyed: (20 + 20) tribal anger once, no capital factor.
        assertEquals(40.0, anger(model, NATION), 1e-9);
    }

    @Test
    void capitalConquestUsesTheCapitalFactorExactlyOnce() {
        final double chance = chance(new World().capital(true));
        final long seed = seedWhere(s -> roll(s, 1) < chance / 2);
        final Model model = new World().capital(true).seed(seed).model();

        assertTrue(attack(model).isConquered());

        assertEquals(80.0, anger(model, NATION), 1e-9);
        assertTrue(conquests(model).get(0).isCapital());
    }

    @Test
    void earnedPromotionIsReportedAndStaysOnTheAttacker() {
        final double chance = chance(new World());
        final long promoted = seedWhere(
                s -> roll(s, 1) < chance / 2 && roll(s, 2) < 0.25);
        final Model with = new World().seed(promoted).model();
        final NativeConquestResult won = attack(with);
        assertTrue(won.getPromotion().isPresent());
        assertEquals(ATTACKER_ID, won.getPromotion().get().getUnitId());
        assertTrue(((UnitFreeColonist) attacker(with)).isVeteran());

        final long unpromoted = seedWhere(
                s -> roll(s, 1) < chance / 2 && roll(s, 2) >= 0.25);
        final Model without = new World().seed(unpromoted).model();
        final NativeConquestResult plain = attack(without);
        assertTrue(plain.getPromotion().isEmpty());
        assertFalse(((UnitFreeColonist) attacker(without)).isVeteran());
    }

    @Test
    void losingTheBattleKeepsSettlementAndLosesEquipment() {
        final double chance = chance(new World());
        final long seed = seedWhere(s -> roll(s, 1) > chance);
        final Model model = new World().seed(seed).model();

        final NativeConquestResult result = attack(model);

        assertFalse(result.isConquered());
        assertTrue(result.isAttackerLost());
        assertEquals(0, result.getDefendersDefeated());
        assertNotNull(site(model));
        assertEquals(1, bravesOnTarget(model));
        assertFalse(((UnitFreeColonist) attacker(model)).isHoldingGuns());
        assertEquals(NativeNation.ARAWAK, site(model).getNation());
        assertTrue(conquests(model).isEmpty());
        // Attacked but not destroyed: (20) tribal anger once.
        assertEquals(20.0, anger(model, NATION), 1e-9);
        assertTrue(model.getNativeLocalAlarm(dutch(model), site(model)) > 0);
    }

    @Test
    void capitalAttackThatFailsRaisesTheCapitalFactorOnce() {
        final double chance = chance(new World().capital(true));
        final long seed = seedWhere(s -> roll(s, 1) > chance);
        final Model model = new World().capital(true).seed(seed).model();

        assertFalse(attack(model).isConquered());

        assertEquals(40.0, anger(model, NATION), 1e-9);
    }

    @Test
    void everyDefenderMustBeOvercomeBeforeRemoval() {
        final double chance = chance(new World().braves(2));
        final long seed = seedWhere(
                s -> roll(s, 1) < chance / 2 && roll(s, 3) > chance);
        final Model model = new World().braves(2).seed(seed).model();

        final NativeConquestResult result = attack(model);

        assertFalse(result.isConquered());
        assertEquals(1, result.getDefendersDefeated());
        assertTrue(result.isAttackerLost());
        assertNotNull(site(model));
        assertEquals(1, bravesOnTarget(model));
        assertTrue(conquests(model).isEmpty());
        // One settlement attack: 20 once, not 20 per brave.
        assertEquals(20.0, anger(model, NATION), 1e-9);
    }

    @Test
    void winningEveryDefenderConquersWithTheCountAndOneRaise() {
        final double chance = chance(new World().braves(2));
        final Model model = new World().braves(2).seed(winWinSeed(chance))
                .model();

        final NativeConquestResult result = attack(model);

        assertTrue(result.isConquered());
        assertEquals(2, result.getDefendersDefeated());
        assertNull(site(model));
        assertEquals(2, conquests(model).get(0).getDefendersDefeated());
        assertEquals(40.0, anger(model, NATION), 1e-9);
    }

    // ----- cancel and stale -----

    @Test
    void readingTheTermsChangesNothing() {
        final Model model = new World().braves(2).seed(77).model();
        final long random = random(model);
        final int points = attacker(model).getActionPoints();
        final double tribal = anger(model, NATION);

        assertEquals(quote(model), quote(model));

        assertEquals(random, random(model));
        assertEquals(points, attacker(model).getActionPoints());
        assertEquals(tribal, anger(model, NATION), 1e-9);
        assertNotNull(site(model));
        assertEquals(2, bravesOnTarget(model));
        assertTrue(conquests(model).isEmpty());
    }

    @Test
    void staleSecondDefenderIsRefusedBeforeAnyDrawMovementOrAnger() {
        final Model model = new World().braves(2).seed(99).model();
        final NativeConquestQuote shown = quote(model);
        final Unit first = shown.getDefenders().get(0);
        final Unit second = shown.getDefenders().get(1);
        final long random = random(model);
        final int points = attacker(model).getActionPoints();
        final double tribal = anger(model, NATION);
        final double alarm = model.getNativeLocalAlarm(dutch(model),
                site(model));

        // Only the second defender changes: it walks off the square.
        second.placeToLocation(NativeTurnScene.at(0, 1));

        assertThrows(IllegalStateException.class, () -> model
                .attackNativeSettlement(attacker(model), site(model), shown));

        assertEquals(random, random(model));
        assertEquals(points, attacker(model).getActionPoints());
        assertEquals(tribal, anger(model, NATION), 1e-9);
        assertEquals(alarm,
                model.getNativeLocalAlarm(dutch(model), site(model)), 1e-9);
        assertNotNull(site(model));
        assertTrue(model.getUnitsAt(TARGET_AT).contains(first));
        assertEquals(NativeTurnScene.at(0, 1), second.getLocation());
        assertTrue(conquests(model).isEmpty());
    }

    @Test
    void staleTermsOfAnotherWorldAreRefusedBeforeAnyDraw() {
        final Model live = new World().braves(2).model();
        final NativeConquestQuote shown = quote(live);
        final Model changed = new World().braves(1).model();
        final long random = random(changed);

        assertThrows(IllegalStateException.class,
                () -> changed.attackNativeSettlement(attacker(changed),
                        site(changed), shown));

        assertEquals(random, random(changed));
        assertNotNull(site(changed));
        assertEquals(1, bravesOnTarget(changed));
        assertTrue(conquests(changed).isEmpty());
    }

    // ----- aftermath -----

    @Test
    void externalBravesRehomeToTheNearestSameNationSettlementByIdOnTies() {
        final World world = new World().companion(-3, 0)
                .extras(scene -> scene.settlement(3, NATION, false, 3, 0)
                        .nativePlayer(NativeNation.AZTEC)
                        .settlement(4, NativeNation.AZTEC, true, 0, 4)
                        // Tie between settlements 2 and 3; the Aztec capital is
                        // nearer
                        // but another nation.
                        .brave(NATION, TARGET_ID, 0, 3)
                        // Nearest is settlement 3, not the lowest ID.
                        .brave(NATION, TARGET_ID, 2, -1)
                        // Not homed here: unchanged.
                        .brave(NativeNation.AZTEC, 4, 0, 2)
                        .brave(NATION, CAPITAL_ID, -2, 2));
        final double chance = chance(world);
        final Model model = world.seed(seedWhere(s -> roll(s, 1) < chance / 2))
                .model();

        assertTrue(attack(model).isConquered());

        assertEquals(CAPITAL_ID, homeOf(model, NativeTurnScene.at(0, 3)));
        assertEquals(3, homeOf(model, NativeTurnScene.at(2, -1)));
        assertEquals(4, homeOf(model, NativeTurnScene.at(0, 2)));
        assertEquals(CAPITAL_ID, homeOf(model, NativeTurnScene.at(-2, 2)));
        // The result is a valid saved game with the same homes.
        final Model reloaded = NativeTurnScene.make(model.save());
        assertEquals(CAPITAL_ID, homeOf(reloaded, NativeTurnScene.at(0, 3)));
        assertEquals(3, homeOf(reloaded, NativeTurnScene.at(2, -1)));
    }

    @Test
    void lastSettlementOfTheNationLeavesLivingHomelessBraves() {
        final World world = new World().capital(true)
                .extras(scene -> scene.brave(NATION, TARGET_ID, 0, 3));
        final double chance = chance(world);
        final Model model = world.seed(seedWhere(s -> roll(s, 1) < chance / 2))
                .model();

        assertTrue(attack(model).isConquered());

        final UnitNativeBrave survivor = (UnitNativeBrave) unitAt(model,
                NativeTurnScene.at(0, 3));
        assertTrue(survivor.getHomeSettlementId().isEmpty());
        assertTrue(model.isUnitInGame(survivor));
        assertTrue(model.getNativeSettlements().isEmpty());
        final Model reloaded = NativeTurnScene.make(model.save());
        assertTrue(
                ((UnitNativeBrave) unitAt(reloaded, NativeTurnScene.at(0, 3)))
                        .getHomeSettlementId().isEmpty());
    }

    @Test
    void overlappingClaimIsInheritedAndPurchasedRightsSurvive() {
        // Settlement 2 stands two squares east: column 1 is claimed by both.
        final World world = new World().companion(2, 0).attackerAt(-1, 0)
                .extras(scene -> scene.dutchColonist(1, 0)).tweak(po -> {
                    po.getNativeLandRights().add(right(1, 0));
                    po.getNativeLandRights().add(right(-1, 1));
                });
        final double chance = chance(world);
        final Model model = world.seed(seedWhere(s -> roll(s, 1) < chance / 2))
                .model();
        final Player player = dutch(model);
        assertEquals(TARGET_ID,
                model.quoteNativeLand(player, NativeTurnScene.at(1, 0)).get()
                        .getClaimant().getId());
        assertEquals(NativeLandStatus.PAID,
                model.quoteNativeLand(player, NativeTurnScene.at(1, 0)).get()
                        .getStatus());
        assertEquals(NativeLandStatus.UNPAID,
                model.quoteNativeLand(player, NativeTurnScene.at(1, 1)).get()
                        .getStatus());

        assertTrue(attack(model).isConquered());

        // Overlap: the other settlement now owns the claim; payment is kept.
        assertEquals(CAPITAL_ID,
                model.quoteNativeLand(player, NativeTurnScene.at(1, 0)).get()
                        .getClaimant().getId());
        assertEquals(NativeLandStatus.PAID,
                model.quoteNativeLand(player, NativeTurnScene.at(1, 0)).get()
                        .getStatus());
        assertEquals(CAPITAL_ID,
                model.quoteNativeLand(player, NativeTurnScene.at(1, 1)).get()
                        .getClaimant().getId());
        assertEquals(NativeLandStatus.UNPAID,
                model.quoteNativeLand(player, NativeTurnScene.at(1, 1)).get()
                        .getStatus());
        // Land only the conquered settlement claimed is claimed by nobody.
        assertTrue(model.quoteNativeLand(player, NativeTurnScene.at(-1, -1))
                .isEmpty());
        assertTrue(model.quoteNativeLand(player, NativeTurnScene.at(-1, 1))
                .isEmpty());
        // The purchased right of that unclaimed square is still on record.
        final Set<Location> paid = model.save().getNativeLandRights().stream()
                .filter(record -> record.getEuropean() == EuropeanNation.DUTCH)
                .map(NativeLandRecordPo::getLocation)
                .collect(Collectors.toSet());
        assertTrue(paid.contains(NativeTurnScene.at(1, 0)));
        assertTrue(paid.contains(NativeTurnScene.at(-1, 1)));
        final Model reloaded = NativeTurnScene.make(model.save());
        assertEquals(NativeLandStatus.PAID, reloaded
                .quoteNativeLand(dutch(reloaded), NativeTurnScene.at(1, 0))
                .get().getStatus());
    }

    private static NativeLandRecordPo right(final int dx, final int dy) {
        final NativeLandRecordPo record = new NativeLandRecordPo();
        record.setEuropean(EuropeanNation.DUTCH);
        record.setLocation(NativeTurnScene.at(dx, dy));
        return record;
    }

    @Test
    void pupilAndMissionaryAreReleasedToTheNearestLegalSquareWithoutSkill() {
        // The French blocker takes the first square of the ring, so the fixed
        // coordinate order gives the next one.
        final World world = new World().capital(true)
                .extras(scene -> scene
                        .rawUnit(PUPIL_ID, UnitType.COLONIST, "Dutch", null,
                                null, NativeTurnScene.at(1, 1))
                        .rawUnit(MISSIONARY_ID, UnitType.COLONIST, "Dutch",
                                null, null, NativeTurnScene.at(1, -1))
                        .rawUnit(FRENCH_BLOCKER_ID, UnitType.COLONIST, "France",
                                null, null, NativeTurnScene.at(-1, -1)))
                .tweak(po -> missionary(po, MISSIONARY_ID));
        final double chance = chance(world);
        final Model model = world.seed(seedWhere(s -> roll(s, 1) < chance / 2))
                .model();
        startCourses(model, PUPIL_ID, MISSIONARY_ID);
        assertTrue(model.getUnitById(PUPIL_ID).isAtPlaceNativeCourse());
        assertTrue(model.getUnitById(MISSIONARY_ID).isAtPlaceNativeMission());

        final NativeConquestResult result = attack(model);

        assertTrue(result.isConquered());
        assertTrue(result.getLosses().isEmpty());
        for (final int id : new int[] { PUPIL_ID, MISSIONARY_ID }) {
            final Unit released = model.getUnitById(id);
            assertTrue(released.isAtPlaceLocation());
            assertFalse(released.isAtPlaceNativeCourse());
            assertFalse(released.isAtPlaceNativeMission());
            assertEquals(NativeTurnScene.at(-1, 0), released.getLocation());
            // The same unit, and no skill was granted by the cut-short course.
            assertEquals(UnitType.COLONIST, released.getType());
        }
        assertTrue(((UnitFreeColonist) model.getUnitById(MISSIONARY_ID))
                .isMissionary());
        assertTrue(losses(model, "Dutch").isEmpty());
        // The saved game has no dangling course or mission.
        final Model reloaded = NativeTurnScene.make(model.save());
        assertTrue(reloaded.getUnitById(PUPIL_ID).isAtPlaceLocation());
        assertTrue(reloaded.getUnitById(MISSIONARY_ID).isAtPlaceLocation());
    }

    /**
     * A French pupil and missionary in the settlement, a Dutch attack that
     * wins, and every square around the settlement held by a Dutch unit: the
     * attacker's own square keeps a Dutch companion when the attacker moves in,
     * so no square beside the settlement is legal for the French. The courses
     * start on the French turn and the attack follows on the Dutch turn.
     *
     * @param land  change of the island before any unit is placed
     * @param world change of the whole saved world, such as removing land
     * @return the model on the Dutch turn, with a winning draw saved
     */
    private static Model frenchCoursesBesideDutchAttack(
            final UnaryOperator<NativeTurnScene> land,
            final Consumer<ModelPo> world) {
        final World setup = new World().capital(true).current("France")
                .extras(scene -> {
                    NativeTurnScene placed = land.apply(scene)
                            .rawUnit(PUPIL_ID, UnitType.COLONIST, "France",
                                    null, null, NativeTurnScene.at(1, 1))
                            .rawUnit(MISSIONARY_ID, UnitType.COLONIST, "France",
                                    null, null, NativeTurnScene.at(1, -1))
                            // The companion stays on the attacker's square; the
                            // two others later fill the course units' squares.
                            .rawUnit(9004, UnitType.COLONIST, "Dutch", null,
                                    null, NativeTurnScene.at(1, 0))
                            .rawUnit(9005, UnitType.COLONIST, "Dutch", null,
                                    null, NativeTurnScene.at(1, 0))
                            .rawUnit(9006, UnitType.COLONIST, "Dutch", null,
                                    null, NativeTurnScene.at(1, 0));
                    int id = 9010;
                    for (final Location shore : List.of(
                            NativeTurnScene.at(-1, -1),
                            NativeTurnScene.at(-1, 0),
                            NativeTurnScene.at(-1, 1),
                            NativeTurnScene.at(0, -1),
                            NativeTurnScene.at(0, 1))) {
                        placed = placed.rawUnit(id++, UnitType.COLONIST,
                                "Dutch", null, null, shore);
                    }
                    return placed;
                }).tweak(po -> {
                    missionary(po, MISSIONARY_ID);
                    world.accept(po);
                });
        final double chance = chance(setup.current("Dutch"));
        final Model french = setup.current("France").seed(0).model();
        startCourses(french, PUPIL_ID, MISSIONARY_ID);
        // Fixture preparation in a separate model: the two squares the course
        // units left are taken by Dutch units before the saved Dutch turn.
        french.getUnitById(9005).placeToLocation(NativeTurnScene.at(1, 1));
        french.getUnitById(9006).placeToLocation(NativeTurnScene.at(1, -1));
        final ModelPo saved = french.save();
        saved.getGameManager().setCurrentPlayer("Dutch");
        saved.setCombatRandomState(seedWhere(s -> roll(s, 1) < chance / 2));
        return NativeTurnScene.make(saved);
    }

    /**
     * Leave no land in the world except the squares within the radius of the
     * settlement: the saved French colonies and their units are removed, so
     * nothing elsewhere is a legal square for anyone.
     */
    private static void onlyLandAroundTheSettlement(final ModelPo po,
            final int radius) {
        po.setColonies(new ArrayList<>());
        po.getUnits().removeIf(unit -> unit.getId() < 5000
                && "France".equals(unit.getOwnerId()));
        final Map<Location, TerrainType> land = new HashMap<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                land.put(NativeTurnScene.at(dx, dy), TerrainType.GRASSLAND);
            }
        }
        po.getMap().setTerrainType(land);
        po.getMap().setTrees(Set.of());
    }

    @Test
    void releaseIsAnEvacuationToTheNearestLegalSquareAcrossWater() {
        // The ring two squares out is water, so the nearest legal squares are
        // three out, on land not joined to the settlement's square.
        final Model model = frenchCoursesBesideDutchAttack(scene -> {
            NativeTurnScene flooded = scene;
            for (int dx = -2; dx <= 2; dx++) {
                for (int dy = -2; dy <= 2; dy++) {
                    if (Math.max(Math.abs(dx), Math.abs(dy)) == 2) {
                        flooded = flooded.sea(dx, dy);
                    }
                }
            }
            return flooded;
        }, po -> {
        });

        final NativeConquestResult result = attack(model);

        assertTrue(result.isConquered());
        assertTrue(result.getLosses().isEmpty());
        // Lowest X, then lowest Y, of the nearest ring with a legal square;
        // both units of the same owner may share it.
        for (final int id : new int[] { PUPIL_ID, MISSIONARY_ID }) {
            final Unit released = model.getUnitById(id);
            assertTrue(released.isAtPlaceLocation());
            assertEquals(NativeTurnScene.at(-3, -3), released.getLocation());
            assertEquals(UnitType.COLONIST, released.getType());
        }
        assertEquals(TARGET_AT, attacker(model).getLocation());
        assertTrue(losses(model, "France").isEmpty());
    }

    @Test
    void trappedPupilAndMissionaryDieAndAreReportedAndPersisted() {
        // The world has no other land: the eight squares beside the settlement
        // hold Dutch units, the settlement's own square holds the attacker, and
        // every other square of the map is water. No square is legal.
        final Model model = frenchCoursesBesideDutchAttack(scene -> scene,
                po -> onlyLandAroundTheSettlement(po, 1));

        final NativeConquestResult result = attack(model);

        assertTrue(result.isConquered());
        assertTrue(result.isTrappedUnitLost());
        assertEquals(2, result.getLosses().size());
        assertEquals(PUPIL_ID, result.getLosses().get(0).getUnitId());
        assertTrue(result.getLosses().get(0).isPupil());
        assertEquals(MISSIONARY_ID, result.getLosses().get(1).getUnitId());
        assertFalse(result.getLosses().get(1).isPupil());
        assertEquals("France", result.getLosses().get(0).getOwnerName());
        assertTrue(model.tryGetUnitById(PUPIL_ID).isEmpty());
        assertTrue(model.tryGetUnitById(MISSIONARY_ID).isEmpty());
        // Each death is its own saved notice of the owner, not the attacker.
        assertEquals(2, losses(model, "France").size());
        assertTrue(losses(model, "Dutch").isEmpty());
        assertEquals(1, conquests(model).size());
        final TurnEventNativeConquestLoss pupilNotice = losses(model, "France")
                .get(0);
        assertEquals(PUPIL_ID, pupilNotice.getUnitId());
        assertTrue(pupilNotice.isPupil());
        assertEquals(NATION, pupilNotice.getNation());
        assertEquals(TARGET_AT, pupilNotice.getLocation());
        // Save and Continue keep the notices exactly once and load cleanly.
        final Model reloaded = NativeTurnScene.make(model.save());
        assertEquals(2, losses(reloaded, "France").size());
        assertEquals(1, conquests(reloaded).size());
        assertTrue(reloaded.tryGetUnitById(PUPIL_ID).isEmpty());
        assertEquals(2,
                losses(NativeTurnScene.make(reloaded.save()), "France").size());
    }

    private static void missionary(final ModelPo po, final int unitId) {
        for (final UnitPo unit : po.getUnits()) {
            if (unit.getId() == unitId) {
                unit.setMissionary(true);
            }
        }
    }

    /** The pupil starts the settlement's course, the missionary its mission. */
    private static void startCourses(final Model model, final int pupilId,
            final int missionaryId) {
        final NativeSettlement site = site(model);
        model.startNativeTeaching(model.getUnitById(pupilId), site,
                site.getTeachableSkill());
        final Unit missionary = model.getUnitById(missionaryId);
        model.establishNativeMission(missionary, site,
                model.getNativeMissionPreview(missionary, site).orElseThrow());
    }

    // ----- persistence -----

    @Test
    void conquestPersistsOnceAndIsNotRepeatedAfterReload() {
        final double chance = chance(new World());
        final long seed = seedWhere(s -> roll(s, 1) < chance / 2);
        final Model model = new World().seed(seed).model();
        // The stable ID is bound before the action: the victory moves the
        // attacker onto the settlement square.
        final int attackerId = attacker(model).getId();
        assertEquals(NativeTurnScene.at(1, 0),
                model.getUnitById(attackerId).getLocation());
        assertTrue(attack(model).isConquered());
        assertEquals(TARGET_AT, model.getUnitById(attackerId).getLocation());
        final double tribal = anger(model, NATION);

        final Model reloaded = NativeTurnScene.make(model.save());

        assertEquals(UnitType.COLONIST,
                reloaded.getUnitById(attackerId).getType());
        assertEquals(TARGET_AT, reloaded.getUnitById(attackerId).getLocation());
        assertNull(settlementAt(reloaded, TARGET_AT));
        assertEquals(List.of(CAPITAL_ID),
                reloaded.getNativeSettlements().stream()
                        .map(NativeSettlement::getId)
                        .collect(Collectors.toList()));
        assertTrue(reloaded.getNativeSettlementsKnownTo(dutch(reloaded))
                .stream().noneMatch(s -> s.getId() == TARGET_ID));
        assertEquals(0, bravesOnTarget(reloaded));
        assertEquals(1, conquests(reloaded).size());
        assertEquals(tribal, anger(reloaded, NATION), 1e-9);
        // A second save and load repeats nothing either.
        final Model again = NativeTurnScene.make(reloaded.save());
        assertEquals(1, conquests(again).size());
        assertEquals(tribal, anger(again, NATION), 1e-9);
        assertEquals(TARGET_AT, again.getUnitById(attackerId).getLocation());
    }

    @Test
    void lostAttackPersistsTheSettlementAndItsRaiseOnce() {
        final double chance = chance(new World());
        final long seed = seedWhere(s -> roll(s, 1) > chance);
        final Model model = new World().seed(seed).model();
        assertFalse(attack(model).isConquered());

        final Model reloaded = NativeTurnScene.make(model.save());

        assertNotNull(site(reloaded));
        assertEquals(1, bravesOnTarget(reloaded));
        assertEquals(20.0, anger(reloaded, NATION), 1e-9);
        assertTrue(conquests(reloaded).isEmpty());
    }
}
