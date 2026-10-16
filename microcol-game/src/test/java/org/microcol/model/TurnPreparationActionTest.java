package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.event.NewUnitWasBornEvent;
import org.microcol.model.unit.UnitActionType;
import org.microcol.model.unit.UnitFreeColonist;
import org.microcol.model.unit.UnitWithCargo;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

class TurnPreparationActionTest {
    private static Stream<String> commands() {
        return Stream.of("buy", "sell", "rename", "independence",
                "queue-building", "queue-unit", "queue-remove", "queue-before",
                "queue-end", "cargo-buy", "cargo-sell", "cargo-transfer",
                "warehouse-load", "warehouse-unload", "field", "indoor",
                "outside", "crop", "job-choice", "action", "move",
                "move-as-far");
    }

    private Model fixture(final boolean beforePreparation) {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        data.getModel().getCalendar().setStartYear(1700);
        data.getModel().getCalendar().setNumberOfPlayedTurns(10);
        data.getModel().getColonies().get(1).getColonyWarehouse().put("CORN",
                200);
        data.getModel().getColonies().get(0).getColonyWarehouse().put("TOOLS",
                0);
        final Model model = make(data);
        final Colony colony = model.getColonyById(0).orElseThrow();
        final UnitFreeColonist pioneer = (UnitFreeColonist) model
                .getUnitById(16);
        pioneer.placeToLocation(colony.getLocation());
        pioneer.setTools(10);
        pioneer.setActionPoints(pioneer.getSpeed());
        model.getUnitById(3).placeToLocation(colony.getLocation());
        ((UnitWithCargo) model.getUnitById(3)).getCargo().getSlotByIndex(4)
                .addGoods(Goods.of(GoodsType.ORE, 7));
        colony.getColonyBuildingQueue().addAtEnd(ConstructionType.STOCKADE);
        colony.getColonyBuildingQueue().addAtEnd(ConstructionType.WAREHOUSE);
        if (!beforePreparation)
            return model;
        data.setModel(model.save());
        data.getModel().getGameManager().setCurrentPlayer("Dutch's King");
        return make(data);
    }

    private Model make(final GameModelPo data) {
        return Model.make(data.getModel(),
                List.of(GameOverEvaluator.GAMEOVER_CONDITION_CALENDAR));
    }

    private Runnable command(final String name, final Model model) {
        final Player player = model.getPlayerByName("Dutch");
        final Colony colony = model.getColonyById(0).orElseThrow();
        final ColonyBuildingQueue queue = colony.getColonyBuildingQueue();
        final Unit unit = model.getUnitById(16);
        final Cargo cargo = ((UnitWithCargo) model.getUnitById(3)).getCargo();
        final CargoSlot target = cargo.getSlotByIndex(0);
        final CargoSlot source = cargo.getSlotByIndex(4);
        final Goods corn = Goods.of(GoodsType.CORN, 1);
        final Goods ore = Goods.of(GoodsType.ORE, 1);
        switch (name) {
        case "buy":
            return () -> player.buyGoods(corn);
        case "sell":
            return () -> player.sellGoods(corn);
        case "rename":
            return () -> model.renameColony(player, colony, "Prepared colony");
        case "independence":
            return player::declareIndependence;
        case "queue-building":
            return () -> queue.addAtEnd(ConstructionType.STABLES);
        case "queue-unit":
            return () -> queue.addAtEnd(UnitType.WAGON);
        case "queue-remove":
            return () -> queue.removeItem(queue.getBuildingQueue().get(0));
        case "queue-before":
            return () -> queue.moveItemBefore(queue.getBuildingQueue().get(1),
                    queue.getBuildingQueue().get(0));
        case "queue-end":
            return () -> queue
                    .moveItemAtTheEnd(queue.getBuildingQueue().get(0));
        case "cargo-buy":
            return () -> target.buyAndStoreFromEuropePort(corn);
        case "cargo-sell":
            return () -> model.sellGoods(source, ore);
        case "cargo-transfer":
            return () -> target.storeFromCargoSlot(ore, source);
        case "warehouse-load":
            return () -> target.storeFromColonyWarehouse(corn, colony);
        case "warehouse-unload":
            return () -> colony.getWarehouse().moveToWarehouse(ore, source);
        case "field":
            return () -> unit.placeToColonyField(
                    colony.getColonyFieldInDirection(Direction.north),
                    GoodsType.CORN);
        case "indoor":
            return () -> unit.placeToColonyStructureSlot(
                    colony.getConstructionByType(ConstructionType.TOWN_HALL)
                            .getFirstEmptySlot().orElseThrow());
        case "job-choice":
            return colony.getJobChoices(unit).stream().filter(
                    choice -> choice.getKind() == ColonyJobChoice.Kind.INDOOR
                            && choice.getGoodsType() == GoodsType.TOOLS)
                    .findFirst().orElseThrow()::execute;
        case "outside":
            return () -> model.getUnitById(0).placeToMap(colony.getLocation());
        case "crop":
            return () -> colony.getColonyFieldInDirection(Direction.west)
                    .setProducedGoodsType(GoodsType.CORN);
        case "action":
            return () -> unit.setActionType(UnitActionType.plowField);
        case "move":
            return () -> model.moveUnit(unit,
                    Path.of(List.of(Location.of(24, 14))));
        case "move-as-far":
            return () -> model.moveUnitAsFarAsPossible(unit,
                    Path.of(List.of(Location.of(24, 14))));
        default:
            throw new IllegalArgumentException(name);
        }
    }

    /**
     * Inspect callback state without asking the model to create a playable
     * save.
     */
    private JsonElement inspect(final Model model) {
        return new Gson()
                .toJsonTree(
                        Map.of("players", model.getPlayers().stream()
                                .map(Player::save).collect(Collectors.toList()),
                                "units",
                                model.getAllUnits().stream().map(Unit::save)
                                        .collect(Collectors.toList()),
                                "colonies",
                                model.getColonies().stream().map(Colony::save)
                                        .collect(Collectors.toList()),
                                "calendar", model.getCalendar().save()));
    }

    @ParameterizedTest
    @MethodSource("commands")
    void otherwiseLegalCommandsRejectThroughoutPreparationWithoutMutation(
            final String name) {
        final Model ready = fixture(false);
        assertDoesNotThrow(command(name, ready)::run,
                "The action must be legal at a completed boundary");
        final Model model = fixture(true);
        final AtomicInteger observed = new AtomicInteger();
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onNewUnitWasBorn(final NewUnitWasBornEvent event) {
                observed.incrementAndGet();
                final JsonElement before = inspect(model);
                assertThrows(IllegalStateException.class,
                        command(name, model)::run);
                assertEquals(before, inspect(model));
                throw new IllegalStateException(
                        "Injected failure after command rejection");
            }
        });
        assertEquals("Injected failure after command rejection",
                assertThrows(IllegalStateException.class, model::endTurn)
                        .getMessage());
        assertEquals(1, observed.get());
        assertFalse(model.isGameRunning());
        final JsonElement failed = inspect(model);
        assertThrows(IllegalStateException.class, command(name, model)::run);
        assertEquals(failed, inspect(model));
    }
}
