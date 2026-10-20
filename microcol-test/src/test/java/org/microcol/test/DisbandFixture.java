package org.microcol.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.microcol.gui.FileSelectingService;
import org.microcol.model.Location;
import org.microcol.page.TestContext;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Scoped runtime fixture for disbanding scenarios. It derives a prepared copy
 * of the shared fortification scenario before the game is loaded: colonist 104
 * is placed on the requested square with the requested tools and horses, and
 * New Amsterdam's warehouse holds the requested horses. The galleon with its
 * passenger keeps the shared content, so the loaded-carrier refusal needs no
 * change. The shared file is never edited.
 */
final class DisbandFixture {

    /** Colonist of the fortification scenario that is disbanded. */
    static final int COLONIST_ID = 104;

    /** Colony that receives equipment on its own square. */
    static final int COLONY_ID = 0;

    /** Square on the land corridor, next to New Amsterdam, with no colony. */
    static final Location WEST = Location.of(20, 15);

    /** New Amsterdam of the Dutch. */
    static final Location COLONY = Location.of(24, 16);

    /** Galleon at sea that carries passenger 4 with 80 tools. */
    static final Location GALLEON = Location.of(25, 15);

    private DisbandFixture() {
    }

    /**
     * Prepare the copy and make the next Load game use it.
     *
     * @param context      running application context
     * @param source       shared scenario
     * @param name         file name of the prepared copy
     * @param at           square of the colonist at the start
     * @param tools        tools the colonist carries
     * @param mounted      whether the colonist carries horses
     * @param colonyHorses horses in New Amsterdam's warehouse
     */
    static void prepare(final TestContext context, final File source,
            final String name, final Location at, final int tools,
            final boolean mounted, final int colonyHorses) throws IOException {
        final JsonObject saved = new Gson()
                .fromJson(Files.readString(source.toPath()), JsonObject.class);
        final JsonObject model = saved.getAsJsonObject("model");
        model.getAsJsonArray("units").forEach(value -> {
            final JsonObject unit = value.getAsJsonObject();
            if (unit.get("id").getAsInt() != COLONIST_ID) {
                return;
            }
            final JsonObject location = unit.getAsJsonObject("placeMap")
                    .getAsJsonObject("location");
            location.addProperty("x", at.getX());
            location.addProperty("y", at.getY());
            unit.addProperty("tools", tools);
            unit.addProperty("mounted", mounted);
            // The shared scenario arms this colonist; muskets are not part of
            // the disbanding scenarios.
            unit.addProperty("holdingGuns", false);
        });
        model.getAsJsonArray("colonies").forEach(value -> {
            final JsonObject colony = value.getAsJsonObject();
            if (colony.get("id").getAsInt() == COLONY_ID) {
                colony.getAsJsonObject("colonyWarehouse").addProperty("HORSE",
                        colonyHorses);
            }
        });
        final File prepared = new File(System.getProperty("user.home"), name);
        Files.writeString(prepared.toPath(), saved.toString());
        when(context.getClassFromGuice(FileSelectingService.class)
                .loadFile(any(File.class))).thenReturn(prepared);
    }
}
