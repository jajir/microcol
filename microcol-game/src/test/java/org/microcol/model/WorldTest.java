package org.microcol.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.microcol.gui.MoveUnitController;
import org.microcol.gui.NextTurnController;
import org.microcol.gui.Point;
import org.microcol.model.GoToMode;
import org.microcol.model.Ship;
import org.microcol.model.World;

import com.google.common.collect.Lists;

public class WorldTest {

	private World world;

	private NextTurnController nextTurnController;

	private MoveUnitController moveUnitController;

	@Test
	public void test_perform_2_steps_move_available_5_point() throws Exception {
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
	public void test_perform_1_steps_move_available_5_point() throws Exception {
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

	@Test
	public void test_perform_2_steps_move_available_1_point() throws Exception {
		Ship ship = new Ship(1);
		ship.decreaseActionPoint(4);
		assertEquals(1, ship.getAvailableSteps());
		ship.setGoToMode(new GoToMode(Lists.newArrayList(Point.make(1, 1), Point.make(2, 2), Point.make(3, 3))));
		moveUnitController.fireMoveUnitEvent(Lists.newArrayList(Point.make(1, 1), Point.make(2, 2)));
		EasyMock.replay(nextTurnController, moveUnitController);

		world.performMove(ship);
		EasyMock.verify(nextTurnController, moveUnitController);
		assertEquals(0, ship.getAvailableSteps());
		assertEquals(2, ship.getGoToMode().getPath().size());
	}

	@Test
	public void test_perform_2_steps_move_available_0_point() throws Exception {
		Ship ship = new Ship(1);
		ship.decreaseActionPoint(5);
		assertEquals(0, ship.getAvailableSteps());
		ship.setGoToMode(new GoToMode(Lists.newArrayList(Point.make(1, 1), Point.make(2, 2), Point.make(3, 3))));
		EasyMock.replay(nextTurnController, moveUnitController);

		world.performMove(ship);
		EasyMock.verify(nextTurnController, moveUnitController);
		assertEquals(0, ship.getAvailableSteps());
		assertEquals(3, ship.getGoToMode().getPath().size());
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