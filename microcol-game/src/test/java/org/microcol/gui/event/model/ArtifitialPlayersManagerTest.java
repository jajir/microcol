package org.microcol.gui.event.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.microcol.ai.AbstractRobotPlayer;
import org.microcol.ai.NativeRobotPlayer;
import org.microcol.ai.RivalPlayerFactory;
import org.microcol.ai.SimpleAiPlayer;
import org.microcol.ai.StandardRivalPlayerFactory;
import org.microcol.gui.screen.game.gamepanel.AnimationManager;
import org.microcol.model.Model;
import org.microcol.model.Player;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;

/**
 * Only European computer rivals are created through the rival factory; the
 * default factory is the real AI and a test module can replace it.
 */
class ArtifitialPlayersManagerTest {

    private final Model model = mock(Model.class);
    private final AnimationManager animation = new AnimationManager(
            new EventBus());

    private static Player player(final boolean king, final boolean nativeNation,
            final boolean computer) {
        final Player player = mock(Player.class);
        when(player.isKing()).thenReturn(king);
        when(player.isNative()).thenReturn(nativeNation);
        when(player.isComputer()).thenReturn(computer);
        return player;
    }

    @Test
    void factoryCreatesOnlyTheEuropeanComputerRivals() {
        final Player nativeNation = player(false, true, true);
        final Player human = player(false, false, false);
        final Player firstRival = player(false, false, true);
        final Player secondRival = player(false, false, true);
        when(model.getPlayers()).thenReturn(
                List.of(nativeNation, human, firstRival, secondRival));
        final List<Player> created = new ArrayList<>();
        final RivalPlayerFactory recording = (m, p, lock) -> {
            created.add(p);
            return new NativeRobotPlayer(m, p, lock);
        };
        final ArtifitialPlayersManager manager = new ArtifitialPlayersManager(
                animation, recording);

        manager.initRobotPlayers(model);
        manager.destroyRobotPlayers();

        assertEquals(List.of(firstRival, secondRival), created);
    }

    @Test
    void defaultFactoryIsTheRealAiForEveryInjector() {
        final RivalPlayerFactory factory = Guice.createInjector()
                .getInstance(RivalPlayerFactory.class);
        assertSame(StandardRivalPlayerFactory.class, factory.getClass());

        final AbstractRobotPlayer robot = factory.createRival(model,
                player(false, false, true), animation);

        assertSame(SimpleAiPlayer.class, robot.getClass());
        robot.stop();
    }

    @Test
    void overrideModuleReplacesTheFactoryOfTheInjectedManager() {
        final Player rival = player(false, false, true);
        when(model.getPlayers()).thenReturn(List.of(rival));
        final List<Player> created = new ArrayList<>();
        final RivalPlayerFactory recording = (m, p, lock) -> {
            created.add(p);
            return new NativeRobotPlayer(m, p, lock);
        };
        final Injector injector = Guice.createInjector(binder -> {
            binder.bind(AnimationManager.class).toInstance(animation);
            binder.bind(ArtifitialPlayersManager.class).in(Singleton.class);
        }, binder -> binder.bind(RivalPlayerFactory.class)
                .toInstance(recording));
        final ArtifitialPlayersManager manager = injector
                .getInstance(ArtifitialPlayersManager.class);

        manager.initRobotPlayers(model);
        manager.destroyRobotPlayers();

        assertTrue(created.contains(rival));
        assertEquals(1, created.size());
    }
}
