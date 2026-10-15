package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.event.NewUnitWasBornEvent;
import org.microcol.model.event.UnitArrivedToColoniesEvent;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.UnitActionPlowField;
import org.microcol.model.unit.UnitActionType;
import org.microcol.model.unit.UnitFreeColonist;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

class NationalTurnPreparationTest {
    @TempDir
    Path directory;
    private final GameModelPoDao dao = new GameModelPoDao();

    private GameModelPo fixture() {
        return dao.loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
    }

    private Model make(final GameModelPo data) {
        return Model.make(data.getModel(),
                List.of(GameOverEvaluator.GAMEOVER_CONDITION_CALENDAR));
    }

    private void nextOwnerTurn(final Model model) {
        model.endTurn();
        model.endTurn();
        assertEquals("Dutch", model.getCurrentPlayer().getName());
    }

    @Test
    void arrivalsAndImmigrationPrecedeBirthAndMovementThenContinuePreservesState() {
        final GameModelPo data = fixture();
        data.getModel().getCalendar().setNumberOfPlayedTurns(10);
        final UnitPo shipData = data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == 3).findFirst().orElseThrow();
        shipData.getPlaceHighSeas().setRemainigTurns(1);
        shipData.setAvailableMoves(0);
        data.getModel().getColonies().get(1).getColonyWarehouse().put("CORN",
                200);
        final Model model = make(data);
        final Colony colony = model.getColonyById(1).orElseThrow();
        final Unit ship = model.getUnitById(3);
        final Player human = model.getCurrentPlayer();
        final long immigrants = human.getAllUnits().stream()
                .filter(Unit::isAtEuropePier).count();
        final int expectedCorn = colony.getGoodsStats()
                .getStatsByType(GoodsType.CORN).getInWarehouseAfter() - 200;
        final List<String> stages = new ArrayList<>();
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onUnitArraviedToColonies(
                    final UnitArrivedToColoniesEvent event) {
                stages.add("arrival");
                assertSame(ship, event.getShip());
                assertEquals(200, colony.getWarehouse().getGoods(GoodsType.CORN)
                        .getAmount());
                assertEquals(0, ship.getActionPoints());
            }

            @Override
            public void onNewUnitWasBorn(final NewUnitWasBornEvent event) {
                stages.add("birth");
                assertTrue(ship.isAtPlaceLocation());
                assertEquals(immigrants + 1, human.getAllUnits().stream()
                        .filter(Unit::isAtEuropePier).count());
                assertEquals(0, ship.getActionPoints());
            }
        });
        nextOwnerTurn(model);
        assertEquals(List.of("arrival", "birth"), stages);
        assertEquals(expectedCorn,
                colony.getWarehouse().getGoods(GoodsType.CORN).getAmount());
        assertEquals(ship.getSpeed(), ship.getActionPoints());
        data.setModel(model.save());
        final Path file = directory.resolve("prepared.microcol");
        dao.saveToFile(file.toString(), data);
        final Model resumed = make(dao.loadFromFile(file.toFile()));
        final Gson gson = new Gson();
        final JsonElement saved = gson.toJsonTree(resumed.save());
        resumed.startGame();
        resumed.startGame();
        assertEquals(saved, gson.toJsonTree(resumed.save()));
    }

    @Test
    void completedPlowingChangesTheFollowingColonyUpdate() {
        final GameModelPo data = fixture();
        final Location field = Location.of(24, 14);
        final java.util.Set<Location> fields = data.getModel().getMap()
                .getFieldSet();
        fields.remove(field);
        data.getModel().getMap().setFields(fields);
        final Model model = make(data);
        final Colony colony = model.getColonyById(0).orElseThrow();
        final UnitFreeColonist pioneer = (UnitFreeColonist) model
                .addUnitOutSideColony(colony);
        pioneer.placeToLocation(field);
        pioneer.setTools(100);
        pioneer.setActionType(UnitActionType.plowField);
        ((UnitActionPlowField) pioneer.getAction()).setRemainingTurns(1);
        final int firstStock = colony.getGoodsStats()
                .getStatsByType(GoodsType.CORN).getInWarehouseAfter();
        final int firstProduction = colony
                .getColonyFieldInDirection(Direction.west).getProduction()
                .orElseThrow().getAmount();
        nextOwnerTurn(model);
        assertEquals(firstStock,
                colony.getWarehouse().getGoods(GoodsType.CORN).getAmount());
        assertTrue(model.getMap().getTerrainAt(field).isHasField());
        assertEquals(UnitActionType.noAction, pioneer.getAction().getType());
        assertEquals(0, pioneer.getActionPoints());
        assertTrue(colony.getColonyFieldInDirection(Direction.west)
                .getProduction().orElseThrow().getAmount() > firstProduction);
        final int secondStock = colony.getGoodsStats()
                .getStatsByType(GoodsType.CORN).getInWarehouseAfter();
        nextOwnerTurn(model);
        assertEquals(secondStock,
                colony.getWarehouse().getGoods(GoodsType.CORN).getAmount());
        assertEquals(pioneer.getSpeed(), pioneer.getActionPoints());
    }
}
