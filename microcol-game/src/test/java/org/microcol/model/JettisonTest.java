package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.PlaceHighSeasPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.Ship;

/**
 * Model rules of the goods jettison on an Atlantic crossing and on a map sea
 * square: the chosen hold loses only the chosen amount, every refusal changes
 * nothing, and a saved game keeps the reduced goods without repeating the
 * command.
 */
class JettisonTest {
    @TempDir
    Path directory;
    private final GameModelPoDao files = new GameModelPoDao();
    private GameModelPo data;
    private Model model;

    /** Sea square of the loaded map that the map-sea cases use. */
    private static final Location SEA = Location.of(2, 15);

    private Ship ship() {
        return (Ship) model.getUnitById(3);
    }

    /**
     * Four-turn voyage toward Europe, the goods hold as in the reversal rules.
     */
    private void load() {
        prepareData();
        make();
    }

    private void make() {
        model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    /**
     * Same crossing, with Brewster and enough crosses that the second owner
     * update pauses for an earned immigrant choice.
     */
    private void loadPendingImmigration() {
        prepareData();
        final PlayerPo dutch = data.getModel().getGameManager().getPlayers()
                .stream().filter(player -> "Dutch".equals(player.getName()))
                .findFirst().orElseThrow();
        dutch.getCongress().setMembers(List.of(Founder.WILLIAM_BREWSTER));
        dutch.getCongress().setSelected(null);
        dutch.getRecruitment().setCrosses(230);
        make();
    }

    private void prepareData() {
        data = files.loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        final UnitPo ship = data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == 3).findFirst().orElseThrow();
        final PlaceHighSeasPo voyage = ship.getPlaceHighSeas();
        ship.setType(UnitType.MERCHANTMAN);
        ship.setAvailableMoves(5);
        ship.getCargo().getSlots().remove(4);
        data.getModel().getUnits().removeIf(unit -> unit.getId() == 13);
        final var goods = ship.getCargo().getSlots().get(3);
        goods.setUnitId(null);
        goods.setGoodsType(GoodsType.ORE);
        goods.setAmount(7);
        voyage.setWeather(1);
        voyage.setOriginalTurns(4);
        voyage.setRemainigTurns(4);
        voyage.setPosition(0);
        voyage.setOriginalToEurope(true);
        voyage.setTravelToEurope(true);
        final var terrain = data.getModel().getMap().getTerrainMap();
        terrain.put(Location.of(2, 15), TerrainType.HIGH_SEA);
        data.getModel().getMap().setTerrainType(terrain);
        ship.setDepartureLocation(Location.of(2, 15));
        ship.setDepartureDirection(Direction.west);
    }

    private void ownerUpdate() {
        model.endTurn();
        model.endTurn();
    }

    /** Moves the loaded ship onto the map sea square, as a plain placement. */
    private void placeOnMapSea() {
        ship().placeToLocation(SEA);
    }

    private void reload() {
        data.setModel(model.save());
        final Path save = directory.resolve("jettisoned.microcol");
        files.saveToFile(save.toString(), data);
        data = files.loadFromFile(save.toFile());
        model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    /** First hold that carries goods, in slot order. */
    private int goodsHold() {
        return ship().getCargo().getSlots().stream()
                .filter(CargoSlot::isLoadedGood).findFirst().orElseThrow()
                .getIndex();
    }

    /** Every hold as text: goods and passenger, in slot order. */
    private List<String> holds() {
        return holds(ship());
    }

    private List<String> holds(final Ship target) {
        return target.getCargo().getSlots().stream()
                .map(slot -> slot.getGoods().toString()
                        + slot.getUnit().map(Unit::getId).toString())
                .collect(Collectors.toList());
    }

    /**
     * Everything a rejected command must leave unchanged: holds, owner,
     * treasury, damage, voyage or square, and the identity of the place.
     */
    private List<Object> state(final Ship target) {
        final List<Object> out = new ArrayList<>(holds(target));
        out.add(target.getOwner());
        out.add(target.getOwner().getGold());
        out.add(target.isDamaged());
        out.add(System.identityHashCode(target.getPlace()));
        if (target.isAtHighSea()) {
            final PlaceHighSea voyage = (PlaceHighSea) target.getPlace();
            out.add(List.of(voyage.isTravelToEurope(), voyage.getPosition(),
                    voyage.getRemainigTurns(), voyage.getRevision()));
        } else if (target.isAtPlaceLocation()) {
            out.add(target.getLocation());
        } else {
            out.add(target.getPlace() == null ? "none"
                    : target.getPlace().getClass());
        }
        return out;
    }

    private int amount(final int hold) {
        return ship().getCargo().getSlotByIndex(hold).getGoods().orElseThrow()
                .getAmount();
    }

    /** Ship moves away after the offer; the old offer must be refused. */
    private void assertRefusedAfterMove(final Consumer<Ship> move) {
        load();
        final int hold = goodsHold();
        final JettisonOffer offer = model.offerJettison(ship(), hold);
        move.accept(ship());
        final List<Object> before = state(ship());
        assertEquals(JettisonProblem.NOT_AT_SEA,
                model.getJettisonProblem(ship()).orElseThrow());
        assertThrows(IllegalStateException.class,
                () -> model.offerJettison(ship(), hold));
        assertThrows(IllegalStateException.class,
                () -> model.jettison(offer, 1));
        assertEquals(before, state(ship()));
    }

    @Test
    void partialJettisonRemovesOnlyTheChosenGoodsAndKeepsEverythingElse() {
        load();
        final int hold = goodsHold();
        final int full = amount(hold);
        final List<String> before = holds();
        final int gold = ship().getOwner().getGold();
        final int remaining = ((PlaceHighSea) ship().getPlace())
                .getRemainigTurns();
        final JettisonOffer offer = model.offerJettison(ship(), hold);
        assertSame(ship(), offer.getShip());
        assertEquals(hold, offer.getSlotIndex());
        assertEquals(
                ship().getCargo().getSlotByIndex(hold).getGoods().orElseThrow(),
                offer.getGoods());
        model.jettison(offer, 3);
        assertEquals(full - 3, amount(hold));
        final List<String> after = holds();
        for (int index = 0; index < after.size(); index++) {
            if (index != hold) {
                assertEquals(before.get(index), after.get(index));
            }
        }
        assertEquals(gold, ship().getOwner().getGold());
        assertTrue(ship().isAtHighSea());
        assertEquals(remaining,
                ((PlaceHighSea) ship().getPlace()).getRemainigTurns());
    }

    @Test
    void fullJettisonEmptiesTheHoldAndRefusesAFurtherOffer() {
        load();
        final int hold = goodsHold();
        final int full = amount(hold);
        model.jettison(model.offerJettison(ship(), hold), full);
        assertTrue(ship().getCargo().getSlotByIndex(hold).getGoods().isEmpty());
        assertThrows(IllegalStateException.class,
                () -> model.offerJettison(ship(), hold));
    }

    @Test
    void zeroAndOverstockQuantitiesAreRejectedWithoutChange() {
        load();
        final int hold = goodsHold();
        final int full = amount(hold);
        final JettisonOffer offer = model.offerJettison(ship(), hold);
        assertThrows(IllegalArgumentException.class,
                () -> model.jettison(offer, 0));
        assertThrows(IllegalArgumentException.class,
                () -> model.jettison(offer, full + 1));
        assertEquals(full, amount(hold));
    }

    @Test
    void staleAndRepeatedConfirmationsRejectWithoutDiscardingTwice() {
        load();
        final int hold = goodsHold();
        final int full = amount(hold);
        final JettisonOffer stale = model.offerJettison(ship(), hold);
        model.jettison(model.offerJettison(ship(), hold), 2);
        assertThrows(IllegalStateException.class,
                () -> model.jettison(stale, 2));
        assertEquals(full - 2, amount(hold));
        final JettisonOffer current = model.offerJettison(ship(), hold);
        model.jettison(current, 1);
        assertThrows(IllegalStateException.class,
                () -> model.jettison(current, 1));
        assertEquals(full - 3, amount(hold));
    }

    @Test
    void passengerHoldCannotBeOfferedAndPassengerStaysAboard() {
        load();
        final int passenger = ship().getCargo().getSlots().stream()
                .filter(CargoSlot::isLoadedUnit).findFirst().orElseThrow()
                .getIndex();
        final List<String> before = holds();
        assertThrows(IllegalStateException.class,
                () -> model.offerJettison(ship(), passenger));
        assertEquals(before, holds());
    }

    @Test
    void reloadRestoresReducedGoodsWithoutRepeatingTheCommand() {
        load();
        final int hold = goodsHold();
        final int full = amount(hold);
        model.jettison(model.offerJettison(ship(), hold), 2);
        reload();
        assertEquals(full - 2, amount(goodsHold()));
        assertEquals(full - 2, amount(hold));
    }

    @Test
    void foreignOwnerIsRefusedOnMapSeaAndAtlanticWithoutChange() {
        for (final boolean onMap : new boolean[] { false, true }) {
            load();
            if (onMap) {
                placeOnMapSea();
            }
            final int hold = goodsHold();
            final JettisonOffer offer = model.offerJettison(ship(), hold);
            final Player king = model.getPlayers().stream()
                    .filter(player -> player != model.getCurrentPlayer())
                    .findFirst().orElseThrow();
            ship().takeOver(king);
            final List<Object> before = state(ship());
            assertEquals(JettisonProblem.NOT_OWNER,
                    model.getJettisonProblem(ship()).orElseThrow());
            assertThrows(IllegalStateException.class,
                    () -> model.offerJettison(ship(), hold));
            assertThrows(IllegalStateException.class,
                    () -> model.jettison(offer, 1));
            assertEquals(before, state(ship()));
        }
    }

    @Test
    void stoppedGameIsWrongPhaseOnMapSeaAndAtlanticWithoutChange() {
        for (final boolean onMap : new boolean[] { false, true }) {
            load();
            if (onMap) {
                placeOnMapSea();
            }
            final int hold = goodsHold();
            final JettisonOffer offer = model.offerJettison(ship(), hold);
            model.stop();
            assertEquals(JettisonProblem.WRONG_PHASE,
                    model.getJettisonProblem(ship()).orElseThrow());
            final List<Object> before = state(ship());
            assertThrows(IllegalStateException.class,
                    () -> model.offerJettison(ship(), hold));
            assertThrows(IllegalStateException.class,
                    () -> model.jettison(offer, 2));
            assertEquals(before, state(ship()));
        }
    }

    @Test
    void colonyDockIsRefusedAfterAnOfferMadeOnTheCrossing() {
        assertRefusedAfterMove(ship -> ship
                .placeToLocation(model.getColonies().get(0).getLocation()));
    }

    @Test
    void landNextToAColonyIsRefusedAfterAnOfferMadeOnTheCrossing() {
        assertRefusedAfterMove(ship -> ship.placeToLocation(model.getColonies()
                .get(0).getLocation().getNeighbors().stream()
                .filter(model::isValid)
                .filter(at -> model.getMap().getTerrainTypeAt(at).isLand()
                        && model.getColonyAt(at).isEmpty())
                .findFirst().orElseThrow()));
    }

    @Test
    void europePortIsRefusedAfterAnOfferMadeOnTheCrossing() {
        assertRefusedAfterMove(
                ship -> model.getEurope().getPort().placeShipToPort(ship));
    }

    @Test
    void nonShipUnitIsUnavailableAsCarrier() {
        load();
        final Unit colonist = model.getUnitById(16);
        assertEquals(model.getCurrentPlayer(), colonist.getOwner());
        assertEquals(JettisonProblem.UNAVAILABLE,
                model.getJettisonProblem(colonist).orElseThrow());
    }

    @Test
    void damagedShipOnMapSeaRefusesTheOfferMadeBeforeDamage() {
        load();
        placeOnMapSea();
        final int hold = goodsHold();
        final JettisonOffer offer = model.offerJettison(ship(), hold);
        model.damageShip(ship());
        assertTrue(ship().isDamaged());
        final List<Object> before = state(ship());
        assertEquals(JettisonProblem.NOT_AT_SEA,
                model.getJettisonProblem(ship()).orElseThrow());
        assertThrows(IllegalStateException.class,
                () -> model.offerJettison(ship(), hold));
        assertThrows(IllegalStateException.class,
                () -> model.jettison(offer, 1));
        assertEquals(before, state(ship()));
    }

    @Test
    void movementOnMapSeaInvalidatesTheOldOfferButNotANewOne() {
        load();
        placeOnMapSea();
        final int hold = goodsHold();
        final JettisonOffer old = model.offerJettison(ship(), hold);
        final Location next = SEA.getNeighbors().stream().filter(model::isValid)
                .filter(at -> model.getMap().getTerrainTypeAt(at).isSea()
                        && model.getColonyAt(at).isEmpty())
                .findFirst().orElseThrow();
        ship().placeToLocation(next);
        final List<Object> before = state(ship());
        assertThrows(IllegalStateException.class, () -> model.jettison(old, 1));
        assertEquals(before, state(ship()));
        final int full = amount(hold);
        model.jettison(model.offerJettison(ship(), hold), 2);
        assertEquals(full - 2, amount(hold));
    }

    @Test
    void voyageProgressBetweenOfferAndConfirmInvalidatesTheOffer() {
        load();
        final int hold = goodsHold();
        final JettisonOffer offer = model.offerJettison(ship(), hold);
        ownerUpdate();
        assertEquals(1, ((PlaceHighSea) ship().getPlace()).getPosition());
        final List<Object> before = state(ship());
        assertThrows(IllegalStateException.class,
                () -> model.jettison(offer, 1));
        assertEquals(before, state(ship()));
        model.jettison(model.offerJettison(ship(), hold), 1);
    }

    @Test
    void voyageReversalBetweenOfferAndConfirmInvalidatesTheOffer() {
        load();
        final JettisonOffer offer = model.offerJettison(ship(), goodsHold());
        model.reverseVoyage(model.offerVoyageReversal(ship()));
        final List<Object> before = state(ship());
        assertThrows(IllegalStateException.class,
                () -> model.jettison(offer, 1));
        assertEquals(before, state(ship()));
        model.jettison(model.offerJettison(ship(), goodsHold()), 1);
    }

    @Test
    void removedShipIsUnavailableAndDoesNotDiscardOnMapSeaAndAtlantic() {
        for (final boolean onMap : new boolean[] { false, true }) {
            load();
            if (onMap) {
                placeOnMapSea();
            }
            final Ship removed = ship();
            final JettisonOffer offer = model.offerJettison(removed,
                    goodsHold());
            model.destroyUnit(removed);
            assertEquals(JettisonProblem.UNAVAILABLE,
                    model.getJettisonProblem(removed).orElseThrow());
            final List<Object> before = state(removed);
            assertThrows(IllegalStateException.class,
                    () -> model.jettison(offer, 1));
            assertEquals(before, state(removed));
        }
    }

    @Test
    void invalidAndEmptyHoldsAreRefusedWithoutChangeOnMapSeaAndAtlantic() {
        for (final boolean onMap : new boolean[] { false, true }) {
            load();
            if (onMap) {
                placeOnMapSea();
            }
            final List<Object> beforeIndexes = state(ship());
            final int size = ship().getCargo().getSlots().size();
            assertThrows(IllegalArgumentException.class,
                    () -> model.offerJettison(ship(), -1));
            assertThrows(IllegalArgumentException.class,
                    () -> model.offerJettison(ship(), size));
            assertEquals(beforeIndexes, state(ship()));

            // All holds are occupied, so a legitimate full jettison empties
            // the goods hold; the empty hold is then refused, nothing more.
            final int empty = goodsHold();
            model.jettison(model.offerJettison(ship(), empty), amount(empty));
            assertTrue(ship().getCargo().getSlotByIndex(empty).isEmpty());
            final List<Object> beforeEmpty = state(ship());
            assertThrows(IllegalStateException.class,
                    () -> model.offerJettison(ship(), empty));
            assertEquals(beforeEmpty, state(ship()));
        }
    }

    @Test
    void pendingImmigrationChoiceIsWrongPhaseWithoutChange() {
        loadPendingImmigration();
        final int hold = goodsHold();
        final JettisonOffer offer = model.offerJettison(ship(), hold);
        ownerUpdate();
        assertTrue(model.isAwaitingImmigrationChoice());
        assertEquals(JettisonProblem.WRONG_PHASE,
                model.getJettisonProblem(ship()).orElseThrow());
        final List<Object> before = state(ship());
        assertThrows(IllegalStateException.class,
                () -> model.offerJettison(ship(), hold));
        assertThrows(IllegalStateException.class,
                () -> model.jettison(offer, 1));
        assertEquals(before, state(ship()));
    }
}
