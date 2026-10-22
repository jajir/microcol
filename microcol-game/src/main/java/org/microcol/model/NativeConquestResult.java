package org.microcol.model;

import java.util.List;
import java.util.Optional;

/**
 * Actual result of one confirmed attack on a native settlement (R-0174). The
 * settlement is conquered only when every defender was overcome; a lost battle
 * ends the attack with the defenders that remain.
 */
public final class NativeConquestResult {

    /**
     * A pupil or missionary of a conquered settlement that found no legal
     * square and died with it. Each loss is also saved as a turn event of the
     * unit's owner.
     */
    public static final class Loss {
        private final int unitId;
        private final UnitType type;
        private final String ownerName;
        private final boolean pupil;

        Loss(final int unitId, final UnitType type, final String ownerName,
                final boolean pupil) {
            this.unitId = unitId;
            this.type = type;
            this.ownerName = ownerName;
            this.pupil = pupil;
        }

        /** @return stable ID of the lost unit */
        public int getUnitId() {
            return unitId;
        }

        /** @return type the unit had when it died */
        public UnitType getType() {
            return type;
        }

        /** @return internal name of the player who owned the unit */
        public String getOwnerName() {
            return ownerName;
        }

        /**
         * @return true for a pupil of the settlement's course, false for its
         *         missionary
         */
        public boolean isPupil() {
            return pupil;
        }
    }

    private final boolean conquered;
    private final int defendersDefeated;
    private final boolean attackerLost;
    private final MilitaryPromotion promotion;
    private final List<Loss> losses;

    NativeConquestResult(final boolean conquered, final int defendersDefeated,
            final boolean attackerLost, final MilitaryPromotion promotion,
            final List<Loss> losses) {
        this.conquered = conquered;
        this.defendersDefeated = defendersDefeated;
        this.attackerLost = attackerLost;
        this.promotion = promotion;
        this.losses = List.copyOf(losses);
    }

    /**
     * @return whether the settlement was conquered and removed
     */
    public boolean isConquered() {
        return conquered;
    }

    /**
     * @return defenders overcome in this attack, possibly fewer than all
     */
    public int getDefendersDefeated() {
        return defendersDefeated;
    }

    /**
     * @return whether the attacker lost a battle, and so was destroyed or lost
     *         its equipment
     */
    public boolean isAttackerLost() {
        return attackerLost;
    }

    /**
     * @return the promotion the attacker earned in a won battle, if any
     */
    public Optional<MilitaryPromotion> getPromotion() {
        return Optional.ofNullable(promotion);
    }

    /**
     * @return the pupil and missionary that died with the settlement, empty
     *         when each of them found a legal square
     */
    public List<Loss> getLosses() {
        return losses;
    }

    /**
     * @return whether a pupil or missionary died with the settlement
     */
    public boolean isTrappedUnitLost() {
        return !losses.isEmpty();
    }
}
