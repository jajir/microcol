package org.microcol.model.turnevent;

import java.util.List;

import org.microcol.model.Goods;
import org.microcol.model.UnitType;
import org.microcol.model.store.TurnEventPo;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/** Persisted notice for a ship repair transition or loss. */
public final class TurnEventShipRepair extends AbstractTurnEvent {
    public enum Kind {
        ARRIVED, COMPLETED, REROUTED, SUNK
    }

    private final Kind kind;
    private final int shipId;
    private final UnitType shipType;
    private final String destinationName;
    private final boolean europeDestination;
    private final int lostPassengers;
    private final List<Goods> lostGoods;

    public TurnEventShipRepair(final String playerName, final Kind kind,
            final int shipId, final UnitType shipType,
            final String destinationName, final boolean europeDestination,
            final int lostPassengers, final List<Goods> lostGoods) {
        super(playerName);
        this.kind = Preconditions.checkNotNull(kind);
        Preconditions.checkArgument(
                shipId >= 0 && shipType != null && shipType.isShip()
                        && destinationName != null && !destinationName.isBlank()
                        && lostPassengers >= 0 && lostGoods != null
                        && lostGoods.stream()
                                .allMatch(java.util.Objects::nonNull),
                "Invalid repair event");
        this.shipId = shipId;
        this.shipType = shipType;
        this.destinationName = destinationName;
        this.europeDestination = europeDestination;
        this.lostPassengers = lostPassengers;
        this.lostGoods = ImmutableList.copyOf(lostGoods);
    }

    public Kind getKind() {
        return kind;
    }

    public int getShipId() {
        return shipId;
    }

    public UnitType getShipType() {
        return shipType;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public boolean isEuropeDestination() {
        return europeDestination;
    }

    public int getLostPassengers() {
        return lostPassengers;
    }

    public List<Goods> getLostGoods() {
        return lostGoods;
    }

    @Override
    public TurnEventPo save() {
        final TurnEventPo po = new TurnEventPo();
        po.setType(getClass().getSimpleName());
        po.setPlayerName(getPlayerName());
        po.setRepairKind(kind.name());
        po.setRepairShipId(shipId);
        po.setRepairShipType(shipType);
        po.setRepairDestinationName(destinationName);
        po.setRepairEuropeDestination(europeDestination);
        po.setRepairLostPassengers(lostPassengers);
        po.setRepairLostGoods(lostGoods);
        return po;
    }

    public static TurnEventShipRepair tryLoad(final TurnEventPo po) {
        if (!TurnEventShipRepair.class.getSimpleName().equals(po.getType())) {
            return null;
        }
        Preconditions.checkArgument(
                po.getRepairKind() != null && po.getRepairShipId() != null
                        && po.getRepairLostPassengers() != null
                        && po.getRepairLostGoods() != null
                        && po.isRepairEuropeDestination() != null,
                "Incomplete repair event");
        return new TurnEventShipRepair(po.getPlayerName(),
                Kind.valueOf(po.getRepairKind()), po.getRepairShipId(),
                po.getRepairShipType(), po.getRepairDestinationName(),
                po.isRepairEuropeDestination(), po.getRepairLostPassengers(),
                po.getRepairLostGoods());
    }
}
