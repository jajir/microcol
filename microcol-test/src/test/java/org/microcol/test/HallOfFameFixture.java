package org.microcol.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.microcol.model.BaseScore;
import org.microcol.model.Difficulty;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Authored saved ranking of the Hall of Fame for the R-0197 scenarios. The ten
 * rivals are stored in ranking order with totals above any score the prepared
 * Dutch nation reaches. Each rival is a conquistador game with only servants,
 * so its total is its servant count times the servant points, which the model
 * checks on load. The ranking file lives where the game keeps its saves.
 */
final class HallOfFameFixture {

    /** Number of authored rivals in the full ranking. */
    static final int RIVALS = 10;

    /** Servants of the weakest rival; the others have more. */
    private static final int WEAKEST_RIVAL_SERVANTS = 1_000_000;

    private HallOfFameFixture() {
    }

    /** Write the full ranking of ten rivals, replacing any saved ranking. */
    static void writeFullRanking() throws IOException {
        final JsonArray entries = new JsonArray();
        // Highest total first, as the saved ranking is kept in ranking order.
        for (int index = RIVALS; index >= 1; index--) {
            entries.add(rival(index));
        }
        final JsonObject saved = new JsonObject();
        saved.add("entries", entries);
        final Path path = file();
        Files.createDirectories(path.getParent());
        Files.writeString(path, saved.toString());
    }

    /** Remove the saved ranking so the scored result is the first entry. */
    static void removeRanking() throws IOException {
        Files.deleteIfExists(file());
    }

    /**
     * Bytes that are not a saved ranking; the scenarios keep them unchanged.
     */
    static final String INVALID_RANKING = "{ not a saved ranking";

    /** Write invalid saved bytes, replacing any saved ranking. */
    static void writeInvalidRanking() throws IOException {
        final Path path = file();
        Files.createDirectories(path.getParent());
        Files.writeString(path, INVALID_RANKING);
    }

    /** @return the saved ranking file in the user's save directory */
    static Path file() {
        return Path.of(System.getProperty("user.home"), ".microcol",
                "hall-of-fame.json");
    }

    private static JsonObject rival(final int index) {
        final int servants = WEAKEST_RIVAL_SERVANTS + index;
        final JsonObject entry = new JsonObject();
        entry.addProperty("identity", "f197-authored-rival-" + index);
        entry.addProperty("playerName", "Dutch");
        entry.addProperty("playerDisplayName", "Authored rival " + index);
        entry.addProperty("difficulty", Difficulty.CONQUISTADOR.name());
        entry.addProperty("servants", servants);
        entry.addProperty("colonists", 0);
        entry.addProperty("experts", 0);
        entry.addProperty("fathers", 0);
        entry.addProperty("gold", 0);
        entry.addProperty("supportPercent", 0);
        entry.addProperty("destroyedSettlements", 0);
        entry.addProperty("bellsBeyondTarget", 0);
        entry.addProperty("total", servants * BaseScore.SERVANT_POINTS);
        return entry;
    }
}
