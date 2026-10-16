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
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;

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
        return makeFromModelPo(gameModelPoDao.loadFromFile(file), eventBus);
    }

    public GameModel loadFromClassPath(final String fileName,
            final EventBus eventBus) {
        return makeFromModelPo(gameModelPoDao.loadFromClassPath(fileName),
                eventBus);
    }

    public GameModel createFreePlay(final FreePlayMap map,
            final String playerName, final Difficulty difficulty,
            final EventBus eventBus) {
        final String name = Player.normalizeDisplayName(playerName);
        final GameModelPo source = gameModelPoDao.loadFromClassPath(
                Preconditions.checkNotNull(map).getResource());
        Preconditions.checkNotNull(difficulty);
        if (map == FreePlayMap.REFERENCE) {
            source.setModel(
                    ReferenceExpedition.create(source.getModel().getMap(),
                            EuropeanNation.DUTCH, difficulty));
        } else {
            source.getModel().setDifficulty(difficulty);
        }
        final GameModel game = makeFromModelPo(source, eventBus);
        game.getModel().getPlayers().stream().filter(Player::isHuman)
                .findFirst().orElseThrow().setDisplayName(name);
        return game;
    }

    private GameModel makeFromModelPo(final GameModelPo gameModelPo,
            final EventBus eventBus) {
        final ExternalGameOver makeGameOver = new ExternalGameOver(
                MissionImpl.GAME_OVER_REASON_ALL_GOALS_ARE_DONE);
        final List<Function<Model, GameOverResult>> evaluators = MissionImpl
                .getPredefinedGameOverEvaluators();
        evaluators.add(makeGameOver);
        final Model model = Model.make(gameModelPo.getModel(), evaluators);

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

}
