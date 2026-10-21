package org.microcol.model;

import java.util.Arrays;
import java.util.List;

import org.microcol.model.unit.UnitFreeColonist;
import org.microcol.model.unit.UnitWithCargo;

import com.google.common.collect.ImmutableList;

/**
 * Role and relation rules for each native visit action, taken from the owner
 * requirements: a scout may visit the chief, an unskilled free colonist or
 * servant may learn, a loaded wagon or ship may trade, scouts and armed or
 * mounted colonists may demand tribute, only units that can attack may attack,
 * and a missionary may establish a mission. Pure reads, so no unit, relation,
 * cargo or treasury changes here. Prerequisites that belong to an action's own
 * owner, such as prior contact for trade, are not judged here.
 */
final class NativeVisitRules {

    private NativeVisitRules() {
    }

    /**
     * @param visitor          required visitor adjacent to the settlement
     * @param hostility        required attitude of the settlement toward the
     *                         visitor's nation
     * @param teachingOccupied whether the settlement already has a pupil
     * @param tradeOpen        whether the market or a pending purchase is open
     *                         to this visitor now, as the model decides it
     * @return one choice per action, in the order of {@link NativeVisitAction}
     */
    static List<NativeVisitChoice> choices(final Unit visitor,
            final NativeHostility hostility, final boolean teachingOccupied,
            final boolean tradeOpen) {
        return Arrays.stream(NativeVisitAction.values())
                .map(action -> new NativeVisitChoice(action,
                        limit(action, visitor, hostility, teachingOccupied,
                                tradeOpen)))
                .collect(ImmutableList.toImmutableList());
    }

    private static NativeVisitChoice.Limit limit(final NativeVisitAction action,
            final Unit visitor, final NativeHostility hostility,
            final boolean teachingOccupied, final boolean tradeOpen) {
        // The model's own trade gates decide availability; an emptied carrier
        // is still a trader while its second stage is pending (R-0161).
        if (action == NativeVisitAction.TRADE && tradeOpen) {
            return NativeVisitChoice.Limit.AVAILABLE;
        }
        if (!hasRole(action, visitor)) {
            return NativeVisitChoice.Limit.ROLE_REFUSED;
        }
        if (refusesRelation(action, hostility)) {
            return NativeVisitChoice.Limit.RELATION_REFUSED;
        }
        if (action == NativeVisitAction.TEACHING) {
            return teachingOccupied
                    ? NativeVisitChoice.Limit.SETTLEMENT_OCCUPIED
                    : NativeVisitChoice.Limit.AVAILABLE;
        }
        return NativeVisitChoice.Limit.NOT_YET_AVAILABLE;
    }

    private static boolean hasRole(final NativeVisitAction action,
            final Unit visitor) {
        final UnitType type = visitor.getType();
        switch (action) {
        case SCOUT_CHIEF:
            return isScout(visitor);
        case TEACHING:
            return type == UnitType.COLONIST
                    || type == UnitType.INDENTURED_SERVANT;
        case TRADE:
            return (type == UnitType.WAGON || type.isShip())
                    && isLoaded(visitor);
        case TRIBUTE:
            return isScout(visitor) || isArmedOrMounted(visitor);
        case MISSION:
            return visitor instanceof UnitFreeColonist
                    && ((UnitFreeColonist) visitor).isMissionary();
        case ATTACK:
            // The shared attack predicate decides: an unarmed scout, wagon or
            // servant cannot fight, and a ship never attacks a settlement.
            return !type.isShip() && visitor.canAttack();
        default:
            throw new IllegalStateException("Unknown native visit action");
        }
    }

    /**
     * Teaching needs content or uneasy natives, and trade is refused by angry
     * or hostile natives.
     */
    private static boolean refusesRelation(final NativeVisitAction action,
            final NativeHostility hostility) {
        final boolean tense = hostility == NativeHostility.ANGRY
                || hostility == NativeHostility.HOSTILE;
        return tense && (action == NativeVisitAction.TEACHING
                || action == NativeVisitAction.TRADE);
    }

    /**
     * Ordinary and seasoned scouts share the one scout predicate of the
     * colonist, so a mounted colonist without guns counts as a scout.
     */
    private static boolean isScout(final Unit unit) {
        return unit instanceof UnitFreeColonist
                && ((UnitFreeColonist) unit).isScout();
    }

    /** Soldiers and dragoons: colonists that hold guns, or are mounted. */
    private static boolean isArmedOrMounted(final Unit unit) {
        return unit instanceof UnitFreeColonist
                && (((UnitFreeColonist) unit).isHoldingGuns()
                        || ((UnitFreeColonist) unit).isMounted());
    }

    /**
     * Passengers alone are not trade goods (R-0159), so only a goods slot
     * counts as a load.
     */
    private static boolean isLoaded(final Unit unit) {
        return unit instanceof UnitWithCargo
                && ((UnitWithCargo) unit).getCargo().getSlots().stream()
                        .anyMatch(CargoSlot::isLoadedGood);
    }

}
