package org.microcol.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.microcol.gui.FileSelectingService;
import org.microcol.model.Location;
import org.microcol.model.TerrainType;
import org.microcol.page.TestContext;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Scoped runtime fixture for terrain identity scenarios. It derives a prepared
 * copy of the coastal scenario, so the shared F42 file keeps its content.
 */
final class TerrainIdentityFixture {

    private TerrainIdentityFixture() {
    }

    /**
     * Retype squares and move the Dutch galleon, then make the next Load game
     * use the prepared copy. Existing forests stay where they are, so a retyped
     * forested square keeps its forest.
     *
     * @param context  running application context
     * @param source   shared scenario
     * @param name     file name of the prepared copy
     * @param shipId   ship that is moved
     * @param shipAt   new ship square
     * @param terrains retyped squares, in pairs of location and type
     */
    static void prepare(final TestContext context, final File source,
            final String name, final int shipId, final Location shipAt,
            final Object... terrains) throws IOException {
        final JsonObject saved = new Gson()
                .fromJson(Files.readString(source.toPath()), JsonObject.class);
        final JsonObject model = saved.getAsJsonObject("model");
        final JsonArray tiles = model.getAsJsonObject("map")
                .getAsJsonArray("tiles");
        final JsonArray explored = humanVisibility(model);
        for (int index = 0; index < terrains.length; index += 2) {
            set(tiles, (Location) terrains[index],
                    ((TerrainType) terrains[index + 1]).getCode());
            // "-" is an explored square in the saved visibility mask; the
            // prepared squares lie in the unexplored west of the scenario.
            set(explored, (Location) terrains[index], "-");
        }
        model.getAsJsonArray("units").forEach(value -> {
            final JsonObject unit = value.getAsJsonObject();
            if (unit.has("id") && unit.get("id").getAsInt() == shipId) {
                final JsonObject location = unit.getAsJsonObject("placeMap")
                        .getAsJsonObject("location");
                location.addProperty("x", shipAt.getX());
                location.addProperty("y", shipAt.getY());
            }
        });
        final File prepared = new File(System.getProperty("user.home"), name);
        Files.writeString(prepared.toPath(), saved.toString());
        when(context.getClassFromGuice(FileSelectingService.class)
                .loadFile(any(File.class))).thenReturn(prepared);
    }

    /** The saved visibility rows of the only human player. */
    private static JsonArray humanVisibility(final JsonObject model) {
        for (final JsonElement player : model.getAsJsonObject("gameManager")
                .getAsJsonArray("players")) {
            final JsonObject object = player.getAsJsonObject();
            if (!object.get("computer").getAsBoolean()) {
                return object.getAsJsonObject("visible")
                        .getAsJsonArray("visible");
            }
        }
        throw new IllegalStateException("No human player in the scenario");
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

    static void prepareCoasts(final TestContext context, final File source,
            final String name, final int shipId, final Location shipAt)
            throws IOException {
        final java.util.List<Object> tiles = new java.util.ArrayList<>();
        for (int y = 12; y <= 18; y++) {
            for (int x = 14; x <= 20; x++) {
                tiles.add(Location.of(x, y));
                tiles.add(x < 17 || y < 15 ? TerrainType.GRASSLAND
                        : TerrainType.OCEAN);
            }
        }
        prepare(context, source, name, shipId, shipAt, tiles.toArray());
        final File prepared = new File(System.getProperty("user.home"), name);
        final Gson gson = new Gson();
        final JsonObject saved = gson.fromJson(
                Files.readString(prepared.toPath()), JsonObject.class);
        final JsonObject map = saved.getAsJsonObject("model")
                .getAsJsonObject("map");
        // Retyping land as sea must also remove its land-only overlays.
        for (int y = 15; y <= 18; y++) {
            for (int x = 17; x <= 20; x++) {
                final Location at = Location.of(x, y);
                set(map.getAsJsonArray("trees"), at, "-");
                set(map.getAsJsonArray("fields"), at, "-");
            }
        }
        Files.writeString(prepared.toPath(), gson.toJson(saved));
    }

    static void prepareBoundaries(final TestContext context, final File source,
            final String name, final int shipId, final Location shipAt)
            throws IOException {
        final String[] rows = { "GGGPPPDD", "GGPPPPDD", "GGGPPDDD", "GGPPPPDD",
                "GGGPPDDD", "GGGGPPDD" };
        final java.util.List<Object> tiles = new java.util.ArrayList<>();
        for (int y = 0; y < rows.length; y++) {
            for (int x = 0; x < rows[y].length(); x++) {
                tiles.add(Location.of(14 + x, 12 + y));
                tiles.add(rows[y].charAt(x) == 'G' ? TerrainType.GRASSLAND
                        : rows[y].charAt(x) == 'P' ? TerrainType.PLAINS
                                : TerrainType.DESERT);
            }
        }
        prepare(context, source, name, shipId, shipAt, tiles.toArray());
    }

}
