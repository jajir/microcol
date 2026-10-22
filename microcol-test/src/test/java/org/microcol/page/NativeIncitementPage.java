package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.microcol.model.EuropeanNation;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.NativeIncitementOrder;
import org.microcol.model.NativeNation;
import org.microcol.model.NativeVisitAction;
import org.testfx.util.WaitForAsyncUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;

/**
 * Paid incitement of a native tribe through the real chooser, its target picker
 * and its confirmation (R-0166). The initial position is a current-schema
 * fixture derived from a saved scenario: a Dutch ordinary missionary stands
 * beside the Aztec capital, the Dutch hold 1000 gold and the Aztecs are at
 * anger 50 toward the Dutch, so the price is 750 gold and the chance 25%. The
 * pursuit fixture adds a known French colonist three squares from an Aztec
 * brave, and tribal anger of 100 toward France, so that the peaceful decay of
 * the first turns cannot end the order. The page reads the chooser, the picker,
 * the terms, the result and the model state, but the model is changed only by
 * the player's own clicks.
 */
public final class NativeIncitementPage extends AbstractScreen {

    private static final String DUTCH = "Dutch";

    private static final String FRANCE = "France";

    private static final String CONTENT_ID = "nativeVisitContent";

    private static final String INCITE_BUTTON_ID = "nativeVisitIncite";

    private static final String CHOICE_ID_PREFIX = "nativeVisitChoice_";

    private static final String PICKER_CONTENT_ID = "nativeIncitementContent";

    private static final String TARGET_ID = "nativeIncitementTarget";

    private static final String TERMS_ID = "nativeIncitementTerms";

    private static final String CONFIRM_ID = "nativeIncitementConfirm";

    private static final String BACK_ID = "nativeIncitementBack";

    /** Current-schema id of the visiting Dutch missionary. */
    public static final int VISITOR_ID = 320;

    /** Gold the Dutch hold in the fixture, so the debit is exact. */
    public static final int FIXTURE_GOLD = 1000;

    /** Price of the incitement in the fixture: 50 * 150 / 10 at anger 50. */
    public static final int PRICE = 750;

    /** Chance of an ordinary missionary, as a percentage. */
    public static final int CHANCE_PERCENT = 25;

    /** Tribal anger of the Aztecs toward the target before an incitement. */
    public static final double FIXTURE_ANGER = 50;

    /** Homed Aztec brave of the saved scenario, on a hill beside the colony. */
    public static final int BRAVE_ID = 200;

    /** Square of that brave before any native turn. */
    public static final Location BRAVE_AT = Location.of(23, 16);

    /** Sentry Aztec lookout that makes the French colonist known. */
    private static final int LOOKOUT_ID = 321;

    private static final Location LOOKOUT_AT = Location.of(21, 19);

    /** French colonist three squares from the brave, known to the Aztecs. */
    private static final int FRENCH_ID = 322;

    /** Square of the French colonist, beyond the two-square raid radius. */
    public static final Location FRENCH_AT = Location.of(22, 19);

    /** Tribal anger of the Aztecs toward France in the pursuit fixture. */
    private static final double PURSUIT_FRENCH_ANGER = 100;

    /**
     * Smallest saved state whose first draw wins, or loses, an ordinary
     * incitement. Nothing draws before the confirmed attempt, so it is the
     * first.
     *
     * @param success whether the incitement should succeed
     * @return the saved randomness state for the first draw
     */
    public static long stateWithOutcome(final boolean success) {
        for (long state = 1;; state++) {
            if ((Model.combatRandomAfter(state) < CHANCE_PERCENT
                    / 100.0) == success) {
                return state;
            }
        }
    }

    public NativeIncitementPage(final TestContext context) {
        super(context);
    }

    /**
     * Load the scenario with the Dutch missionary beside the capital, the Dutch
     * purse set to the fixture gold and the saved randomness given.
     *
     * @param source      the current-schema scenario to copy
     * @param combatState saved randomness, see {@link #stateWithOutcome}
     */
    public void loadIncitementFixture(final File source, final long combatState)
            throws Exception {
        replaceGame(prepared(source, combatState, false));
    }

    /**
     * The same position as {@link #loadIncitementFixture}, with tribal anger of
     * 100 toward France, a sentry Aztec lookout and a French colonist that the
     * lookout makes known. The colonist stands three squares from the Aztec
     * brave, so only the paid order can send the brave after it.
     *
     * @param source      the current-schema scenario to copy
     * @param combatState saved randomness, see {@link #stateWithOutcome}
     */
    public void loadPursuitFixture(final File source, final long combatState)
            throws Exception {
        replaceGame(prepared(source, combatState, true));
    }

    private JsonObject prepared(final File source, final long combatState,
            final boolean pursuit) throws Exception {
        final Gson gson = new Gson();
        final JsonObject game = gson.fromJson(Files.readString(source.toPath()),
                JsonObject.class);
        final JsonObject model = game.getAsJsonObject("model");
        model.addProperty("combatRandomState", combatState);
        final JsonArray units = model.getAsJsonArray("units");
        units.add(colonist(VISITOR_ID, DUTCH, Location.of(22, 14), true));
        if (pursuit) {
            units.add(lookout(LOOKOUT_ID, LOOKOUT_AT));
            units.add(colonist(FRENCH_ID, FRANCE, FRENCH_AT, false));
        }
        for (final var entry : model.getAsJsonArray("nativeTribalAnger")) {
            final JsonObject anger = entry.getAsJsonObject();
            if (!"AZTEC".equals(anger.get("nation").getAsString())) {
                continue;
            }
            if ("DUTCH".equals(anger.get("european").getAsString())) {
                anger.addProperty("value", FIXTURE_ANGER);
            } else if (pursuit
                    && "FRENCH".equals(anger.get("european").getAsString())) {
                anger.addProperty("value", PURSUIT_FRENCH_ANGER);
            }
        }
        for (final var entry : model.getAsJsonObject("gameManager")
                .getAsJsonArray("players")) {
            final JsonObject player = entry.getAsJsonObject();
            if (DUTCH.equals(player.get("name").getAsString())) {
                player.addProperty("gold", FIXTURE_GOLD);
            }
        }
        return game;
    }

    private void replaceGame(final JsonObject game) throws Exception {
        final Path fixture = Files.createTempFile("native-incite-",
                ".microcol");
        try {
            Files.writeString(fixture, new Gson().toJson(game));
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

    private static JsonObject colonist(final int id, final String owner,
            final Location at, final boolean missionary) {
        final JsonObject unit = unit(id, "COLONIST", owner, at);
        unit.addProperty("workUpdates", 0);
        unit.addProperty("missionary", missionary);
        return unit;
    }

    /**
     * A homeless Aztec brave on a sentry order: it makes its neighbors known to
     * the Aztecs, but it can neither move nor attack while the order stands.
     */
    private static JsonObject lookout(final int id, final Location at) {
        final JsonObject unit = unit(id, "BRAVE", "AZTEC", at);
        final JsonObject sentry = new JsonObject();
        sentry.addProperty("type", "sentry");
        unit.add("action", sentry);
        unit.addProperty("nativeHomeless", true);
        unit.addProperty("nativeRole", "BRAVE");
        return unit;
    }

    /** Open the incitement from the chooser; nothing is paid yet. */
    public void startIncitementChoice() {
        getRobot().clickOn(incitementButton());
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Choose the target power in the picker by its displayed name.
     *
     * @param name displayed name of a selectable power
     */
    public void selectTarget(final String name) {
        final ComboBox<EuropeanNation> combo = targetCombo();
        final EuropeanNation nation = combo.getItems().stream().filter(
                item -> combo.getConverter().toString(item).equals(name))
                .findFirst().orElseThrow();
        WaitForAsyncUtils.asyncFx(() -> combo.setValue(nation));
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Confirm the shown terms: the model pays once and draws once. */
    public void confirmIncitement() {
        getRobot().clickOn(pickerButton(CONFIRM_ID));
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Back out of the picker; nothing changes. */
    public void backFromIncitement() {
        getRobot().clickOn(pickerButton(BACK_ID));
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Dismiss the reported result of the incitement. */
    public void closeIncitementResult() {
        DialogMessagePage.of(getContext()).close();
    }

    private Button incitementButton() {
        return getNodeFinder()
                .lookup("#" + CONTENT_ID + " #" + INCITE_BUTTON_ID)
                .queryButton();
    }

    private Button pickerButton(final String id) {
        return getNodeFinder().lookup("#" + PICKER_CONTENT_ID + " #" + id)
                .queryButton();
    }

    @SuppressWarnings("unchecked")
    private ComboBox<EuropeanNation> targetCombo() {
        return (ComboBox<EuropeanNation>) getNodeFinder()
                .lookup("#" + PICKER_CONTENT_ID + " #" + TARGET_ID).query();
    }

    /** The chooser offers the incitement to this missionary now. */
    public void verifyIncitementButtonShown() {
        assertTrue(isCssIdShown(INCITE_BUTTON_ID));
    }

    /**
     * @param expected fragment of the incitement choice line
     */
    public void verifyIncitementChoiceLimit(final String expected) {
        assertTrue(getLabeledById(
                CHOICE_ID_PREFIX + NativeVisitAction.INCITE.name()).getText()
                .contains(expected));
    }

    /**
     * @param name a power that the picker must offer
     */
    public void verifyTargetOffered(final String name) {
        assertTrue(targetNames().contains(name), "Offered: " + targetNames());
    }

    /**
     * @param name a power that the picker must not offer, such as the visitor's
     *             own nation
     */
    public void verifyTargetNotOffered(final String name) {
        assertFalse(targetNames().contains(name), "Offered: " + targetNames());
    }

    private List<String> targetNames() {
        final ComboBox<EuropeanNation> combo = targetCombo();
        return combo.getItems().stream()
                .map(item -> combo.getConverter().toString(item))
                .collect(Collectors.toList());
    }

    /**
     * @param expected fragment of the terms: the price, the chance and the gold
     */
    public void verifyTerms(final String expected) {
        assertTrue(getLabeledById(TERMS_ID).getText().contains(expected));
    }

    /**
     * The picker stays inside the given size, so its terms wrap instead of
     * widening the window; the whole text and both buttons remain.
     *
     * @param maxWidth  widest allowed window
     * @param maxHeight tallest allowed window
     */
    public void verifyIncitementFits(final int maxWidth, final int maxHeight) {
        final javafx.stage.Window window = pickerButton(CONFIRM_ID).getScene()
                .getWindow();
        assertTrue(window.getWidth() <= maxWidth,
                "Incitement picker width " + window.getWidth());
        assertTrue(window.getHeight() <= maxHeight,
                "Incitement picker height " + window.getHeight());
    }

    public void captureIncitement(final String fileName) throws Exception {
        captureDialog(PICKER_CONTENT_ID, fileName);
    }

    /**
     * @param expected fragment of the reported result of the incitement
     */
    public void verifyIncitementResultMessage(final String expected)
            throws Exception {
        DialogMessagePage.of(getContext()).verifyDialogTextContains(expected);
    }

    /**
     * The picker has closed; the chooser is not shown behind it.
     */
    public void verifyPickerClosed() {
        assertFalse(isCssIdVisible(PICKER_CONTENT_ID));
    }

    /**
     * @param expected the gold the Dutch must hold now
     */
    public void verifyGold(final int expected) {
        assertEquals(expected, getModel().getPlayerByName(DUTCH).getGold());
    }

    /**
     * @param expected the randomness state that must still be saved
     */
    public void verifyCombatRandomState(final long expected) {
        assertEquals(expected, getModel().save().getCombatRandomState());
    }

    /**
     * @param owner    the European whose anger is read: the requester or the
     *                 target
     * @param expected exact tribal anger of the Aztecs toward that European
     */
    public void verifyTribalAnger(final String owner, final double expected) {
        assertEquals(expected,
                getModel().getNativeTribalAnger(
                        getModel().getPlayerByName(owner), NativeNation.AZTEC),
                1e-9);
    }

    /**
     * @param owner the European whose anger is read
     * @param floor the lowest tribal anger of the Aztecs toward that European;
     *              75 is the hostile band that keeps an order alive
     */
    public void verifyTribalAngerAtLeast(final String owner,
            final double floor) {
        assertTrue(getModel().getNativeTribalAnger(
                getModel().getPlayerByName(owner), NativeNation.AZTEC) >= floor,
                "Anger toward " + owner + " fell below " + floor);
    }

    /**
     * @param target the power the Aztecs pursue
     * @param turns  native turns that the order has left
     */
    public void verifyOrder(final EuropeanNation target, final int turns)
            throws Exception {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> getModel()
                .getNativeIncitementOrder(NativeNation.AZTEC).isPresent());
        final NativeIncitementOrder order = getModel()
                .getNativeIncitementOrder(NativeNation.AZTEC).orElseThrow();
        assertEquals(target, order.getTarget());
        assertEquals(turns, order.getRemainingTurns());
    }

    /** No tribe pursues a power: the attempt installed nothing. */
    public void verifyNoOrder() {
        assertTrue(getModel().getNativeIncitementOrder(NativeNation.AZTEC)
                .isEmpty());
    }

    /**
     * The brave's Chebyshev distance to a square once the movement of the last
     * turn has finished. Three squares is beyond the two-square raid radius, so
     * only the paid order explains a brave that closes in on the target.
     *
     * @param braveId  identity of the Aztec brave
     * @param target   the square of the pursued unit
     * @param expected the distance the brave must have now
     */
    public void verifyBraveDistanceTo(final int braveId, final Location target,
            final int expected) throws Exception {
        GamePage.of(getContext()).waitWhileMoving();
        assertEquals(expected, getModel().getUnitById(braveId).getLocation()
                .getDistanceChebyshev(target));
    }

    /**
     * The brave still stands on the square the scenario gave it.
     *
     * @param braveId  identity of the Aztec brave
     * @param expected the square it stands on
     */
    public void verifyBraveAt(final int braveId, final Location expected) {
        assertEquals(expected, getModel().getUnitById(braveId).getLocation());
    }

}
