package org.microcol.gui.screen.turnreport;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.microcol.gui.screen.Screen;

public class EuropeOpeningRequestTest {

    private final EuropeOpeningRequest request = new EuropeOpeningRequest();

    @Test
    public void verify_closing_without_request_returns_to_the_game() {
        assertEquals(Screen.GAME, request.takeScreenAfterReport());
    }

    @Test
    public void verify_request_is_used_exactly_once() {
        request.request();

        assertEquals(Screen.EUROPE, request.takeScreenAfterReport());
        assertEquals(Screen.GAME, request.takeScreenAfterReport());
    }

    @Test
    public void verify_leaving_the_report_cancels_a_pending_request() {
        request.request();
        request.clear();

        assertEquals(Screen.GAME, request.takeScreenAfterReport());
    }

}
