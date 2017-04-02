package org.microcol.gui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.microcol.gui.panelview.ScreenScrolling;
import org.microcol.model.Location;

public class ScreenScrollingTest {

	private PathPlanning pathPlanning;

	@Test(expected = NullPointerException.class)
	public void test_constructor_pathPlanningIsNull() throws Exception {
		new ScreenScrolling(null, Point.of(10, 10), Point.of(30, 10));
	}

	// TODO JJ how to mock lambda?
	@Test
	public void test_constructor() throws Exception {
		pathPlanning.paintPath(EasyMock.eq(Location.of(10, 10)), EasyMock.eq(Location.of(30, 10)), EasyMock.notNull(),
				EasyMock.eq(10));
		EasyMock.replay(pathPlanning);
		ScreenScrolling screenScrolling = new ScreenScrolling(pathPlanning, Point.of(10, 10), Point.of(30, 10));

		EasyMock.verify(pathPlanning);
		assertNotNull(screenScrolling);
		assertFalse(screenScrolling.isNextPointAvailable());
	}

	@Before
	public void setup() {
		pathPlanning = EasyMock.createMock(PathPlanning.class);
	}

	@After
	public void tearDown() {
		pathPlanning = null;
	}

}
