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
import org.akropia.model.campaign.po.PersistingTool;
import org.akropia.model.campaign.po.RecoverySlot;
import org.akropia.model.campaign.po.SaveMetadataPo;
import org.akropia.model.campaign.po.SaveSummaryPo;
import org.akropia.model.campaign.po.SavedGames;
import org.akropia.model.campaign.po.TurnSnapshot;
import org.akropia.model.generation.GeneratedWorld;
import org.akropia.model.generation.GeneratedWorldSettings;
import org.akropia.model.generation.NorthAmericaWorld;
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

    private GameModel finishFreePlay(final GameModelPo source,
            final String name, final EventBus eventBus) {
        final GameModel game = makeFromModelPo(source, eventBus);
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

    /**
     * Save the prepared turn as the next turn snapshot of its playthrough
     * (R-0388). The new snapshot is written completely first; only then is it
     * copied into the recovery slots and are the oldest snapshots beyond
     * {@link SavedGames#TURN_HISTORY} removed. A failed write changes no file.
     *
     * @param game    required game at a prepared human turn
     * @param saves   required save catalogue
     * @param paths   required save locations
     * @param slots   required recovery slots receiving the same snapshot
     * @param preview optional PNG preview of the same state
     * @return the written snapshot with its slot and cleanup outcome
     * @throws AkropiaException when the snapshot cannot be written
     */
    public TurnSnapshot saveTurnSnapshot(final GameModel game,
            final SavedGames saves, final PersistingTool paths,
            final List<RecoverySlot> slots, final byte[] preview) {
        Preconditions.checkNotNull(game);
        Preconditions.checkNotNull(saves);
        Preconditions.checkNotNull(paths);
        Preconditions.checkNotNull(slots);
        final File file = saves.nextTurnSnapshot(game.getPlaythroughId());
        final JsonElement state = saveGame(file, game,
                SavedGames.Kind.AUTOMATIC, null, preview);
        AkropiaException slotFailure = null;
        try {
            for (final RecoverySlot slot : slots) {
                gameModelPoDao.copyPlayerGame(file,
                        paths.getRecoveryFile(slot));
            }
        } catch (AkropiaException failed) {
            slotFailure = failed;
        }
        boolean pruned;
        try {
            pruned = saves.pruneTurnSnapshots(game.getPlaythroughId());
        } catch (AkropiaException unavailable) {
            pruned = false;
        }
        return new TurnSnapshot(file, state, slotFailure, pruned);
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
     * @return the player's name and the game date within the 64-code-point name
     *         limit
     */
    static String defaultName(final SaveSummaryPo summary) {
        final String date = " " + summary.getYear()
                + (summary.getSeason() == null ? ""
                        : " " + summary.getSeason());
        String player = summary.getPlayerName();
        // The 64-code-point limit of Player.normalizeDisplayName.
        final int room = 64 - date.length();
        if (player.codePointCount(0, player.length()) > room) {
            player = player.substring(0, player.offsetByCodePoints(0, room));
        }
        return Player.normalizeDisplayName(player + date);
    }

}
