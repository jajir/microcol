package org.akropia.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.akropia.gui.screen.game.gamepanel.AnimationManager;
import org.akropia.model.EuropeanNation;
import org.akropia.model.Location;
import org.akropia.model.Model;
import org.akropia.model.ModelListenerAdapter;
import org.akropia.model.NativeNation;
import org.akropia.model.Player;
import org.akropia.model.PlayerKing;
import org.akropia.model.TerrainType;
import org.akropia.model.UnitType;
import org.akropia.model.campaign.MissionImpl;
import org.akropia.model.event.TurnStartedEvent;
import org.akropia.model.generation.ScenarioDraft;
import org.akropia.model.store.ModelPo;
import org.akropia.model.unit.UnitWithCargo;
import org.junit.jupiter.api.Test;

import com.google.common.eventbus.EventBus;

/**
 * An authored scenario of the map editor (R-0435) with one European nation, its
 * Crown and one native tribe plays whole turns: every participant's turn
 * returns control to the player, whose expedition and the settlement remain.
 */
class AuthoredScenarioTurnTest {

    private static final Location SEA = Location.of(9, 4);

    private static final Location VILLAGE = Location.of(4, 4);

    @Test
    void authoredExpeditionPlaysSeveralTurns() throws Exception {
        final ScenarioDraft draft = ScenarioDraft.ocean(12, 10);
        for (int x = 3; x <= 6; x++) {
            for (int y = 3; y <= 5; y++) {
                draft.setTerrain(Location.of(x, y), TerrainType.GRASSLAND);
            }
        }
        draft.setNation(EuropeanNation.SPANISH);
        final int ship = draft.addUnit(UnitType.CARAVEL, SEA);
        draft.addPassenger(ship, UnitType.COLONIST);
        draft.addPassenger(ship, UnitType.EXPERT_FARMER);
        draft.addUnit(UnitType.SEASONED_SCOUT, Location.of(3, 5));
        draft.placeSettlement(VILLAGE, NativeNation.SIOUX, true);
        final ModelPo authored = draft.playableScenario().getModel();

        final Model model = Model.make(authored,
                MissionImpl.getPredefinedGameOverEvaluators());
        final List<AbstractRobotPlayer> robots = new ArrayList<>();
        final BlockingQueue<Player> humanTurns = new LinkedBlockingQueue<>();
        try {
            final AnimationManager animations = new AnimationManager(
                    new EventBus());
            for (final Player player : model.getPlayers()) {
                if (player.isKing()) {
                    robots.add(new KingRobotPlayer(model, (PlayerKing) player,
                            animations));
                } else if (player.isComputer()) {
                    robots.add(new SimpleAiPlayer(model, player, animations));
                }
            }
            model.addListener(new ModelListenerAdapter() {
                @Override
                public void onTurnStarted(final TurnStartedEvent event) {
                    if (event.getPlayer().isHuman()) {
                        humanTurns.add(event.getPlayer());
                    }
                }
            });
            model.startGame();
            final Player spain = model.getPlayerByName("Spain");
            assertSame(spain, humanTurns.poll(5, TimeUnit.SECONDS));
            for (int turn = 0; turn < 3; turn++) {
                spain.endTurn();
                assertSame(spain, humanTurns.poll(5, TimeUnit.SECONDS),
                        "Every turn returns control to the player");
            }

            assertEquals(4, spain.getAllUnits().size());
            assertEquals(List.of(UnitType.COLONIST, UnitType.EXPERT_FARMER),
                    ((UnitWithCargo) model.getUnitById(ship)).getCargo()
                            .getSlots().stream().filter(slot -> !slot.isEmpty())
                            .map(slot -> slot.getUnit().orElseThrow().getType())
                            .collect(java.util.stream.Collectors.toList()));
            assertEquals(SEA, model.getUnitById(ship).getLocation());
        } finally {
            robots.forEach(AbstractRobotPlayer::stop);
            model.stop();
        }
    }

}
