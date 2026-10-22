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
import org.microcol.model.NativeSettlement;
import org.microcol.model.NativeVisitAction;
import org.testfx.util.WaitForAsyncUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Establishing a native mission through the real chooser and its confirmation
 * (R-0164). The initial position is a current-schema fixture derived from a
 * saved scenario: Dutch missionaries are added beside the Aztec capital. The
 * page reads the confirmation, the chooser and the settlement details; the
 * model state is read only to verify it, never changed here.
 */
public final class NativeMissionPage extends AbstractScreen {

    private static final String CONTENT_ID = "nativeVisitContent";

    private static final String MISSION_BUTTON_ID = "nativeVisitMission";

    private static final String MISSION_CONTENT_ID = "nativeMissionContent";

    private static final String MISSION_LINE_ID = "nativeMissionLine";

    private static final String MISSION_CONFIRM_ID = "nativeMissionConfirm";

    private static final String MISSION_BACK_ID = "nativeMissionBack";

    private static final String CHOICE_ID_PREFIX = "nativeVisitChoice_";

    public NativeMissionPage(final TestContext context) {
        super(context);
    }

    /**
     * Load the scenario with two Dutch visitors beside the Aztec capital. The
     * first has the given type, an ordinary missionary or a Jesuit; the second
     * is an ordinary missionary. The natives' anger toward the Dutch starts at
     * zero, so the tribe is content before any update.
     */
    public void loadMissionFixture(final File source, final String firstType,
            final int firstId, final Location firstAt, final int secondId,
            final Location secondAt) throws Exception {
        final Gson gson = new Gson();
        final JsonObject game = gson.fromJson(Files.readString(source.toPath()),
                JsonObject.class);
        final JsonObject model = game.getAsJsonObject("model");
        final JsonArray units = model.getAsJsonArray("units");
        // A Jesuit is a specialist whose role flag stays false; an ordinary
        // missionary is ordained.
        units.add(missionary(firstId, firstType, firstAt,
                !"JESUIT_MISSIONARY".equals(firstType)));
        units.add(missionary(secondId, "COLONIST", secondAt, true));
        for (final var entry : model.getAsJsonArray("nativeTribalAnger")) {
            final JsonObject anger = entry.getAsJsonObject();
            if ("AZTEC".equals(anger.get("nation").getAsString())
                    && "DUTCH".equals(anger.get("european").getAsString())) {
                anger.addProperty("value", 0.0);
            }
        }
        final Path fixture = Files.createTempFile("native-mission-",
                ".microcol");
        try {
            Files.writeString(fixture, gson.toJson(game));
            new AttackPage(getContext()).replaceGame(fixture.toFile());
        } finally {
            Files.deleteIfExists(fixture);
        }
    }

    private static JsonObject missionary(final int id, final String type,
            final Location at, final boolean ordained) {
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
        unit.addProperty("workUpdates", 0);
        unit.addProperty("missionary", ordained);
        return unit;
    }

    /**
     * Open the mission confirmation from the chooser; nothing is established.
     */
    public void startMissionChoice() {
        getRobot().clickOn(missionButton());
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Confirm the mission: the model establishes it and the missionary leaves
     * the map for the settlement.
     */
    public void confirmMission() {
        getRobot().clickOn(missionDialogButton(MISSION_CONFIRM_ID));
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Back out of the confirmation; nothing changes. */
    public void backFromMission() {
        getRobot().clickOn(missionDialogButton(MISSION_BACK_ID));
        WaitForAsyncUtils.waitForFxEvents();
    }

    private javafx.scene.control.Button missionButton() {
        return getNodeFinder()
                .lookup("#" + CONTENT_ID + " #" + MISSION_BUTTON_ID)
                .queryButton();
    }

    private javafx.scene.control.Button missionDialogButton(final String id) {
        return getNodeFinder().lookup("#" + MISSION_CONTENT_ID + " #" + id)
                .queryButton();
    }

    /** The chooser offers the mission to this missionary now. */
    public void verifyMissionButtonShown() {
        assertTrue(isCssIdShown(MISSION_BUTTON_ID));
    }

    /** A settlement that holds a mission offers no mission button. */
    public void verifyMissionButtonHidden() {
        assertFalse(isCssIdShown(MISSION_BUTTON_ID));
    }

    /**
     * @param expected fragment of the mission choice line, such as the reason
     *                 the action is refused
     */
    public void verifyMissionChoiceLimit(final String expected) {
        assertTrue(getLabeledById(
                CHOICE_ID_PREFIX + NativeVisitAction.MISSION.name()).getText()
                .contains(expected));
    }

    /**
     * The confirmation names the owner, the missionary's expertise, the
     * natives' attitude and the progress before anything is established.
     *
     * @param expected fragment that the confirmation must contain
     */
    public void verifyMissionConfirmation(final String expected) {
        assertTrue(
                getLabeledById(MISSION_LINE_ID).getText().contains(expected));
    }

    /**
     * The confirmation window stays inside the given size, so its line wraps
     * instead of widening the window; the whole text and both buttons remain.
     *
     * @param maxWidth  widest allowed window
     * @param maxHeight tallest allowed window
     */
    public void verifyMissionFits(final int maxWidth, final int maxHeight) {
        final javafx.stage.Window window = missionDialogButton(
                MISSION_CONFIRM_ID).getScene().getWindow();
        assertTrue(window.getWidth() <= maxWidth,
                "Mission confirmation width " + window.getWidth());
        assertTrue(window.getHeight() <= maxHeight,
                "Mission confirmation height " + window.getHeight());
    }

    public void captureMission(final String fileName) throws Exception {
        captureDialog(MISSION_CONTENT_ID, fileName);
    }

    /**
     * The missionary stands at the settlement's mission: it has no square, the
     * settlement holds it, and its counted progress is as expected. The model
     * change runs in the background, so the check waits for it.
     */
    public void verifyMissionEstablished(final int unitId,
            final Location settlement, final int updates) throws Exception {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> getModel().getUnitById(unitId).isAtPlaceNativeMission());
        final NativeSettlement site = knownSettlement(settlement);
        assertEquals(unitId, site.getMissionaryId().orElseThrow().intValue());
        assertEquals(updates, site.getMissionUpdates());
        assertFalse(getModel().getUnitById(unitId).isAtPlaceLocation());
    }

    /**
     * Back out left the missionary on the map and the settlement without one.
     */
    public void verifyMissionNotEstablished(final int unitId,
            final Location settlement) {
        assertTrue(getModel().getUnitById(unitId).isAtPlaceLocation());
        assertTrue(knownSettlement(settlement).getMissionaryId().isEmpty());
    }

    /** The settlement's mission belongs to this missionary. */
    public void verifyMissionHeldBy(final int unitId,
            final Location settlement) {
        assertEquals(unitId, knownSettlement(settlement).getMissionaryId()
                .orElseThrow().intValue());
    }

    /** A refused missionary keeps its square beside the settlement. */
    public void verifyStandsOnMap(final int unitId) {
        assertTrue(getModel().getUnitById(unitId).isAtPlaceLocation());
    }

    /**
     * The settlement details show the mission line, which stays while the
     * missionary is stationed off the map.
     *
     * @param expected fragment that the mission line must contain
     */
    public void verifySettlementMission(final String expected) {
        assertTrue(getLabeledById(NativeSettlementsDialog.MISSION_ID).getText()
                .contains(expected));
    }

    private NativeSettlement knownSettlement(final Location settlement) {
        return getModel()
                .getNativeSettlementKnownTo(getModel().getPlayerByName("Dutch"),
                        settlement)
                .orElseThrow(() -> new AssertionError(
                        "No native settlement known at " + settlement));
    }

}
