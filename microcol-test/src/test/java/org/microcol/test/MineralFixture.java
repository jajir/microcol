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
 * Scoped runtime fixture for finite mineral deposits. It derives a prepared
 * copy of the shared F01 scenario and puts one deposit on Delft's east field,
 * the square that the worker leaves. The saved reserve is near the boundary, so
 * the first mining turns reach the last partial units before the deposit is
 * used up. The shared F01 file keeps its content.
 */
final class MineralFixture {

    /** Delft's east field, which the worker leaves before mining. */
    static final Location DEPOSIT_LOCATION = Location.of(23, 12);

    private MineralFixture() {
    }

    /**
     * Author one deposit on the east field and make the next Load game use the
     * prepared copy. The given worker of Delft leaves that field and stands in
     * the colony centre.
     *
     * @param context      running application context
     * @param source       shared scenario
     * @param name         file name of the prepared copy
     * @param workerId     unit that worked the east field
     * @param terrainCode  saved code of the square, hills or mountains
     * @param resourceCode saved code of the deposit, silver, ore or mixed
     * @param silver       remaining silver units of the deposit
     * @param ore          remaining ore units of the deposit
     */
    static void prepare(final TestContext context, final File source,
            final String name, final int workerId, final String terrainCode,
            final String resourceCode, final int silver, final int ore)
            throws IOException {
        final JsonObject saved = new Gson()
                .fromJson(Files.readString(source.toPath()), JsonObject.class);
        final JsonObject model = saved.getAsJsonObject("model");
        RiverFixture.freeFieldAndStandInColony(model, workerId);
        final JsonObject map = model.getAsJsonObject("map");
        RoadFixture.set(map.getAsJsonArray("tiles"), DEPOSIT_LOCATION,
                terrainCode);
        RoadFixture.set(map.getAsJsonArray("trees"), DEPOSIT_LOCATION, "-");
        RoadFixture.set(map.getAsJsonArray("fields"), DEPOSIT_LOCATION, "-");
        final JsonArray resources = RoadFixture
                .emptyRows(map.getAsJsonArray("trees"));
        RoadFixture.set(resources, DEPOSIT_LOCATION, resourceCode);
        map.add("resources", resources);
        map.add("mineralReserves", reserves(silver, ore));
        final File prepared = new File(System.getProperty("user.home"), name);
        Files.writeString(prepared.toPath(), saved.toString());
        when(context.getClassFromGuice(FileSelectingService.class)
                .loadFile(any(File.class))).thenReturn(prepared);
    }

    /** One saved entry with both remaining counts for the deposit square. */
    private static JsonArray reserves(final int silver, final int ore) {
        final JsonObject location = new JsonObject();
        location.addProperty("x", DEPOSIT_LOCATION.getX());
        location.addProperty("y", DEPOSIT_LOCATION.getY());
        final JsonObject entry = new JsonObject();
        entry.add("location", location);
        entry.addProperty("silver", silver);
        entry.addProperty("ore", ore);
        final JsonArray out = new JsonArray();
        out.add(entry);
        return out;
    }
}
