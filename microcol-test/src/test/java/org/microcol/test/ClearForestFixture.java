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
 * Scoped runtime fixture for forest-clearing scenarios. It derives a prepared
 * copy of a shared scenario, so the shared F01/F05 files keep their content.
 */
final class ClearForestFixture {

    private ClearForestFixture() {
    }

    /**
     * Equip every free colonist, plant a forest on one square, set the lumber
     * stock of one colony and make the next Load game use the prepared copy.
     *
     * @param context    running application context
     * @param source     shared scenario
     * @param name       file name of the prepared copy
     * @param tools      tools carried by each colonist
     * @param unitType   unit type given to each colonist, or null to keep it
     * @param forestAt   square that gets a forest and no field
     * @param colonyName colony whose lumber stock is set
     * @param lumber     lumber stock of that colony
     */
    static void prepare(final TestContext context, final File source,
            final String name, final int tools, final String unitType,
            final Location forestAt, final String colonyName, final int lumber)
            throws IOException {
        final JsonObject saved = new Gson()
                .fromJson(Files.readString(source.toPath()), JsonObject.class);
        final JsonObject model = saved.getAsJsonObject("model");
        model.getAsJsonArray("units").forEach(value -> {
            final JsonObject unit = value.getAsJsonObject();
            if ("COLONIST".equals(unit.get("type").getAsString())) {
                unit.addProperty("tools", tools);
                if (unitType != null) {
                    unit.addProperty("type", unitType);
                    // Every free colonist keeps a valid, empty work history.
                    unit.addProperty("workUpdates", 0);
                    unit.remove("workSkill");
                }
            }
        });
        for (final JsonElement colony : model.getAsJsonArray("colonies")) {
            final JsonObject colonyObject = colony.getAsJsonObject();
            if (colonyName.equals(colonyObject.get("name").getAsString())) {
                colonyObject.getAsJsonObject("colonyWarehouse")
                        .addProperty("LUMBER", lumber);
            }
        }
        final JsonObject map = model.getAsJsonObject("map");
        set(map.getAsJsonArray("trees"), forestAt, "t");
        set(map.getAsJsonArray("fields"), forestAt, "-");
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
