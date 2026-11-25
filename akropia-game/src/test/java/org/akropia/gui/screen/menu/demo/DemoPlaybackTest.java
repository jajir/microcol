package org.akropia.gui.screen.menu.demo;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DemoPlaybackTest {
    @Test
    void founderWalkIsPresentationOnlyAndReplaysAfterSeekingBack() {
        var scene = DemoScene.load();
        long start = 0;
        for (var step : scene.steps) {
            if ("animate".equals(step.action))
                break;
            start += step.durationMs;
        }
        var playback = new DemoPlayback(scene);
        playback.seek(start + 600);
        assertEquals("animate", playback.current().action);
        assertEquals(0.5, playback.fraction(), 0.0001);
        assertEquals(org.akropia.model.Location.of(25, 15),
                scene.movementOrigin(playback.model(), playback.current()));
        assertEquals(org.akropia.model.Location.of(25, 14),
                playback.model().getUnitById(0).getLocation());
        assertEquals(1, playback.model().getColonies().size());
        playback.seek(0);
        playback.seek(start + 600);
        assertEquals("animate", playback.current().action);
        assertEquals(0.5, playback.fraction(), 0.0001);
    }

    @Test
    void loadedSceneKeepsItsInitialStateUntilExplicitReload(
            @org.junit.jupiter.api.io.TempDir java.nio.file.Path directory)
            throws Exception {
        for (String file : java.util.List.of("scene.json",
                "initial-state.json")) {
            try (var input = DemoScene.class
                    .getResourceAsStream("/demos/first-colony/" + file)) {
                java.nio.file.Files.copy(input, directory.resolve(file));
            }
        }
        var scene = DemoScene.load(directory);
        var playback = new DemoPlayback(scene);
        playback.seek(playback.duration());
        java.nio.file.Files.writeString(directory.resolve("initial-state.json"),
                "{");
        playback.seek(0);
        assertTrue(playback.model().getColonies().isEmpty());
        assertEquals(31, playback.model().getUnitById(3).getLocation().getX());
        assertThrows(RuntimeException.class,
                () -> new DemoPlayback(DemoScene.load(directory)));
    }

    @Test
    void seekingBackReconstructsStateAndPreservesPartialMovement() {
        var playback = new DemoPlayback(DemoScene.load());
        playback.seek(playback.duration());
        assertEquals(1, playback.model().getColonies().size());
        playback.seek(0);
        assertTrue(playback.model().getColonies().isEmpty());
        assertEquals(31, playback.model().getUnitById(3).getLocation().getX());
        playback.seek(2100);
        assertEquals("move", playback.current().action);
        assertEquals(0.5, playback.fraction(), 0.0001);
        assertEquals(31, playback.model().getUnitById(3).getLocation().getX());
        playback.seek(2700);
        assertEquals(30, playback.model().getUnitById(3).getLocation().getX());
        assertThrows(IllegalArgumentException.class,
                () -> playback.seek(Double.NaN));
    }
}
