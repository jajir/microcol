package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.microcol.gui.screen.nativesettlements.NativeSettlementsDialog;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.NativeSettlement;
import org.microcol.model.NativeVisitAction;
import org.testfx.util.WaitForAsyncUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Challenging a rival mission through the real chooser and its confirmation
 * (R-0165). The initial position is a current-schema fixture derived from a
 * saved scenario: a Dutch missionary stands beside the Aztec capital, a French
 * missionary already holds the capital's mission, the natives are angry toward
 * both nations, and the saved randomness is chosen by the caller. The page
 * reads the chooser, the confirmation, the result and the settlement details;
 * the model state is read only to verify it, never changed here.
 */
public final class NativeContestPage extends AbstractScreen {

    private static final String DUTCH = "Dutch";

    private static final String FRANCE = "France";

    private static final String CONTENT_ID = "nativeVisitContent";

    private static final String MISSION_BUTTON_ID = "nativeVisitMission";

    private static final String CONTEST_BUTTON_ID = "nativeVisitContest";

    private static final String CHOICE_ID_PREFIX = "nativeVisitChoice_";

    private static final String CONTEST_CONTENT_ID = "nativeContestContent";

    private static final String CONTEST_LINE_ID = "nativeContestLine";

    private static final String CONTEST_CONFIRM_ID = "nativeContestConfirm";

    private static final String CONTEST_BACK_ID = "nativeContestBack";

    /** Current-schema id of the visiting Dutch missionary. */
    private static final int VISITOR_ID = 310;

    /** Current-schema id of the French missionary that holds the mission. */
    private static final int RESIDENT_ID = 311;

    /** Identity of the Aztec capital in the saved scenario. */
    private static final int CAPITAL_ID = 0;

    public NativeContestPage(final TestContext context) {
        super(context);
    }

    /**
     * Smallest saved state whose first draw wins, or loses, a contest at the
     * given chance. The contest is the first draw of the scenario, because the
     * game draws nothing before the confirmation.
     *
     * @param win    whether the challenger should win
     * @param chance the challenger's chance of winning, from the terms
     * @return the saved randomness state for the first draw
     */
    public static long stateWithOutcome(final boolean win,
            final double chance) {
        for (long state = 1;; state++) {
            if ((Model.combatRandomAfter(state) < chance) == win) {
                return state;
            }
        }
    }

    /**
     * Load the scenario with a Dutch ordinary missionary beside the Aztec
     * capital and a French ordinary missionary stationed at its mission, which
     * has the given count of nonhostile updates. Both nations hold angry
     * natives at 50, so both missionaries have strength 1.5 and the chance is
     * one half.
     *
     * @param source          the current-schema scenario to copy
     * @param combatState     saved randomness, see {@link #stateWithOutcome}
     * @param residentUpdates nonhostile updates the French mission has counted
     */
    public void loadContestFixture(final File source, final long combatState,
            final int residentUpdates) throws Exception {
        final Gson gson = new Gson();
        final JsonObject game = gson.fromJson(Files.readString(source.toPath()),
                JsonObject.class);
        final JsonObject model = game.getAsJsonObject("model");
        model.addProperty("combatRandomState", combatState);
        final JsonArray units = model.getAsJsonArray("units");
        units.add(onMap(VISITOR_ID, DUTCH, Location.of(22, 14)));
        units.add(stationed(RESIDENT_ID, FRANCE));
        for (final var entry : model.getAsJsonArray("nativeTribalAnger")) {
            final JsonObject anger = entry.getAsJsonObject();
            if ("AZTEC".equals(anger.get("nation").getAsString()) && ("DUTCH"
                    .equals(anger.get("european").getAsString())
                    || "FRENCH".equals(anger.get("european").getAsString()))) {
                anger.addProperty("value", 50.0);
            }
        }
        for (final var entry : model.getAsJsonArray("nativeSettlements")) {
            final JsonObject settlement = entry.getAsJsonObject();
            if (settlement.get("id").getAsInt() == CAPITAL_ID) {
                settlement.addProperty("missionaryId", RESIDENT_ID);
                settlement.addProperty("missionUpdates", residentUpdates);
            }
        }
        final Path fixture = Files.createTempFile("native-contest-",
                ".microcol");
        try {
            Files.writeString(fixture, gson.toJson(game));
            new AttackPage(getContext()).replaceGame(fixture.toFile());
        } finally {
            Files.deleteIfExists(fixture);
        }
    }

    private static JsonObject ordinary(final int id, final String owner) {
        final JsonObject cargo = new JsonObject();
        cargo.add("slots", new JsonArray());
        final JsonObject action = new JsonObject();
        action.addProperty("type", "noAction");
        final JsonObject unit = new JsonObject();
        unit.addProperty("id", id);
        unit.addProperty("type", "COLONIST");
        unit.addProperty("ownerId", owner);
        unit.add("cargo", cargo);
        unit.add("action", action);
        unit.addProperty("movementThirds", 3);
        unit.addProperty("tools", 0);
        unit.addProperty("holdingGuns", false);
        unit.addProperty("mounted", false);
        unit.addProperty("workUpdates", 0);
        unit.addProperty("missionary", true);
        return unit;
    }

    private static JsonObject onMap(final int id, final String owner,
            final Location at) {
        final JsonObject location = new JsonObject();
        location.addProperty("x", at.getX());
        location.addProperty("y", at.getY());
        final JsonObject place = new JsonObject();
        place.add("location", location);
        place.addProperty("orientation", "west");
        final JsonObject unit = ordinary(id, owner);
        unit.add("placeMap", place);
        return unit;
    }

    /** A missionary stationed at the capital's mission, off the map. */
    private static JsonObject stationed(final int id, final String owner) {
        final JsonObject place = new JsonObject();
        place.addProperty("settlementId", CAPITAL_ID);
        final JsonObject unit = ordinary(id, owner);
        unit.add("placeNativeMission", place);
        return unit;
    }

    /** Open the contest from the chooser; nothing is challenged yet. */
    public void startContestChoice() {
        getRobot().clickOn(contestButton());
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Confirm the challenge: the model draws once and the result follows. */
    public void confirmContest() {
        getRobot().clickOn(contestDialogButton(CONTEST_CONFIRM_ID));
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Back out of the confirmation; nothing changes. */
    public void backFromContest() {
        getRobot().clickOn(contestDialogButton(CONTEST_BACK_ID));
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Dismiss the reported result of the contest. */
    public void closeContestResult() {
        DialogMessagePage.of(getContext()).close();
    }

    private javafx.scene.control.Button contestButton() {
        return getNodeFinder()
                .lookup("#" + CONTENT_ID + " #" + CONTEST_BUTTON_ID)
                .queryButton();
    }

    private javafx.scene.control.Button contestDialogButton(final String id) {
        return getNodeFinder().lookup("#" + CONTEST_CONTENT_ID + " #" + id)
                .queryButton();
    }

    /** The chooser offers the challenge to this missionary now. */
    public void verifyContestButtonShown() {
        assertTrue(isCssIdShown(CONTEST_BUTTON_ID));
    }

    /** A rival mission is never offered as an establishment. */
    public void verifyMissionButtonHidden() {
        assertFalse(isCssIdShown(MISSION_BUTTON_ID));
    }

    /**
     * @param expected fragment of the mission choice line, such as the rival
     *                 mission that may be challenged
     */
    public void verifyContestChoiceLimit(final String expected) {
        assertTrue(getLabeledById(
                CHOICE_ID_PREFIX + NativeVisitAction.MISSION.name()).getText()
                .contains(expected));
    }

    /**
     * The confirmation names the rival, both missionaries' expertise, the
     * natives' attitude toward each side and the exact chance before anything
     * is drawn.
     *
     * @param expected fragment that the confirmation must contain
     */
    public void verifyContestConfirmation(final String expected) {
        assertTrue(
                getLabeledById(CONTEST_LINE_ID).getText().contains(expected));
    }

    /**
     * The confirmation window stays inside the given size, so its line wraps
     * instead of widening the window; the whole text and both buttons remain.
     *
     * @param maxWidth  widest allowed window
     * @param maxHeight tallest allowed window
     */
    public void verifyContestFits(final int maxWidth, final int maxHeight) {
        final javafx.stage.Window window = contestDialogButton(
                CONTEST_CONFIRM_ID).getScene().getWindow();
        assertTrue(window.getWidth() <= maxWidth,
                "Contest confirmation width " + window.getWidth());
        assertTrue(window.getHeight() <= maxHeight,
                "Contest confirmation height " + window.getHeight());
    }

    public void captureContest(final String fileName) throws Exception {
        captureDialog(CONTEST_CONTENT_ID, fileName);
    }

    /** The confirmation has closed; the chooser is not shown behind it. */
    public void verifyContestClosed() {
        assertFalse(isCssIdVisible(CONTEST_CONTENT_ID));
    }

    /**
     * @param expected fragment of the reported result of the contest
     */
    public void verifyContestResultMessage(final String expected)
            throws Exception {
        DialogMessagePage.of(getContext()).verifyDialogTextContains(expected);
    }

    /** Back out left both missionaries and the mission as they were. */
    public void verifyContestNotStarted(final int visitorId,
            final int residentId, final Location settlement,
            final int residentUpdates) throws Exception {
        verifyStandsOnMap(visitorId);
        verifyMissionHeldBy(residentId, settlement, residentUpdates);
    }

    /**
     * The visiting missionary stands beside the settlement, on the map.
     *
     * @param unitId identity of the missionary
     */
    public void verifyStandsOnMap(final int unitId) {
        assertTrue(getModel().getUnitById(unitId).isAtPlaceLocation());
    }

    /**
     * The missionary is gone from the game: it died in the contest.
     *
     * @param unitId identity of the missionary that must be removed
     */
    public void verifyMissionaryDied(final int unitId) throws Exception {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> getModel().tryGetUnitById(unitId).isEmpty());
        assertTrue(getModel().tryGetUnitById(unitId).isEmpty());
    }

    /**
     * The settlement's mission belongs to this missionary, which stands at the
     * mission off the map with its counted progress.
     *
     * @param unitId     identity of the missionary holding the mission
     * @param settlement the settlement that holds the mission
     * @param updates    nonhostile updates counted in the mission
     */
    public void verifyMissionHeldBy(final int unitId, final Location settlement,
            final int updates) throws Exception {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> getModel().getUnitById(unitId).isAtPlaceNativeMission());
        final NativeSettlement site = knownSettlement(settlement);
        assertEquals(unitId, site.getMissionaryId().orElseThrow().intValue());
        assertEquals(updates, site.getMissionUpdates());
        assertFalse(getModel().getUnitById(unitId).isAtPlaceLocation());
    }

    /**
     * @return the saved randomness state now; it moves only by confirmed draws
     */
    public long combatRandomState() {
        return getModel().save().getCombatRandomState();
    }

    /**
     * @param expected the randomness state that must still be saved
     */
    public void verifyCombatRandomState(final long expected) {
        assertEquals(expected, combatRandomState());
    }

    /**
     * The settlement details show the mission line; after a contest it names
     * the holder of the mission.
     *
     * @param expected fragment that the mission line must contain
     */
    public void verifySettlementMission(final String expected) {
        assertTrue(getLabeledById(NativeSettlementsDialog.MISSION_ID).getText()
                .contains(expected));
    }

    private NativeSettlement knownSettlement(final Location settlement) {
        return getModel()
                .getNativeSettlementKnownTo(getModel().getPlayerByName(DUTCH),
                        settlement)
                .orElseThrow(() -> new AssertionError(
                        "No native settlement known at " + settlement));
    }

}
