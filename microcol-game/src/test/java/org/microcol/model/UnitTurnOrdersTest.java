package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.event.TurnFinishedEvent;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.UnitActionType;
import org.microcol.model.unit.UnitFreeColonist;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class UnitTurnOrdersTest {
    @TempDir
    Path directory;
    private final GameModelPoDao dao = new GameModelPoDao();
    private final Gson gson = new Gson();

    private Model make(final ModelPo state) {
        return Model.make(state,
                List.of(GameOverEvaluator.GAMEOVER_CONDITION_CALENDAR));
    }

    private Model fixture() {
        final GameModelPo data = dao.loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        data.getModel().getCalendar().setNumberOfPlayedTurns(10);
        final java.util.Set<Location> trees = data.getModel().getMap()
                .getTreeSet();
        trees.remove(Location.of(24, 14));
        data.getModel().getMap().setTrees(trees);
        final java.util.Set<Location> fields = data.getModel().getMap()
                .getFieldSet();
        fields.remove(Location.of(24, 14));
        data.getModel().getMap().setFields(fields);
        final Model model = make(data.getModel());
        model.getAllUnits()
                .forEach(unit -> ((AbstractUnit) unit).setActionPoints(0));
        for (final int id : List.of(0, 1, 2)) {
            final Unit unit = model.getUnitById(id);
            unit.placeToLocation(Location.of(25, 14));
            ((AbstractUnit) unit).setActionPoints(unit.getSpeed());
        }
        return model;
    }

    private JsonElement snapshot(final Model model) {
        return gson.toJsonTree(model.save());
    }

    private void verifyRejectedWithoutMutation(final Model model,
            final Unit unit) {
        final JsonElement before = snapshot(model);
        assertFalse(model.canSkipUnitTurn(unit));
        assertThrows(IllegalStateException.class,
                () -> model.skipUnitTurn(unit));
        assertEquals(before, snapshot(model));
    }

    private Unit addEnemy(final Model model) {
        final UnitPo enemy = model.getUnitById(0).save();
        enemy.setId(900);
        enemy.setOwnerId("Dutch's King");
        enemy.getPlaceMap().setLocation(Location.of(24, 14));
        return model.createUnit(model.save(), enemy);
    }

    // --- Test methods -----------------------------------------------------

    @Test
    void testWaitLikeQueryLoop() {
        final Model model = fixture();
        addEnemy(model);
        final JsonElement before = snapshot(model);
        Unit selected = model.getFirstSelectableUnit().orElseThrow();
        assertEquals(0, selected.getId());
        for (final int expectedId : List.of(1, 2, 0, 1, 2, 0)) {
            selected = model.getNextUnitForCurrentPlayer(selected)
                    .orElseThrow();
            assertEquals(expectedId, selected.getId());
        }
        assertEquals(before, snapshot(model));
    }

    @Test
    void testOneZero() {
        final Model model = fixture();
        final Unit unit0 = model.getUnitById(0);
        final Unit unit1 = model.getUnitById(1);
        final Unit unit2 = model.getUnitById(2);
        model.skipUnitTurn(unit0);
        model.skipUnitTurn(unit1);
        model.skipUnitTurn(unit2);
        assertFalse(model.getFirstSelectableUnit().isPresent());
        assertFalse(model.getNextUnitForCurrentPlayer(unit0).isPresent());
        ((AbstractUnit) unit2).setActionPoints(unit2.getSpeed());
        final Optional<Unit> first = model.getFirstSelectableUnit();
        assertTrue(first.isPresent());
        assertEquals(unit2, first.get());
        final Optional<Unit> next = model
                .getNextUnitForCurrentPlayer(first.get());
        assertTrue(next.isPresent());
        assertEquals(unit2, next.get());
    }

    @Test
    void testSuccessfulSkip() {
        final Model model = fixture();
        final Unit unit = model.getUnitById(0);
        final JsonElement expected = snapshot(model);
        for (final JsonElement entry : expected.getAsJsonObject()
                .getAsJsonArray("units")) {
            if (entry.getAsJsonObject().get("id").getAsInt() == unit.getId()) {
                entry.getAsJsonObject().addProperty("movementThirds", 0);
            }
        }
        assertTrue(model.canSkipUnitTurn(unit));
        model.skipUnitTurn(unit);
        assertEquals(0, unit.getActionPoints());
        assertTrue(unit.getAction().isNoAction());
        assertEquals(expected, snapshot(model));
        assertEquals(1, model.getFirstSelectableUnit().orElseThrow().getId());
        verifyRejectedWithoutMutation(model, unit);
    }

    @Test
    void testRealFileRoundTrip() throws Exception {
        final Model model = fixture();
        model.skipUnitTurn(model.getUnitById(0));
        final Path path = directory.resolve("save.microcol");
        final GameModelPo data = dao.loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        data.setModel(model.save());
        dao.saveToFile(path.toString(), data);
        final Model loaded = make(dao.loadFromFile(path.toFile()).getModel());
        assertEquals(0, loaded.getUnitById(0).getActionPoints());
        final JsonElement before = snapshot(loaded);
        loaded.startGame();
        loaded.startGame();
        assertEquals(before, snapshot(loaded));
        assertFalse(loaded.canSkipUnitTurn(loaded.getUnitById(0)));
        loaded.endTurn();
        loaded.endTurn();
        final Unit restored = loaded.getUnitById(0);
        assertEquals(restored.getSpeed(), restored.getActionPoints());
        assertTrue(restored.getAction().isNoAction());
    }

    @Test
    void testShipWithCargo() {
        final Model model = fixture();
        final Unit ship = model.getUnitById(3);
        ship.placeToLocation(Location.of(49, 15));
        ((AbstractUnit) ship).setActionPoints(ship.getSpeed());
        final JsonObject expected = gson.toJsonTree(ship.save())
                .getAsJsonObject();
        expected.addProperty("movementThirds", 0);
        final JsonElement passenger = gson
                .toJsonTree(model.getUnitById(16).save());
        model.skipUnitTurn(ship);
        assertEquals(expected, gson.toJsonTree(ship.save()));
        assertEquals(passenger, gson.toJsonTree(model.getUnitById(16).save()));
        assertTrue(ship.getAction().isNoAction());
    }

    @Test
    void testIdentityOwnership() {
        final Model model = fixture();
        verifyRejectedWithoutMutation(model, addEnemy(model));
        verifyRejectedWithoutMutation(model, fixture().getUnitById(0));
        final Unit removed = model.getUnitById(0);
        model.destroyUnit(removed);
        verifyRejectedWithoutMutation(model, removed);
    }

    @ParameterizedTest
    @ValueSource(strings = { "stopped", "finished", "not-started" })
    void testUnavailableGameStates(final String state) {
        Model model = fixture();
        if ("stopped".equals(state)) {
            model.stop();
        } else {
            final ModelPo saved = model.save();
            if ("finished".equals(state)) {
                saved.getGameManager().setGameFinished(true);
            } else {
                saved.getGameManager().setGameStarted(false);
            }
            model = make(saved);
        }
        if ("not-started".equals(state)) {
            final Model unavailable = model;
            final Unit unit = unavailable.getUnitById(0);
            final JsonElement before = gson.toJsonTree(unit.save());
            assertFalse(unavailable.canSkipUnitTurn(unit));
            assertThrows(IllegalStateException.class,
                    () -> unavailable.skipUnitTurn(unit));
            assertEquals(before, gson.toJsonTree(unit.save()));
            assertFalse(unavailable.isGameRunning());
        } else {
            verifyRejectedWithoutMutation(model, model.getUnitById(0));
        }
    }

    @Test
    void testRejectOffMap() {
        final Model model = fixture();
        verifyRejectedWithoutMutation(model, model.getUnitById(16));
        final Unit spent = model.getUnitById(0);
        model.skipUnitTurn(spent);
        verifyRejectedWithoutMutation(model, spent);
        final Unit busy = model.getUnitById(1);
        // Plowing needs a pioneer outside the colony on clear land.
        busy.placeToLocation(Location.of(24, 14));
        ((UnitFreeColonist) busy).setTools(100);
        busy.setActionType(UnitActionType.plowField);
        ((AbstractUnit) busy).setActionPoints(busy.getSpeed());
        verifyRejectedWithoutMutation(model, busy);
        assertEquals(2, model.getFirstSelectableUnit().orElseThrow().getId());
        assertEquals(2,
                model.getNextUnitForCurrentPlayer(busy).orElseThrow().getId());
    }

    @Test
    void testNull() {
        final Model model = fixture();
        final JsonElement before = snapshot(model);
        assertFalse(model.canSkipUnitTurn(null));
        assertThrows(NullPointerException.class,
                () -> model.skipUnitTurn(null));
        assertEquals(before, snapshot(model));
    }

    @Test
    void testPreparationAndFailedPreparation() {
        final Model model = fixture();
        final Unit unit = model.getUnitById(0);
        final JsonElement before = gson.toJsonTree(unit.save());
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onTurnFinished(final TurnFinishedEvent event) {
                assertFalse(model.isGameRunning());
                assertFalse(model.canSkipUnitTurn(unit));
                assertThrows(IllegalStateException.class,
                        () -> model.skipUnitTurn(unit));
                assertEquals(before, gson.toJsonTree(unit.save()));
                throw new IllegalStateException("Injected preparation failure");
            }
        });
        assertEquals("Injected preparation failure",
                assertThrows(IllegalStateException.class, model::endTurn)
                        .getMessage());
        assertFalse(model.canSkipUnitTurn(unit));
        assertThrows(IllegalStateException.class,
                () -> model.skipUnitTurn(unit));
        assertEquals(before, gson.toJsonTree(unit.save()));
    }
}
