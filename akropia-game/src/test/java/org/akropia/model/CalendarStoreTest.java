package org.akropia.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.akropia.model.game.GameModel;
import org.akropia.model.game.po.GameModelPoDao;

public class CalendarStoreTest {

    @ParameterizedTest
    @ValueSource(strings = { "/maps/free-play.akropia",
            "/maps/free-play-two-islands.akropia",
            "/maps/free-play-rival-expedition.akropia",
            "/maps/fixture-arrival-world.akropia",
            "/maps/fixture-army-world.akropia",
            "/maps/fixture-developed-world.akropia" })
    void bundledGameStartsInTheFirstSummer(final String resource) {
        final Model model = Model.make(
                new GameModelPoDao().loadFromClassPath(resource).getModel(),
                GameModel.gameOverEvaluators());
        model.startGame();
        assertEquals(1, model.getCalendar().getStartYear());
        assertEquals(1, model.getCalendar().getCurrentYear());
        assertEquals(0, model.getCalendar().getNumberOfPlayedTurns());
        assertEquals(Calendar.Season.summer,
                model.getCalendar().getCurrentSeason().orElseThrow());
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 6, 7, 20, 21, 41, 42, 62, 63, 83, 84, 221 })
    void roundTripPreservesTheExactDate(int day) {
        Calendar calendar = new Calendar(1, 20, day);
        Calendar restored = Calendar.make(calendar.save());
        assertEquals(day, restored.getNumberOfPlayedTurns());
        assertEquals(calendar.getCurrentYear(), restored.getCurrentYear());
        assertEquals(calendar.getMonthName(), restored.getMonthName());
        assertEquals(calendar.getDayOfMonth(), restored.getDayOfMonth());
        assertEquals(calendar.getCurrentSeason(), restored.getCurrentSeason());
        assertEquals(calendar.save().getTurnLimit(),
                restored.save().getTurnLimit());
    }
}
