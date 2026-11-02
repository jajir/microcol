package org.akropia.model.turnevent;

import java.util.Optional;

import org.akropia.model.Goods;
import org.akropia.model.GoodsType;
import org.akropia.model.UnitType;
import org.akropia.model.store.TurnEventPo;

import com.google.common.base.Preconditions;

/**
 * A persisted notice of one goods entry that an automatic trade route actually
 * moved at a stop (R-0392). It keeps the route, carrier and stop as they were,
 * so the line stays readable after a rename or removal. A European sale or
 * purchase is one notice with its actual treasury change after tax; a colony
 * transfer has no treasury change. It is routine: it never opens the report by
 * itself when the turn summary is off.
 */
public final class TurnEventTradeRouteTransfer extends AbstractTurnEvent {

    private final int routeId;

    private final String routeName;

    private final int carrierId;

    private final UnitType carrierType;

    private final int stopIndex;

    private final Integer colonyId;

    private final String colonyName;

    private final boolean unload;

    private final Goods goods;

    private final int goldChange;

    TurnEventTradeRouteTransfer(final String playerName, final int routeId,
            final String routeName, final int carrierId,
            final UnitType carrierType, final int stopIndex,
            final Integer colonyId, final String colonyName,
            final boolean unload, final Goods goods, final int goldChange) {
        super(playerName);
        final boolean europe = colonyId == null;
        Preconditions
                .checkArgument(
                        routeId >= 0 && routeName != null && carrierId >= 0
                                && carrierType != null
                                && UnitType.UNIT_TYPES.contains(carrierType)
                                && stopIndex >= 0 && goods != null
                                && goods.getAmount() > 0
                                && GoodsType.BUYABLE_GOOD_TYPES
                                        .contains(goods.getType())
                                && (europe
                                        ? colonyName == null
                                                && (unload ? goldChange >= 0
                                                        : goldChange <= 0)
                                        : colonyId >= 0 && colonyName != null
                                                && goldChange == 0),
                        "Invalid trade route transfer event");
        this.routeId = routeId;
        this.routeName = routeName;
        this.carrierId = carrierId;
        this.carrierType = carrierType;
        this.stopIndex = stopIndex;
        this.colonyId = colonyId;
        this.colonyName = colonyName;
        this.unload = unload;
        this.goods = goods;
        this.goldChange = goldChange;
    }

    public int getRouteId() {
        return routeId;
    }

    /** @return name the route had when the goods moved */
    public String getRouteName() {
        return routeName;
    }

    public int getCarrierId() {
        return carrierId;
    }

    /** @return type the carrier had when the goods moved */
    public UnitType getCarrierType() {
        return carrierType;
    }

    /** @return zero-based index of the stop in the route */
    public int getStopIndex() {
        return stopIndex;
    }

    /** @return whether the stop was Europe, where goods are traded */
    public boolean isEurope() {
        return colonyId == null;
    }

    /** @return ID of the stop colony; empty for Europe */
    public Optional<Integer> getColonyId() {
        return Optional.ofNullable(colonyId);
    }

    /** @return name the stop colony had; empty for Europe */
    public Optional<String> getColonyName() {
        return Optional.ofNullable(colonyName);
    }

    /** @return whether the goods left the carrier, otherwise they boarded */
    public boolean isUnload() {
        return unload;
    }

    /** @return goods type and the quantity actually moved */
    public Goods getGoods() {
        return goods;
    }

    /**
     * @return signed treasury change after tax: positive for a sale, negative
     *         for a purchase, zero at a colony
     */
    public int getGoldChange() {
        return goldChange;
    }

    @Override
    public TurnEventPo save() {
        final TurnEventPo po = new TurnEventPo();
        po.setType(getClass().getSimpleName());
        po.setPlayerName(getPlayerName());
        po.setTradeRouteId(routeId);
        po.setTradeRouteName(routeName);
        po.setTradeRouteCarrierId(carrierId);
        po.setTradeRouteCarrierType(carrierType);
        po.setTradeRouteStopIndex(stopIndex);
        po.setColonyId(colonyId);
        po.setColonyName(colonyName);
        po.setTradeRouteUnload(unload);
        po.setGoods(goods);
        po.setTradeRouteGold(goldChange);
        return po;
    }

    public static TurnEventTradeRouteTransfer tryLoad(final TurnEventPo po) {
        if (!TurnEventTradeRouteTransfer.class.getSimpleName()
                .equals(po.getType())) {
            return null;
        }
        Preconditions.checkArgument(
                po.getPlayerName() != null && po.getTradeRouteId() != null
                        && po.getTradeRouteCarrierId() != null
                        && po.getTradeRouteStopIndex() != null
                        && po.isTradeRouteUnload() != null
                        && po.getTradeRouteGold() != null,
                "Incomplete trade route transfer event");
        return new TurnEventTradeRouteTransfer(po.getPlayerName(),
                po.getTradeRouteId(), po.getTradeRouteName(),
                po.getTradeRouteCarrierId(), po.getTradeRouteCarrierType(),
                po.getTradeRouteStopIndex(), po.getColonyId(),
                po.getColonyName(), po.isTradeRouteUnload(), po.getGoods(),
                po.getTradeRouteGold());
    }

}
