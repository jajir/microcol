package org.microcol.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.microcol.model.GoToMode;
import org.microcol.model.Ship;
import org.microcol.model.World;

import com.google.common.collect.Lists;

public class WorldTest {

	private World world;

	private NextTurnController nextTurnController;

	private MoveUnitController moveUnitController;

	@Test
	public void test_perform_2_steps_move() throws Exception {
		Ship ship = new Ship(1);
		assertEquals(5, ship.getAvailableSteps());
		ship.setGoToMode(new GoToMode(Lists.newArrayList(Point.make(1, 1), Point.make(2, 2), Point.make(3, 3))));
		moveUnitController.fireMoveUnitEvent(Lists.newArrayList(Point.make(1, 1), Point.make(2, 2), Point.make(3, 3)));
		EasyMock.replay(nextTurnController, moveUnitController);

		world.performMove(ship);
		EasyMock.verify(nextTurnController, moveUnitController);
		assertEquals(3, ship.getAvailableSteps());
		assertTrue(ship.getGoToMode().getPath().isEmpty());
	}

	@Test
	public void test_perform_1_steps_move() throws Exception {
		Ship ship = new Ship(1);
		assertEquals(5, ship.getAvailableSteps());
		ship.setGoToMode(new GoToMode(Lists.newArrayList(Point.make(1, 1), Point.make(2, 2))));
		moveUnitController.fireMoveUnitEvent(Lists.newArrayList(Point.make(1, 1), Point.make(2, 2)));
		EasyMock.replay(nextTurnController, moveUnitController);

		world.performMove(ship);
		EasyMock.verify(nextTurnController, moveUnitController);
		assertEquals(4, ship.getAvailableSteps());
		assertTrue(ship.getGoToMode().getPath().isEmpty());
	}

	@Before
	public void setup() {
		nextTurnController = EasyMock.createMock(NextTurnController.class);
		moveUnitController = EasyMock.createMock(MoveUnitController.class);
		world = new World(nextTurnController, moveUnitController);
	}

	@After
	public void tearDown() {

	}

}
