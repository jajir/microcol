package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.microcol.model.campaign.FreePlayMap;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;

class DifficultyProfileTest {

    @TempDir
    Path directory;

    private final GameModelPoDao dao = new GameModelPoDao();

    @Test
    void bundledScenariosStartAtConquistador() {
        for (final FreePlayMap map : FreePlayMap.values()) {
            assertEquals(Difficulty.CONQUISTADOR,
                    make(dao.loadFromClassPath(map.getResource()))
                            .getDifficulty());
        }
        for (final String mission : new String[] {
                "/maps/default-campaign-mission-0-findNewWorld.microcol",
                "/maps/default-campaign-mission-1-buildArmy.microcol",
                "/maps/default-campaign-mission-2-thrive.microcol" }) {
            assertEquals(Difficulty.CONQUISTADOR,
                    make(dao.loadFromClassPath(mission)).getDifficulty());
        }
    }

    @Test
    void everyProfileSurvivesModelAndDiskRoundTrip() {
        for (final Difficulty difficulty : Difficulty.values()) {
            final GameModelPo game = dao
                    .loadFromClassPath(FreePlayMap.ORIGINAL.getResource());
            game.getModel().setDifficulty(difficulty);
            CrownRosterFixtures.freshCrownRosters(game.getModel());
            final Model model = make(game);
            assertEquals(difficulty, model.getDifficulty());
            model.startGame();
            game.setModel(model.save());
            assertEquals(difficulty, game.getModel().getDifficulty());
            final Path file = directory
                    .resolve(difficulty.name() + ".microcol");
            dao.saveToFile(file.toString(), game);
            final GameModelPo loaded = dao.loadFromFile(file.toFile());
            assertEquals(difficulty, loaded.getModel().getDifficulty());
            assertEquals(difficulty, make(loaded).getDifficulty());
        }
    }

    @Test
    void missingOrUnknownProfileIsRejected() throws IOException {
        final GameModelPo game = dao
                .loadFromClassPath(FreePlayMap.ORIGINAL.getResource());
        game.getModel().setDifficulty(Difficulty.CONQUISTADOR);
        final Path file = directory.resolve("invalid.microcol");
        dao.saveToFile(file.toString(), game);
        final String valid = Files.readString(file);
        final String savedField = "\"difficulty\": \"CONQUISTADOR\"";
        assertTrue(valid.contains(savedField));
        Files.writeString(file, valid.replace(savedField + ",", ""));
        assertThrows(IllegalArgumentException.class,
                () -> make(dao.loadFromFile(file.toFile())));
        Files.writeString(file,
                valid.replace(savedField, "\"difficulty\": \"UNKNOWN\""));
        assertThrows(IllegalArgumentException.class,
                () -> make(dao.loadFromFile(file.toFile())));
    }

    private Model make(final GameModelPo game) {
        return Model.make(game.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }
}
