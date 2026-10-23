package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.microcol.gui.FileSelectingService;
import org.microcol.gui.screen.game.gamepanel.SelectedUnitManager;
import org.microcol.model.Location;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.microcol.model.unit.UnitFreeColonist;
import org.microcol.model.unit.UnitWithCargo;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.testfx.util.WaitForAsyncUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * A specialist bought in Europe that reaches a native settlement by sea
 * (R-0074). The starting position is the saved expert-purchase game with the
 * authored Aztec capital, its resident brave and the native player of the
 * native-turns scenario added before the game is loaded. After the load the
 * page changes nothing in the running model: the purchase, boarding, voyage,
 * landing and the visit are played through the real screens by the scenario.
 */
public final class NativeSpecialistJourneyPage extends AbstractScreen {

    private static final String SETTLEMENT_ID = "0";

    private static final String SOURCE_BRAVE_ID = "200";

    /**
     * Below every saved Dutch unit ID, so the next purchase keeps the ID that
     * the expert-purchase scenario already expects.
     */
    private static final int RESIDENT_BRAVE_ID = 50;

    private static final String NATIVE_PLAYER = "AZTEC";

    public NativeSpecialistJourneyPage(final TestContext context) {
        super(context);
    }

    /**
     * Prepare the game that the next Load command opens: the base saved game
     * plus the Aztec capital, its four local alarms, one resident brave and the
     * Aztec player, all copied from the native scenario unchanged except for
     * the brave's ID. Nothing is loaded here and no running model is touched.
     *
     * @param base    saved game with the purchase journey (Europe, ship,
     *                colony)
     * @param natives saved game that owns the authored Aztec capital
     */
    public void prepareLoadableFixture(final File base, final File natives)
            throws Exception {
        final Gson gson = new Gson();
        final JsonObject game = gson.fromJson(Files.readString(base.toPath()),
                JsonObject.class);
        final JsonObject source = gson
                .fromJson(Files.readString(natives.toPath()), JsonObject.class);
        final JsonObject model = game.getAsJsonObject("model");
        final JsonObject sourceModel = source.getAsJsonObject("model");

        model.getAsJsonArray("nativeSettlements")
                .add(find(sourceModel.getAsJsonArray("nativeSettlements"), "id",
                        SETTLEMENT_ID));
        for (final JsonElement alarm : sourceModel
                .getAsJsonArray("nativeLocalAlarm")) {
            if (SETTLEMENT_ID.equals(alarm.getAsJsonObject().get("settlementId")
                    .getAsString())) {
                model.getAsJsonArray("nativeLocalAlarm").add(alarm);
            }
        }
        final JsonObject brave = find(sourceModel.getAsJsonArray("units"), "id",
                SOURCE_BRAVE_ID);
        brave.addProperty("id", RESIDENT_BRAVE_ID);
        model.getAsJsonArray("units").add(brave);

        final JsonObject manager = model.getAsJsonObject("gameManager");
        final JsonArray players = new JsonArray();
        players.add(find(sourceModel.getAsJsonObject("gameManager")
                .getAsJsonArray("players"), "name", NATIVE_PLAYER));
        manager.getAsJsonArray("players").forEach(players::add);
        manager.add("players", players);

        final Path fixture = Files.createTempFile("native-specialist-journey-",
                ".microcol");
        fixture.toFile().deleteOnExit();
        Files.writeString(fixture, gson.toJson(game));
        Mockito.when(getContext().getClassFromGuice(FileSelectingService.class)
                .loadFile(ArgumentMatchers.any(File.class)))
                .thenReturn(fixture.toFile());
    }

    private static JsonObject find(final JsonArray array, final String key,
            final String value) {
        for (final JsonElement element : array) {
            final JsonObject object = element.getAsJsonObject();
            if (object.has(key)
                    && value.equals(object.get(key).getAsString())) {
                return object;
            }
        }
        throw new IllegalStateException(
                "The native scenario has no entry " + key + "=" + value);
    }

    /**
     * Choose one of several own units on a colony square, as a player does. A
     * plain click there opens the colony, so the square is selected with a drag
     * toward the pointed square that Escape cancels before the release; nothing
     * moves. The right panel then lists the units of that square and the wanted
     * one is clicked.
     *
     * @param colony    required colony square with the units
     * @param pointedAt required other visible square the drag points to
     * @param unitId    ID of the unit to choose in the right panel
     */
    public void chooseUnitOnColonySquare(final Location colony,
            final Location pointedAt, final int unitId) throws Exception {
        final FogPage map = new FogPage(getContext());
        map.preview(colony, pointedAt);
        map.cancelPreview();
        final String panel = "#unitPanel-" + unitId;
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> getNodeFinder().lookup(panel).tryQuery().isPresent());
        getRobot().clickOn(panel);
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> selectedUnitId().filter(id -> id == unitId).isPresent());
        WaitForAsyncUtils.waitForFxEvents();
    }

    private Optional<Integer> selectedUnitId() {
        return getContext().getClassFromGuice(SelectedUnitManager.class)
                .getSelectedUnit().map(Unit::getId);
    }

    /**
     * The chosen unit is the selected one, so the next drag from its square
     * moves it and not another unit there.
     */
    public void verifySelectedUnit(final int unitId) {
        assertEquals(Optional.of(unitId), selectedUnitId());
    }

    /**
     * The Dutch have learned the native settlement by sight, so the visit and
     * the mission chooser may open there.
     */
    public void verifySettlementKnown(final Location settlement) {
        assertTrue(
                getModel()
                        .getNativeSettlementKnownTo(
                                getModel().getPlayerByName("Dutch"), settlement)
                        .isPresent(),
                "The Dutch do not know the settlement at " + settlement);
    }

    /**
     * The bought specialist is still the same Jesuit of the Dutch: unarmed, a
     * missionary and an expert by the model's own query, with the expected
     * treasury after the single purchase.
     *
     * @param unitId required ID given to the purchase
     * @param gold   expected treasury of the Dutch
     */
    public void verifyBoughtJesuit(final int unitId, final int gold) {
        final UnitFreeColonist jesuit = (UnitFreeColonist) getModel()
                .getUnitById(unitId);
        assertSame(UnitType.JESUIT_MISSIONARY, jesuit.getType());
        assertSame(getModel().getPlayerByName("Dutch"), jesuit.getOwner());
        assertEquals(gold, jesuit.getOwner().getGold());
        assertTrue(jesuit.isMissionary());
        assertTrue(jesuit.hasEffectiveMissionaryExpertise());
        assertFalse(jesuit.isHoldingGuns());
    }

    /**
     * The voyage ended in the colony: the passenger stands on the colony square
     * with the ship and the ship's passenger hold is free again.
     *
     * @param unitId required passenger
     * @param shipId required carrier
     * @param colony required colony square
     */
    public void verifyJesuitLanded(final int unitId, final int shipId,
            final Location colony) {
        final Unit jesuit = getModel().getUnitById(unitId);
        assertTrue(jesuit.isAtPlaceLocation());
        assertEquals(colony, jesuit.getLocation());
        final UnitWithCargo ship = (UnitWithCargo) getModel()
                .getUnitById(shipId);
        assertEquals(colony, ship.getLocation());
        assertTrue(ship.getCargo().getSlotByIndex(0).isEmpty());
    }

    /**
     * The Jesuit has movement left for the visit, so the next gesture is not
     * refused for lack of moves.
     */
    public void verifyJesuitCanMove(final int unitId) {
        final Unit jesuit = getModel().getUnitById(unitId);
        assertTrue(jesuit.getMovementThirds() > 0, "The Jesuit has no moves");
    }
}
