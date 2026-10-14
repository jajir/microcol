package org.microcol.ai;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.microcol.gui.screen.game.gamepanel.AnimationManager;
import org.microcol.model.Direction;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.TerrainType;
import org.microcol.model.Unit;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.UnitPo;

import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;

class BoundedAiMovementTest {
    private static final Location START = Location.of(30, 15);
    private static final Location EXIT = Location.of(31, 15);

    private Model prepare(final boolean withExit, final int moves) {
        final GameModelPo data = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.microcol");
        data.getModel().getGameManager().setGameStarted(true);
        data.getModel().getGameManager().setCurrentPlayer("France");
        final Map<Location, TerrainType> terrain = data.getModel().getMap()
                .getTerrainMap();
        START.getNeighbors().forEach(at -> terrain.put(at, TerrainType.OCEAN));
        terrain.put(START, TerrainType.GRASSLAND);
        if (withExit) {
            terrain.put(EXIT, TerrainType.GRASSLAND);
        }
        data.getModel().getMap().setTerrainType(terrain);
        final UnitPo guard = data.getModel().getUnits().stream()
                .filter(u -> u.getId() == 6).findFirst().orElseThrow();
        final PlaceMapPo place = new PlaceMapPo();
        place.setLocation(START);
        place.setOrientation(Direction.west);
        guard.setPlaceMap(place);
        guard.setAvailableMoves(moves);
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private void move(final Model model) {
        final SimpleAiPlayer ai = new SimpleAiPlayer(model,
                model.getPlayerByName("France"),
                new AnimationManager(new EventBus()));
        try {
            assertTimeoutPreemptively(Duration.ofSeconds(2),
                    () -> ai.moveUnit(model.getUnitById(6)));
        } finally {
            ai.stop();
        }
    }

    @Test
    void surroundedUnitReturnsWithoutChangingTheWorld() {
        final Model model = prepare(false, 1);
        final String before = new Gson().toJson(model.save());
        move(model);
        assertEquals(before, new Gson().toJson(model.save()));
    }

    @Test
    void singleLegalExitIsUsedWithoutDependingOnRandomRetries() {
        final Model model = prepare(true, 1);
        final String humanBefore = new Gson()
                .toJson(model.getUnitById(3).save());
        move(model);
        final Unit guard = model.getUnitById(6);
        assertEquals(EXIT, guard.getLocation());
        assertEquals(0, guard.getActionPoints());
        assertEquals(humanBefore,
                new Gson().toJson(model.getUnitById(3).save()));
    }

    @Test
    void exhaustedUnitDoesNotMoveEvenWithAnOpenExit() {
        final Model model = prepare(true, 0);
        final String before = new Gson().toJson(model.save());
        move(model);
        assertEquals(before, new Gson().toJson(model.save()));
    }
}
