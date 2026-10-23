package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.store.CargoPo;
import org.microcol.model.store.CargoSlotPo;
import org.microcol.model.store.EuropeanRelationPo;
import org.microcol.model.store.MercenaryDeliveryPo;
import org.microcol.model.store.MercenaryOfferPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.turnevent.TurnEventMercenaryArrival;
import org.microcol.model.unit.UnitActionNoAction;
import org.microcol.model.unit.UnitFreeColonist;

import com.google.gson.Gson;

/**
 * R-0169 stateful rules, driven by real turn updates of the authored Dutch
 * campaign. France, England and Spain are computer nations cloned from the
 * Dutch save and take their turns before the Dutch, so each Dutch update is a
 * real end of turn. The shared combat random state is the only draw source; its
 * value is set in the saved data, never through a production hook.
 *
 * <p>
 * Colony 0 (New Amsterdam) touches the sea only at (26,13) and (26,14); Delft
 * (colony 1) opens to the northern ocean. Crown ships on the approach squares
 * obstruct a port of the declared rebel, as in the intervention tests.
 * </p>
 */
class MercenaryOfferTest {

    private static final String DUTCH = RoyalScenarioFixtures.REBELS;

    private static final String CROWN = RoyalScenarioFixtures.KING;

    private static final long STEP = 0x9E3779B97F4A7C15L;

    private static final double CHANCE = 0.25;

    private static final int NEW_AMSTERDAM = 0;

    private static final int DELFT = 1;

    private static final Location NORTH_APPROACH = Location.of(26, 13);

    private static final Location SOUTH_APPROACH = Location.of(26, 14);

    private static final int BLOCKER = 200;

    private static final List<String> ALL_ISSUERS = List.of("Spain", "England",
            "France");

    // ------------------------------------------------------------------
    // Fixtures

    /**
     * The authored campaign with computer issuers, every pair at peace, and the
     * Dutch in contact with the named powers. The Dutch tax counter is reset so
     * that no Crown demand pauses an update of the test.
     */
    private static GameModelPo data(final boolean declared,
            final Difficulty difficulty, final List<String> issuers,
            final List<String> contacted) {
        final GameModelPo data = RoyalScenarioFixtures.crown(difficulty,
                declared);
        final ModelPo model = data.getModel();
        final PlayerPo dutch = player(model, DUTCH);
        if (dutch.getTax() != null) {
            dutch.getTax().setUpdates(0);
        }
        final Gson gson = new Gson();
        final List<String> colonizers = new ArrayList<>();
        colonizers.add(DUTCH);
        for (final String issuer : issuers) {
            final PlayerPo copy = gson.fromJson(gson.toJson(dutch),
                    PlayerPo.class);
            copy.setName(issuer);
            copy.setComputer(true);
            copy.setDeclaredIndependence(false);
            copy.setRebellionColonyIds(null);
            copy.setIntervention(null);
            copy.setKnownPowers(new ArrayList<>());
            copy.getColonySightings().clear();
            copy.getExtraData().clear();
            copy.setMercenaryTurns(null);
            copy.setMercenaryRebelUpdates(null);
            model.getGameManager().getPlayers().add(copy);
            colonizers.add(issuer);
        }
        for (int i = 0; i < colonizers.size(); i++) {
            for (int j = i + 1; j < colonizers.size(); j++) {
                final EuropeanRelationPo pair = new EuropeanRelationPo();
                pair.setFirstPlayer(colonizers.get(i));
                pair.setSecondPlayer(colonizers.get(j));
                pair.setState(EuropeanRelationState.PEACE);
                model.getEuropeanRelations().add(pair);
            }
        }
        dutch.setKnownPowers(new ArrayList<>(contacted));
        return data;
    }

    /** Peace-time Dutch with all three issuers contacted. */
    private static GameModelPo peacetime() {
        return data(false, Difficulty.CONQUISTADOR, ALL_ISSUERS, ALL_ISSUERS);
    }

    /** Declared Dutch with all three issuers contacted. */
    private static GameModelPo rebel() {
        return data(true, Difficulty.CONQUISTADOR, ALL_ISSUERS, ALL_ISSUERS);
    }

    /**
     * A declared Dutch whose next update is the tenth rebel update, with enough
     * gold for the bundle.
     */
    private static GameModelPo offerDue() {
        final GameModelPo data = rebel();
        player(data.getModel(), DUTCH).setMercenaryRebelUpdates(9);
        player(data.getModel(), DUTCH).setGold(5000);
        return data;
    }

    private static PlayerPo player(final ModelPo model, final String name) {
        return model.getGameManager().getPlayers().stream()
                .filter(player -> player.getName().equals(name)).findFirst()
                .orElseThrow();
    }

    private static EuropeanRelationPo pair(final ModelPo model,
            final String first, final String second) {
        return model.getEuropeanRelations().stream()
                .filter(row -> Set
                        .of(row.getFirstPlayer(), row.getSecondPlayer())
                        .equals(Set.of(first, second)))
                .findFirst().orElseThrow();
    }

    private static Model load(final ModelPo po) {
        return Model.make(po, MissionImpl.getPredefinedGameOverEvaluators());
    }

    private static Player dutch(final Model model) {
        return model.getPlayerByName(DUTCH);
    }

    /**
     * Load the saved game, continue it and play one complete Dutch update. The
     * saved game is already in the Dutch turn, and Continue only resumes that
     * turn without updating anything; the saved counters therefore describe the
     * state before the first update of the test, which is a real one.
     */
    private static Model start(final GameModelPo data) {
        final Model model = load(data.getModel());
        model.startGame();
        assertSame(dutch(model), model.getCurrentPlayer());
        nextUpdate(model);
        return model;
    }

    /** End turns of the other actors until the named one is current. */
    private static void toTurnOf(final Model model, final Player owner) {
        for (int guard = 0; model.getCurrentPlayer() != owner; guard++) {
            assertTrue(guard < 12, "The turn of " + owner + " never came");
            model.getCurrentPlayer().endTurn();
        }
    }

    /**
     * The Dutch end their turn and every other actor plays, so the next Dutch
     * turn begins with one complete owner update.
     */
    private static void nextUpdate(final Model model) {
        final Player dutch = dutch(model);
        assertSame(dutch, model.getCurrentPlayer());
        dutch.endTurn();
        toTurnOf(model, dutch);
    }

    private static UnitPo ship(final int id, final String owner,
            final UnitType type, final Location at) {
        final UnitPo unit = new UnitPo();
        unit.setId(id);
        unit.setType(type);
        unit.setOwnerId(owner);
        unit.setAvailableMoves(type.getSpeed());
        unit.setAction(new UnitActionNoAction().save());
        final PlaceMapPo place = new PlaceMapPo();
        place.setLocation(at);
        place.setOrientation(Direction.east);
        unit.setPlaceMap(place);
        final CargoPo cargo = new CargoPo();
        for (int slot = 0; slot < type.getCargoCapacity(); slot++) {
            cargo.getSlots().add(new CargoSlotPo());
        }
        unit.setCargo(cargo);
        return unit;
    }

    private static void block(final GameModelPo data, final int firstId,
            final Location... at) {
        for (int i = 0; i < at.length; i++) {
            data.getModel().getUnits()
                    .add(ship(firstId + i, CROWN, UnitType.CARAVEL, at[i]));
        }
    }

    /** Both approach squares of New Amsterdam hold a Crown ship. */
    private static void blockNewAmsterdam(final GameModelPo data) {
        block(data, BLOCKER, NORTH_APPROACH, SOUTH_APPROACH);
    }

    /**
     * Every sea square beside Delft holds a Crown ship. The squares are read
     * from a loaded copy of the same map, because the saved terrain map leaves
     * ordinary ocean out.
     */
    private static void blockDelft(final GameModelPo data) {
        final Model probe = load(rebel().getModel());
        final Location at = colony(probe, DELFT).getLocation();
        final Location[] sea = at.getNeighbors().stream()
                .filter(probe.getMap()::isValid)
                .filter(near -> probe.getMap().getTerrainTypeAt(near).isSea())
                .toArray(Location[]::new);
        assertTrue(sea.length > 0, "Delft must touch the sea");
        block(data, BLOCKER + 10, sea);
    }

    private static void removeBlockers(final ModelPo saved) {
        assertTrue(saved.getUnits().removeIf(unit -> unit.getId() >= BLOCKER
                && unit.getId() < BLOCKER + 100));
    }

    private static Colony colony(final Model model, final int id) {
        return model.getColonyById(id).orElseThrow();
    }

    private static Set<Integer> unitIds(final Model model) {
        return model.getAllUnits().stream().map(Unit::getId)
                .collect(Collectors.toSet());
    }

    private static List<Unit> created(final Model model,
            final Set<Integer> before) {
        return model.getAllUnits().stream()
                .filter(unit -> !before.contains(unit.getId()))
                .sorted((a, b) -> Integer.compare(a.getId(), b.getId()))
                .collect(Collectors.toList());
    }

    private static long randomState(final Model model) {
        return model.save().getCombatRandomState();
    }

    private static MercenaryOffer offer(final Model model) {
        return model.getPendingMercenaryOffer(dutch(model)).orElseThrow();
    }

    private static int arrivalNotices(final Model model) {
        return (int) model.getTurnEventsLocalizedMessages(dutch(model)).stream()
                .filter(TurnEventMercenaryArrival.class::isInstance).count();
    }

    /** Everything an answer could change, as one comparable text. */
    private static String fingerprint(final Model model) {
        final Player dutch = dutch(model);
        return dutch.getGold() + "|" + new java.util.TreeSet<>(unitIds(model))
                + "|" + dutch.getMercenaryDeliveries().size() + "|"
                + dutch.getMercenaryDeliveries().stream()
                        .map(group -> group.getPortColonyId() + ":"
                                + group.isDelivered())
                        .collect(Collectors.toList())
                + "|" + dutch.getMercenaryOffer().isPresent() + "|"
                + arrivalNotices(model);
    }

    /** Exact draws next to the 25% line, found in the shared random stream. */
    private static long stateJustBelowChance() {
        long best = -1;
        double bestDraw = -1;
        for (long state = 0; state < 20_000; state++) {
            final double draw = Model.combatRandomAfter(state);
            if (draw < CHANCE && draw > bestDraw) {
                bestDraw = draw;
                best = state;
            }
        }
        assertTrue(best >= 0);
        return best;
    }

    private static long stateJustAtOrAboveChance() {
        long best = -1;
        double bestDraw = 2;
        for (long state = 0; state < 20_000; state++) {
            final double draw = Model.combatRandomAfter(state);
            if (draw >= CHANCE && draw < bestDraw) {
                bestDraw = draw;
                best = state;
            }
        }
        assertTrue(best >= 0);
        return best;
    }

    private static void setDraw(final GameModelPo data, final long state) {
        data.getModel().setCombatRandomState(state);
    }

    // ------------------------------------------------------------------
    // Peace-time offer: timing, treasury, chance, issuer

    @Test
    void peacetimeNinthUpdateOpensNothingAndTenthOpensOneOfferWithOneDraw() {
        final GameModelPo data = peacetime();
        player(data.getModel(), DUTCH).setMercenaryTurns(8);
        player(data.getModel(), DUTCH).setGold(6000);
        final long state = stateJustBelowChance();
        setDraw(data, state);
        final Model model = start(data);
        final Player dutch = dutch(model);
        // Update nine: nothing is offered and nothing is drawn.
        assertEquals(9, dutch.save().getMercenaryTurns());
        assertTrue(dutch.getMercenaryOffer().isEmpty());
        assertEquals(state, randomState(model));
        // Update ten: exactly one draw, and the offer follows.
        nextUpdate(model);
        assertEquals(10, dutch.save().getMercenaryTurns());
        assertEquals(state + STEP, randomState(model));
        final MercenaryOffer offer = offer(model);
        assertSame(dutch, offer.getOwner());
        assertEquals(1000, offer.getPrice());
        assertEquals(2, offer.getSoldiers());
        // Update eleven draws nothing again.
        nextUpdate(model);
        assertEquals(11, dutch.save().getMercenaryTurns());
        assertTrue(dutch.getMercenaryOffer().isEmpty());
        assertEquals(state + STEP, randomState(model));
    }

    @Test
    void treasuryThresholdIsFiveThousandWithNoDrawBelowIt() {
        final GameModelPo poor = peacetime();
        player(poor.getModel(), DUTCH).setMercenaryTurns(9);
        player(poor.getModel(), DUTCH).setGold(4999);
        final long state = stateJustBelowChance();
        setDraw(poor, state);
        final Model below = start(poor);
        // Guard: no production in the first update changed the treasury.
        assertEquals(4999, dutch(below).getGold());
        assertEquals(10, dutch(below).save().getMercenaryTurns());
        assertTrue(dutch(below).getMercenaryOffer().isEmpty());
        assertEquals(state, randomState(below));

        final GameModelPo exact = peacetime();
        player(exact.getModel(), DUTCH).setMercenaryTurns(9);
        player(exact.getModel(), DUTCH).setGold(5000);
        setDraw(exact, state);
        final Model at = start(exact);
        assertEquals(5000, dutch(at).getGold());
        assertTrue(dutch(at).getMercenaryOffer().isPresent());
        assertEquals(state + STEP, randomState(at));
    }

    @Test
    void chanceIsTwentyFivePercentWithAStrictLessThanBoundary() {
        final long below = stateJustBelowChance();
        final long above = stateJustAtOrAboveChance();
        assertTrue(Model.combatRandomAfter(below) < CHANCE);
        assertTrue(Model.combatRandomAfter(above) >= CHANCE);

        final GameModelPo wins = peacetime();
        player(wins.getModel(), DUTCH).setMercenaryTurns(9);
        player(wins.getModel(), DUTCH).setGold(6000);
        setDraw(wins, below);
        final Model offered = start(wins);
        assertTrue(dutch(offered).getMercenaryOffer().isPresent());
        assertEquals(below + STEP, randomState(offered));

        final GameModelPo loses = peacetime();
        player(loses.getModel(), DUTCH).setMercenaryTurns(9);
        player(loses.getModel(), DUTCH).setGold(6000);
        setDraw(loses, above);
        final Model refused = start(loses);
        assertTrue(dutch(refused).getMercenaryOffer().isEmpty());
        // The draw was spent: the stream moved on by exactly one step.
        assertEquals(above + STEP, randomState(refused));
        assertEquals(10, dutch(refused).save().getMercenaryTurns());
    }

    @Test
    void issuerIsTheLowestIdContactedNationAtPeace() {
        // France is not contacted; England and Spain are. England is the
        // lower nation identity.
        final GameModelPo data = data(false, Difficulty.CONQUISTADOR,
                ALL_ISSUERS, List.of("Spain", "England"));
        player(data.getModel(), DUTCH).setMercenaryTurns(9);
        player(data.getModel(), DUTCH).setGold(6000);
        setDraw(data, stateJustBelowChance());
        final Model model = start(data);
        assertEquals("England", offer(model).getIssuer().getName());

        // England at war is no issuer; Spain follows although it is listed
        // first among the saved players.
        final GameModelPo war = data(false, Difficulty.CONQUISTADOR,
                ALL_ISSUERS, List.of("Spain", "England"));
        pair(war.getModel(), DUTCH, "England")
                .setState(EuropeanRelationState.WAR);
        player(war.getModel(), DUTCH).setMercenaryTurns(9);
        player(war.getModel(), DUTCH).setGold(6000);
        setDraw(war, stateJustBelowChance());
        assertEquals("Spain", offer(start(war)).getIssuer().getName());

        // A nation that is a lower identity but in alliance is not at peace.
        final GameModelPo allied = data(false, Difficulty.CONQUISTADOR,
                ALL_ISSUERS, ALL_ISSUERS);
        pair(allied.getModel(), DUTCH, "France")
                .setState(EuropeanRelationState.ALLIANCE);
        player(allied.getModel(), DUTCH).setMercenaryTurns(9);
        player(allied.getModel(), DUTCH).setGold(6000);
        setDraw(allied, stateJustBelowChance());
        assertEquals("England", offer(start(allied)).getIssuer().getName());
    }

    @Test
    void noEligibleIssuerSkipsTheIntervalWithoutADraw() {
        final GameModelPo data = data(false, Difficulty.CONQUISTADOR,
                ALL_ISSUERS, List.of());
        player(data.getModel(), DUTCH).setMercenaryTurns(9);
        player(data.getModel(), DUTCH).setGold(6000);
        final long state = stateJustBelowChance();
        setDraw(data, state);
        final Model model = start(data);
        assertEquals(10, dutch(model).save().getMercenaryTurns());
        assertTrue(dutch(model).getMercenaryOffer().isEmpty());
        assertEquals(state, randomState(model));
        // The next update is not an interval: the skipped one is not retried.
        nextUpdate(model);
        assertTrue(dutch(model).getMercenaryOffer().isEmpty());
        assertEquals(state, randomState(model));
    }

    // ------------------------------------------------------------------
    // Rebel offer: every tenth rebel update, no threshold, no draw, price

    @Test
    void rebelTenthUpdateOffersWithoutTreasuryThresholdOrDraw() {
        final GameModelPo data = rebel();
        player(data.getModel(), DUTCH).setMercenaryRebelUpdates(8);
        player(data.getModel(), DUTCH).setMercenaryTurns(3);
        player(data.getModel(), DUTCH).setGold(0);
        final long state = stateJustAtOrAboveChance();
        setDraw(data, state);
        final Model model = start(data);
        final Player dutch = dutch(model);
        assertEquals(9, dutch.save().getMercenaryRebelUpdates());
        assertTrue(dutch.getMercenaryOffer().isEmpty());
        nextUpdate(model);
        // The tenth rebel update offers although the draw would have failed,
        // and the historical peace-time count stays as it was.
        assertEquals(10, dutch.save().getMercenaryRebelUpdates());
        assertEquals(3, dutch.save().getMercenaryTurns());
        // All three are contacted and at peace: France is the lowest identity.
        assertEquals("France", offer(model).getIssuer().getName());
        assertEquals(state, randomState(model));
        // Update eleven offers nothing.
        nextUpdate(model);
        assertTrue(dutch.getMercenaryOffer().isEmpty());
        assertEquals(11, dutch.save().getMercenaryRebelUpdates());
    }

    @ParameterizedTest
    @CsvSource({ "DISCOVERER, 750", "EXPLORER, 900", "CONQUISTADOR, 1000",
            "GOVERNOR, 1150", "VICEROY, 1300" })
    void offerPriceIsTheCeilingOfThousandTimesTheDifficultyFactor(
            final Difficulty difficulty, final int price) {
        final GameModelPo data = data(true, difficulty, ALL_ISSUERS,
                ALL_ISSUERS);
        player(data.getModel(), DUTCH).setMercenaryRebelUpdates(9);
        final Model model = start(data);
        assertEquals(price, offer(model).getPrice());
    }

    @Test
    void computerNationsShareTheSameOfferState() {
        // France is the only other nation, so it takes the first turn.
        final GameModelPo data = data(false, Difficulty.CONQUISTADOR,
                List.of("France"), List.of("France"));
        // France is a computer nation that knows the Dutch and is rich.
        final PlayerPo france = player(data.getModel(), "France");
        france.setKnownPowers(new ArrayList<>(List.of(DUTCH)));
        france.setMercenaryTurns(9);
        france.setGold(6000);
        setDraw(data, stateJustBelowChance());
        final Model model = load(data.getModel());
        // The saved game is in the Dutch turn; Continue resumes it without an
        // update. The Dutch and their Crown then end their turns, and the new
        // round begins with a complete update of France.
        model.startGame();
        final Player france1 = model.getPlayerByName("France");
        assertSame(dutch(model), model.getCurrentPlayer());
        assertTrue(france1.getMercenaryOffer().isEmpty());
        toTurnOf(model, france1);
        // France's tenth own update offers the Dutch bundle through the same
        // rule, and the offer expires with its turn.
        assertEquals(10, france1.save().getMercenaryTurns());
        assertEquals(DUTCH, france1.getMercenaryOffer().orElseThrow()
                .getIssuer().getName());
        france1.endTurn();
        assertTrue(france1.getMercenaryOffer().isEmpty());
    }

    // ------------------------------------------------------------------
    // Answers: accept, decline, expiry, future offers

    @Test
    void acceptDebitsOnceAndPlacesTwoArmedVeteransAtTheChosenPort() {
        final Model model = start(offerDue());
        final Player dutch = dutch(model);
        final MercenaryOffer offer = offer(model);
        final Colony delft = colony(model, DELFT);
        assertTrue(model.getMercenaryDeliveryPorts(dutch).contains(delft));
        // The lowest-ID port is not the choice: the player chose Delft.
        assertEquals(NEW_AMSTERDAM,
                model.getMercenaryDeliveryPorts(dutch).get(0).getId());
        final Set<Integer> before = unitIds(model);
        final int gold = dutch.getGold();

        assertTrue(model.acceptMercenaryOffer(offer, delft));

        assertEquals(gold - offer.getPrice(), dutch.getGold());
        final List<Unit> soldiers = created(model, before);
        assertEquals(2, soldiers.size());
        assertEquals(soldiers.get(0).getId() + 1, soldiers.get(1).getId());
        for (final Unit unit : soldiers) {
            final UnitFreeColonist soldier = (UnitFreeColonist) unit;
            assertSame(dutch, soldier.getOwner());
            assertEquals(UnitType.VETERAN_SOLDIER, soldier.getType());
            assertTrue(soldier.isVeteran());
            assertTrue(soldier.isHoldingGuns());
            // Paid mercenaries are not Continentals of the intervention.
            assertEquals(MilitaryRole.COLONIAL, soldier.getMilitaryRole());
            assertFalse(soldier.isInterventionDelivered());
            assertEquals(delft.getLocation(), soldier.getLocation());
        }
        assertEquals(1, dutch.getMercenaryDeliveries().size());
        assertTrue(dutch.getMercenaryDeliveries().get(0).isDelivered());
        assertEquals(1, arrivalNotices(model));
        assertTrue(model.getPendingMercenaryOffer(dutch).isEmpty());
        // The same offer cannot be paid twice.
        final String after = fingerprint(model);
        assertThrows(IllegalStateException.class,
                () -> model.acceptMercenaryOffer(offer, delft));
        assertEquals(after, fingerprint(model));
    }

    @Test
    void declineChargesNothingAndALaterOfferIsStillMade() {
        final Model model = start(offerDue());
        final Player dutch = dutch(model);
        final MercenaryOffer offer = offer(model);
        final String before = fingerprint(model);

        model.declineMercenaryOffer(offer);

        assertTrue(model.getPendingMercenaryOffer(dutch).isEmpty());
        assertEquals(before.replace("|true|", "|false|"), fingerprint(model));
        assertThrows(IllegalStateException.class,
                () -> model.declineMercenaryOffer(offer));
        assertThrows(IllegalStateException.class, () -> model
                .acceptMercenaryOffer(offer, colony(model, NEW_AMSTERDAM)));

        // Ten rebel updates later (the saved count is moved to nine updates
        // before) the nation offers again: a refusal bars nothing.
        final ModelPo saved = model.save();
        player(saved, DUTCH).setMercenaryRebelUpdates(19);
        final Model later = load(saved);
        later.startGame();
        // Continue alone opens nothing; only the next real update does.
        assertEquals(19, dutch(later).save().getMercenaryRebelUpdates());
        assertTrue(later.getPendingMercenaryOffer(dutch(later)).isEmpty());
        nextUpdate(later);
        assertEquals(20, dutch(later).save().getMercenaryRebelUpdates());
        assertTrue(later.getPendingMercenaryOffer(dutch(later)).isPresent());
    }

    @Test
    void unansweredOfferExpiresWithTheTurnAndChargesNothing() {
        final Model model = start(offerDue());
        final Player dutch = dutch(model);
        final int gold = dutch.getGold();
        final Set<Integer> units = unitIds(model);
        assertTrue(dutch.getMercenaryOffer().isPresent());

        nextUpdate(model);

        assertTrue(dutch.getMercenaryOffer().isEmpty());
        assertEquals(gold, dutch.getGold());
        assertEquals(units, unitIds(model));
        assertTrue(dutch.getMercenaryDeliveries().isEmpty());
    }

    @Test
    void closingTheDialogKeepsTheSameOfferAvailableInTheSameTurn() {
        final Model model = start(offerDue());
        final MercenaryOffer first = offer(model);
        // Reading the offer again, as the review button does, changes nothing
        // and returns the very same offer.
        final String before = fingerprint(model);
        assertSame(first, offer(model));
        assertEquals(before, fingerprint(model));
        assertTrue(model.acceptMercenaryOffer(first,
                colony(model, NEW_AMSTERDAM)));
    }

    // ------------------------------------------------------------------
    // Rejections that change nothing

    @Test
    void insufficientFundsChangeNothing() {
        final Model model = start(offerDue());
        final Player dutch = dutch(model);
        final MercenaryOffer offer = offer(model);
        dutch.setGold(offer.getPrice() - 1);
        final String before = fingerprint(model);

        assertThrows(IllegalStateException.class, () -> model
                .acceptMercenaryOffer(offer, colony(model, NEW_AMSTERDAM)));

        assertEquals(before, fingerprint(model));
        assertSame(offer, offer(model));
        // Exactly the price is enough.
        dutch.setGold(offer.getPrice());
        assertTrue(model.acceptMercenaryOffer(offer,
                colony(model, NEW_AMSTERDAM)));
        assertEquals(0, dutch.getGold());
    }

    @Test
    void issuerThatWentToWarMakesTheOfferStale() {
        final Model model = start(offerDue());
        final Player dutch = dutch(model);
        final MercenaryOffer offer = offer(model);
        assertEquals("France", offer.getIssuer().getName());
        model.declareHostilities(dutch, offer.getIssuer());
        final String before = fingerprint(model);

        // The next issuer is another nation, so the quoted terms changed.
        assertThrows(IllegalStateException.class, () -> model
                .acceptMercenaryOffer(offer, colony(model, NEW_AMSTERDAM)));

        assertEquals(before, fingerprint(model));
    }

    @Test
    void portCapturedAfterTheOfferIsRejectedBeforeAnyChange() {
        final Model model = start(offerDue());
        final Player dutch = dutch(model);
        final MercenaryOffer offer = offer(model);
        final Colony delft = colony(model, DELFT);
        capture(model, delft);
        assertFalse(model.getMercenaryDeliveryPorts(dutch).contains(delft));
        final String before = fingerprint(model);

        assertThrows(IllegalStateException.class,
                () -> model.acceptMercenaryOffer(offer, delft));

        assertEquals(before, fingerprint(model));
        // An inland or foreign colony is no delivery port either.
        assertThrows(IllegalStateException.class,
                () -> model.acceptMercenaryOffer(offer, delft));
    }

    @Test
    void foreignOrStaleOwnerIsRejected() {
        final Model model = start(offerDue());
        final MercenaryOffer offer = offer(model);
        final Model other = start(offerDue());
        final String before = fingerprint(model);
        // A port of another game is not an owned delivery port here.
        assertThrows(IllegalStateException.class, () -> model
                .acceptMercenaryOffer(offer, colony(other, NEW_AMSTERDAM)));
        // An offer from the other game is not this game's pending offer.
        assertThrows(IllegalStateException.class, () -> model
                .acceptMercenaryOffer(offer(other), colony(model, DELFT)));
        assertEquals(before, fingerprint(model));
    }

    @Test
    void oneFreeUnitIdentityIsNotEnoughButTwoAre() {
        final GameModelPo tooFew = offerDue();
        tooFew.getModel().getUnits().add(ship(Integer.MAX_VALUE - 2, DUTCH,
                UnitType.CARAVEL, Location.of(35, 10)));
        final Model few = start(tooFew);
        final MercenaryOffer offer = offer(few);
        final String before = fingerprint(few);
        assertThrows(IllegalStateException.class, () -> few
                .acceptMercenaryOffer(offer, colony(few, NEW_AMSTERDAM)));
        assertEquals(before, fingerprint(few));

        final GameModelPo exact = offerDue();
        exact.getModel().getUnits().add(ship(Integer.MAX_VALUE - 3, DUTCH,
                UnitType.CARAVEL, Location.of(35, 10)));
        final Model enough = start(exact);
        final Set<Integer> known = unitIds(enough);
        assertTrue(enough.acceptMercenaryOffer(offer(enough),
                colony(enough, NEW_AMSTERDAM)));
        final Set<Integer> ids = created(enough, known).stream()
                .map(Unit::getId).collect(Collectors.toSet());
        assertEquals(Set.of(Integer.MAX_VALUE - 2, Integer.MAX_VALUE - 1), ids);
    }

    // ------------------------------------------------------------------
    // Blockade: paid and pending, retry once, captured port

    @Test
    void blockedPaidGroupWaitsAndTheRetryDeliversExactlyTwoOnce() {
        final GameModelPo data = offerDue();
        blockNewAmsterdam(data);
        final Model model = start(data);
        final Player dutch = dutch(model);
        final MercenaryOffer offer = offer(model);
        final Colony port = colony(model, NEW_AMSTERDAM);
        assertTrue(model.isMercenaryPortBlocked(dutch, port));
        // The blocked port stays in the list: the player may choose it.
        assertTrue(model.getMercenaryDeliveryPorts(dutch).contains(port));
        final Set<Integer> before = unitIds(model);
        final int gold = dutch.getGold();

        assertFalse(model.acceptMercenaryOffer(offer, port));

        // Paid once, nothing placed, no notice, and the group is on record.
        assertEquals(gold - offer.getPrice(), dutch.getGold());
        assertEquals(before, unitIds(model));
        assertEquals(1, dutch.getMercenaryDeliveries().size());
        assertFalse(dutch.getMercenaryDeliveries().get(0).isDelivered());
        assertEquals(0, arrivalNotices(model));

        // Later updates retry while the route is closed and change nothing.
        nextUpdate(model);
        assertEquals(gold - offer.getPrice(), dutch.getGold());
        assertEquals(before, unitIds(model));
        assertEquals(0, arrivalNotices(model));

        // The pending group survives Save and Continue, still unpaid twice.
        final ModelPo saved = model.save();
        assertEquals(1, player(saved, DUTCH).getMercenaryDeliveries().size());
        assertFalse(player(saved, DUTCH).getMercenaryDeliveries().get(0)
                .getDelivered());
        removeBlockers(saved);
        final Model open = load(saved);
        open.startGame();
        final Set<Integer> beforeOpen = unitIds(open);
        assertEquals(gold - offer.getPrice(), dutch(open).getGold());
        // Loading itself delivers nothing.
        assertEquals(beforeOpen, unitIds(open));

        nextUpdate(open);

        final List<Unit> soldiers = created(open, beforeOpen).stream()
                .filter(unit -> unit.getType() == UnitType.VETERAN_SOLDIER)
                .collect(Collectors.toList());
        assertEquals(2, soldiers.size());
        for (final Unit soldier : soldiers) {
            assertEquals(colony(open, NEW_AMSTERDAM).getLocation(),
                    soldier.getLocation());
        }
        assertEquals(1, arrivalNotices(open));
        assertEquals(gold - offer.getPrice(), dutch(open).getGold());
        assertTrue(dutch(open).getMercenaryDeliveries().get(0).isDelivered());

        // Neither another update nor a reload places or charges anything.
        final Set<Integer> placed = unitIds(open);
        nextUpdate(open);
        assertEquals(placed, unitIds(open));
        final Model restored = load(open.save());
        restored.startGame();
        assertEquals(placed, unitIds(restored));
        nextUpdate(restored);
        assertEquals(placed, unitIds(restored));
        assertEquals(gold - offer.getPrice(), dutch(restored).getGold());
        assertEquals(1, dutch(restored).getMercenaryDeliveries().size());
    }

    @Test
    void capturedChosenPortMovesThePendingGroupToTheLowestIdPort() {
        // New Amsterdam is blocked and chosen; the Crown then takes it.
        final GameModelPo data = offerDue();
        blockNewAmsterdam(data);
        final Model model = start(data);
        final Player dutch = dutch(model);
        final MercenaryOffer offer = offer(model);
        assertFalse(model.acceptMercenaryOffer(offer,
                colony(model, NEW_AMSTERDAM)));
        final int paid = dutch.getGold();
        capture(model, colony(model, NEW_AMSTERDAM));
        final Set<Integer> before = unitIds(model);

        nextUpdate(model);

        // The group is neither lost nor paid again: the lowest-ID reachable
        // owned port, Delft, receives it.
        final List<Unit> soldiers = created(model, before).stream()
                .filter(unit -> unit.getType() == UnitType.VETERAN_SOLDIER)
                .collect(Collectors.toList());
        assertEquals(2, soldiers.size());
        for (final Unit soldier : soldiers) {
            assertEquals(colony(model, DELFT).getLocation(),
                    soldier.getLocation());
        }
        assertEquals(paid, dutch.getGold());
        assertTrue(dutch.getMercenaryDeliveries().get(0).isDelivered());
    }

    @Test
    void blockedLowestFallbackKeepsThePendingGroupWithoutSkippingToAFreePort() {
        // Both Delft and New Amsterdam are obstructed. The player chose
        // Delft, which the Crown then captures; the geographic replacement is
        // New Amsterdam, which is still blocked, so the group waits instead of
        // going to a later free port.
        final GameModelPo data = offerDue();
        blockNewAmsterdam(data);
        blockDelft(data);
        final Model model = start(data);
        final Player dutch = dutch(model);
        final MercenaryOffer offer = offer(model);
        assertFalse(model.acceptMercenaryOffer(offer, colony(model, DELFT)));
        final int paid = dutch.getGold();
        capture(model, colony(model, DELFT));
        final Set<Integer> before = unitIds(model);

        nextUpdate(model);

        assertEquals(before, unitIds(model));
        assertFalse(dutch.getMercenaryDeliveries().get(0).isDelivered());
        assertEquals(paid, dutch.getGold());
        assertEquals(0, arrivalNotices(model));

        // Once New Amsterdam opens, exactly that port receives the group.
        final ModelPo saved = model.save();
        removeBlockers(saved);
        final Model open = load(saved);
        open.startGame();
        final Set<Integer> beforeOpen = unitIds(open);
        nextUpdate(open);
        final List<Unit> soldiers = created(open, beforeOpen).stream()
                .filter(unit -> unit.getType() == UnitType.VETERAN_SOLDIER)
                .collect(Collectors.toList());
        assertEquals(2, soldiers.size());
        for (final Unit soldier : soldiers) {
            assertEquals(colony(open, NEW_AMSTERDAM).getLocation(),
                    soldier.getLocation());
        }
    }

    // ------------------------------------------------------------------
    // Save and Continue

    @Test
    void savedOfferReloadsWithoutANewDrawDebitOrDelivery() {
        final GameModelPo data = peacetime();
        player(data.getModel(), DUTCH).setMercenaryTurns(9);
        player(data.getModel(), DUTCH).setGold(6000);
        final long state = stateJustBelowChance();
        setDraw(data, state);
        final Model model = start(data);
        final MercenaryOffer before = offer(model);
        final String fingerprint = fingerprint(model);
        assertEquals(state + STEP, randomState(model));

        final Model loaded = load(model.save());
        loaded.startGame();

        final MercenaryOffer after = offer(loaded);
        assertEquals(before.getIssuer().getName(), after.getIssuer().getName());
        assertEquals(before.getPrice(), after.getPrice());
        assertEquals(fingerprint, fingerprint(loaded));
        assertEquals(state + STEP, randomState(loaded));
        assertEquals(10, dutch(loaded).save().getMercenaryTurns());
        // The reloaded offer is the one that can be accepted, once.
        assertTrue(loaded.acceptMercenaryOffer(after,
                colony(loaded, NEW_AMSTERDAM)));
        assertThrows(IllegalStateException.class, () -> loaded
                .acceptMercenaryOffer(after, colony(loaded, NEW_AMSTERDAM)));
    }

    @Test
    void savedDeliveredGroupDoesNotRepeatAfterReload() {
        final Model model = start(offerDue());
        assertTrue(model.acceptMercenaryOffer(offer(model),
                colony(model, NEW_AMSTERDAM)));
        final Set<Integer> units = unitIds(model);
        final int gold = dutch(model).getGold();

        final Model loaded = load(model.save());
        loaded.startGame();
        assertEquals(units, unitIds(loaded));
        assertTrue(dutch(loaded).getMercenaryDeliveries().get(0).isDelivered());
        nextUpdate(loaded);
        assertEquals(units, unitIds(loaded));
        assertEquals(gold, dutch(loaded).getGold());
        assertEquals(1, dutch(loaded).getMercenaryDeliveries().size());
    }

    @Test
    void invalidSavedOfferAndGroupAreRejectedAtLoad() {
        final GameModelPo unknown = offerDue();
        player(unknown.getModel(), DUTCH)
                .setMercenaryOffer(offerPo("Nobody", 1000));
        assertThrows(IllegalArgumentException.class,
                () -> load(unknown.getModel()));

        final GameModelPo self = offerDue();
        player(self.getModel(), DUTCH).setMercenaryOffer(offerPo(DUTCH, 1000));
        assertThrows(IllegalArgumentException.class,
                () -> load(self.getModel()));

        final GameModelPo cheap = offerDue();
        player(cheap.getModel(), DUTCH)
                .setMercenaryOffer(offerPo("England", 1));
        assertThrows(IllegalArgumentException.class,
                () -> load(cheap.getModel()));

        final GameModelPo incomplete = offerDue();
        player(incomplete.getModel(), DUTCH)
                .setMercenaryOffer(offerPo(null, 1000));
        assertThrows(IllegalArgumentException.class,
                () -> load(incomplete.getModel()));

        final GameModelPo group = offerDue();
        player(group.getModel(), DUTCH).getMercenaryDeliveries()
                .add(new MercenaryDeliveryPo());
        assertThrows(IllegalArgumentException.class,
                () -> load(group.getModel()));

        final GameModelPo negative = offerDue();
        player(negative.getModel(), DUTCH).getMercenaryDeliveries()
                .add(groupPo(-1, 1000, false));
        assertThrows(IllegalArgumentException.class,
                () -> load(negative.getModel()));

        // A valid saved offer and group do load.
        final GameModelPo valid = offerDue();
        player(valid.getModel(), DUTCH)
                .setMercenaryOffer(offerPo("England", 1000));
        player(valid.getModel(), DUTCH).getMercenaryDeliveries()
                .add(groupPo(NEW_AMSTERDAM, 1000, true));
        assertNotNull(load(valid.getModel()));
    }

    private static MercenaryOfferPo offerPo(final String issuer,
            final Integer price) {
        final MercenaryOfferPo po = new MercenaryOfferPo();
        po.setIssuer(issuer);
        po.setPrice(price);
        return po;
    }

    private static MercenaryDeliveryPo groupPo(final Integer port,
            final Integer price, final Boolean delivered) {
        final MercenaryDeliveryPo po = new MercenaryDeliveryPo();
        po.setPortColonyId(port);
        po.setPrice(price);
        po.setDelivered(delivered);
        return po;
    }

    // ------------------------------------------------------------------
    // End states

    @Test
    void endedRebellionOffersNothingAndDeliversNothing() {
        final GameModelPo data = rebel();
        player(data.getModel(), DUTCH).setMercenaryRebelUpdates(9);
        player(data.getModel(), DUTCH).getMercenaryDeliveries()
                .add(groupPo(NEW_AMSTERDAM, 1000, false));
        // Every colony the rebellion held is Crown-held: the rebellion is lost.
        final int[] members = RebellionFixtures
                .ownedColonyIds(data.getModel(), DUTCH).stream()
                .mapToInt(Integer::intValue).toArray();
        RebellionFixtures.transfer(data.getModel(), CROWN, members);
        final Model model = load(data.getModel());
        final Player dutch = dutch(model);
        final Set<Integer> before = unitIds(model);

        assertTrue(dutch.startTurn());

        assertTrue(dutch.getMercenaryOffer().isEmpty());
        assertEquals(before, unitIds(model));
        assertFalse(dutch.getMercenaryDeliveries().get(0).isDelivered());
        // The ended rebellion did not even count the update.
        assertEquals(9, dutch.save().getMercenaryRebelUpdates());
    }

    // ------------------------------------------------------------------

    /** The Crown takes a colony only through the real defense battle. */
    private static void capture(final Model model, final Colony colony) {
        final Player crown = model.getPlayerByName(CROWN);
        final Unit invader = model.addUnitOutSideColony(colony);
        invader.takeOver(crown);
        ColonyBattles.capture(model, colony, invader);
        assertSame(crown, colony.getOwner());
    }
}
