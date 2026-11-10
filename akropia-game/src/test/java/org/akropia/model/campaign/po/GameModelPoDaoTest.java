package org.akropia.model.campaign.po;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * Test basic loading of game model.
 * <p>
 * DAO can't test logic error in data like non existing colony name.
 * </p>
 */
public class GameModelPoDaoTest {

    private static final String MAP = "/maps/default-campaign-mission-2-thrive.akropia";

    private GameModelPoDao dao = new GameModelPoDao();

    @TempDir
    Path directory;

    @Test
    public void test_loadFromClassPath_invalid_file_format() throws Exception {
        assertThrows(JsonSyntaxException.class, () -> {
            dao.loadFromClassPath("/maps/test-map-invalid-0x0.json");
        });
    }

    @Test
    public void test_loadFromClassPath_non_existing_file() {
        assertThrows(IllegalArgumentException.class, () -> {
            dao.loadFromClassPath("/maps/test-map-missing-0x0.json");
        });
    }

    @Test
    public void test_loadFromClassPath_curruped_file() {
        final JsonSyntaxException exception = assertThrows(
                JsonSyntaxException.class, () -> {
                    dao.loadFromClassPath("/maps/test-currupted-file.akropia");
                });

        assertEquals(
                "com.google.gson.stream.MalformedJsonException: Expected name at line 189 column 9 path $.model.units[9].action",
                exception.getMessage());
    }

    /** A valid manual player save of the thrive mission. */
    private Path validSave(final String name) throws Exception {
        final Path file = directory.resolve(name + ".akropia");
        SaveArchives.playerSave(file, dao.loadFromClassPath(MAP));
        return file;
    }

    @Test
    void archiveKeepsTheModelJsonMetadataAndPreviewTogether() throws Exception {
        final GameModelPo data = dao.loadFromClassPath(MAP);
        data.setPlaythroughId(UUID.randomUUID().toString());
        data.setSaveName("Přístav / první");
        final Path file = directory.resolve("game.akropia");
        final Instant savedAt = Instant.parse("2026-11-01T10:15:30Z");
        final byte[] preview = SaveArchives.pngHeader(960, 540);
        final SaveSummaryPo summary = SaveArchives
                .metadata(data, data.getSaveName()).getSummary();

        dao.savePlayerGame(file.toString(), data,
                SaveMetadataPo.of(data.getSaveName(), SavedGames.Kind.QUICK,
                        savedAt, data.getPlaythroughId(), summary),
                preview);

        // The archive has exactly the three root entries.
        assertEquals(
                java.util.List.of("metadata.json", "game.json", "preview.png"),
                new java.util.ArrayList<>(SaveArchives.read(file).keySet()));
        // The game entry is exactly the production model-to-JSON output, the
        // same text as the authored scenario format of that model.
        final Path scenario = directory.resolve("scenario.json");
        dao.saveScenarioToFile(scenario.toString(), data);
        assertEquals(Files.readString(scenario),
                dao.loadGameJson(file.toFile()));
        // Reading it back through the production adapters restores the model.
        final Gson gson = new Gson();
        assertEquals(gson.toJsonTree(data),
                gson.toJsonTree(dao.loadPlayerGame(file.toFile())));
        final SaveMetadataPo metadata = dao.loadMetadata(file.toFile());
        assertEquals(GameModelPoDao.FORMAT_VERSION,
                metadata.getFormatVersion());
        assertFalse(metadata.getCreatedWith().isBlank());
        assertEquals("Přístav / první", metadata.getSaveName());
        assertEquals(SavedGames.Kind.QUICK, metadata.getKind());
        assertEquals(savedAt, metadata.getSavedAt());
        assertEquals(data.getPlaythroughId(), metadata.getPlaythroughId());
        assertEquals(gson.toJsonTree(summary),
                gson.toJsonTree(metadata.getSummary()));
        assertArrayEquals(preview,
                dao.loadPreview(file.toFile()).orElseThrow());
    }

    @Test
    void metadataAreReadWithoutTheGameJson() throws Exception {
        final Path file = validSave("game");
        SaveArchives.replace(file, "game.json", "{ not json");
        final byte[] before = Files.readAllBytes(file);

        assertEquals(SavedGames.Kind.MANUAL,
                dao.loadMetadata(file.toFile()).getKind());
        assertReason(SaveArchiveException.Reason.INVALID, file);
        assertArrayEquals(before, Files.readAllBytes(file));
    }

    @Test
    void unsupportedVersionIsDistinguishedFromDamage() throws Exception {
        final Path file = validSave("game");
        editMetadata(file, json -> json.addProperty("formatVersion", 2));

        assertMetadataReason(SaveArchiveException.Reason.UNSUPPORTED, file);
        assertReason(SaveArchiveException.Reason.UNSUPPORTED, file);
    }

    @Test
    void missingOrInvalidRequiredMetadataRejectTheSave() throws Exception {
        final java.util.List<Consumer<JsonObject>> damages = java.util.List.of(
                json -> json.remove("createdWith"),
                json -> json.addProperty("createdWith", " "),
                json -> json.remove("saveName"),
                json -> json.addProperty("saveName", ""),
                json -> json.addProperty("saveName", "a".repeat(65)),
                json -> json.remove("kind"),
                json -> json.addProperty("kind", "OTHER"),
                json -> json.remove("savedAt"),
                json -> json.addProperty("savedAt", "yesterday"),
                json -> json.remove("playthroughId"),
                json -> json.addProperty("playthroughId", "not a playthrough"),
                json -> json.remove("summary"),
                json -> summary(json).remove("playerName"),
                json -> summary(json).remove("nation"),
                json -> summary(json).addProperty("difficulty", "EASYISH"),
                json -> summary(json).addProperty("year", 0),
                json -> summary(json).addProperty("season", "monsoon"),
                json -> summary(json).addProperty("gold", -1),
                json -> summary(json).addProperty("rebelSupportPercent", 101),
                json -> summary(json).addProperty("peopleCount", -1),
                json -> summary(json).addProperty("colonyCount", -1),
                json -> summary(json).addProperty("shipCount", -1),
                json -> summary(json).addProperty("missionName", " "));
        for (final Consumer<JsonObject> damage : damages) {
            final Path file = validSave("game");
            editMetadata(file, damage);
            assertMetadataReason(SaveArchiveException.Reason.INVALID, file);
            assertReason(SaveArchiveException.Reason.INVALID, file);
        }
    }

    @Test
    void metadataOfAnotherGameRejectTheGameOnLoad() throws Exception {
        for (final Consumer<JsonObject> other : java.util.List
                .<Consumer<JsonObject>>of(
                        json -> json.addProperty("playthroughId",
                                UUID.randomUUID().toString()),
                        json -> summary(json).addProperty("missionName",
                                "another mission"),
                        json -> summary(json).addProperty("campaignName",
                                "another campaign"))) {
            final Path file = validSave("game");
            editMetadata(file, other);
            // The listing still reads the metadata; loading compares them.
            dao.loadMetadata(file.toFile());
            assertReason(SaveArchiveException.Reason.INVALID, file);
        }
    }

    @Test
    void writerRequiresCompleteMetadataOfTheSameGame() throws Exception {
        final GameModelPo data = dao.loadFromClassPath(MAP);
        final SaveMetadataPo valid = SaveArchives.metadata(data, "Valid");
        final SaveSummaryPo summary = valid.getSummary();
        final Instant now = Instant.now();
        final String id = data.getPlaythroughId();

        assertThrows(IllegalArgumentException.class, () -> SaveMetadataPo
                .of("Valid", SavedGames.Kind.MANUAL, now, id, null));
        assertThrows(IllegalArgumentException.class, () -> SaveMetadataPo
                .of("Valid", SavedGames.Kind.MANUAL, now, null, summary));
        assertThrows(IllegalArgumentException.class, () -> SaveMetadataPo
                .of(null, SavedGames.Kind.MANUAL, now, id, summary));
        assertThrows(IllegalArgumentException.class,
                () -> SaveMetadataPo.of("Valid", null, now, id, summary));
        final Path file = directory.resolve("other.akropia");
        final SaveMetadataPo other = SaveMetadataPo.of("Other",
                SavedGames.Kind.MANUAL, now, UUID.randomUUID().toString(),
                summary);
        assertThrows(IllegalArgumentException.class,
                () -> dao.savePlayerGame(file.toString(), data, other, null));
        assertFalse(Files.exists(file), "Nothing is written");
    }

    @Test
    void unexpectedMissingDuplicateAndPathEntriesAreRejected()
            throws Exception {
        final Path valid = validSave("valid");
        final Map<String, byte[]> entries = SaveArchives.read(valid);
        final Path file = directory.resolve("game.akropia");

        for (final String extra : new String[] { "notes.txt", "../game.json",
                "saves/metadata.json", "game.json/" }) {
            final Map<String, byte[]> changed = new LinkedHashMap<>(entries);
            changed.put(extra, new byte[] { 1 });
            SaveArchives.write(file, changed);
            assertMetadataReason(SaveArchiveException.Reason.INVALID, file);
            assertReason(SaveArchiveException.Reason.INVALID, file);
        }
        for (final String missing : new String[] { "metadata.json",
                "game.json" }) {
            final Map<String, byte[]> changed = new LinkedHashMap<>(entries);
            changed.remove(missing);
            SaveArchives.write(file, changed);
            assertReason(SaveArchiveException.Reason.INVALID, file);
        }
        SaveArchives.writeDuplicate(file, entries, "game.json",
                "{}".getBytes(StandardCharsets.UTF_8));
        assertMetadataReason(SaveArchiveException.Reason.INVALID, file);
        assertReason(SaveArchiveException.Reason.INVALID, file);
    }

    @Test
    void entriesBeyondTheirLimitsAreRejectedByTheirActualSize()
            throws Exception {
        final Path file = validSave("game");
        final Map<String, byte[]> entries = SaveArchives.read(file);
        final byte[] metadata = new byte[GameModelPoDao.MAX_METADATA_BYTES + 1];
        java.util.Arrays.fill(metadata, (byte) ' ');
        System.arraycopy(entries.get("metadata.json"), 0, metadata, 0,
                entries.get("metadata.json").length);
        entries.put("metadata.json", metadata);
        SaveArchives.write(file, entries);

        assertMetadataReason(SaveArchiveException.Reason.INVALID, file);
        assertReason(SaveArchiveException.Reason.INVALID, file);
    }

    @Test
    void missingDamagedOrOversizedPreviewDoesNotInvalidateTheGame()
            throws Exception {
        final GameModelPo data = dao.loadFromClassPath(MAP);
        final Path file = directory.resolve("game.akropia");
        SaveArchives.playerSave(file, data);
        assertFalse(dao.loadPreview(file.toFile()).isPresent());

        for (final byte[] image : new byte[][] { { 1, 2, 3 },
                SaveArchives.pngHeader(961, 540),
                SaveArchives.pngHeader(960, 541),
                SaveArchives.pngHeader(0, 10) }) {
            final Map<String, byte[]> entries = SaveArchives.read(file);
            entries.put("preview.png", image);
            SaveArchives.write(file, entries);
            assertFalse(dao.loadPreview(file.toFile()).isPresent());
            dao.loadMetadata(file.toFile());
            assertEquals(new Gson().toJsonTree(data),
                    new Gson().toJsonTree(dao.loadPlayerGame(file.toFile())));
        }
    }

    @Test
    void writerOmitsAPreviewOutsideTheLimits() throws Exception {
        final GameModelPo data = dao.loadFromClassPath(MAP);
        final Path file = directory.resolve("game.akropia");
        dao.savePlayerGame(file.toString(), data,
                SaveArchives.metadata(data, "Wide"),
                SaveArchives.pngHeader(2000, 100));

        assertFalse(SaveArchives.read(file).containsKey("preview.png"));
        assertFalse(dao.loadPreview(file.toFile()).isPresent());
        dao.loadPlayerGame(file.toFile());
    }

    @Test
    void plainJsonIsNotAPlayerSave() throws Exception {
        final Path file = directory.resolve("plain.akropia");
        dao.saveScenarioToFile(file.toString(), dao.loadFromClassPath(MAP));

        assertMetadataReason(SaveArchiveException.Reason.INVALID, file);
        assertReason(SaveArchiveException.Reason.INVALID, file);
        assertFalse(dao.loadPreview(file.toFile()).isPresent());
    }

    @Test
    void authoredScenariosStayPlainJsonOfTheSameModel() throws Exception {
        final GameModelPo data = dao.loadFromClassPath(MAP);
        final Path file = directory.resolve("scenario.akropia");

        dao.saveScenarioToFile(file.toString(), data);

        final String text = Files.readString(file);
        assertTrue(text.startsWith("{"), "Scenario is plain JSON");
        final Gson gson = new Gson();
        assertEquals(gson.toJsonTree(data),
                gson.toJsonTree(dao.loadScenarioFromFile(file.toFile())));
        assertNull(dao.loadScenarioFromFile(file.toFile()).getSaveName());
        // A player save is never read as a scenario.
        final Path save = validSave("save");
        assertThrows(RuntimeException.class,
                () -> dao.loadScenarioFromFile(save.toFile()));
    }

    private static JsonObject summary(final JsonObject metadata) {
        return metadata.getAsJsonObject("summary");
    }

    private static void editMetadata(final Path file,
            final Consumer<JsonObject> edit) throws Exception {
        final JsonObject json = new JsonParser()
                .parse(new String(SaveArchives.read(file).get("metadata.json"),
                        StandardCharsets.UTF_8))
                .getAsJsonObject();
        edit.accept(json);
        SaveArchives.replace(file, "metadata.json", json.toString());
    }

    private void assertReason(final SaveArchiveException.Reason expected,
            final Path file) throws Exception {
        final byte[] before = Files.readAllBytes(file);
        assertEquals(expected, assertThrows(SaveArchiveException.class,
                () -> dao.loadPlayerGame(file.toFile())).getReason());
        assertArrayEquals(before, Files.readAllBytes(file),
                "Reading never repairs the file");
    }

    private void assertMetadataReason(
            final SaveArchiveException.Reason expected, final Path file) {
        assertEquals(expected, assertThrows(SaveArchiveException.class,
                () -> dao.loadMetadata(file.toFile())).getReason());
    }

    @Test
    void editorRecognizesArchivesByContentWithoutChangingReaders()
            throws Exception {
        final Path archive = validSave("archive");
        final Path renamed = directory.resolve("archive.json");
        Files.copy(archive, renamed);
        final Path scenario = directory.resolve("scenario.akropia");
        dao.saveScenarioToFile(scenario.toString(), dao.loadFromClassPath(MAP));
        assertTrue(dao.isPlayerSaveArchive(archive.toFile()));
        assertTrue(dao.isPlayerSaveArchive(renamed.toFile()));
        assertFalse(dao.isPlayerSaveArchive(scenario.toFile()));
        assertThrows(JsonSyntaxException.class,
                () -> dao.loadScenarioFromFile(archive.toFile()));
        assertThrows(SaveArchiveException.class,
                () -> dao.loadPlayerGame(scenario.toFile()));
        final Path broken = directory.resolve("broken.akropia");
        Files.write(broken, new byte[] { 'P', 'K', 3, 4, 0 });
        assertTrue(dao.isPlayerSaveArchive(broken.toFile()));
        assertThrows(SaveArchiveException.class,
                () -> dao.loadPlayerGame(broken.toFile()));
        Files.write(broken, new byte[0]);
        assertFalse(dao.isPlayerSaveArchive(broken.toFile()));
    }

}
