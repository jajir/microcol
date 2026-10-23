package org.microcol.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.microcol.model.Location;
import org.microcol.page.TestContext;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Scoped runtime fixture for the calculated result (R-0196). It starts from the
 * valid open rebellion of {@link RoyalVictoryFixture}: the Dutch declared in
 * the saved calendar year with all their colonies, the Crown holds one colony,
 * and no victory is recorded yet. It adds only a saved history of destroyed
 * native settlements, so the real battle that wins the rebellion is what
 * records the first victory. The shared file keeps its content.
 */
final class ResultHistoryFixture {

    private static final String DUTCH = "Dutch";

    /** Native settlements the Dutch destroyed in the saved history. */
    static final int DESTROYED_SETTLEMENTS = 1;

    private ResultHistoryFixture() {
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
        RoyalVictoryFixture.prepare(context, source, name, crownColonyId,
                attackFrom);
        final File prepared = new File(System.getProperty("user.home"), name);
        final JsonObject saved = new Gson().fromJson(
                Files.readString(prepared.toPath()), JsonObject.class);
        for (final JsonElement element : saved.getAsJsonObject("model")
                .getAsJsonObject("gameManager").getAsJsonArray("players")) {
            final JsonObject player = element.getAsJsonObject();
            if (DUTCH.equals(player.get("name").getAsString())) {
                player.addProperty("nativeSettlementsDestroyed",
                        DESTROYED_SETTLEMENTS);
            }
        }
        Files.writeString(prepared.toPath(), saved.toString());
    }
}
