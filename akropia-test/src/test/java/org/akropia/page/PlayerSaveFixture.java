package org.akropia.page;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.akropia.model.Model;
import org.akropia.model.campaign.CampaignModule;
import org.akropia.model.campaign.GameModel;
import org.akropia.model.campaign.GameModelDao;
import org.akropia.model.campaign.MissionImpl;
import org.akropia.model.campaign.po.GameModelPo;
import org.akropia.model.campaign.po.GameModelPoDao;
import org.akropia.model.campaign.po.SaveMetadataPo;
import org.akropia.model.campaign.po.SaveSummaryPo;
import org.akropia.model.campaign.po.SavedGames;
import org.akropia.model.campaign.store.CampaignsDao;
import org.akropia.model.campaign.store.CampaignsPo;
import org.mockito.Mockito;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;

/**
 * Test setup at the player-file boundary (R-0382). Fixtures are authored
 * scenarios in plain JSON, while Load, Import and Continue accept only player
 * save archives. A fixture used as a save is therefore first saved as a player
 * archive by the production owners: the scenario is loaded with the game's
 * validation and saved with its summary. The game's boundary is not relaxed.
 */
public final class PlayerSaveFixture {

    private static final Path DIRECTORY = Path.of("target", "test-saves");

    private static final byte[] ZIP = { 'P', 'K', 3, 4 };

    private static GameModelDao games;

    private PlayerSaveFixture() {
    }

    /**
     * Files a scenario deliberately chooses although the game must reject them,
     * e.g. malformed content. Only these reach the game unconverted.
     */
    private static final Set<File> REJECTED = ConcurrentHashMap.newKeySet();

    /**
     * Let a deliberately malformed file reach the game as chosen, so its
     * rejection is tested instead of failing the fixture conversion.
     *
     * @param file required file the game must reject
     */
    public static void chooseAsRejected(final File file) {
        REJECTED.add(file.getAbsoluteFile());
    }

    /**
     * The file a chosen fixture reaches the game as. A player save, a missing
     * file and a file marked by {@link #chooseAsRejected(File)} stay as they
     * are; any other file must be a valid authored scenario and becomes a new
     * player save.
     *
     * @param chosen optional file returned by the stubbed file chooser
     * @return file passed to the game
     * @throws IllegalStateException when a chosen fixture is not a valid
     *                               scenario
     */
    public static File asPlayerSave(final File chosen) {
        if (chosen == null || !chosen.isFile() || isArchive(chosen)
                || REJECTED.contains(chosen.getAbsoluteFile())) {
            return chosen;
        }
        return toPlayerSave(chosen);
    }

    /**
     * Save a valid authored scenario as a new player save for a direct player
     * load. A player save is returned as it is.
     *
     * @param scenario required valid authored scenario or player save
     * @return player save of the scenario
     * @throws IllegalStateException with the path and cause when the scenario
     *                               is not valid
     */
    public static File toPlayerSave(final File scenario) {
        if (isArchive(scenario)) {
            return scenario;
        }
        try {
            Files.createDirectories(DIRECTORY);
            final File target = Files.createTempFile(DIRECTORY,
                    scenario.getName() + "-", ".akropia").toFile();
            target.deleteOnExit();
            save(scenario, target);
            return target;
        } catch (IOException | RuntimeException failed) {
            throw new IllegalStateException(
                    "Fixture " + scenario.getAbsolutePath()
                            + " cannot be saved as a player save",
                    failed);
        }
    }

    /**
     * Place an authored scenario as a player save, for example as the recovery
     * point that Continue opens.
     *
     * @param scenario required valid authored scenario
     * @param target   required save file
     */
    public static void copyAsSave(final File scenario, final Path target) {
        try {
            save(scenario, target.toFile());
        } catch (RuntimeException failed) {
            throw new IllegalStateException(
                    "Fixture " + scenario.getAbsolutePath()
                            + " cannot be saved as " + target,
                    failed);
        }
    }

    /**
     * Write a player save whose game data are deliberately invalid. Its
     * metadata describe a valid authored state with the identity and campaign
     * keys of the data, so only the game data are wrong.
     *
     * @param target required save file
     * @param data   required invalid persistent model
     * @param valid  class-path scenario the summary describes
     */
    public static void writeInvalidGame(final File target,
            final GameModelPo data, final String valid) {
        final GameModelPoDao dao = new GameModelPoDao();
        final Model model = Model.make(dao.loadFromClassPath(valid).getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        if (data.getPlaythroughId() == null) {
            data.setPlaythroughId(UUID.randomUUID().toString());
        }
        dao.savePlayerGame(target.getAbsolutePath(), data, SaveMetadataPo
                .of("Invalid game data", SavedGames.Kind.MANUAL, Instant.now(),
                        data.getPlaythroughId(), SaveSummaryPo.describe(model,
                                data.getCampaignName(), data.getMissionName())),
                null);
    }

    private static void save(final File scenario, final File target) {
        final GameModel game = games().loadScenarioFromFile(scenario,
                new EventBus());
        if (!game.getModel().isGameStarted()) {
            game.startGame();
        }
        games().saveGame(target, game, SavedGames.Kind.MANUAL, null, null);
    }

    private static synchronized GameModelDao games() {
        if (games == null) {
            final CampaignsDao campaigns = Mockito.mock(CampaignsDao.class);
            Mockito.when(campaigns.load()).thenReturn(new CampaignsPo());
            games = Guice
                    .createInjector(new CampaignModule(),
                            binder -> binder.bind(CampaignsDao.class)
                                    .toInstance(campaigns))
                    .getInstance(GameModelDao.class);
        }
        return games;
    }

    private static boolean isArchive(final File file) {
        try (InputStream in = Files.newInputStream(file.toPath())) {
            return Arrays.equals(in.readNBytes(ZIP.length), ZIP);
        } catch (IOException unreadable) {
            return false;
        }
    }
}
