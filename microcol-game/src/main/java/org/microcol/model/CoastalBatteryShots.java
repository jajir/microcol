package org.microcol.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.microcol.model.store.CoastalBatteryShotPo;
import org.microcol.model.unit.Ship;

import com.google.common.base.Preconditions;

/**
 * Coastal battery shots taken in the colony owner's round (R-0177). One record
 * per colony, ship and colony owner stops a second shot from that battery at
 * that ship, so a second adjacent step, a Continue or a reopened screen cannot
 * repeat it. The record expires only when that same owner starts its own turn
 * preparation; turns of other players and loading leave it in place. A captured
 * colony has its own record under its new owner.
 */
final class CoastalBatteryShots {

    private final Set<Shot> shots;

    private CoastalBatteryShots(final Set<Shot> shots) {
        this.shots = shots;
    }

    /**
     * Restore saved records. An absent list means no shot was taken. Each entry
     * needs a colony ID already allocated, a ship ID, and the name of a
     * registered player who owns the battery; an entry appears only once. Ship
     * IDs are never reused, so a record of a since-destroyed ship is history
     * that cannot match a new ship.
     *
     * @param saved        saved entries, nullable meaning none
     * @param nextColonyId the saved next colony ID, above every allocated one
     * @param playerNames  names of the registered players
     * @return validated records
     */
    static CoastalBatteryShots make(final List<CoastalBatteryShotPo> saved,
            final int nextColonyId, final Set<String> playerNames) {
        final Set<Shot> shots = new LinkedHashSet<>();
        for (final CoastalBatteryShotPo entry : saved == null
                ? List.<CoastalBatteryShotPo>of()
                : saved) {
            Preconditions.checkArgument(
                    entry != null && entry.getColonyId() != null
                            && entry.getShipId() != null
                            && entry.getOwnerName() != null,
                    "Unsupported save: invalid coastal battery shot");
            Preconditions.checkArgument(
                    entry.getColonyId() >= 0
                            && entry.getColonyId() < nextColonyId
                            && entry.getShipId() >= 0
                            && playerNames.contains(entry.getOwnerName()),
                    "Unsupported save: coastal battery shot refers to an unknown colony, ship or owner");
            Preconditions.checkArgument(
                    shots.add(new Shot(entry.getColonyId(), entry.getShipId(),
                            entry.getOwnerName())),
                    "Unsupported save: duplicate coastal battery shot");
        }
        return new CoastalBatteryShots(shots);
    }

    /**
     * @return saved entries in the order they were taken
     */
    List<CoastalBatteryShotPo> save() {
        final List<CoastalBatteryShotPo> out = new ArrayList<>();
        for (final Shot shot : shots) {
            final CoastalBatteryShotPo po = new CoastalBatteryShotPo();
            po.setColonyId(shot.colonyId);
            po.setShipId(shot.shipId);
            po.setOwnerName(shot.ownerName);
            out.add(po);
        }
        return out;
    }

    /**
     * @param colony required battery colony, with its current owner
     * @param ship   required ship that may be fired at
     * @return whether this battery already fired at this ship in its owner's
     *         current round
     */
    boolean contains(final Colony colony, final Ship ship) {
        return shots.contains(new Shot(colony, ship));
    }

    /**
     * @param colony required battery colony that has just fired
     * @param ship   required ship that was fired at
     */
    void record(final Colony colony, final Ship ship) {
        shots.add(new Shot(colony, ship));
    }

    /**
     * The owner has begun its own turn: only that owner's shots expire.
     *
     * @param ownerName required name of the colony owner
     */
    void renew(final String ownerName) {
        Preconditions.checkNotNull(ownerName);
        shots.removeIf(shot -> shot.ownerName.equals(ownerName));
    }

    private static final class Shot {

        private final int colonyId;

        private final int shipId;

        private final String ownerName;

        Shot(final Colony colony, final Ship ship) {
            this(colony.getId(), ship.getId(), colony.getOwner().getName());
        }

        Shot(final int colonyId, final int shipId, final String ownerName) {
            this.colonyId = colonyId;
            this.shipId = shipId;
            this.ownerName = ownerName;
        }

        @Override
        public boolean equals(final Object other) {
            if (!(other instanceof Shot)) {
                return false;
            }
            final Shot that = (Shot) other;
            return colonyId == that.colonyId && shipId == that.shipId
                    && ownerName.equals(that.ownerName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(colonyId, shipId, ownerName);
        }
    }

}
