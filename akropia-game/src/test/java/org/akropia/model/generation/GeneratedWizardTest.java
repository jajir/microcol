package org.akropia.model.generation;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import org.akropia.model.*;
import org.akropia.model.campaign.MissionImpl;
import org.akropia.model.store.ModelPo;
import org.junit.jupiter.api.Test;

class GeneratedWizardTest {
    @Test
    void everySizeAndLayoutHasPlayableSeparatedLandAndSavedMetadata() {
        for (GeneratedMapSize size : GeneratedMapSize.values()) {
            for (GeneratedLandLayout layout : GeneratedLandLayout.values()) {
                long begin = System.nanoTime();
                ModelPo world = GeneratedWorld.create(
                        new GeneratedWorldSettings("Nový svět 42!", size,
                                layout),
                        EuropeanNation.DUTCH, Difficulty.CONQUISTADOR);
                assertEquals(size.getWidth(), world.getMap().getMaxX());
                assertEquals(size.getHeight(), world.getMap().getMaxY());
                Set<Location> remaining = new HashSet<>();
                world.getMap().getTerrainMap().forEach((at, type) -> {
                    if (!type.isSea())
                        remaining.add(at);
                });
                int components = 0;
                while (!remaining.isEmpty()) {
                    components++;
                    Deque<Location> queue = new ArrayDeque<>();
                    Location first = remaining.iterator().next();
                    remaining.remove(first);
                    queue.add(first);
                    while (!queue.isEmpty()) {
                        Location at = queue.remove();
                        for (int[] step : GeneratedGrid.STEPS) {
                            Location next = Location.of(at.getX() + step[0],
                                    at.getY() + step[1]);
                            if (remaining.remove(next))
                                queue.add(next);
                        }
                    }
                }
                assertEquals(layout == GeneratedLandLayout.ONE_LANDMASS ? 1
                        : layout == GeneratedLandLayout.BIG_ISLANDS ? 4
                                : Math.max(3, size.getWidth() / 24)
                                        * Math.max(2, size.getHeight() / 24),
                        components);
                assertFalse(world.getMap().getRiverMap().isEmpty());
                assertFalse(world.getMap().getResourceMap().isEmpty());
                assertTrue(world.getNativeSettlements().size() >= 3);
                Model model = Model.make(world,
                        MissionImpl.getPredefinedGameOverEvaluators());
                model.startGame();
                ModelPo saved = model.save();
                model.stop();
                assertEquals("Nový svět 42!",
                        saved.getGeneratedWorld().getTextSeed());
                assertEquals(size, saved.getGeneratedWorld().getSize());
                assertEquals(layout, saved.getGeneratedWorld().getLayout());
                assertEquals(2,
                        saved.getGeneratedWorld().getVersion().intValue());
                assertArrayEquals(world.getMap().getTiles(),
                        saved.getMap().getTiles());
                System.out.println(
                        size + " " + layout + " generation/validation ms="
                                + (System.nanoTime() - begin) / 1_000_000);
            }
        }
    }

    @Test
    void textSeedsAreStableAndIndependentOfDifficultyAndNation() {
        GeneratedWorldSettings settings = new GeneratedWorldSettings(
                "  Žluťoučký svět!  ", GeneratedMapSize.SMALL,
                GeneratedLandLayout.ARCHIPELAGO);
        ModelPo first = GeneratedWorld.create(settings, EuropeanNation.DUTCH,
                Difficulty.CONQUISTADOR);
        ModelPo second = GeneratedWorld.create(settings, EuropeanNation.ENGLISH,
                Difficulty.DISCOVERER);
        assertEquals("Žluťoučký svět!", settings.getTextSeed());
        assertArrayEquals(first.getMap().getTiles(),
                second.getMap().getTiles());
        assertEquals(first.getMap().getRiverMap(),
                second.getMap().getRiverMap());
        assertEquals(first.getMap().getResourceMap(),
                second.getMap().getResourceMap());
        assertEquals(
                first.getNativeSettlements().stream().map(s -> s.getLocation())
                        .collect(java.util.stream.Collectors.toList()),
                second.getNativeSettlements().stream().map(s -> s.getLocation())
                        .collect(java.util.stream.Collectors.toList()));
        assertNotEquals(settings.getGenerationSeed(),
                new GeneratedWorldSettings("jiný svět", GeneratedMapSize.SMALL,
                        GeneratedLandLayout.ARCHIPELAGO).getGenerationSeed());
        assertThrows(IllegalArgumentException.class,
                () -> new GeneratedWorldSettings(" ", GeneratedMapSize.SMALL,
                        GeneratedLandLayout.BIG_ISLANDS));
        assertThrows(IllegalArgumentException.class,
                () -> new GeneratedWorldSettings("😀".repeat(65),
                        GeneratedMapSize.SMALL,
                        GeneratedLandLayout.BIG_ISLANDS));
        assertEquals(64,
                new GeneratedWorldSettings("😀".repeat(64),
                        GeneratedMapSize.SMALL, GeneratedLandLayout.BIG_ISLANDS)
                        .getTextSeed().codePointCount(0, 128));
        first.getGeneratedWorld().setTextSeed("tampered");
        assertThrows(IllegalArgumentException.class, () -> Model.make(first,
                MissionImpl.getPredefinedGameOverEvaluators()));
    }
}
