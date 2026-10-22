package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.microcol.model.CargoSlot;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.unit.UnitTreasure;
import org.microcol.model.unit.UnitWithCargo;
import org.testfx.util.WaitForAsyncUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;

/**
 * Six-hold treasure train (R-0134) through the ordinary player controls. The
 * ship's holds are drawn only by the dock of a colony or of the European port,
 * so the hold checks open the colony and select the ship in port, as the
 * passenger transfer scenarios do. On the map the same train is moved with the
 * unit drag and the passenger chooser. The bundled passenger scenario is
 * rewritten in memory to a layout and a hold state and loaded as a temporary
 * save, as the native pages do. Actions only move units, open screens or
 * capture; every assertion sits in a verify method.
 */
public final class TreasureCargoPage extends AbstractScreen {

    /** The owned coastal colony of the bundled scenario, with a port. */
    public static final Location COLONY = Location.of(24, 16);

    /** The sea square where the galleon stands at sea. */
    public static final Location WATER = Location.of(25, 15);

    /** The land square beside the galleon at sea. */
    public static final Location SHORE = Location.of(24, 15);

    /** Identity of the train added to the scenario. */
    public static final int TRAIN_ID = 6000;

    /** Face value of the train added to the scenario, in gold. */
    public static final int TRAIN_VALUE = 500;

    /** Ore that the one-ore state keeps in the first hold. */
    public static final int ORE_AMOUNT = 7;

    /** Identity of the galleon in the bundled scenario. */
    public static final int GALLEON_ID = 3;

    /** Passenger colonists that the bundled scenario carries in the galleon. */
    private static final int[] BUNDLED_PASSENGERS = { 4, 5 };

    /** Holds of the galleon. */
    private static final int HOLDS = 6;

    private static final Gson GSON = new Gson();

    /** Where the galleon and the train stand. */
    public enum Layout {
        /**
         * Both stand on the colony square, so the colony dock shows the ship.
         */
        PORT,
        /** The galleon is at sea and the train on the land square beside it. */
        SEA
    }

    /** Hold state that the scenario is loaded with. */
    public enum Holds {
        /** All six holds are free and the train stands ashore. */
        ALL_FREE,
        /** The first hold carries ore, so five holds are free. */
        ONE_ORE_HOLD,
        /** The train is already aboard and reserves all six holds. */
        TRAIN_ABOARD
    }

    public TreasureCargoPage(final TestContext context) {
        super(context);
    }

    /**
     * Replace the current game with the bundled scenario, rewritten to the
     * given layout and hold state, with the train added.
     *
     * @param source required bundled scenario
     * @param layout required positions of the galleon and the train
     * @param holds  required hold state of the galleon
     * @throws IOException when the temporary save cannot be written
     */
    public void loadScenario(final File source, final Layout layout,
            final Holds holds) throws IOException {
        loadScenario(source, layout, holds, false);
    }

    /**
     * Load a detached delivery fixture; Europe places the galleon at its home
     * dock.
     */
    public void loadScenario(final File source, final Layout layout,
            final Holds holds, final boolean europe) throws IOException {
        final JsonObject game = GSON.fromJson(Files.readString(source.toPath()),
                JsonObject.class);
        final JsonArray units = game.getAsJsonObject("model")
                .getAsJsonArray("units");
        removeUnits(units, BUNDLED_PASSENGERS);
        final JsonObject galleon = unitById(units, GALLEON_ID);
        if (layout == Layout.PORT) {
            final JsonObject at = galleon.getAsJsonObject("placeMap")
                    .getAsJsonObject("location");
            at.addProperty("x", COLONY.getX());
            at.addProperty("y", COLONY.getY());
        }
        if (europe) {
            galleon.remove("placeMap");
            galleon.add("placeEuropePort", new JsonObject());
        }
        galleon.getAsJsonObject("cargo").add("slots", slots(holds));
        units.add(trainUnit(layout == Layout.PORT ? COLONY : SHORE,
                holds == Holds.TRAIN_ABOARD));
        final Path fixture = Files.createTempFile("treasure-cargo-",
                ".microcol");
        try {
            Files.writeString(fixture, GSON.toJson(game));
            new AttackPage(getContext()).replaceGame(fixture.toFile());
        } finally {
            Files.deleteIfExists(fixture);
        }
    }

    /**
     * Open the colony from the map and select the galleon in its port, which
     * shows the six crates of the ship.
     *
     * @return the opened colony screen
     */
    public ColonyScreen openDock() {
        return GamePage.of(getContext()).openColonyAt(COLONY, "New Amsterdam")
                .selectUnitFromPort(0);
    }

    /**
     * Drag from a crate to the area outside the colony. A crate that only
     * reserves a hold of the train has no unit to drag, so nothing may move.
     *
     * @param holdIndex required zero-based crate
     */
    public void dragHoldOutside(final int holdIndex) {
        getRobot().drag(getCrates().get(holdIndex), MouseButton.PRIMARY)
                .dropTo(".outside");
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Capture the colony screen with the galleon's crates as shown now.
     *
     * @param fileName required name of the picture
     * @throws IOException when the picture cannot be written
     */
    public void captureHolds(final String fileName) throws IOException {
        final javafx.scene.Scene scene = getCrates().get(0).getScene();
        final Path output = Path.of("target", "warehouse-qa", fileName);
        Files.createDirectories(output.getParent());
        final WritableImage image = WaitForAsyncUtils.waitForAsyncFx(5000,
                () -> snapshotScene(scene));
        getRobot().robotContext().getCaptureSupport()
                .saveImage(getRobot().capture(image).getImage(), output);
    }

    /**
     * The train stands on the map at the square, with its face value, and is
     * not in the galleon.
     *
     * @param expected required square of the train
     */
    public void verifyTrainAt(final Location expected) {
        final UnitTreasure train = train();
        assertTrue(train.isAtPlaceLocation());
        assertEquals(expected, train.getLocation());
        assertEquals(TRAIN_VALUE, train.getValue());
        assertEquals("Dutch", train.getOwner().getName());
    }

    /**
     * Boarding was refused: the train still stands at the square and no hold of
     * the galleon holds a unit or a reservation.
     *
     * @param expected required square of the train
     */
    public void verifyTrainRefused(final Location expected) {
        verifyTrainAt(expected);
        assertTrue(galleon().getCargo().getSlots().stream().noneMatch(
                slot -> slot.isLoadedUnit() || slot.isReservedHold()));
    }

    /**
     * One train aboard in the model: one unit in one hold, five reserved holds
     * and no free hold, with the identity and face value kept.
     */
    public void verifyTrainAboard() {
        final UnitWithCargo ship = galleon();
        final UnitTreasure train = train();
        assertTrue(train.isAtCargoSlot());
        assertEquals(TRAIN_VALUE, train.getValue());
        assertEquals(1, ship.getCargo().getSlots().stream()
                .filter(CargoSlot::isLoadedUnit).count());
        assertEquals(HOLDS - 1, ship.getCargo().getSlots().stream()
                .filter(CargoSlot::isReservedHold).count());
        assertTrue(ship.getCargo().getSlots().stream()
                .noneMatch(CargoSlot::isEmpty));
        assertSame(train,
                ship.getCargo().getSlots().stream()
                        .filter(CargoSlot::isLoadedUnit).findFirst()
                        .orElseThrow().getUnit().orElseThrow());
    }

    /**
     * One train aboard, and every one of the six crates shows it as occupied.
     */
    public void verifyOneTrainInSixHolds() {
        verifyTrainAboard();
        assertEquals(HOLDS, getCrates().size());
        assertEquals(HOLDS, cratesWithCargo().size(),
                "Every hold shows the train as occupied");
    }

    /** The galleon is empty in the model, and six empty crates are shown. */
    public void verifySixFreeHolds() {
        verifyHoldsReleased();
        assertEquals(HOLDS, getCrates().size());
        assertEquals(0, cratesWithCargo().size());
    }

    /** The galleon is empty in the model and the train is not aboard. */
    public void verifyHoldsReleased() {
        assertTrue(galleon().getCargo().getSlots().stream()
                .allMatch(CargoSlot::isEmpty));
        assertFalse(train().isAtCargoSlot());
    }

    /**
     * The first hold keeps its ore and the other five are free, in the model.
     *
     * @param amount required ore in the first hold
     */
    public void verifyOreHold(final int amount) {
        final UnitWithCargo ship = galleon();
        assertEquals(Goods.of(GoodsType.ORE, amount),
                ship.getCargo().getSlotByIndex(0).getGoods().orElseThrow());
        assertEquals(HOLDS - 1, ship.getCargo().getSlots().stream()
                .filter(CargoSlot::isEmpty).count());
    }

    /**
     * The first crate shows the ore amount and no other crate shows cargo.
     *
     * @param amount required ore shown in the first crate
     */
    public void verifyOreCrate(final int amount) {
        assertEquals(HOLDS, getCrates().size());
        assertEquals(String.valueOf(amount),
                ((Label) getCrates().get(0).lookup(".label")).getText());
        assertEquals(1, cratesWithCargo().size());
    }

    private UnitWithCargo galleon() {
        return (UnitWithCargo) getModel().getUnitById(GALLEON_ID);
    }

    private UnitTreasure train() {
        return (UnitTreasure) getModel().getUnitById(TRAIN_ID);
    }

    private List<StackPane> getCrates() {
        return PageComponentPanelDock.of(getContext()).getListOfCrates();
    }

    /** Crates whose cargo image is shown, so the hold is occupied. */
    private List<StackPane> cratesWithCargo() {
        return getCrates().stream()
                .filter(crate -> crate.lookup(".cargo") != null)
                .collect(Collectors.toList());
    }

    private static JsonArray slots(final Holds holds) {
        final JsonArray slots = new JsonArray();
        for (int hold = 0; hold < HOLDS; hold++) {
            final JsonObject slot = new JsonObject();
            if (hold == 0 && holds == Holds.ONE_ORE_HOLD) {
                slot.addProperty("goodsType", "ORE");
                slot.addProperty("amount", ORE_AMOUNT);
            } else if (holds == Holds.TRAIN_ABOARD) {
                slot.addProperty("unitId", TRAIN_ID);
            }
            slots.add(slot);
        }
        return slots;
    }

    private static JsonObject trainUnit(final Location at,
            final boolean aboard) {
        final JsonObject train = new JsonObject();
        train.addProperty("id", TRAIN_ID);
        train.addProperty("type", "TREASURE_TRAIN");
        train.addProperty("ownerId", "Dutch");
        train.addProperty("treasureValue", TRAIN_VALUE);
        train.addProperty("movementThirds", 3);
        if (aboard) {
            train.add("placeCargoSlot", new JsonObject());
        } else {
            final JsonObject location = new JsonObject();
            location.addProperty("x", at.getX());
            location.addProperty("y", at.getY());
            final JsonObject place = new JsonObject();
            place.add("location", location);
            place.addProperty("orientation", "west");
            train.add("placeMap", place);
        }
        final JsonObject cargo = new JsonObject();
        cargo.add("slots", new JsonArray());
        train.add("cargo", cargo);
        final JsonObject action = new JsonObject();
        action.addProperty("type", "noAction");
        train.add("action", action);
        return train;
    }

    private static JsonObject unitById(final JsonArray units, final int id) {
        for (final JsonElement unit : units) {
            if (unit.getAsJsonObject().get("id").getAsInt() == id) {
                return unit.getAsJsonObject();
            }
        }
        throw new IllegalStateException("Bundled scenario has no unit " + id);
    }

    private static void removeUnits(final JsonArray units, final int... ids) {
        for (int index = units.size() - 1; index >= 0; index--) {
            final int id = units.get(index).getAsJsonObject().get("id")
                    .getAsInt();
            for (final int removed : ids) {
                if (id == removed) {
                    units.remove(index);
                }
            }
        }
    }

}
