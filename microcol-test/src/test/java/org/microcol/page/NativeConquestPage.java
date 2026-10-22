package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.microcol.gui.screen.nativesettlements.NativeSettlementsDialog;
import org.microcol.gui.util.ButtonBarOk;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.NativeNation;
import org.microcol.model.NativeSettlement;
import org.microcol.model.NativeVisitAction;
import org.microcol.model.turnevent.TurnEventNativeConquest;
import org.microcol.model.turnevent.TurnEventNativeConquestLoss;
import org.microcol.model.unit.UnitFreeColonist;
import org.microcol.model.unit.UnitNativeBrave;
import org.testfx.util.WaitForAsyncUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javafx.scene.control.Button;
import javafx.scene.control.ListView;

/**
 * Attack and conquest of a native settlement through the real chooser, its
 * confirmation and its result message (R-0174). The initial position is a
 * current-schema fixture derived from a saved scenario: a Dutch soldier and an
 * unarmed Dutch colonist stand beside the Aztec capital, native braves defend
 * its square, and the tribe is at anger zero toward the Dutch, so the relation
 * numbers after an attack are exact. The saved randomness is chosen so that
 * each draw has a certain result whatever the terrain of the settlement square
 * is. The page reads the chooser, the confirmation, the result and the model
 * state; the model is changed only by the player's own clicks.
 */
public final class NativeConquestPage extends AbstractScreen {

    private static final String DUTCH = "Dutch";

    private static final String CONTENT_ID = "nativeVisitContent";

    private static final String ATTACK_BUTTON_ID = "nativeVisitAttack";

    private static final String CHOICE_ID_PREFIX = "nativeVisitChoice_";

    private static final String ATTACK_CONTENT_ID = "nativeAttackContent";

    private static final String LINE_ID = "nativeAttackLine";

    private static final String DEFENDERS_ID = "nativeAttackDefenders";

    private static final String SPANISH_ID = "nativeAttackSpanish";

    private static final String RELATIONS_ID = "nativeAttackRelations";

    private static final String CONFIRM_ID = "nativeAttackConfirm";

    private static final String BACK_ID = "nativeAttackBack";

    /** Armed Dutch soldier that attacks. */
    public static final int SOLDIER_ID = 330;

    /** First of the Aztec braves on the settlement square. */
    public static final int FIRST_DEFENDER_ID = 331;

    /** Unarmed Dutch colonist, which may not attack. */
    public static final int COLONIST_ID = 333;

    /** Homed Aztec brave of the saved scenario, outside the settlement. */
    public static final int EXTERNAL_BRAVE_ID = 200;

    private static final Location CAPITAL = Location.of(23, 15);

    private static final Location SOLDIER_AT = Location.of(23, 14);

    private static final Location COLONIST_AT = Location.of(22, 15);

    /** Stable ID of the Aztec capital in the saved scenario. */
    private static final int CAPITAL_ID = 0;

    /** The Inca capital that a rival conquered in the loss notice fixture. */
    private static final int LOST_SETTLEMENT_ID = 3;

    private static final Location LOST_SETTLEMENT_AT = Location.of(15, 22);

    /** Identity of the Dutch pupil that died with the Inca capital. */
    private static final int LOST_PUPIL_ID = 340;

    private static final long GOLDEN = 0x9E3779B97F4A7C15L;

    /**
     * A draw below this wins the battle whatever the terrain: the soldier's
     * attack of 3 against the strongest possible defender of 2.75 gives a win
     * chance above one half.
     */
    private static final double CERTAIN_WIN = 0.25;

    /**
     * A draw above this loses the battle: the best win chance is below 0.64.
     */
    private static final double CERTAIN_LOSS = 0.9;

    /** The ordinary promotion roll promotes when it is below this. */
    private static final double PROMOTION = 0.25;

    private final Gson gson = new Gson();

    private JsonElement remembered;

    public NativeConquestPage(final TestContext context) {
        super(context);
    }

    private static double draw(final long state, final int number) {
        return Model.combatRandomAfter(state + (number - 1) * GOLDEN);
    }

    /**
     * Saved randomness whose draws win the first battle, promote the winner,
     * and win the second battle. The second battle uses the third draw, since
     * the second is the promotion roll of the first victory.
     *
     * @return the saved randomness state
     */
    public static long victoryState() {
        for (long state = 1;; state++) {
            if (draw(state, 1) < CERTAIN_WIN && draw(state, 2) < PROMOTION
                    && draw(state, 3) < CERTAIN_WIN) {
                return state;
            }
        }
    }

    /**
     * Saved randomness whose first draw loses the first battle.
     *
     * @return the saved randomness state
     */
    public static long defeatState() {
        for (long state = 1;; state++) {
            if (draw(state, 1) > CERTAIN_LOSS) {
                return state;
            }
        }
    }

    /**
     * Load the scenario with the Dutch soldier and the unarmed colonist beside
     * the Aztec capital and the given number of braves on its square.
     *
     * @param source      the current-schema scenario to copy
     * @param combatState saved randomness, see {@link #victoryState}
     * @param defenders   braves that defend the settlement square
     */
    public void loadConquestFixture(final File source, final long combatState,
            final int defenders) throws Exception {
        final JsonObject game = gson.fromJson(Files.readString(source.toPath()),
                JsonObject.class);
        final JsonObject model = game.getAsJsonObject("model");
        model.addProperty("combatRandomState", combatState);
        final JsonArray units = model.getAsJsonArray("units");
        units.add(colonist(SOLDIER_ID, SOLDIER_AT, true));
        units.add(colonist(COLONIST_ID, COLONIST_AT, false));
        for (int index = 0; index < defenders; index++) {
            units.add(defender(FIRST_DEFENDER_ID + index));
        }
        for (final JsonElement entry : model
                .getAsJsonArray("nativeTribalAnger")) {
            final JsonObject anger = entry.getAsJsonObject();
            if ("AZTEC".equals(anger.get("nation").getAsString())
                    && "DUTCH".equals(anger.get("european").getAsString())) {
                anger.addProperty("value", 0.0);
            }
        }
        final Path fixture = Files.createTempFile("native-conquest-",
                ".microcol");
        try {
            Files.writeString(fixture, gson.toJson(game));
            new AttackPage(getContext()).replaceGame(fixture.toFile());
        } finally {
            Files.deleteIfExists(fixture);
        }
    }

    /**
     * Load the scenario as a rival's conquest of the Inca capital left it: the
     * settlement and its alarm rows are gone, its brave lives on without a
     * home, and the Dutch hold one saved notice that their pupil there found no
     * legal square and died. The notice is saved data of the position, in the
     * same schema the model writes; no running model is changed.
     *
     * @param source the current-schema scenario to copy
     */
    public void loadLossNoticeFixture(final File source) throws Exception {
        final JsonObject game = gson.fromJson(Files.readString(source.toPath()),
                JsonObject.class);
        final JsonObject model = game.getAsJsonObject("model");
        removeWhere(model.getAsJsonArray("nativeSettlements"),
                entry -> entry.get("id").getAsInt() == LOST_SETTLEMENT_ID);
        removeWhere(model.getAsJsonArray("nativeLocalAlarm"), entry -> entry
                .get("settlementId").getAsInt() == LOST_SETTLEMENT_ID);
        for (final JsonElement element : model.getAsJsonArray("units")) {
            final JsonObject unit = element.getAsJsonObject();
            if (unit.has("nativeHomeSettlementId")
                    && unit.get("nativeHomeSettlementId")
                            .getAsInt() == LOST_SETTLEMENT_ID) {
                unit.remove("nativeHomeSettlementId");
                unit.addProperty("nativeHomeless", true);
            }
        }
        final JsonObject notice = new JsonObject();
        notice.addProperty("type", "TurnEventNativeConquestLoss");
        notice.addProperty("playerName", DUTCH);
        notice.addProperty("nativeConquestLossUnitId", LOST_PUPIL_ID);
        notice.addProperty("nativeConquestLossUnitType", "COLONIST");
        notice.addProperty("nativeConquestLossPupil", true);
        notice.addProperty("nativeConquestNation", "INCA");
        notice.addProperty("nativeConquestSettlementId", LOST_SETTLEMENT_ID);
        notice.addProperty("nativeConquestX", LOST_SETTLEMENT_AT.getX());
        notice.addProperty("nativeConquestY", LOST_SETTLEMENT_AT.getY());
        model.getAsJsonArray("turnEvents").add(notice);
        final Path fixture = Files.createTempFile("native-conquest-loss-",
                ".microcol");
        try {
            Files.writeString(fixture, gson.toJson(game));
            new AttackPage(getContext()).replaceGame(fixture.toFile());
        } finally {
            Files.deleteIfExists(fixture);
        }
    }

    private static void removeWhere(final JsonArray array,
            final java.util.function.Predicate<JsonObject> condition) {
        for (int index = array.size() - 1; index >= 0; index--) {
            if (condition.test(array.get(index).getAsJsonObject())) {
                array.remove(index);
            }
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

    /** Keep the whole saved world, to compare it after a backed-out action. */
    public void rememberWorld() {
        getRobot().interact(() -> remembered = gameState());
    }

    /**
     * The whole saved world except the map tile the interface has focused.
     * Selecting the visitor moves that focus; it is presentation, not game
     * state, so it is the one saved field the comparison leaves out. Units,
     * randomness, movement, gold, relations and events are all kept.
     */
    private JsonElement gameState() {
        final JsonObject world = gson.toJsonTree(getModel().save())
                .getAsJsonObject();
        world.remove("focusedField");
        return world;
    }

    /** Open the attack confirmation from the chooser; nothing is drawn. */
    public void startAttackChoice() {
        getRobot().clickOn(attackButton());
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Confirm the shown terms: the model fights the defenders. */
    public void confirmAttack() {
        getRobot().clickOn(confirmationButton(CONFIRM_ID));
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Back out of the confirmation; nothing changes. */
    public void backFromAttack() {
        getRobot().clickOn(confirmationButton(BACK_ID));
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Dismiss the reported result of the attack. */
    public void closeAttackResult() {
        DialogMessagePage.of(getContext()).close();
    }

    public void captureAttack(final String fileName) throws Exception {
        captureDialog(ATTACK_CONTENT_ID, fileName);
    }

    /** Capture the open result message. */
    public void captureResult(final String fileName) throws Exception {
        captureDialog(ButtonBarOk.BUTTON_OK_ID, fileName);
    }

    private Button attackButton() {
        return getNodeFinder()
                .lookup("#" + CONTENT_ID + " #" + ATTACK_BUTTON_ID)
                .queryButton();
    }

    private Button confirmationButton(final String id) {
        return getNodeFinder().lookup("#" + ATTACK_CONTENT_ID + " #" + id)
                .queryButton();
    }

    /** The chooser offers the attack to this visitor now. */
    public void verifyAttackButtonShown() {
        assertTrue(isCssIdShown(ATTACK_BUTTON_ID));
    }

    /**
     * A refused visitor has no attack button, only the refusal line. The
     * chooser keeps the button node but hides it, so visibility is checked.
     */
    public void verifyAttackButtonHidden() {
        assertTrue(isCssIdVisible(CONTENT_ID), "Chooser is not open");
        assertFalse(isCssIdShown(ATTACK_BUTTON_ID));
    }

    /**
     * @param expected the whole attack line of the chooser, action and limit
     */
    public void verifyAttackChoiceLine(final String expected) {
        assertEquals(expected,
                getLabeledById(
                        CHOICE_ID_PREFIX + NativeVisitAction.ATTACK.name())
                        .getText());
    }

    /**
     * @param expected fragment of the line that names the settlement
     */
    public void verifyAttackLine(final String expected) {
        assertTrue(getLabeledById(LINE_ID).getText().contains(expected),
                getLabeledById(LINE_ID).getText());
    }

    /**
     * @param expected start of the defender line: the number of defenders, the
     *                 settlement's defense and the strength of the attacker
     */
    public void verifyAttackDefenders(final String expected) {
        assertTrue(getLabeledById(DEFENDERS_ID).getText().startsWith(expected),
                getLabeledById(DEFENDERS_ID).getText());
    }

    /**
     * @param expected the whole line about the Spanish attack bonus
     */
    public void verifyAttackSpanish(final String expected) {
        assertEquals(expected, getLabeledById(SPANISH_ID).getText());
    }

    /**
     * @param expected the whole line about the relation consequence
     */
    public void verifyAttackRelations(final String expected) {
        assertEquals(expected, getLabeledById(RELATIONS_ID).getText());
    }

    /**
     * The confirmation stays inside the given size, so its lines wrap instead
     * of widening the window; the whole text and both buttons remain.
     *
     * @param maxWidth  widest allowed window
     * @param maxHeight tallest allowed window
     */
    public void verifyAttackFits(final int maxWidth, final int maxHeight) {
        final javafx.stage.Window window = confirmationButton(CONFIRM_ID)
                .getScene().getWindow();
        assertTrue(window.getWidth() <= maxWidth,
                "Attack confirmation width " + window.getWidth());
        assertTrue(window.getHeight() <= maxHeight,
                "Attack confirmation height " + window.getHeight());
    }

    /** The confirmation has closed. */
    public void verifyAttackClosed() {
        assertFalse(isCssIdVisible(ATTACK_CONTENT_ID));
    }

    /**
     * @param expected fragment of the reported result of the attack
     */
    public void verifyResultMessage(final String expected) throws Exception {
        DialogMessagePage.of(getContext()).verifyDialogTextContains(expected);
    }

    /**
     * The saved game state equals the remembered one: nothing was changed. Only
     * the interface's focused tile may differ, see {@link #gameState}.
     */
    public void verifyWorldUnchanged() {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(remembered, gameState());
    }

    /**
     * @param at the square of the settlement that the Dutch know no more
     */
    public void verifySettlementRemoved(final Location at) throws Exception {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> getModel()
                .getNativeSettlementKnownTo(dutch(), at).isEmpty());
        assertTrue(
                getModel().getNativeSettlementKnownTo(dutch(), at).isEmpty());
        assertTrue(getModel().getNativeSettlementsKnownTo(dutch()).stream()
                .noneMatch(site -> site.getLocation().equals(at)));
    }

    /**
     * @param at the square of the settlement that is still known and standing
     */
    public void verifySettlementStands(final Location at) {
        assertTrue(
                getModel().getNativeSettlementKnownTo(dutch(), at).isPresent());
    }

    /**
     * @param at       square of the settlement
     * @param expected braves that stand on the square now
     */
    public void verifyDefenders(final Location at, final int expected) {
        assertEquals(expected,
                getModel().getUnitsAt(at).stream()
                        .filter(unit -> unit instanceof UnitNativeBrave)
                        .collect(Collectors.toList()).size());
    }

    /**
     * @param unitId   the Dutch unit
     * @param expected the square it stands on
     */
    public void verifyUnitAt(final int unitId, final Location expected) {
        assertEquals(expected, getModel().getUnitById(unitId).getLocation());
    }

    /**
     * @param unitId the Dutch soldier that lost its battle
     */
    public void verifyDisarmed(final int unitId) {
        assertFalse(((UnitFreeColonist) getModel().getUnitById(unitId))
                .isHoldingGuns());
    }

    /**
     * @param expected exact tribal anger of the Aztecs toward the Dutch
     */
    public void verifyTribalAnger(final double expected) {
        assertEquals(expected,
                getModel().getNativeTribalAnger(dutch(), NativeNation.AZTEC),
                1e-9);
    }

    /**
     * @param expected saved conquest records of the Dutch, one for each
     *                 conquered settlement and never repeated
     */
    public void verifyConquestRecords(final int expected) {
        assertEquals(expected,
                getModel().getTurnEventStore().getForPlayer(dutch()).stream()
                        .filter(TurnEventNativeConquest.class::isInstance)
                        .collect(Collectors.toList()).size());
    }

    /**
     * @param braveId an Aztec brave whose settlement was conquered, with no
     *                other Aztec settlement left to rehome it
     */
    public void verifyBraveHomeless(final int braveId) {
        assertTrue(getModel().getUnitById(braveId) instanceof UnitNativeBrave);
        assertTrue(((UnitNativeBrave) getModel().getUnitById(braveId))
                .getHomeSettlementId().isEmpty());
    }

    /**
     * @param braveId an Aztec brave that still has its settlement
     * @param homeId  the settlement it belongs to
     */
    public void verifyBraveHome(final int braveId, final int homeId) {
        assertEquals(homeId, ((UnitNativeBrave) getModel().getUnitById(braveId))
                .getHomeSettlementId().orElseThrow());
    }

    /**
     * The open details list the known settlements; the conquered one is not
     * among them.
     *
     * @param at       the square of a settlement
     * @param expected whether the list shows the settlement
     */
    @SuppressWarnings("unchecked")
    public void verifySettlementListed(final Location at,
            final boolean expected) {
        final ListView<NativeSettlement> list = (ListView<NativeSettlement>) getNodeFinder()
                .lookup("#" + NativeSettlementsDialog.LIST_ID).query();
        assertEquals(expected, list.getItems().stream()
                .anyMatch(site -> site.getLocation().equals(at)));
    }

    /**
     * @param expected saved notices of Dutch pupils or missionaries lost with a
     *                 conquered settlement, each kept once until the Dutch end
     *                 their turn
     */
    public void verifyLossNoticeCount(final int expected) throws Exception {
        GamePage.of(getContext()).waitWhileMoving();
        assertEquals(expected,
                getModel().getTurnEventStore().getForPlayer(dutch()).stream()
                        .filter(TurnEventNativeConquestLoss.class::isInstance)
                        .collect(Collectors.toList()).size());
    }

    /**
     * @param report   the open turn report
     * @param expected fragment of the loss notice
     * @param count    report lines that must contain the fragment
     */
    public void verifyReportLossNotice(final ScreenTurnReport report,
            final String expected, final int count) {
        report.verifyThatAtLeastOneEventMessageContains(expected);
        report.verifyEventMessageCount(expected, count);
    }

    /** Capture the open turn report. */
    public void captureReport(final String fileName) throws Exception {
        captureDialog("closeButtonId", fileName);
    }

    private org.microcol.model.Player dutch() {
        return getModel().getPlayerByName(DUTCH);
    }

}
