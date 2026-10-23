package org.microcol.page;

import java.util.TreeSet;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Authors a declared rebellion in a saved scenario file (R-0192). A declaration
 * fixes the colonies the nation owns at that moment; a colony that the Crown
 * has taken since stays a member and is named explicitly. Loading never
 * rebuilds the members from ownership, so every declared fixture names them.
 * The declaration also Crown-seizes the nation's units in its home port
 * (R-0187), as the game does, because loading rejects an unseized one.
 */
public final class RebellionJson {

    private RebellionJson() {
    }

    /**
     * Declare the saved player's nation with its members.
     *
     * @param model           required saved model object
     * @param player          required saved player object of the nation
     * @param capturedMembers colonies that were the nation's and are now held
     *                        by another owner, such as its Crown
     */
    public static void declare(final JsonObject model, final JsonObject player,
            final int... capturedMembers) {
        final TreeSet<Integer> members = new TreeSet<>();
        final String owner = player.get("name").getAsString();
        for (final JsonElement element : model.getAsJsonArray("colonies")) {
            final JsonObject colony = element.getAsJsonObject();
            if (owner.equals(colony.get("ownerName").getAsString())) {
                members.add(colony.get("id").getAsInt());
            }
        }
        for (final int id : capturedMembers) {
            members.add(id);
        }
        final JsonArray ids = new JsonArray();
        members.forEach(ids::add);
        player.addProperty("declaredIndependence", true);
        player.add("rebellionColonyIds", ids);
        seizeHomePort(model, owner);
    }

    /**
     * Crown-seize the nation's units in its home port: units on the pier or at
     * the port, and the passengers aboard those ships. Units keep their places
     * and cargo.
     */
    private static void seizeHomePort(final JsonObject model,
            final String owner) {
        final JsonArray units = model.getAsJsonArray("units");
        final TreeSet<Integer> seized = new TreeSet<>();
        for (final JsonElement element : units) {
            final JsonObject unit = element.getAsJsonObject();
            if (!owner.equals(unit.get("ownerId").getAsString())
                    || !unit.has("placeEuropePort")) {
                continue;
            }
            seized.add(unit.get("id").getAsInt());
            if (unit.has("cargo")) {
                for (final JsonElement slot : unit.getAsJsonObject("cargo")
                        .getAsJsonArray("slots")) {
                    if (slot.getAsJsonObject().has("unitId")) {
                        seized.add(slot.getAsJsonObject().get("unitId")
                                .getAsInt());
                    }
                }
            }
        }
        for (final JsonElement element : units) {
            final JsonObject unit = element.getAsJsonObject();
            if (owner.equals(unit.get("ownerId").getAsString())
                    && seized.contains(unit.get("id").getAsInt())) {
                unit.addProperty("crownSeized", true);
            }
        }
    }

    /**
     * Declare the nation as {@link #declare}, or leave it undeclared without
     * members.
     */
    public static void set(final JsonObject model, final JsonObject player,
            final boolean declared) {
        if (declared) {
            declare(model, player);
        } else {
            player.addProperty("declaredIndependence", false);
            player.remove("rebellionColonyIds");
        }
    }
}
