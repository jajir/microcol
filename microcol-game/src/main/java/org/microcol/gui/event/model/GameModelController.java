package org.microcol.gui.event.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.microcol.ai.AbstractRobotPlayer;
import org.microcol.ai.KingPlayer;
import org.microcol.ai.SimpleAiPlayer;
import org.microcol.gui.gamepanel.AnimationManager;
import org.microcol.gui.gamepanel.SelectedTileManager;
import org.microcol.gui.mainmenu.CenterViewController;
import org.microcol.gui.mainmenu.CenterViewEvent;
import org.microcol.model.GoodAmount;
import org.microcol.model.GoodTrade;
import org.microcol.model.GoodType;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Path;
import org.microcol.model.Player;
import org.microcol.model.Unit;
import org.microcol.model.campaign.ModelMission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Exchange events between model and GUI.
 * <p>
 * There is one class instance in runtime.
 * </p>
 */
public class GameModelController {

    private final static Logger logger = LoggerFactory.getLogger(GameModelController.class);

    private final ModelEventManager modelEventManager;

    private final AnimationManager animationManager;

    private ModelListenerImpl modelListener;

    private ModelMission modelMission;

    private List<AbstractRobotPlayer> players;

    private final CenterViewController centerViewController;

    private final SelectedTileManager selectedTileManager;

    @Inject
    public GameModelController(final ModelEventManager modelEventManager,
            final AnimationManager animationManager,
            final CenterViewController centerViewController,
            final SelectedTileManager selectedTileManager) {
        this.modelEventManager = Preconditions.checkNotNull(modelEventManager);
        this.animationManager = Preconditions.checkNotNull(animationManager);
        this.centerViewController = Preconditions.checkNotNull(centerViewController);
        this.selectedTileManager = Preconditions.checkNotNull(selectedTileManager);
        modelMission = null;
        modelListener = null;
    }

    /**
     * It start new game from model and prepare King AI and players AI.
     * 
     * @param newModel
     *            required game model
     */
    void setAndStartModel(final ModelMission newModel, final MissionCallBack missionCallBack) {
        tryToStopGame();
        modelMission = Preconditions.checkNotNull(newModel);
        players = new ArrayList<>();
        modelMission.getModel().getPlayers().stream().filter(player -> player.isKing())
                .forEach(player -> {
                    players.add(new KingPlayer(modelMission.getModel(), player, animationManager));
                });
        modelMission.getModel().getPlayers().stream()
                .filter(player -> !player.isKing() && player.isComputer()).forEach(player -> {
                    players.add(
                            new SimpleAiPlayer(modelMission.getModel(), player, animationManager));
                });
        modelListener = new ModelListenerImpl(modelEventManager, this);
        modelMission.getModel().addListener(modelListener);
        modelMission.startGame(missionCallBack);
        if (getModel().getFocusedField() != null) {
            selectedTileManager.setSelectedTile(getModel().getFocusedField());
            centerViewController.fireEvent(new CenterViewEvent());
        }
    }

    public Model getModel() {
        Preconditions.checkState(modelMission != null, "Model is not ready");
        return modelMission.getModel();
    }

    public void captureColonyAt(final Unit movingUnit, final Location moveToLocation) {
        new Thread(() -> movingUnit.captureColony(moveToLocation)).start();
    }

    public boolean isModelReady() {
        return modelMission != null;
    }

    public Player getCurrentPlayer() {
        return getModel().getPlayers().stream().filter(Player::isHuman).findFirst()
                .orElseThrow(() -> new IllegalStateException("There is no human player"));
    }

    public GoodAmount getMaxBuyableGoodsAmount(final GoodType goodType) {
        final GoodTrade goodTrade = modelMission.getModel().getEurope()
                .getGoodTradeForType(goodType);
        return goodTrade.getAvailableAmountFor(getCurrentPlayer().getGold());
    }

    /**
     * Get human player, even in not on turn.
     * 
     * @return return human player
     */
    public Player getHumanPlayer() {
        // It use getCurrentPLaye implementation which is not correct.
        return getCurrentPlayer();
    }

    private void tryToStopGame() {
        if (modelMission != null) {
            Preconditions.checkArgument(modelMission != null);
            Preconditions.checkArgument(modelListener != null);
            modelMission.getModel().removeListener(modelListener);
            modelMission = null;
            modelListener = null;
            players.forEach(player -> player.stop());
            players = null;
        }
    }

    public void performMove(final Unit ship, final List<Location> path) {
        logger.debug("Start move ship: " + ship);
        new Thread(() -> modelMission.getModel().moveUnit(ship, Path.of(path))).start();
    }

    public void performFight(final Unit attacker, final Unit defender) {
        logger.debug("Start move ship: " + attacker);
        new Thread(() -> {
            /**
             * If it's necessary than move.
             */
            final Optional<List<Location>> locations = attacker.getPath(defender.getLocation(),
                    true);
            if (locations.isPresent() && !locations.get().isEmpty()) {
                modelMission.getModel().moveUnit(attacker, Path.of(locations.get()));
            }
            attacker.attack(defender.getLocation());
        }).start();
    }

    public void nextTurn() {
        logger.debug("Next Year event was triggered.");
        new Thread(() -> modelMission.getModel().endTurn()).start();
    }

    public void suspendAi() {
        players.forEach(player -> player.suspend());
    }

    public void resumeAi() {
        players.forEach(player -> player.resume());
    }

    /**
     * @return the modelCapgaign
     */
    public ModelMission getModelCampaign() {
        return modelMission;
    }

    public void stop() {
        modelMission = null;
    }

}
