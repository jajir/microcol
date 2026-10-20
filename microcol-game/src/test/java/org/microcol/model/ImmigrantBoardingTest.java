package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.CargoSlotPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlaceEuropePortPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.RecruitmentPo;
import org.microcol.model.store.UnitActionNoActionPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.Ship;
import org.microcol.model.unit.UnitActionType;

import com.google.gson.Gson;

/** Immigrant boarding willingness and front choice on the European pier. */
class ImmigrantBoardingTest {
    private static final Location SHORE = Location.of(48, 15);
    private static final int GALLEON_HOLDS = 6;

    private GameModelPo scenario() {
        return new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
    }

    private Model load(final GameModelPo data) {
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private RecruitmentPo savedDutchPool(final GameModelPo data) {
        return data.getModel().getGameManager().getPlayers().stream()
                .filter(player -> "Dutch".equals(player.getName())).findFirst()
                .orElseThrow().getRecruitment();
    }

    private UnitPo unitPo(final GameModelPo data, final int id) {
        return data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == id).findFirst().orElseThrow();
    }

    private PlaceMapPo at(final Location location) {
        final PlaceMapPo place = new PlaceMapPo();
        place.setLocation(location);
        place.setOrientation(Direction.west);
        return place;
    }

    private String snapshot(final Model model) {
        return new Gson().toJson(model.save());
    }

    private void advanceToNextDutchTurn(final Model model) {
        model.endTurn();
        model.endTurn();
        assertEquals("Dutch", model.getCurrentPlayer().getName());
    }

    /** Newest paid arrival; its willingness starts on the pier. */
    private Unit recruit(final Player buyer) {
        buyer.setGold(100_000);
        return buyer.recruit(buyer.getRecruitmentOffers().get(0));
    }

    /** Ordinary expert bought on the pier, which never becomes an immigrant. */
    private Unit buyExpert(final Player buyer) {
        buyer.setGold(100_000);
        buyer.buy(UnitType.MASTER_BLACKSMITH);
        return buyer.getAllUnits().stream()
                .max(Comparator.comparingInt(Unit::getId)).orElseThrow();
    }

    /**
     * Galleon waits in the European port with exactly the given number of empty
     * holds. Passengers already aboard are put ashore first.
     */
    private Model europeShipModel(final int freeHolds) {
        final GameModelPo data = scenario();
        final UnitPo shipPo = unitPo(data, 3);
        shipPo.setPlaceMap(null);
        shipPo.setPlaceHighSeas(null);
        shipPo.setDepartureLocation(null);
        shipPo.setDepartureDirection(null);
        shipPo.setPlaceEuropePort(new PlaceEuropePortPo());
        shipPo.setAction(new UnitActionNoActionPo());
        final Map<Location, TerrainType> terrain = data.getModel().getMap()
                .getTerrainMap();
        terrain.put(SHORE, TerrainType.GRASSLAND);
        data.getModel().getMap().setTerrainType(terrain);
        final List<CargoSlotPo> slots = shipPo.getCargo().getSlots();
        for (final CargoSlotPo slot : slots) {
            if (slot.getUnitId() != null) {
                final UnitPo passenger = unitPo(data, slot.getUnitId());
                passenger.setPlaceCargoSlot(null);
                passenger.setPlaceMap(at(SHORE));
            }
        }
        slots.clear();
        for (int i = 0; i < GALLEON_HOLDS; i++) {
            final CargoSlotPo slot = new CargoSlotPo();
            if (i < GALLEON_HOLDS - freeHolds) {
                slot.setGoodsType(GoodsType.ORE);
                slot.setAmount(1);
            }
            slots.add(slot);
        }
        return load(data);
    }

    /** Ids of the given units in the order the pier lists them. */
    private List<Integer> pierOrder(final Model model, final Player owner,
            final Unit... units) {
        final List<Integer> ids = Arrays.stream(units).map(Unit::getId)
                .collect(Collectors.toList());
        return model.getEurope().getPier().getUnits(owner).stream()
                .map(Unit::getId).filter(ids::contains)
                .collect(Collectors.toList());
    }

    @Test
    void paidArrivalIsWillingWhilePurchaseAndEarnedStartOnlyForImmigration() {
        final Model model = load(scenario());
        final Player buyer = model.getPlayerByName("Dutch");
        final Unit paid = recruit(buyer);
        assertTrue(paid.isImmigrant());
        assertTrue(paid.isWillingToBoard());
        assertTrue(paid.isAtEuropePier());
        final Unit purchased = buyExpert(buyer);
        assertFalse(purchased.isImmigrant());
        assertFalse(purchased.isWillingToBoard());

        final GameModelPo earnedData = scenario();
        savedDutchPool(earnedData).setCrosses(100);
        final Model earned = load(earnedData);
        advanceToNextDutchTurn(earned);
        final Unit arrival = earned.getPlayerByName("Dutch").getAllUnits()
                .stream().filter(Unit::isImmigrant).findFirst().orElseThrow();
        assertTrue(arrival.isAtEuropePier());
        assertTrue(arrival.isWillingToBoard());
    }

    @Test
    void optOutAndRestoreKeepThePersonAshoreAndRejectStaleRequests() {
        final Model model = load(scenario());
        final Player buyer = model.getPlayerByName("Dutch");
        final Unit immigrant = recruit(buyer);
        model.optOutOfBoarding(immigrant);
        assertFalse(immigrant.isWillingToBoard());
        assertTrue(immigrant.isBoardingOptedOut());
        assertTrue(immigrant.isAtEuropePier());
        assertFalse(model.canOptOutOfBoarding(immigrant));
        final String optedOut = snapshot(model);
        assertThrows(IllegalStateException.class,
                () -> model.optOutOfBoarding(immigrant));
        assertEquals(optedOut, snapshot(model));
        model.restoreBoarding(immigrant);
        assertTrue(immigrant.isWillingToBoard());
        assertFalse(model.canRestoreBoarding(immigrant));

        final Unit ordinary = buyExpert(buyer);
        assertFalse(model.canOptOutOfBoarding(ordinary));
        assertFalse(model.canPrioritizeBoarding(ordinary));
        assertThrows(IllegalStateException.class,
                () -> model.optOutOfBoarding(ordinary));

        model.stop();
        assertFalse(model.canRestoreBoarding(immigrant));
        assertThrows(IllegalStateException.class,
                () -> model.prioritizeBoarding(immigrant));
    }

    @Test
    void frontChoiceReplacesEarlierChoiceAndLeadsThePierOrder() {
        final Model model = load(scenario());
        final Player buyer = model.getPlayerByName("Dutch");
        final Unit first = recruit(buyer);
        final Unit second = recruit(buyer);
        assertEquals(List.of(first.getId(), second.getId()),
                pierOrder(model, buyer, first, second));

        model.prioritizeBoarding(second);
        assertTrue(second.isBoardingPriority());
        assertFalse(model.canPrioritizeBoarding(second));
        assertEquals(List.of(second.getId(), first.getId()),
                pierOrder(model, buyer, first, second));

        model.prioritizeBoarding(first);
        assertFalse(second.isBoardingPriority());
        assertTrue(first.isBoardingPriority());
        assertEquals(List.of(first.getId(), second.getId()),
                pierOrder(model, buyer, first, second));

        model.optOutOfBoarding(first);
        assertFalse(first.isBoardingPriority());
        assertEquals(List.of(first.getId(), second.getId()),
                pierOrder(model, buyer, first, second));
    }

    @Test
    void departureBoardsFrontThenStableIdsOnlyWithinFreeHolds() {
        final Model model = europeShipModel(2);
        final Player buyer = model.getPlayerByName("Dutch");
        final Unit first = recruit(buyer);
        final Unit second = recruit(buyer);
        final Unit expert = buyExpert(buyer);
        model.setSentry(expert);
        model.prioritizeBoarding(second);
        final Ship ship = (Ship) model.getUnitById(3);
        final int moves = second.getActionPoints();
        ship.placeToHighSeas(false);
        assertTrue(second.isAtCargoSlot());
        assertTrue(first.isAtCargoSlot());
        assertTrue(expert.isAtEuropePier());
        assertEquals(UnitActionType.sentry, expert.getAction().getType());
        assertEquals(moves, second.getActionPoints());
        assertFalse(second.isImmigrant());
        assertFalse(first.isImmigrant());
        assertFalse(second.isBoardingPriority());
    }

    @Test
    void zeroFreeHoldsKeepEveryoneAshoreAndKeepsTheFrontChoice() {
        final Model model = europeShipModel(0);
        final Player buyer = model.getPlayerByName("Dutch");
        final Unit first = recruit(buyer);
        final Unit second = recruit(buyer);
        model.prioritizeBoarding(second);
        model.getUnitById(3).placeToHighSeas(false);
        assertTrue(first.isAtEuropePier());
        assertTrue(second.isAtEuropePier());
        assertTrue(second.isBoardingPriority());
    }

    @Test
    void oneFreeHoldAdmitsOnlyTheFrontChoice() {
        final Model model = europeShipModel(1);
        final Player buyer = model.getPlayerByName("Dutch");
        final Unit first = recruit(buyer);
        final Unit second = recruit(buyer);
        model.prioritizeBoarding(second);
        model.getUnitById(3).placeToHighSeas(false);
        assertTrue(second.isAtCargoSlot());
        assertTrue(first.isAtEuropePier());
        assertTrue(first.isWillingToBoard());
    }

    @Test
    void optedOutImmigrantDoesNotBoardThroughSentryOrder() {
        final Model model = europeShipModel(3);
        final Player buyer = model.getPlayerByName("Dutch");
        final Unit first = recruit(buyer);
        final Unit second = recruit(buyer);
        final Unit expert = buyExpert(buyer);
        model.optOutOfBoarding(first);
        model.setSentry(first);
        model.setSentry(expert);
        model.getUnitById(3).placeToHighSeas(false);
        assertTrue(first.isAtEuropePier());
        assertEquals(UnitActionType.sentry, first.getAction().getType());
        assertTrue(second.isAtCargoSlot());
        assertTrue(expert.isAtCargoSlot());
    }

    @Test
    void saveAndContinueKeepsChoicesAndNeverBoardsOnLoad() {
        final Model model = europeShipModel(3);
        final Player buyer = model.getPlayerByName("Dutch");
        final Unit first = recruit(buyer);
        final Unit second = recruit(buyer);
        model.optOutOfBoarding(first);
        model.prioritizeBoarding(second);
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Unit reloadedFirst = restored.getUnitById(first.getId());
        final Unit reloadedSecond = restored.getUnitById(second.getId());
        assertTrue(reloadedFirst.isBoardingOptedOut());
        assertFalse(reloadedFirst.isWillingToBoard());
        assertTrue(reloadedFirst.isAtEuropePier());
        assertTrue(reloadedSecond.isBoardingPriority());
        assertTrue(reloadedSecond.isAtEuropePier());
        assertFalse(reloadedSecond.isAtCargoSlot());
    }

    @Test
    void duplicateSavedFrontChoiceIsRejectedOnLoad() {
        final Model model = europeShipModel(3);
        final Player buyer = model.getPlayerByName("Dutch");
        final Unit first = recruit(buyer);
        final Unit second = recruit(buyer);
        final ModelPo saved = model.save();
        saved.getUnits().stream().filter(unit -> unit.getId() == first.getId())
                .findFirst().orElseThrow().setBoardingPriority(true);
        saved.getUnits().stream().filter(unit -> unit.getId() == second.getId())
                .findFirst().orElseThrow().setBoardingPriority(true);
        assertThrows(IllegalArgumentException.class, () -> Model.make(saved,
                MissionImpl.getPredefinedGameOverEvaluators()));
    }
}
