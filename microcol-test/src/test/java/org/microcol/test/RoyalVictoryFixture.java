package org.microcol.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

import org.microcol.gui.FileSelectingService;
import org.microcol.model.Location;
import org.microcol.model.MovementPoints;
import org.microcol.page.TestContext;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Scoped runtime fixture for the won rebellion (R-0191). It derives a prepared
 * copy of the independence scenario in which the Dutch have declared
 * independence with all their colonies. The Crown holds one colony with its
 * residents, and nothing else stands on that square, so no unit shares a square
 * with an enemy. No armed unit defends it: its unarmed residents defend as a
 * group, so one won battle returns the whole colony. The Crown's royal force is
 * cut to two reserve regulars: the rest of its roster is destroyed and the
 * ships are untouched. A Dutch armed colonist stands beside the colony, ready
 * to attack it. The shared file keeps its content.
 */
final class RoyalVictoryFixture {

    /** Name of the Crown in the shared scenario. */
    static final String CROWN = "Dutch's King";

    private static final String DUTCH = "Dutch";

    /** Identity of the Dutch colonist that attacks the colony. */
    static final int ATTACKER = 310;

    /** Ground regulars that the Crown keeps in reserve. */
    private static final int KEPT_GROUND = 2;

    private RoyalVictoryFixture() {
    }

    /**
     * Write the prepared copy and make the next Load game use it.
     *
     * @param context       running application context
     * @param source        shared scenario
     * @param name          file name of the prepared copy
     * @param crownColonyId identity of the colony that the Crown holds
     * @param attackFrom    free land square beside that colony for the attacker
     */
    static void prepare(final TestContext context, final File source,
            final String name, final int crownColonyId,
            final Location attackFrom) throws IOException {
        final JsonObject saved = new Gson()
                .fromJson(Files.readString(source.toPath()), JsonObject.class);
        final JsonObject model = saved.getAsJsonObject("model");
        final JsonArray colonies = model.getAsJsonArray("colonies");
        final JsonArray units = model.getAsJsonArray("units");
        // The declaration fixes the rebellion's colonies before any changes.
        for (final JsonElement element : model.getAsJsonObject("gameManager")
                .getAsJsonArray("players")) {
            final JsonObject player = element.getAsJsonObject();
            if (DUTCH.equals(player.get("name").getAsString())) {
                player.addProperty("declaredIndependence", true);
                player.add("rebellionColonyIds",
                        ownedColonyIds(colonies, DUTCH));
            }
            if (player.has("royalRoster")) {
                keepReserveGroundOnly(player.getAsJsonObject("royalRoster"));
            }
        }
        final Set<Integer> residents = new HashSet<>();
        for (final JsonElement element : colonies) {
            final JsonObject colony = element.getAsJsonObject();
            if (colony.get("id").getAsInt() == crownColonyId) {
                colony.addProperty("ownerName", CROWN);
                collectResidents(colony, residents);
            }
        }
        for (final JsonElement element : units) {
            final JsonObject unit = element.getAsJsonObject();
            if (residents.contains(unit.get("id").getAsInt())) {
                unit.addProperty("ownerId", CROWN);
            }
        }
        units.add(colonist(ATTACKER, DUTCH, attackFrom));
        final File prepared = new File(System.getProperty("user.home"), name);
        Files.writeString(prepared.toPath(), saved.toString());
        when(context.getClassFromGuice(FileSelectingService.class)
                .loadFile(any(File.class))).thenReturn(prepared);
    }

    /**
     * Keep the first ground regulars in reserve and destroy every other one.
     * Ships stay as they are, and a destroyed member carries no unit.
     */
    private static void keepReserveGroundOnly(final JsonObject roster) {
        int kept = 0;
        for (final JsonElement element : roster.getAsJsonArray("members")) {
            final JsonObject member = element.getAsJsonObject();
            if ("MAN_OF_WAR".equals(member.get("family").getAsString())) {
                continue;
            }
            if (kept < KEPT_GROUND) {
                kept++;
            } else {
                member.addProperty("state", "DESTROYED");
            }
        }
    }

    /** An armed civilian colonist standing on a map square. */
    private static JsonObject colonist(final int id, final String owner,
            final Location at) {
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
        unit.addProperty("ownerId", owner);
        unit.add("placeMap", place);
        unit.add("cargo", cargo);
        unit.add("action", action);
        unit.addProperty("movementThirds", MovementPoints.ofPoints(1));
        unit.addProperty("holdingGuns", true);
        unit.addProperty("mounted", false);
        unit.addProperty("veteran", false);
        return unit;
    }

    /** @return identities of the colonies the nation owns in the saved file */
    private static JsonArray ownedColonyIds(final JsonArray colonies,
            final String owner) {
        final JsonArray out = new JsonArray();
        for (final JsonElement element : colonies) {
            final JsonObject colony = element.getAsJsonObject();
            if (owner.equals(colony.get("ownerName").getAsString())) {
                out.add(colony.get("id").getAsInt());
            }
        }
        return out;
    }

    /** Workers of the fields and of every building slot of one colony. */
    private static void collectResidents(final JsonObject colony,
            final Set<Integer> residents) {
        for (final JsonElement field : colony.getAsJsonArray("colonyFields")) {
            addWorker(field.getAsJsonObject(), residents);
        }
        for (final JsonElement construction : colony
                .getAsJsonArray("constructions")) {
            for (final JsonElement slot : construction.getAsJsonObject()
                    .getAsJsonArray("slots")) {
                addWorker(slot.getAsJsonObject(), residents);
            }
        }
    }

    private static void addWorker(final JsonObject place,
            final Set<Integer> residents) {
        if (place.has("workerId")) {
            residents.add(place.get("workerId").getAsInt());
        }
    }
}
