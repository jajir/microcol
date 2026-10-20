package org.microcol.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.microcol.gui.FileSelectingService;
import org.microcol.page.TestContext;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Scoped runtime fixture for the goods jettison scenarios. It derives a
 * prepared copy of a shared scenario before the game is loaded: the ship keeps
 * its passenger in slot 1, gets ore in slot 0 with the requested amount, and
 * gets tobacco in slot 2 as the other goods hold that must never change. The
 * shared file is never edited.
 */
final class JettisonFixture {

    /** Ship of both shared scenarios that carries the goods. */
    static final int SHIP_ID = 3;

    /** Hold that the jettison scenarios discard from. */
    static final int ORE_HOLD = 0;

    /** Passenger that must stay aboard. */
    static final int PASSENGER_HOLD = 1;

    /** Other goods hold that must keep its tobacco. */
    static final int OTHER_HOLD = 2;

    /** Passenger that must stay aboard. */
    static final int PASSENGER_ID = 4;

    /** Tobacco amount placed in the other goods hold. */
    static final int OTHER_AMOUNT = 6;

    private JettisonFixture() {
    }

    /**
     * Prepare the copy and make the next Load game use it.
     *
     * @param context   running application context
     * @param source    shared scenario with ship 3 and passenger 4
     * @param name      file name of the prepared copy
     * @param oreAmount ore that the ore hold receives
     */
    static void prepare(final TestContext context, final File source,
            final String name, final int oreAmount) throws IOException {
        final JsonObject saved = new Gson()
                .fromJson(Files.readString(source.toPath()), JsonObject.class);
        final JsonObject model = saved.getAsJsonObject("model");
        model.getAsJsonArray("units").forEach(value -> {
            final JsonObject unit = value.getAsJsonObject();
            if (unit.get("id").getAsInt() != SHIP_ID) {
                return;
            }
            final JsonArray slots = unit.getAsJsonObject("cargo")
                    .getAsJsonArray("slots");
            slots.set(ORE_HOLD, goods("ORE", oreAmount));
            slots.set(OTHER_HOLD, goods("TOBACCO", OTHER_AMOUNT));
        });
        final File prepared = new File(System.getProperty("user.home"), name);
        Files.writeString(prepared.toPath(), saved.toString());
        when(context.getClassFromGuice(FileSelectingService.class)
                .loadFile(any(File.class))).thenReturn(prepared);
    }

    private static JsonObject goods(final String type, final int amount) {
        final JsonObject out = new JsonObject();
        out.addProperty("goodsType", type);
        out.addProperty("amount", amount);
        return out;
    }
}
