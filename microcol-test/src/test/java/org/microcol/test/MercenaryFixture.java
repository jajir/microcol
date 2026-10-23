package org.microcol.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.microcol.page.TestContext;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Scoped runtime fixture for the paid mercenary offer (R-0169). It derives a
 * prepared copy of the shared independence scenario in which the Dutch have
 * already declared independence, stand one rebel update before the tenth, hold
 * 5000 gold and know France, a computer nation at peace with them. The shared
 * F03 file keeps its content; the intervention target is set out of reach so
 * that no earning or arrival notice mixes into the offer journey.
 */
final class MercenaryFixture {

    /** The year 1747 of the shared independence scenario, as in TC_155. */
    private static final int YEAR_1747 = 700;

    private static final int END_YEAR = 1900;

    /** Full liberty in the four colonies of the scenario, as in TC_155. */
    private static final long FULL = 1600L;

    /** A target that no update of a test can reach. */
    private static final int FAR_TARGET = 1_000_000;

    /** The next rebel update is the tenth one. */
    private static final int NINTH_UPDATE = 9;

    private static final int TREASURY = 5000;

    static final String ISSUER = "France";

    private MercenaryFixture() {
    }

    /**
     * Write the prepared copy and make the next Load game use it.
     *
     * @param context running application context
     * @param source  shared independence scenario
     * @param name    file name of the prepared copy
     * @param ships   saved ships with empty holds, such as the Crown ships that
     *                block a port, or none
     */
    static void prepare(final TestContext context, final File source,
            final String name, final IndependenceFixture.Ship... ships)
            throws IOException {
        IndependenceFixture.prepareDeclaredWithShips(context, source, name,
                YEAR_1747, END_YEAR, new long[] { FULL, FULL, FULL, FULL },
                IndependenceFixture.intervention(FAR_TARGET, 0, false, false),
                ships);
        final File prepared = new File(System.getProperty("user.home"), name);
        final JsonObject saved = new Gson().fromJson(
                Files.readString(prepared.toPath()), JsonObject.class);
        final JsonObject model = saved.getAsJsonObject("model");
        final JsonArray players = model.getAsJsonObject("gameManager")
                .getAsJsonArray("players");
        JsonObject dutch = null;
        for (final JsonElement element : players) {
            final JsonObject player = element.getAsJsonObject();
            if ("Dutch".equals(player.get("name").getAsString())
                    && !player.has("whosKingThisPlayerIs")) {
                dutch = player;
            }
        }
        if (dutch == null) {
            throw new IllegalStateException("The scenario has no Dutch nation");
        }
        dutch.addProperty("gold", TREASURY);
        dutch.addProperty("mercenaryRebelUpdates", NINTH_UPDATE);
        final JsonArray contacts = new JsonArray();
        contacts.add(ISSUER);
        dutch.add("knownPowers", contacts);

        // France is a computer nation cloned from the Dutch save without the
        // rebellion; it owns nothing and takes its turn before the Dutch.
        final JsonObject france = dutch.deepCopy();
        france.addProperty("name", ISSUER);
        france.addProperty("computer", true);
        france.addProperty("gold", 1000);
        france.addProperty("declaredIndependence", false);
        for (final String key : new String[] { "rebellionColonyIds",
                "declarationYear", "intervention", "mercenaryRebelUpdates" }) {
            france.remove(key);
        }
        // France has met nobody and remembers no colony.
        france.add("knownPowers", new JsonArray());
        france.add("colonySightings", new JsonArray());
        players.add(france);

        final JsonObject peace = new JsonObject();
        peace.addProperty("firstPlayer", "Dutch");
        peace.addProperty("secondPlayer", ISSUER);
        peace.addProperty("state", "PEACE");
        final JsonArray relations = new JsonArray();
        relations.add(peace);
        model.add("europeanRelations", relations);
        Files.writeString(prepared.toPath(), saved.toString());
    }
}
