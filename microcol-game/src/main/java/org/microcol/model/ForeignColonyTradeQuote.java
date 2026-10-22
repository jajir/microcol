package org.microcol.model;

import java.util.Objects;

/**
 * Immutable terms of one sale or barter between a loaded carrier and an
 * adjacent foreign colony (R-0167). The incoming goods leave the carrier for
 * the colony at the colony owner's Europe bid; the outgoing goods leave the
 * colony for the carrier at its Europe ask. The net gold settles the difference
 * and never includes the visitor's home tax.
 * <p>
 * The quote records the live participants, goods, warehouse, carrier room and
 * treasuries it was built from. Equality compares all of them, so a confirmed
 * quote matches the model only while nothing it depends on has changed.
 */
public final class ForeignColonyTradeQuote {

    private final int visitorId;
    private final int colonyId;
    private final Goods incoming;
    private final Goods outgoing;
    private final ForeignColonyTradeProblem problem;
    private final int incomingUnitPrice;
    private final int outgoingUnitPrice;
    private final int netGold;
    private final int visitorGold;
    private final int hostGold;
    private final int carried;
    private final int hostFreeStorage;
    private final int hostStock;
    private final int carrierRoom;

    ForeignColonyTradeQuote(final int visitorId, final int colonyId,
            final Goods incoming, final Goods outgoing,
            final ForeignColonyTradeProblem problem,
            final int incomingUnitPrice, final int outgoingUnitPrice,
            final int netGold, final int visitorGold, final int hostGold,
            final int carried, final int hostFreeStorage, final int hostStock,
            final int carrierRoom) {
        this.visitorId = visitorId;
        this.colonyId = colonyId;
        this.incoming = incoming;
        this.outgoing = outgoing;
        this.problem = problem;
        this.incomingUnitPrice = incomingUnitPrice;
        this.outgoingUnitPrice = outgoingUnitPrice;
        this.netGold = netGold;
        this.visitorGold = visitorGold;
        this.hostGold = hostGold;
        this.carried = carried;
        this.hostFreeStorage = hostFreeStorage;
        this.hostStock = hostStock;
        this.carrierRoom = carrierRoom;
    }

    /** @return identifier of the visiting carrier this quote was made for */
    public int getVisitorId() {
        return visitorId;
    }

    /** @return identifier of the host colony this quote was made for */
    public int getColonyId() {
        return colonyId;
    }

    /** @return goods the carrier gives to the colony */
    public Goods getIncoming() {
        return incoming;
    }

    /**
     * @return goods the colony gives to the carrier, {@code null} for a sale
     */
    public Goods getOutgoing() {
        return outgoing;
    }

    /**
     * @return {@code true} when the offered terms are a sale without goods back
     */
    public boolean isSale() {
        return outgoing == null;
    }

    /**
     * @return the first reason the trade is not legal,
     *         {@link ForeignColonyTradeProblem#NONE} when legal
     */
    public ForeignColonyTradeProblem getProblem() {
        return problem;
    }

    /** @return {@code true} when the trade may be confirmed */
    public boolean isLegal() {
        return problem == ForeignColonyTradeProblem.NONE;
    }

    /** @return the colony's Europe bid per piece of the incoming goods */
    public int getIncomingUnitPrice() {
        return incomingUnitPrice;
    }

    /**
     * @return the colony's Europe ask per piece of the outgoing goods, zero for
     *         a sale
     */
    public int getOutgoingUnitPrice() {
        return outgoingUnitPrice;
    }

    /**
     * @return net gold between the nations: positive when the colony owner pays
     *         the visitor, negative when the visitor pays the colony owner,
     *         zero when no gold changes hands
     */
    public int getNetGold() {
        return netGold;
    }

    /** @return the visiting nation's treasury when the quote was made */
    public int getVisitorGold() {
        return visitorGold;
    }

    /** @return the colony owner's treasury when the quote was made */
    public int getHostGold() {
        return hostGold;
    }

    /**
     * @return pieces of the incoming goods the carrier held when the quote was
     *         made
     */
    public int getCarried() {
        return carried;
    }

    /**
     * @return free warehouse room for the incoming goods when the quote was
     *         made
     */
    public int getHostFreeStorage() {
        return hostFreeStorage;
    }

    /** @return warehouse stock of the outgoing goods when the quote was made */
    public int getHostStock() {
        return hostStock;
    }

    /**
     * @return carrier room for the outgoing goods once the incoming goods have
     *         left, when the quote was made; holds that the incoming goods only
     *         partly empty, other goods and units do not count
     */
    public int getCarrierRoom() {
        return carrierRoom;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ForeignColonyTradeQuote)) {
            return false;
        }
        final ForeignColonyTradeQuote that = (ForeignColonyTradeQuote) o;
        return visitorId == that.visitorId && colonyId == that.colonyId
                && Objects.equals(incoming, that.incoming)
                && Objects.equals(outgoing, that.outgoing)
                && problem == that.problem
                && incomingUnitPrice == that.incomingUnitPrice
                && outgoingUnitPrice == that.outgoingUnitPrice
                && netGold == that.netGold && visitorGold == that.visitorGold
                && hostGold == that.hostGold && carried == that.carried
                && hostFreeStorage == that.hostFreeStorage
                && hostStock == that.hostStock
                && carrierRoom == that.carrierRoom;
    }

    @Override
    public int hashCode() {
        return Objects.hash(visitorId, colonyId, incoming, outgoing, problem,
                incomingUnitPrice, outgoingUnitPrice, netGold, visitorGold,
                hostGold, carried, hostFreeStorage, hostStock, carrierRoom);
    }
}
