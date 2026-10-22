package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.microcol.model.Founder;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Player;
import org.microcol.model.Unit;
import org.microcol.model.unit.UnitFreeColonist;
import org.testfx.util.WaitForAsyncUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

/**
 * A native convert (R-0149) on the real game screens. The initial position is a
 * current-schema fixture: a saved randomness state for the conversion draw, a
 * convert on the map or at a colony, or every colonist of a scenario turned
 * into a convert. The actions are real player gestures (a drag onto a building,
 * the build key); the verifications read the model and the unit label only and
 * never change them.
 */
public final class NativeConvertPage extends AbstractScreen {

    /** Same saved stream as the model: each draw advances the state once. */
    private static final long DRAW_STEP = 0x9E3779B97F4A7C15L;

    private static final String DUTCH = "Dutch";

    private static final String CONVERT_LABEL_PREFIX = "unitConvert-";

    private static final String REPORT_CLOSE_ID = "closeButtonId";

    public NativeConvertPage(final TestContext context) {
        super(context);
    }

    /**
     * The game also draws from the saved stream for other events (a Crown war
     * pairing, a tied settlement, a computer player's combat), so the draw that
     * reaches the conversion is not always the first. A state whose next
     * several draws all succeed makes the conversion deterministic whichever of
     * them the other events take first.
     *
     * @param chance      the conversion chance every draw must beat
     * @param consecutive how many coming draws must all succeed
     * @return the smallest saved state whose next draws all fall below chance
     */
    public static long stateWithSuccessfulDraws(final double chance,
            final int consecutive) {
        for (long state = 1;; state++) {
            boolean allSucceed = true;
            for (int draw = 0; draw < consecutive && allSucceed; draw++) {
                allSucceed = Model
                        .combatRandomAfter(state + draw * DRAW_STEP) < chance;
            }
            if (allSucceed) {
                return state;
            }
        }
    }

    /**
     * Load the scenario with an ordinary Dutch missionary beside the Aztec
     * capital and the given saved randomness. The natives' anger toward the
     * Dutch starts at zero.
     */
    public void loadMissionConvertFixture(final File source,
            final long combatState, final Location missionaryAt,
            final int missionaryId) throws Exception {
        final Gson gson = new Gson();
        final JsonObject game = readGame(source, gson);
        final JsonObject model = game.getAsJsonObject("model");
        model.addProperty("combatRandomState", combatState);
        model.getAsJsonArray("units")
                .add(unit(missionaryId, "COLONIST", missionaryAt, true, null));
        zeroAnger(model);
        replaceGame(gson, game);
    }

    /**
     * Load the scenario with one saved convert standing at the given square,
     * unassigned for the given number of owner updates.
     */
    public void loadConvertFixture(final File source, final Location at,
            final int convertId, final int unassignedUpdates) throws Exception {
        final Gson gson = new Gson();
        final JsonObject game = readGame(source, gson);
        final JsonObject model = game.getAsJsonObject("model");
        model.getAsJsonArray("units")
                .add(unit(convertId, "COLONIST", at, false, unassignedUpdates));
        zeroAnger(model);
        replaceGame(gson, game);
    }

    /**
     * Load the scenario with every colonist standing on the map turned into a
     * convert of the Aztecs, so the founding key meets only converts.
     */
    public void loadConvertColonistsFixture(final File source)
            throws Exception {
        final Gson gson = new Gson();
        final JsonObject game = readGame(source, gson);
        final JsonObject model = game.getAsJsonObject("model");
        for (final JsonElement element : model.getAsJsonArray("units")) {
            final JsonObject unit = element.getAsJsonObject();
            if ("COLONIST".equals(unit.get("type").getAsString())
                    && unit.has("placeMap")) {
                unit.addProperty("missionary", false);
                unit.addProperty("nativeConvert", true);
                unit.addProperty("nativeConvertNation", "AZTEC");
                unit.addProperty("nativeConvertUnassignedUpdates", 0);
            }
        }
        replaceGame(gson, game);
    }

    /**
     * Load the scenario with one Dutch convert standing on the map, the Dutch
     * Congress offering Las Casas with enough bells for the next turn, and an
     * ordinary missionary beside the Aztec capital for a later mission. The
     * saved randomness is the given state.
     */
    public void loadLasCasasFixture(final File source, final long combatState,
            final Location convertAt, final int convertId,
            final Location missionaryAt, final int missionaryId)
            throws Exception {
        final Gson gson = new Gson();
        final JsonObject game = readGame(source, gson);
        final JsonObject model = game.getAsJsonObject("model");
        model.addProperty("combatRandomState", combatState);
        model.getAsJsonArray("units")
                .add(unit(convertId, "COLONIST", convertAt, false, 0));
        model.getAsJsonArray("units")
                .add(unit(missionaryId, "COLONIST", missionaryAt, true, null));
        zeroAnger(model);
        offerLasCasas(model);
        replaceGame(gson, game);
    }

    /**
     * The Dutch Congress offers Las Casas beside four other founders, one of
     * each category, with enough bells for one join at the next turn.
     */
    private static void offerLasCasas(final JsonObject model) {
        final List<Founder> offer = List.of(Founder.JOHN_PAUL_JONES,
                Founder.THOMAS_JEFFERSON, Founder.ADAM_SMITH,
                Founder.BARTOLOME_DE_LAS_CASAS, Founder.FERDINAND_MAGELLAN);
        for (final JsonElement element : model.getAsJsonObject("gameManager")
                .getAsJsonArray("players")) {
            final JsonObject player = element.getAsJsonObject();
            if (DUTCH.equals(player.get("name").getAsString())) {
                final JsonObject congress = player.getAsJsonObject("congress");
                congress.addProperty("bells", 1000);
                final JsonArray candidates = new JsonArray();
                for (final Founder founder : offer) {
                    candidates.add(founder.name());
                }
                congress.add("candidates", candidates);
            }
        }
    }

    private static JsonObject readGame(final File source, final Gson gson)
            throws Exception {
        return gson.fromJson(Files.readString(source.toPath()),
                JsonObject.class);
    }

    private void replaceGame(final Gson gson, final JsonObject game)
            throws Exception {
        final Path fixture = Files.createTempFile("native-convert-",
                ".microcol");
        try {
            Files.writeString(fixture, gson.toJson(game));
            new AttackPage(getContext()).replaceGame(fixture.toFile());
        } finally {
            Files.deleteIfExists(fixture);
        }
    }

    /** Scenarios without native anger have nothing to reset. */
    private static void zeroAnger(final JsonObject model) {
        if (!model.has("nativeTribalAnger")) {
            return;
        }
        for (final var entry : model.getAsJsonArray("nativeTribalAnger")) {
            final JsonObject anger = entry.getAsJsonObject();
            if ("AZTEC".equals(anger.get("nation").getAsString())
                    && "DUTCH".equals(anger.get("european").getAsString())) {
                anger.addProperty("value", 0.0);
            }
        }
    }

    /**
     * A saved colonist on the map. When convertUpdates is given, the unit is a
     * convert of the Aztecs with that many unassigned owner updates.
     */
    private static JsonObject unit(final int id, final String type,
            final Location at, final boolean missionary,
            final Integer convertUpdates) {
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
        unit.addProperty("ownerId", DUTCH);
        unit.add("placeMap", place);
        unit.add("cargo", cargo);
        unit.add("action", action);
        unit.addProperty("movementThirds", 3);
        unit.addProperty("tools", 0);
        unit.addProperty("holdingGuns", false);
        unit.addProperty("mounted", false);
        unit.addProperty("workUpdates", 0);
        unit.addProperty("missionary", missionary);
        if (convertUpdates != null) {
            unit.addProperty("nativeConvert", true);
            unit.addProperty("nativeConvertNation", "AZTEC");
            unit.addProperty("nativeConvertUnassignedUpdates", convertUpdates);
        }
        return unit;
    }

    /**
     * Drag the unit from the colony centre onto a building. A building that
     * refuses the worker does not accept the drop; the gesture still finishes
     * and nothing is assigned.
     *
     * @param unitId     unit standing at the colony centre
     * @param buildingId screen id of the building, for example id_blacksmith
     */
    public void dragCenterToBuildingExpectingRefusal(final int unitId,
            final String buildingId) throws Exception {
        final Parent building = getNodeFinder().lookup("#" + buildingId)
                .queryAs(Parent.class);
        final Canvas canvas = (Canvas) building.getChildrenUnmodifiable()
                .stream().filter(node -> node instanceof Canvas).findFirst()
                .orElseThrow();
        final Node source = getNodeFinder().lookup("#colonyUnit-" + unitId)
                .query();
        final CountDownLatch finished = new CountDownLatch(1);
        final javafx.event.EventHandler<DragEvent> done = event -> finished
                .countDown();
        getRobot().interact(
                () -> source.addEventHandler(DragEvent.DRAG_DONE, done));
        try {
            getRobot().drag("#colonyUnit-" + unitId, MouseButton.PRIMARY)
                    .dropTo(canvas.localToScreen(50, 30));
            if (!finished.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("The refused drag must finish");
            }
            WaitForAsyncUtils.waitForFxEvents();
        } finally {
            getRobot().interact(
                    () -> source.removeEventHandler(DragEvent.DRAG_DONE, done));
        }
    }

    /** Press the build-colony key for the selected unit. */
    public void pressBuildColony() {
        getRobot().push(KeyCode.B);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Capture the open turn report. */
    public void captureReport(final String fileName) throws Exception {
        captureDialog(REPORT_CLOSE_ID, fileName);
    }

    /** Every convert the Dutch hold, read from the model. */
    private List<UnitFreeColonist> converts() {
        final List<Unit> owned = getModel().getPlayerByName(DUTCH)
                .getAllUnits();
        return owned.stream()
                .filter(unit -> unit instanceof UnitFreeColonist
                        && ((UnitFreeColonist) unit).isNativeConvert())
                .map(UnitFreeColonist.class::cast).collect(Collectors.toList());
    }

    /** The single convert the Dutch hold; the verifications check the count. */
    private UnitFreeColonist onlyConvert() {
        final List<UnitFreeColonist> converts = converts();
        if (converts.size() != 1) {
            throw new IllegalStateException(
                    "Exactly one convert is expected, found "
                            + converts.size());
        }
        return converts.get(0);
    }

    /**
     * A convert joined an owned colony as an unassigned resident with the
     * expected native affiliation and count.
     */
    public void verifyConvertAcquired(final String expectedNation,
            final int expectedUpdates, final Location expectedColony) {
        assertEquals(1, converts().size(), "Exactly one convert is expected");
        final UnitFreeColonist convert = onlyConvert();
        final Player dutch = getModel().getPlayerByName(DUTCH);
        assertEquals(expectedUpdates, convert.getConvertUnassignedUpdates());
        assertEquals(expectedNation,
                convert.getConvertNation().orElseThrow().name());
        assertTrue(convert.isAtPlaceLocation());
        assertEquals(expectedColony, convert.getLocation());
        assertTrue(dutch.getColonies().stream().anyMatch(
                colony -> colony.getLocation().equals(convert.getLocation())));
    }

    /** No convert is held: the unassigned expiry returned it to its tribe. */
    public void verifyConvertReturned() {
        assertTrue(converts().isEmpty(), "No convert may remain");
    }

    /** The convert has spent exactly this many unassigned owner updates. */
    public void verifyUnassignedUpdates(final int expected) {
        assertEquals(1, converts().size(), "Exactly one convert is expected");
        assertEquals(expected, onlyConvert().getConvertUnassignedUpdates());
    }

    /**
     * The convert is the same person with the same native affiliation and
     * stands unassigned at the colony centre.
     */
    public void verifyUnassignedAtColony(final int unitId,
            final String expectedNation, final Location colony) {
        final Unit unit = getModel().getUnitById(unitId);
        assertTrue(unit instanceof UnitFreeColonist
                && ((UnitFreeColonist) unit).isNativeConvert());
        assertEquals(expectedNation, ((UnitFreeColonist) unit)
                .getConvertNation().orElseThrow().name());
        assertTrue(unit.isAtPlaceLocation());
        assertEquals(colony, unit.getLocation());
    }

    /**
     * The unit label names the native nation and the unassigned count, in the
     * language the game is set to.
     */
    public void verifyVisibleConvertLabel(final String expected) {
        final UnitFreeColonist convert = onlyConvert();
        new FogPage(getContext()).inspect(convert.getLocation());
        final javafx.scene.control.Labeled label = getLabeledById(
                CONVERT_LABEL_PREFIX + convert.getId());
        assertEquals(expected, label.getText());
        assertTrue(label.getHeight() >= label.prefHeight(label.getWidth()),
                "The convert label must fit in its panel");
    }

    /**
     * The person is still the same colonist at the same square, and no longer a
     * native convert after Las Casas joined.
     */
    public void verifyFreedColonist(final int unitId,
            final Location expectedAt) {
        final Unit unit = getModel().getUnitById(unitId);
        assertTrue(
                unit instanceof UnitFreeColonist
                        && !((UnitFreeColonist) unit).isNativeConvert(),
                "The person must be a free colonist");
        assertTrue(unit.isAtPlaceLocation());
        assertEquals(expectedAt, unit.getLocation());
    }

    /**
     * The unit panel shows no convert label for the freed colonist; the square
     * is inspected first, so the panel is the one of that person.
     */
    public void verifyNoConvertLabel(final int unitId) {
        final Unit unit = getModel().getUnitById(unitId);
        new FogPage(getContext()).inspect(unit.getLocation());
        assertTrue(isCssIdVisible("unitPanel-" + unitId),
                "The panel must show the freed colonist");
        assertFalse(isCssIdVisible(CONVERT_LABEL_PREFIX + unitId),
                "The freed colonist must show no convert label");
    }

    /**
     * Load the colony scenario with one Dutch convert at the given colony
     * square, unassigned, and the Dutch Congress offering Las Casas with bells
     * for the next turn. The convert is then assigned through the job picker.
     */
    public void loadConvertFounderFixture(final File source, final Location at,
            final int convertId) throws Exception {
        final Gson gson = new Gson();
        final JsonObject game = readGame(source, gson);
        final JsonObject model = game.getAsJsonObject("model");
        model.getAsJsonArray("units")
                .add(unit(convertId, "COLONIST", at, false, 0));
        zeroAnger(model);
        offerLasCasas(model);
        replaceGame(gson, game);
    }

    /** No native convert is held by the Dutch any more. */
    public void verifyNoConvertHeld() {
        assertTrue(converts().isEmpty(), "No convert may remain");
    }

}
