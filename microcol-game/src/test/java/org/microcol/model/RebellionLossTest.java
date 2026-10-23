package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.event.GameFinishedEvent;
import org.microcol.model.store.InterventionPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.unit.UnitFreeColonist;

/**
 * Membership of a declared rebellion and its loss reasons (R-0192). The scene
 * is the thrive map of the royal tests: the Dutch rebels own New Amsterdam (5
 * residents), Delft (1) and Haarlem (6), all coastal, and their Crown owns
 * nothing. Colonies are given to the Crown in the saved game, or taken by real
 * defense battles, so every reason is reached from a state a game can reach.
 */
class RebellionLossTest {

    private static final String KING = RoyalScenarioFixtures.KING;

    private static final String DUTCH = RoyalScenarioFixtures.REBELS;

    private static final int NEW_AMSTERDAM = 0;

    private static final int DELFT = 1;

    private static final int HAARLEM = 2;

    private static final Location DELFT_AT = Location.of(22, 12);

    private static final Location HAARLEM_AT = Location.of(23, 17);

    /** Free land square beside Delft where its only resident steps out. */
    private static final Location DELFT_FIELD = Location.of(23, 12);

    /** The only resident of Delft. */
    private static final int DELFT_RESIDENT = 6;

    /**
     * A land square far from every colony where a new colony can be founded.
     */
    private static final Location NEW_SITE = Location.of(17, 15);

    private static final int TARGET = 500;

    private static Model load(final ModelPo data) {
        return Model.make(data, MissionImpl.getPredefinedGameOverEvaluators());
    }

    private static PlayerPo rebelPo(final ModelPo data) {
        return data.getGameManager().getPlayers().stream()
                .filter(player -> DUTCH.equals(player.getName())).findFirst()
                .orElseThrow();
    }

    private static List<Integer> ids(final int... values) {
        return Arrays.stream(values).boxed().collect(Collectors.toList());
    }

    /**
     * The declared thrive scene in which the Crown holds the given colonies and
     * the rebellion has exactly the given members.
     */
    private static GameModelPo scene(final int[] crownColonies,
            final int[] members) {
        final GameModelPo data = RoyalScenarioFixtures.declaredCrown();
        RebellionFixtures.transfer(data.getModel(), KING, crownColonies);
        rebelPo(data.getModel()).setRebellionColonyIds(ids(members));
        return data;
    }

    private static String reasonOf(final Model model) {
        return RebellionLoss.evaluate(model, model.getPlayerByName(DUTCH))
                .map(GameOverResult::getGameOverReason).orElse(null);
    }

    private static String reasonOf(final GameModelPo data) {
        return reasonOf(load(data.getModel()));
    }

    /**
     * Take a colony by the real defense battle. The winning force stays on the
     * colony square as an armed garrison, so a test that lets the other side
     * retake the colony first removes that force.
     */
    private static Unit capture(final Model model, final int colonyId,
            final String attacker) {
        final Colony colony = model.getColonyById(colonyId).orElseThrow();
        final Unit force = model.addUnitOutSideColony(colony);
        force.takeOver(model.getPlayerByName(attacker));
        ColonyBattles.capture(model, colony, force);
        return force;
    }

    private static List<GameFinishedEvent> listen(final Model model) {
        final List<GameFinishedEvent> seen = new ArrayList<>();
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onGameFinished(final GameFinishedEvent event) {
                seen.add(event);
            }
        });
        return seen;
    }

    private static InterventionPo pendingHelp() {
        final InterventionPo po = new InterventionPo();
        po.setTarget(TARGET);
        po.setBells((long) TARGET);
        po.setEarned(true);
        po.setDelivered(false);
        return po;
    }

    /** Men-of-war and Continental soldiers that an intervention delivered. */
    private static long helpUnits(final Player owner) {
        return owner.getAllUnits().stream()
                .filter(unit -> unit.getType() == UnitType.MAN_OF_WAR
                        || unit instanceof UnitFreeColonist
                                && ((UnitFreeColonist) unit)
                                        .isInterventionDelivered())
                .count();
    }

    // ---- membership -------------------------------------------------------

    @Test
    void declarationFixesTheMembersAsTheColoniesOwnedAtThatTime() {
        final Model model = load(
                RoyalScenarioFixtures.declaredCrown().getModel());
        final Player rebel = model.getPlayerByName(DUTCH);
        final Set<Integer> owned = model.getColonies(rebel).stream()
                .map(Colony::getId).collect(Collectors.toSet());
        assertEquals(Set.of(NEW_AMSTERDAM, DELFT, HAARLEM), owned);
        assertEquals(owned, rebel.getRebellionColonyIds());
    }

    @Test
    void undeclaredNationKeepsNoMembers() {
        final Model model = load(RoyalScenarioFixtures
                .crown(Difficulty.CONQUISTADOR, false).getModel());
        assertTrue(
                model.getPlayerByName(DUTCH).getRebellionColonyIds().isEmpty());
    }

    @Test
    void aCapturedMemberStaysAMemberAndRecoveryDoesNotDuplicateIt() {
        final Model model = load(
                RoyalScenarioFixtures.declaredCrown().getModel());
        final Player rebel = model.getPlayerByName(DUTCH);
        final Player crown = model.getPlayerByName(KING);

        // The Crown takes New Amsterdam in a real defense battle.
        final Unit garrison = capture(model, NEW_AMSTERDAM, KING);
        assertSame(crown,
                model.getColonyById(NEW_AMSTERDAM).orElseThrow().getOwner());
        assertEquals(Set.of(NEW_AMSTERDAM, DELFT, HAARLEM),
                rebel.getRebellionColonyIds());
        assertFalse(model.isGameFinished(),
                "Two coastal members still hold most residents");

        // The rebels take it back; it is still the same single member.
        model.destroyUnit(garrison);
        capture(model, NEW_AMSTERDAM, DUTCH);
        assertSame(rebel,
                model.getColonyById(NEW_AMSTERDAM).orElseThrow().getOwner());
        assertEquals(Set.of(NEW_AMSTERDAM, DELFT, HAARLEM),
                rebel.getRebellionColonyIds());
        assertFalse(model.isGameFinished());
    }

    @Test
    void aColonyTheRebelsCaptureLaterJoinsAndStaysAfterTheCrownRetakesIt() {
        // Haarlem and New Amsterdam were always royal; only Delft is a member.
        final GameModelPo data = scene(new int[] { NEW_AMSTERDAM, HAARLEM },
                new int[] { DELFT });
        final Model model = load(data.getModel());
        final Player rebel = model.getPlayerByName(DUTCH);
        assertEquals(Set.of(DELFT), rebel.getRebellionColonyIds());

        final Unit garrison = capture(model, HAARLEM, DUTCH);
        assertEquals(Set.of(DELFT, HAARLEM), rebel.getRebellionColonyIds());
        assertFalse(rebel.getRebellionColonyIds().contains(NEW_AMSTERDAM),
                "A royal colony the rebels never took is not a member");

        model.destroyUnit(garrison);
        capture(model, HAARLEM, KING);
        assertEquals(Set.of(DELFT, HAARLEM), rebel.getRebellionColonyIds());
        assertFalse(model.isGameFinished(),
                "Delft is still held by the rebels");
    }

    @Test
    void aColonyTheRebelsFoundAfterTheDeclarationJoins() {
        final Model model = load(
                RoyalScenarioFixtures.declaredCrown().getModel());
        final Player rebel = model.getPlayerByName(DUTCH);
        final Unit founder = model.addUnitOutSideColony(
                model.getColonyById(NEW_AMSTERDAM).orElseThrow());
        founder.placeToLocation(NEW_SITE);

        final Colony founded = model.buildColony(rebel, founder, "Zaandam");

        assertEquals(Set.of(NEW_AMSTERDAM, DELFT, HAARLEM, founded.getId()),
                rebel.getRebellionColonyIds());
        assertFalse(model.isGameFinished());
    }

    @Test
    void aDestroyedMemberLeavesTheRebellionWhoeverOwnedIt() {
        // Rebel-held Delft: its only resident steps out, the colony is gone.
        final Model rebelHeld = load(
                RoyalScenarioFixtures.declaredCrown().getModel());
        rebelHeld.getUnitById(DELFT_RESIDENT).placeToMap(DELFT_FIELD);
        assertTrue(rebelHeld.getColonyById(DELFT).isEmpty());
        assertEquals(Set.of(NEW_AMSTERDAM, HAARLEM),
                rebelHeld.getPlayerByName(DUTCH).getRebellionColonyIds());
        assertFalse(rebelHeld.isGameFinished());

        // Crown-held Delft that the Crown took: it is a member until it is
        // gone.
        final Model crownHeld = load(scene(new int[] { DELFT },
                new int[] { NEW_AMSTERDAM, DELFT, HAARLEM }).getModel());
        assertEquals(Set.of(NEW_AMSTERDAM, DELFT, HAARLEM),
                crownHeld.getPlayerByName(DUTCH).getRebellionColonyIds());
        crownHeld.getUnitById(DELFT_RESIDENT).placeToMap(DELFT_FIELD);
        assertTrue(crownHeld.getColonyById(DELFT).isEmpty());
        assertEquals(Set.of(NEW_AMSTERDAM, HAARLEM),
                crownHeld.getPlayerByName(DUTCH).getRebellionColonyIds());
        assertFalse(crownHeld.isGameFinished());
    }

    // ---- each loss reason on its own --------------------------------------

    @Test
    void noSavedMembersIsALossAndOwnershipDoesNotAddMembers() {
        final GameModelPo data = RoyalScenarioFixtures.declaredCrown();
        rebelPo(data.getModel()).setRebellionColonyIds(List.of());
        final Model model = load(data.getModel());
        assertTrue(
                model.getPlayerByName(DUTCH).getRebellionColonyIds().isEmpty());
        assertEquals(3, model.getColonies(model.getPlayerByName(DUTCH)).size(),
                "The rebels still own colonies; they are not members");
        assertEquals(GameOverEvaluator.REASON_REBELLION_NO_COLONIES,
                reasonOf(model));
    }

    @Test
    void everySurvivingMemberHeldByTheCrownLosesFirst() {
        // The coastal and population conditions hold too; the listed order
        // gives the single reason.
        final GameModelPo data = scene(
                new int[] { NEW_AMSTERDAM, DELFT, HAARLEM },
                new int[] { NEW_AMSTERDAM, DELFT, HAARLEM });
        assertEquals(GameOverEvaluator.REASON_REBELLION_ALL_COLONIES_CROWN,
                reasonOf(data));
    }

    @Test
    void anInlandMemberAloneLosesBecauseNoCoastalMemberSurvives() {
        // Delft is the only member, inland and rebel-held; the royal colonies
        // are not members and add nothing.
        final GameModelPo data = scene(new int[] { NEW_AMSTERDAM, HAARLEM },
                new int[] { DELFT });
        RoyalScenarioFixtures.wallAround(data, DELFT_AT);
        assertEquals(GameOverEvaluator.REASON_REBELLION_COASTAL_CROWN,
                reasonOf(data));
    }

    @Test
    void everyCoastalMemberHeldByTheCrownLosesWhileAnInlandMemberHasHalf() {
        // Haarlem (6 residents) is rebel-held but inland; New Amsterdam (5) and
        // Delft (1) are royal. Not all members are royal, and the Crown holds
        // only 6 of 12 residents, so the coastal condition alone decides.
        final GameModelPo data = scene(new int[] { NEW_AMSTERDAM, DELFT },
                new int[] { NEW_AMSTERDAM, DELFT, HAARLEM });
        RoyalScenarioFixtures.wallAround(data, HAARLEM_AT);
        assertEquals(GameOverEvaluator.REASON_REBELLION_COASTAL_CROWN,
                reasonOf(data));
    }

    @Test
    void theCoastalReasonComesBeforeThePopulationReason() {
        // Delft is rebel-held and inland; the royal New Amsterdam and Haarlem
        // hold 11 of 12 residents, so the population condition holds as well.
        final GameModelPo data = scene(new int[] { NEW_AMSTERDAM, HAARLEM },
                new int[] { NEW_AMSTERDAM, DELFT, HAARLEM });
        RoyalScenarioFixtures.wallAround(data, DELFT_AT);
        assertEquals(GameOverEvaluator.REASON_REBELLION_COASTAL_CROWN,
                reasonOf(data));
    }

    @Test
    void ninetyPercentOfTheResidentsAloneLosesWhileACoastalMemberHolds() {
        // Delft is coastal and rebel-held; New Amsterdam and Haarlem are royal
        // and hold 11 of the 12 residents. Outside troops do not help.
        final GameModelPo data = scene(new int[] { NEW_AMSTERDAM, HAARLEM },
                new int[] { NEW_AMSTERDAM, DELFT, HAARLEM });
        final Model model = load(data.getModel());
        final Colony delft = model.getColonyById(DELFT).orElseThrow();
        for (int index = 0; index < 20; index++) {
            model.addUnitOutSideColony(delft);
        }
        assertEquals(1, delft.getPopulation());
        assertEquals(GameOverEvaluator.REASON_REBELLION_RESIDENTS_CROWN,
                reasonOf(model));
    }

    @Test
    void belowNinetyPercentOfTheResidentsTheRebellionHolds() {
        // Haarlem and Delft are royal: 7 of 12 residents.
        assertNullReason(scene(new int[] { HAARLEM, DELFT },
                new int[] { NEW_AMSTERDAM, DELFT, HAARLEM }));
        // Only Haarlem is royal: 6 of 12 residents.
        assertNullReason(scene(new int[] { HAARLEM },
                new int[] { NEW_AMSTERDAM, DELFT, HAARLEM }));
        // Nothing is royal.
        assertNullReason(RoyalScenarioFixtures.declaredCrown());
    }

    private static void assertNullReason(final GameModelPo data) {
        assertEquals(null, reasonOf(data));
    }

    @Test
    void theNinetyPercentThresholdIsAnExactIntegerComparison() {
        assertFalse(RebellionLoss.isCrownResidentLoss(89, 100));
        assertTrue(RebellionLoss.isCrownResidentLoss(90, 100));
        assertFalse(RebellionLoss.isCrownResidentLoss(8, 10));
        assertTrue(RebellionLoss.isCrownResidentLoss(9, 10));
        assertFalse(RebellionLoss.isCrownResidentLoss(179, 200));
        assertTrue(RebellionLoss.isCrownResidentLoss(180, 200));
        assertFalse(RebellionLoss.isCrownResidentLoss(0, 1));
        assertTrue(RebellionLoss.isCrownResidentLoss(1, 1));
        // No rounding and no overflow far beyond a real colony.
        assertFalse(RebellionLoss.isCrownResidentLoss(
                Integer.MAX_VALUE * 9L / 10 - 1, Integer.MAX_VALUE));
        assertTrue(RebellionLoss.isCrownResidentLoss(Integer.MAX_VALUE,
                Integer.MAX_VALUE));
    }

    @Test
    void aZeroDenominatorIsALoss() {
        assertTrue(RebellionLoss.isCrownResidentLoss(0, 0));
    }

    /**
     * Put new workers into free building jobs of a colony, as its owner would
     * on its own turn; the fixture only says whose turn it is.
     */
    private static void addResidents(final Model model, final int colonyId,
            final int count) {
        final Colony colony = model.getColonyById(colonyId).orElseThrow();
        final Player previous = model.getCurrentPlayer();
        ColonyBattles.setCurrentPlayer(model, colony.getOwner());
        try {
            for (int index = 0; index < count; index++) {
                final Unit worker = model.addUnitOutSideColony(colony);
                final ConstructionSlot job = colony.getConstructions().stream()
                        .flatMap(construction -> construction
                                .getConstructionSlots().stream())
                        .filter(slot -> slot.canAccept(worker)).findFirst()
                        .orElseThrow(() -> new AssertionError(
                                "No free job in " + colony.getName()));
                worker.placeToColonyStructureSlot(job);
            }
        } finally {
            ColonyBattles.setCurrentPlayer(model, previous);
        }
    }

    /** Units standing outside the colony; they are troops, not residents. */
    private static void addOutsideTroops(final Model model, final int colonyId,
            final int count) {
        final Colony colony = model.getColonyById(colonyId).orElseThrow();
        for (int index = 0; index < count; index++) {
            ((UnitFreeColonist) model.addUnitOutSideColony(colony))
                    .setHoldingGuns(true);
        }
    }

    private static int population(final Model model, final int colonyId) {
        return model.getColonyById(colonyId).orElseThrow().getPopulation();
    }

    /**
     * The Crown holds New Amsterdam and Haarlem; the rebels keep coastal Delft,
     * so neither the first nor the coastal condition can decide and only the
     * current residents of the surviving members do.
     */
    private static Model populationScene() {
        final Model model = load(scene(new int[] { NEW_AMSTERDAM, HAARLEM },
                new int[] { NEW_AMSTERDAM, DELFT, HAARLEM }).getModel());
        assertEquals(5, population(model, NEW_AMSTERDAM));
        assertEquals(6, population(model, HAARLEM));
        assertEquals(1, population(model, DELFT));
        return model;
    }

    @Test
    void eighteenOfTwentyActualResidentsHeldByTheCrownLose() {
        final Model model = populationScene();
        // Crown 5 + 7 + 6 = 18, rebels 1 + 1 = 2: exactly ninety percent.
        addResidents(model, NEW_AMSTERDAM, 7);
        addResidents(model, DELFT, 1);
        // Rebel troops outside Delft are no residents and change nothing.
        addOutsideTroops(model, DELFT, 20);
        assertEquals(12, population(model, NEW_AMSTERDAM));
        assertEquals(2, population(model, DELFT));

        assertEquals(GameOverEvaluator.REASON_REBELLION_RESIDENTS_CROWN,
                reasonOf(model));
        model.checkRebellionLoss();
        assertEquals(
                Optional.of(GameOverEvaluator.REASON_REBELLION_RESIDENTS_CROWN),
                model.getGameOverResult()
                        .map(GameOverResult::getGameOverReason));
    }

    @Test
    void seventeenOfTwentyActualResidentsHeldByTheCrownHold() {
        final Model model = populationScene();
        // Crown 5 + 6 + 6 = 17, rebels 1 + 2 = 3: below ninety percent.
        addResidents(model, NEW_AMSTERDAM, 6);
        addResidents(model, DELFT, 2);
        // Crown troops outside its colonies are no residents either.
        addOutsideTroops(model, NEW_AMSTERDAM, 20);
        addOutsideTroops(model, HAARLEM, 20);
        assertEquals(3, population(model, DELFT));

        assertEquals(null, reasonOf(model));
        model.checkRebellionLoss();
        assertFalse(model.isGameFinished());
    }

    @Test
    void theCurrentResidentsCountNotThoseAtTheDeclaration() {
        final Model model = populationScene();
        // As saved, the Crown holds 11 of 12 residents.
        assertEquals(GameOverEvaluator.REASON_REBELLION_RESIDENTS_CROWN,
                reasonOf(model));
        // Two new workers in Delft make it 11 of 14 before anything is judged.
        addResidents(model, DELFT, 2);
        assertEquals(null, reasonOf(model));
        model.checkRebellionLoss();
        assertFalse(model.isGameFinished());
        // A worker leaving Delft again makes it 11 of 13, still below.
        final Unit worker = model.getColonyById(DELFT).orElseThrow()
                .getUnitsInsideColony().get(0);
        worker.placeToMap(DELFT_AT);
        assertEquals(2, population(model, DELFT));
        assertFalse(model.isGameFinished());
    }

    @Test
    void royalColoniesOutsideTheRebellionNeverCountAsResidents() {
        // Delft is the only member. The Crown's own New Amsterdam and Haarlem
        // hold 11 of 12 residents on the map and must not decide anything.
        final GameModelPo data = scene(new int[] { NEW_AMSTERDAM, HAARLEM },
                new int[] { DELFT });
        assertNullReason(data);
    }

    // ---- completed boundaries ---------------------------------------------

    @Test
    void aResidentsExitIsJudgedOnlyAfterTheWholePlacement() {
        // Delft is rebel-held with 1 resident. Haarlem, a member, was taken by
        // the Crown: 6 of 7 residents is below ninety percent. New Amsterdam
        // is royal and not a member.
        final GameModelPo data = scene(new int[] { NEW_AMSTERDAM, HAARLEM },
                new int[] { DELFT, HAARLEM });
        final Model model = load(data.getModel());
        assertEquals(null, reasonOf(model));
        final Unit resident = model.getUnitById(DELFT_RESIDENT);
        final List<String> seen = new ArrayList<>();
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onGameFinished(final GameFinishedEvent event) {
                seen.add(event.getGameOverResult().getGameOverReason() + ":"
                        + (resident.isAtPlaceLocation()
                                ? resident.getLocation().toString()
                                : "in transit"));
            }
        });

        // The last resident leaves Delft by the shared placement path.
        resident.placeToMap(DELFT_FIELD);

        assertEquals(
                List.of(GameOverEvaluator.REASON_REBELLION_ALL_COLONIES_CROWN
                        + ":" + DELFT_FIELD),
                seen,
                "The loss is announced once, with the resident already placed");
        assertTrue(model.isGameFinished());
        assertEquals(
                Optional.of(
                        GameOverEvaluator.REASON_REBELLION_ALL_COLONIES_CROWN),
                model.getGameOverResult()
                        .map(GameOverResult::getGameOverReason));
    }

    @Test
    void aLaterActionNeverAnnouncesOrOverridesTheResult() {
        final Model model = load(scene(new int[] { NEW_AMSTERDAM, HAARLEM },
                new int[] { DELFT, HAARLEM }).getModel());
        final List<GameFinishedEvent> events = listen(model);
        model.getUnitById(DELFT_RESIDENT).placeToMap(DELFT_FIELD);
        assertEquals(1, events.size());
        final GameOverResult first = model.getGameOverResult().orElseThrow();

        model.checkRebellionLoss();
        model.checkRebellionLoss();

        assertEquals(1, events.size());
        assertEquals(first, model.getGameOverResult().orElseThrow());
    }

    // ---- persistence and reload -------------------------------------------

    @Test
    void theSavedReasonSurvivesReloadWithoutANewEvent() {
        final Model model = load(scene(new int[] { NEW_AMSTERDAM, HAARLEM },
                new int[] { DELFT, HAARLEM }).getModel());
        model.getUnitById(DELFT_RESIDENT).placeToMap(DELFT_FIELD);
        final String reason = GameOverEvaluator.REASON_REBELLION_ALL_COLONIES_CROWN;

        final ModelPo saved = model.save();
        assertTrue(saved.getGameManager().isGameFinished());
        assertEquals(reason, saved.getGameManager().getGameOverReason());
        assertEquals(reason, rebelPo(saved).getRebellionLossReason());
        assertEquals(ids(HAARLEM), rebelPo(saved).getRebellionColonyIds());

        final Model restored = load(saved);
        final List<GameFinishedEvent> events = listen(restored);
        assertTrue(restored.isGameFinished());
        assertEquals(Optional.of(reason), restored.getGameOverResult()
                .map(GameOverResult::getGameOverReason));
        assertEquals(Optional.of(reason),
                restored.getPlayerByName(DUTCH).getRebellionLossReason());
        assertEquals(Set.of(HAARLEM),
                restored.getPlayerByName(DUTCH).getRebellionColonyIds());

        // Continue cannot start a finished game, and a judgment adds nothing.
        assertThrows(IllegalStateException.class, restored::startGame);
        restored.checkRebellionLoss();
        assertTrue(events.isEmpty(), "The end event is not repeated");
    }

    @Test
    void anOpenGameKeepsNoReasonAndReloadsOpen() {
        final Model model = load(
                RoyalScenarioFixtures.declaredCrown().getModel());
        final ModelPo saved = model.save();
        assertFalse(saved.getGameManager().isGameFinished());
        assertEquals(null, saved.getGameManager().getGameOverReason());
        assertEquals(null, rebelPo(saved).getRebellionLossReason());
        final Model restored = load(saved);
        assertTrue(restored.getGameOverResult().isEmpty());
        assertTrue(restored.getPlayerByName(DUTCH).getRebellionLossReason()
                .isEmpty());
    }

    @Test
    void loadRejectsAnIncoherentLossState() {
        final String reason = GameOverEvaluator.REASON_REBELLION_NO_COLONIES;

        // A lost human rebellion without the finished game that it ended.
        final ModelPo open = load(
                RoyalScenarioFixtures.declaredCrown().getModel()).save();
        rebelPo(open).setRebellionLossReason(reason);
        assertThrows(RuntimeException.class, () -> load(open));

        // A game over reason without a finished game.
        final ModelPo notFinished = load(
                RoyalScenarioFixtures.declaredCrown().getModel()).save();
        notFinished.getGameManager().setGameOverReason(reason);
        assertThrows(RuntimeException.class, () -> load(notFinished));

        // An unknown reason, also for a computer rebel.
        final ModelPo unknown = load(
                RoyalScenarioFixtures.declaredCrown().getModel()).save();
        rebelPo(unknown).setComputer(true);
        rebelPo(unknown).setRebellionLossReason("BOGUS");
        assertThrows(RuntimeException.class, () -> load(unknown));

        // A loss of a nation that never declared.
        final ModelPo undeclared = RoyalScenarioFixtures
                .crown(Difficulty.CONQUISTADOR, false).getModel();
        rebelPo(undeclared).setRebellionLossReason(reason);
        assertThrows(RuntimeException.class, () -> load(undeclared));
    }

    // ---- loss before help --------------------------------------------------

    /**
     * The Crown holds New Amsterdam and Haarlem, 11 of the 12 residents, while
     * the rebels keep coastal Delft: a real port for the earned help. The saved
     * game is not started yet, so starting it runs the rebels' own update.
     */
    private static GameModelPo pendingHelpScene() {
        final GameModelPo data = scene(new int[] { NEW_AMSTERDAM, HAARLEM },
                new int[] { NEW_AMSTERDAM, DELFT, HAARLEM });
        rebelPo(data.getModel()).setIntervention(pendingHelp());
        data.getModel().getGameManager().setGameStarted(false);
        return data;
    }

    /**
     * What a player can see of the world, to show nothing moves after the end.
     */
    private static String worldOf(final Model model) {
        return model.getAllUnits().stream()
                .map(unit -> unit.getId() + "@" + unit.getOwner().getName())
                .sorted().collect(Collectors.joining(","))
                + "|"
                + model.getColonies().stream()
                        .map(colony -> colony.getId() + ":"
                                + colony.getOwner().getName() + ":"
                                + colony.getPopulation())
                        .collect(Collectors.joining(","))
                + "|" + model.getPlayerByName(DUTCH).getGold() + "|"
                + model.getCalendar().getNumberOfPlayedTurns();
    }

    @Test
    void theRebelsOwnUpdateEndsTheGameAfterItsLastStepWithoutTheHelp() {
        final Model model = load(pendingHelpScene().getModel());
        final Player rebel = model.getPlayerByName(DUTCH);
        assertTrue(model.isOwnedCoastalPort(rebel, DELFT_AT),
                "Delft is a real port for the help");
        assertEquals(GameOverEvaluator.REASON_REBELLION_RESIDENTS_CROWN,
                reasonOf(model));
        assertTrue(model.getCalendar().getNumberOfPlayedTurns() <= 15,
                "The scene stands inside the legacy 15-turn grace period, "
                        + "which a declared revolution does not have");
        final long helpBefore = helpUnits(rebel);
        final List<String> atEnd = new ArrayList<>();
        final List<String> afterEnd = new ArrayList<>();
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onGameFinished(final GameFinishedEvent event) {
                atEnd.add(event.getGameOverResult().getGameOverReason());
                atEnd.add(worldOf(model));
                // The update is complete: saving refuses a pending preparation.
                try {
                    model.save();
                    atEnd.add("saveable");
                } catch (final TurnPreparationException pending) {
                    atEnd.add("preparation pending");
                }
            }

            @Override
            public void onTurnStarted(
                    final org.microcol.model.event.TurnStartedEvent event) {
                record("turn " + event.getPlayer().getName());
            }

            @Override
            public void onRoundStarted(
                    final org.microcol.model.event.RoundStartedEvent event) {
                record("round");
            }

            @Override
            public void onGoldWasChanged(
                    final org.microcol.model.event.GoldWasChangedEvent event) {
                record("gold");
            }

            @Override
            public void onUnitMovedToLocation(
                    final org.microcol.model.event.UnitMovedToLocationEvent event) {
                record("move " + event.getUnit().getId());
            }

            @Override
            public void onNewUnitWasBorn(
                    final org.microcol.model.event.NewUnitWasBornEvent event) {
                record("born " + event.getUnit().getId());
            }

            private void record(final String what) {
                if (!atEnd.isEmpty()) {
                    afterEnd.add(what);
                }
            }
        });

        // Starting the saved game runs the rebels' own update first.
        model.startGame();

        assertEquals(3, atEnd.size(), "The end is published exactly once");
        assertEquals(GameOverEvaluator.REASON_REBELLION_RESIDENTS_CROWN,
                atEnd.get(0));
        assertEquals("saveable", atEnd.get(2),
                "The end is published after the whole update");
        assertEquals(List.of(), afterEnd,
                "No turn, round or gameplay follows the end");
        assertEquals(atEnd.get(1), worldOf(model),
                "The world is the one the end was published with");
        assertTrue(model.isGameFinished());
        assertEquals(
                Optional.of(GameOverEvaluator.REASON_REBELLION_RESIDENTS_CROWN),
                rebel.getRebellionLossReason());
        final InterventionTarget help = rebel.getIntervention().orElseThrow();
        assertTrue(help.isEarned());
        assertFalse(help.isDelivered(), "The earned help never arrives");
        assertEquals(helpBefore, helpUnits(rebel), "No eight units arrive");

        // The finished state saves without a pending preparation and reloads
        // finished, with the same reason and without a second end.
        final Model restored = load(model.save());
        final List<GameFinishedEvent> again = listen(restored);
        assertTrue(restored.isGameFinished());
        assertEquals(
                Optional.of(GameOverEvaluator.REASON_REBELLION_RESIDENTS_CROWN),
                restored.getGameOverResult()
                        .map(GameOverResult::getGameOverReason));
        assertFalse(restored.getPlayerByName(DUTCH).getIntervention()
                .orElseThrow().isDelivered());
        assertThrows(IllegalStateException.class, restored::startGame);
        assertTrue(again.isEmpty());
    }

    @Test
    void aComputerRebelThatLostGetsNoHelpAndDoesNotEndTheGame() {
        final GameModelPo data = pendingHelpScene();
        rebelPo(data.getModel()).setComputer(true);
        final Model model = load(data.getModel());
        final Player rebel = model.getPlayerByName(DUTCH);
        final long before = helpUnits(rebel);
        final List<GameFinishedEvent> events = listen(model);

        // The computer rebel's own update runs first.
        model.startGame();

        assertEquals(
                Optional.of(GameOverEvaluator.REASON_REBELLION_RESIDENTS_CROWN),
                rebel.getRebellionLossReason());
        assertFalse(model.isGameFinished(),
                "A computer rebel's loss never ends the game");
        assertTrue(model.getGameOverResult().isEmpty());
        assertTrue(events.isEmpty());
        assertTrue(model.isGameRunning(), "Its turn starts as usual");
        assertFalse(rebel.getIntervention().orElseThrow().isDelivered());
        assertEquals(before, helpUnits(rebel));

        // The record is durable: it survives a reload and sends nothing then.
        final ModelPo saved = model.save();
        assertEquals(GameOverEvaluator.REASON_REBELLION_RESIDENTS_CROWN,
                rebelPo(saved).getRebellionLossReason());
        assertEquals(null, saved.getGameManager().getGameOverReason());
        final Model restored = load(saved);
        final Player restoredRebel = restored.getPlayerByName(DUTCH);
        assertEquals(
                Optional.of(GameOverEvaluator.REASON_REBELLION_RESIDENTS_CROWN),
                restoredRebel.getRebellionLossReason());
        assertTrue(restoredRebel.startTurn());
        assertFalse(
                restoredRebel.getIntervention().orElseThrow().isDelivered());
        assertEquals(before, helpUnits(restoredRebel));
        assertFalse(restored.isGameFinished());
    }

    // ---- load validation of membership ------------------------------------

    @Test
    void loadRejectsADeclaredNationWithoutSavedMembers() {
        final ModelPo data = RoyalScenarioFixtures.declaredCrown().getModel();
        rebelPo(data).setRebellionColonyIds(null);
        assertThrows(RuntimeException.class, () -> load(data));
    }

    @Test
    void loadRejectsMembersThatNameNoSavedColony() {
        final ModelPo data = RoyalScenarioFixtures.declaredCrown().getModel();
        rebelPo(data).setRebellionColonyIds(List.of(Integer.MAX_VALUE));
        assertThrows(RuntimeException.class, () -> load(data));
    }

    @Test
    void loadRejectsDuplicateMembers() {
        final ModelPo data = RoyalScenarioFixtures.declaredCrown().getModel();
        rebelPo(data).setRebellionColonyIds(ids(DELFT, DELFT));
        assertThrows(RuntimeException.class, () -> load(data));
    }
}
