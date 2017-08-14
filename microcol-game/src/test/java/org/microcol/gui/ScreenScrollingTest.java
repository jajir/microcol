package org.microcol.gui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.microcol.gui.PathPlanning.WhatToDoWithPointInPath;
import org.microcol.gui.panelview.ScreenScrolling;

import mockit.Expectations;
import mockit.Mocked;

public class ScreenScrollingTest {

	private @Mocked PathPlanning pathPlanning;

	@Test(expected = NullPointerException.class)
	public void test_constructor_pathPlanningIsNull() throws Exception {
		new ScreenScrolling(null, Point.of(10, 10), Point.of(30, 10));
	}

	@Test
	public void test_constructor() throws Exception {
		new Expectations() {{
			pathPlanning.paintPathWithStepsLimit(Point.of(10, 10), Point.of(30, 10),
					(WhatToDoWithPointInPath)any, 10);
		}};
		ScreenScrolling screenScrolling = new ScreenScrolling(pathPlanning, Point.of(10, 10), Point.of(30, 10));

		assertNotNull(screenScrolling);
		assertFalse(screenScrolling.isNextPointAvailable());
	}

}
