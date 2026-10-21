package org.microcol.model;

import com.google.common.base.Preconditions;

/**
 * One completed action that changes native relations. Its owner creates it at
 * the commit of the origin action, never at preview or dialog opening. The
 * action ID identifies that transaction: applying the same ID again is refused,
 * while a distinct action has its own ID. Settlement and nation relationships
 * are checked by {@link NativeRelations} before any change.
 */
final class NativeRelationOutcome {

    /** Completed action kinds, each with one fixed effect. */
    enum Kind {
        /**
         * Attack on a native unit; a homeless brave changes tribal anger only.
         */
        UNIT_ATTACK,
        /** Attack on a settlement that remains standing. */
        SETTLEMENT_ATTACK,
        /** Attack that destroys a settlement; includes the attack's effect. */
        SETTLEMENT_DESTROYED,
        /** Disturbed burial site, already resolved to one native nation. */
        BURIAL,
        /** Tribute demand at a settlement, whether it was paid or refused. */
        TRIBUTE,
        /** Completed trade with a settlement. */
        TRADE,
        /** Goods given to a settlement. */
        GIFT,
        /** Muskets and horses sold to a native nation. */
        ARMS_SALE
    }

    private final long actionId;

    private final Kind kind;

    private final NativeNation nation;

    /** Settlement of the action; null only for a homeless brave's attack. */
    private final NativeSettlement settlement;

    /** Goods of a gift or muskets and horses sold; zero otherwise. */
    private final int quantity;

    private NativeRelationOutcome(final long actionId, final Kind kind,
            final NativeNation nation, final NativeSettlement settlement,
            final int quantity) {
        this.actionId = actionId;
        this.kind = kind;
        this.nation = nation;
        this.settlement = settlement;
        this.quantity = quantity;
    }

    /**
     * Attack on a native unit.
     *
     * @param actionId required positive origin action ID
     * @param nation   required nation of the attacked brave
     * @param home     home settlement, or null when the brave is homeless
     * @return outcome of the attack
     */
    static NativeRelationOutcome unitAttack(final long actionId,
            final NativeNation nation, final NativeSettlement home) {
        return create(actionId, Kind.UNIT_ATTACK, nation, home, 0);
    }

    /**
     * Attack on a settlement, optionally destroying it.
     *
     * @param actionId   required positive origin action ID
     * @param settlement required attacked settlement
     * @param destroyed  whether the attack destroyed the settlement
     * @return outcome of the attack
     */
    static NativeRelationOutcome settlementAttack(final long actionId,
            final NativeSettlement settlement, final boolean destroyed) {
        Preconditions.checkNotNull(settlement);
        return create(actionId,
                destroyed ? Kind.SETTLEMENT_DESTROYED : Kind.SETTLEMENT_ATTACK,
                settlement.getNation(), settlement, 0);
    }

    /**
     * Disturbed burial site, with its nearest settlement already resolved.
     *
     * @param actionId required positive origin action ID
     * @param nation   required resolved native nation
     * @return outcome of the disturbance
     */
    static NativeRelationOutcome burial(final long actionId,
            final NativeNation nation) {
        return create(actionId, Kind.BURIAL, nation, null, 0);
    }

    /**
     * Tribute demand at a settlement, paid or refused.
     *
     * @param actionId   required positive origin action ID
     * @param settlement required settlement of the demand
     * @return outcome of the demand
     */
    static NativeRelationOutcome tribute(final long actionId,
            final NativeSettlement settlement) {
        Preconditions.checkNotNull(settlement);
        return create(actionId, Kind.TRIBUTE, settlement.getNation(),
                settlement, 0);
    }

    /**
     * Completed trade with a settlement.
     *
     * @param actionId   required positive origin action ID
     * @param settlement required settlement of the trade
     * @return outcome of the trade
     */
    static NativeRelationOutcome trade(final long actionId,
            final NativeSettlement settlement) {
        Preconditions.checkNotNull(settlement);
        return create(actionId, Kind.TRADE, settlement.getNation(), settlement,
                0);
    }

    /**
     * Gift of goods to a settlement.
     *
     * @param actionId   required positive origin action ID
     * @param settlement required settlement receiving the gift
     * @param goods      nonnegative number of goods given
     * @return outcome of the gift
     */
    static NativeRelationOutcome gift(final long actionId,
            final NativeSettlement settlement, final int goods) {
        Preconditions.checkNotNull(settlement);
        return create(actionId, Kind.GIFT, settlement.getNation(), settlement,
                goods);
    }

    /**
     * Muskets and horses sold to a native nation, in one completed sale.
     *
     * @param actionId required positive origin action ID
     * @param nation   required buying native nation
     * @param quantity nonnegative muskets plus horses sold
     * @return outcome of the sale
     */
    static NativeRelationOutcome armsSale(final long actionId,
            final NativeNation nation, final int quantity) {
        return create(actionId, Kind.ARMS_SALE, nation, null, quantity);
    }

    private static NativeRelationOutcome create(final long actionId,
            final Kind kind, final NativeNation nation,
            final NativeSettlement settlement, final int quantity) {
        Preconditions.checkArgument(actionId > 0,
                "Native relation action ID must be positive");
        Preconditions.checkNotNull(kind);
        Preconditions.checkNotNull(nation);
        Preconditions.checkArgument(quantity >= 0,
                "Native relation quantity cannot be negative");
        return new NativeRelationOutcome(actionId, kind, nation, settlement,
                quantity);
    }

    long getActionId() {
        return actionId;
    }

    Kind getKind() {
        return kind;
    }

    NativeNation getNation() {
        return nation;
    }

    /**
     * @return settlement of the action, or null when it has none
     */
    NativeSettlement getSettlement() {
        return settlement;
    }

    int getQuantity() {
        return quantity;
    }

}
