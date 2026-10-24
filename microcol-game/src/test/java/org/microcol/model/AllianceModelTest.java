package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.CargoPo;
import org.microcol.model.store.CargoSlotPo;
import org.microcol.model.store.EuropeanRelationPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.turnevent.TurnEvent;
import org.microcol.model.turnevent.TurnEventDiplomacy;
import org.microcol.model.unit.UnitActionNoAction;

import com.google.gson.Gson;

/**
 * Explicit alliances: proposal, acceptance, termination, narrow passage, mutual
 * defense and the Crown-war event.
 */
class AllianceModelTest {
    private static final Location CONTACT = Location.of(18, 10);
    private static final Location COLONY = Location.of(17, 10);
    private static final Location TARGET = Location.of(44, 15);

    private final Gson gson = new Gson();

    private ModelPo data(final String current, final String... extra) {
        final ModelPo data = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.microcol").getModel();
        data.getGameManager().setGameStarted(true);
        data.getGameManager().setCurrentPlayer(current);
        final List<PlayerPo> players = data.getGameManager().getPlayers();
        final PlayerPo template = players.stream()
                .filter(player -> player.getName().equals("France")).findFirst()
                .orElseThrow();
        final List<String> nations = new ArrayList<>(
                List.of("Dutch", "France"));
        for (final String name : extra) {
            final PlayerPo clone = gson.fromJson(gson.toJson(template),
                    PlayerPo.class);
            clone.setName(name);
            players.add(clone);
            nations.add(name);
        }
        for (int first = 0; first < nations.size(); first++) {
            for (int second = first + 1; second < nations.size(); second++) {
                if (find(data, nations.get(first),
                        nations.get(second)) == null) {
                    final EuropeanRelationPo row = new EuropeanRelationPo();
                    row.setFirstPlayer(nations.get(first));
                    row.setSecondPlayer(nations.get(second));
                    row.setState(EuropeanRelationState.PEACE);
                    data.getEuropeanRelations().add(row);
                }
            }
        }
        return data;
    }

    private EuropeanRelationPo find(final ModelPo data, final String first,
            final String second) {
        return data.getEuropeanRelations().stream()
                .filter(row -> Set
                        .of(row.getFirstPlayer(), row.getSecondPlayer())
                        .equals(Set.of(first, second)))
                .findFirst().orElse(null);
    }

    private EuropeanRelationPo row(final ModelPo data, final String first,
            final String second) {
        return java.util.Objects.requireNonNull(find(data, first, second));
    }

    private void relate(final ModelPo data, final String first,
            final String second, final EuropeanRelationState state) {
        find(data, first, second).setState(state);
    }

    private Model make(final ModelPo data) {
        return Model.make(data, MissionImpl.getPredefinedGameOverEvaluators());
    }

    /** Dutch colonist 0 stands next to the visible French colony. */
    private Model contact(final ModelPo data) {
        data.getUnits().stream().filter(unit -> unit.getId() == 0).findFirst()
                .orElseThrow().setAvailableMoves(1);
        final Model model = make(data);
        final Unit visitor = model.getUnitById(0);
        visitor.placeToLocation(CONTACT);
        visitor.getOwner().revealMapForUnit(visitor);
        model.getPlayerByName("Dutch").setGold(500);
        model.getPlayerByName("France").setGold(1000);
        return model;
    }

    private String snapshot(final Model model) {
        return gson.toJson(model.save());
    }

    private Player player(final Model model, final String name) {
        return model.getPlayerByName(name);
    }

    @Test
    void eligibleContactProposalAndAcceptanceChargeExactlyOnce() {
        final Model model = contact(data("Dutch"));
        final Player dutch = player(model, "Dutch");
        final Player france = player(model, "France");
        assertEquals(List.of(model.getUnitById(0)),
                model.getAllianceVisitors(dutch, france));
        assertEquals(0, model.getAllianceBlockedRounds(dutch, france));
        final String sightBefore = gson.toJson(dutchPlayer(model).getVisible());
        final AllianceOffer offer = model
                .prepareAllianceOffer(model.getUnitById(0), france);
        assertEquals(200, offer.getPrice());
        assertEquals(300, offer.getActorGoldAfter());
        assertEquals(1200, offer.getRivalGoldAfter());
        assertSame(offer,
                model.prepareAllianceOffer(model.getUnitById(0), france));
        assertEquals(500, dutch.getGold());
        model.acceptAllianceOffer(offer);
        assertEquals(EuropeanRelationState.ALLIANCE,
                model.getEuropeanRelation(dutch, france));
        assertEquals(EuropeanRelationState.ALLIANCE,
                model.getEuropeanRelation(france, dutch));
        assertTrue(model.areAllied(dutch, france));
        assertFalse(model.areHostile(dutch, france));
        assertEquals(300, dutch.getGold());
        assertEquals(1200, france.getGold());
        assertTrue(model.getPendingAllianceOffer(dutch, france).isEmpty());
        // An alliance grants no map knowledge.
        assertEquals(sightBefore, gson.toJson(dutchPlayer(model).getVisible()));
        final String before = snapshot(model);
        assertThrows(IllegalStateException.class,
                () -> model.acceptAllianceOffer(offer));
        assertEquals(before, snapshot(model));
    }

    private PlayerPo dutchPlayer(final Model model) {
        return model.save().getGameManager().getPlayers().stream()
                .filter(po -> po.getName().equals("Dutch")).findFirst()
                .orElseThrow();
    }

    @Test
    void cancelAndRefusalLeavePeaceAndChargeNothing() {
        final Model model = contact(data("Dutch"));
        final Player dutch = player(model, "Dutch");
        final Player france = player(model, "France");
        final AllianceOffer offer = model
                .prepareAllianceOffer(model.getUnitById(0), france);
        model.cancelAllianceOffer(offer);
        assertEquals(EuropeanRelationState.PEACE,
                model.getEuropeanRelation(dutch, france));
        assertEquals(500, dutch.getGold());
        assertEquals(1000, france.getGold());
        assertTrue(model.getPendingAllianceOffer(dutch, france).isEmpty());
        final String before = snapshot(model);
        assertThrows(IllegalStateException.class,
                () -> model.cancelAllianceOffer(offer));
        assertThrows(IllegalStateException.class,
                () -> model.acceptAllianceOffer(offer));
        assertEquals(before, snapshot(model));
    }

    @Test
    void staleContactChangedTermsAndPovertyRejectAcceptanceWithoutPayment() {
        // Lost contact rejects acceptance without payment.
        final Model lost = contact(data("Dutch"));
        final AllianceOffer first = lost.prepareAllianceOffer(
                lost.getUnitById(0), player(lost, "France"));
        lost.getUnitById(0).placeToLocation(Location.of(24, 16));
        final String lostBefore = snapshot(lost);
        assertThrows(IllegalStateException.class,
                () -> lost.acceptAllianceOffer(first));
        assertEquals(lostBefore, snapshot(lost));
        // A changed treasury changes the frozen terms.
        final Model changed = contact(data("Dutch"));
        final AllianceOffer second = changed.prepareAllianceOffer(
                changed.getUnitById(0), player(changed, "France"));
        player(changed, "France").setGold(1001);
        final String changedBefore = snapshot(changed);
        assertThrows(IllegalStateException.class,
                () -> changed.acceptAllianceOffer(second));
        assertEquals(changedBefore, snapshot(changed));
        // A poor treasury can inspect but not accept.
        final Model poor = contact(data("Dutch"));
        player(poor, "Dutch").setGold(199);
        final AllianceOffer third = poor.prepareAllianceOffer(
                poor.getUnitById(0), player(poor, "France"));
        assertEquals(-1, third.getActorGoldAfter());
        final String poorBefore = snapshot(poor);
        assertThrows(IllegalStateException.class,
                () -> poor.acceptAllianceOffer(third));
        assertEquals(poorBefore, snapshot(poor));
        // A war declaration retires a pending quote.
        final Model declared = contact(data("Dutch"));
        declared.prepareAllianceOffer(declared.getUnitById(0),
                player(declared, "France"));
        declared.declareHostilities(player(declared, "Dutch"),
                player(declared, "France"));
        assertTrue(declared.getPendingAllianceOffer(player(declared, "Dutch"),
                player(declared, "France")).isEmpty());
        assertTrue(declared.getAllianceVisitors(player(declared, "Dutch"),
                player(declared, "France")).isEmpty());
    }

    @Test
    void wrongTurnOrNonAiRivalRejectsProposalBeforeMutation() {
        final Model model = contact(data("Dutch"));
        final Player dutch = player(model, "Dutch");
        final Player france = player(model, "France");
        final String before = snapshot(model);
        assertTrue(model.getAllianceVisitors(france, dutch).isEmpty());
        assertThrows(IllegalStateException.class,
                () -> model.prepareAllianceOffer(model.getUnitById(0), dutch));
        assertTrue(
                model.getAllianceVisitors(dutch, player(model, "Dutch's King"))
                        .isEmpty());
        assertEquals(before, snapshot(model));
    }

    @Test
    void savedProposalSurvivesReloadWithoutAcceptanceReplay() {
        final Model model = contact(data("Dutch"));
        model.prepareAllianceOffer(model.getUnitById(0),
                player(model, "France"));
        final ModelPo saved = model.save();
        final Model restored = make(saved);
        final Player dutch = player(restored, "Dutch");
        final Player france = player(restored, "France");
        final AllianceOffer offer = restored
                .getPendingAllianceOffer(dutch, france).orElseThrow();
        assertEquals(500, dutch.getGold());
        assertEquals(1000, france.getGold());
        assertEquals(EuropeanRelationState.PEACE,
                restored.getEuropeanRelation(dutch, france));
        // Continue does not accept; the same saved proposal is accepted once.
        restored.startGame();
        assertEquals(500, dutch.getGold());
        restored.acceptAllianceOffer(offer);
        final Model allied = make(restored.save());
        allied.startGame();
        assertEquals(300, player(allied, "Dutch").getGold());
        assertEquals(1200, player(allied, "France").getGold());
        assertTrue(allied.areAllied(player(allied, "Dutch"),
                player(allied, "France")));
        assertTrue(allied.getPendingAllianceOffer(player(allied, "Dutch"),
                player(allied, "France")).isEmpty());
    }

    @Test
    void malformedSavedAllianceDataIsRejected() {
        final Model model = contact(data("Dutch"));
        model.prepareAllianceOffer(model.getUnitById(0),
                player(model, "France"));
        final ModelPo wrongState = model.save();
        wrongState.getEuropeanRelations().get(0)
                .setState(EuropeanRelationState.WAR);
        assertThrows(IllegalArgumentException.class, () -> make(wrongState));
        final ModelPo noPrice = model.save();
        noPrice.getEuropeanRelations().get(0).getAllianceOffer().setPrice(null);
        assertThrows(IllegalArgumentException.class, () -> make(noPrice));
        final ModelPo future = model.save();
        future.getEuropeanRelations().get(0).setLastAggressionRound(5);
        assertThrows(IllegalArgumentException.class, () -> make(future));
        final ModelPo ended = model.save();
        ended.getEuropeanRelations().get(0)
                .setState(EuropeanRelationState.ALLIANCE);
        ended.getEuropeanRelations().get(0).setAllianceOffer(null);
        ended.getEuropeanRelations().get(0).setAllianceEndedRound(0);
        assertThrows(IllegalArgumentException.class, () -> make(ended));
        final ModelPo round = model.save();
        round.setCrownWarRound(20);
        assertThrows(IllegalArgumentException.class, () -> make(round));
    }

    @Test
    void recentTributeOrActualAttackBlocksTheProposalForTenRounds() {
        for (final boolean tribute : List.of(false, true)) {
            for (final int[] expected : new int[][] { { 12, 1 }, { 13, 0 } }) {
                final ModelPo data = data("Dutch");
                data.getCalendar().setNumberOfPlayedTurns(expected[0]);
                if (tribute) {
                    data.getEuropeanRelations().get(0).setLastTributeRound(3);
                } else {
                    data.getEuropeanRelations().get(0)
                            .setLastAggressionRound(3);
                }
                final Model model = contact(data);
                final Player dutch = player(model, "Dutch");
                final Player france = player(model, "France");
                assertEquals(expected[1],
                        model.getAllianceBlockedRounds(dutch, france));
                assertEquals(expected[1] == 0,
                        !model.getAllianceVisitors(dutch, france).isEmpty());
                if (expected[1] > 0) {
                    final String before = snapshot(model);
                    assertThrows(IllegalStateException.class,
                            () -> model.prepareAllianceOffer(
                                    model.getUnitById(0), france));
                    assertEquals(before, snapshot(model));
                }
            }
        }
    }

    @Test
    void warDeclarationAloneIsNotRecentAggression() {
        final Model model = contact(data("Dutch"));
        final Player dutch = player(model, "Dutch");
        final Player france = player(model, "France");
        model.declareHostilities(dutch, france);
        model.acceptPeace(dutch, france);
        assertEquals(0, model.getAllianceBlockedRounds(dutch, france));
        assertFalse(model.getAllianceVisitors(dutch, france).isEmpty());
    }

    @Test
    void terminationReturnsToPeaceAndForbidsWarOrAttackForFiveRounds() {
        final ModelPo data = data("Dutch");
        relate(data, "Dutch", "France", EuropeanRelationState.ALLIANCE);
        final Model model = contact(data);
        final Player dutch = player(model, "Dutch");
        final Player france = player(model, "France");
        // Only the current player may end it, and a declaration cannot skip it.
        assertThrows(IllegalArgumentException.class,
                () -> model.declareHostilities(dutch, france));
        final String before = snapshot(model);
        assertThrows(IllegalStateException.class,
                () -> model.terminateAlliance(france, dutch));
        assertEquals(before, snapshot(model));
        model.terminateAlliance(dutch, france);
        assertEquals(EuropeanRelationState.PEACE,
                model.getEuropeanRelation(dutch, france));
        assertEquals(Model.ALLIANCE_WAR_WAIT_ROUNDS,
                model.getWarDeclarationWait(dutch, france));
        assertTrue(model.isAttackRestrained(dutch, france));
        final String afterEnd = snapshot(model);
        assertThrows(IllegalStateException.class,
                () -> model.declareHostilities(dutch, france));
        assertThrows(IllegalArgumentException.class,
                () -> model.terminateAlliance(dutch, france));
        assertEquals(afterEnd, snapshot(model));
        // France cannot attack by declaration during the wait either.
        final ModelPo frenchTurn = model.save();
        frenchTurn.getGameManager().setCurrentPlayer("France");
        final Model french = make(frenchTurn);
        assertTrue(
                french.getUnitById(6).prepareAttack(CONTACT, true).isEmpty());
        // The wait counts complete rounds and then expires.
        for (final int[] expected : new int[][] { { 4, 1 }, { 5, 0 } }) {
            final ModelPo later = model.save();
            later.getCalendar().setNumberOfPlayedTurns(expected[0]);
            final Model waited = make(later);
            assertEquals(expected[1], waited.getWarDeclarationWait(
                    player(waited, "Dutch"), player(waited, "France")));
            if (expected[1] == 0) {
                waited.declareHostilities(player(waited, "Dutch"),
                        player(waited, "France"));
                assertEquals(EuropeanRelationState.WAR,
                        waited.getEuropeanRelation(player(waited, "Dutch"),
                                player(waited, "France")));
            } else {
                assertThrows(IllegalStateException.class,
                        () -> waited.declareHostilities(player(waited, "Dutch"),
                                player(waited, "France")));
            }
        }
    }

    @Test
    void alliedPassageUsesTheColonyTileWithoutOwnershipOrWorkforceAccess() {
        final ModelPo peaceData = data("Dutch");
        final Model peace = contact(peaceData);
        assertFalse(peace.getUnitById(0).isPossibleToMoveAt(COLONY));
        final ModelPo data = data("Dutch");
        relate(data, "Dutch", "France", EuropeanRelationState.ALLIANCE);
        final Model model = contact(data);
        final Player dutch = player(model, "Dutch");
        final Unit visitor = model.getUnitById(0);
        final String colonies = gson.toJson(model.save().getColonies());
        assertFalse(dutch.isCurrentlyVisible(Location.of(15, 10)));
        assertTrue(visitor.isPossibleToMoveAt(COLONY));
        assertTrue(visitor.getAvailableLocations().contains(COLONY));
        visitor.moveOneStep(COLONY);
        assertEquals(COLONY, visitor.getLocation());
        final Colony colony = model.getColonyAt(COLONY).orElseThrow();
        assertEquals("France", colony.getOwner().getName());
        assertEquals(Set.of("France"),
                colony.getUnitsInsideColony().stream()
                        .map(unit -> unit.getOwner().getName())
                        .collect(java.util.stream.Collectors.toSet()));
        assertFalse(colony.getUnitsOutSideColony().contains(visitor));
        assertTrue(colony.getUnitsInPort().isEmpty());
        assertFalse(model.isOwnedCoastalPort(dutch, COLONY));
        assertEquals(colonies, gson.toJson(model.save().getColonies()));
        assertEquals("Dutch", visitor.getOwner().getName());
        assertFalse(dutch.isCurrentlyVisible(Location.of(15, 10)));
        // An allied overlap is a lawful saved position; no evidence is needed.
        final ModelPo allied = model.save();
        assertNull(row(allied, "Dutch", "France").getRetainedOverlaps());
        final Model reloaded = make(allied);
        assertEquals(COLONY, reloaded.getUnitById(0).getLocation());
        assertEquals(COLONY, reloaded.getUnitById(6).getLocation());
        assertTrue(reloaded.areAllied(player(reloaded, "Dutch"),
                player(reloaded, "France")));
    }

    /** Dutch colonist 0 shares the French colony square as an ally. */
    private Model overlapping(final int playedTurns, final String... extra) {
        final ModelPo data = data("Dutch", extra);
        data.getCalendar().setNumberOfPlayedTurns(playedTurns);
        relate(data, "Dutch", "France", EuropeanRelationState.ALLIANCE);
        if (extra.length > 0) {
            relate(data, "France", "Spain", EuropeanRelationState.ALLIANCE);
        }
        final Model model = contact(data);
        model.getUnitById(0).moveOneStep(COLONY);
        return model;
    }

    private ModelPo withMoves(final Model model) {
        final ModelPo saved = model.save();
        saved.getUnits().stream().filter(unit -> unit.getId() == 0).findFirst()
                .orElseThrow().setAvailableMoves(1);
        return saved;
    }

    @Test
    void terminationKeepsTheSharedSquareThenDepartureExpiresTheEvidence() {
        final Model model = overlapping(0);
        model.terminateAlliance(player(model, "Dutch"),
                player(model, "France"));
        // Nobody is relocated and no privilege changes.
        assertEquals(COLONY, model.getUnitById(0).getLocation());
        assertEquals(COLONY, model.getUnitById(6).getLocation());
        assertEquals("France",
                model.getColonyAt(COLONY).orElseThrow().getOwner().getName());
        final ModelPo saved = withMoves(model);
        assertEquals(List.of(COLONY),
                row(saved, "Dutch", "France").getRetainedOverlaps());
        // Arbitrary mixed placement without evidence is still malformed.
        final ModelPo noEvidence = withMoves(model);
        row(noEvidence, "Dutch", "France").setRetainedOverlaps(null);
        assertThrows(IllegalArgumentException.class, () -> make(noEvidence));
        final Model reloaded = make(saved);
        assertEquals(COLONY, reloaded.getUnitById(0).getLocation());
        assertEquals(EuropeanRelationState.PEACE, reloaded.getEuropeanRelation(
                player(reloaded, "Dutch"), player(reloaded, "France")));
        // Departure is allowed and expires the evidence; re-entry is refused.
        final Unit leaving = reloaded.getUnitById(0);
        assertTrue(leaving.isPossibleToMoveAt(CONTACT));
        leaving.moveOneStep(CONTACT);
        assertEquals(CONTACT, leaving.getLocation());
        assertNull(
                row(reloaded.save(), "Dutch", "France").getRetainedOverlaps());
        assertFalse(leaving.isPossibleToMoveAt(COLONY));
        // Evidence for a square nobody shares any more is rejected.
        final ModelPo stale = reloaded.save();
        row(stale, "Dutch", "France")
                .setRetainedOverlaps(new ArrayList<>(List.of(COLONY)));
        assertThrows(IllegalArgumentException.class, () -> make(stale));
        // The departed model loads normally.
        assertEquals(CONTACT,
                make(reloaded.save()).getUnitById(0).getLocation());
    }

    @Test
    void crownWarKeepsTheOverlapAndOnlyAdjacentCombatFollowsSeparation() {
        final Model model = overlapping(20);
        model.processCrownWar();
        final Player dutch = player(model, "Dutch");
        final Player france = player(model, "France");
        assertEquals(EuropeanRelationState.WAR,
                model.getEuropeanRelation(dutch, france));
        assertEquals(COLONY, model.getUnitById(0).getLocation());
        assertEquals(COLONY, model.getUnitById(6).getLocation());
        final ModelPo saved = withMoves(model);
        assertEquals(List.of(COLONY),
                row(saved, "Dutch", "France").getRetainedOverlaps());
        // No same-square battle, from either side.
        saved.getGameManager().setCurrentPlayer("France");
        final Model french = make(saved);
        assertTrue(french.getUnitById(6).prepareAttack(COLONY, true).isEmpty());
        // The Dutch unit leaves; the French soldier may then fight it normally.
        final ModelPo dutchTurn = withMoves(model);
        final Model leaving = make(dutchTurn);
        leaving.getUnitById(0).moveOneStep(CONTACT);
        final ModelPo afterwards = leaving.save();
        assertNull(row(afterwards, "Dutch", "France").getRetainedOverlaps());
        afterwards.getGameManager().setCurrentPlayer("France");
        final Model fight = make(afterwards);
        fight.getUnitById(6).getOwner().revealMapForUnit(fight.getUnitById(6));
        assertTrue(
                fight.getUnitById(6).prepareAttack(CONTACT, false).isPresent());
        // A hostile unit cannot enter the square it left.
        assertFalse(fight.getUnitById(0).isPossibleToMoveAt(COLONY));
    }

    @Test
    void defenseDrivenBreakKeepsTheOverlapWithoutRelocation() {
        final Model model = overlapping(0, "Spain");
        final Player dutch = player(model, "Dutch");
        final Player spain = player(model, "Spain");
        final AggressionPlan plan = model.planAggression(dutch, Set.of(spain),
                false);
        model.applyAggression(plan);
        assertRelation(model, "Dutch", "France", EuropeanRelationState.WAR);
        assertEquals(COLONY, model.getUnitById(0).getLocation());
        assertEquals(COLONY, model.getUnitById(6).getLocation());
        final ModelPo saved = model.save();
        assertEquals(List.of(COLONY),
                row(saved, "Dutch", "France").getRetainedOverlaps());
        final Model reloaded = make(saved);
        assertEquals(COLONY, reloaded.getUnitById(0).getLocation());
        assertFalse(reloaded.areAllied(player(reloaded, "Dutch"),
                player(reloaded, "France")));
    }

    @Test
    void evidenceCannotCoexistWithAnAllianceAndRealliancePrunesIt() {
        final Model model = overlapping(0);
        model.terminateAlliance(player(model, "Dutch"),
                player(model, "France"));
        // Evidence cannot be combined with a live alliance in a saved game.
        final ModelPo both = model.save();
        row(both, "Dutch", "France").setState(EuropeanRelationState.ALLIANCE);
        assertThrows(IllegalArgumentException.class, () -> make(both));
        // Re-allying makes the alliance itself the permission.
        final Unit visitor = model.getUnitById(0);
        final Unit contact = model.getUnitById(6);
        assertEquals(visitor.getLocation(), contact.getLocation());
        final ModelPo realliance = model.save();
        row(realliance, "Dutch", "France").setRetainedOverlaps(null);
        row(realliance, "Dutch", "France")
                .setState(EuropeanRelationState.ALLIANCE);
        row(realliance, "Dutch", "France").setAllianceEndedRound(null);
        assertNull(row(make(realliance).save(), "Dutch", "France")
                .getRetainedOverlaps());
    }

    @Test
    void alliesCannotAttackEvenByDeclarationAndPeaceStillAllowsIt() {
        final ModelPo peaceData = data("France");
        final Model peace = contact(peaceData);
        assertTrue(peace.getUnitById(6).prepareAttack(CONTACT, true)
                .orElseThrow().isDeclarationRequired());
        final ModelPo data = data("France");
        relate(data, "Dutch", "France", EuropeanRelationState.ALLIANCE);
        final Model model = contact(data);
        final Unit attacker = model.getUnitById(6);
        assertTrue(attacker.prepareAttack(CONTACT, true).isEmpty());
        assertTrue(attacker.prepareAttack(CONTACT, false).isEmpty());
        assertFalse(attacker.isPossibleToAttackAt(CONTACT));
        final String before = snapshot(model);
        assertThrows(IllegalStateException.class,
                () -> attacker.attack(CONTACT));
        assertEquals(before, snapshot(model));
    }

    private Model ships(final ModelPo data, final String attackerOwner,
            final UnitType attackerType, final UnitType targetType,
            final String targetOwner) {
        data.getGameManager().setGameStarted(false);
        data.getGameManager().setCurrentPlayer(null);
        data.setCombatRandomState(3L);
        final UnitPo attacker = data.getUnits().stream()
                .filter(unit -> unit.getId() == 3).findFirst().orElseThrow();
        attacker.setType(attackerType);
        attacker.setOwnerId(attackerOwner);
        attacker.setAvailableMoves(attackerType.getSpeed());
        attacker.getCargo().getSlots().clear();
        data.getUnits().removeIf(unit -> unit.getId() <= 2);
        for (int i = 0; i < attackerType.getCargoCapacity(); i++) {
            attacker.getCargo().getSlots().add(new CargoSlotPo());
        }
        final UnitPo target = new UnitPo();
        target.setId(100);
        target.setType(targetType);
        target.setOwnerId(targetOwner);
        target.setAvailableMoves(targetType.getSpeed());
        target.setAction(new UnitActionNoAction().save());
        final PlaceMapPo place = new PlaceMapPo();
        place.setLocation(TARGET);
        place.setOrientation(Direction.east);
        target.setPlaceMap(place);
        final CargoPo cargo = new CargoPo();
        for (int i = 0; i < targetType.getCargoCapacity(); i++) {
            cargo.getSlots().add(new CargoSlotPo());
        }
        target.setCargo(cargo);
        data.getUnits().add(target);
        final Model model = make(data);
        model.startGame();
        for (int i = 0; i < 12 && !model.getCurrentPlayer().getName()
                .equals(attackerOwner); i++) {
            model.endTurn();
        }
        assertEquals(attackerOwner, model.getCurrentPlayer().getName());
        model.getUnitById(3).getOwner().revealMapForUnit(model.getUnitById(3));
        return model;
    }

    private void assertRelation(final Model model, final String first,
            final String second, final EuropeanRelationState expected) {
        assertEquals(expected, model.getEuropeanRelation(player(model, first),
                player(model, second)));
        assertEquals(expected, model.getEuropeanRelation(player(model, second),
                player(model, first)));
    }

    @Test
    void alliedShipsAndPrivateersCannotFightEachOther() {
        final ModelPo data = data("Dutch");
        relate(data, "Dutch", "France", EuropeanRelationState.ALLIANCE);
        final Model privateer = ships(data, "Dutch", UnitType.PRIVATEER,
                UnitType.FRIGATE, "France");
        assertFalse(privateer.isPrivateerNavalException(
                privateer.getUnitById(3), privateer.getUnitById(100)));
        assertTrue(
                privateer.getUnitById(3).prepareAttack(TARGET, true).isEmpty());
        assertFalse(privateer.getUnitById(3).isPossibleToAttackAt(TARGET));
        final ModelPo counter = data("Dutch");
        relate(counter, "Dutch", "France", EuropeanRelationState.ALLIANCE);
        final Model frigate = ships(counter, "Dutch", UnitType.FRIGATE,
                UnitType.PRIVATEER, "France");
        assertTrue(
                frigate.getUnitById(3).prepareAttack(TARGET, false).isEmpty());
        // Without the alliance the same privateer exception still applies.
        final Model open = ships(data("Dutch"), "Dutch", UnitType.FRIGATE,
                UnitType.PRIVATEER, "France");
        assertTrue(open.getUnitById(3).prepareAttack(TARGET, false)
                .orElseThrow().isPrivateerException());
    }

    @Test
    void actualAttackRecordsAggressionAndDefendersJoinBeforeCombat() {
        // Dutch attacks France; Spain is allied with France only.
        final ModelPo data = data("Dutch", "Spain", "England");
        relate(data, "France", "Spain", EuropeanRelationState.ALLIANCE);
        // The attacker's own ally is not dragged into the aggressive war.
        relate(data, "Dutch", "England", EuropeanRelationState.ALLIANCE);
        final Model model = ships(data, "Dutch", UnitType.FRIGATE,
                UnitType.GALLEON, "France");
        final Player dutch = player(model, "Dutch");
        final Player france = player(model, "France");
        final String before = snapshot(model);
        final AttackOrder order = model.getUnitById(3)
                .prepareAttack(TARGET, true).orElseThrow();
        assertTrue(order.isDeclarationRequired());
        // Preparing the order changes no relation, history or random state.
        assertEquals(before, snapshot(model));
        order.execute();
        assertRelation(model, "Dutch", "France", EuropeanRelationState.WAR);
        assertRelation(model, "Dutch", "Spain", EuropeanRelationState.WAR);
        assertRelation(model, "France", "Spain",
                EuropeanRelationState.ALLIANCE);
        assertRelation(model, "Dutch", "England",
                EuropeanRelationState.ALLIANCE);
        assertRelation(model, "France", "England", EuropeanRelationState.PEACE);
        assertRelation(model, "Spain", "England", EuropeanRelationState.PEACE);
        assertEquals(10, model.getAllianceBlockedRounds(dutch, france));
        assertEquals(0,
                model.getAllianceBlockedRounds(dutch, player(model, "Spain")));
        final Model restored = make(model.save());
        assertRelation(restored, "Dutch", "Spain", EuropeanRelationState.WAR);
        assertEquals(10, restored.getAllianceBlockedRounds(
                player(restored, "Dutch"), player(restored, "France")));
    }

    @Test
    void conflictingAllianceCannotBypassTheDutyToDefendTheVictim() {
        final ModelPo data = data("Dutch", "Spain");
        relate(data, "France", "Spain", EuropeanRelationState.ALLIANCE);
        relate(data, "Dutch", "Spain", EuropeanRelationState.ALLIANCE);
        final Model model = ships(data, "Dutch", UnitType.FRIGATE,
                UnitType.GALLEON, "France");
        model.getUnitById(3).prepareAttack(TARGET, true).orElseThrow()
                .execute();
        assertRelation(model, "Dutch", "France", EuropeanRelationState.WAR);
        assertRelation(model, "Dutch", "Spain", EuropeanRelationState.WAR);
        assertRelation(model, "France", "Spain",
                EuropeanRelationState.ALLIANCE);
        assertFalse(model.areAllied(player(model, "Dutch"),
                player(model, "Spain")));
    }

    @Test
    void humanDefenderAllyReceivesTheRecordedNotice() {
        final ModelPo data = data("France", "Spain");
        relate(data, "Dutch", "Spain", EuropeanRelationState.ALLIANCE);
        final Model model = ships(data, "France", UnitType.FRIGATE,
                UnitType.GALLEON, "Spain");
        model.getUnitById(3).prepareAttack(TARGET, true).orElseThrow()
                .execute();
        assertRelation(model, "France", "Spain", EuropeanRelationState.WAR);
        assertRelation(model, "Dutch", "France", EuropeanRelationState.WAR);
        assertRelation(model, "Dutch", "Spain", EuropeanRelationState.ALLIANCE);
        final List<TurnEvent> events = model
                .getTurnEventsLocalizedMessages(player(model, "Dutch"));
        assertEquals(1, events.stream()
                .filter(TurnEventDiplomacy.class::isInstance).count());
        final TurnEventDiplomacy notice = (TurnEventDiplomacy) events.stream()
                .filter(TurnEventDiplomacy.class::isInstance).findFirst()
                .orElseThrow();
        assertEquals(TurnEventDiplomacy.Kind.ALLY_DEFENSE, notice.getKind());
        assertEquals(player(model, "France").getDisplayName(),
                notice.getRivalName());
        assertEquals(player(model, "Spain").getDisplayName(),
                notice.getVictimName());
        // The notice is saved and reloaded without replaying relation changes.
        final Model restored = make(model.save());
        assertEquals(1, restored
                .getTurnEventsLocalizedMessages(player(restored, "Dutch"))
                .stream().filter(TurnEventDiplomacy.class::isInstance).count());
    }

    @Test
    void privateerAttackRecordsHistoryWithoutWarOrDefenseCascade() {
        final ModelPo data = data("Dutch", "Spain");
        relate(data, "France", "Spain", EuropeanRelationState.ALLIANCE);
        final Model model = ships(data, "Dutch", UnitType.PRIVATEER,
                UnitType.FRIGATE, "France");
        model.getUnitById(3).prepareAttack(TARGET, false).orElseThrow()
                .execute();
        assertRelation(model, "Dutch", "France", EuropeanRelationState.PEACE);
        assertRelation(model, "Dutch", "Spain", EuropeanRelationState.PEACE);
        assertEquals(10, model.getAllianceBlockedRounds(player(model, "Dutch"),
                player(model, "France")));
    }

    @Test
    void allianceDeniesTheColonyAttackReviewEvenWithDeclaration() {
        final ModelPo data = data("Dutch");
        relate(data, "Dutch", "France", EuropeanRelationState.ALLIANCE);
        final Model model = contact(data);
        final Unit visitor = model.getUnitById(0);
        ((org.microcol.model.unit.UnitFreeColonist) visitor)
                .setHoldingGuns(true);
        final String before = snapshot(model);
        assertTrue(visitor.prepareAttack(COLONY, true).isEmpty());
        assertFalse(visitor.isPossibleToAttackAt(COLONY));
        assertEquals(before, snapshot(model));
    }

    @Test
    void declarationAloneNeitherRecordsAggressionNorDrawsAllies() {
        final ModelPo data = data("Dutch", "Spain");
        relate(data, "France", "Spain", EuropeanRelationState.ALLIANCE);
        final Model model = contact(data);
        model.declareHostilities(player(model, "Dutch"),
                player(model, "France"));
        assertRelation(model, "Dutch", "France", EuropeanRelationState.WAR);
        assertRelation(model, "Dutch", "Spain", EuropeanRelationState.PEACE);
        assertEquals(0, model.getAllianceBlockedRounds(player(model, "Dutch"),
                player(model, "France")));
    }

    @Test
    void capturingAColonyIsAggressionThatObligesTheVictimsAllies() {
        final ModelPo data = data("Dutch", "Spain");
        relate(data, "Dutch", "France", EuropeanRelationState.WAR);
        relate(data, "France", "Spain", EuropeanRelationState.ALLIANCE);
        data.getUnits().removeIf(unit -> unit.getId() == 6);
        data.setCombatRandomState(CombatDraws.attackerWins());
        final Model model = contact(data);
        final Unit capturer = model.getUnitById(0);
        ((org.microcol.model.unit.UnitFreeColonist) capturer)
                .setHoldingGuns(true);
        // A step never takes a colony its inhabitants defend; the won battle
        // is the actual aggression that obliges the victim's allies.
        assertFalse(capturer.isPossibleToMoveAt(COLONY));
        capturer.prepareAttack(COLONY, false).orElseThrow().execute();
        assertEquals("Dutch",
                model.getColonyAt(COLONY).orElseThrow().getOwner().getName());
        assertRelation(model, "Dutch", "France", EuropeanRelationState.WAR);
        assertRelation(model, "Dutch", "Spain", EuropeanRelationState.WAR);
        assertRelation(model, "France", "Spain",
                EuropeanRelationState.ALLIANCE);
        assertEquals(10, model.getAllianceBlockedRounds(player(model, "Dutch"),
                player(model, "France")));
    }

    @Test
    void invalidOrStalePlansChangeNothing() {
        final ModelPo data = data("Dutch", "Spain");
        relate(data, "France", "Spain", EuropeanRelationState.ALLIANCE);
        relate(data, "Dutch", "Spain", EuropeanRelationState.ALLIANCE);
        final Model model = contact(data);
        final Player dutch = player(model, "Dutch");
        final Player france = player(model, "France");
        final Player spain = player(model, "Spain");
        final String before = snapshot(model);
        // Allies cannot attack each other.
        assertThrows(IllegalStateException.class,
                () -> model.planAggression(dutch, Set.of(spain), false));
        assertEquals(before, snapshot(model));
        // A plan made before a relation change cannot be applied afterwards.
        final AggressionPlan plan = model.planAggression(dutch, Set.of(france),
                false);
        model.declareHostilities(dutch, france);
        final String changed = snapshot(model);
        assertThrows(IllegalStateException.class,
                () -> model.applyAggression(plan));
        assertEquals(changed, snapshot(model));
        // A Crown victim never causes a European relation change.
        assertTrue(model.planAggression(dutch,
                Set.of(player(model, "Dutch's King")), false).isEmpty());
    }

    private ModelPo scripted(final List<String> nations) {
        final ModelPo data = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.microcol").getModel();
        final List<PlayerPo> players = data.getGameManager().getPlayers();
        final PlayerPo template = players.stream()
                .filter(player -> player.getName().equals("France")).findFirst()
                .orElseThrow();
        final PlayerPo copy = gson.fromJson(gson.toJson(template),
                PlayerPo.class);
        players.clear();
        for (final String name : nations) {
            final PlayerPo nation = gson.fromJson(gson.toJson(copy),
                    PlayerPo.class);
            nation.setName(name);
            nation.setComputer(!name.equals("Dutch"));
            players.add(nation);
        }
        data.getUnits().clear();
        data.getColonies().clear();
        data.getTurnEvents().clear();
        data.getEuropeanRelations().clear();
        for (int first = 0; first < nations.size(); first++) {
            for (int second = first + 1; second < nations.size(); second++) {
                final EuropeanRelationPo pair = new EuropeanRelationPo();
                pair.setFirstPlayer(nations.get(first));
                pair.setSecondPlayer(nations.get(second));
                pair.setState(EuropeanRelationState.PEACE);
                data.getEuropeanRelations().add(pair);
            }
        }
        data.getGameManager().setGameStarted(false);
        data.getGameManager().setCurrentPlayer(null);
        return data;
    }

    private Model scriptedModel(final ModelPo data) {
        return Model.make(data,
                List.of(GameOverEvaluator.GAMEOVER_CONDITION_CALENDAR));
    }

    /** A saved game in progress, for direct round-boundary processing. */
    private ModelPo inProgress(final ModelPo data, final int playedTurns) {
        data.getGameManager().setGameStarted(true);
        data.getGameManager().setCurrentPlayer("Dutch");
        data.getCalendar().setNumberOfPlayedTurns(playedTurns);
        return data;
    }

    private void finishRound(final Model model) {
        final int before = model.getCalendar().getNumberOfPlayedTurns();
        for (int i = 0; i < 12 && model.getCalendar()
                .getNumberOfPlayedTurns() == before; i++) {
            model.endTurn();
        }
        assertEquals(before + 1, model.getCalendar().getNumberOfPlayedTurns());
    }

    private List<String> wars(final Model model) {
        final List<String> out = new ArrayList<>();
        for (final Player first : model.getPlayers()) {
            for (final Player second : model.getPlayers()) {
                if (!first.isKing() && !second.isKing()
                        && first.getName().compareTo(second.getName()) < 0
                        && model.getEuropeanRelation(first,
                                second) == EuropeanRelationState.WAR) {
                    out.add(first.getName() + "-" + second.getName());
                }
            }
        }
        java.util.Collections.sort(out);
        return out;
    }

    @Test
    void crownEventWaitsForTheTwentiethCompleteRound() {
        final ModelPo data = scripted(List.of("Dutch", "France", "Spain"));
        data.getCalendar().setNumberOfPlayedTurns(18);
        final Model model = scriptedModel(data);
        model.startGame();
        finishRound(model);
        assertEquals(19, model.getCalendar().getNumberOfPlayedTurns());
        assertTrue(wars(model).isEmpty());
        assertNull(model.save().getCrownWarRound());
        finishRound(model);
        assertEquals(20, model.getCalendar().getNumberOfPlayedTurns());
        assertEquals(1, wars(model).size());
        assertEquals(20, model.save().getCrownWarRound());
        final String atTwenty = snapshot(model);
        finishRound(model);
        assertEquals(21, model.getCalendar().getNumberOfPlayedTurns());
        assertEquals(1, wars(model).size());
        assertEquals(20, model.save().getCrownWarRound());
        assertNotEquals(atTwenty, snapshot(model));
    }

    @Test
    void secondPeriodFiresAgainAtRoundForty() {
        final ModelPo data = scripted(List.of("Dutch", "France", "Spain"));
        data.getCalendar().setNumberOfPlayedTurns(39);
        data.setCrownWarRound(20);
        final Model model = scriptedModel(data);
        model.startGame();
        finishRound(model);
        assertEquals(40, model.getCalendar().getNumberOfPlayedTurns());
        assertEquals(1, wars(model).size());
        assertEquals(40, model.save().getCrownWarRound());
    }

    @Test
    void startLoadAndContinueNeverRepeatTheEvent() {
        final ModelPo data = scripted(List.of("Dutch", "France", "Spain"));
        data.getCalendar().setNumberOfPlayedTurns(19);
        final Model model = scriptedModel(data);
        model.startGame();
        finishRound(model);
        final List<String> forced = wars(model);
        assertEquals(1, forced.size());
        final String saved = snapshot(model);
        final Model resumed = scriptedModel(model.save());
        assertEquals(saved, snapshot(resumed));
        resumed.startGame();
        resumed.processCrownWar();
        assertEquals(saved, snapshot(resumed));
        assertEquals(forced, wars(resumed));
        // Starting a fresh game at a multiple of twenty is no round boundary.
        final ModelPo fresh = scripted(List.of("Dutch", "France", "Spain"));
        fresh.getCalendar().setNumberOfPlayedTurns(20);
        final Model started = scriptedModel(fresh);
        started.startGame();
        assertTrue(wars(started).isEmpty());
    }

    @Test
    void oneEligiblePairIsChosenAndItsPlayersAreTold() {
        final ModelPo data = scripted(List.of("Dutch", "France", "Spain"));
        relate(data, "Dutch", "France", EuropeanRelationState.WAR);
        relate(data, "Dutch", "Spain", EuropeanRelationState.WAR);
        data.getCalendar().setNumberOfPlayedTurns(19);
        final Model model = scriptedModel(data);
        model.startGame();
        finishRound(model);
        assertEquals(List.of("Dutch-France", "Dutch-Spain", "France-Spain"),
                wars(model));
        final ModelPo two = scripted(List.of("Dutch", "France"));
        two.getCalendar().setNumberOfPlayedTurns(19);
        final Model pair = scriptedModel(two);
        pair.startGame();
        finishRound(pair);
        assertEquals(List.of("Dutch-France"), wars(pair));
        final List<TurnEvent> events = pair
                .getTurnEventsLocalizedMessages(player(pair, "Dutch"));
        assertEquals(1, events.stream()
                .filter(TurnEventDiplomacy.class::isInstance).count());
        assertEquals(TurnEventDiplomacy.Kind.CROWN_WAR,
                ((TurnEventDiplomacy) events.stream()
                        .filter(TurnEventDiplomacy.class::isInstance)
                        .findFirst().orElseThrow()).getKind());
    }

    @Test
    void noEligiblePairProcessesTheRoundWithoutDrawOrChange() {
        final ModelPo data = scripted(List.of("Dutch", "France", "Spain"));
        relate(data, "Dutch", "France", EuropeanRelationState.WAR);
        relate(data, "Dutch", "Spain", EuropeanRelationState.WAR);
        relate(data, "France", "Spain", EuropeanRelationState.WAR);
        data.getCalendar().setNumberOfPlayedTurns(19);
        final Long random = data.getCombatRandomState();
        final Model model = scriptedModel(data);
        model.startGame();
        finishRound(model);
        assertEquals(3, wars(model).size());
        assertEquals(random, model.save().getCombatRandomState());
        assertEquals(20, model.save().getCrownWarRound());
        assertTrue(model.getTurnEventsLocalizedMessages(player(model, "Dutch"))
                .stream().noneMatch(TurnEventDiplomacy.class::isInstance));
    }

    @ParameterizedTest
    @ValueSource(strings = { "Dutch", "France" })
    void independenceOfEitherParticipantExcludesThePair(final String who) {
        final ModelPo data = scripted(List.of("Dutch", "France"));
        RebellionFixtures.declare(data,
                data.getGameManager().getPlayers().stream()
                        .filter(player -> player.getName().equals(who))
                        .findFirst().orElseThrow());
        final Long random = data.getCombatRandomState();
        final Model model = scriptedModel(inProgress(data, 20));
        model.processCrownWar();
        assertTrue(wars(model).isEmpty());
        assertEquals(random, model.save().getCombatRandomState());
        assertEquals(20, model.save().getCrownWarRound());
    }

    @ParameterizedTest
    @ValueSource(strings = { "Dutch", "France" })
    void franklinMembershipOfEitherParticipantExcludesThePair(
            final String who) {
        final ModelPo data = scripted(List.of("Dutch", "France"));
        final var congress = data.getGameManager().getPlayers().stream()
                .filter(player -> player.getName().equals(who)).findFirst()
                .orElseThrow().getCongress();
        congress.setActivated(true);
        congress.setMembers(List.of(Founder.BENJAMIN_FRANKLIN));
        congress.setCandidates(List.of(Founder.JOHN_PAUL_JONES,
                Founder.THOMAS_PAINE, Founder.ADAM_SMITH, Founder.WILLIAM_PENN,
                Founder.FERDINAND_MAGELLAN));
        congress.setGeneration(2);
        final Long random = data.getCombatRandomState();
        final Model model = scriptedModel(inProgress(data, 20));
        model.processCrownWar();
        assertTrue(wars(model).isEmpty());
        assertEquals(random, model.save().getCombatRandomState());
    }

    @Test
    void franklinJoiningThroughOwnerUpdateExcludesThePairFromTheCrownWar() {
        // Control: without the father the same pair is forced into war.
        final Model control = scriptedModel(
                inProgress(scripted(List.of("Dutch", "France")), 20));
        control.processCrownWar();
        assertEquals(List.of("Dutch-France"), wars(control));

        final ModelPo data = inProgress(scripted(List.of("Dutch", "France")),
                20);
        RealFounderJoin.readyFor(RealFounderJoin.congressOf(data, "Dutch"),
                Founder.BENJAMIN_FRANKLIN);
        final Long random = data.getCombatRandomState();
        final Model model = scriptedModel(data);
        RealFounderJoin.joinThroughOwnerUpdate(model,
                Founder.BENJAMIN_FRANKLIN);
        model.processCrownWar();
        assertTrue(wars(model).isEmpty());
        assertEquals(random, model.save().getCombatRandomState());
        assertEquals(20, model.save().getCrownWarRound());
    }

    @Test
    void anotherEligiblePairStillQualifiesWhenOneMemberIsExcluded() {
        final ModelPo data = scripted(List.of("Dutch", "France", "Spain"));
        RebellionFixtures.declare(data,
                data.getGameManager().getPlayers().stream()
                        .filter(player -> player.getName().equals("Spain"))
                        .findFirst().orElseThrow());
        final Model model = scriptedModel(inProgress(data, 20));
        model.processCrownWar();
        assertEquals(List.of("Dutch-France"), wars(model));
    }

    @Test
    void crownWarEndsTheSelectedPairsAllianceBeforeApplyingWar() {
        final ModelPo data = scripted(List.of("Dutch", "France"));
        relate(data, "Dutch", "France", EuropeanRelationState.ALLIANCE);
        final Model model = scriptedModel(inProgress(data, 20));
        model.processCrownWar();
        final Player dutch = player(model, "Dutch");
        final Player france = player(model, "France");
        assertEquals(EuropeanRelationState.WAR,
                model.getEuropeanRelation(dutch, france));
        assertFalse(model.areAllied(dutch, france));
        assertEquals(0, model.getWarDeclarationWait(dutch, france));
        final TurnEventDiplomacy notice = (TurnEventDiplomacy) model
                .getTurnEventsLocalizedMessages(dutch).stream()
                .filter(TurnEventDiplomacy.class::isInstance).findFirst()
                .orElseThrow();
        assertEquals(TurnEventDiplomacy.Kind.CROWN_WAR_ALLIANCE_ENDED,
                notice.getKind());
        assertNull(row(model.save(), "Dutch", "France").getAllianceOffer());
        assertNull(
                row(model.save(), "Dutch", "France").getAllianceEndedRound());
    }

    @Test
    void selectionIsUniformOverEligiblePairsAndUsesTheSavedRandomState() {
        final Set<String> seen = new HashSet<>();
        for (long seed = 0; seed < 12; seed++) {
            final ModelPo data = scripted(List.of("Dutch", "France", "Spain"));
            data.setCombatRandomState(seed);
            final Model first = scriptedModel(inProgress(data, 20));
            first.processCrownWar();
            assertEquals(1, wars(first).size());
            seen.addAll(wars(first));
            // The same saved state reproduces the same outcome.
            final ModelPo again = scripted(List.of("Dutch", "France", "Spain"));
            again.setCombatRandomState(seed);
            final Model second = scriptedModel(inProgress(again, 20));
            second.processCrownWar();
            assertEquals(wars(first), wars(second));
            assertNotEquals(Long.valueOf(seed),
                    first.save().getCombatRandomState());
        }
        assertTrue(seen.size() >= 2);
    }

}
