package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.CalendarPo;

public class CalendarStoreTest {

    @ParameterizedTest
    @ValueSource(strings = { "/maps/free-play.microcol",
            "/maps/free-play-two-islands.microcol",
            "/maps/free-play-rival-expedition.microcol",
            "/maps/default-campaign-mission-0-findNewWorld.microcol",
            "/maps/default-campaign-mission-1-buildArmy.microcol",
            "/maps/default-campaign-mission-2-thrive.microcol" })
    void bundledGameStartsIn1490(final String resource) {
        final Model model = Model.make(
                new GameModelPoDao().loadFromClassPath(resource).getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        model.startGame();
        assertEquals(1490, model.getCalendar().getStartYear());
        assertEquals(1490, model.getCalendar().getCurrentYear());
        assertEquals(0, model.getCalendar().getNumberOfPlayedTurns());
        assertTrue(model.getCalendar().getCurrentSeason().isEmpty());
    }

    @ParameterizedTest(name = "save start {0}, round {1}: {2} {3}")
    @CsvSource({ "1490, 109, 1599,", "1490, 110, 1600, spring",
            "1490, 111, 1600, summer", "1490, 112, 1600, autumn",
            "1490, 113, 1600, winter", "1490, 114, 1601, spring",
            "1630, 1, 1630, summer", "1630, 3, 1630, winter",
            "1630, 4, 1631, spring" })
    void roundTripPreservesTheExactDate(final int startYear, final int round,
            final int expectedYear, final Calendar.Season expectedSeason) {
        final CalendarPo saved = new Calendar(startYear, 1850, round).save();
        final Calendar restored = Calendar.make(saved);
        assertEquals(startYear, restored.getStartYear());
        assertEquals(1850, restored.getEndYear());
        assertEquals(round, restored.getNumberOfPlayedTurns());
        assertEquals(expectedYear, restored.getCurrentYear());
        assertEquals(expectedSeason, restored.getCurrentSeason().orElse(null));
    }

    @Test
    public void test_save() throws Exception {
        final Calendar cal = new Calendar(100, 999, 0);
        CalendarPo data = cal.save();

        assertEquals(100, data.getStartYear());
        assertEquals(999, data.getEndYear());
    }

    @Test
    public void test_make() throws Exception {
        final Calendar cal = new Calendar(100, 999, 0);
        Calendar tst = Calendar.make(cal.save());

        assertEquals(100, tst.getStartYear());
        assertEquals(999, tst.getEndYear());
        assertEquals(100, tst.getCurrentYear());
    }

    @Test
    public void testCreation() throws Exception {
        final Calendar cal = new Calendar(1630, 1800, 0);

        assertEquals(1630, cal.getStartYear());
        assertEquals(1800, cal.getEndYear());
        assertEquals(1630, cal.getCurrentYear());
        assertTrue(cal.getCurrentSeason().isPresent());
        assertEquals(Calendar.Season.spring, cal.getCurrentSeason().get());
    }

}
