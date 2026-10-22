package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.microcol.model.store.CargoSlotPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlaceCargoSlotPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.UnitTreasure;
import org.microcol.model.unit.UnitWithCargo;

import com.google.gson.Gson;

/**
 * Six-hold reservation of a treasure train in a galleon (R-0134). A train
 * boards only with six free holds and then fills them all as one unit, so no
 * other cargo fits beside it. Unloading or sinking releases the six holds
 * exactly once, and a saved reservation is validated on load. A one-hold
 * passenger keeps its single hold.
 */
class TreasureHoldReservationTest {

    private static final String DUTCH = "Dutch";

    private static final String FRENCH = "France";

    private static final int GALLEON_ID = 7000;

    private static final int TREASURE_ID = 7001;

    private static final int OTHER_GALLEON_ID = 7002;

    private static final int COLONIST_ID = 7003;

    private static final Location SEA = NativeTurnScene.at(5, 0);

    private static final Location OTHER_SEA = NativeTurnScene.at(5, 1);

    private static final Location LAND = NativeTurnScene.at(4, 0);

    private static final int HOLDS = 6;

    // ----- boarding and refusal -----

    @Test
    void sixFreeHoldsTakeOneTrainInSixReservedHoldsAndNothingElseFits() {
        final Model model = NativeTurnScene.make(withTreasure());
        final UnitWithCargo galleon = galleon(model);
        final UnitTreasure train = treasure(model);
        final List<CargoSlot> holds = galleon.getCargo().getSlots();
        assertEquals(HOLDS, holds.size());
        assertTrue(holds.stream().allMatch(CargoSlot::isEmpty));

        train.embarkFromLocation(holds.get(0));

        assertEquals(1, holds.stream().filter(CargoSlot::isLoadedUnit).count());
        assertEquals(HOLDS - 1,
                holds.stream().filter(CargoSlot::isReservedHold).count());
        // One unit, never copied: only the first hold offers it.
        assertEquals(1, holds.stream()
                .filter(hold -> hold.getUnit().isPresent()).count());
        assertEquals(train, holds.get(0).getUnit().orElseThrow());
        assertTrue(holds.stream()
                .allMatch(hold -> hold.getAvailableCapacity() == 0));
        assertTrue(holds.stream().noneMatch(CargoSlot::isLoadedGood));
        // No goods and no second train fit beside it.
        assertThrows(IllegalArgumentException.class,
                () -> holds.get(3).addGoods(Goods.of(GoodsType.ORE, 1)));
        assertFalse(train.canEmbark(holds.get(1)));
    }

    @Test
    void fiveFreeHoldsRefuseTheTrainAtEveryHoldWithoutAnyChange() {
        final Model model = NativeTurnScene.make(withOreInFirstHold());
        final UnitWithCargo galleon = galleon(model);
        final UnitTreasure train = treasure(model);
        final String before = snapshot(model);

        // Repeated attempts are each refused with the same unchanged state.
        for (int attempt = 0; attempt < 3; attempt++) {
            for (final CargoSlot slot : galleon.getCargo().getSlots()) {
                assertFalse(train.canEmbark(slot));
            }
            assertThrows(IllegalStateException.class, () -> train
                    .embarkFromLocation(galleon.getCargo().getSlotByIndex(1)));
        }

        assertEquals(before, snapshot(model));
        assertTrue(train.isAtPlaceLocation());
        assertEquals(LAND, train.getLocation());
    }

    @Test
    void anotherOwnersTrainIsNotBoarded() {
        final ModelPo po = withTreasure();
        treasurePo(po).setOwnerId(FRENCH);
        final Model model = NativeTurnScene.make(po);
        final UnitTreasure train = treasure(model);
        final CargoSlot slot = galleon(model).getCargo().getSlotByIndex(0);
        final String before = snapshot(model);

        assertFalse(train.canEmbark(slot));
        assertThrows(IllegalStateException.class,
                () -> train.embarkFromLocation(slot));

        assertEquals(before, snapshot(model));
    }

    @Test
    void alreadyBoardedTrainIsNotBoardedAgain() {
        final Model model = NativeTurnScene.make(boardedWorld());
        final UnitWithCargo galleon = galleon(model);
        final UnitTreasure train = treasure(model);
        final String before = snapshot(model);

        assertFalse(train.canEmbark(galleon.getCargo().getSlotByIndex(0)));
        assertThrows(IllegalStateException.class, () -> train
                .embarkFromLocation(galleon.getCargo().getSlotByIndex(0)));

        assertEquals(before, snapshot(model));
    }

    @Test
    void ordinaryPassengerStillTakesOneHoldBesideFiveFreeHolds() {
        assertEquals(1, UnitType.COLONIST.getRequiredHolds());
        assertEquals(1, UnitType.ARTILLERY.getRequiredHolds());
        final ModelPo po = withOreInFirstHold();
        addColonist(po);
        final Model model = NativeTurnScene.make(po);
        final UnitWithCargo galleon = galleon(model);
        final Unit colonist = model.getUnitById(COLONIST_ID);

        colonist.embarkFromLocation(galleon.getCargo().getSlotByIndex(1));

        assertEquals(1, galleon.getCargo().getSlots().stream()
                .filter(CargoSlot::isLoadedUnit).count());
        assertEquals(0, galleon.getCargo().getSlots().stream()
                .filter(CargoSlot::isReservedHold).count());
        assertEquals(4, galleon.getCargo().getSlots().stream()
                .filter(CargoSlot::isEmpty).count());
    }

    // ----- release -----

    @Test
    void unloadingReleasesAllSixHoldsExactlyOnce() {
        final Model model = NativeTurnScene.make(boardedWorld());
        final UnitWithCargo galleon = galleon(model);
        final UnitTreasure train = treasure(model);
        assertTrue(galleon.getCargo().getSlots().stream()
                .noneMatch(CargoSlot::isEmpty));

        train.disembarkToLocation(LAND);

        assertTrue(train.isAtPlaceLocation());
        assertEquals(LAND, train.getLocation());
        assertTrue(galleon.getCargo().getSlots().stream()
                .allMatch(CargoSlot::isEmpty));
        // Nothing is left to release a second time.
        assertThrows(IllegalStateException.class,
                () -> galleon.getCargo().getSlotByIndex(0).empty());
    }

    @Test
    void destroyingTheBoardedTrainReleasesAllSixHoldsOnce() {
        final Model model = NativeTurnScene.make(boardedWorld());
        final UnitWithCargo galleon = galleon(model);
        final UnitTreasure train = treasure(model);

        train.getPlace().destroy();
        model.destroyUnit(train);

        assertTrue(galleon.getCargo().getSlots().stream()
                .allMatch(CargoSlot::isEmpty));
        assertTrue(model.tryGetUnitById(TREASURE_ID).isEmpty());
        assertThrows(IllegalStateException.class,
                () -> train.getPlace().destroy());
    }

    @Test
    void directDestructionOfTheBoardedTrainReleasesSixHoldsOnce() {
        final Model model = NativeTurnScene.make(boardedWorld());
        final UnitWithCargo galleon = galleon(model);
        final UnitTreasure train = treasure(model);

        model.destroyUnit(train);
        model.destroyUnit(train);

        assertTrue(galleon.getCargo().getSlots().stream()
                .allMatch(CargoSlot::isEmpty));
        assertTrue(model.tryGetUnitById(TREASURE_ID).isEmpty());
    }

    @Test
    void sinkingTheGalleonDestroysItsTrainOnceWithoutAPayout() {
        final Model model = NativeTurnScene.make(boardedWorld());
        final UnitWithCargo galleon = galleon(model);
        final int units = model.getAllUnits().size();

        model.destroyUnit(galleon);

        assertTrue(model.tryGetUnitById(GALLEON_ID).isEmpty());
        assertTrue(model.tryGetUnitById(TREASURE_ID).isEmpty());
        assertEquals(units - 2, model.getAllUnits().size());
    }

    // ----- save and continue -----

    @Test
    void savedTrainKeepsOneUnitAndItsFullReservationAfterContinue() {
        final Model model = NativeTurnScene.make(boardedWorld());

        final Model loaded = NativeTurnScene.make(model.save());

        final UnitWithCargo galleon = galleon(loaded);
        final UnitTreasure train = treasure(loaded);
        assertEquals(500, train.getValue());
        assertEquals(1, galleon.getCargo().getSlots().stream()
                .filter(CargoSlot::isLoadedUnit).count());
        assertEquals(HOLDS - 1, galleon.getCargo().getSlots().stream()
                .filter(CargoSlot::isReservedHold).count());
        assertEquals(snapshot(loaded),
                snapshot(NativeTurnScene.make(loaded.save())));
    }

    // ----- loader validation -----

    @Test
    void partialReservationIsRefusedOnLoad() {
        final ModelPo po = boardedWorld();
        galleonPo(po).getCargo().getSlots().get(HOLDS - 1).setUnitId(null);

        assertThrows(IllegalArgumentException.class,
                () -> NativeTurnScene.make(po));
    }

    @Test
    void reservationInTwoShipsIsRefusedOnLoad() {
        final ModelPo po = boardedWorld();
        addGalleon(po, OTHER_GALLEON_ID, OTHER_SEA);
        galleonPo(po, OTHER_GALLEON_ID).getCargo().getSlots().get(0)
                .setUnitId(TREASURE_ID);

        assertThrows(IllegalArgumentException.class,
                () -> NativeTurnScene.make(po));
    }

    @Test
    void holdNamingAnUnplacedUnitIsRefusedOnLoad() {
        final ModelPo po = boardedWorld();
        treasurePo(po).setPlaceCargoSlot(null);
        final PlaceMapPo place = new PlaceMapPo();
        place.setLocation(LAND);
        place.setOrientation(Direction.west);
        treasurePo(po).setPlaceMap(place);

        assertThrows(IllegalArgumentException.class,
                () -> NativeTurnScene.make(po));
    }

    @Test
    void holdMixingUnitAndGoodsIsRefusedOnLoad() {
        final ModelPo po = boardedWorld();
        final CargoSlotPo hold = galleonPo(po).getCargo().getSlots()
                .get(HOLDS - 1);
        hold.setGoodsType(GoodsType.ORE);
        hold.setAmount(3);

        assertThrows(IllegalArgumentException.class,
                () -> NativeTurnScene.make(po));
    }

    // ----- helpers -----

    /**
     * Free-play island with a galleon on the sea, Dutch to move, and no cargo.
     */
    private static ModelPo base() {
        final ModelPo po = NativeTurnScene.island()
                .rawUnit(GALLEON_ID, UnitType.GALLEON, DUTCH, null, null, SEA)
                .po();
        for (int hold = 0; hold < HOLDS; hold++) {
            galleonPo(po).getCargo().getSlots().add(new CargoSlotPo());
        }
        po.getGameManager().setGameStarted(true);
        po.getGameManager().setCurrentPlayer(DUTCH);
        return po;
    }

    /**
     * Base world with a Dutch treasure train on the land beside the galleon.
     */
    private static ModelPo withTreasure() {
        final ModelPo po = base();
        final UnitPo train = NativeTurnScene.island()
                .rawUnit(TREASURE_ID, UnitType.TREASURE_TRAIN, DUTCH, null,
                        null, LAND)
                .po().getUnits().stream()
                .filter(unit -> unit.getId() == TREASURE_ID).findFirst()
                .orElseThrow();
        train.setTreasureValue(500);
        train.setMovementThirds(MovementPoints.ofPoints(1));
        po.getUnits().add(train);
        return po;
    }

    /** Base world with the treasure, and one of the six holds carrying ore. */
    private static ModelPo withOreInFirstHold() {
        final ModelPo po = withTreasure();
        final CargoSlotPo hold = galleonPo(po).getCargo().getSlots().get(0);
        hold.setGoodsType(GoodsType.ORE);
        hold.setAmount(10);
        return po;
    }

    /** Base world with the treasure already stored in all six holds. */
    private static ModelPo boardedWorld() {
        final ModelPo po = withTreasure();
        final UnitPo train = treasurePo(po);
        train.setPlaceMap(null);
        train.setPlaceCargoSlot(new PlaceCargoSlotPo());
        for (final CargoSlotPo hold : galleonPo(po).getCargo().getSlots()) {
            hold.setUnitId(TREASURE_ID);
        }
        return po;
    }

    private static void addColonist(final ModelPo po) {
        final UnitPo colonist = NativeTurnScene.island()
                .rawUnit(COLONIST_ID, UnitType.COLONIST, DUTCH, null, null,
                        LAND)
                .po().getUnits().stream()
                .filter(unit -> unit.getId() == COLONIST_ID).findFirst()
                .orElseThrow();
        colonist.setMovementThirds(MovementPoints.ofPoints(1));
        po.getUnits().add(colonist);
    }

    private static void addGalleon(final ModelPo po, final int id,
            final Location at) {
        final UnitPo galleon = NativeTurnScene.island()
                .rawUnit(id, UnitType.GALLEON, DUTCH, null, null, at).po()
                .getUnits().stream().filter(unit -> unit.getId() == id)
                .findFirst().orElseThrow();
        for (int hold = 0; hold < HOLDS; hold++) {
            galleon.getCargo().getSlots().add(new CargoSlotPo());
        }
        po.getUnits().add(galleon);
    }

    private static UnitPo galleonPo(final ModelPo po) {
        return galleonPo(po, GALLEON_ID);
    }

    private static UnitPo galleonPo(final ModelPo po, final int id) {
        return po.getUnits().stream().filter(unit -> unit.getId() == id)
                .findFirst().orElseThrow();
    }

    private static UnitPo treasurePo(final ModelPo po) {
        return po.getUnits().stream()
                .filter(unit -> unit.getId() == TREASURE_ID).findFirst()
                .orElseThrow();
    }

    private static UnitWithCargo galleon(final Model model) {
        return (UnitWithCargo) model.getUnitById(GALLEON_ID);
    }

    private static UnitTreasure treasure(final Model model) {
        return (UnitTreasure) model.getUnitById(TREASURE_ID);
    }

    private static String snapshot(final Model model) {
        return new Gson().toJson(model.save());
    }

}
