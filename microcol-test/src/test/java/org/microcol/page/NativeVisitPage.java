package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.microcol.model.Location;
import org.microcol.model.NativeSettlement;
import org.microcol.model.NativeVisitAction;
import org.microcol.model.UnitType;
import org.testfx.util.WaitForAsyncUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

/**
 * Entering a known native settlement through the real map gesture and the
 * chooser it opens. The initial position is a current-schema fixture derived
 * from a saved scenario: Dutch visitors of several roles are added beside the
 * Aztec capital. Nothing in the running model is changed by this page.
 */
public final class NativeVisitPage extends AbstractScreen {

    private static final String CONTENT_ID = "nativeVisitContent";

    private static final String SKILL_ID = "nativeVisitSkill";

    private static final String CHOICE_ID_PREFIX = "nativeVisitChoice_";

    private static final String TEACH_BUTTON_ID = "nativeVisitTeach";

    private static final String TEACHING_CONTENT_ID = "nativeTeachingContent";

    private static final String TEACHING_LINE_ID = "nativeTeachingLine";

    private static final String TEACHING_CONFIRM_ID = "nativeTeachingConfirm";

    private static final String TEACHING_BACK_ID = "nativeTeachingBack";

    private final FogPage map;

    private int rememberedMovement;

    public NativeVisitPage(final TestContext context) {
        super(context);
        map = new FogPage(context);
    }

    /**
     * Load the scenario with one more Dutch visitor of each wanted role beside
     * the settlement. Roles are plain saved units: an ordinary scout is a
     * mounted colonist without guns, a soldier holds guns, and a criminal is a
     * petty criminal.
     */
    public void loadFixture(final File source, final int scoutId,
            final Location scoutAt, final int criminalId,
            final Location criminalAt, final int soldierId,
            final Location soldierAt) throws Exception {
        final Gson gson = new Gson();
        final JsonObject game = gson.fromJson(Files.readString(source.toPath()),
                JsonObject.class);
        final JsonArray units = game.getAsJsonObject("model")
                .getAsJsonArray("units");
        units.add(colonist(scoutId, scoutAt, false, true));
        units.add(colonist(soldierId, soldierAt, true, false));
        units.add(criminal(criminalId, criminalAt));
        final Path fixture = Files.createTempFile("native-visit-", ".microcol");
        try {
            Files.writeString(fixture, gson.toJson(game));
            new AttackPage(getContext()).replaceGame(fixture.toFile());
        } finally {
            Files.deleteIfExists(fixture);
        }
    }

    private static JsonObject unit(final int id, final String type,
            final Location at) {
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
        unit.addProperty("ownerId", "Dutch");
        unit.add("placeMap", place);
        unit.add("cargo", cargo);
        unit.add("action", action);
        unit.addProperty("movementThirds", 3);
        unit.addProperty("tools", 0);
        unit.addProperty("holdingGuns", false);
        unit.addProperty("mounted", false);
        return unit;
    }

    private static JsonObject colonist(final int id, final Location at,
            final boolean guns, final boolean mounted) {
        final JsonObject unit = unit(id, "COLONIST", at);
        unit.addProperty("workUpdates", 0);
        unit.addProperty("missionary", false);
        unit.addProperty("holdingGuns", guns);
        unit.addProperty("mounted", mounted);
        return unit;
    }

    /**
     * A petty criminal is a colonist too, so it carries the colonist work
     * history and missionary fields of the saved schema.
     */
    private static JsonObject criminal(final int id, final Location at) {
        final JsonObject unit = unit(id, "PETTY_CRIMINAL", at);
        unit.addProperty("workUpdates", 0);
        unit.addProperty("missionary", false);
        return unit;
    }

    /** Remember the visitor's remaining movement before the gesture. */
    public void rememberMovement(final int unitId) {
        rememberedMovement = getModel().getUnitById(unitId).getMovementThirds();
    }

    /**
     * Select the visitor and drag onto the settlement, the same gesture that
     * would move or attack there, and release.
     */
    public void enterSettlement(final Location from, final Location settlement)
            throws Exception {
        map.preview(from, settlement);
        getRobot().release(MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Close the chooser with the keyboard. */
    public void escape() {
        getRobot().push(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Close the chooser with its button. */
    public void clickOk() {
        getRobot().clickOn(okButton());
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Open the confirmation of the offered skill from the chooser. */
    public void startTeachingChoice() {
        getRobot().clickOn(teachButton());
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Confirm learning; the model starts the course and the window closes. */
    public void confirmTeaching() {
        getRobot().clickOn(teachingButton(TEACHING_CONFIRM_ID));
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Back out of the confirmation; nothing changes. */
    public void backFromTeaching() {
        getRobot().clickOn(teachingButton(TEACHING_BACK_ID));
        WaitForAsyncUtils.waitForFxEvents();
    }

    private javafx.scene.control.Button teachButton() {
        return getNodeFinder().lookup("#" + CONTENT_ID + " #" + TEACH_BUTTON_ID)
                .queryButton();
    }

    private javafx.scene.control.Button teachingButton(final String id) {
        return getNodeFinder().lookup("#" + TEACHING_CONTENT_ID + " #" + id)
                .queryButton();
    }

    /**
     * The confirmation names the visitor, the offered skill and the owner
     * updates before anything is started.
     */
    public void verifyTeachingConfirmation(final String expected) {
        assertEquals(expected, getLabeledById(TEACHING_LINE_ID).getText());
    }

    public void verifyTeachingClosed() {
        assertFalse(isCssIdVisible(TEACHING_CONTENT_ID));
    }

    public void captureTeaching(final String fileName) throws Exception {
        captureDialog(TEACHING_CONTENT_ID, fileName);
    }

    /**
     * The colonist left the map for the native course: it has no square, the
     * settlement's single course names it, and the offered skill is recorded.
     * The start runs in the background, so the check waits for it.
     */
    public void verifyCourseStarted(final int unitId, final Location settlement)
            throws Exception {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> getModel().getUnitById(unitId).isAtPlaceNativeCourse());
        final NativeSettlement site = knownSettlement(settlement);
        assertEquals(unitId, site.getTeachingPupilId().orElseThrow());
        assertEquals(UnitType.EXPERT_FARMER, getModel()
                .getNativeTeachingCourse(site).orElseThrow().getSkill());
        assertFalse(getModel().getUnitById(unitId).isAtPlaceLocation());
    }

    /** Back out left the colonist on the map and the settlement free. */
    public void verifyNoCourse(final int unitId, final Location settlement) {
        assertTrue(getModel().getUnitById(unitId).isAtPlaceLocation());
        assertTrue(knownSettlement(settlement).getTeachingPupilId().isEmpty());
    }

    private NativeSettlement knownSettlement(final Location settlement) {
        return getModel()
                .getNativeSettlementKnownTo(getModel().getPlayerByName("Dutch"),
                        settlement)
                .orElseThrow(() -> new AssertionError(
                        "No native settlement known at " + settlement));
    }

    /** The chooser's own close button; other windows reuse the same ID. */
    private javafx.scene.control.Button okButton() {
        return getNodeFinder().lookup("#" + CONTENT_ID + " #buttonOk")
                .queryButton();
    }

    /**
     * The chooser names the settlement's offered skill and shows one line per
     * action, in the order chief, teaching, trade, tribute, mission, attack.
     */
    public void verifyChooser(final String skillLine, final String chief,
            final String teaching, final String trade, final String tribute,
            final String mission, final String attack) {
        assertEquals(skillLine, getLabeledById(SKILL_ID).getText());
        assertEquals(chief, choice(NativeVisitAction.SCOUT_CHIEF));
        assertEquals(teaching, choice(NativeVisitAction.TEACHING));
        assertEquals(trade, choice(NativeVisitAction.TRADE));
        assertEquals(tribute, choice(NativeVisitAction.TRIBUTE));
        assertEquals(mission, choice(NativeVisitAction.MISSION));
        assertEquals(attack, choice(NativeVisitAction.ATTACK));
    }

    private String choice(final NativeVisitAction action) {
        return getLabeledById(CHOICE_ID_PREFIX + action.name()).getText();
    }

    /** Keyboard focus starts on the close button. */
    public void verifyCloseButtonFocused() throws Exception {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> WaitForAsyncUtils
                .asyncFx(() -> okButton().isFocused()).get());
        assertTrue(okButton().isFocused());
    }

    public void verifyChooserFits(final int width, final int height) {
        final javafx.stage.Window window = okButton().getScene().getWindow();
        assertTrue(window.getWidth() <= width,
                "Chooser width " + window.getWidth());
        assertTrue(window.getHeight() <= height,
                "Chooser height " + window.getHeight());
    }

    public void verifyChooserClosed() {
        assertFalse(isCssIdVisible(CONTENT_ID));
    }

    /** An eligible visitor sees the teaching button in the chooser. */
    public void verifyTeachButtonShown() {
        assertTrue(isCssIdShown(TEACH_BUTTON_ID));
    }

    /**
     * A refused visitor has no teaching button, only the refusal line. The
     * chooser keeps the button node but hides it, so visibility is checked.
     */
    public void verifyTeachButtonHidden() {
        assertTrue(isCssIdVisible(CONTENT_ID), "Chooser is not open");
        assertFalse(isCssIdShown(TEACH_BUTTON_ID));
    }

    public void captureChooser(final String fileName) throws Exception {
        captureDialog(CONTENT_ID, fileName);
    }

    /**
     * The visitor never moved onto or fought for the settlement: it stands
     * where it started and kept its full movement.
     */
    public void verifyVisitorUnmoved(final int unitId, final Location at) {
        assertEquals(at, getModel().getUnitById(unitId).getLocation());
        assertEquals(rememberedMovement,
                getModel().getUnitById(unitId).getMovementThirds());
    }

    /** Both braves beside the settlement are still alive where they stood. */
    public void verifyBraveStays(final int braveId, final Location at) {
        assertEquals(at, getModel().getUnitById(braveId).getLocation());
    }

    /**
     * The settlement keeps the skill it was created with, as saved in the model
     * and not drawn again by viewing or loading.
     */
    public void verifyOfferedSkill(final Location settlement,
            final UnitType expected) {
        assertEquals(expected,
                getModel()
                        .getNativeSettlementKnownTo(
                                getModel().getPlayerByName("Dutch"), settlement)
                        .orElseThrow(() -> new AssertionError(
                                "No native settlement known at " + settlement))
                        .getTeachableSkill());
    }
}
