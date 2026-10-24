package org.microcol.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;

import org.microcol.model.unit.UnitWithCargo;

import com.google.common.base.Preconditions;

/**
 * Fleet report (R-0202) of one colonial nation. It lists every owned ship with
 * its current place, voyage or repair, its passengers, goods and holds. After
 * that it lists the foreign ships that the nation sees right now, each with the
 * count of its occupied holds only, read through
 * {@link Player#getObservedOccupiedHolds(Unit)}. Own figures come from the same
 * owners as the rules: {@link LaborLocation} for places, {@link PlaceHighSea}
 * for voyages, {@link PlaceShipRepair} for repairs and {@link CargoSlot} for
 * holds. The report is a snapshot of the live state; nothing is kept between
 * calls and no ship, cargo, place or resource changes.
 */
public final class FleetReport {

    /** Where a ship on the high seas is sailing to. */
    public enum Destination {
        /** The ship sails to the European port. */
        EUROPE,
        /** The ship sails back to the colonial coast it left. */
        COLONIES
    }

    /** The voyage of an own ship on the high seas with its current figures. */
    public static final class Voyage {

        private final Destination destination;
        private final int remainingTurns;
        private final boolean reversed;
        private final boolean waiting;

        private Voyage(final Destination destination, final int remainingTurns,
                final boolean reversed, final boolean waiting) {
            this.destination = destination;
            this.remainingTurns = remainingTurns;
            this.reversed = reversed;
            this.waiting = waiting;
        }

        /** @return where the ship is sailing to */
        public Destination getDestination() {
            return destination;
        }

        /** @return turns left until the ship reaches its destination */
        public int getRemainingTurns() {
            return remainingTurns;
        }

        /** @return whether the voyage was turned around after its departure */
        public boolean isReversed() {
            return reversed;
        }

        /**
         * @return whether the ship has arrived at the colonial coast but waits
         *         for a free square there
         */
        public boolean isWaiting() {
            return waiting;
        }
    }

    /** The repair of an own ship, which takes it off the map. */
    public static final class Repair {

        private final PlaceShipRepair.Phase phase;
        private final int remainingOwnerUpdates;
        private final boolean europeDestination;
        private final Optional<String> colonyName;

        private Repair(final PlaceShipRepair.Phase phase,
                final int remainingOwnerUpdates,
                final boolean europeDestination,
                final Optional<String> colonyName) {
            this.phase = phase;
            this.remainingOwnerUpdates = remainingOwnerUpdates;
            this.europeDestination = europeDestination;
            this.colonyName = colonyName;
        }

        /** @return the transfer or the repair phase of the ship */
        public PlaceShipRepair.Phase getPhase() {
            return phase;
        }

        /** @return owner updates left until the ship is back at sea */
        public int getRemainingOwnerUpdates() {
            return remainingOwnerUpdates;
        }

        /** @return whether the repair yard is in Europe */
        public boolean isEuropeDestination() {
            return europeDestination;
        }

        /**
         * @return name of the colony that is the repair yard, empty for Europe
         *         or for a colony the nation no longer owns
         */
        public Optional<String> getColonyName() {
            return colonyName;
        }
    }

    /** One own ship with its place, voyage or repair, and its cargo. */
    public static final class OwnShip {

        private final int unitId;
        private final UnitType type;
        private final LaborLocation location;
        private final Optional<Voyage> voyage;
        private final Optional<Repair> repair;
        private final Map<UnitType, Integer> passengers;
        private final Map<GoodsType, Integer> goods;
        private final int occupiedHolds;
        private final int capacity;

        private OwnShip(final int unitId, final UnitType type,
                final LaborLocation location, final Optional<Voyage> voyage,
                final Optional<Repair> repair,
                final Map<UnitType, Integer> passengers,
                final Map<GoodsType, Integer> goods, final int occupiedHolds,
                final int capacity) {
            this.unitId = unitId;
            this.type = type;
            this.location = location;
            this.voyage = voyage;
            this.repair = repair;
            this.passengers = passengers;
            this.goods = goods;
            this.occupiedHolds = occupiedHolds;
            this.capacity = capacity;
        }

        /** @return stable unit ID of the ship */
        public int getUnitId() {
            return unitId;
        }

        /** @return type of the ship */
        public UnitType getType() {
            return type;
        }

        /** @return current place of the ship, never null */
        public LaborLocation getLocation() {
            return location;
        }

        /** @return the voyage, present only while the ship is at sea */
        public Optional<Voyage> getVoyage() {
            return voyage;
        }

        /** @return the repair, present only while the ship is under repair */
        public Optional<Repair> getRepair() {
            return repair;
        }

        /**
         * @return passengers aboard, each counted once by its first hold; a
         *         treasure train counts as one passenger
         */
        public int getPassengers() {
            return passengers.values().stream().mapToInt(Integer::intValue)
                    .sum();
        }

        /**
         * @return number of passengers of each unit type aboard, in the order
         *         of the holds; empty when no passenger is aboard
         */
        public Map<UnitType, Integer> getPassengersByType() {
            return passengers;
        }

        /** @return amount of each good aboard, in goods order; never zero */
        public Map<GoodsType, Integer> getGoods() {
            return goods;
        }

        /**
         * @return holds that hold a passenger, goods or a reservation of a
         *         multi-hold passenger
         */
        public int getOccupiedHolds() {
            return occupiedHolds;
        }

        /** @return holds that are not occupied */
        public int getFreeHolds() {
            return capacity - occupiedHolds;
        }

        /** @return number of holds of the ship */
        public int getCapacity() {
            return capacity;
        }
    }

    /**
     * A foreign ship in present sight. It carries only the count of its
     * occupied holds; neither its cargo nor its route is known to the viewer.
     */
    public static final class ForeignShip {

        private final int unitId;
        private final UnitType type;
        private final Location location;
        private final int occupiedHolds;

        private ForeignShip(final int unitId, final UnitType type,
                final Location location, final int occupiedHolds) {
            this.unitId = unitId;
            this.type = type;
            this.location = location;
            this.occupiedHolds = occupiedHolds;
        }

        /** @return stable unit ID of the ship, used only for identity */
        public int getUnitId() {
            return unitId;
        }

        /** @return type of the ship, which is visible to the viewer */
        public UnitType getType() {
            return type;
        }

        /** @return map tile where the ship stands now */
        public Location getLocation() {
            return location;
        }

        /** @return number of occupied holds, the only disclosed figure */
        public int getOccupiedHolds() {
            return occupiedHolds;
        }
    }

    private final List<OwnShip> ownShips;
    private final List<ForeignShip> foreignShips;

    private FleetReport(final List<OwnShip> ownShips,
            final List<ForeignShip> foreignShips) {
        this.ownShips = ownShips;
        this.foreignShips = foreignShips;
    }

    /**
     * Build the report of the current state of one colonial nation. Own ships
     * are the owner's ships that are not Crown-seized, as in the labor report.
     *
     * @param model required model that owns the owner
     * @param owner required colonial nation whose fleet is reported
     * @return immutable report of the current state
     */
    static FleetReport calculate(final Model model, final Player owner) {
        Preconditions.checkNotNull(owner, "Owner is required");
        Preconditions.checkArgument(owner.isColonialNation(),
                "Fleet report belongs to a colonial nation");
        final List<NativeSettlement> knownSites = model
                .getNativeSettlementsKnownTo(owner);
        final List<OwnShip> own = owner.getAllUnits().stream()
                .filter(unit -> unit instanceof UnitWithCargo && unit.isShip())
                .filter(unit -> !unit.isCrownSeized())
                .map(unit -> ownShip(model, owner, (UnitWithCargo) unit,
                        knownSites))
                .sorted(Comparator.comparingInt(OwnShip::getUnitId))
                .collect(Collectors.toUnmodifiableList());
        final List<ForeignShip> foreign = model.getAllUnits().stream()
                .filter(unit -> unit.isShip() && unit.getOwner() != owner)
                .map(unit -> foreignShip(owner, unit)).flatMap(Optional::stream)
                .sorted(Comparator.comparingInt(ForeignShip::getUnitId))
                .collect(Collectors.toUnmodifiableList());
        return new FleetReport(own, foreign);
    }

    private static OwnShip ownShip(final Model model, final Player owner,
            final UnitWithCargo ship, final List<NativeSettlement> knownSites) {
        final Map<UnitType, Integer> passengers = new LinkedHashMap<>();
        final Map<GoodsType, Integer> amounts = new HashMap<>();
        int occupied = 0;
        for (final CargoSlot slot : ship.getCargo().getSlots()) {
            if (!slot.isEmpty()) {
                occupied++;
            }
            // Only the first hold of a passenger names it; its reserved holds
            // count as occupied above but add no passenger.
            slot.getUnit().ifPresent(passenger -> passengers
                    .merge(passenger.getType(), 1, Integer::sum));
            slot.getGoods().ifPresent(load -> amounts.merge(load.getType(),
                    load.getAmount(), Integer::sum));
        }
        final Map<GoodsType, Integer> goods = new LinkedHashMap<>();
        GoodsType.GOOD_TYPES.stream().filter(amounts::containsKey)
                .forEach(type -> goods.put(type, amounts.get(type)));
        return new OwnShip(ship.getId(), ship.getType(),
                LaborLocation.of(ship, knownSites), voyageOf(ship),
                repairOf(model, owner, ship),
                Collections.unmodifiableMap(passengers),
                Collections.unmodifiableMap(goods), occupied,
                ship.getCargo().getSlots().size());
    }

    private static Optional<Voyage> voyageOf(final Unit ship) {
        if (!ship.isAtHighSea()) {
            return Optional.empty();
        }
        final PlaceHighSea sea = (PlaceHighSea) ship.getPlace();
        final int remaining = sea.getRemainigTurns();
        final Destination destination = sea.isTravelToEurope()
                ? Destination.EUROPE
                : Destination.COLONIES;
        return Optional.of(new Voyage(destination, remaining,
                sea.isTravelToEurope() != sea.isOriginalToEurope(),
                remaining == 0 && destination == Destination.COLONIES));
    }

    private static Optional<Repair> repairOf(final Model model,
            final Player owner, final Unit ship) {
        if (!(ship.getPlace() instanceof PlaceShipRepair)) {
            return Optional.empty();
        }
        final PlaceShipRepair repair = (PlaceShipRepair) ship.getPlace();
        final Integer colonyId = repair.getDestinationColonyId();
        final Optional<String> colonyName = colonyId == null ? Optional.empty()
                : model.getColonyById(colonyId)
                        .filter(colony -> colony.getOwner() == owner)
                        .map(Colony::getName);
        return Optional.of(
                new Repair(repair.getPhase(), repair.getRemainingOwnerUpdates(),
                        repair.isEuropeDestination(), colonyName));
    }

    /**
     * Only the holds that the owner's visibility allows are read: the count is
     * empty for a ship that the viewer cannot see now.
     */
    private static Optional<ForeignShip> foreignShip(final Player owner,
            final Unit unit) {
        final OptionalInt holds = owner.getObservedOccupiedHolds(unit);
        if (holds.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new ForeignShip(unit.getId(), unit.getType(),
                unit.getLocation(), holds.getAsInt()));
    }

    /** @return own ships ordered by stable unit ID */
    public List<OwnShip> getOwnShips() {
        return ownShips;
    }

    /**
     * @return foreign ships in present sight ordered by stable unit ID; an
     *         unseen or unknown ship is absent
     */
    public List<ForeignShip> getForeignShips() {
        return foreignShips;
    }

    /**
     * @param unitId stable unit ID
     * @return the own ship with the ID, empty when it is not in this report
     */
    public Optional<OwnShip> findOwnShip(final int unitId) {
        return ownShips.stream().filter(ship -> ship.getUnitId() == unitId)
                .findFirst();
    }

    /**
     * @param unitId stable unit ID
     * @return the foreign ship in present sight with the ID, empty when it is
     *         not visible now
     */
    public Optional<ForeignShip> findForeignShip(final int unitId) {
        return foreignShips.stream().filter(ship -> ship.getUnitId() == unitId)
                .findFirst();
    }
}
