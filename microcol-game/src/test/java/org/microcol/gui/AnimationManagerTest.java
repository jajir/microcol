package org.microcol.gui;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.microcol.gui.screen.game.gamepanel.Animation;
import org.microcol.gui.screen.game.gamepanel.AnimationManager;
import org.microcol.gui.screen.game.gamepanel.Area;

import com.google.common.eventbus.EventBus;

public class AnimationManagerTest {

    private AnimationManager am;

    private final Animation part1 = mock(Animation.class);

    private final EventBus eventBus = mock(EventBus.class);

    private final Animation part2 = mock(Animation.class);

    private final Area area = mock(Area.class);

    @Test
    public void test_constructor() throws Exception {
        assertNotNull(am);
        assertFalse(am.hasNextStep(area));
    }

    @Test
    public void test_addiong_one_animation_without_nextStep() throws Exception {
        when(part1.hasNextStep()).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> {
            am.addAnimation(part1);
        });
    }

    @Test
    public void validate_animation_with_1_step_which_canBePainted() throws Exception {
        when(part1.hasNextStep()).thenReturn(true, true, false);
        when(part1.canBePainted(area)).thenReturn(false);
        am.addAnimation(part1);

        assertFalse(am.hasNextStep(area));
    }

    @Test()
    public void test_invalid_perform_step_call() throws Exception {
        assertFalse(am.hasNextStep(area));
    }

    @Test
    public void verify_that_only_one_animation_is_processed() throws Exception {
        when(part1.hasNextStep()).thenReturn(true);
        am.addAnimation(part1);

        assertThrows(IllegalStateException.class, () -> {
            am.addAnimation(part2);
        });
    }

    @Test
    public void test_addAnimationPart_verify_than_null_is_not_allowed() throws Exception {
        AnimationManager am = new AnimationManager(eventBus);

        assertThrows(NullPointerException.class, () -> {
            am.addAnimation(null);
        });
    }

    @BeforeEach
    public void startUp() {
        am = new AnimationManager(eventBus);
    }

    @AfterEach
    public void tearDown() {
        am = null;
    }

}
