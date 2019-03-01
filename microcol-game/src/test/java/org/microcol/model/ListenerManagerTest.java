package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class ListenerManagerTest {
    
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
    
}
