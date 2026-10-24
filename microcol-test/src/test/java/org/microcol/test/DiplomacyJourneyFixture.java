package org.microcol.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.microcol.gui.FileSelectingService;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.MovementPoints;
import org.microcol.model.UnitType;
import org.microcol.page.ForeignColonyTradePage;
import org.microcol.page.TestContext;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The one initial world of the connected diplomacy journey (R-0219, UC-117),
 * derived before the game loads from the native turns scenario F106 and never
 * touched again. F106 already holds the Dutch colony New Amsterdam (24, 16),
 * the Aztec capital (23, 15) teaching Expert Farmer and buying muskets, tools
 * and horses, the Aztec brave 200 on its hill, the French colony Hidden Quebec
 * on the two-square islet (27..28, 17), the French galleon 106 and the native
 * players that make the natives act.
 *
 * <p>
 * Changes, all legal saved data: the calendar stands at turn 4, so the Crown
 * war of turn 20 stays out of the journey; the Dutch hold 2,000 gold and Jan de
 * Witt, France 1,000; the Dutch hold the F105 paid rights on the colony
 * squares, so the plow square (22, 16) is still unpaid; the Aztecs are uneasy
 * toward the Dutch (anger 30, never hostile enough to refuse a wagon) and at
 * 100 toward France; Hidden Quebec holds 20 corn, 30 tobacco and no sugar, and
 * nothing there produces or consumes sugar; a second French colony, Fort
 * Orleans (22, 21), stands on the main island with one field worker and a
 * French sea route Hidden Quebec, Fort Orleans; the saved combat random state
 * is chosen so that its first eight draws are below 0.2 and every battle of the
 * journey is won by its attacker. Added Dutch units: a pioneer, an ordinary
 * scout, a loaded wagon and an ordinary missionary beside the capital, an armed
 * soldier beside Fort Orleans, an unarmed observer on the islet beside Hidden
 * Quebec and a frigate with 4 sugar at sea beside it. Added Aztec lookout and a
 * sentried French colonist make the paid pursuit possible. Every French unit
 * outside a colony is sentried and France owns no armed land unit.
 * </p>
 *
 * <p>
 * Variants: {@link Variant#HOSTILE} makes the Aztecs hostile toward the Dutch
 * and leaves one armed soldier beside the brave and nothing else near the
 * capital; {@link Variant#PRIVATEER} leaves a Dutch privateer beside the French
 * galleon and nothing else near the capital.
 * </p>
 */
final class DiplomacyJourneyFixture {

    enum Variant {
        /** The connected journey. */
        JOURNEY,
        /** Hostile Aztecs raid an armed soldier beside their brave. */
        HOSTILE,
        /** A privateer attacks a foreign ship without declaring war. */
        PRIVATEER
    }

    static final int SCOUT = 300;
    static final int PIONEER = 301;
    static final int WAGON = 310;
    static final int MISSIONARY = 320;
    static final int FRENCH_TARGET = 322;
    static final int SOLDIER = 330;
    static final int OBSERVER = 340;
    static final int FRIGATE = ForeignColonyTradePage.SHIP_ID;
    static final int FORT_WORKER = 360;
    static final int RAID_SOLDIER = 370;
    static final int PRIVATEER_SHIP = 380;
    static final int AZTEC_BRAVE = 200;
    static final int FRENCH_GALLEON = 106;

    static final int HIDDEN_QUEBEC = 1;
    static final int FORT_ORLEANS = 2;
    static final String ROUTE = "Orleans line";

    static final Location AZTEC_CAPITAL = Location.of(23, 15);
    static final Location BRAVE_AT = Location.of(23, 16);
    static final Location SCOUT_AT = Location.of(22, 15);
    static final Location PIONEER_AT = Location.of(22, 16);
    static final Location WAGON_AT = Location.of(23, 14);
    static final Location MISSIONARY_AT = Location.of(22, 14);
    static final Location RAID_SOLDIER_AT = Location.of(23, 17);
    static final Location LOOKOUT_AT = Location.of(21, 19);
    static final Location FRENCH_TARGET_AT = Location.of(22, 19);
    static final Location SOLDIER_AT = Location.of(21, 20);
    static final Location FORT_AT = Location.of(22, 21);
    static final Location OBSERVER_AT = Location.of(27, 17);
    static final Location FRIGATE_AT = Location.of(27, 16);
    static final Location GALLEON_AT = Location.of(28, 16);
    static final Location HIDDEN_QUEBEC_AT = Location.of(28, 17);
    static final Location PRIVATEER_AT = Location.of(27, 15);

    static final int DUTCH_GOLD = 2000;
    static final int FRENCH_GOLD = 1000;
    static final int WAGON_CORN = 50;
    static final int FRIGATE_SUGAR = 4;
    static final int QUEBEC_CORN = 20;
    static final int QUEBEC_TOBACCO = 30;

    /** Tribal anger of the Aztecs toward the Dutch in the journey. */
    static final double UNEASY_ANGER = 30;

    /** Tribal anger of the Aztecs toward the Dutch in the hostile branch. */
    static final double HOSTILE_ANGER = 80;

    private static final int TURN = 4;
    private static final long GAMMA = 0x9E3779B97F4A7C15L;
    private static final int DRAWS = 8;
    private static final double LOW_DRAW = 0.2;

    private DiplomacyJourneyFixture() {
    }

    /**
     * Smallest saved state whose first eight draws are all below 0.2: the
     * incitement succeeds at 25%, and the brave, the soldier and the privateer
     * win their battles at any odds above 0.2. It runs only while the fixture
     * is prepared and follows the model's own stream.
     */
    static long seed() {
        for (long state = 1;; state++) {
            boolean low = true;
            for (int draw = 0; draw < DRAWS && low; draw++) {
                low = Model.combatRandomAfter(state + draw * GAMMA) < LOW_DRAW;
            }
            if (low) {
                return state;
            }
        }
    }

    /** Write the prepared copy and make the Load button select it. */
    static void prepare(final TestContext context, final File source,
            final String name, final Variant variant) throws IOException {
        final Gson gson = new Gson();
        final JsonObject saved = gson
                .fromJson(Files.readString(source.toPath()), JsonObject.class);
        final JsonObject model = saved.getAsJsonObject("model");
        model.getAsJsonObject("calendar").addProperty("numberOfPlayedTurns",
                TURN);
        model.addProperty("combatRandomState", seed());
        players(gson, model);
        anger(model, variant == Variant.HOSTILE ? HOSTILE_ANGER : UNEASY_ANGER);
        model.add("nativeLandRights", rights(gson));
        final JsonArray units = model.getAsJsonArray("units");
        sentryFrench(units);
        french(gson, model, units);
        if (variant == Variant.JOURNEY) {
            journey(units);
        } else if (variant == Variant.HOSTILE) {
            units.add(colonist(RAID_SOLDIER, "Dutch", RAID_SOLDIER_AT, true,
                    false, false));
        } else {
            units.add(ship(PRIVATEER_SHIP, "PRIVATEER", PRIVATEER_AT));
        }
        final File prepared = new File(System.getProperty("user.home"), name);
        Files.writeString(prepared.toPath(), saved.toString());
        when(context.getClassFromGuice(FileSelectingService.class)
                .loadFile(any(File.class))).thenReturn(prepared);
    }

    /** Dutch purse and Congress, French purse. */
    private static void players(final Gson gson, final JsonObject model) {
        for (final JsonElement element : model.getAsJsonObject("gameManager")
                .getAsJsonArray("players")) {
            final JsonObject player = element.getAsJsonObject();
            final String name = player.get("name").getAsString();
            if ("Dutch".equals(name)) {
                player.addProperty("gold", DUTCH_GOLD);
                player.add("congress",
                        gson.fromJson("{\"activated\": true, \"bells\": 0, "
                                + "\"members\": [\"JAN_DE_WITT\"], "
                                + "\"candidates\": [\"JOHN_PAUL_JONES\", "
                                + "\"THOMAS_JEFFERSON\", \"ADAM_SMITH\", "
                                + "\"WILLIAM_PENN\", \"FERDINAND_MAGELLAN\"], "
                                + "\"generation\": 1, "
                                + "\"jonesDeliveryPending\": false}",
                                JsonObject.class));
            } else if ("France".equals(name)) {
                player.addProperty("gold", FRENCH_GOLD);
            }
        }
    }

    /** Aztec anger toward the Dutch as given and toward France at 100. */
    private static void anger(final JsonObject model, final double dutch) {
        for (final JsonElement entry : model
                .getAsJsonArray("nativeTribalAnger")) {
            final JsonObject anger = entry.getAsJsonObject();
            if (!"AZTEC".equals(anger.get("nation").getAsString())) {
                continue;
            }
            if ("DUTCH".equals(anger.get("european").getAsString())) {
                anger.addProperty("value", dutch);
            } else if ("FRENCH".equals(anger.get("european").getAsString())) {
                anger.addProperty("value", 100.0);
            }
        }
    }

    /**
     * The paid rights of F105: the Dutch colony square and its free squares.
     */
    private static JsonArray rights(final Gson gson) {
        return gson.fromJson("[{\"european\": \"DUTCH\", \"location\": "
                + "{\"x\": 24, \"y\": 16}}, {\"european\": \"DUTCH\", "
                + "\"location\": {\"x\": 23, \"y\": 17}}, {\"european\": "
                + "\"DUTCH\", \"location\": {\"x\": 21, \"y\": 15}}, "
                + "{\"european\": \"DUTCH\", \"location\": {\"x\": 22, "
                + "\"y\": 15}}, {\"european\": \"DUTCH\", \"location\": "
                + "{\"x\": 21, \"y\": 16}}, {\"european\": \"DUTCH\", "
                + "\"location\": {\"x\": 23, \"y\": 16}}, {\"european\": "
                + "\"DUTCH\", \"location\": {\"x\": 21, \"y\": 17}}, "
                + "{\"european\": \"DUTCH\", \"location\": {\"x\": 22, "
                + "\"y\": 17}}]", JsonArray.class);
    }

    /** The French galleon stays where it is for the whole journey. */
    private static void sentryFrench(final JsonArray units) {
        for (final JsonElement element : units) {
            final JsonObject unit = element.getAsJsonObject();
            if (unit.has("id") && unit.get("id").getAsInt() == FRENCH_GALLEON) {
                unit.add("action", sentry());
            }
        }
    }

    /**
     * Hidden Quebec's stock, Fort Orleans with one field worker, the French sea
     * route between them and the sentried French colonist the Aztecs can be
     * paid to pursue.
     */
    private static void french(final Gson gson, final JsonObject model,
            final JsonArray units) {
        final JsonArray colonies = model.getAsJsonArray("colonies");
        JsonObject hidden = null;
        for (final JsonElement element : colonies) {
            final JsonObject colony = element.getAsJsonObject();
            if ("Hidden Quebec".equals(colony.get("name").getAsString())) {
                hidden = colony;
            }
        }
        final JsonObject stock = hidden.getAsJsonObject("colonyWarehouse");
        stock.addProperty("CORN", QUEBEC_CORN);
        stock.addProperty("TOBACCO", QUEBEC_TOBACCO);
        stock.addProperty("SUGAR", 0);
        final JsonObject fort = hidden.deepCopy();
        fort.addProperty("id", FORT_ORLEANS);
        fort.addProperty("name", "Fort Orleans");
        final JsonObject at = new JsonObject();
        at.addProperty("x", FORT_AT.getX());
        at.addProperty("y", FORT_AT.getY());
        fort.add("location", at);
        final int worker = FORT_WORKER;
        for (final JsonElement element : fort.getAsJsonArray("colonyFields")) {
            final JsonObject field = element.getAsJsonObject();
            field.remove("workerId");
            field.remove("producedGoodsType");
            final JsonObject direction = field.getAsJsonObject("direction");
            if (direction.get("x").getAsInt() == -1
                    && direction.get("y").getAsInt() == 0) {
                field.addProperty("workerId", worker);
                field.addProperty("producedGoodsType", "CORN");
            }
        }
        colonies.add(fort);
        model.addProperty("nextColonyId", 3);
        final JsonObject resident = colonist(worker, "France", null, false,
                false, false);
        resident.remove("placeMap");
        resident.add("placeColonyField", new JsonObject());
        units.add(resident);
        model.add("tradeRoutes", gson.fromJson("[{\"id\": 0, "
                + "\"ownerName\": \"France\", \"name\": \"" + ROUTE + "\", "
                + "\"kind\": \"SEA\", \"startColonyId\": " + HIDDEN_QUEBEC
                + ", \"stops\": [{\"europe\": false, \"colonyId\": "
                + FORT_ORLEANS + ", \"unloadGoods\": [], \"loadGoods\": []}, "
                + "{\"europe\": false, \"colonyId\": " + HIDDEN_QUEBEC
                + ", \"unloadGoods\": [], \"loadGoods\": []}]}]",
                JsonArray.class));
        model.addProperty("nextTradeRouteId", 1);
        final JsonObject lookout = unit(321, "BRAVE", "AZTEC", LOOKOUT_AT);
        lookout.add("action", sentry());
        lookout.addProperty("nativeHomeless", true);
        lookout.addProperty("nativeRole", "BRAVE");
        units.add(lookout);
        final JsonObject target = colonist(FRENCH_TARGET, "France",
                FRENCH_TARGET_AT, false, false, false);
        // Sentries wake near enemies and then wander randomly; ongoing legal
        // pioneer work keeps this pursuit target in place for four owner turns.
        target.addProperty("tools", 80);
        final JsonObject work = new JsonObject();
        work.addProperty("type", "plowField");
        work.addProperty("remainingTurns", 4);
        target.add("action", work);
        units.add(target);
    }

    /** The Dutch units of the connected journey. */
    private static void journey(final JsonArray units) {
        final JsonObject pioneer = colonist(PIONEER, "Dutch", PIONEER_AT, false,
                false, false);
        pioneer.addProperty("tools", 80);
        units.add(pioneer);
        units.add(colonist(SCOUT, "Dutch", SCOUT_AT, false, true, false));
        units.add(colonist(MISSIONARY, "Dutch", MISSIONARY_AT, false, false,
                true));
        final JsonObject wagon = unit(WAGON, "WAGON", "Dutch", WAGON_AT);
        wagon.add("cargo", cargo(UnitType.WAGON, "CORN", WAGON_CORN));
        units.add(wagon);
        units.add(colonist(SOLDIER, "Dutch", SOLDIER_AT, true, false, false));
        units.add(
                colonist(OBSERVER, "Dutch", OBSERVER_AT, false, false, false));
        final JsonObject frigate = ship(FRIGATE, "FRIGATE", FRIGATE_AT);
        frigate.add("cargo", cargo(UnitType.FRIGATE, "SUGAR", FRIGATE_SUGAR));
        units.add(frigate);
    }

    private static JsonObject sentry() {
        final JsonObject action = new JsonObject();
        action.addProperty("type", "sentry");
        return action;
    }

    private static JsonObject unit(final int id, final String type,
            final String owner, final Location at) {
        final UnitType unitType = UnitType
                .valueOf("BRAVE".equals(type) ? "COLONIST" : type);
        final JsonObject location = new JsonObject();
        location.addProperty("x", at.getX());
        location.addProperty("y", at.getY());
        final JsonObject place = new JsonObject();
        place.add("location", location);
        place.addProperty("orientation", "west");
        final JsonObject action = new JsonObject();
        action.addProperty("type", "noAction");
        final JsonObject unit = new JsonObject();
        unit.addProperty("id", id);
        unit.addProperty("type", type);
        unit.addProperty("ownerId", owner);
        unit.add("placeMap", place);
        unit.add("cargo", cargo(unitType, null, 0));
        unit.add("action", action);
        unit.addProperty("movementThirds", 3);
        unit.addProperty("tools", 0);
        unit.addProperty("holdingGuns", false);
        unit.addProperty("mounted", false);
        return unit;
    }

    /** A free colonist; the missionary flag is saved with the colonist data. */
    private static JsonObject colonist(final int id, final String owner,
            final Location at, final boolean guns, final boolean mounted,
            final boolean missionary) {
        final JsonObject unit = unit(id, "COLONIST", owner,
                at == null ? Location.of(1, 1) : at);
        unit.addProperty("workUpdates", 0);
        unit.addProperty("missionary", missionary);
        unit.addProperty("holdingGuns", guns);
        unit.addProperty("mounted", mounted);
        return unit;
    }

    /** A Dutch ship at sea with its full movement. */
    private static JsonObject ship(final int id, final String type,
            final Location at) {
        final JsonObject unit = unit(id, type, "Dutch", at);
        unit.addProperty("movementThirds",
                MovementPoints.ofPoints(UnitType.valueOf(type).getSpeed()));
        return unit;
    }

    /** Cargo slots sized by the unit type; the first holds the given goods. */
    private static JsonObject cargo(final UnitType type, final String goods,
            final int amount) {
        final JsonArray slots = new JsonArray();
        for (int index = 0; index < type.getCargoCapacity(); index++) {
            final JsonObject slot = new JsonObject();
            if (index == 0 && goods != null) {
                slot.addProperty("goodsType", goods);
                slot.addProperty("amount", amount);
            }
            slots.add(slot);
        }
        final JsonObject cargo = new JsonObject();
        cargo.add("slots", slots);
        return cargo;
    }
}
