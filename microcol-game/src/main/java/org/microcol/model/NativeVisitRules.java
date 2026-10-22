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
     * @param missionOccupied  whether the settlement already holds a mission
     * @param rivalMission     whether that held mission belongs to another
     *                         nation; only meaningful when it is occupied
     * @param tradeOpen        whether the market or a pending purchase is open
     *                         to this visitor now, as the model decides it
     * @param incitable        whether at least one European power may be
     *                         selected as the target of an incitement now
     * @param attackable       whether the settlement has an actual conquest
     *                         quote this visitor can confirm now (R-0174)
     * @param tributeCooling   whether this visitor's European demanded tribute
     *                         here within the last ten turns (R-0162)
     * @param tributable       whether a tribute demand from this visitor has no
     *                         other unit holds the settlement square now
     *                         (R-0162)
     * @return one choice per action, in the order of {@link NativeVisitAction}
     */
    static List<NativeVisitChoice> choices(final Unit visitor,
            final NativeHostility hostility, final boolean teachingOccupied,
            final boolean missionOccupied, final boolean rivalMission,
            final boolean tradeOpen, final boolean incitable,
            final boolean attackable, final boolean tributeCooling,
            final boolean tributable) {
        return Arrays.stream(NativeVisitAction.values())
                .map(action -> new NativeVisitChoice(action,
                        limit(action, visitor, hostility, teachingOccupied,
                                missionOccupied, rivalMission, tradeOpen,
                                incitable, attackable, tributeCooling,
                                tributable)))
                .collect(ImmutableList.toImmutableList());
    }

    /**
     * Role of a tribute demand (R-0162): a scout, soldier or dragoon. The model
     * uses the same predicate to refuse a demand that the chooser never offers.
     *
     * @param visitor required unit that would demand tribute
     * @return whether the unit's role may demand tribute
     */
    static boolean canDemandTribute(final Unit visitor) {
        return hasRole(NativeVisitAction.TRIBUTE, visitor);
    }

    private static NativeVisitChoice.Limit limit(final NativeVisitAction action,
            final Unit visitor, final NativeHostility hostility,
            final boolean teachingOccupied, final boolean missionOccupied,
            final boolean rivalMission, final boolean tradeOpen,
            final boolean incitable, final boolean attackable,
            final boolean tributeCooling, final boolean tributable) {
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
        // Tribute is never refused by attitude: the refusal of the settlement
        // still provokes it (R-0162). The wait is named before the target.
        if (action == NativeVisitAction.TRIBUTE) {
            if (tributeCooling) {
                return NativeVisitChoice.Limit.TRIBUTE_COOLDOWN;
            }
            return tributable ? NativeVisitChoice.Limit.AVAILABLE
                    : NativeVisitChoice.Limit.NO_TRIBUTE_TARGET;
        }
        // Every mood admits the chief; the risk shown before confirmation
        // depends on the mood (R-0158).
        if (action == NativeVisitAction.SCOUT_CHIEF) {
            return NativeVisitChoice.Limit.AVAILABLE;
        }
        if (action == NativeVisitAction.TEACHING) {
            return teachingOccupied
                    ? NativeVisitChoice.Limit.SETTLEMENT_OCCUPIED
                    : NativeVisitChoice.Limit.AVAILABLE;
        }
        if (action == NativeVisitAction.MISSION) {
            if (!missionOccupied) {
                return NativeVisitChoice.Limit.AVAILABLE;
            }
            // A rival's mission is challenged, never replaced by establishment.
            return rivalMission ? NativeVisitChoice.Limit.CONTEST_AVAILABLE
                    : NativeVisitChoice.Limit.MISSION_OCCUPIED;
        }
        // The missionary's own mission state never stops an incitement
        // (R-0166).
        if (action == NativeVisitAction.INCITE) {
            return incitable ? NativeVisitChoice.Limit.AVAILABLE
                    : NativeVisitChoice.Limit.NO_INCITEMENT_TARGET;
        }
        // The attack is open only with its real defenders and attack terms; a
        // hostile or content attitude does not refuse it (R-0174).
        if (action == NativeVisitAction.ATTACK) {
            return attackable ? NativeVisitChoice.Limit.AVAILABLE
                    : NativeVisitChoice.Limit.NO_ATTACK_TARGET;
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
        case INCITE:
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
