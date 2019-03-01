package org.microcol.gui;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.microcol.gui.event.Listener;
import org.microcol.gui.util.OrderedEventController;

public class OrderedEventControllerTest {

    @Test
    public void test_addListener_nullValue() throws Exception {
        OrderedEventController<String> controller = new OrderedEventController<>();

        assertThrows(NullPointerException.class, () -> {
            controller.addListener(null);
        });

    }

    @Test
    public void test_addListener_withPriority_nullValue() throws Exception {
        OrderedEventController<String> controller = new OrderedEventController<>();

        assertThrows(NullPointerException.class, () -> {
            controller.addListener(null, 11);
        });
    }

    @Test
    public void test_fireEvent_verifyThatEventIsRequired_1() throws Exception {
        OrderedEventController<String> controller = new OrderedEventController<>();

        assertThrows(NullPointerException.class, () -> {
            controller.fireEvent(null);
        });
    }

    @Test
    public void test_fireEvent_verifyCorrectOrdering() throws Exception {
        OrderedEventController<List<String>> controller = new OrderedEventController<>();

        MockListener l0 = new MockListener("l0");
        MockListener l1 = new MockListener("l1");
        MockListener l2 = new MockListener("l2");

        controller.addListener(l0, 12);
        controller.addListener(l1, 14);
        controller.addListener(l2, 123);

        List<String> list = new ArrayList<>();
        controller.fireEvent(list);

        assertEquals(3, list.size(), "list size is not correct");
        assertEquals("l0", list.get(0), "item 0 is not correct");
        assertEquals("l1", list.get(1), "item 1 is not correct");
        assertEquals("l2", list.get(2), "item 2 is not correct");
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

}
