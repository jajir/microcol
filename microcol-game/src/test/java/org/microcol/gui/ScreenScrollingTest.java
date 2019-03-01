package org.microcol.gui;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.microcol.gui.PathPlanning.WhatToDoWithPointInPath;
import org.microcol.gui.screen.game.gamepanel.ScreenScrolling;

public class ScreenScrollingTest {

    private final PathPlanning pathPlanning = mock(PathPlanning.class);

    @Test
    public void test_constructor_pathPlanningIsNull() throws Exception {
        assertThrows(NullPointerException.class, ()->{
            new ScreenScrolling(null, Point.of(10, 10), Point.of(30, 10));
        });
    }

    // TODO is it possible to write test as positive?
    // FIXME test is not correct. ScreenScrolling can't be instantiated
    // TODO probably remove WhatToDoWithPointInPath
    @Test
    public void test_constructor() throws Exception {
        doNothing().when(pathPlanning).paintPathWithStepsLimit(eq(Point.of(10, 10)),
                eq(Point.of(30, 10)), (WhatToDoWithPointInPath) any(), eq(10));

        assertThrows(IllegalArgumentException.class, ()->{
        new ScreenScrolling(pathPlanning, Point.of(10, 10),
                Point.of(30, 10));
        });
    }

}
