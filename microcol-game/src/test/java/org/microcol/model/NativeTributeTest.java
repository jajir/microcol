package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.microcol.model.store.CargoSlotPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.NativeTributeRecordPo;
import org.microcol.model.store.PlaceCargoSlotPo;
import org.microcol.model.store.UnitPo;

/**
 * Native tribute demand (R-0162): the role matrix, the force against the
 * defenders' strength with equality paying, the own adjacent force counted once
 * with foreign, distant, artillery and carried units excluded, the caps of an
 * ordinary settlement, a capital and an empty treasury, the gold and relation
 * changes applied once, the ten-turn wait across a save, the separate wait of
 * each European, stale and cancelled terms that change nothing, and the saved
 * history that is validated when the model is built. The defenders of a Tupi
 * camp stand on grassland with no bonus, so each brave has defense 1; a Tupi
 * capital adds 25 percent and an Aztec city 50 percent. No draw is taken.
 */
class NativeTributeTest {

    /** Camp: no settlement defense bonus of its own. */
    private static final NativeNation CAMP_NATION = NativeNation.TUPI;

    /** City: a 50 percent settlement defense bonus. */
    private static final NativeNation CITY_NATION = NativeNation.AZTEC;

    private static final int TARGET_ID = 1;

    private static final int CAPITAL_ID = 2;

    private static final int VISITOR_ID = 9000;

    /** Soldier that rides in the caravel, or stands beside it on the map. */
    private static final int PASSENGER_ID = 9101;

    private static final int CARAVEL_ID = 9100;

    private static final String DUTCH = "Dutch";

    private static final String FRANCE = "France";

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

    private static final Consumer<UnitPo> MISSIONARY = unit -> unit
            .setMissionary(true);

    private static final Consumer<UnitPo> INTACT_GUN = unit -> unit
            .setArtilleryDamaged(false);

    /**
     * A native settlement with the given number of braves on its square. The
     * nation's capital stands beside an ordinary one, so the nation has exactly
     * one capital.
     */
    private static NativeTurnScene village(final NativeNation nation,
            final boolean capital, final int braves) {
        NativeTurnScene scene = NativeTurnScene.island().nativePlayer(nation)
                .settlement(TARGET_ID, nation, capital, 0, 0);
        if (!capital) {
            scene = scene.settlement(CAPITAL_ID, nation, true, -3, -3);
        }
        for (int index = 0; index < braves; index++) {
            scene = scene.brave(nation, TARGET_ID, 0, 0);
        }
        return scene;
    }

    /**
     * A colonial unit of the owner on the given square. The saved unit is the
     * last one added, so its equipment is set right after it is placed.
     */
    private static NativeTurnScene unit(final NativeTurnScene scene,
            final UnitType type, final int id, final String owner, final int dx,
            final int dy, final Consumer<UnitPo> equipment) {
        scene.rawUnit(id, type, owner, null, null, NativeTurnScene.at(dx, dy));
        final List<UnitPo> units = scene.po().getUnits();
        equipment.accept(units.get(units.size() - 1));
        return scene;
    }

    /**
     * A Dutch soldier beside the target, the visitor of most tests.
     */
    private static NativeTurnScene visitor(final NativeTurnScene scene) {
        return unit(scene, UnitType.COLONIST, VISITOR_ID, DUTCH, 1, 0, SOLDIER);
    }

    /** Model of the saved scene with the given current player and game turn. */
    private static Model model(final NativeTurnScene scene,
            final String current, final int turn) {
        final ModelPo po = scene.po();
        po.getGameManager().setGameStarted(true);
        po.getGameManager().setCurrentPlayer(current);
        po.getCalendar().setNumberOfPlayedTurns(turn);
        return NativeTurnScene.make(po);
    }

    private static Model reload(final Model model, final String current,
            final int turn) {
        final ModelPo po = model.save();
        po.getGameManager().setCurrentPlayer(current);
        po.getCalendar().setNumberOfPlayedTurns(turn);
        return NativeTurnScene.make(po);
    }

    private static Unit visitor(final Model model) {
        return model.getUnitById(VISITOR_ID);
    }

    private static NativeSettlement target(final Model model) {
        return model.getNativeSettlements().stream().filter(
                settlement -> settlement.getLocation().equals(TARGET_AT))
                .findAny().orElseThrow();
    }

    private static Optional<NativeTributeQuote> quote(final Model model) {
        return model.getNativeTributeQuote(visitor(model), target(model));
    }

    private static NativeTributeQuote quoteOf(final Model model) {
        return quote(model).orElseThrow();
    }

    private static NativeTributeResult demand(final Model model) {
        return model.demandNativeTribute(visitor(model), target(model),
                quoteOf(model));
    }

    private static NativeVisitChoice.Limit tributeLimit(final Model model) {
        return model.getNativeVisitChoices(visitor(model), target(model))
                .stream()
                .filter(choice -> choice
                        .getAction() == NativeVisitAction.TRIBUTE)
                .findFirst().orElseThrow().getLimit();
    }

    private static int gold(final Model model, final String owner) {
        return model.getPlayerByName(owner).getGold();
    }

    private static long random(final Model model) {
        return model.save().getCombatRandomState();
    }

    private static double anger(final Model model, final NativeNation nation) {
        return model.getNativeTribalAnger(model.getPlayerByName(DUTCH), nation);
    }

    private static double alarm(final Model model, final String owner) {
        return model.getNativeLocalAlarm(model.getPlayerByName(owner),
                target(model));
    }

    private static NativeTurnScene withTreasury(final NativeTurnScene scene,
            final int treasury) {
        scene.po().getNativeSettlements().get(0).setTreasury(treasury);
        return scene;
    }

    @Test
    void soldierDragoonAndScoutMayDemandButOtherRolesMayNot() {
        assertTrue(
                quote(model(visitor(village(CAMP_NATION, false, 1)), DUTCH, 5))
                        .isPresent());
        assertTrue(quote(model(unit(village(CAMP_NATION, false, 1),
                UnitType.COLONIST, VISITOR_ID, DUTCH, 1, 0, DRAGOON), DUTCH, 5))
                .isPresent());
        assertTrue(quote(model(unit(village(CAMP_NATION, false, 1),
                UnitType.COLONIST, VISITOR_ID, DUTCH, 1, 0, SCOUT), DUTCH, 5))
                .isPresent());
        assertFalse(quote(model(unit(village(CAMP_NATION, false, 1),
                UnitType.COLONIST, VISITOR_ID, DUTCH, 1, 0, UNARMED), DUTCH, 5))
                .isPresent());
        assertFalse(quote(
                model(unit(village(CAMP_NATION, false, 1), UnitType.COLONIST,
                        VISITOR_ID, DUTCH, 1, 0, MISSIONARY), DUTCH, 5))
                .isPresent());
        assertFalse(quote(
                model(unit(village(CAMP_NATION, false, 1), UnitType.ARTILLERY,
                        VISITOR_ID, DUTCH, 1, 0, INTACT_GUN), DUTCH, 5))
                .isPresent());
        assertFalse(quote(model(unit(village(CAMP_NATION, false, 1),
                UnitType.WAGON, VISITOR_ID, DUTCH, 1, 0, UNARMED), DUTCH, 5))
                .isPresent());
    }

    @Test
    void forceEqualToTheDefendersPaysAndBelowIsRefused() {
        final NativeTributeQuote equal = quoteOf(
                model(visitor(village(CAMP_NATION, false, 2)), DUTCH, 5));
        assertEquals(2.0, equal.getForce(), 1e-9);
        assertEquals(2.0, equal.getDefense(), 1e-9);
        assertTrue(equal.isAccepted());
        assertEquals(100, equal.getPayment());

        final NativeTributeQuote below = quoteOf(
                model(visitor(village(CAMP_NATION, false, 3)), DUTCH, 5));
        assertEquals(3.0, below.getDefense(), 1e-9);
        assertFalse(below.isAccepted());
        assertEquals(0, below.getPayment());
    }

    @Test
    void ownAdjacentForceIsCountedOnceAndForeignDistantAndArtilleryAreExcluded() {
        NativeTurnScene scene = village(CAMP_NATION, false, 11);
        scene = visitor(scene);
        scene = unit(scene, UnitType.COLONIST, 9001, DUTCH, 1, 1, SOLDIER);
        scene = unit(scene, UnitType.COLONIST, 9002, DUTCH, -1, 0, DRAGOON);
        scene = unit(scene, UnitType.COLONIST, 9003, DUTCH, 0, 1, SCOUT);
        scene = unit(scene, UnitType.COLONIST, 9004, FRANCE, -1, -1, SOLDIER);
        scene = unit(scene, UnitType.ARTILLERY, 9005, DUTCH, 0, -1, INTACT_GUN);
        scene = unit(scene, UnitType.COLONIST, 9006, DUTCH, 2, 0, SOLDIER);
        final NativeTributeQuote terms = quoteOf(model(scene, DUTCH, 5));
        // Visitor 2 + soldier 2 + dragoon 3 + scout 3 = 10; the foreign
        // soldier,
        // the artillery and the distant soldier are not counted.
        assertEquals(List.of(VISITOR_ID, 9001, 9002, 9003),
                terms.getForceUnitIds());
        assertEquals(10.0, terms.getForce(), 1e-9);
        assertEquals(11.0, terms.getDefense(), 1e-9);
        assertFalse(terms.isAccepted());
    }

    @Test
    void theSameAdjacentForceAtExactlyTheDefenseIsAccepted() {
        NativeTurnScene scene = village(CAMP_NATION, false, 10);
        scene = visitor(scene);
        scene = unit(scene, UnitType.COLONIST, 9001, DUTCH, 1, 1, SOLDIER);
        scene = unit(scene, UnitType.COLONIST, 9002, DUTCH, -1, 0, DRAGOON);
        scene = unit(scene, UnitType.COLONIST, 9003, DUTCH, 0, 1, SCOUT);
        scene = unit(scene, UnitType.COLONIST, 9004, FRANCE, -1, -1, SOLDIER);
        final NativeTributeQuote terms = quoteOf(model(scene, DUTCH, 5));
        assertEquals(10.0, terms.getForce(), 1e-9);
        assertEquals(10.0, terms.getDefense(), 1e-9);
        assertTrue(terms.isAccepted());
    }

    @Test
    void paymentIsCappedByTheSettlementAndTheCapitalAndAnEmptyTreasuryPaysNothing() {
        // An ordinary settlement pays at most 100, or less when it holds less.
        assertEquals(100, quoteOf(
                model(visitor(village(CAMP_NATION, false, 1)), DUTCH, 5))
                .getPayment());
        assertEquals(50,
                quoteOf(model(withTreasury(
                        visitor(village(CAMP_NATION, false, 1)), 50), DUTCH, 5))
                        .getPayment());
        // A capital pays at most 200 and has the 25 percent defense bonus.
        final NativeTributeQuote capital = quoteOf(
                model(visitor(village(CAMP_NATION, true, 1)), DUTCH, 5));
        assertEquals(25, capital.getDefenseBonusPercent());
        assertEquals(1.25, capital.getDefense(), 1e-9);
        assertEquals(200, capital.getPayment());
        assertEquals(150,
                quoteOf(model(withTreasury(
                        visitor(village(CAMP_NATION, true, 1)), 150), DUTCH, 5))
                        .getPayment());
        // An empty treasury is accepted and pays exactly nothing.
        final NativeTributeQuote empty = quoteOf(
                model(withTreasury(visitor(village(CAMP_NATION, true, 1)), 0),
                        DUTCH, 5));
        assertTrue(empty.isAccepted());
        assertEquals(0, empty.getPayment());
        // The city class of the Aztecs: 50 percent for each brave.
        final NativeTributeQuote city = quoteOf(
                model(visitor(village(CITY_NATION, false, 1)), DUTCH, 5));
        assertEquals(50, city.getDefenseBonusPercent());
        assertEquals(1.5, city.getDefense(), 1e-9);
        assertTrue(city.isAccepted());
        assertFalse(quoteOf(
                model(visitor(village(CITY_NATION, false, 2)), DUTCH, 5))
                .isAccepted());
        assertEquals(1.75,
                quoteOf(model(visitor(village(CITY_NATION, true, 1)), DUTCH, 5))
                        .getDefense(),
                1e-9);
    }

    @Test
    void paidDemandMovesGoldOnceAndRaisesAngerAndAlarmOnce() {
        final Model model = model(visitor(village(CAMP_NATION, false, 1)),
                DUTCH, 5);
        final int gold = gold(model, DUTCH);
        final int treasury = target(model).getTreasury();
        final long random = random(model);
        final NativeTributeQuote shown = quoteOf(model);
        final NativeTributeResult result = demand(model);
        assertTrue(result.isAccepted());
        assertEquals(100, result.getGold());
        assertEquals(treasury - 100, target(model).getTreasury());
        assertEquals(gold + 100, gold(model, DUTCH));
        assertEquals(treasury + gold,
                target(model).getTreasury() + gold(model, DUTCH));
        // Tribal anger and local alarm each rise by 10, once.
        assertEquals(10.0, anger(model, CAMP_NATION), 1e-9);
        assertEquals(10.0, alarm(model, DUTCH), 1e-9);
        assertEquals(random, random(model));
        assertEquals(Optional.of(5),
                target(model).findTributeTurn(EuropeanNation.DUTCH));
        // The same terms are stale now: the wait refuses them and nothing
        // moves.
        assertThrows(IllegalStateException.class, () -> model
                .demandNativeTribute(visitor(model), target(model), shown));
        assertEquals(treasury - 100, target(model).getTreasury());
        assertEquals(gold + 100, gold(model, DUTCH));
        assertEquals(10.0, anger(model, CAMP_NATION), 1e-9);
        assertEquals(10.0, alarm(model, DUTCH), 1e-9);
    }

    @Test
    void refusedDemandPaysNothingButStillRaisesAngerAndAlarmOnce() {
        final Model model = model(visitor(village(CAMP_NATION, false, 3)),
                DUTCH, 5);
        final int gold = gold(model, DUTCH);
        final int treasury = target(model).getTreasury();
        final NativeTributeResult result = demand(model);
        assertFalse(result.isAccepted());
        assertEquals(0, result.getGold());
        assertEquals(treasury, target(model).getTreasury());
        assertEquals(gold, gold(model, DUTCH));
        assertEquals(10.0, anger(model, CAMP_NATION), 1e-9);
        assertEquals(10.0, alarm(model, DUTCH), 1e-9);
        assertEquals(NativeVisitChoice.Limit.TRIBUTE_COOLDOWN,
                tributeLimit(model));
    }

    @Test
    void cooldownBlocksTheNinthTurnAndAllowsTheTenthAfterAReload() {
        final Model demanded = model(visitor(village(CAMP_NATION, false, 1)),
                DUTCH, 5);
        demand(demanded);
        final int treasury = target(demanded).getTreasury();
        final int gold = gold(demanded, DUTCH);

        // Nine turns later the saved wait still holds; loading ticks nothing.
        final Model ninth = reload(demanded, DUTCH, 14);
        assertTrue(quote(ninth).isEmpty());
        assertEquals(NativeVisitChoice.Limit.TRIBUTE_COOLDOWN,
                tributeLimit(ninth));
        assertEquals(14, ninth.save().getCalendar().getNumberOfPlayedTurns());
        assertEquals(treasury, target(ninth).getTreasury());
        assertEquals(gold, gold(ninth, DUTCH));

        // Ten turns later the wait is over and the demand is offered again.
        final Model tenth = reload(ninth, DUTCH, 15);
        assertTrue(quote(tenth).isPresent());
        assertEquals(NativeVisitChoice.Limit.AVAILABLE, tributeLimit(tenth));
        assertEquals(10.0, anger(tenth, CAMP_NATION), 1e-9);
    }

    @Test
    void aSecondEuropeanIsNotBlockedByTheDutchWait() {
        final NativeTurnScene scene = unit(
                visitor(village(CAMP_NATION, false, 1)), UnitType.COLONIST,
                9001, FRANCE, -1, 0, SOLDIER);
        final Model dutch = model(scene, DUTCH, 5);
        demand(dutch);
        final Model french = reload(dutch, FRANCE, 5);
        final Unit french9001 = french.getUnitById(9001);
        final NativeTributeQuote terms = french
                .getNativeTributeQuote(french9001, target(french))
                .orElseThrow();
        assertTrue(terms.isAccepted());
        final NativeTributeResult result = french
                .demandNativeTribute(french9001, target(french), terms);
        assertTrue(result.isAccepted());
        // France halves the local alarm, while the direct tribal anger is not.
        // Each European keeps its own anger toward the tribe and its own alarm.
        assertEquals(5.0, alarm(french, FRANCE), 1e-9);
        assertEquals(10.0, alarm(french, DUTCH), 1e-9);
        assertEquals(10.0, french.getNativeTribalAnger(
                french.getPlayerByName(FRANCE), CAMP_NATION), 1e-9);
        assertEquals(10.0, anger(french, CAMP_NATION), 1e-9);
        assertEquals(Optional.of(5),
                target(french).findTributeTurn(EuropeanNation.FRENCH));
    }

    @Test
    void staleTermsAreRefusedBeforeAnyChangeAndCancelingChangesNothing() {
        final Model model = model(visitor(village(CAMP_NATION, false, 1)),
                DUTCH, 5);
        final long random = random(model);
        final int gold = gold(model, DUTCH);
        final NativeTributeQuote shown = quoteOf(model);
        // Reading the terms and then backing out changes nothing.
        assertEquals(random, random(model));
        assertEquals(gold, gold(model, DUTCH));
        assertEquals(0.0, anger(model, CAMP_NATION), 1e-9);
        assertTrue(
                target(model).findTributeTurn(EuropeanNation.DUTCH).isEmpty());

        // The treasury changes after the terms were shown: refused, unchanged.
        target(model).setTreasury(500);
        assertThrows(IllegalStateException.class, () -> model
                .demandNativeTribute(visitor(model), target(model), shown));
        assertEquals(500, target(model).getTreasury());
        assertEquals(gold, gold(model, DUTCH));
        assertEquals(random, random(model));
        assertEquals(0.0, anger(model, CAMP_NATION), 1e-9);
        assertTrue(
                target(model).findTributeTurn(EuropeanNation.DUTCH).isEmpty());

        // The anger changes after the terms were shown: the same refusal.
        final Model angry = model(visitor(village(CAMP_NATION, false, 1)),
                DUTCH, 5);
        final NativeTributeQuote quoted = quoteOf(angry);
        angry.addNativeTribalAnger(CAMP_NATION, angry.getPlayerByName(DUTCH),
                5);
        assertThrows(IllegalStateException.class, () -> angry
                .demandNativeTribute(visitor(angry), target(angry), quoted));
        assertEquals(gold, gold(angry, DUTCH));
        assertEquals(5.0, anger(angry, CAMP_NATION), 1e-9);
    }

    @Test
    void chooserOffersTheDemandWithOrWithoutDefendersAndOutsideTheWait() {
        // An empty square is open: a demand is not an attack (R-0162).
        assertEquals(NativeVisitChoice.Limit.AVAILABLE, tributeLimit(
                model(visitor(village(CAMP_NATION, false, 0)), DUTCH, 5)));
        final Model model = model(visitor(village(CAMP_NATION, false, 1)),
                DUTCH, 5);
        assertEquals(NativeVisitChoice.Limit.AVAILABLE, tributeLimit(model));
        demand(model);
        assertEquals(NativeVisitChoice.Limit.TRIBUTE_COOLDOWN,
                tributeLimit(model));
    }

    /**
     * A non-native unit on the settlement square is not a native defender, so
     * the valid occupant check still refuses the demand (R-0162). The square
     * holds no brave, because the loader refuses an enemy stack on one square;
     * the occupant is an unarmed Dutch colonist.
     */
    @Test
    void aNonNativeUnitOnTheSettlementSquareRefusesTheDemand() {
        final NativeTurnScene scene = unit(
                visitor(village(CAMP_NATION, false, 0)), UnitType.COLONIST,
                9200, DUTCH, 0, 0, UNARMED);
        final Model model = model(scene, DUTCH, 5);
        assertTrue(quote(model).isEmpty());
        assertEquals(NativeVisitChoice.Limit.NO_TRIBUTE_TARGET,
                tributeLimit(model));
    }

    /**
     * With no defender on the square the defense is zero, so the demand pays
     * the ordinary cap, is recorded once, provokes the tribe, and then waits.
     */
    @Test
    void withoutDefendersTheDemandPaysTheCapOnceAndThenWaits() {
        final Model model = model(visitor(village(CAMP_NATION, false, 0)),
                DUTCH, 5);
        final NativeTributeQuote terms = quoteOf(model);
        assertEquals(2.0, terms.getForce(), 1e-9);
        assertEquals(0.0, terms.getDefense(), 1e-9);
        assertTrue(terms.isAccepted());
        assertEquals(100, terms.getPayment());
        final int gold = gold(model, DUTCH);
        final int treasury = target(model).getTreasury();
        final long random = random(model);
        final NativeTributeResult result = demand(model);
        assertTrue(result.isAccepted());
        assertEquals(100, result.getGold());
        assertEquals(treasury - 100, target(model).getTreasury());
        assertEquals(gold + 100, gold(model, DUTCH));
        assertEquals(10.0, anger(model, CAMP_NATION), 1e-9);
        assertEquals(10.0, alarm(model, DUTCH), 1e-9);
        assertEquals(random, random(model));
        assertEquals(NativeVisitChoice.Limit.TRIBUTE_COOLDOWN,
                tributeLimit(model));
    }

    /**
     * Without defenders a capital still pays up to its cap of 200 or its whole
     * treasury, and an empty treasury is accepted without any gold.
     */
    @Test
    void withoutDefendersTheCapitalAndAnEmptyTreasuryFollowThePurse() {
        assertEquals(150,
                quoteOf(model(withTreasury(
                        visitor(village(CAMP_NATION, true, 0)), 150), DUTCH, 5))
                        .getPayment());
        assertEquals(200,
                quoteOf(model(visitor(village(CAMP_NATION, true, 0)), DUTCH, 5))
                        .getPayment());
        final Model empty = model(
                withTreasury(visitor(village(CAMP_NATION, false, 0)), 0), DUTCH,
                5);
        final int gold = gold(empty, DUTCH);
        final NativeTributeResult result = demand(empty);
        assertTrue(result.isAccepted());
        assertEquals(0, result.getGold());
        assertEquals(gold, gold(empty, DUTCH));
        assertEquals(0, target(empty).getTreasury());
    }

    /**
     * A soldier aboard an adjacent caravel is a passenger, not on the map, so
     * it adds no force. The same soldier standing on the map adds its base.
     */
    @Test
    void aPassengerAboardAnAdjacentShipAddsNoForceButTheSameSoldierOnTheMapDoes() {
        final NativeTributeQuote aboard = quoteOf(
                model(passengerScene(true), DUTCH, 5));
        assertEquals(List.of(VISITOR_ID), aboard.getForceUnitIds());
        assertEquals(2.0, aboard.getForce(), 1e-9);
        final NativeTributeQuote onMap = quoteOf(
                model(passengerScene(false), DUTCH, 5));
        assertEquals(List.of(VISITOR_ID, PASSENGER_ID),
                onMap.getForceUnitIds());
        assertEquals(4.0, onMap.getForce(), 1e-9);
    }

    /**
     * The soldier of a passenger scene, aboard a caravel on the ocean square
     * north of the settlement, or standing on the map beside the settlement.
     */
    private static NativeTurnScene passengerScene(final boolean aboard) {
        NativeTurnScene scene = visitor(village(CAMP_NATION, false, 1)).sea(0,
                1);
        scene = unit(scene, UnitType.CARAVEL, CARAVEL_ID, DUTCH, 0, 1, UNARMED);
        scene = unit(scene, UnitType.COLONIST, PASSENGER_ID, DUTCH, -1, -1,
                SOLDIER);
        if (aboard) {
            final ModelPo po = scene.po();
            final UnitPo passenger = unitPo(po, PASSENGER_ID);
            passenger.setPlaceMap(null);
            passenger.setPlaceCargoSlot(new PlaceCargoSlotPo());
            final CargoSlotPo seat = new CargoSlotPo();
            seat.setUnitId(PASSENGER_ID);
            unitPo(po, CARAVEL_ID).getCargo().getSlots().add(seat);
        }
        return scene;
    }

    private static UnitPo unitPo(final ModelPo po, final int id) {
        return po.getUnits().stream().filter(unit -> unit.getId() == id)
                .findFirst().orElseThrow();
    }

    /** Seed whose first combat draw is below one half: the attacker wins. */
    private static long winningSeed() {
        for (long seed = 1;; seed++) {
            if (Model.combatRandomAfter(seed) < 0.5) {
                return seed;
            }
        }
    }

    /**
     * A conquered settlement takes its tribute record with it: the record lives
     * on the settlement, so the removed settlement keeps no history and the
     * remaining capital holds none of it.
     */
    @Test
    void conqueringTheSettlementRemovesItsTributeHistory() {
        final NativeTurnScene scene = visitor(village(CAMP_NATION, false, 1));
        unitPo(scene.po(), VISITOR_ID)
                .setMovementThirds(MovementPoints.ofPoints(1));
        scene.po().setCombatRandomState(winningSeed());
        final Model model = model(scene, DUTCH, 5);
        demand(model);
        assertEquals(Optional.of(5),
                target(model).findTributeTurn(EuropeanNation.DUTCH));
        final NativeConquestQuote terms = model
                .getNativeConquestQuote(visitor(model), target(model))
                .orElseThrow();
        final NativeConquestResult result = model
                .attackNativeSettlement(visitor(model), target(model), terms);
        assertTrue(result.isConquered());
        assertTrue(model.getNativeSettlements().stream()
                .noneMatch(settlement -> settlement.getId() == TARGET_ID));
        assertTrue(model.getNativeSettlements().stream()
                .allMatch(settlement -> settlement
                        .findTributeTurn(EuropeanNation.DUTCH).isEmpty()));
        final Model reloaded = reload(model, DUTCH, 5);
        assertTrue(reloaded.getNativeSettlements().stream()
                .noneMatch(settlement -> settlement.getId() == TARGET_ID));
    }

    @Test
    void aSavedDemandFromTheFutureOrAMalformedOneIsRejectedWhenTheModelIsBuilt() {
        final NativeTurnScene future = visitor(village(CAMP_NATION, false, 1));
        future.po().getCalendar().setNumberOfPlayedTurns(4);
        future.po().getNativeSettlements().get(0)
                .setTributes(List.of(record(EuropeanNation.DUTCH, 5)));
        assertThrows(IllegalArgumentException.class,
                () -> NativeTurnScene.make(future.po()));

        final NativeTurnScene negative = visitor(
                village(CAMP_NATION, false, 1));
        negative.po().getNativeSettlements().get(0)
                .setTributes(List.of(record(EuropeanNation.DUTCH, -1)));
        assertThrows(IllegalArgumentException.class,
                () -> NativeTurnScene.make(negative.po()));

        final NativeTurnScene duplicate = visitor(
                village(CAMP_NATION, false, 1));
        duplicate.po().getNativeSettlements().get(0)
                .setTributes(List.of(record(EuropeanNation.DUTCH, 2),
                        record(EuropeanNation.DUTCH, 3)));
        assertThrows(IllegalArgumentException.class,
                () -> NativeTurnScene.make(duplicate.po()));
    }

    private static NativeTributeRecordPo record(final EuropeanNation european,
            final int turn) {
        final NativeTributeRecordPo po = new NativeTributeRecordPo();
        po.setEuropean(european);
        po.setTurn(turn);
        return po;
    }

}
