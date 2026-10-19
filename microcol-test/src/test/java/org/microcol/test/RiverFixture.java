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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Scoped runtime fixture for river scenarios. It derives a prepared copy of the
 * shared F01 scenario and authors one course on the row east of Delft: a large
 * river on the colony square, a small river east of it and a large river
 * further east, joined by two segments. The shared F01 file keeps its content.
 */
final class RiverFixture {

    /** Delft, the colony square where the course begins. */
    private static final Location COLONY_LOCATION = Location.of(22, 12);

    /** Small river east of the colony; also the field of Delft's worker. */
    private static final Location SMALL_LOCATION = Location.of(23, 12);

    /** Large river east of the small one; empty land without units. */
    private static final Location LARGE_LOCATION = Location.of(24, 12);

    private RiverFixture() {
    }

    /**
     * Author the river course and make the next Load game use the prepared
     * copy. Colonists and fields keep their original state.
     *
     * @param context running application context
     * @param source  shared scenario
     * @param name    file name of the prepared copy
     */
    static void prepareCourse(final TestContext context, final File source,
            final String name) throws IOException {
        write(context, source, name, null);
    }

    /**
     * Same as {@link #prepareCourse(TestContext, File, String)} but the given
     * worker of Delft leaves its east field, which is the small river square,
     * and stands in the colony centre.
     *
     * @param context  running application context
     * @param source   shared scenario
     * @param name     file name of the prepared copy
     * @param workerId unit that worked the small river square
     */
    static void prepareFieldWorker(final TestContext context, final File source,
            final String name, final int workerId) throws IOException {
        write(context, source, name, workerId);
    }

    private static void write(final TestContext context, final File source,
            final String name, final Integer workerId) throws IOException {
        final JsonObject saved = new Gson()
                .fromJson(Files.readString(source.toPath()), JsonObject.class);
        final JsonObject model = saved.getAsJsonObject("model");
        if (workerId != null) {
            freeFieldAndStandInColony(model, workerId);
        }
        final JsonObject map = model.getAsJsonObject("map");
        final JsonArray rivers = RoadFixture
                .emptyRows(map.getAsJsonArray("trees"));
        RoadFixture.set(rivers, COLONY_LOCATION, "l");
        RoadFixture.set(rivers, SMALL_LOCATION, "s");
        RoadFixture.set(rivers, LARGE_LOCATION, "l");
        map.add("rivers", rivers);
        final JsonArray segments = new JsonArray();
        segments.add(segment(COLONY_LOCATION, SMALL_LOCATION));
        segments.add(segment(SMALL_LOCATION, LARGE_LOCATION));
        map.add("riverSegments", segments);
        final File prepared = new File(System.getProperty("user.home"), name);
        Files.writeString(prepared.toPath(), saved.toString());
        when(context.getClassFromGuice(FileSelectingService.class)
                .loadFile(any(File.class))).thenReturn(prepared);
    }

    /**
     * Same placement as the terrain production fixture: the field loses its
     * worker and the worker is moved into the colony centre.
     */
    private static void freeFieldAndStandInColony(final JsonObject model,
            final int workerId) {
        for (final JsonElement colony : model.getAsJsonArray("colonies")) {
            final JsonObject colonyObject = colony.getAsJsonObject();
            if (!isAt(colonyObject, COLONY_LOCATION)) {
                continue;
            }
            for (final JsonElement field : colonyObject
                    .getAsJsonArray("colonyFields")) {
                final JsonObject fieldObject = field.getAsJsonObject();
                final JsonObject direction = fieldObject
                        .getAsJsonObject("direction");
                final int x = COLONY_LOCATION.getX()
                        + direction.get("x").getAsInt();
                final int y = COLONY_LOCATION.getY()
                        + direction.get("y").getAsInt();
                if (x == SMALL_LOCATION.getX() && y == SMALL_LOCATION.getY()) {
                    fieldObject.remove("workerId");
                    fieldObject.remove("producedGoodsType");
                }
            }
        }
        model.getAsJsonArray("units").forEach(value -> {
            final JsonObject unit = value.getAsJsonObject();
            if (unit.has("id") && unit.get("id").getAsInt() == workerId) {
                unit.remove("placeColonyField");
                final JsonObject place = new JsonObject();
                place.add("location", location(COLONY_LOCATION));
                place.addProperty("orientation", "west");
                unit.add("placeMap", place);
            }
        });
    }

    private static boolean isAt(final JsonObject colonyObject,
            final Location at) {
        final JsonObject location = colonyObject.getAsJsonObject("location");
        return location.get("x").getAsInt() == at.getX()
                && location.get("y").getAsInt() == at.getY();
    }

    private static JsonObject segment(final Location from, final Location to) {
        final JsonObject out = new JsonObject();
        out.add("from", location(from));
        out.add("to", location(to));
        return out;
    }

    private static JsonObject location(final Location at) {
        final JsonObject out = new JsonObject();
        out.addProperty("x", at.getX());
        out.addProperty("y", at.getY());
        return out;
    }
}
