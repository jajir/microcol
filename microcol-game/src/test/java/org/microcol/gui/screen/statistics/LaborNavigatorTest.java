package org.microcol.gui.screen.statistics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.screen.game.gamepanel.SelectedTileManager;
import org.microcol.model.LaborMember;
import org.microcol.model.LaborReport;
import org.microcol.model.Model;
import org.microcol.model.Player;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPoDao;

import com.google.common.eventbus.EventBus;

/**
 * R-0198 navigation guard: a Show request made from rows of one game never
 * opens anything in another game, even when that game has a person with the
 * same unit ID, and a person that is still in the running game is followed.
 */
class LaborNavigatorTest {

    private static final String THRIVE = "/maps/default-campaign-mission-2-thrive.microcol";

    private static final String DUTCH = "Dutch";

    private final EventBus eventBus = mock(EventBus.class);

    private final GameModelController game = mock(GameModelController.class);

    private final LaborNavigator navigator = new LaborNavigator(game, eventBus,
            new SelectedTileManager(eventBus));

    private static Model thrive() {
        return Model.make(
                new GameModelPoDao().loadFromClassPath(THRIVE).getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private static LaborMember firstPerson(final Model model) {
        final LaborReport report = model
                .getLaborReport(model.getPlayerByName(DUTCH));
        assertTrue(report.getTotal() > 0, "The reference game has people");
        return report.getMembers(report.getTypes().get(0)).get(0);
    }

    private void run(final Model running) {
        final Player human = running.getPlayerByName(DUTCH);
        when(game.isGameModelReady()).thenReturn(true);
        when(game.getModel()).thenReturn(running);
        when(game.getHumanPlayer()).thenReturn(human);
    }

    @Test
    void rowsOfAReplacedGameOpenNothingEvenWithTheSameUnitId() {
        final Model shown = thrive();
        final LaborMember member = firstPerson(shown);
        final Model replacement = thrive();
        assertTrue(
                replacement.getLaborReport(replacement.getPlayerByName(DUTCH))
                        .findMember(member.getUnitId()).isPresent(),
                "The replacement game has a person with the same ID");
        run(replacement);

        assertEquals(LaborNavigator.Result.UNAVAILABLE,
                navigator.open(shown, member));
        verifyZeroInteractions(eventBus);
    }

    @Test
    void aStoppedGameOpensNothing() {
        final Model shown = thrive();
        final LaborMember member = firstPerson(shown);
        when(game.isGameModelReady()).thenReturn(false);

        assertEquals(LaborNavigator.Result.UNAVAILABLE,
                navigator.open(shown, member));
        verifyZeroInteractions(eventBus);
    }

    @Test
    void aPersonOfTheRunningGameIsFollowedToItsCurrentPlace() {
        final Model shown = thrive();
        final LaborMember member = firstPerson(shown);
        run(shown);

        assertNotEquals(LaborNavigator.Result.UNAVAILABLE,
                navigator.open(shown, member));
    }
}
