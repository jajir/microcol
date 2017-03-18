package org.microcol.model;

import org.junit.Test;

public class ListenerManagerTest {
	@Test(expected = NullPointerException.class)
	public void testAddListenerNull() {
		final ListenerManager listenerManager = new ListenerManager();

		listenerManager.addListener(null);
	}

	@Test(expected = NullPointerException.class)
	public void testRemoveListenerNull() {
		final ListenerManager listenerManager = new ListenerManager();

		listenerManager.removeListener(null);
	}
}
