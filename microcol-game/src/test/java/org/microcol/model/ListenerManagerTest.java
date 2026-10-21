package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.microcol.model.event.ActionStartedEvent;

public class ListenerManagerTest {

    private final Model model = mock(Model.class);

    @Test
    public void testAddListenerNull() {
        final ListenerManager listenerManager = new ListenerManager();

        assertThrows(NullPointerException.class, () -> {
            listenerManager.addListener(null);
        });
    }

    @Test
    public void testRemoveListenerNull() {
        final ListenerManager listenerManager = new ListenerManager();

        assertThrows(NullPointerException.class, () -> {
            listenerManager.removeListener(null);
        });
    }

    @Test
    public void listenerChangingRegistrationsDuringDispatchAffectsOnlyLaterEvents() {
        final ListenerManager listenerManager = new ListenerManager();
        final Counting added = new Counting(listenerManager, manager -> {
        });
        final Counting later = new Counting(listenerManager, manager -> {
        });
        final Counting[] self = new Counting[1];
        self[0] = new Counting(listenerManager, manager -> {
            manager.removeListener(self[0]);
            manager.addListener(added);
            // Already registered: still one delivery per event.
            manager.addListener(later);
        });
        listenerManager.addListener(self[0]);
        listenerManager.addListener(later);

        listenerManager.fireActionStarted(model);

        assertEquals(1, self[0].calls);
        assertEquals(1, later.calls);
        assertEquals(0, added.calls);

        listenerManager.fireActionStarted(model);

        assertEquals(1, self[0].calls);
        assertEquals(2, later.calls);
        assertEquals(1, added.calls);
    }

    @Test
    public void removingAllListenersDuringDispatchStopsOnlyLaterEvents() {
        final ListenerManager listenerManager = new ListenerManager();
        final Counting first = new Counting(listenerManager,
                ListenerManager::removeAllListeners);
        final Counting second = new Counting(listenerManager, manager -> {
        });
        listenerManager.addListener(first);
        listenerManager.addListener(second);

        listenerManager.fireActionStarted(model);
        listenerManager.fireActionStarted(model);

        assertEquals(1, first.calls);
        assertEquals(1, second.calls);
    }

    @Test
    public void registrationFromAnotherThreadDuringDispatchIsSafe()
            throws Exception {
        final ListenerManager listenerManager = new ListenerManager();
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        final Counting blocking = new Counting(listenerManager, manager -> {
            entered.countDown();
            try {
                assertTrue(release.await(10, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
        });
        final Counting removed = new Counting(listenerManager, manager -> {
        });
        final Counting added = new Counting(listenerManager, manager -> {
        });
        listenerManager.addListener(blocking);
        listenerManager.addListener(removed);
        final AtomicReference<Throwable> failure = new AtomicReference<>();
        final Thread dispatch = new Thread(() -> {
            try {
                listenerManager.fireActionStarted(model);
            } catch (Throwable e) {
                failure.set(e);
            }
        });
        dispatch.start();
        assertTrue(entered.await(10, TimeUnit.SECONDS));

        // The dispatch is inside the first callback while the list changes.
        listenerManager.addListener(added);
        listenerManager.removeListener(removed);
        release.countDown();
        dispatch.join(TimeUnit.SECONDS.toMillis(10));

        assertFalse(dispatch.isAlive(), "The dispatch must finish");
        assertNull(failure.get());
        assertEquals(1, blocking.calls);
        assertEquals(1, removed.calls);
        assertEquals(0, added.calls);

        blocking.onEvent = manager -> {
        };
        listenerManager.fireActionStarted(model);

        assertEquals(2, blocking.calls);
        assertEquals(1, removed.calls);
        assertEquals(1, added.calls);
    }

    private static final class Counting extends ModelListenerAdapter {

        private final ListenerManager manager;

        private volatile Consumer<ListenerManager> onEvent;

        private volatile int calls;

        Counting(final ListenerManager manager,
                final Consumer<ListenerManager> onEvent) {
            this.manager = manager;
            this.onEvent = onEvent;
        }

        @Override
        public void onActionStarted(final ActionStartedEvent event) {
            calls++;
            onEvent.accept(manager);
        }
    }
}
