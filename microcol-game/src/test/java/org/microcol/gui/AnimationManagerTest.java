package org.microcol.gui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.microcol.gui.panelview.AnimationManager;
import org.microcol.gui.panelview.AnimationPart;

public class AnimationManagerTest {

	private AnimationPart part1;

	private AnimationPart part2;

	@Test
	public void test_constructor() throws Exception {
		AnimationManager am = new AnimationManager();
		assertFalse(am.hasNextStep());
	}

	@Test
	public void test_simple_scenario_verify_that_paint_is_not_called() throws Exception {
		AnimationManager am = new AnimationManager();
		EasyMock.expect(part1.hasNextStep()).andReturn(false);
		EasyMock.replay(part1);
		am.addAnimationPart(part1);

		assertFalse(am.hasNextStep());
		EasyMock.verify(part1);
	}

	@Test
	public void test_simple_scenario() throws Exception {
		AnimationManager am = new AnimationManager();
		EasyMock.expect(part1.hasNextStep()).andReturn(true);
		EasyMock.replay(part1);
		am.addAnimationPart(part1);
		assertTrue(am.hasNextStep());

		EasyMock.verify(part1);
	}

	@Test
	public void test_last_animationPart() throws Exception {
		AnimationManager am = new AnimationManager();
		EasyMock.expect(part1.hasNextStep()).andReturn(false);
		EasyMock.replay(part1);
		am.addAnimationPart(part1);
		assertFalse(am.hasNextStep());

		EasyMock.verify(part1);
	}

	@Test(expected=IllegalArgumentException.class)
	public void test_invalid_next_step_call() throws Exception {
		AnimationManager am = new AnimationManager();
		EasyMock.replay(part1);
		assertFalse(am.hasNextStep());
		
		am.performStep();
	}

	@Test(expected=IllegalArgumentException.class)
	public void test_invalid_paint_call() throws Exception {
		AnimationManager am = new AnimationManager();
		EasyMock.replay(part1);
		assertFalse(am.hasNextStep());
		
		am.performStep();
	}

	@Test
	public void test_two_parts_together() throws Exception {
		AnimationManager am = new AnimationManager();
		EasyMock.expect(part1.hasNextStep()).andReturn(true);
		part1.nextStep();
		EasyMock.expect(part1.hasNextStep()).andReturn(false);
		
		EasyMock.expect(part2.hasNextStep()).andReturn(true);
		part2.nextStep();
		EasyMock.expect(part2.hasNextStep()).andReturn(false);
		
		EasyMock.replay(part1, part2);
		am.addAnimationPart(part1);
		am.addAnimationPart(part2);
		
		assertTrue(am.hasNextStep());
		am.performStep();
		
		assertTrue(am.hasNextStep());
		am.performStep();
		assertFalse(am.hasNextStep());
		
		EasyMock.verify(part1, part2);
	}

	@Test
	public void test_two_parts_one_by_one() throws Exception {
		AnimationManager am = new AnimationManager();
		
		EasyMock.expect(part1.hasNextStep()).andReturn(true);
		part1.nextStep();
		EasyMock.expect(part1.hasNextStep()).andReturn(false);
		
		EasyMock.expect(part2.hasNextStep()).andReturn(true);
		part2.nextStep();
		EasyMock.expect(part2.hasNextStep()).andReturn(false);
		
		EasyMock.replay(part1, part2);
		
		am.addAnimationPart(part1);
		assertTrue(am.hasNextStep());
		am.performStep();
		assertFalse(am.hasNextStep());
		
		
		am.addAnimationPart(part2);
		assertTrue(am.hasNextStep());
		am.performStep();
		assertFalse(am.hasNextStep());
		
		EasyMock.verify(part1, part2);
	}

	@Test(expected = NullPointerException.class)
	public void test_addAnimationPart_verify_than_null_is_not_allowed() throws Exception {
		AnimationManager am = new AnimationManager();
		am.addAnimationPart(null);
	}

	@Before
	public void setup() {
		part1 = EasyMock.createMock(AnimationPart.class);
		part2 = EasyMock.createMock(AnimationPart.class);
	}

	@After
	public void rearDown() {
		part1 = null;
		part2 = null;
	}

}
