package org.akropia.gui.screen.menu.demo;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.akropia.model.Location;

class DemoSceneTest {
    @Test
    void landingAndFieldWalkHaveSeparateOriginsWithoutEarlyMutation() {
        var scene = DemoScene.load();
        var model = scene.newModel();
        int landings = 0;
        boolean walked = false;
        for (var step : scene.steps) {
            if ("disembark".equals(step.action)) {
                var passenger = scene.actor(model, step);
                assertTrue(passenger.isAtCargoSlot());
                assertEquals(model.getUnitById(3).getLocation(),
                        scene.movementOrigin(model, step));
                assertTrue(passenger.isAtCargoSlot());
                assertEquals(Location.of(25, 15), scene.destination(step));
                assertTrue(step.durationMs > 0);
                landings++;
            }
            if ("move".equals(step.action) && "pioneer".equals(step.actor)) {
                assertEquals(Location.of(25, 15),
                        scene.movementOrigin(model, step));
                assertEquals(Location.of(25, 16), scene.destination(step));
                assertTrue(step.durationMs > 0);
                walked = true;
            }
            scene.execute(model, step);
        }
        assertEquals(2, landings);
        assertTrue(walked);
        assertTrue(
                model.getMap().getTerrainAt(Location.of(25, 16)).isHasField());
    }

    @Test
    void completeSceneAndRestartAreIsolated() {
        var scene = DemoScene.load();
        var model = scene.newModel();
        var untouched = scene.newModel();
        var before = new com.google.gson.Gson().toJson(untouched.save());
        int index = 0;
        for (var step : scene.steps) {
            try {
                scene.execute(model, step);
            } catch (RuntimeException e) {
                throw new AssertionError("Step " + index + " " + step.action,
                        e);
            }
            index++;
        }
        assertEquals(33, model.getUnitById(3).getLocation().getX());
        assertEquals(1, model.getColonies().size());
        assertTrue(
                model.getMap().getTerrainAt(Location.of(25, 16)).isHasField());
        assertEquals(before,
                new com.google.gson.Gson().toJson(untouched.save()));
        assertTrue(untouched.getColonies().isEmpty());
        assertEquals(31, untouched.getUnitById(3).getLocation().getX());
        assertTrue(scene.newModel().getColonies().isEmpty());
    }

    @Test
    void rejectsCoordinatesOutsideMap() {
        var scene = DemoScene.load();
        scene.camera = new int[] { 9999, 9999 };
        assertThrows(IllegalArgumentException.class, scene::newModel);
        scene = DemoScene.load();
        scene.steps.get(1).to = new int[] { 9999, 9999 };
        assertThrows(IllegalArgumentException.class, scene::newModel);
    }

    @Test
    void rejectsUnknownActionAndInvalidTiming() {
        var scene = DemoScene.load();
        scene.steps.get(0).action = "script";
        assertThrows(IllegalArgumentException.class, scene::validate);
        scene = DemoScene.load();
        scene.steps.get(0).durationMs = -1;
        assertThrows(IllegalArgumentException.class, scene::validate);
    }
}
