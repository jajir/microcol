package org.microcol.gui.screen.statistics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.screen.ShowScreenEvent;
import org.microcol.model.ColonyAdvisorReport;
import org.microcol.model.ColonyAdvisorReport.ColonyStatus;
import org.microcol.model.Model;
import org.microcol.model.Player;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPoDao;

import com.google.common.eventbus.EventBus;

/**
 * R-0200 navigation guard: an Open request made from rows of one game never
 * opens anything in another game, even when that game has a colony with the
 * same ID, and an owned colony of the running game opens its colony screen
 * once.
 */
class ColonyAdvisorNavigatorTest {

    private static final String THRIVE = "/maps/default-campaign-mission-2-thrive.microcol";

    private static final String DUTCH = "Dutch";

    private final EventBus eventBus = mock(EventBus.class);

    private final GameModelController game = mock(GameModelController.class);

    private final ColonyAdvisorNavigator navigator = new ColonyAdvisorNavigator(
            game, eventBus);

    private static Model thrive() {
        return Model.make(
                new GameModelPoDao().loadFromClassPath(THRIVE).getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private static ColonyStatus firstColony(final Model model) {
        final ColonyAdvisorReport report = model
                .getColonyAdvisorReport(model.getPlayerByName(DUTCH));
        assertTrue(!report.getColonies().isEmpty(),
                "The reference game has colonies");
        return report.getColonies().get(0);
    }

    private void run(final Model running) {
        final Player human = running.getPlayerByName(DUTCH);
        when(game.isGameModelReady()).thenReturn(true);
        when(game.getModel()).thenReturn(running);
        when(game.getHumanPlayer()).thenReturn(human);
    }

    @Test
    void rowsOfAReplacedGameOpenNothingEvenWithTheSameColonyId() {
        final Model shown = thrive();
        final ColonyStatus colony = firstColony(shown);
        final Model replacement = thrive();
        assertTrue(replacement.getColonyById(colony.getColonyId()).isPresent(),
                "The replacement game has a colony with the same ID");
        run(replacement);

        assertEquals(ColonyAdvisorNavigator.Result.UNAVAILABLE,
                navigator.open(shown, colony));
        verifyZeroInteractions(eventBus);
    }

    @Test
    void aStoppedGameOpensNothing() {
        final Model shown = thrive();
        final ColonyStatus colony = firstColony(shown);
        when(game.isGameModelReady()).thenReturn(false);

        assertEquals(ColonyAdvisorNavigator.Result.UNAVAILABLE,
                navigator.open(shown, colony));
        verifyZeroInteractions(eventBus);
    }

    @Test
    void anOwnedColonyOfTheRunningGameOpensItsColonyScreenOnce() {
        final Model shown = thrive();
        final ColonyStatus colony = firstColony(shown);
        run(shown);

        assertEquals(ColonyAdvisorNavigator.Result.OPENED,
                navigator.open(shown, colony));
        verify(eventBus, times(1)).post(any(ShowScreenEvent.class));
    }
}
