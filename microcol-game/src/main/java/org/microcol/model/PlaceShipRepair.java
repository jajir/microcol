package org.microcol.model;

import org.microcol.model.store.PlacePo;
import org.microcol.model.store.PlaceShipRepairPo;
import org.microcol.model.store.UnitPo;

import com.google.common.base.Preconditions;

/** Off-map transfer and repair; its presence is the ship's damage state. */
public final class PlaceShipRepair extends AbstractPlace {

    public enum Phase {
        TRANSFER, REPAIR
    }

    private final Location origin;
    private final boolean originWasPort;
    private boolean europeDestination;
    private Integer destinationColonyId;
    private Phase phase;
    private int remainingPhaseUpdates;
    private int repairDuration;

    PlaceShipRepair(final Unit ship, final Location origin,
            final ShipRepairDestination destination) {
        this(ship, origin,
                !((AbstractUnit) ship).model.getMap().getTerrainTypeAt(origin)
                        .isSea(),
                destination.isEurope(), destination.getColonyId(),
                Phase.TRANSFER, 1, destination.getRepairDuration());
    }

    PlaceShipRepair(final Unit ship, final Location origin,
            final boolean originWasPort, final boolean europeDestination,
            final Integer colonyId, final Phase phase, final int remaining,
            final int duration) {
        super(ship);
        Preconditions.checkArgument(ship.isShip(), "Only ships can repair");
        final WorldMap map = ((AbstractUnit) ship).model.getMap();
        Preconditions.checkArgument(
                origin != null && map.isValid(origin) && (originWasPort
                        ? !map.getTerrainTypeAt(origin).isSea() && origin
                                .getNeighbors().stream().filter(map::isValid)
                                .anyMatch(next -> map.getTerrainTypeAt(next)
                                        .isSea())
                        : map.getTerrainTypeAt(origin).isSea()),
                "Repair origin must match its saved sea or port kind");
        Preconditions.checkArgument(
                europeDestination == (colonyId == null)
                        && (colonyId == null || colonyId >= 0),
                "Invalid repair destination");
        Preconditions.checkArgument(duration == 2 || duration == 3,
                "Invalid repair duration");
        Preconditions.checkArgument(!europeDestination || duration == 2,
                "Europe repair takes two updates");
        Preconditions.checkNotNull(phase, "Repair phase is missing");
        Preconditions
                .checkArgument(
                        remaining >= 1
                                && (phase == Phase.TRANSFER ? remaining == 1
                                        : remaining <= duration),
                        "Invalid repair progress");
        this.origin = origin;
        this.originWasPort = originWasPort;
        this.europeDestination = europeDestination;
        this.destinationColonyId = colonyId;
        this.phase = phase;
        this.remainingPhaseUpdates = remaining;
        this.repairDuration = duration;
    }

    public Location getOrigin() {
        return origin;
    }

    public boolean wasOriginPort() {
        return originWasPort;
    }

    public boolean isEuropeDestination() {
        return europeDestination;
    }

    public Integer getDestinationColonyId() {
        return destinationColonyId;
    }

    public Phase getPhase() {
        return phase;
    }

    public int getRemainingPhaseUpdates() {
        return remainingPhaseUpdates;
    }

    public int getRepairDuration() {
        return repairDuration;
    }

    public int getRemainingOwnerUpdates() {
        return remainingPhaseUpdates
                + (phase == Phase.TRANSFER ? repairDuration : 0);
    }

    boolean matches(final ShipRepairDestination destination) {
        return europeDestination == destination.isEurope() && (europeDestination
                || destinationColonyId.equals(destination.getColonyId()));
    }

    void reroute(final ShipRepairDestination destination) {
        europeDestination = destination.isEurope();
        destinationColonyId = destination.getColonyId();
        phase = Phase.TRANSFER;
        remainingPhaseUpdates = 1;
        repairDuration = destination.getRepairDuration();
    }

    /** @return true only when repair has completed on this update */
    boolean advance() {
        if (phase == Phase.TRANSFER) {
            phase = Phase.REPAIR;
            remainingPhaseUpdates = repairDuration;
            return false;
        }
        remainingPhaseUpdates--;
        return remainingPhaseUpdates == 0;
    }

    @Override
    public String getName() {
        return "Ship repair";
    }

    @Override
    public PlacePo save(final UnitPo unitPo) {
        final PlaceShipRepairPo out = new PlaceShipRepairPo();
        out.setOrigin(origin);
        out.setOriginWasPort(originWasPort);
        out.setEuropeDestination(europeDestination);
        out.setDestinationColonyId(destinationColonyId);
        out.setPhase(phase.name());
        out.setRemainingPhaseUpdates(remainingPhaseUpdates);
        out.setRepairDuration(repairDuration);
        unitPo.setPlaceShipRepair(out);
        return out;
    }
}
