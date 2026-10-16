package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import com.google.gson.Gson;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.UnitActionFortifyPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.UnitActionType;
import org.microcol.model.unit.UnitFreeColonist;

class FortifyOrderTest {
    private static final Location SHORE = Location.of(48, 15);

    private GameModelPo scenario() {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        final UnitPo soldier = data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == 16).findFirst().orElseThrow();
        soldier.setHoldingGuns(true);
        soldier.setTools(0);
        soldier.setAvailableMoves(1);
        final java.util.Map<Location, TerrainType> terrain = data.getModel()
                .getMap().getTerrainMap();
        terrain.put(SHORE, TerrainType.GRASSLAND);
        terrain.put(Location.of(47, 15), TerrainType.GRASSLAND);
        data.getModel().getMap().setTerrainType(terrain);
        return data;
    }

    private Model load(final GameModelPo data) {
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    @Test
    void orderSpendsMovesAndBecomesEffectiveOnlyOnNextOwnerTurn() {
        final Model model = load(scenario());
        final Unit soldier = model.getUnitById(16);
        soldier.placeToLocation(SHORE);
        assertTrue(model.canFortify(soldier));
        model.fortify(soldier);
        assertEquals(0, soldier.getActionPoints());
        assertEquals(UnitActionType.fortify, soldier.getAction().getType());
        assertFalse(model.isFortified(soldier));
        assertFalse(model.canFortify(soldier));
        assertThrows(IllegalStateException.class, () -> model.fortify(soldier));
        assertThrows(IllegalStateException.class,
                () -> soldier.placeToLocation(Location.of(47, 15)));
        assertEquals(SHORE, soldier.getLocation());
        assertThrows(IllegalStateException.class,
                () -> soldier.setActionType(UnitActionType.noAction));
        assertThrows(IllegalStateException.class,
                () -> ((UnitFreeColonist) soldier).setHoldingGuns(false));
        assertTrue(((UnitFreeColonist) soldier).isHoldingGuns());

        final Model restored = load(saved(model));
        final Unit waiting = restored.getUnitById(16);
        assertFalse(restored.isFortified(waiting));
        assertEquals(0, waiting.getActionPoints());
        assertEquals(UnitActionType.fortify, waiting.getAction().getType());
        final Player owner = waiting.getOwner();
        restored.endTurn();
        assertNotSame(owner, restored.getCurrentPlayer());
        assertFalse(restored.isFortified(waiting));
        assertEquals(0, waiting.getActionPoints());
        for (int turns = 0; turns < 8
                && restored.getCurrentPlayer() != owner; turns++) {
            restored.endTurn();
        }
        assertSame(owner, restored.getCurrentPlayer());
        assertTrue(restored.isFortified(waiting));
        assertEquals(waiting.getSpeed(), waiting.getActionPoints());
        final String effectiveState = snapshot(restored);
        assertTrue(((AbstractUnit) waiting)
                .prepareAttack(Location.of(47, 15), true).isEmpty());
        assertThrows(IllegalStateException.class, () -> restored
                .moveUnit(waiting, Path.of(List.of(Location.of(47, 15)))));
        assertThrows(IllegalStateException.class,
                () -> waiting.placeToLocation(Location.of(47, 15)));
        assertThrows(IllegalStateException.class,
                () -> waiting.setActionType(UnitActionType.plowField));
        assertThrows(IllegalStateException.class,
                () -> waiting.placeToColonyField(null, GoodsType.CORN));
        assertEquals(effectiveState, snapshot(restored));
        final Model effective = load(saved(restored));
        final Unit active = effective.getUnitById(16);
        assertTrue(effective.isFortified(active));
        final int refreshedMoves = active.getActionPoints();
        effective.cancelFortify(active);
        assertEquals(refreshedMoves, active.getActionPoints());
        assertEquals(UnitActionType.noAction, active.getAction().getType());
    }

    @Test
    void cancellationDoesNotRefundMovesAndRequiresCurrentLiveOwner() {
        final Model model = load(scenario());
        final Unit soldier = model.getUnitById(16);
        soldier.placeToLocation(SHORE);
        model.fortify(soldier);
        assertTrue(model.canCancelFortify(soldier));
        model.cancelFortify(soldier);
        assertEquals(UnitActionType.noAction, soldier.getAction().getType());
        assertEquals(0, soldier.getActionPoints());
        assertThrows(IllegalStateException.class,
                () -> model.cancelFortify(soldier));
        final Model other = load(scenario());
        assertFalse(model.canCancelFortify(other.getUnitById(16)));
        other.getUnitById(16).placeToLocation(SHORE);
        other.fortify(other.getUnitById(16));
        other.endTurn();
        final String heldState = snapshot(other);
        assertFalse(other.canCancelFortify(other.getUnitById(16)));
        assertThrows(IllegalStateException.class,
                () -> other.cancelFortify(other.getUnitById(16)));
        assertEquals(heldState, snapshot(other));
        final String firstState = snapshot(model);
        assertThrows(IllegalStateException.class,
                () -> model.cancelFortify(other.getUnitById(16)));
        assertEquals(firstState, snapshot(model));
    }

    @Test
    void roleAndSavedStateAreValidatedWithoutRepair() {
        final GameModelPo data = scenario();
        final Model model = load(data);
        final Unit soldier = model.getUnitById(16);
        soldier.placeToLocation(SHORE);
        ((UnitFreeColonist) soldier).setHoldingGuns(false);
        assertFalse(model.canFortify(soldier));
        assertThrows(IllegalStateException.class, () -> model.fortify(soldier));
        assertFalse(model.canFortify(model.getUnitById(3)));

        final GameModelPo savedData = saved(model);
        final UnitPo saved = savedData.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == 16).findFirst().orElseThrow();
        final UnitActionFortifyPo action = new UnitActionFortifyPo();
        saved.setAction(action);
        assertThrows(IllegalArgumentException.class, () -> load(savedData));
        action.setFortified(false);
        saved.setAvailableMoves(1);
        assertThrows(IllegalArgumentException.class, () -> load(savedData));
        saved.setAvailableMoves(0);
        saved.setHoldingGuns(false);
        assertThrows(IllegalArgumentException.class, () -> load(savedData));
        saved.setHoldingGuns(true);
        final java.util.Map<Location, TerrainType> terrain = savedData
                .getModel().getMap().getTerrainMap();
        terrain.put(SHORE, TerrainType.OCEAN);
        savedData.getModel().getMap().setTerrainType(terrain);
        assertThrows(IllegalArgumentException.class, () -> load(savedData));
    }

    @Test
    void placementMovementAndExistingOrdersRejectWithoutMutation() {
        final Model model = load(scenario());
        final Unit soldier = model.getUnitById(16);
        final String cargoState = snapshot(model);
        assertFalse(model.canFortify(soldier));
        assertThrows(IllegalStateException.class, () -> model.fortify(soldier));
        assertEquals(cargoState, snapshot(model));

        final Unit resident = model.getAllUnits().stream()
                .filter(unit -> unit.isAtPlaceConstructionSlot()
                        || unit.isAtPlaceColonyField())
                .findFirst().orElseThrow();
        assertFalse(model.canFortify(resident));
        assertThrows(IllegalStateException.class,
                () -> model.fortify(resident));
        assertEquals(cargoState, snapshot(model));

        soldier.placeToLocation(SHORE);
        ((AbstractUnit) soldier).setActionPoints(0);
        final String exhausted = snapshot(model);
        assertFalse(model.canFortify(soldier));
        assertThrows(IllegalStateException.class, () -> model.fortify(soldier));
        assertEquals(exhausted, snapshot(model));

        ((AbstractUnit) soldier).setActionPoints(1);
        model.setSentry(soldier);
        final String ordered = snapshot(model);
        assertFalse(model.canFortify(soldier));
        assertThrows(IllegalStateException.class, () -> model.fortify(soldier));
        assertEquals(ordered, snapshot(model));
    }

    @Test
    void wrongOwnerStaleAndStoppedOrdersLeaveTheWholeSaveUnchanged() {
        final Model model = load(scenario());
        final Unit soldier = model.getUnitById(16);
        soldier.placeToLocation(SHORE);
        final Unit stale = load(scenario()).getUnitById(16);
        final String before = snapshot(model);
        assertFalse(model.canFortify(stale));
        assertThrows(IllegalStateException.class, () -> model.fortify(stale));
        assertEquals(before, snapshot(model));

        model.endTurn();
        final String foreignTurn = snapshot(model);
        assertFalse(model.canFortify(soldier));
        assertThrows(IllegalStateException.class, () -> model.fortify(soldier));
        assertEquals(foreignTurn, snapshot(model));

        final Model stopped = load(scenario());
        final Unit waiting = stopped.getUnitById(16);
        waiting.placeToLocation(SHORE);
        stopped.fortify(waiting);
        stopped.stop();
        final String stoppedState = snapshot(stopped);
        assertFalse(stopped.canCancelFortify(waiting));
        assertThrows(IllegalStateException.class,
                () -> stopped.cancelFortify(waiting));
        assertFalse(stopped.canFortify(waiting));
        assertThrows(IllegalStateException.class,
                () -> stopped.fortify(waiting));
        assertEquals(stoppedState, snapshot(stopped));
    }

    @Test
    void effectiveOrderBlocksAnOtherwiseLegalAttackUntilCanceled() {
        final GameModelPo data = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.microcol");
        data.getModel().getGameManager().setGameStarted(true);
        data.getModel().getGameManager().setCurrentPlayer("Dutch");
        final Model model = load(data);
        final UnitFreeColonist soldier = (UnitFreeColonist) model
                .getUnitById(0);
        soldier.placeToLocation(Location.of(18, 10));
        soldier.setHoldingGuns(true);
        soldier.setActionPoints(1);
        final Unit rival = model.getUnitById(6);
        rival.placeToLocation(Location.of(17, 11));
        soldier.getOwner().revealMapForUnit(soldier);
        model.declareHostilities(soldier.getOwner(), rival.getOwner());
        assertTrue(((AbstractUnit) soldier)
                .prepareAttack(rival.getLocation(), true).isPresent());
        model.fortify(soldier);
        model.endTurn();
        for (int turns = 0; turns < model.getPlayers().size()
                && model.getCurrentPlayer() != soldier.getOwner(); turns++) {
            model.endTurn();
        }
        assertSame(soldier.getOwner(), model.getCurrentPlayer());
        assertTrue(model.isFortified(soldier));
        final String before = snapshot(model);
        assertTrue(((AbstractUnit) soldier)
                .prepareAttack(rival.getLocation(), true).isEmpty());
        assertEquals(before, snapshot(model));
        model.cancelFortify(soldier);
        assertTrue(((AbstractUnit) soldier)
                .prepareAttack(rival.getLocation(), true).isPresent());
    }

    private GameModelPo saved(final Model model) {
        final GameModelPo data = scenario();
        data.setModel(model.save());
        return data;
    }

    private String snapshot(final Model model) {
        return new Gson().toJson(model.save());
    }
}
