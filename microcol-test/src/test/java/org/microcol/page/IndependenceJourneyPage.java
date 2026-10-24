package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.microcol.gui.screen.game.components.ButtonsGamePanel;
import org.microcol.gui.screen.game.gamepanel.SelectedUnitManager;
import org.microcol.model.Colony;
import org.microcol.model.GameResult;
import org.microcol.model.GoodsType;
import org.microcol.model.InterventionTarget;
import org.microcol.model.Location;
import org.microcol.model.MilitaryRole;
import org.microcol.model.ModelListenerAdapter;
import org.microcol.model.Player;
import org.microcol.model.ResultScore;
import org.microcol.model.RoyalRoster;
import org.microcol.model.RoyalRoster.Family;
import org.microcol.model.RoyalRoster.State;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.microcol.model.event.GameFinishedEvent;
import org.microcol.model.event.TurnStartedEvent;
import org.microcol.model.unit.UnitFreeColonist;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;

/**
 * The connected independence journey as the player lives it (R-0220, UC-118):
 * from the support that is built, through the declaration and the royal war, to
 * the earned score. Every action is a player gesture: the acting unit is chosen
 * in the right panel of its square, so a stacked garrison never acts with
 * another unit, and it then attacks or walks with the map gesture; the turn
 * ends with the ordinary control. The model is only read, to choose the gesture
 * and to observe the outcome. Only the {@code verify...} methods assert.
 */
public final class IndependenceJourneyPage extends AbstractScreen {

    private static final String DUTCH = "Dutch";

    private static final String CROWN = "Dutch's King";

    /** Steps one unit may walk in one Dutch turn. */
    private static final int MAX_STEPS = 4;

    private static final int MAX_MESSAGES = 3;

    private static final String CAPTURE_NOTICE = "You captured colony";

    private final AttackPage attack;

    /** Whether the Crown has held a Dutch colony at any moment seen so far. */
    private boolean crownHeldColony;

    /** Identities of the intervention units at their single arrival. */
    private final Set<Integer> arrivedForces = new HashSet<>();

    /** Every intervention unit identity observed during the journey. */
    private final Set<Integer> observedForces = new HashSet<>();

    public IndependenceJourneyPage(final TestContext context) {
        super(context);
        attack = new AttackPage(context);
    }

    // --- player gestures ---------------------------------------------------

    /**
     * Hold the receiving port while withdrawing the other garrisons. Reclaim
     * occupied colonies only after intervention, so victory cannot end the
     * journey before the aid arrives. Leave its arrival report open.
     *
     * @param game  required game screen
     * @param limit largest number of turns; it only bounds the test
     * @return the report of the arrival update, if it was shown
     */
    public Optional<ScreenTurnReport> playTurnsUntilInterventionArrives(
            final GamePage game, final int limit) throws Exception {
        for (int turn = 0; turn < limit
                && !getModel().isGameFinished(); turn++) {
            withdrawGarrisons(game);
            for (final UnitFreeColonist soldier : armedDutchSoldiers()) {
                if (soldier.getLocation().equals(receivingPort())
                        && soldier.getMovementThirds() > 0) {
                    final Optional<Location> target = hostileNeighbour(soldier);
                    if (target.isPresent()) {
                        strike(game, soldier, target.get());
                    }
                }
            }
            if (getModel().isGameFinished()) {
                break;
            }
            final Optional<ScreenTurnReport> report = game.nextTurn();
            observeWar();
            if (isInterventionDelivered()) {
                arrivedForces.addAll(interventionUnitIds());
                observedForces.addAll(arrivedForces);
                return report;
            }
            report.ifPresent(ScreenTurnReport::close);
        }
        return Optional.empty();
    }

    /**
     * Play ordinary Dutch turns with one round of fighting in each until the
     * game ends, by a Dutch battle or in a Crown turn.
     *
     * @param game  required game screen
     * @param limit largest number of turns; it only bounds the test
     */
    public void playTurnsUntilTheGameEnds(final GamePage game, final int limit)
            throws Exception {
        for (int turn = 0; turn < limit
                && !getModel().isGameFinished(); turn++) {
            if (crownHeldColony) {
                fightRound(game);
            } else {
                withdrawGarrisons(game);
            }
            if (!getModel().isGameFinished()) {
                endTurn(game);
            }
            observeWar();
        }
    }

    private Location receivingPort() {
        return getModel().getColonyById(0).orElseThrow().getLocation();
    }

    /** Keep the port until aid arrives, then withdraw its garrison as well. */
    private void withdrawGarrisons(final GamePage game) throws Exception {
        for (final UnitFreeColonist soldier : armedDutchSoldiers()) {
            final Location from = soldier.getLocation();
            if (soldier.getMovementThirds() <= 0
                    || (!isInterventionDelivered()
                            && from.equals(receivingPort()))
                    || getModel().getColonyAt(from).isEmpty()) {
                continue;
            }
            // Adjacent defenders intercept the royal approach instead of
            // yielding the colony.
            final int colony = getModel().getColonyAt(from).orElseThrow()
                    .getId();
            final Location refuge = colony == 0 ? Location.of(25, 15)
                    : colony == 1 ? Location.of(21, 12) : Location.of(22, 16);
            selectUnitAt(game, from, soldier.getId());
            game.panToTile(refuge);
            new FogPage(getContext()).move(from, refuge, soldier.getId());
        }
    }

    /**
     * One round of the Dutch turn: each armed Dutch soldier with movement left
     * attacks a Crown soldier or a Crown-held colony beside it. A Continental,
     * mustered or delivered by the intervention, without such a target walks
     * toward the nearest one; the colonial veterans stay in their garrison.
     *
     * @param game required game screen
     */
    public void fightRound(final GamePage game) throws Exception {
        observeWar();
        for (final UnitFreeColonist soldier : armedDutchSoldiers()) {
            if (getModel().isGameFinished()) {
                return;
            }
            fightOrWalk(game, soldier.getId());
            observeWar();
        }
    }

    /** Close a notice that the player captured a colony, when one is shown. */
    public void closeCaptureNoticeIfShown() {
        final Optional<Label> label = captureNotice();
        if (label.isEmpty()) {
            return;
        }
        final Button ok = (Button) label.get().getScene().getRoot()
                .lookup("#buttonOk");
        getRobot().clickOn(ok, MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Select the export of ore through the colony's custom house. */
    public void exportOreThroughCustomHouse(final GamePage game,
            final Location colonySquare, final String colonyName) {
        final ColonyScreen colony = game.openColonyAt(colonySquare, colonyName);
        final CustomHousePage exports = new CustomHousePage(getContext());
        exports.open();
        exports.toggle(GoodsType.ORE);
        exports.confirm();
        colony.close();
    }

    private void fightOrWalk(final GamePage game, final int unitId)
            throws Exception {
        for (int step = 0; step <= MAX_STEPS; step++) {
            final Optional<Unit> found = getModel().tryGetUnitById(unitId);
            if (found.isEmpty() || getModel().isGameFinished()
                    || !found.get().isAtPlaceLocation()
                    || found.get().getActionPoints() <= 0) {
                return;
            }
            final Unit soldier = found.get();
            final Optional<Location> adjacent = hostileNeighbour(soldier);
            if (adjacent.isPresent()) {
                strike(game, soldier, adjacent.get());
                return;
            }
            if (!isWalker(soldier) || !walkOneStep(game, soldier)) {
                return;
            }
        }
    }

    private void strike(final GamePage game, final Unit soldier,
            final Location target) throws Exception {
        final Location from = soldier.getLocation();
        selectUnitAt(game, from, soldier.getId());
        game.panToTile(target);
        attack.chooseAttack(from, target);
        attack.confirm();
        WaitForAsyncUtils.waitForFxEvents();
        for (int message = 0; message < MAX_MESSAGES
                && !getModel().isGameFinished()
                && isCssIdVisible("buttonOk"); message++) {
            attack.closeMessage();
            WaitForAsyncUtils.waitForFxEvents();
        }
        game.waitWhileMoving();
    }

    private boolean walkOneStep(final GamePage game, final Unit soldier)
            throws Exception {
        final Optional<Location> goal = nearestCrownTarget(
                soldier.getLocation());
        if (goal.isEmpty()) {
            return false;
        }
        final Location from = soldier.getLocation();
        Location best = null;
        int bestDistance = from.getDistanceChebyshev(goal.get());
        for (final Location neighbour : from.getNeighbors()) {
            if (getModel().isValid(neighbour)
                    && soldier.isPossibleToMoveAt(neighbour)
                    && getModel().getUnitsAt(neighbour).isEmpty()
                    && getModel().getColonyAt(neighbour).isEmpty() && neighbour
                            .getDistanceChebyshev(goal.get()) < bestDistance) {
                best = neighbour;
                bestDistance = neighbour.getDistanceChebyshev(goal.get());
            }
        }
        if (best == null) {
            return false;
        }
        selectUnitAt(game, from, soldier.getId());
        game.panToTile(best);
        attack.preview(from, best);
        attack.release();
        game.waitWhileMoving();
        return true;
    }

    /**
     * Select the soldier with the ordinary next-unit key without opening its
     * colony.
     */
    private void selectUnitAt(final GamePage game, final Location square,
            final int unitId) throws Exception {
        final int limit = getModel().getAllUnits().size() + 1;
        for (int press = 0; press < limit
                && selectedUnitId() != unitId; press++) {
            game.pressTab();
            WaitForAsyncUtils.waitForFxEvents();
        }
        if (selectedUnitId() != unitId) {
            throw new IllegalStateException(
                    "Cannot select active soldier " + unitId);
        }
        // Tab starts smooth scrolling; a visible tile does not mean the camera
        // has stopped.
        final org.microcol.gui.Point[] previous = { null };
        final long[] unchangedSince = { System.nanoTime() };
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS, () -> {
            final org.microcol.gui.Point current = getContext().getArea()
                    .convertToCanvasPoint(square);
            if (!current.equals(previous[0])) {
                previous[0] = current;
                unchangedSince[0] = System.nanoTime();
            }
            return System.nanoTime()
                    - unchangedSince[0] >= TimeUnit.MILLISECONDS.toNanos(300);
        });
        game.panToTile(square);
    }

    /**
     * End the turn with the next-turn control and wait for the real outcome:
     * the next human turn or the end of the game, which a Crown battle may
     * bring. A turn report that opens is closed.
     */
    private void endTurn(final GamePage game) throws Exception {
        final int previousRound = getModel().getCalendar()
                .getNumberOfPlayedTurns();
        final CountDownLatch settled = new CountDownLatch(1);
        final ModelListenerAdapter listener = new ModelListenerAdapter() {
            @Override
            public void onTurnStarted(final TurnStartedEvent event) {
                if (event.getPlayer().isHuman() && getModel().getCalendar()
                        .getNumberOfPlayedTurns() > previousRound) {
                    settled.countDown();
                }
            }

            @Override
            public void onGameFinished(final GameFinishedEvent event) {
                settled.countDown();
            }
        };
        getModel().addListener(listener);
        try {
            getRobot().clickOn(
                    getButtonById(ButtonsGamePanel.BUTTON_NEXT_TURN_ID));
            if (!settled.await(60, TimeUnit.SECONDS)) {
                throw new TimeoutException(
                        "Neither the next human turn nor the end of the game came");
            }
        } finally {
            getModel().removeListener(listener);
        }
        WaitForAsyncUtils.waitForFxEvents();
        if (!getModel().isGameFinished()) {
            game.waitWhileMoving();
            if (!getModel()
                    .isTurnEventsMessagesEmpty(getModel().getCurrentPlayer())) {
                WaitForAsyncUtils.waitFor(60, TimeUnit.SECONDS,
                        () -> WaitForAsyncUtils
                                .asyncFx(this::isButtonBackVisible)
                                .get(60, TimeUnit.SECONDS));
                ScreenTurnReport.of(getContext()).close();
            }
        }
    }

    private int selectedUnitId() {
        return getContext().getClassFromGuice(SelectedUnitManager.class)
                .getSelectedUnit().map(Unit::getId).orElse(-1);
    }

    // --- reading what the player can see ----------------------------------

    private List<UnitFreeColonist> armedDutchSoldiers() {
        return getModel().getAllUnits().stream()
                .filter(unit -> unit instanceof UnitFreeColonist
                        && DUTCH.equals(unit.getOwner().getName())
                        && unit.isAtPlaceLocation() && unit.canAttack())
                .map(UnitFreeColonist.class::cast)
                .sorted(Comparator.comparingInt(Unit::getId))
                .collect(Collectors.toList());
    }

    /** Continentals leave their square; colonial veterans hold it. */
    private static boolean isWalker(final Unit soldier) {
        return soldier instanceof UnitFreeColonist
                && ((UnitFreeColonist) soldier)
                        .getMilitaryRole() == MilitaryRole.CONTINENTAL;
    }

    private boolean isCrownTarget(final Location square) {
        final Optional<Colony> colony = getModel().getColonyAt(square);
        if (colony.isPresent()
                && CROWN.equals(colony.get().getOwner().getName())) {
            return true;
        }
        return getModel().getUnitsAt(square).stream()
                .anyMatch(unit -> CROWN.equals(unit.getOwner().getName())
                        && !unit.isShip());
    }

    private Optional<Location> hostileNeighbour(final Unit soldier) {
        return soldier.getLocation().getNeighbors().stream()
                .filter(getModel()::isValid).filter(this::isCrownTarget)
                .findFirst();
    }

    private Optional<Location> nearestCrownTarget(final Location from) {
        Location nearest = null;
        for (final Unit unit : getModel().getAllUnits()) {
            if (CROWN.equals(unit.getOwner().getName()) && !unit.isShip()
                    && unit.isAtPlaceLocation()) {
                nearest = nearer(from, nearest, unit.getLocation());
            }
        }
        for (final Colony colony : getModel().getColonies()) {
            if (CROWN.equals(colony.getOwner().getName())) {
                nearest = nearer(from, nearest, colony.getLocation());
            }
        }
        return Optional.ofNullable(nearest);
    }

    private static Location nearer(final Location from, final Location best,
            final Location candidate) {
        return best == null || from.getDistanceChebyshev(candidate) < from
                .getDistanceChebyshev(best) ? candidate : best;
    }

    /** Remember what the war has shown so far; nothing is changed. */
    private void observeWar() {
        if (getModel().getColonies().stream().anyMatch(
                colony -> CROWN.equals(colony.getOwner().getName()))) {
            crownHeldColony = true;
        }
        if (!arrivedForces.isEmpty()) {
            observedForces.addAll(interventionUnitIds());
        }
    }

    private boolean isInterventionDelivered() {
        return dutch().getIntervention().map(InterventionTarget::isDelivered)
                .orElse(false);
    }

    /** Living Dutch men-of-war and soldiers delivered by the intervention. */
    private Set<Integer> interventionUnitIds() {
        return dutch().getAllUnits().stream()
                .filter(unit -> unit.getType() == UnitType.MAN_OF_WAR
                        || unit instanceof UnitFreeColonist
                                && ((UnitFreeColonist) unit)
                                        .isInterventionDelivered())
                .map(Unit::getId).collect(Collectors.toSet());
    }

    private Optional<Label> captureNotice() {
        return getNodeFinder()
                .lookup((Node node) -> node instanceof Label
                        && ((Label) node).getText() != null
                        && ((Label) node).getText().contains(CAPTURE_NOTICE))
                .tryQuery().map(Label.class::cast);
    }

    private Player dutch() {
        return getModel().getPlayerByName(DUTCH);
    }

    private RoyalRoster roster() {
        return getModel().getRoyalRosterOf(getModel().getPlayerByName(CROWN));
    }

    private GameResult frozen() {
        return getModel().getFrozenResult().orElseThrow();
    }

    private List<String> referenceLedger;

    /**
     * Preserve the completed reference route before loading its earlier
     * checkpoint.
     */
    public void rememberFinalLedger() {
        referenceLedger = finalLedger();
    }

    private List<String> finalLedger() {
        return List.of(frozen().getBaseScore().toString(),
                frozen().getScore().toString(),
                String.valueOf(
                        getModel().getCalendar().getNumberOfPlayedTurns()),
                dutch().getAllUnits().stream()
                        .filter(unit -> unit instanceof UnitFreeColonist)
                        .map(Unit::getId).sorted().map(String::valueOf)
                        .collect(Collectors.joining(",")),
                new com.google.gson.Gson().toJson(roster()));
    }

    /**
     * Continue from the same saved world must preserve the terminal score, turn
     * and forces.
     */
    public void verifyRememberedFinalLedger() {
        assertEquals(referenceLedger, finalLedger(),
                "Uninterrupted versus Save/Continue ledger");
    }

    // --- explicit observations --------------------------------------------

    /** The Dutch treasury holds exactly the given gold. */
    public void verifyDutchGold(final int expected) {
        assertEquals(expected, dutch().getGold(), "Dutch gold");
    }

    /**
     * National rebel support as the exact liberty points over 200 points per
     * resident; units outside the colonies are not residents.
     */
    public void verifyNationalSupport(final long expectedPoints,
            final long expectedCapacity) {
        assertEquals(expectedPoints,
                dutch().getNationalRebelSupport().getNumerator(),
                "Liberty points");
        assertEquals(expectedCapacity,
                dutch().getNationalRebelSupport().getDenominator(),
                "Liberty capacity");
    }

    /** The support line of the declaration confirmation. */
    public void verifySupportLine(final String expected) {
        assertEquals(expected,
                getLabeledById("declareIndependenceSupport").getText());
    }

    /** The calendar year and played turns of the current turn. */
    public void verifyTurn(final int expectedPlayedTurns,
            final int expectedYear) {
        assertEquals(expectedPlayedTurns,
                getModel().getCalendar().getNumberOfPlayedTurns(),
                "Played turns");
        assertEquals(expectedYear, getModel().getCalendar().getCurrentYear(),
                "Year");
    }

    /** The year in which the Dutch declared independence. */
    public void verifyDeclarationYear(final int expected) {
        assertEquals(Optional.of(expected), dutch().getDeclarationYear(),
                "Declaration year");
    }

    /** Count of roster members of one family in one state. */
    public void verifyRosterCount(final Family family, final State state,
            final int expected) {
        assertEquals(expected, roster().getCount(family, state),
                family + " " + state);
    }

    /** The roster has exactly this many members, so it did not grow. */
    public void verifyRosterMembers(final int expectedMembers) {
        assertEquals(expectedMembers, roster().getMembers().size(),
                "Roster members");
    }

    /** The victory left at most the given number of living ground units. */
    public void verifyLivingRoyalGroundAtMost(final int limit) {
        assertTrue(roster().getLivingGroundCount() <= limit,
                "Living royal ground units: "
                        + roster().getLivingGroundCount());
    }

    /** Stock of one goods in a colony. */
    public void verifyColonyStock(final int colonyId, final GoodsType goods,
            final int expected) {
        assertEquals(
                expected, getModel().getColonyById(colonyId).orElseThrow()
                        .getWarehouse().getGoods(goods).getAmount(),
                "Stock of " + goods);
    }

    /** Bells counted toward the foreign intervention since the declaration. */
    public void verifyInterventionBells(final long expected) {
        assertEquals(expected,
                dutch().getIntervention().orElseThrow().getBells(),
                "Intervention bells");
    }

    /** Whether the earned forces were delivered to the map. */
    public void verifyInterventionDelivered(final boolean expected) {
        assertEquals(expected,
                dutch().getIntervention().orElseThrow().isDelivered(),
                "Intervention delivered");
    }

    /**
     * The intervention arrived once: eight units at its arrival, and no
     * intervention unit of another identity was ever seen afterwards.
     */
    public void verifyInterventionArrivedOnce(final int expectedUnits) {
        assertEquals(expectedUnits, arrivedForces.size(),
                "Intervention units at the arrival");
        assertEquals(arrivedForces, observedForces,
                "Intervention units seen after the arrival");
    }

    /**
     * The Crown held a rebel colony during the war and every colony of the
     * rebellion is Dutch again.
     */
    public void verifyOccupiedAndReclaimed() {
        assertTrue(crownHeldColony, "The Crown never held a colony");
        for (final int id : dutch().getRebellionColonyIds()) {
            assertEquals(DUTCH, getModel().getColonyById(id).orElseThrow()
                    .getOwner().getName(), "Colony " + id);
        }
    }

    /** The members of the rebellion that were fixed at the declaration. */
    public void verifyRebellionMembers(final Integer... expected) {
        assertEquals(Set.of(expected), dutch().getRebellionColonyIds());
    }

    /** Persons the Dutch own, as the base score counts them. */
    public void verifyDutchPersons(final int expectedColonists) {
        assertEquals(expectedColonists,
                getModel().getAllUnits().stream()
                        .filter(unit -> unit instanceof UnitFreeColonist
                                && unit.getOwner() == dutch())
                        .count(),
                "Dutch colonists");
    }

    /** The Crown holds no colony. */
    public void verifyCrownHoldsNoColony() {
        assertFalse(
                getModel().getColonies().stream().anyMatch(
                        colony -> CROWN.equals(colony.getOwner().getName())),
                "The Crown still holds a colony");
    }

    /**
     * The terms of the frozen result that the rules fix for this journey,
     * independent of the course of the war: no father joined, no settlement was
     * destroyed, the bells after the target are the counted bells less the
     * target, the order bonus, the early points and the difficulty factor, and
     * the total is the single floor of the calculated result.
     *
     * @param fatherPoints      expected father points
     * @param target            intervention target in bells
     * @param orderBonusPercent expected order bonus
     * @param earlyPoints       expected early declaration points
     * @param difficultyPercent expected difficulty factor
     * @param expectedGold      treasury after the single replacement-trade
     *                          export
     */
    public void verifyFrozenScoreLedger(final long fatherPoints,
            final int target, final int orderBonusPercent,
            final long earlyPoints, final int difficultyPercent,
            final long expectedGold) {
        final GameResult result = frozen();
        final ResultScore score = result.getScore();
        assertEquals(fatherPoints, result.getBaseScore().getFatherPoints(),
                "Father points");
        assertEquals(0, score.getDestroyedSettlements(),
                "Destroyed settlements");
        assertEquals(
                dutch().getIntervention().orElseThrow().getBells() - target,
                score.getBellsAfterTarget(), "Bells after the target");
        assertEquals(orderBonusPercent, score.getOrderBonusPercent(),
                "Order bonus");
        assertEquals(earlyPoints, score.getEarlyPoints(), "Early points");
        assertEquals(difficultyPercent, score.getDifficultyPercent(),
                "Difficulty factor");
        // This fixture contains ordinary colonists and elder statesmen only.
        long population = 0;
        for (final Unit unit : dutch().getAllUnits()) {
            if (unit instanceof UnitFreeColonist && !unit.isCrownSeized()) {
                assertTrue(
                        unit.getType() == UnitType.COLONIST
                                || unit.getType() == UnitType.ELDER_STATESMAN,
                        "Unexpected civilian identity in the journey ledger: "
                                + unit.getType());
                population += unit.getType() == UnitType.ELDER_STATESMAN ? 4
                        : 2;
            }
        }
        long liberty = 0;
        long capacity = 0;
        for (final Colony colony : getModel().getColonies(dutch())) {
            liberty += colony.getLiberty();
            capacity += 200L * colony.getPopulation();
        }
        final long support = capacity == 0 ? 0 : 100 * liberty / capacity;
        assertEquals(expectedGold, dutch().getGold(),
                "Treasury after the single ore export");
        final long subtotal = population + fatherPoints + expectedGold / 1000
                + support;
        final long bells = dutch().getIntervention().orElseThrow().getBells()
                - target;
        final long adjusted = subtotal + bells;
        final long expectedTotal = (adjusted * (100 + orderBonusPercent)
                + 100 * earlyPoints) * difficultyPercent / 10000;
        assertEquals(population, result.getBaseScore().getPopulationPoints(),
                "Population ledger");
        assertEquals(expectedGold / 1000, result.getBaseScore().getGoldPoints(),
                "Treasury points");
        assertEquals(support, result.getBaseScore().getSupportPoints(),
                "Support from colony liberty");
        assertEquals(subtotal, result.getBaseScore().getSubtotal(),
                "Independent base subtotal");
        assertEquals(subtotal, score.getBaseSubtotal(), "Result base subtotal");
        assertEquals(0, score.getDestructionDeduction(),
                "No native settlement destroyed");
        assertEquals(adjusted, score.getClampedBase(),
                "Independent adjusted base");
        assertEquals(expectedTotal, score.getTotal(),
                "Independent final score");
        System.out.println("R0220 score ledger: population=" + population
                + ", gold=" + expectedGold + ", support=" + support + ", bells="
                + bells + ", total=" + expectedTotal);
    }
}
