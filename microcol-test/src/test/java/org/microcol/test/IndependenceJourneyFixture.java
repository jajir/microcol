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
import org.microcol.page.TestContext;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The one initial world of the connected independence journey (R-0220, UC-118),
 * derived before the game loads from the independence scenario F03 and never
 * touched again. F03 already holds the four Dutch colonies New Amsterdam (0, at
 * 25,14), Delft (1, at 22,12), Haarlem (2, at 23,17) and Leiden (3, at 19,11)
 * with eight residents each, forty Dutch colonists, two galleons and the Dutch
 * Crown with its recorded royal force. No native settlement exists.
 *
 * <p>
 * Changes, all legal saved data: the game is scored free play on the Discoverer
 * profile, so the royal force is the fresh Discoverer roster of four infantry,
 * two cavalry, two artillery and two men-of-war and the intervention target is
 * ceil(500 x 75 / 100) = 375 bells; the calendar stands at turn 698, spring
 * 1747; the Dutch hold 1,500 gold, below the 5,000 at which mercenaries are
 * offered; every colony holds 762 liberty of its 1,600, which is 47 percent,
 * and its three elder statesmen work in the town hall, with a newspaper; every
 * colony stores 100 corn, so the two derived updates can neither starve a
 * resident nor reach the 200 of a birth; New Amsterdam has a completed custom
 * house and a warehouse that stores 200, with 160 ore; colonies 0, 1 and 2 hold
 * four armed, mounted veterans each on their square, and Leiden holds none. The
 * saved combat random state is the smallest whose first six draws are below
 * 0.2. The prepared file declares nothing, holds no intervention, ships no
 * royal unit and sets no victory, so every one of those is earned by play.
 * </p>
 */
final class IndependenceJourneyFixture {

    static final String DUTCH = "Dutch";

    static final String CROWN = RoyalVictoryFixture.CROWN;

    static final int NEW_AMSTERDAM = 0;

    static final Location NEW_AMSTERDAM_AT = Location.of(25, 14);

    static final String NEW_AMSTERDAM_NAME = "New Amsterdam";

    /** Gold of the Dutch treasury at the start. */
    static final int GOLD = 1500;

    /** Liberty before the 38 bells that bring each colony to 800 of 1,600. */
    static final long LIBERTY = 762;

    /** Ore stored in New Amsterdam; the part above 100 can be exported. */
    static final int ORE = 160;

    /** Corn of every colony: no famine and no birth in the derived updates. */
    static final int CORN = 100;

    /** First veteran identity; colony c holds 500 + 4c to 503 + 4c. */
    static final int FIRST_VETERAN = 500;

    /** Armed, mounted veterans on the square of each garrisoned colony. */
    static final int VETERANS_PER_COLONY = 4;

    /** Colonies that hold veterans: New Amsterdam, Delft and Haarlem. */
    static final int GARRISONED_COLONIES = 3;

    /** Played turns at the start; turns 698 to 701 are the year 1747. */
    static final int TURN = 698;

    private static final long GAMMA = 0x9E3779B97F4A7C15L;
    private static final int DRAWS = 6;
    private static final double LOW_DRAW = 0.2;

    /** Starting force of the Discoverer profile: ids run in this order. */
    private static final String[] ROSTER = { "INFANTRY", "INFANTRY", "INFANTRY",
            "INFANTRY", "CAVALRY", "CAVALRY", "ARTILLERY", "ARTILLERY",
            "MAN_OF_WAR", "MAN_OF_WAR" };

    private IndependenceJourneyFixture() {
    }

    /**
     * Smallest saved state whose first six draws are all below 0.2. It runs
     * only while the fixture is prepared and follows the model's own stream.
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
            final String name) throws IOException {
        final Gson gson = new Gson();
        final JsonObject saved = gson
                .fromJson(Files.readString(source.toPath()), JsonObject.class);
        RoyalVictoryFixture.toScoredFreePlay(saved);
        final JsonObject model = saved.getAsJsonObject("model");
        model.addProperty("difficulty", "DISCOVERER");
        model.addProperty("combatRandomState", seed());
        model.getAsJsonObject("calendar").addProperty("numberOfPlayedTurns",
                TURN);
        players(model);
        final JsonArray colonies = model.getAsJsonArray("colonies");
        for (final JsonElement element : colonies) {
            final JsonObject colony = element.getAsJsonObject();
            colony.addProperty("liberty", LIBERTY);
            colony.getAsJsonObject("colonyWarehouse").addProperty("CORN", CORN);
            staffTownHall(colony);
            final JsonObject newspaper = new JsonObject();
            newspaper.addProperty("type", "NEWSPAPER");
            newspaper.add("slots", new JsonArray());
            colony.getAsJsonArray("constructions").add(newspaper);
            for (final JsonElement construction : colony
                    .getAsJsonArray("constructions")) {
                if ("TOWN_HALL".equals(construction.getAsJsonObject()
                        .get("type").getAsString())) {
                    for (final JsonElement slot : construction.getAsJsonObject()
                            .getAsJsonArray("slots")) {
                        final int worker = slot.getAsJsonObject()
                                .get("workerId").getAsInt();
                        for (final JsonElement unit : model
                                .getAsJsonArray("units")) {
                            if (unit.getAsJsonObject().get("id")
                                    .getAsInt() == worker) {
                                unit.getAsJsonObject().addProperty("type",
                                        "ELDER_STATESMAN");
                            }
                        }
                    }
                }
            }
        }
        customHouse(colonies.get(NEW_AMSTERDAM).getAsJsonObject());
        final JsonArray units = model.getAsJsonArray("units");
        for (int index = 0; index < GARRISONED_COLONIES; index++) {
            final JsonObject colony = colonies.get(index).getAsJsonObject();
            final JsonObject at = colony.getAsJsonObject("location");
            final Location square = Location.of(at.get("x").getAsInt(),
                    at.get("y").getAsInt());
            for (int veteran = 0; veteran < VETERANS_PER_COLONY; veteran++) {
                units.add(veteran(
                        FIRST_VETERAN + VETERANS_PER_COLONY * index + veteran,
                        square));
            }
        }
        final File prepared = new File(System.getProperty("user.home"), name);
        Files.writeString(prepared.toPath(), saved.toString());
        when(context.getClassFromGuice(FileSelectingService.class)
                .loadFile(any(File.class))).thenReturn(prepared);
    }

    /**
     * Save through the player command; later Load selects this exact
     * checkpoint.
     */
    static File saveCheckpoint(final TestContext context,
            final org.microcol.page.GamePage game) {
        final File file = new File(System.getProperty("user.home"),
                "R0220-reference.microcol");
        when(context.getClassFromGuice(FileSelectingService.class).saveFile(
                any(File.class), org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(file);
        game.saveGame();
        when(context.getClassFromGuice(FileSelectingService.class)
                .loadFile(any(File.class))).thenReturn(file);
        return file;
    }

    /** Dutch purse, and the Discoverer starting force of the Crown. */
    private static void players(final JsonObject model) {
        for (final JsonElement element : model.getAsJsonObject("gameManager")
                .getAsJsonArray("players")) {
            final JsonObject player = element.getAsJsonObject();
            if (DUTCH.equals(player.get("name").getAsString())) {
                player.addProperty("gold", GOLD);
            }
            if (player.has("royalRoster")) {
                player.add("royalRoster", roster());
            }
        }
    }

    private static JsonObject roster() {
        final JsonArray members = new JsonArray();
        for (int index = 0; index < ROSTER.length; index++) {
            final JsonObject member = new JsonObject();
            member.addProperty("id", index + 1);
            member.addProperty("family", ROSTER[index]);
            member.addProperty("state", "RESERVE");
            members.add(member);
        }
        final JsonObject roster = new JsonObject();
        roster.addProperty("growthCount", 0);
        roster.addProperty("remainder", 0);
        roster.add("members", members);
        return roster;
    }

    /**
     * Move the workers of the tobacconist's house into the town hall of the
     * colony. They keep their identity, and only the slot changes.
     */
    private static void staffTownHall(final JsonObject colony) {
        JsonArray hall = null;
        JsonArray house = null;
        for (final JsonElement element : colony
                .getAsJsonArray("constructions")) {
            final JsonObject construction = element.getAsJsonObject();
            final String type = construction.get("type").getAsString();
            if ("TOWN_HALL".equals(type)) {
                hall = construction.getAsJsonArray("slots");
            } else if ("TOBACCONISTS_HOUSE".equals(type)) {
                house = construction.getAsJsonArray("slots");
            }
        }
        for (int slot = 0; slot < house.size(); slot++) {
            final JsonObject worker = house.get(slot).getAsJsonObject();
            hall.get(slot).getAsJsonObject().add("workerId",
                    worker.get("workerId"));
            worker.remove("workerId");
        }
    }

    /**
     * A completed custom house, the warehouse that replaces the basic one (a
     * colony has one warehouse, and only the warehouse stores 200 of a goods)
     * and the ore that the custom house may export.
     */
    private static void customHouse(final JsonObject colony) {
        final JsonArray constructions = colony.getAsJsonArray("constructions");
        for (final JsonElement element : constructions) {
            final JsonObject construction = element.getAsJsonObject();
            if ("BASIC_WAREHOUSE"
                    .equals(construction.get("type").getAsString())) {
                construction.addProperty("type", "WAREHOUSE");
            }
        }
        final JsonObject house = new JsonObject();
        house.addProperty("type", "CUSTOM_HOUSE");
        house.add("slots", new JsonArray());
        constructions.add(house);
        colony.getAsJsonObject("colonyWarehouse").addProperty("ORE", ORE);
    }

    /** An armed, mounted veteran colonist standing on a colony square. */
    private static JsonObject veteran(final int id, final Location at) {
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
        unit.addProperty("type", "COLONIST");
        unit.addProperty("ownerId", DUTCH);
        unit.add("placeMap", place);
        unit.add("cargo", cargo);
        unit.add("action", action);
        unit.addProperty("movementThirds", MovementPoints.ofPoints(1));
        unit.addProperty("holdingGuns", true);
        unit.addProperty("mounted", true);
        unit.addProperty("veteran", true);
        return unit;
    }
}
