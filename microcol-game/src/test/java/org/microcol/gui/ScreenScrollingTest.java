package org.microcol.gui;

import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.microcol.gui.screen.game.gamepanel.ScreenScrolling;
import org.microcol.gui.util.PathPlanning;

import com.google.common.collect.Lists;

public class ScreenScrollingTest {

    private final PathPlanning pathPlanning = mock(PathPlanning.class);

    private final List<Point> stepsToDo = Lists.newArrayList(Point.of(11, 11), Point.of(12, 12));

    @Test
    public void test_constructor_pathPlanningIsNull() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            new ScreenScrolling(null, Point.of(10, 10), Point.of(30, 10));
        });
    }

    @Test
    public void test_constructor() throws Exception {
        when(pathPlanning.getPathLimitSteps(Point.of(10, 10), Point.of(15, 15), 10))
                .thenReturn(stepsToDo);

        final ScreenScrolling ret = new ScreenScrolling(pathPlanning, Point.of(10, 10),
                Point.of(15, 15));

        // Verify sequence of processing points.
        assertTrue(ret.isNextPointAvailable());
        assertEquals(Point.of(11, 11), ret.getNextPoint());
        assertTrue(ret.isNextPointAvailable());
        assertEquals(Point.of(12, 12), ret.getNextPoint());
        assertFalse(ret.isNextPointAvailable());
    }

}
