package org.microcol.integration;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.microcol.gui.event.Listener;
import org.microcol.gui.util.AbstractEventController;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

public class EventControllerTest {

	@Test(expected = NullPointerException.class)
	public void test_addListener_nullValue() throws Exception {
		MockEventController controller = new MockEventController();

		controller.addListener(null);
	}

	@Test(expected = NullPointerException.class)
	public void test_fireEvent_verifyThatEventIsRequired_1() throws Exception {
		MockEventController controller = new MockEventController();

		controller.fireEvent(null);
	}

	@Test(timeout = 1000)
	public void test_adding_same_listener_in_more_than_one_shoul_count_just_one() throws Exception {
		MockEventController controller = new MockEventController();

		MockListener l0 = new MockListener("l0");

		controller.addListener(l0);
		controller.addListener(l0);

		List<String> set = new ArrayList<>();
		controller.fireEvent(set);

		while (set.size() == 0) {
			Thread.sleep(100);
		}
		assertEquals("list size is not correct", 1, set.size());
		assertEquals("set should contains l0", true, set.contains("l0"));
	}

	@Test(timeout = 1000)
	public void test_fireEvent_verify_that_all_listeners_are_called() throws Exception {
		MockEventController controller = new MockEventController();

		MockListener l0 = new MockListener("l0");
		MockListener l1 = new MockListener("l1");
		MockListener l2 = new MockListener("l2");

		controller.addListener(l0);
		controller.addListener(l1);
		controller.addListener(l2);

		List<String> set = new ArrayList<>();
		controller.fireEvent(set);

		while (set.size() != 3) {
			Thread.sleep(100);
		}
		assertEquals("list size is not correct", 3, set.size());
		assertEquals("set should contains l0", true, set.contains("l0"));
		assertEquals("set should contains l1", true, set.contains("l1"));
		assertEquals("set should contains l2", true, set.contains("l2"));
	}

	@Before
	public void setUp() {
		new JFXPanel();
		Platform.setImplicitExit(false);
	}

	private class MockListener implements Listener<List<String>> {

		private final String prefix;

		MockListener(final String prefix) {
			this.prefix = prefix;
		}

		@Override
		public void onEvent(final List<String> event) {
			event.add(prefix);
		}
	}

	private class MockEventController extends AbstractEventController<List<String>> {
	}

}
