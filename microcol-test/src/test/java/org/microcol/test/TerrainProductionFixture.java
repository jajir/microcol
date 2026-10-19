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
import com.google.gson.JsonPrimitive;

/**
 * Scoped runtime fixture for terrain production scenarios. It derives a
 * prepared copy of a shared scenario, so the shared F01 file keeps its content.
 */
final class TerrainProductionFixture {

    private TerrainProductionFixture() {
    }

    /**
     * Plant a forest on a colony square, free that square of its worker and
     * stand the worker in the colony centre, then make the next Load game use
     * the prepared copy.
     *
     * @param context  running application context
     * @param source   shared scenario
     * @param name     file name of the prepared copy
     * @param colonyAt location of the colony that owns the square
     * @param forestAt square that gets a forest and no field
     * @param workerId unit that worked the square
     */
    static void prepare(final TestContext context, final File source,
            final String name, final Location colonyAt, final Location forestAt,
            final int workerId) throws IOException {
        final JsonObject saved = new Gson()
                .fromJson(Files.readString(source.toPath()), JsonObject.class);
        final JsonObject model = saved.getAsJsonObject("model");
        final JsonObject map = model.getAsJsonObject("map");
        set(map.getAsJsonArray("trees"), forestAt, "t");
        set(map.getAsJsonArray("fields"), forestAt, "-");
        for (final JsonElement colony : model.getAsJsonArray("colonies")) {
            final JsonObject colonyObject = colony.getAsJsonObject();
            final JsonObject location = colonyObject
                    .getAsJsonObject("location");
            if (location.get("x").getAsInt() != colonyAt.getX()
                    || location.get("y").getAsInt() != colonyAt.getY()) {
                continue;
            }
            for (final JsonElement field : colonyObject
                    .getAsJsonArray("colonyFields")) {
                final JsonObject fieldObject = field.getAsJsonObject();
                final JsonObject direction = fieldObject
                        .getAsJsonObject("direction");
                if (colonyAt.getX() + direction.get("x").getAsInt() == forestAt
                        .getX()
                        && colonyAt.getY() + direction.get("y")
                                .getAsInt() == forestAt.getY()) {
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
                final JsonObject location = new JsonObject();
                location.addProperty("x", colonyAt.getX());
                location.addProperty("y", colonyAt.getY());
                place.add("location", location);
                place.addProperty("orientation", "west");
                unit.add("placeMap", place);
            }
        });
        final File prepared = new File(System.getProperty("user.home"), name);
        Files.writeString(prepared.toPath(), saved.toString());
        when(context.getClassFromGuice(FileSelectingService.class)
                .loadFile(any(File.class))).thenReturn(prepared);
    }

    private static void set(final JsonArray rows, final Location location,
            final String code) {
        final String row = rows.get(location.getY()).getAsString();
        final int colon = row.indexOf(':');
        final String[] cells = row.substring(colon + 1).split(",", -1);
        cells[location.getX() - 1] = code;
        rows.set(location.getY(), new JsonPrimitive(
                row.substring(0, colon + 1) + String.join(",", cells)));
    }
}
