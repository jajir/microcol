package org.microcol.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;

import org.microcol.gui.FileSelectingService;
import org.microcol.model.Location;
import org.microcol.page.TestContext;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Scoped runtime fixture for road scenarios. It derives a prepared copy of a
 * shared scenario, so the shared F01 file keeps its content.
 */
final class RoadFixture {

    /**
     * Saved type name of an ordinary pioneer, as the scenario file spells it.
     */
    static final String COLONIST_TYPE = "COLONIST";

    /** Saved type name of a hardy pioneer. */
    static final String HARDY_PIONEER_TYPE = "HARDY_PIONEER";

    private RoadFixture() {
    }

    /**
     * Equip every ordinary colonist, leave the given squares bare and without a
     * road, and make the next Load game use the prepared copy.
     *
     * @param context running application context
     * @param source  shared scenario
     * @param name    file name of the prepared copy
     * @param tools   tools carried by each colonist
     * @param bareAt  squares that lose any forest or field and get no road
     */
    static void prepare(final TestContext context, final File source,
            final String name, final int tools, final Location... bareAt)
            throws IOException {
        write(context, source, name, tools, null, new Location[0], bareAt);
    }

    /**
     * Same as {@link #prepare(TestContext, File, String, int, Location...)} but
     * every colonist is saved as a hardy pioneer.
     *
     * @param context running application context
     * @param source  shared scenario
     * @param name    file name of the prepared copy
     * @param tools   tools carried by each colonist
     * @param bareAt  squares that lose any forest or field and get no road
     */
    static void prepareHardy(final TestContext context, final File source,
            final String name, final int tools, final Location... bareAt)
            throws IOException {
        write(context, source, name, tools, HARDY_PIONEER_TYPE, new Location[0],
                bareAt);
    }

    /**
     * Same as {@link #prepare(TestContext, File, String, int, Location...)} but
     * each given square also gets a finished road.
     *
     * @param context running application context
     * @param source  shared scenario
     * @param name    file name of the prepared copy
     * @param tools   tools carried by each colonist
     * @param roadsAt squares that lose any forest or field and get a road
     */
    static void prepareWithRoads(final TestContext context, final File source,
            final String name, final int tools, final Location... roadsAt)
            throws IOException {
        write(context, source, name, tools, null, roadsAt, roadsAt);
    }

    private static void write(final TestContext context, final File source,
            final String name, final int tools, final String unitType,
            final Location[] roadsAt, final Location[] bareAt)
            throws IOException {
        final JsonObject saved = new Gson()
                .fromJson(Files.readString(source.toPath()), JsonObject.class);
        final JsonObject model = saved.getAsJsonObject("model");
        model.getAsJsonArray("units").forEach(value -> {
            final JsonObject unit = value.getAsJsonObject();
            if (COLONIST_TYPE.equals(unit.get("type").getAsString())) {
                unit.addProperty("tools", tools);
                if (unitType != null) {
                    unit.addProperty("type", unitType);
                    // A hardy pioneer keeps a valid, empty work history.
                    unit.addProperty("workUpdates", 0);
                    unit.remove("workSkill");
                }
            }
        });
        final JsonObject map = model.getAsJsonObject("map");
        final JsonArray roads = emptyRows(map.getAsJsonArray("trees"));
        for (final Location at : bareAt) {
            set(map.getAsJsonArray("trees"), at, "-");
            set(map.getAsJsonArray("fields"), at, "-");
        }
        for (final Location at : roadsAt) {
            set(roads, at, "r");
        }
        map.add("roads", roads);
        final File prepared = new File(System.getProperty("user.home"), name);
        Files.writeString(prepared.toPath(), saved.toString());
        when(context.getClassFromGuice(FileSelectingService.class)
                .loadFile(any(File.class))).thenReturn(prepared);
    }

    /** Same row layout as the map, with every square empty. */
    private static JsonArray emptyRows(final JsonArray template) {
        final JsonArray out = new JsonArray();
        for (int y = 0; y < template.size(); y++) {
            final String row = template.get(y).getAsString();
            if (y == 0) {
                // The header row names columns and carries no squares.
                out.add(row);
                continue;
            }
            final int colon = row.indexOf(':');
            final int cells = row.substring(colon + 1).split(",", -1).length;
            out.add(row.substring(0, colon + 1)
                    + String.join(",", Collections.nCopies(cells, "-")));
        }
        return out;
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
