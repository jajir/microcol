package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.microcol.gui.util.ButtonBarOk;
import org.microcol.model.EuropeanNation;
import org.microcol.model.Location;
import org.microcol.model.NativeNation;
import org.microcol.model.NativeSettlement;
import org.microcol.model.NativeVisitAction;
import org.microcol.model.Player;
import org.testfx.util.WaitForAsyncUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javafx.scene.control.Button;

/**
 * Tribute demand on a native settlement through the real chooser, its
 * confirmation and its result message (R-0162). The initial position is a
 * current-schema fixture derived from a saved scenario: Dutch soldiers stand
 * beside the Aztec capital, native braves defend its square, and the tribe is
 * at anger zero toward the Dutch. The capital's defense bonus is 75 percent, so
 * each brave has strength 1.75; the Dutch soldiers have strength 2 each. The
 * saved combat randomness is fixed, so a demand that draws would be visible.
 * The page reads the chooser, the confirmation, the result and the model state;
 * the model is changed only by the player's own clicks.
 */
public final class NativeTributePage extends AbstractScreen {

    private static final String DUTCH = "Dutch";

    private static final String CONTENT_ID = "nativeVisitContent";

    private static final String TRIBUTE_BUTTON_ID = "nativeVisitTribute";

    private static final String CHOICE_ID_PREFIX = "nativeVisitChoice_";

    private static final String TRIBUTE_CONTENT_ID = "nativeTributeContent";

    private static final String LINE_ID = "nativeTributeLine";

    private static final String FORCE_ID = "nativeTributeForce";

    private static final String DEFENSE_ID = "nativeTributeDefense";

    private static final String OUTCOME_ID = "nativeTributeOutcome";

    private static final String RELATIONS_ID = "nativeTributeRelations";

    private static final String CONFIRM_ID = "nativeTributeConfirm";

    private static final String BACK_ID = "nativeTributeBack";

    /** Dutch soldier that demands tribute beside the Aztec capital. */
    public static final int SOLDIER_ID = 330;

    /** Second Dutch soldier beside the capital, adds force when present. */
    public static final int SECOND_SOLDIER_ID = 334;

    /** Unarmed Dutch colonist, which may not demand tribute. */
    public static final int COLONIST_ID = 335;

    /** First of the Aztec braves on the capital square. */
    private static final int FIRST_DEFENDER_ID = 331;

    /** Stable ID of the Aztec capital in the saved scenario. */
    private static final int CAPITAL_ID = 0;

    private static final Location CAPITAL = Location.of(23, 15);

    private static final Location SOLDIER_AT = Location.of(23, 14);

    private static final Location SECOND_SOLDIER_AT = Location.of(22, 14);

    private static final Location COLONIST_AT = Location.of(22, 15);

    /** Saved combat randomness; a tribute demand never draws, so it stays. */
    private static final long COMBAT_STATE = 4242L;

    /** Relation step of one demand, before the national factors. */
    private static final double RELATION_STEP = 10;

    private final Gson gson = new Gson();

    private JsonElement remembered;

    private int goldBefore;

    private int treasuryBefore;

    private double angerBefore;

    private double alarmBefore;

    public NativeTributePage(final TestContext context) {
        super(context);
    }

    /**
     * Load the scenario with the Dutch soldiers beside the Aztec capital and
     * the given number of braves on its square. The Dutch may hold one saved
     * demand of the capital from the given turn, and the game turn is the given
     * one.
     *
     * @param source     the current-schema scenario to copy
     * @param soldiers   armed Dutch soldiers beside the capital, one or two
     * @param defenders  braves that defend the capital square
     * @param treasury   gold the capital holds
     * @param turn       game turn of the saved position
     * @param lastDemand turn of the Dutch demand already made here, or null
     */
    public void loadTributeFixture(final File source, final int soldiers,
            final int defenders, final int treasury, final int turn,
            final Integer lastDemand) throws Exception {
        final JsonObject game = gson.fromJson(Files.readString(source.toPath()),
                JsonObject.class);
        final JsonObject model = game.getAsJsonObject("model");
        model.addProperty("combatRandomState", COMBAT_STATE);
        model.getAsJsonObject("calendar").addProperty("numberOfPlayedTurns",
                turn);
        final JsonArray units = model.getAsJsonArray("units");
        units.add(colonist(SOLDIER_ID, SOLDIER_AT, true));
        if (soldiers > 1) {
            units.add(colonist(SECOND_SOLDIER_ID, SECOND_SOLDIER_AT, true));
        }
        for (int index = 0; index < defenders; index++) {
            units.add(defender(FIRST_DEFENDER_ID + index));
        }
        setCapital(model, treasury, lastDemand);
        zeroAnger(model);
        replace(game);
    }

    /**
     * Load the scenario with an unarmed Dutch colonist beside the capital and
     * two braves on its square; the colonist has no demand.
     *
     * @param source the current-schema scenario to copy
     */
    public void loadUnarmedFixture(final File source) throws Exception {
        final JsonObject game = gson.fromJson(Files.readString(source.toPath()),
                JsonObject.class);
        final JsonObject model = game.getAsJsonObject("model");
        model.addProperty("combatRandomState", COMBAT_STATE);
        final JsonArray units = model.getAsJsonArray("units");
        units.add(colonist(COLONIST_ID, COLONIST_AT, false));
        units.add(defender(FIRST_DEFENDER_ID));
        units.add(defender(FIRST_DEFENDER_ID + 1));
        setCapital(model, 2000, null);
        zeroAnger(model);
        replace(game);
    }

    private static void setCapital(final JsonObject model, final int treasury,
            final Integer lastDemand) {
        for (final JsonElement element : model
                .getAsJsonArray("nativeSettlements")) {
            final JsonObject settlement = element.getAsJsonObject();
            if (settlement.get("id").getAsInt() != CAPITAL_ID) {
                continue;
            }
            settlement.addProperty("treasury", treasury);
            if (lastDemand != null) {
                final JsonObject record = new JsonObject();
                record.addProperty("european", "DUTCH");
                record.addProperty("turn", lastDemand);
                final JsonArray tributes = new JsonArray();
                tributes.add(record);
                settlement.add("tributes", tributes);
            }
        }
    }

    private static void zeroAnger(final JsonObject model) {
        for (final JsonElement entry : model
                .getAsJsonArray("nativeTribalAnger")) {
            final JsonObject anger = entry.getAsJsonObject();
            if ("AZTEC".equals(anger.get("nation").getAsString())
                    && "DUTCH".equals(anger.get("european").getAsString())) {
                anger.addProperty("value", 0.0);
            }
        }
    }

    private void replace(final JsonObject game) throws Exception {
        final Path fixture = Files.createTempFile("native-tribute-",
                ".microcol");
        try {
            Files.writeString(fixture, gson.toJson(game));
            new AttackPage(getContext()).replaceGame(fixture.toFile());
        } finally {
            Files.deleteIfExists(fixture);
        }
    }

    private static JsonObject unit(final int id, final String type,
            final String owner, final Location at) {
        final JsonObject location = new JsonObject();
        location.addProperty("x", at.getX());
        location.addProperty("y", at.getY());
        final JsonObject place = new JsonObject();
        place.add("location", location);
        place.addProperty("orientation", "west");
        final JsonObject cargo = new JsonObject();
        cargo.add("slots", new JsonArray());
        final JsonObject action = new JsonObject();
        action.addProperty("type", "noAction");
        final JsonObject unit = new JsonObject();
        unit.addProperty("id", id);
        unit.addProperty("type", type);
        unit.addProperty("ownerId", owner);
        unit.add("cargo", cargo);
        unit.add("action", action);
        unit.add("placeMap", place);
        unit.addProperty("movementThirds", 3);
        unit.addProperty("tools", 0);
        unit.addProperty("holdingGuns", false);
        unit.addProperty("mounted", false);
        return unit;
    }

    private static JsonObject colonist(final int id, final Location at,
            final boolean guns) {
        final JsonObject unit = unit(id, "COLONIST", DUTCH, at);
        unit.addProperty("workUpdates", 0);
        unit.addProperty("missionary", false);
        unit.addProperty("holdingGuns", guns);
        return unit;
    }

    /** An Aztec brave of the capital, standing on the capital's own square. */
    private static JsonObject defender(final int id) {
        final JsonObject unit = unit(id, "BRAVE", "AZTEC", CAPITAL);
        unit.addProperty("nativeHomeSettlementId", CAPITAL_ID);
        unit.addProperty("nativeHomeless", false);
        unit.addProperty("nativeRole", "BRAVE");
        return unit;
    }

    /**
     * Remember the whole saved world, to compare it after a backed-out action.
     */
    public void rememberWorld() {
        getRobot().interact(() -> remembered = gameState());
    }

    /**
     * Remember the Dutch gold, the capital's treasury, and the anger and alarm
     * before a demand, so the exact change can be checked afterwards.
     */
    public void rememberPurse() {
        goldBefore = dutch().getGold();
        treasuryBefore = treasury();
        angerBefore = anger();
        alarmBefore = alarm();
    }

    /**
     * The whole saved world except the map tile the interface has focused.
     * Selecting the visitor moves that focus; it is presentation, not game
     * state, so it is the one saved field the comparison leaves out.
     */
    private JsonElement gameState() {
        final JsonObject world = gson.toJsonTree(getModel().save())
                .getAsJsonObject();
        world.remove("focusedField");
        return world;
    }

    /** Open the demand confirmation from the chooser; nothing is paid. */
    public void startTributeChoice() {
        getRobot().clickOn(tributeButton());
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Confirm the shown terms: the model moves the gold and records the demand.
     */
    public void confirmTribute() {
        getRobot().clickOn(confirmationButton(CONFIRM_ID));
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Back out of the confirmation; nothing changes. */
    public void backFromTribute() {
        getRobot().clickOn(confirmationButton(BACK_ID));
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Dismiss the reported result of the demand. */
    public void closeTributeResult() {
        DialogMessagePage.of(getContext()).close();
    }

    public void captureTribute(final String fileName) throws Exception {
        captureDialog(TRIBUTE_CONTENT_ID, fileName);
    }

    /** Capture the open result message. */
    public void captureResult(final String fileName) throws Exception {
        captureDialog(ButtonBarOk.BUTTON_OK_ID, fileName);
    }

    private Button tributeButton() {
        return getNodeFinder()
                .lookup("#" + CONTENT_ID + " #" + TRIBUTE_BUTTON_ID)
                .queryButton();
    }

    private Button confirmationButton(final String id) {
        return getNodeFinder().lookup("#" + TRIBUTE_CONTENT_ID + " #" + id)
                .queryButton();
    }

    /** The chooser offers the demand to this visitor now. */
    public void verifyTributeButtonShown() {
        assertTrue(isCssIdShown(TRIBUTE_BUTTON_ID));
    }

    /**
     * A refused or waiting visitor has no demand button, only the reason line.
     * The chooser keeps the button node but hides it, so visibility is checked.
     */
    public void verifyTributeButtonHidden() {
        assertTrue(isCssIdVisible(CONTENT_ID), "Chooser is not open");
        assertFalse(isCssIdShown(TRIBUTE_BUTTON_ID));
    }

    /**
     * @param expected the whole tribute line of the chooser, action and limit
     */
    public void verifyTributeChoiceLine(final String expected) {
        assertEquals(expected,
                getLabeledById(
                        CHOICE_ID_PREFIX + NativeVisitAction.TRIBUTE.name())
                        .getText());
    }

    /**
     * @param expected fragment of the confirmation line about the settlement
     */
    public void verifyTributeLine(final String expected) {
        assertTrue(getLabeledById(LINE_ID).getText().contains(expected),
                getLabeledById(LINE_ID).getText());
    }

    /**
     * @param expected fragment of the force line: the force, and the defenders
     */
    public void verifyTributeForce(final String expected) {
        assertTrue(getLabeledById(FORCE_ID).getText().contains(expected),
                getLabeledById(FORCE_ID).getText());
    }

    /**
     * @param expected fragment of the defense line: the settlement's bonus
     */
    public void verifyTributeDefense(final String expected) {
        assertTrue(getLabeledById(DEFENSE_ID).getText().contains(expected),
                getLabeledById(DEFENSE_ID).getText());
    }

    /**
     * @param expected fragment of the payment or refusal the model quoted
     */
    public void verifyTributeOutcome(final String expected) {
        assertTrue(getLabeledById(OUTCOME_ID).getText().contains(expected),
                getLabeledById(OUTCOME_ID).getText());
    }

    /**
     * @param expected fragment of the relation consequence line
     */
    public void verifyTributeRelations(final String expected) {
        assertTrue(getLabeledById(RELATIONS_ID).getText().contains(expected),
                getLabeledById(RELATIONS_ID).getText());
    }

    /**
     * The confirmation stays inside the given size, so its lines wrap instead
     * of widening the window; the whole text and both buttons remain.
     *
     * @param maxWidth  widest allowed window
     * @param maxHeight tallest allowed window
     */
    public void verifyTributeFits(final int maxWidth, final int maxHeight) {
        final javafx.stage.Window window = confirmationButton(CONFIRM_ID)
                .getScene().getWindow();
        assertTrue(window.getWidth() <= maxWidth,
                "Tribute confirmation width " + window.getWidth());
        assertTrue(window.getHeight() <= maxHeight,
                "Tribute confirmation height " + window.getHeight());
    }

    /** The confirmation has closed. */
    public void verifyTributeClosed() {
        assertFalse(isCssIdVisible(TRIBUTE_CONTENT_ID));
    }

    /**
     * @param expected fragment of the reported result of the demand
     */
    public void verifyResultMessage(final String expected) throws Exception {
        DialogMessagePage.of(getContext()).verifyDialogTextContains(expected);
    }

    /**
     * The Dutch gained exactly the paid gold and the capital lost the same
     * amount, so gold is conserved; the change is exact after a save and load.
     *
     * @param gold gold paid by the capital, zero for an empty or refused demand
     */
    public void verifyPaidExactly(final int gold) {
        assertEquals(goldBefore + gold, dutch().getGold());
        assertEquals(treasuryBefore - gold, treasury());
    }

    /** A refused demand moved neither gold. */
    public void verifyRefusedNothingPaid() {
        assertEquals(goldBefore, dutch().getGold());
        assertEquals(treasuryBefore, treasury());
    }

    /**
     * Tribal anger and local alarm rose by exactly one demand step, each once;
     * the Dutch have no national factor on alarm.
     */
    public void verifyRelationsRaisedByTen() {
        assertEquals(angerBefore + RELATION_STEP, anger(), 1e-9);
        assertEquals(alarmBefore + RELATION_STEP, alarm(), 1e-9);
    }

    /**
     * @param expected turn of the Dutch demand saved at the capital
     */
    public void verifyTributeRecord(final int expected) {
        assertEquals(Optional.of(expected),
                settlement().findTributeTurn(EuropeanNation.DUTCH));
    }

    /** The saved combat randomness is unchanged: no draw was taken. */
    public void verifyCombatRandomStateUnchanged() {
        assertEquals(COMBAT_STATE,
                getModel().save().getCombatRandomState().longValue());
    }

    /**
     * The saved game state equals the remembered one: nothing was changed. Only
     * the interface's focused tile may differ, see {@link #gameState}.
     */
    public void verifyWorldUnchanged() {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(remembered, gameState());
    }

    private Player dutch() {
        return getModel().getPlayerByName(DUTCH);
    }

    private NativeSettlement settlement() {
        return getModel().getNativeSettlementKnownTo(dutch(), CAPITAL)
                .orElseThrow();
    }

    private int treasury() {
        return settlement().getTreasury();
    }

    private double anger() {
        return getModel().getNativeTribalAnger(dutch(), NativeNation.AZTEC);
    }

    private double alarm() {
        return getModel().getNativeLocalAlarm(dutch(), settlement());
    }

}
