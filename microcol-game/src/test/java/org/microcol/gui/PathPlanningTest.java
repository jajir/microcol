package org.microcol.gui;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.microcol.model.Location;

public class PathPlanningTest {

	private PathPlanning pathPlanning;

	@Test
	public void test_simple() throws Exception {
		PathPlanning.WhatToDoWithPointInPath whatToDo = EasyMock.createMock(PathPlanning.WhatToDoWithPointInPath.class);
		whatToDo.pathPoint(Location.make(2, 4));
		whatToDo.pathPoint(Location.make(3, 4));
		whatToDo.pathPoint(Location.make(4, 5));
		whatToDo.pathPoint(Location.make(5, 5));
		whatToDo.pathPoint(Location.make(6, 6));
		whatToDo.pathPoint(Location.make(7, 6));
		EasyMock.replay(whatToDo);
		pathPlanning.paintPath(Location.make(2, 4), Location.make(7, 6), whatToDo);

		EasyMock.verify(whatToDo);
	}

	@Test
	public void test_diagonal_move() throws Exception {
		PathPlanning.WhatToDoWithPointInPath whatToDo = EasyMock.createMock(PathPlanning.WhatToDoWithPointInPath.class);
		whatToDo.pathPoint(Location.make(2, 2));
		whatToDo.pathPoint(Location.make(3, 3));
		whatToDo.pathPoint(Location.make(4, 4));
		whatToDo.pathPoint(Location.make(5, 5));
		whatToDo.pathPoint(Location.make(6, 6));
		EasyMock.replay(whatToDo);
		pathPlanning.paintPath(Location.make(2, 2), Location.make(6, 6), whatToDo);

		EasyMock.verify(whatToDo);
	}

	@Test
	public void test_from_and_destiny_points_are_same() throws Exception {
		PathPlanning.WhatToDoWithPointInPath whatToDo = EasyMock.createMock(PathPlanning.WhatToDoWithPointInPath.class);
		EasyMock.replay(whatToDo);
		pathPlanning.paintPath(Location.make(5, 5), Location.make(5, 5), whatToDo);
		EasyMock.verify(whatToDo);
	}

	@Test
	public void test_first_step_is_not_skipped() throws Exception {
		PathPlanning.WhatToDoWithPointInPath whatToDo = EasyMock.createMock(PathPlanning.WhatToDoWithPointInPath.class);
		whatToDo.pathPoint(Location.make(5, 5));
		whatToDo.pathPoint(Location.make(6, 4));
		whatToDo.pathPoint(Location.make(7, 3));
		whatToDo.pathPoint(Location.make(8, 2));
		whatToDo.pathPoint(Location.make(9, 2));
		whatToDo.pathPoint(Location.make(10, 1));
		whatToDo.pathPoint(Location.make(11, 0));
		EasyMock.replay(whatToDo);
		pathPlanning.paintPath(Location.make(5, 5), Location.make(11, 0), whatToDo);

		EasyMock.verify(whatToDo);
	}

	@Test
	public void test_move_to_lover_values() throws Exception {
		PathPlanning.WhatToDoWithPointInPath whatToDo = EasyMock.createMock(PathPlanning.WhatToDoWithPointInPath.class);
		whatToDo.pathPoint(Location.make(6, 7));
		whatToDo.pathPoint(Location.make(6, 6));
		whatToDo.pathPoint(Location.make(5, 5));
		whatToDo.pathPoint(Location.make(5, 4));
		whatToDo.pathPoint(Location.make(4, 3));
		EasyMock.replay(whatToDo);
		pathPlanning.paintPath(Location.make(6, 7), Location.make(4, 3), whatToDo);

		EasyMock.verify(whatToDo);
	}

	@Before
	public void setUp() {
		pathPlanning = new PathPlanning();
	}

	@After
	public void tearDown() {
		pathPlanning = null;
	}

}
