package org.microcol.gui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.microcol.gui.panelview.Animation;
import org.microcol.gui.panelview.AnimationManager;

import mockit.Expectations;
import mockit.Mocked;
import mockit.StrictExpectations;
import mockit.Tested;
import mockit.integration.junit4.JMockit;

@RunWith(JMockit.class)
public class AnimationManagerTest {
	
	@Tested(availableDuringSetup=true)
	private AnimationManager am;

	@Mocked
	private Animation part1;

	@Mocked
	private Animation part2;

	@Test
	public void test_constructor() throws Exception {
		assertNotNull(am);
		assertFalse(am.hasNextStep());
	}

	@Test
	public void test_addiong_one_animation_without_nextStep() throws Exception {
		new Expectations() {{
			part1.hasNextStep();result=false;times=1;
		}};
		am.addAnimation(part1);

		assertFalse(am.hasNextStep());
	}

	@Test
	public void test_addiong_one_animation_with_nextStep() throws Exception {
		new Expectations() {{
			part1.hasNextStep();result=true;times=1;
		}};
		am.addAnimation(part1);

		assertTrue(am.hasNextStep());
	}

	@Test
	public void test_one_animation_with_one_step() throws Exception {
		new StrictExpectations() {{
			part1.hasNextStep();result=true;times=1;
			part1.nextStep();times=1;
			part1.hasNextStep();result=false;times=1;
		}};
		am.addAnimation(part1);
		assertTrue(am.hasNextStep());
		am.performStep();
		assertFalse(am.hasNextStep());
	}

	@Test(expected=IllegalStateException.class)
	public void test_invalid_perform_step_call() throws Exception {
		assertFalse(am.hasNextStep());
		am.performStep();
	}

	@Test
	public void test_two_parts_together() throws Exception {
		new StrictExpectations() {{
			part1.hasNextStep();result=true;times=1;
			part1.nextStep();times=1;
			part1.hasNextStep();result=false;times=1;
			
			part2.hasNextStep();result=true;times=1;
			part2.nextStep();times=1;
			part2.hasNextStep();result=false;times=1;
		}};

		am.addAnimation(part1);
		am.addAnimation(part2);
		
		assertTrue(am.hasNextStep());
		am.performStep();
		
		assertTrue(am.hasNextStep());
		am.performStep();
		
		assertFalse(am.hasNextStep());
	}

	@Test
	public void test_two_parts_one_by_one() throws Exception {
		new StrictExpectations() {{
			part1.hasNextStep();result=true;times=1;
			part1.nextStep();times=1;
			part1.hasNextStep();result=false;times=1;
			
			part2.hasNextStep();result=true;times=1;
			part2.nextStep();times=1;
			part2.hasNextStep();result=false;times=1;
		}};		
		am.addAnimation(part1);
		assertTrue(am.hasNextStep());
		am.performStep();
		assertFalse(am.hasNextStep());
		
		am.addAnimation(part2);
		assertTrue(am.hasNextStep());
		am.performStep();
		assertFalse(am.hasNextStep());
	}

	@Test(expected = NullPointerException.class)
	public void test_addAnimationPart_verify_than_null_is_not_allowed() throws Exception {
		AnimationManager am = new AnimationManager();
		am.addAnimation(null);
	}

}
