package org.microcol.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.microcol.gui.FileSelectingService;
import org.microcol.page.TestContext;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Initial world of the connected two-colony production network (R-0218). It is
 * derived from the authored F109 route scenario before the game is loaded and
 * is never touched again: New Amsterdam (26,16) and Delft (28,16) keep their
 * places, the galleon in Delft keeps its mixed cargo and the wagon stands in
 * Delft. New Amsterdam gets a school with a teacher and a student, two
 * blacksmith workers and a stock of hammers; Delft holds no ore, so nothing is
 * produced until the player delivers ore. Horses are removed so that breeding
 * cannot spend food, and both colonies keep food for the whole journey, so
 * neither starves nor founds a colonist. The started game and the current owner
 * of F109 are kept. The shared scenario file itself is not changed.
 */
final class ProductionNetworkFixture {

    static final int WAGON = 4;
    static final int TEACHER = 200;
    static final int STUDENT = 201;
    static final int BLACKSMITH_ONE = 11;
    static final int BLACKSMITH_TWO = 13;

    /** The idle wagon of F109, removed with its route. */
    private static final int IDLE_WAGON = 6;

    private static final int NEW_AMSTERDAM = 0;
    private static final int DELFT = 1;
    private static final int DELFT_X = 28;
    private static final int FOOD = 60;

    private ProductionNetworkFixture() {
    }

    /** Write the prepared copy and make the Load button select it. */
    static void prepare(final TestContext context, final File source,
            final String name) throws IOException {
        final JsonObject saved = new Gson()
                .fromJson(Files.readString(source.toPath()), JsonObject.class);
        final JsonObject model = saved.getAsJsonObject("model");
        removeIdleWagonAndRoute(model);
        parkWagonInDelft(model);
        final JsonArray units = model.getAsJsonArray("units");
        final JsonObject template = unitById(units, 0).deepCopy();
        final JsonObject newAmsterdam = colony(model, NEW_AMSTERDAM);
        final JsonObject delft = colony(model, DELFT);

        // The school: the teacher waits in the carpenters' stand until the
        // player assigns the course, the student works a corn field.
        addSchool(newAmsterdam);
        addUnit(units, template, TEACHER, "MASTER_CARPENTER",
                "placeConstructionSlot");
        slot(newAmsterdam, "CARPENTERS_STAND", 0, TEACHER);
        addUnit(units, template, STUDENT, "COLONIST", "placeColonyField");
        final JsonObject field = newAmsterdam.getAsJsonArray("colonyFields")
                .get(0).getAsJsonObject();
        field.addProperty("workerId", STUDENT);
        field.addProperty("producedGoodsType", "CORN");

        // Two blacksmiths turn delivered ore into tools.
        addUnit(units, template, BLACKSMITH_ONE, "COLONIST",
                "placeConstructionSlot");
        addUnit(units, template, BLACKSMITH_TWO, "COLONIST",
                "placeConstructionSlot");
        slot(newAmsterdam, "BLACKSMITHS_HOUSE", 0, BLACKSMITH_ONE);
        slot(newAmsterdam, "BLACKSMITHS_HOUSE", 1, BLACKSMITH_TWO);

        // Literal stocks of the initial world.
        stock(newAmsterdam, "ORE", 0);
        stock(newAmsterdam, "TOOLS", 0);
        stock(newAmsterdam, "HAMMERS", 64);
        stock(newAmsterdam, "HORSE", 0);
        stock(newAmsterdam, "CORN", FOOD);
        stock(delft, "ORE", 0);
        stock(delft, "TOOLS", 0);
        stock(delft, "HAMMERS", 0);
        stock(delft, "HORSE", 0);
        stock(delft, "CORN", FOOD);

        final File prepared = new File(System.getProperty("user.home"), name);
        Files.writeString(prepared.toPath(), saved.toString());
        when(context.getClassFromGuice(FileSelectingService.class)
                .loadFile(any(File.class))).thenReturn(prepared);
    }

    private static void removeIdleWagonAndRoute(final JsonObject model) {
        final JsonArray kept = new JsonArray();
        for (final JsonElement element : model.getAsJsonArray("units")) {
            final JsonObject unit = element.getAsJsonObject();
            if (!unit.has("id") || unit.get("id").getAsInt() != IDLE_WAGON) {
                kept.add(unit);
            }
        }
        model.add("units", kept);
        model.add("tradeRoutes", new JsonArray());
        model.addProperty("nextTradeRouteId", 0);
    }

    private static void parkWagonInDelft(final JsonObject model) {
        unitById(model.getAsJsonArray("units"), WAGON)
                .getAsJsonObject("placeMap").getAsJsonObject("location")
                .addProperty("x", DELFT_X);
    }

    private static JsonObject unitById(final JsonArray units, final int id) {
        for (final JsonElement element : units) {
            final JsonObject unit = element.getAsJsonObject();
            if (unit.has("id") && unit.get("id").getAsInt() == id) {
                return unit;
            }
        }
        throw new IllegalStateException("No unit " + id);
    }

    private static JsonObject colony(final JsonObject model, final int id) {
        for (final JsonElement element : model.getAsJsonArray("colonies")) {
            final JsonObject colony = element.getAsJsonObject();
            if (colony.get("id").getAsInt() == id) {
                return colony;
            }
        }
        throw new IllegalStateException("No colony " + id);
    }

    /** A copy of the resident template placed in a slot or on a field. */
    private static void addUnit(final JsonArray units,
            final JsonObject template, final int id, final String type,
            final String place) {
        final JsonObject unit = template.deepCopy();
        unit.remove("placeColonyField");
        unit.remove("placeConstructionSlot");
        unit.addProperty("id", id);
        unit.addProperty("type", type);
        unit.add(place, new JsonObject());
        units.add(unit);
    }

    private static void addSchool(final JsonObject colony) {
        final JsonObject school = new JsonObject();
        school.addProperty("type", "SCHOOLHOUSE");
        final JsonArray slots = new JsonArray();
        for (int index = 0; index < 3; index++) {
            slots.add(new JsonObject());
        }
        school.add("slots", slots);
        colony.getAsJsonArray("constructions").add(school);
    }

    private static void slot(final JsonObject colony, final String building,
            final int index, final int workerId) {
        for (final JsonElement element : colony
                .getAsJsonArray("constructions")) {
            final JsonObject construction = element.getAsJsonObject();
            if (building.equals(construction.get("type").getAsString())) {
                construction.getAsJsonArray("slots").get(index)
                        .getAsJsonObject().addProperty("workerId", workerId);
                return;
            }
        }
        throw new IllegalStateException("No building " + building);
    }

    private static void stock(final JsonObject colony, final String goods,
            final int amount) {
        colony.getAsJsonObject("colonyWarehouse").addProperty(goods, amount);
    }
}
