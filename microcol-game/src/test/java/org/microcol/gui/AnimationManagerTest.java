package org.microcol.gui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.microcol.gui.gamepanel.Animation;
import org.microcol.gui.gamepanel.AnimationIsDoneController;
import org.microcol.gui.gamepanel.AnimationManager;
import org.microcol.gui.gamepanel.AnimationStartedController;
import org.microcol.gui.gamepanel.Area;

import mockit.Injectable;
import mockit.Mocked;
import mockit.StrictExpectations;
import mockit.Tested;
import mockit.integration.junit4.JMockit;

@RunWith(JMockit.class)
public class AnimationManagerTest {

    @Tested(availableDuringSetup = true)
    private AnimationManager am;

    @Injectable
    private AnimationIsDoneController animationIsDoneController;

    @Injectable
    private AnimationStartedController animationStartedController;

    @Mocked
    private Animation part1;

    @Mocked
    private Animation part2;

    @Mocked
    private Area area;

    @Test
    public void test_constructor() throws Exception {
        assertNotNull(am);
        assertFalse(am.hasNextStep(area));
    }

    @Test(expected = IllegalStateException.class)
    public void test_addiong_one_animation_without_nextStep() throws Exception {
        am.addAnimation(part1);

        assertFalse(am.hasNextStep(area));
    }

    @Test
    public void validate_animation_with_1_step_which_canBePainted() throws Exception {
        new StrictExpectations() {{
                part1.hasNextStep();result=true;times=2;
                part1.canBePainted(area);result=false;times=1;
                part1.nextStep();
                part1.hasNextStep();result=false;times=1;
            }};
        am.addAnimation(part1);

        assertFalse(am.hasNextStep(area));
    }

    @Test()
    public void test_invalid_perform_step_call() throws Exception {
        assertFalse(am.hasNextStep(area));
    }

    @Test(expected=IllegalStateException.class)
    public void verify_that_only_one_animation_is_processed() throws Exception {
        am.addAnimation(part1);
        am.addAnimation(part2);
    }

    @Test(expected = NullPointerException.class)
    public void test_addAnimationPart_verify_than_null_is_not_allowed() throws Exception {
        AnimationManager am = new AnimationManager(animationStartedController,
                animationIsDoneController);
        am.addAnimation(null);
    }

}
