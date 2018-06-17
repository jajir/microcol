package org.microcol.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.microcol.model.event.UnitMoveStartedEvent;

import mockit.Mocked;

/**
 * Verify that stoppable events are correctly processed.
 */
public class ListenerManagerStoppableTest {

    private @Mocked Unit unit;

    private @Mocked Path path;

    private @Mocked Model model;

    @Test
    public void verify_1_event_notStopping() throws Exception {
        final ListenerManager listenerManager = new ListenerManager();
        final SimpleListener s1 = new SimpleListener(false);
        listenerManager.addListener(s1);

        assertTrue(listenerManager.fireUnitMoveStarted(model, unit, path));
        assertTrue(s1.wasCalled());
    }

    @Test
    public void verify_1_event_stopping() throws Exception {
        final ListenerManager listenerManager = new ListenerManager();
        final SimpleListener s1 = new SimpleListener(true);
        listenerManager.addListener(s1);

        assertFalse(listenerManager.fireUnitMoveStarted(model, unit, path));
        assertTrue(s1.wasCalled());
    }

    @Test
    public void verify_3_event_notStopping() throws Exception {
        final ListenerManager listenerManager = new ListenerManager();
        final SimpleListener s1 = new SimpleListener(false);
        final SimpleListener s2 = new SimpleListener(false);
        final SimpleListener s3 = new SimpleListener(false);
        listenerManager.addListener(s1);
        listenerManager.addListener(s2);
        listenerManager.addListener(s3);

        assertTrue(listenerManager.fireUnitMoveStarted(model, unit, path));
        assertTrue(s1.wasCalled());
        assertTrue(s2.wasCalled());
        assertTrue(s3.wasCalled());
    }

    @Test
    public void verify_3_event_stopping() throws Exception {
        final ListenerManager listenerManager = new ListenerManager();
        final SimpleListener s1 = new SimpleListener(false);
        final SimpleListener s2 = new SimpleListener(false);
        final SimpleListener s3 = new SimpleListener(true);
        listenerManager.addListener(s1);
        listenerManager.addListener(s2);
        listenerManager.addListener(s3);

        assertFalse(listenerManager.fireUnitMoveStarted(model, unit, path));
        assertTrue(s1.wasCalled());
        assertTrue(s2.wasCalled());
        assertTrue(s3.wasCalled());
    }

    @Test
    public void verify_3_event_stopping_different_order() throws Exception {
        final ListenerManager listenerManager = new ListenerManager();
        final SimpleListener s1 = new SimpleListener(true);
        final SimpleListener s2 = new SimpleListener(false);
        final SimpleListener s3 = new SimpleListener(false);
        listenerManager.addListener(s1);
        listenerManager.addListener(s2);
        listenerManager.addListener(s3);

        assertFalse(listenerManager.fireUnitMoveStarted(model, unit, path));
        assertTrue(s1.wasCalled());
        assertFalse(s2.wasCalled());
        assertFalse(s3.wasCalled());
    }

    class SimpleListener extends ModelListenerAdapter {

        private final boolean stopEvent;

        private boolean called = false;

        SimpleListener(final boolean stopEvent) {
            this.stopEvent = stopEvent;
        }

        @Override
        public void onUnitMoveStarted(UnitMoveStartedEvent event) {
            called = true;
            if (stopEvent) {
                event.stopEventExecution();
            }

        }

        /**
         * @return the called
         */
        public boolean wasCalled() {
            return called;
        }

    }

}
