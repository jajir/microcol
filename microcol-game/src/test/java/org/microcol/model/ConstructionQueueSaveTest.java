package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/** Stored queue data is validated without rewriting or advancing the game. */
class ConstructionQueueSaveTest {
    @TempDir
    Path directory;
    private final GameModelPoDao dao = new GameModelPoDao();

    static Stream<String> malformedQueues() {
        return Stream.of("null", "[null]",
                "[{\"type\": \"CONSTRUCTION\", \"id\": 0, \"constructionType\": \"WAREHOUSE\"}]",
                "[{\"type\": \"CONSTRUCTION\", \"id\": 0, \"constructionType\": \"WAREHOUSE\", \"buildHammers\": null}]",
                "[{\"type\": \"UNIT\", \"id\": 0, \"unitType\": \"WAGON\", \"constructionType\": \"WAREHOUSE\", \"buildHammers\": 0}]",
                "[{\"type\": \"UNIT\", \"id\": 0, \"unitType\": \"INVALID\", \"buildHammers\": 0}]",

                "[{\"id\":0,\"buildHammers\":0,\"constructionType\":\"WAREHOUSE\"}]",
                "[{\"type\":\"INVALID\",\"id\":0,\"buildHammers\":0}]",
                "[{\"type\":\"CONSTRUCTION\",\"id\":0,\"buildHammers\":0}]",
                "[{\"type\":\"CONSTRUCTION\",\"id\":0,\"buildHammers\":0,\"constructionType\":\"INVALID\"}]",
                "[{\"type\":\"UNIT\",\"id\":0,\"buildHammers\":0}]",
                "[{\"type\":\"UNIT\",\"id\":0,\"buildHammers\":0,\"unitType\":\"COLONIST\"}]",
                "[{\"type\":\"CONSTRUCTION\",\"buildHammers\":0,\"constructionType\":\"WAREHOUSE\"}]",
                "[{\"type\":\"CONSTRUCTION\",\"id\":null,\"buildHammers\":0,\"constructionType\":\"WAREHOUSE\"}]",
                "[{\"type\":\"CONSTRUCTION\",\"id\":-1,\"buildHammers\":0,\"constructionType\":\"WAREHOUSE\"}]",
                "[{\"type\":\"CONSTRUCTION\",\"id\":0,\"buildHammers\":-1,\"constructionType\":\"WAREHOUSE\"}]",
                "[{\"type\":\"CONSTRUCTION\",\"id\":0,\"buildHammers\":0,\"constructionType\":\"TOWN_HALL\"}]",
                "[{\"type\":\"CONSTRUCTION\",\"id\":0,\"buildHammers\":0,\"constructionType\":\"WAREHOUSE\",\"unitType\":\"WAGON\"}]",
                "[{\"type\":\"CONSTRUCTION\",\"id\":0,\"buildHammers\":0,\"constructionType\":\"WAREHOUSE\"},{\"type\":\"UNIT\",\"id\":0,\"buildHammers\":0,\"unitType\":\"WAGON\"}]",
                "[{\"type\":\"CONSTRUCTION\",\"id\":0,\"buildHammers\":0,\"constructionType\":\"WAREHOUSE\"},{\"type\":\"CONSTRUCTION\",\"id\":1,\"buildHammers\":0,\"constructionType\":\"WAREHOUSE\"}]",
                "[{\"type\":\"CONSTRUCTION\",\"id\":0,\"buildHammers\":0,\"constructionType\":\"WAREHOUSE\"},{\"type\":\"CONSTRUCTION\",\"id\":1,\"buildHammers\":0,\"constructionType\":\"WAREHOUSE_EXPANSION\"}]");
    }

    private Path saveWithQueue(final String queueJson) throws Exception {
        final Path file = directory.resolve("construction.microcol");
        dao.saveToFile(file.toString(), dao.loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol"));
        final Gson gson = new GsonBuilder().serializeNulls().create();
        final JsonObject json = gson.fromJson(Files.readString(file),
                JsonObject.class);
        for (final JsonElement entry : json.getAsJsonObject("model")
                .getAsJsonArray("colonies")) {
            final JsonObject colony = entry.getAsJsonObject();
            if (colony.get("name").getAsString().equals("Delft")) {
                colony.add("buildingQueue",
                        gson.fromJson(queueJson, JsonElement.class));
            }
        }
        Files.writeString(file, gson.toJson(json));
        return file;
    }

    @ParameterizedTest
    @MethodSource("malformedQueues")
    void malformedQueueIsRejectedWithoutChangingTheSave(final String queueJson)
            throws Exception {
        final Path file = saveWithQueue(queueJson);
        final byte[] before = Files.readAllBytes(file);
        assertThrows(IllegalArgumentException.class, () -> {
            final GameModelPo data = dao.loadFromFile(file.toFile());
            Model.make(data.getModel(),
                    MissionImpl.getPredefinedGameOverEvaluators());
        });
        assertArrayEquals(before, Files.readAllBytes(file));
    }

    @Test
    void priorProjectWithMissingPredecessorAndLowerPopulationRestoresWithoutCompletion()
            throws Exception {
        final Path file = saveWithQueue(
                "[{\"type\":\"CONSTRUCTION\",\"id\":23,\"buildHammers\":120,\"constructionType\":\"FORT\"}]");
        final byte[] before = Files.readAllBytes(file);
        final GameModelPo data = dao.loadFromFile(file.toFile());
        final Model model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Colony colony = model.getColonyByName("Delft").orElseThrow();
        assertEquals(1, colony.getUnitsInsideColony().size());
        assertFalse(
                colony.isContainsConstructionByType(ConstructionType.STOCKADE));
        assertFalse(colony.isContainsConstructionByType(ConstructionType.FORT));
        final ColonyBuildingItemProgress<?> item = colony
                .getColonyBuildingQueue().getBuildingQueue().get(0);
        assertEquals(23, item.getId());
        assertEquals(120, item.getBuildHammers());
        assertEquals(ConstructionType.FORT,
                ((ColonyBuildingItemProgressConstruction) item)
                        .getConstructionType());
        assertArrayEquals(before, Files.readAllBytes(file));
    }

    @Test
    void loadingAFullySuppliedProjectDoesNotRunConstruction() throws Exception {
        final Path file = saveWithQueue(
                "[{\"type\":\"CONSTRUCTION\",\"id\":23,\"buildHammers\":80,\"constructionType\":\"WAREHOUSE\"}]");
        final byte[] before = Files.readAllBytes(file);
        final GameModelPo data = dao.loadFromFile(file.toFile());
        final Model model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final int turns = model.getCalendar().getNumberOfPlayedTurns();
        model.startGame();
        final Colony colony = model.getColonyByName("Delft").orElseThrow();
        assertNotNull(
                colony.getConstructionByType(ConstructionType.WAREHOUSE_BASIC));
        assertFalse(colony
                .isContainsConstructionByType(ConstructionType.WAREHOUSE));
        assertEquals(1,
                colony.getColonyBuildingQueue().getBuildingQueue().size());
        final ColonyBuildingItemProgress<?> item = colony
                .getColonyBuildingQueue().getBuildingQueue().get(0);
        assertEquals(23, item.getId());
        assertEquals(80, item.getBuildHammers());
        assertEquals(turns, model.getCalendar().getNumberOfPlayedTurns());
        assertArrayEquals(before, Files.readAllBytes(file));
    }

}
