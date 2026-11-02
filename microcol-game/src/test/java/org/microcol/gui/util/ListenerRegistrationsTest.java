package org.microcol.gui.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.beans.property.SimpleDoubleProperty;

/**
 * A restarted application on the same primary stage must not leave its stage
 * listeners behind; they would keep every earlier application reachable.
 */
class ListenerRegistrationsTest {

    @Test
    void removeAllDetachesEveryListenerFromTheLongerLivedObservable() {
        final SimpleDoubleProperty width = new SimpleDoubleProperty(100);
        final SimpleDoubleProperty height = new SimpleDoubleProperty(50);
        final List<String> seen = new ArrayList<>();
        final ListenerRegistrations registrations = new ListenerRegistrations();
        registrations.add(width, (object, oldValue, newValue) -> seen
                .add("width " + newValue.intValue()));
        registrations.add(height, (object, oldValue, newValue) -> seen
                .add("height " + newValue.intValue()));

        width.set(120);
        height.set(60);
        assertEquals(List.of("width 120", "height 60"), seen);

        registrations.removeAll();
        width.set(140);
        height.set(70);
        assertEquals(List.of("width 120", "height 60"), seen);

        // Stopping twice is harmless and the observables stay usable.
        registrations.removeAll();
        registrations.add(width,
                (object, oldValue, newValue) -> seen.add("restarted"));
        width.set(160);
        assertEquals(List.of("width 120", "height 60", "restarted"), seen);
    }

}
