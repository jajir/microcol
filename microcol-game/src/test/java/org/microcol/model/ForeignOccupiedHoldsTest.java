package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.OptionalInt;

import org.junit.jupiter.api.Test;
import org.microcol.model.store.CargoSlotPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.Ship;
import org.microcol.model.unit.UnitTreasure;

import com.google.gson.Gson;

/**
 * Foreign occupied-hold count (R-0205): a viewer learns only how many holds of
 * a ship it sees now are occupied. Passengers and goods are never named, and
 * own, unseen, stale or foreign-model ships disclose nothing. Every world is
 * saved through the ordinary PO form and boarding goes through the model's
 * embark operation, so the loader's place and hold invariants hold for each
 * saved state.
 */
class ForeignOccupiedHoldsTest {

    private static final String DUTCH = "Dutch";

    private static final String FRENCH = "France";

    /** Foreign galleon at sea, owned by France. */
    private static final int GALLEON_ID = 7000;

    private static final int TREASURE_ID = 7001;

    private static final int COLONIST_ID = 7003;

    /**
     * Dutch colonist on the island, first unit of the island scene; its sight
     * covers the galleon.
     */
    private static final int VIEWER_ID = 5000;

    /** Ocean next to the island; the island ends one column before it. */
    private static final Location SEA = NativeTurnScene.at(5, 0);

    private static final Location LAND = NativeTurnScene.at(4, 0);

    /** Far from the viewer's sight and from the galleon. */
    private static final Location FAR = NativeTurnScene.at(-4, -4);

    private static final int HOLDS = 6;

    private final Gson gson = new Gson();

    @Test
    void emptyVisibleShipDisclosesZeroOccupiedHolds() {
        final Model model = NativeTurnScene.make(world());

        assertEquals(OptionalInt.of(0),
                dutch(model).getObservedOccupiedHolds(ship(model)));
    }

    @Test
    void partialHoldsCountGoodsAndPassengersOncePerHold() {
        final ModelPo po = world();
        withGoods(po, 1, GoodsType.ORE, 10);
        withGoods(po, 2, GoodsType.CIGARS, 1);
        final Model model = NativeTurnScene.make(po);
        // The colonist boards the first free hold through the model.
        colonist(model)
                .embarkFromLocation(ship(model).getCargo().getSlotByIndex(0));

        assertEquals(OptionalInt.of(3),
                dutch(model).getObservedOccupiedHolds(ship(model)));
    }

    @Test
    void multiHoldPassengerOccupiesEachOfItsHoldsAndCountsThemOnce() {
        final Model model = NativeTurnScene.make(world());
        treasure(model)
                .embarkFromLocation(ship(model).getCargo().getSlotByIndex(0));

        // One train reserves all six holds: each hold counts once.
        assertEquals(OptionalInt.of(HOLDS),
                dutch(model).getObservedOccupiedHolds(ship(model)));
    }

    @Test
    void fullShipDisclosesEveryHoldAsOccupied() {
        final ModelPo po = world();
        for (int hold = 0; hold < HOLDS; hold++) {
            withGoods(po, hold, GoodsType.CORN, 1);
        }
        final Model model = NativeTurnScene.make(po);

        assertEquals(OptionalInt.of(HOLDS),
                dutch(model).getObservedOccupiedHolds(ship(model)));
    }

    @Test
    void currentVisibilityDecidesNotExplorationOfTheTile() {
        final Model model = NativeTurnScene.make(world());
        final Player dutch = dutch(model);
        assertEquals(OptionalInt.of(0),
                dutch.getObservedOccupiedHolds(ship(model)));

        viewer(model).placeToLocation(FAR);

        assertTrue(dutch.isVisible(SEA));
        assertFalse(dutch.isCurrentlyVisible(SEA));
        assertEquals(OptionalInt.empty(),
                dutch.getObservedOccupiedHolds(ship(model)));
    }

    @Test
    void ownerSwitchHidesOwnShipAndDisclosesToTheNewForeignViewerOnly() {
        final ModelPo po = world();
        withGoods(po, 0, GoodsType.CORN, 5);
        final Model model = NativeTurnScene.make(po);
        final Ship ship = ship(model);

        ship.takeOver(dutch(model));
        // Own cargo uses the owner API, not the foreign count.
        assertEquals(OptionalInt.empty(),
                dutch(model).getObservedOccupiedHolds(ship));
        // France still sees the square from its colonist on the island.
        assertEquals(OptionalInt.of(1),
                france(model).getObservedOccupiedHolds(ship));

        ship.takeOver(france(model));
        assertEquals(OptionalInt.of(1),
                dutch(model).getObservedOccupiedHolds(ship));
        assertEquals(OptionalInt.empty(),
                france(model).getObservedOccupiedHolds(ship));
    }

    @Test
    void staleRemovedOrForeignModelIdentityDisclosesNothing() {
        final ModelPo po = world();
        withGoods(po, 0, GoodsType.CORN, 5);
        final Model model = NativeTurnScene.make(po);
        final Ship ship = ship(model);
        assertEquals(OptionalInt.of(1),
                dutch(model).getObservedOccupiedHolds(ship));

        // The saved form reloads through the ordinary loader and keeps the
        // same disclosure for the same ship.
        final Model restored = NativeTurnScene.make(model.save());
        assertEquals(OptionalInt.of(1),
                dutch(restored).getObservedOccupiedHolds(ship(restored)));
        // Identity from another model is not live here, in either direction.
        assertEquals(OptionalInt.empty(),
                dutch(model).getObservedOccupiedHolds(ship(restored)));
        assertEquals(OptionalInt.empty(),
                dutch(restored).getObservedOccupiedHolds(ship));

        model.destroyUnit(ship);
        assertEquals(OptionalInt.empty(),
                dutch(model).getObservedOccupiedHolds(ship));
    }

    @Test
    void destroyingTheCarrierReleasesItsPassengersAndTheSavedFormStillLoads() {
        final Model model = NativeTurnScene.make(world());
        treasure(model)
                .embarkFromLocation(ship(model).getCargo().getSlotByIndex(0));
        assertEquals(OptionalInt.of(HOLDS),
                dutch(model).getObservedOccupiedHolds(ship(model)));

        // Sinking the galleon ends its train too; nothing stays reserved.
        model.destroyUnit(ship(model));
        assertTrue(model.tryGetUnitById(TREASURE_ID).isEmpty());
        final Model restored = NativeTurnScene.make(model.save());
        assertTrue(restored.tryGetUnitById(GALLEON_ID).isEmpty());
        assertTrue(restored.tryGetUnitById(TREASURE_ID).isEmpty());
    }

    @Test
    void queryNeverChangesCargoExplorationOrMovement() {
        final ModelPo po = world();
        withGoods(po, 0, GoodsType.CORN, 30);
        final Model model = NativeTurnScene.make(po);
        final Ship ship = ship(model);
        final Player dutch = dutch(model);
        final String before = gson.toJson(model.save());
        final int movementBefore = ship.getMovementThirds();

        assertEquals(OptionalInt.of(1), dutch.getObservedOccupiedHolds(ship));
        assertEquals(OptionalInt.of(1), dutch.getObservedOccupiedHolds(ship));

        assertEquals(before, gson.toJson(model.save()));
        assertEquals(movementBefore, ship.getMovementThirds());
        assertEquals(SEA, ship.getLocation());
    }

    @Test
    void nullUnitIsRejectedAsAProgrammingError() {
        final Model model = NativeTurnScene.make(world());
        assertThrows(NullPointerException.class,
                () -> dutch(model).getObservedOccupiedHolds(null));
    }

    // ----- helpers -----

    /**
     * Island world with a French galleon at sea, a Dutch viewer on land, and a
     * French colonist and treasure train on land ready to board. The French
     * player is to move, so both passengers may board through the model.
     */
    private static ModelPo world() {
        final ModelPo po = NativeTurnScene.island()
                .rawUnit(GALLEON_ID, UnitType.GALLEON, FRENCH, null, null, SEA)
                .rawUnit(COLONIST_ID, UnitType.COLONIST, FRENCH, null, null,
                        LAND)
                .rawUnit(TREASURE_ID, UnitType.TREASURE_TRAIN, FRENCH, null,
                        null, LAND)
                .dutchColonist(4, 1).po();
        for (int hold = 0; hold < HOLDS; hold++) {
            unitPo(po, GALLEON_ID).getCargo().getSlots().add(new CargoSlotPo());
        }
        unitPo(po, COLONIST_ID).setMovementThirds(MovementPoints.ofPoints(1));
        final UnitPo train = unitPo(po, TREASURE_ID);
        train.setTreasureValue(500);
        train.setMovementThirds(MovementPoints.ofPoints(1));
        po.getGameManager().setGameStarted(true);
        po.getGameManager().setCurrentPlayer(FRENCH);
        return po;
    }

    private static void withGoods(final ModelPo po, final int hold,
            final GoodsType type, final int amount) {
        final CargoSlotPo slot = unitPo(po, GALLEON_ID).getCargo().getSlots()
                .get(hold);
        slot.setGoodsType(type);
        slot.setAmount(amount);
    }

    private static UnitPo unitPo(final ModelPo po, final int id) {
        return po.getUnits().stream().filter(unit -> unit.getId() == id)
                .findFirst().orElseThrow();
    }

    private static Ship ship(final Model model) {
        return (Ship) model.getUnitById(GALLEON_ID);
    }

    private static Unit colonist(final Model model) {
        return model.getUnitById(COLONIST_ID);
    }

    private static UnitTreasure treasure(final Model model) {
        return (UnitTreasure) model.getUnitById(TREASURE_ID);
    }

    private static Unit viewer(final Model model) {
        return model.getUnitById(VIEWER_ID);
    }

    private static Player dutch(final Model model) {
        return model.getPlayerByName(DUTCH);
    }

    private static Player france(final Model model) {
        return model.getPlayerByName(FRENCH);
    }
}
