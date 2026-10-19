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
 * Scoped runtime fixture for special-resource scenarios. It derives a prepared
 * copy of the shared F01 scenario and authors two resources east of Delft: a
 * prime sugar land on the colony's east field, which the Delft worker leaves,
 * and a game area one square further east. The shared F01 file keeps its
 * content.
 */
final class ResourceFixture {

    /** Delft's east field: grassland with a prime sugar cane resource. */
    static final Location SUGAR_LOCATION = Location.of(23, 12);

    /**
     * Grassland two squares east of Delft: a game area, unexplored in tests.
     */
    static final Location GAME_LOCATION = Location.of(24, 12);

    private ResourceFixture() {
    }

    /**
     * Author the two resources and make the next Load game use the prepared
     * copy. The given worker of Delft leaves its east field and stands in the
     * colony centre, as in the river fixture.
     *
     * @param context  running application context
     * @param source   shared scenario
     * @param name     file name of the prepared copy
     * @param workerId unit that worked the sugar square
     */
    static void prepare(final TestContext context, final File source,
            final String name, final int workerId) throws IOException {
        final JsonObject saved = new Gson()
                .fromJson(Files.readString(source.toPath()), JsonObject.class);
        final JsonObject model = saved.getAsJsonObject("model");
        RiverFixture.freeFieldAndStandInColony(model, workerId);
        final JsonObject map = model.getAsJsonObject("map");
        final JsonArray resources = RoadFixture
                .emptyRows(map.getAsJsonArray("trees"));
        RoadFixture.set(resources, SUGAR_LOCATION, "ps");
        RoadFixture.set(resources, GAME_LOCATION, "ga");
        map.add("resources", resources);
        final File prepared = new File(System.getProperty("user.home"), name);
        Files.writeString(prepared.toPath(), saved.toString());
        when(context.getClassFromGuice(FileSelectingService.class)
                .loadFile(any(File.class))).thenReturn(prepared);
    }
}
