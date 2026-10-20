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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Scoped runtime fixture for go-to scenarios. It derives a prepared copy of the
 * shared fortification scenario before the game is loaded: colonist 104 starts
 * at the requested square with the requested movement, and the only land route
 * between the colony's north-west and the far end of row 15 is that row, so
 * every owner turn moves the colonist along one known square. The shared file
 * keeps its content, and nothing is changed while the game runs.
 */
final class GoToFixture {

    /** Colonist of the fortification scenario that receives the order. */
    static final int COLONIST_ID = 104;

    /** East end of the corridor, the colonist's square in the scenario. */
    static final Location EAST = Location.of(24, 15);

    /** West end of the corridor, four squares from {@link #EAST}. */
    static final Location WEST = Location.of(20, 15);

    /** New Amsterdam of the Dutch, next to the east end of the corridor. */
    static final Location COLONY = Location.of(24, 16);

    /** Grass islet in the sea that no land unit can reach. */
    static final Location ISLET = Location.of(26, 15);

    private GoToFixture() {
    }

    /**
     * Prepare the copy without an order and make the next Load game use it.
     *
     * @param context        running application context
     * @param source         shared scenario
     * @param name           file name of the prepared copy
     * @param start          square of the colonist at the start
     * @param movementThirds movement of the colonist at the start
     */
    static void prepare(final TestContext context, final File source,
            final String name, final Location start, final int movementThirds)
            throws IOException {
        write(context, source, name, start, movementThirds, null, null, false);
    }

    /**
     * Prepare the copy with a go-to order already saved on the colonist, as a
     * game saved earlier in the journey would carry it.
     *
     * @param context         running application context
     * @param source          shared scenario
     * @param name            file name of the prepared copy
     * @param start           square of the colonist at the start
     * @param movementThirds  movement of the colonist at the start
     * @param destination     destination square of the saved order
     * @param destinationName name the saved order shows
     * @param paused          whether the saved order is already paused
     */
    static void prepareOrdered(final TestContext context, final File source,
            final String name, final Location start, final int movementThirds,
            final Location destination, final String destinationName,
            final boolean paused) throws IOException {
        write(context, source, name, start, movementThirds, destination,
                destinationName, paused);
    }

    private static void write(final TestContext context, final File source,
            final String name, final Location start, final int movementThirds,
            final Location destination, final String destinationName,
            final boolean paused) throws IOException {
        final JsonObject saved = new Gson()
                .fromJson(Files.readString(source.toPath()), JsonObject.class);
        final JsonObject model = saved.getAsJsonObject("model");
        model.getAsJsonArray("units").forEach(value -> {
            final JsonObject unit = value.getAsJsonObject();
            if (unit.get("id").getAsInt() != COLONIST_ID) {
                return;
            }
            final JsonObject at = unit.getAsJsonObject("placeMap")
                    .getAsJsonObject("location");
            at.addProperty("x", start.getX());
            at.addProperty("y", start.getY());
            unit.addProperty("movementThirds", movementThirds);
            if (destination != null) {
                final JsonObject target = new JsonObject();
                target.addProperty("x", destination.getX());
                target.addProperty("y", destination.getY());
                unit.add("goToDestination", target);
                unit.addProperty("goToDestinationName", destinationName);
                unit.addProperty("goToPaused", paused);
            }
        });
        // Sea on both sides of the corridor leaves one cheapest route, and sea
        // at (25, 14) cuts the islet off from the land. These squares are
        // outside the colony's work area, so no job changes.
        final JsonArray tiles = model.getAsJsonObject("map")
                .getAsJsonArray("tiles");
        for (int x = 19; x <= 25; x++) {
            RoadFixture.set(tiles, Location.of(x, 14), "_");
        }
        for (int x = 19; x <= 22; x++) {
            RoadFixture.set(tiles, Location.of(x, 16), "_");
        }
        final File prepared = new File(System.getProperty("user.home"), name);
        Files.writeString(prepared.toPath(), saved.toString());
        when(context.getClassFromGuice(FileSelectingService.class)
                .loadFile(any(File.class))).thenReturn(prepared);
    }
}
