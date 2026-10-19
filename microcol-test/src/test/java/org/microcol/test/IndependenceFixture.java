package org.microcol.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.microcol.gui.FileSelectingService;
import org.microcol.model.Location;
import org.microcol.model.MovementPoints;
import org.microcol.page.TestContext;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Scoped runtime fixture for independence scenarios. It derives a prepared copy
 * of the independence scenario with chosen liberty per colony, a chosen
 * calendar and extra Dutch soldiers on colony squares, so the shared F03 file
 * keeps its content.
 */
final class IndependenceFixture {

    private IndependenceFixture() {
    }

    /** One saved Dutch soldier standing on a map square. */
    static final class Soldier {
        private final int id;
        private final String type;
        private final Location at;
        private final boolean armed;
        private final boolean mounted;
        private final boolean veteran;

        private Soldier(final int id, final String type, final Location at,
                final boolean armed, final boolean mounted,
                final boolean veteran) {
            this.id = id;
            this.type = type;
            this.at = at;
            this.armed = armed;
            this.mounted = mounted;
            this.veteran = veteran;
        }

        private JsonObject toJson() {
            final JsonObject location = new JsonObject();
            location.addProperty("x", at.getX());
            location.addProperty("y", at.getY());
            final JsonObject place = new JsonObject();
            place.add("location", location);
            place.addProperty("orientation", "east");
            final JsonObject cargo = new JsonObject();
            cargo.add("slots", new JsonArray());
            final JsonObject action = new JsonObject();
            action.addProperty("type", "noAction");
            final JsonObject unit = new JsonObject();
            unit.addProperty("workUpdates", 0);
            unit.addProperty("missionary", false);
            unit.addProperty("id", id);
            unit.addProperty("type", type);
            unit.addProperty("ownerId", "Dutch");
            unit.add("placeMap", place);
            unit.add("cargo", cargo);
            unit.add("action", action);
            unit.addProperty("movementThirds", MovementPoints.ofPoints(1));
            unit.addProperty("holdingGuns", armed);
            unit.addProperty("mounted", mounted);
            unit.addProperty("veteran", veteran);
            return unit;
        }
    }

    static Soldier soldier(final int id, final String type, final Location at,
            final boolean armed, final boolean mounted, final boolean veteran) {
        return new Soldier(id, type, at, armed, mounted, veteran);
    }

    /**
     * Write the prepared copy and make the next Load game use it.
     *
     * @param context     running application context
     * @param source      shared scenario
     * @param name        file name of the prepared copy
     * @param playedTurns saved number of played turns
     * @param endYear     last calendar year of the game
     * @param liberty     saved liberty of the colonies, in file order
     * @param soldiers    extra Dutch soldiers
     */
    static void prepare(final TestContext context, final File source,
            final String name, final int playedTurns, final int endYear,
            final long[] liberty, final Soldier... soldiers)
            throws IOException {
        final JsonObject saved = new Gson()
                .fromJson(Files.readString(source.toPath()), JsonObject.class);
        final JsonObject model = saved.getAsJsonObject("model");
        final JsonObject calendar = model.getAsJsonObject("calendar");
        calendar.addProperty("numberOfPlayedTurns", playedTurns);
        calendar.addProperty("endYear", endYear);
        final JsonArray colonies = model.getAsJsonArray("colonies");
        for (int index = 0; index < liberty.length; index++) {
            colonies.get(index).getAsJsonObject().addProperty("liberty",
                    liberty[index]);
        }
        final JsonArray units = model.getAsJsonArray("units");
        for (final Soldier soldier : soldiers) {
            units.add(soldier.toJson());
        }
        final File prepared = new File(System.getProperty("user.home"), name);
        Files.writeString(prepared.toPath(), saved.toString());
        when(context.getClassFromGuice(FileSelectingService.class)
                .loadFile(any(File.class))).thenReturn(prepared);
    }
}
