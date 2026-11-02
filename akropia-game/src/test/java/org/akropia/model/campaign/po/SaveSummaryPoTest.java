package org.akropia.model.campaign.po;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.akropia.model.Model;
import org.akropia.model.Player;
import org.akropia.model.Unit;
import org.akropia.model.campaign.MissionImpl;
import org.akropia.model.unit.UnitFreeColonist;

import com.google.gson.Gson;

/** The saved overview reuses the owners of each counted value (R-0382). */
class SaveSummaryPoTest {

    @TempDir
    Path directory;

    private final GameModelPoDao dao = new GameModelPoDao();

    private Model freePlay() {
        final Model model = Model.make(
                dao.loadFromClassPath("/maps/free-play.akropia").getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        model.startGame();
        return model;
    }

    @Test
    void summaryCountsEachOwnPersonOnceAndOnlyShipsAsShips() {
        final Model model = freePlay();
        final Player human = model.getPlayers().stream().filter(Player::isHuman)
                .findFirst().orElseThrow();

        final SaveSummaryPo summary = SaveSummaryPo.describe(model, "freePlay",
                "freePlay");

        assertEquals(human.getDisplayName(), summary.getPlayerName());
        assertEquals("DUTCH", summary.getNation());
        assertEquals(model.getDifficulty().name(), summary.getDifficulty());
        assertEquals(model.getCalendar().getCurrentYear(), summary.getYear());
        assertEquals(human.getGold(), summary.getGold());
        assertEquals(human.getNationalRebelSupport().getDisplayPercent(),
                summary.getRebelSupportPercent());
        assertEquals(human.getColonies().size(), summary.getColonyCount());
        // People on the map, aboard ships and in Europe count once each;
        // ships, wagons and artillery are no people.
        final long people = human.getAllUnits().stream()
                .filter(unit -> unit instanceof UnitFreeColonist)
                .filter(unit -> !unit.isCrownSeized()).map(Unit::getId)
                .collect(Collectors.toSet()).size();
        assertEquals(people, summary.getPeopleCount());
        assertTrue(summary.getPeopleCount() > 0);
        assertEquals(
                human.getAllUnits().stream()
                        .filter(unit -> !unit.isCrownSeized())
                        .filter(unit -> unit.getType().isShip()).count(),
                summary.getShipCount());
        assertEquals("freePlay", summary.getCampaignName());
        assertEquals("freePlay", summary.getMissionName());
    }

    @Test
    void loadingIgnoresTheDisplayedSummary() throws Exception {
        final GameModelPo data = dao
                .loadFromClassPath("/maps/free-play.akropia");
        final Model model = freePlay();
        final Path file = directory.resolve("game.akropia");
        data.setPlaythroughId(java.util.UUID.randomUUID().toString());
        dao.savePlayerGame(file.toString(), data,
                SaveMetadataPo.of("Free play", SavedGames.Kind.MANUAL,
                        Instant.now(), data.getPlaythroughId(),
                        SaveSummaryPo.describe(model, null, null)),
                null);
        final String metadata = new String(
                SaveArchives.read(file).get("metadata.json"),
                StandardCharsets.UTF_8);
        SaveArchives.replace(file, "metadata.json",
                metadata.replaceAll("\"gold\": \\d+", "\"gold\": 999999"));

        assertEquals(999999,
                dao.loadMetadata(file.toFile()).getSummary().getGold());
        final Gson gson = new Gson();
        assertEquals(gson.toJsonTree(data),
                gson.toJsonTree(dao.loadPlayerGame(file.toFile())));
    }
}
