package org.microcol.model.campaign;

import java.io.File;
import java.util.List;
import java.util.function.Function;

import org.microcol.model.Difficulty;
import org.microcol.model.EuropeanNation;
import org.microcol.model.GameOverResult;
import org.microcol.model.MicroColException;
import org.microcol.model.Model;
import org.microcol.model.Player;
import org.microcol.model.RecruitmentPool;
import org.microcol.model.Congress;
import org.microcol.model.TaxState;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.campaign.po.PersistingTool;
import org.microcol.model.campaign.po.RecoverySlot;
import org.microcol.model.store.ModelPo;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

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

    public GameModel loadFromFile(final File file, final EventBus eventBus) {
        Preconditions.checkNotNull(file, "Save file is null");
        Preconditions.checkNotNull(eventBus, "Event bus is null");
        return makeFromModelPo(gameModelPoDao.loadFromFile(file), eventBus);
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
                map == FreePlayMap.REFERENCE || nation == EuropeanNation.DUTCH,
                "Authored maps support the Dutch nation only");
        final GameModelPo source = gameModelPoDao
                .loadFromClassPath(map.getResource());
        if (map == FreePlayMap.REFERENCE) {
            source.setModel(ReferenceExpedition
                    .create(source.getModel().getMap(), nation, difficulty));
        } else {
            source.getModel().setDifficulty(difficulty);
            source.getModel().setCombatRandomState(0L);
            source.getModel().getGameManager().getPlayers().stream()
                    .filter(player -> player.getWhosKingThisPlayerIs() == null)
                    .forEach(player -> {
                        player.setRecruitment(RecruitmentPool.initial());
                        player.setCongress(Congress.initial());
                        player.setTax(TaxState.initial());
                    });
        }
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
        return new GameModel(campaign, campaignMission, mission, model);
    }

    private CampaignName resolve(final String name) {
        if (CampaignNames.defaultCampaign.toString().equals(name)) {
            return CampaignNames.defaultCampaign;
        } else if (CampaignNames.freePlay.toString().equals(name)) {
            return CampaignNames.freePlay;
        }
        throw new MicroColException(
                String.format("Unabble to resolve campaign '%s'", name));
    }

    public void saveToFile(final String fileName, final GameModel gameMode) {
        gameModelPoDao.saveToFile(fileName, gameMode.getModelPo());
    }

    public void saveRecoverySlots(final GameModel game,
            final PersistingTool paths, final List<RecoverySlot> slots) {
        Preconditions.checkNotNull(game);
        Preconditions.checkNotNull(paths);
        Preconditions.checkNotNull(slots);
        if (slots.isEmpty()) {
            return;
        }
        final GameModelPo snapshot = game.getModelPo();
        for (final RecoverySlot slot : slots) {
            gameModelPoDao.saveToFile(
                    paths.getRecoveryFile(slot).getAbsolutePath(), snapshot);
        }
    }

}
