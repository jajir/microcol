package org.microcol.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.microcol.gui.FileSelectingService;
import org.microcol.model.Location;
import org.microcol.model.MovementPoints;
import org.microcol.model.UnitType;
import org.microcol.page.RebellionJson;
import org.microcol.page.TestContext;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Scoped runtime fixture for independence scenarios. It derives a prepared copy
 * of the independence scenario with chosen liberty per colony, a chosen
 * calendar and extra Dutch soldiers on colony squares, so the shared F03 file
 * keeps its content.
 */
final class IndependenceFixture {

    private IndependenceFixture() {
    }

    /** One saved Dutch soldier standing on a map square. */
    static final class Soldier {
        private final int id;
        private final String type;
        private final Location at;
        private final boolean armed;
        private final boolean mounted;
        private final boolean veteran;

        private Soldier(final int id, final String type, final Location at,
                final boolean armed, final boolean mounted,
                final boolean veteran) {
            this.id = id;
            this.type = type;
            this.at = at;
            this.armed = armed;
            this.mounted = mounted;
            this.veteran = veteran;
        }

        private JsonObject toJson() {
            final JsonObject location = new JsonObject();
            location.addProperty("x", at.getX());
            location.addProperty("y", at.getY());
            final JsonObject place = new JsonObject();
            place.add("location", location);
            place.addProperty("orientation", "east");
            final JsonObject cargo = new JsonObject();
            cargo.add("slots", new JsonArray());
            final JsonObject action = new JsonObject();
            action.addProperty("type", "noAction");
            final JsonObject unit = new JsonObject();
            unit.addProperty("workUpdates", 0);
            unit.addProperty("missionary", false);
            unit.addProperty("id", id);
            unit.addProperty("type", type);
            unit.addProperty("ownerId", "Dutch");
            unit.add("placeMap", place);
            unit.add("cargo", cargo);
            unit.add("action", action);
            unit.addProperty("movementThirds", MovementPoints.ofPoints(1));
            unit.addProperty("holdingGuns", armed);
            unit.addProperty("mounted", mounted);
            unit.addProperty("veteran", veteran);
            return unit;
        }
    }

    static Soldier soldier(final int id, final String type, final Location at,
            final boolean armed, final boolean mounted, final boolean veteran) {
        return new Soldier(id, type, at, armed, mounted, veteran);
    }

    /** One saved ship with empty holds standing on a sea square. */
    static final class Ship {
        private final int id;
        private final UnitType type;
        private final String owner;
        private final Location at;

        private Ship(final int id, final UnitType type, final String owner,
                final Location at) {
            this.id = id;
            this.type = type;
            this.owner = owner;
            this.at = at;
        }

        private JsonObject toJson() {
            final JsonObject location = new JsonObject();
            location.addProperty("x", at.getX());
            location.addProperty("y", at.getY());
            final JsonObject place = new JsonObject();
            place.add("location", location);
            place.addProperty("orientation", "west");
            final JsonArray slots = new JsonArray();
            for (int slot = 0; slot < type.getCargoCapacity(); slot++) {
                slots.add(new JsonObject());
            }
            final JsonObject cargo = new JsonObject();
            cargo.add("slots", slots);
            final JsonObject action = new JsonObject();
            action.addProperty("type", "noAction");
            final JsonObject unit = new JsonObject();
            unit.addProperty("id", id);
            unit.addProperty("type", type.name());
            unit.addProperty("ownerId", owner);
            unit.add("placeMap", place);
            unit.add("cargo", cargo);
            unit.add("action", action);
            unit.addProperty("movementThirds",
                    MovementPoints.ofPoints(type.getSpeed()));
            return unit;
        }
    }

    static Ship ship(final int id, final UnitType type, final String owner,
            final Location at) {
        return new Ship(id, type, owner, at);
    }

    /**
     * Write the prepared copy and make the next Load game use it.
     *
     * @param context     running application context
     * @param source      shared scenario
     * @param name        file name of the prepared copy
     * @param playedTurns saved number of played turns
     * @param endYear     last calendar year of the game
     * @param liberty     saved liberty of the colonies, in file order
     * @param soldiers    extra Dutch soldiers
     */
    static void prepare(final TestContext context, final File source,
            final String name, final int playedTurns, final int endYear,
            final long[] liberty, final Soldier... soldiers)
            throws IOException {
        write(context, source, name, playedTurns, endYear, liberty, null, false,
                false, null, soldiers);
    }

    /**
     * Like {@link #prepare}, with the Dutch nation already declared. Without an
     * intervention the first owner update starts the target in play; with one,
     * the authored target and progress are loaded as they are.
     *
     * @param intervention saved intervention, or null for none
     */
    static void prepareDeclared(final TestContext context, final File source,
            final String name, final int playedTurns, final int endYear,
            final long[] liberty, final JsonObject intervention)
            throws IOException {
        write(context, source, name, playedTurns, endYear, liberty, null, false,
                true, intervention);
    }

    /**
     * Like {@link #prepareDeclared}, with authored ships added to the saved
     * file before it is loaded, such as a Dutch warship and the Crown ships
     * that block a port (R-0190).
     *
     * @param ships saved ships with empty holds, standing on sea squares
     */
    static void prepareDeclaredWithShips(final TestContext context,
            final File source, final String name, final int playedTurns,
            final int endYear, final long[] liberty,
            final JsonObject intervention, final Ship... ships)
            throws IOException {
        prepareDeclared(context, source, name, playedTurns, endYear, liberty,
                intervention);
        final File prepared = new File(System.getProperty("user.home"), name);
        final JsonObject saved = new Gson().fromJson(
                Files.readString(prepared.toPath()), JsonObject.class);
        final JsonArray units = saved.getAsJsonObject("model")
                .getAsJsonArray("units");
        for (final Ship ship : ships) {
            units.add(ship.toJson());
        }
        Files.writeString(prepared.toPath(), saved.toString());
    }

    /** Saved intervention of the Dutch nation; see R-0189. */
    static JsonObject intervention(final int target, final long bells,
            final boolean earned) {
        return intervention(target, bells, earned, false);
    }

    /**
     * Saved intervention of the Dutch nation with its delivered flag (R-0190).
     */
    static JsonObject intervention(final int target, final long bells,
            final boolean earned, final boolean delivered) {
        final JsonObject saved = new JsonObject();
        saved.addProperty("target", target);
        saved.addProperty("bells", bells);
        saved.addProperty("earned", earned);
        saved.addProperty("delivered", delivered);
        return saved;
    }

    /**
     * Merchantman sailing to Europe: one update spent of a four-update trip.
     */
    static final int INBOUND_SHIP = 201;
    /** Passenger aboard the inbound merchantman. */
    static final int INBOUND_PASSENGER = 202;
    /** Galleon waiting in the home port with a passenger and goods. */
    static final int PORT_SHIP = 203;
    /** Passenger aboard the galleon in the port. */
    static final int PORT_PASSENGER = 204;
    /** Colonist waiting on the home pier. */
    static final int PIER_PERSON = 205;
    /** Galleon sailing from Europe to the colonies. */
    static final int OUTBOUND_SHIP = 206;

    /**
     * Like {@link #prepare}, with the home Europe of the Dutch populated: an
     * inbound merchantman carrying 7 ore and a passenger, a galleon in the port
     * with 40 muskets and a passenger, a colonist on the pier, and an outbound
     * galleon. The voyage factors are those of the voyage reversal scenario, so
     * the saved durations are consistent: merchantman, weather 1, westward to
     * Europe, four updates; galleon, weather 0, outward, two updates.
     */
    static void prepareWithHomeEurope(final TestContext context,
            final File source, final String name, final int playedTurns,
            final int endYear, final long[] liberty) throws IOException {
        write(context, source, name, playedTurns, endYear, liberty, null, true,
                false, null);
    }

    /**
     * Like {@link #prepare}, with every Crown holding this many produced bells
     * toward its next ground unit.
     */
    static void prepareWithCrownRemainder(final TestContext context,
            final File source, final String name, final int playedTurns,
            final int endYear, final long[] liberty, final int crownRemainder)
            throws IOException {
        write(context, source, name, playedTurns, endYear, liberty,
                crownRemainder, false, false, null);
    }

    private static JsonObject unit(final int id, final String type) {
        final JsonObject action = new JsonObject();
        action.addProperty("type", "noAction");
        final JsonObject unit = new JsonObject();
        unit.addProperty("id", id);
        unit.addProperty("type", type);
        unit.addProperty("ownerId", "Dutch");
        unit.add("action", action);
        return unit;
    }

    private static JsonObject cargo(final JsonObject... slots) {
        final JsonArray array = new JsonArray();
        for (final JsonObject slot : slots) {
            array.add(slot);
        }
        final JsonObject cargo = new JsonObject();
        cargo.add("slots", array);
        return cargo;
    }

    private static JsonObject slotOfUnit(final int unitId) {
        final JsonObject slot = new JsonObject();
        slot.addProperty("unitId", unitId);
        return slot;
    }

    private static JsonObject slotOfGoods(final String goods,
            final int amount) {
        final JsonObject slot = new JsonObject();
        slot.addProperty("goodsType", goods);
        slot.addProperty("amount", amount);
        return slot;
    }

    private static JsonObject person(final int id, final String placeKey) {
        final JsonObject person = unit(id, "COLONIST");
        person.addProperty("workUpdates", 0);
        person.addProperty("missionary", false);
        person.add(placeKey, new JsonObject());
        person.add("cargo", cargo());
        person.addProperty("movementThirds", MovementPoints.ofPoints(1));
        person.addProperty("tools", 0);
        person.addProperty("holdingGuns", false);
        person.addProperty("mounted", false);
        return person;
    }

    private static JsonObject voyage(final boolean toEurope,
            final boolean originalToEurope, final int position,
            final int weather, final int originalTurns) {
        final boolean sameWay = toEurope == originalToEurope;
        final JsonObject voyage = new JsonObject();
        voyage.addProperty("isTravelToEurope", toEurope);
        voyage.addProperty("originalToEurope", originalToEurope);
        voyage.addProperty("position", position);
        voyage.addProperty("weather", weather);
        voyage.addProperty("originalTurns", originalTurns);
        voyage.addProperty("remainigTurns",
                sameWay ? originalTurns - position : position);
        return voyage;
    }

    private static JsonObject sailing(final JsonObject ship, final int laneY,
            final JsonObject voyage) {
        final JsonObject lane = new JsonObject();
        lane.addProperty("x", 1);
        lane.addProperty("y", laneY);
        ship.add("departureLocation", lane);
        ship.addProperty("departureDirection", "west");
        ship.add("placeHighSeas", voyage);
        return ship;
    }

    /** The populated home Europe of the Dutch; see prepareWithHomeEurope. */
    private static void addHomeEurope(final JsonArray units) {
        final JsonObject inbound = unit(INBOUND_SHIP, "MERCHANTMAN");
        inbound.add("cargo",
                cargo(slotOfGoods("ORE", 7), slotOfUnit(INBOUND_PASSENGER),
                        new JsonObject(), new JsonObject()));
        inbound.addProperty("movementThirds", MovementPoints.ofPoints(1));
        units.add(sailing(inbound, 17, voyage(true, true, 1, 1, 4)));
        units.add(person(INBOUND_PASSENGER, "placeCargoSlot"));

        final JsonObject docked = unit(PORT_SHIP, "GALLEON");
        docked.add("cargo",
                cargo(slotOfUnit(PORT_PASSENGER), slotOfGoods("MUSKET", 40),
                        new JsonObject(), new JsonObject(), new JsonObject()));
        docked.addProperty("movementThirds", MovementPoints.ofPoints(6));
        docked.add("placeEuropePort", new JsonObject());
        units.add(docked);
        units.add(person(PORT_PASSENGER, "placeCargoSlot"));
        units.add(person(PIER_PERSON, "placeEuropePort"));

        final JsonObject outbound = unit(OUTBOUND_SHIP, "GALLEON");
        outbound.add("cargo", cargo(new JsonObject(), new JsonObject(),
                new JsonObject(), new JsonObject(), new JsonObject()));
        outbound.addProperty("movementThirds", MovementPoints.ofPoints(6));
        units.add(sailing(outbound, 19, voyage(false, false, 0, 0, 2)));
    }

    /** @return identities of the colonies the nation owns in the saved file */
    private static JsonArray ownedColonyIds(final JsonArray colonies,
            final String owner) {
        final JsonArray out = new JsonArray();
        for (final JsonElement colony : colonies) {
            final JsonObject saved = colony.getAsJsonObject();
            if (owner.equals(saved.get("ownerName").getAsString())) {
                out.add(saved.get("id").getAsInt());
            }
        }
        return out;
    }

    private static void write(final TestContext context, final File source,
            final String name, final int playedTurns, final int endYear,
            final long[] liberty, final Integer crownRemainder,
            final boolean homeEurope, final boolean declared,
            final JsonObject intervention, final Soldier... soldiers)
            throws IOException {
        final JsonObject saved = new Gson()
                .fromJson(Files.readString(source.toPath()), JsonObject.class);
        final JsonObject model = saved.getAsJsonObject("model");
        final JsonObject calendar = model.getAsJsonObject("calendar");
        calendar.addProperty("numberOfPlayedTurns", playedTurns);
        calendar.addProperty("endYear", endYear);
        final JsonArray colonies = model.getAsJsonArray("colonies");
        for (int index = 0; index < liberty.length; index++) {
            colonies.get(index).getAsJsonObject().addProperty("liberty",
                    liberty[index]);
        }
        final JsonArray units = model.getAsJsonArray("units");
        for (final Soldier soldier : soldiers) {
            units.add(soldier.toJson());
        }
        if (homeEurope) {
            addHomeEurope(units);
            // Two adjacent lane squares allow an actual westward departure.
            final JsonArray tiles = model.getAsJsonObject("map")
                    .getAsJsonArray("tiles");
            final String row = tiles.get(17).getAsString();
            final int separator = row.indexOf(':');
            final String[] terrain = row.substring(separator + 1).split(",");
            terrain[1] = terrain[0];
            tiles.set(17,
                    new com.google.gson.JsonPrimitive(
                            row.substring(0, separator + 1)
                                    + String.join(",", terrain)));
        }
        if (declared) {
            for (final JsonElement element : model
                    .getAsJsonObject("gameManager").getAsJsonArray("players")) {
                final JsonObject player = element.getAsJsonObject();
                if ("Dutch".equals(player.get("name").getAsString())
                        && !player.has("whosKingThisPlayerIs")) {
                    player.addProperty("declaredIndependence", true);
                    // The declaration is dated by the saved calendar (R-0196).
                    player.addProperty("declarationYear",
                            RebellionJson.currentYear(model));
                    // The declaration fixes the colonies the Dutch own now.
                    player.add("rebellionColonyIds",
                            ownedColonyIds(colonies, "Dutch"));
                    if (intervention != null) {
                        player.add("intervention", intervention);
                    }
                }
            }
        }
        if (crownRemainder != null) {
            for (final JsonElement element : model
                    .getAsJsonObject("gameManager").getAsJsonArray("players")) {
                final JsonObject player = element.getAsJsonObject();
                if (player.has("whosKingThisPlayerIs")) {
                    player.getAsJsonObject("royalRoster")
                            .addProperty("remainder", crownRemainder);
                }
            }
        }
        final File prepared = new File(System.getProperty("user.home"), name);
        Files.writeString(prepared.toPath(), saved.toString());
        when(context.getClassFromGuice(FileSelectingService.class)
                .loadFile(any(File.class))).thenReturn(prepared);
    }
}
