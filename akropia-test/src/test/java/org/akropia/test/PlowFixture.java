package org.akropia.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.akropia.gui.FileSelectingService;
import org.akropia.model.Location;
import org.akropia.page.TestContext;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Scoped runtime fixture for plowing scenarios. It derives a prepared copy of a
 * shared scenario, so the shared F01/F05 files keep their original content.
 */
final class PlowFixture {

    private PlowFixture() {
    }

    /**
     * Equip every free colonist, clear the listed squares of forest and fields,
     * and make the next Load game use the prepared copy.
     *
     * @param context  running application context
     * @param source   shared scenario
     * @param name     file name of the prepared copy
     * @param tools    tools carried by each colonist
     * @param unitType unit type given to each colonist, or null to keep it
     * @param clearAt  squares to make clear and unplowed
     */
    static void prepare(final TestContext context, final File source,
            final String name, final int tools, final String unitType,
            final Location... clearAt) throws IOException {
        final JsonObject saved = new Gson()
                .fromJson(Files.readString(source.toPath()), JsonObject.class);
        final JsonObject model = saved.getAsJsonObject("model");
        houseWorkersAt(model, clearAt);
        model.getAsJsonArray("units").forEach(value -> {
            final JsonObject unit = value.getAsJsonObject();
            if ("COLONIST".equals(unit.get("type").getAsString())) {
                unit.addProperty("tools", tools);
                if (unitType != null) {
                    unit.addProperty("type", unitType);
                    // Every free colonist keeps a valid, empty work history.
                    unit.addProperty("workUpdates", 0);
                    unit.addProperty("missionary", false);
                    unit.remove("workSkill");
                }
            }
        });
        final JsonObject map = model.getAsJsonObject("map");
        for (final Location location : clearAt) {
            clear(map.getAsJsonArray("trees"), location);
            clear(map.getAsJsonArray("fields"), location);
        }
        use(context, saved, name);
    }

    /**
     * Turn one unit of another nation into a pioneer that is already plowing
     * its clear, unplowed square, and make the next Load game use the prepared
     * copy. The game validates the copy as it does any loaded scenario.
     *
     * @param context        running application context
     * @param source         shared scenario
     * @param name           file name of the prepared copy
     * @param unitId         colonist of another nation, standing on the map
     * @param remainingTurns remaining owner updates of its plowing order
     */
    static void prepareForeignPlowing(final TestContext context,
            final File source, final String name, final int unitId,
            final int remainingTurns) throws IOException {
        final JsonObject saved = new Gson()
                .fromJson(Files.readString(source.toPath()), JsonObject.class);
        saved.getAsJsonObject("model").getAsJsonArray("units")
                .forEach(value -> {
                    final JsonObject unit = value.getAsJsonObject();
                    if (unit.get("id").getAsInt() == unitId) {
                        // A pioneer carries the required tools, no weapons.
                        unit.addProperty("tools", 20);
                        unit.addProperty("holdingWeapons", false);
                        final JsonObject action = new JsonObject();
                        action.addProperty("type", "plowField");
                        action.addProperty("remainingTurns", remainingTurns);
                        unit.add("action", action);
                    }
                });
        use(context, saved, name);
    }

    static void prepareTerrain(final TestContext context, final String name,
            final Location location, final String terrainCode)
            throws IOException {
        prepare(context,
                new File("src/test/scenarios/F01-embark-disembark.akropia"),
                name, 100, null, location);
        final File prepared = new File(System.getProperty("user.home"), name);
        final JsonObject saved = new Gson().fromJson(
                Files.readString(prepared.toPath()), JsonObject.class);
        setCell(saved.getAsJsonObject("model").getAsJsonObject("map")
                .getAsJsonArray("tiles"), location, terrainCode);
        use(context, saved, name);
    }

    private static void use(final TestContext context, final JsonObject saved,
            final String name) throws IOException {
        final File prepared = new File(System.getProperty("user.home"), name);
        Files.writeString(prepared.toPath(), saved.toString());
        when(context.getClassFromGuice(FileSelectingService.class)
                .loadFile(any(File.class))).thenReturn(prepared);
    }

    private static void clear(final JsonArray rows, final Location location) {
        setCell(rows, location, "-");
    }

    private static void setCell(final JsonArray rows, final Location location,
            final String value) {
        final String row = rows.get(location.getY()).getAsString();
        final int colon = row.indexOf(':');
        final String[] cells = row.substring(colon + 1).split(",", -1);
        cells[location.getX() - 1] = value;
        rows.set(location.getY(), new JsonPrimitive(
                row.substring(0, colon + 1) + String.join(",", cells)));
    }

    /** Keep a construction-test square free of unrelated field workers. */
    static void houseWorkersAt(final JsonObject model,
            final Location... locations) {
        for (final com.google.gson.JsonElement entry : model
                .getAsJsonArray("colonies")) {
            final JsonObject colony = entry.getAsJsonObject();
            final JsonObject center = colony.getAsJsonObject("location");
            for (final com.google.gson.JsonElement fieldEntry : colony
                    .getAsJsonArray("colonyFields")) {
                final JsonObject field = fieldEntry.getAsJsonObject();
                if (!field.has("workerId")) {
                    continue;
                }
                final JsonObject direction = field.getAsJsonObject("direction");
                final Location at = Location.of(
                        center.get("x").getAsInt()
                                + direction.get("x").getAsInt(),
                        center.get("y").getAsInt()
                                + direction.get("y").getAsInt());
                if (!java.util.Arrays.asList(locations).contains(at)) {
                    continue;
                }
                final int id = field.get("workerId").getAsInt();
                final JsonObject hall = java.util.stream.StreamSupport
                        .stream(colony.getAsJsonArray("constructions")
                                .spliterator(), false)
                        .map(value -> value.getAsJsonObject())
                        .filter(value -> "TOWN_HALL"
                                .equals(value.get("type").getAsString()))
                        .findFirst().orElseThrow();
                final JsonObject slot = java.util.stream.StreamSupport
                        .stream(hall.getAsJsonArray("slots").spliterator(),
                                false)
                        .map(value -> value.getAsJsonObject())
                        .filter(value -> !value.has("workerId")).findFirst()
                        .orElseThrow();
                slot.addProperty("workerId", id);
                field.remove("workerId");
                field.remove("producedGoodsType");
                for (final com.google.gson.JsonElement unitEntry : model
                        .getAsJsonArray("units")) {
                    final JsonObject unit = unitEntry.getAsJsonObject();
                    if (unit.get("id").getAsInt() == id) {
                        unit.remove("placeColonyField");
                        unit.add("placeConstructionSlot", new JsonObject());
                    }
                }
            }
        }
    }

}
