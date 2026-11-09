package org.akropia.model.campaign;

import java.io.File;
import java.time.Instant;
import java.util.List;
import java.util.function.Function;

import org.akropia.model.Difficulty;
import org.akropia.model.EuropeanNation;
import org.akropia.model.GameOverResult;
import org.akropia.model.AkropiaException;
import org.akropia.model.Model;
import org.akropia.model.Player;
import org.akropia.model.RecruitmentPool;
import org.akropia.model.RoyalRoster;
import org.akropia.model.Congress;
import org.akropia.model.TaxState;
import org.akropia.model.campaign.po.GameModelPo;
import org.akropia.model.campaign.po.GameModelPoDao;
import org.akropia.model.campaign.po.SaveMetadataPo;
import org.akropia.model.campaign.po.SaveSummaryPo;
import org.akropia.model.campaign.po.SavedGames;
import org.akropia.model.campaign.po.TurnSnapshot;
import org.akropia.model.generation.GeneratedWorld;
import org.akropia.model.generation.GeneratedWorldSettings;
import org.akropia.model.generation.NorthAmericaWorld;
import org.akropia.model.generation.ScenarioDraft;
import org.akropia.model.store.ModelPo;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.gson.JsonElement;

/**
 * Load and save model campaign from to file.
 */
@Singleton
public final class GameModelDao {

    private final GameModelPoDao gameModelPoDao;

    private final CampaignManager campaignManager;

    private final MissionFactoryManager missionFactoryManager;

    @Inject
    GameModelDao(final GameModelPoDao gameModelPoDao,
            final CampaignManager campaignManager,
            final MissionFactoryManager missionFactoryManager) {
        this.gameModelPoDao = Preconditions.checkNotNull(gameModelPoDao);
        this.campaignManager = Preconditions.checkNotNull(campaignManager);
        this.missionFactoryManager = Preconditions
                .checkNotNull(missionFactoryManager);
    }

    /**
     * Load a player save; only a valid saved-game archive is accepted.
     */
    public GameModel loadFromFile(final File file, final EventBus eventBus) {
        Preconditions.checkNotNull(file, "Save file is null");
        Preconditions.checkNotNull(eventBus, "Event bus is null");
        return makeFromModelPo(gameModelPoDao.loadPlayerGame(file), eventBus);
    }

    /**
     * Load an authored scenario in plain JSON, the same validation as a save.
     * Used by test setup that turns an authored position into a player save.
     */
    public GameModel loadScenarioFromFile(final File file,
            final EventBus eventBus) {
        Preconditions.checkNotNull(file, "Scenario file is null");
        Preconditions.checkNotNull(eventBus, "Event bus is null");
        return makeFromModelPo(gameModelPoDao.loadScenarioFromFile(file),
                eventBus);
    }

    public GameModel loadFromClassPath(final String fileName,
            final EventBus eventBus) {
        return makeFromModelPo(gameModelPoDao.loadFromClassPath(fileName),
                eventBus);
    }

    public GameModel createFreePlay(final FreePlayMap map,
            final String playerName, final Difficulty difficulty,
            final EuropeanNation nation, final EventBus eventBus) {
        final String name = Player.normalizeDisplayName(playerName);
        Preconditions.checkNotNull(map, "Map is null");
        Preconditions.checkNotNull(difficulty, "Difficulty is null");
        Preconditions.checkNotNull(nation, "Nation is null");
        Preconditions.checkNotNull(eventBus, "Event bus is null");
        Preconditions.checkArgument(
                map.isNationSelectable() || nation == EuropeanNation.DUTCH,
                "Authored maps support the Dutch nation only");
        final GameModelPo source = gameModelPoDao
                .loadFromClassPath(map.getResource());
        if (map == FreePlayMap.NORTH_AMERICA) {
            source.setModel(NorthAmericaWorld.create(nation, difficulty));
        } else if (map == FreePlayMap.REFERENCE) {
            source.setModel(ReferenceExpedition
                    .create(source.getModel().getMap(), nation, difficulty));
        } else {
            source.getModel().setDifficulty(difficulty);
            source.getModel().setCombatRandomState(0L);
            source.getModel().getGameManager().getPlayers().stream()
                    // Only colonial nations get European state; Crowns read
                    // their nation's and native nations have none.
                    .filter(player -> player.getWhosKingThisPlayerIs() == null
                            && player.getNativeNation() == null)
                    .forEach(player -> {
                        player.setRecruitment(RecruitmentPool.initial());
                        player.setCongress(Congress.initial());
                        player.setTax(TaxState.initial());
                    });
            // A new authored game starts every Crown at its fresh force.
            source.getModel().getGameManager().getPlayers().stream()
                    .filter(player -> player.getWhosKingThisPlayerIs() != null)
                    .forEach(player -> player
                            .setRoyalRoster(RoyalRoster.initial(difficulty)));
        }
        return finishFreePlay(source, name, eventBus);
    }

    /**
     * Create a New World from generated terrain with the reference expedition
     * and the native nations. The complete state is built before it is
     * returned, so a failed generation leaves the caller's active game as it
     * was.
     *
     * @param settings   required generation settings
     * @param playerName required player name
     * @param difficulty required difficulty
     * @param nation     required nation the player controls
     * @param eventBus   required event bus of the new game
     * @return new game in its start state
     * @throws IllegalStateException when no valid world could be generated
     */
    public GameModel createGeneratedFreePlay(
            final GeneratedWorldSettings settings, final String playerName,
            final Difficulty difficulty, final EuropeanNation nation,
            final EventBus eventBus) {
        final String name = Player.normalizeDisplayName(playerName);
        Preconditions.checkNotNull(settings, "Settings are null");
        Preconditions.checkNotNull(difficulty, "Difficulty is null");
        Preconditions.checkNotNull(nation, "Nation is null");
        Preconditions.checkNotNull(eventBus, "Event bus is null");
        final GameModelPo source = gameModelPoDao
                .loadFromClassPath(FreePlayMap.REFERENCE.getResource());
        source.setModel(GeneratedWorld.create(settings, nation, difficulty));
        return finishFreePlay(source, name, eventBus);
    }

    public GameModel createGeneratedFreePlay(
            final org.akropia.model.store.ModelPo world,
            final String playerName, final EventBus eventBus) {
        final String name = Player.normalizeDisplayName(playerName);
        final GameModelPo source = gameModelPoDao
                .loadFromClassPath(FreePlayMap.REFERENCE.getResource());
        source.setModel(world);
        return finishFreePlay(source, name, eventBus);
    }

    /**
     * Create a new game from an authored scenario exactly as authored (R-0435).
     * Playability is checked first, so a rejected scenario leaves the caller's
     * active game as it was.
     *
     * @param scenario required authored scenario
     * @param eventBus required event bus of the new game
     * @return new game in its start state
     * @throws org.akropia.model.generation.ScenarioDraftException when the game
     *                                                             cannot start
     *                                                             it
     */
    public GameModel createAuthoredScenario(final GameModelPo scenario,
            final EventBus eventBus) {
        Preconditions.checkNotNull(eventBus, "Event bus is null");
        return makeFromModelPo(ScenarioDraft.of(scenario).playableScenario(),
                eventBus);
    }

    private GameModel finishFreePlay(final GameModelPo source,
            final String name, final EventBus eventBus) {
        final GameModel game = makeFromModelPo(source, eventBus);
        // Only a new free-play game learns its sea route from Europe; loaded
        // saves and campaign missions keep their exploration (R-0424).
        game.getModel().rememberStartingArrivalRoutes();
        game.getModel().getPlayers().stream().filter(Player::isHuman)
                .findFirst().orElseThrow().setDisplayName(name);
        return game;
    }

    private GameModel makeFromModelPo(final GameModelPo gameModelPo,
            final EventBus eventBus) {
        Preconditions.checkArgument(
                gameModelPo != null && gameModelPo.getModel() != null
                        && gameModelPo.getMissionName() != null,
                "Incomplete saved game");
        final ModelPo saved = gameModelPo.getModel();
        Preconditions.checkArgument(saved.getCalendar() != null
                && saved.getMap() != null && saved.getGameManager() != null
                && saved.getUnits() != null && saved.getColonies() != null
                && saved.getNativeSettlements() != null
                && saved.getNativeSettlementKnowledge() != null
                && saved.getNativeTribalAnger() != null
                && saved.getNativeLocalAlarm() != null
                && saved.getEuropeanRelations() != null,
                "Incomplete saved game model");
        Preconditions
                .checkArgument(
                        saved.getGameManager().getPlayers() != null
                                && saved.getGameManager().getPlayers().stream()
                                        .allMatch(player -> player != null
                                                && player.getName() != null
                                                && player.getVisible() != null
                                                && player.getVisible()
                                                        .getVisible() != null),
                        "Incomplete saved player");
        Preconditions.checkArgument(
                saved.getColonies().stream().allMatch(colony -> colony != null),
                "Invalid saved colony");
        Preconditions
                .checkArgument(
                        saved.getUnits().stream().allMatch(unit -> unit != null
                                && unit.getId() != null
                                && unit.getType() != null
                                && unit.getOwnerId() != null
                                && unit.getAction() != null
                                && unit.getCargo() != null
                                && unit.getCargo().getSlots() != null
                                && (unit.getPlaceMap() == null || unit
                                        .getPlaceMap().getLocation() != null)),
                        "Incomplete saved unit");
        final ExternalGameOver makeGameOver = new ExternalGameOver(
                MissionImpl.GAME_OVER_REASON_ALL_GOALS_ARE_DONE);
        final List<Function<Model, GameOverResult>> evaluators = MissionImpl
                .getPredefinedGameOverEvaluators();
        evaluators.add(makeGameOver);
        final Model model = Model.make(saved, evaluators);

        final Campaign campaign = campaignManager
                .getCampaignByName(resolve(gameModelPo.getCampaignName()));
        final CampaignMission campaignMission = campaign
                .getMisssionByName(gameModelPo.getMissionName());

        final MissionCreationContext context = new MissionCreationContext(
                eventBus, model, gameModelPo, campaignManager, campaignMission,
                makeGameOver);
        final Mission<?> mission = missionFactoryManager
                .make(campaignMission.getMissionName(), context);
        final GameModel game = new GameModel(campaign, campaignMission, mission,
                model);
        game.restorePlaythroughId(gameModelPo.getPlaythroughId());
        return game;
    }

    private CampaignName resolve(final String name) {
        if (CampaignNames.defaultCampaign.toString().equals(name)) {
            return CampaignNames.defaultCampaign;
        } else if (CampaignNames.freePlay.toString().equals(name)) {
            return CampaignNames.freePlay;
        }
        throw new AkropiaException(
                String.format("Unabble to resolve campaign '%s'", name));
    }

    public void saveToFile(final String fileName, final GameModel gameMode) {
        saveGame(new File(fileName), gameMode, SavedGames.Kind.MANUAL, null,
                null);
    }

    public void saveNamedGame(final File file, final GameModel game,
            final String name) {
        saveGame(file, game, SavedGames.Kind.MANUAL,
                Preconditions.checkNotNull(name), null);
    }

    /**
     * Save the game as one archive whose metadata and summary describe the same
     * snapshot as its game data (R-0382).
     *
     * @param file    required target file
     * @param game    required game
     * @param kind    required kind of the save
     * @param name    optional save name, normalized and validated before any
     *                write
     * @param preview optional PNG preview of the same state
     * @return the gameplay state the written file holds (R-0387)
     */
    public JsonElement saveGame(final File file, final GameModel game,
            final SavedGames.Kind kind, final String name,
            final byte[] preview) {
        Preconditions.checkNotNull(file);
        Preconditions.checkNotNull(game);
        Preconditions.checkNotNull(kind);
        final String given = name == null ? null
                : Player.normalizeDisplayName(name);
        final GameModelPo snapshot = game.getModelPo();
        final SaveSummaryPo summary = summary(game, snapshot);
        // Every save has a readable name; without one it names the player and
        // the date.
        snapshot.setSaveName(given == null ? defaultName(summary) : given);
        gameModelPoDao.savePlayerGame(file.getAbsolutePath(), snapshot,
                metadata(snapshot, kind, summary), preview);
        return gameModelPoDao.gameplayState(snapshot);
    }

    /**
     * @param game required game
     * @return its current gameplay state in the form a save would store it
     * @throws org.akropia.model.TurnPreparationException while the turn is not
     *                                                    complete
     */
    public JsonElement gameplayState(final GameModel game) {
        return gameModelPoDao
                .gameplayState(Preconditions.checkNotNull(game).getModelPo());
    }

    /** Atomically replace the next shared circular slot. */
    public TurnSnapshot saveTurnSnapshot(final GameModel game,
            final SavedGames saves, final byte[] preview) {
        synchronized (saves) {
            final long sequence = saves.nextSequence();
            final File file = saves.nextTurnSnapshot(sequence);
            final GameModelPo snapshot = game.getModelPo();
            final SaveSummaryPo summary = summary(game, snapshot);
            snapshot.setSaveName(defaultName(summary));
            final SaveMetadataPo metadata = metadata(snapshot,
                    SavedGames.Kind.AUTOMATIC, summary)
                    .withAutosaveSequence(sequence);
            gameModelPoDao.savePlayerGame(file.getAbsolutePath(), snapshot,
                    metadata, preview);
            return new TurnSnapshot(file,
                    gameModelPoDao.gameplayState(snapshot));
        }
    }

    private static SaveSummaryPo summary(final GameModel game,
            final GameModelPo snapshot) {
        return SaveSummaryPo.describe(game.getModel(),
                snapshot.getCampaignName(), snapshot.getMissionName());
    }

    private static SaveMetadataPo metadata(final GameModelPo snapshot,
            final SavedGames.Kind kind, final SaveSummaryPo summary) {
        return SaveMetadataPo.of(snapshot.getSaveName(), kind, Instant.now(),
                snapshot.getPlaythroughId(), summary);
    }

    /**
     * @return the played nationality and game year within the 64-code-point
     *         name limit
     */
    public static String defaultName(final SaveSummaryPo summary) {
        return summary.getNation().toLowerCase(java.util.Locale.ROOT) + "-"
                + summary.getYear();
    }

}
