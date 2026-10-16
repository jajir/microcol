package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.io.TempDir;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.event.NewUnitWasBornEvent;
import org.microcol.model.event.UnitArrivedToColoniesEvent;
import org.microcol.model.store.UnitPo;
import org.microcol.model.turnevent.TurnEventNewUnitInColony;
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

    @ParameterizedTest
    @CsvSource({ "199,199,0", "200,0,1", "201,1,1", "399,199,1", "400,0,2",
            "401,1,2" })
    void foodBoundaryCreatesOutsideColonistsAndSurvivesResume(
            final int effectiveFood, final int remainingFood,
            final int births) {
        final GameModelPo data = fixture();
        final Model model = make(data);
        final Colony colony = model.getColonyById(1).orElseThrow();
        for (int index = 0; index < 2; index++) {
            model.addUnitOutSideColony(colony).placeToColonyField(
                    colony.getEmptyFieldsWithMaxCornProduction().get(0),
                    GoodsType.CORN);
        }
        model.addUnitOutSideColony(colony);
        model.addUnitOutSideColony(colony);
        data.setModel(model.save());
        final Player owner = colony.getOwner();
        final int consumed = colony.getRequiredFoodPerTurn();
        assertEquals(3, colony.getUnitsInsideColony().size());
        assertEquals(6, consumed);
        final int initialOutside = colony.getUnitsOutSideColony().size();
        assertEquals(2, initialOutside);
        final List<Integer> visitorIds = colony.getUnitsOutSideColony().stream()
                .map(Unit::getId).collect(Collectors.toList());
        final int initialUnits = owner.getAllUnits().size();
        final int highestId = model.save().getUnits().stream()
                .mapToInt(UnitPo::getId).max().orElseThrow();
        final int productionDiff = colony.getRawGoodsStats()
                .getStatsByType(GoodsType.CORN).getDiff();
        final var savedColony = data.getModel().getColonies().get(1);
        final var warehouse = new HashMap<>(savedColony.getColonyWarehouse());
        warehouse.put("CORN", effectiveFood - productionDiff);
        savedColony.setColonyWarehouse(warehouse);
        final Model prepared = make(data);
        assertEquals(effectiveFood,
                prepared.getColonyById(1).orElseThrow().getRawGoodsStats()
                        .getStatsByType(GoodsType.CORN).getInWarehouseAfter());
        assertEquals(consumed, colony.getRequiredFoodPerTurn());

        data.setModel(prepared.save());
        final Model before = make(data);
        assertEquals(effectiveFood,
                before.getColonyById(1).orElseThrow().getRawGoodsStats()
                        .getStatsByType(GoodsType.CORN).getInWarehouseAfter());
        nextOwnerTurn(before);
        final Colony updated = before.getColonyById(1).orElseThrow();
        final Player updatedOwner = updated.getOwner();
        assertEquals(remainingFood,
                updated.getWarehouse().getGoods(GoodsType.CORN).getAmount());
        assertEquals(initialUnits + births, updatedOwner.getAllUnits().size());
        assertEquals(initialOutside + births,
                updated.getUnitsOutSideColony().size());
        assertTrue(updated.getUnitsOutSideColony().stream().map(Unit::getId)
                .collect(Collectors.toList()).containsAll(visitorIds));
        assertEquals(births,
                before.getTurnEventStore().getForPlayer(updatedOwner).stream()
                        .filter(TurnEventNewUnitInColony.class::isInstance)
                        .count());
        for (int index = 1; index <= births; index++) {
            final Unit newborn = before.getUnitById(highestId + index);
            assertSame(updatedOwner, newborn.getOwner());
            assertTrue(updated.getUnitsOutSideColony().contains(newborn));
            assertEquals(consumed, updated.getRequiredFoodPerTurn());
        }

        data.setModel(before.save());
        final Model resumed = make(data);
        final JsonElement saved = new Gson().toJsonTree(resumed.save());
        resumed.startGame();
        assertEquals(saved, new Gson().toJsonTree(resumed.save()));
        assertEquals(births, resumed.getTurnEventStore()
                .getForPlayer(resumed.getColonyById(1).orElseThrow().getOwner())
                .stream().filter(TurnEventNewUnitInColony.class::isInstance)
                .count());
    }

    @Test
    void arrivalsAndImmigrationPrecedeBirthAndMovementThenContinuePreservesState() {
        final GameModelPo data = fixture();
        data.getModel().getCalendar().setNumberOfPlayedTurns(10);
        data.getModel().getGameManager().getPlayers().stream()
                .filter(player -> "Dutch".equals(player.getName())).findFirst()
                .orElseThrow().getRecruitment().setCrosses(100);
        final UnitPo shipData = data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == 3).findFirst().orElseThrow();
        shipData.getPlaceHighSeas().setRemainigTurns(1);
        shipData.getPlaceHighSeas().setPosition(2);
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
                .getStatsByType(GoodsType.CORN).getInWarehouseAfter();
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
