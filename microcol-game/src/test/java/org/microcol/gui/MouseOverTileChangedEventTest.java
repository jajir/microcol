package org.microcol.gui;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.microcol.gui.screen.game.gamepanel.MouseOverTileChangedEvent;
import org.microcol.model.Location;

public class MouseOverTileChangedEventTest {

    @Test
    public void test_location_is_required() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            new MouseOverTileChangedEvent(null);
        });
    }

    @Test
    public void test_equals_verify_reflexivity() throws Exception {
        Location loc1 = Location.of(2, 4);
        MouseOverTileChangedEvent motce1 = new MouseOverTileChangedEvent(loc1);

        assertTrue(motce1.equals(motce1));
    }

    @Test
    public void test_equals_verify_two_same_instances() throws Exception {
        Location loc1 = Location.of(2, 4);
        Location loc2 = Location.of(2, 4);
        MouseOverTileChangedEvent motce1 = new MouseOverTileChangedEvent(loc1);
        MouseOverTileChangedEvent motce2 = new MouseOverTileChangedEvent(loc2);

        assertTrue(motce1.equals(motce2));
    }

}
