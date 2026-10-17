package org.microcol.model.turnevent;

import org.microcol.model.Colony;
import org.microcol.model.ColonyBuildingItemProgress;
import org.microcol.model.ColonyBuildingItemProgressConstruction;
import org.microcol.model.ColonyBuildingItemProgressUnit;
import org.microcol.model.Goods;
import org.microcol.model.Location;
import org.microcol.model.Player;
import org.microcol.model.Unit;
import org.microcol.model.GoodsType;
import org.microcol.model.Founder;
import java.util.stream.Collectors;

import org.microcol.model.store.QueueItemType;

import com.google.common.base.Preconditions;

/**
 * Class simplify creating of turn events.
 */
public final class TurnEventProvider {

    /**
     * Hidden default constructor.
     */
    private TurnEventProvider() {
    }

    /** Persist one completed custom-house sale with its colony identity. */
    public static TurnEvent getCustomHouseExport(final Player owner,
            final Colony colony, final Goods goods, final int income) {
        return new TurnEventCustomHouseExport(owner.getName(), colony.getId(),
                colony.getName(), goods, income);
    }

    /** Report one construction phase blocked only by missing tools. */
    public static TurnEvent getConstructionWaitingForTools(final Player owner,
            final Colony colony, final ColonyBuildingItemProgress<?> project,
            final int missingTools) {
        if (project instanceof ColonyBuildingItemProgressConstruction) {
            return new TurnEventConstructionWaitingForTools(owner.getName(),
                    colony.getId(), colony.getName(),
                    QueueItemType.CONSTRUCTION,
                    ((ColonyBuildingItemProgressConstruction) project)
                            .getConstructionType(),
                    null, missingTools);
        }
        Preconditions.checkArgument(
                project instanceof ColonyBuildingItemProgressUnit,
                "Unknown queue item kind");
        return new TurnEventConstructionWaitingForTools(owner.getName(),
                colony.getId(), colony.getName(), QueueItemType.UNIT, null,
                ((ColonyBuildingItemProgressUnit) project).getUnitType(),
                missingTools);
    }

    /**
     * Get new turn event when ship come to Europe port.
     *
     * @param player required owner of ship
     * @return turn event object
     */
    public static TurnEvent getShipComeEuropePort(final Player player) {
        return new TurnEventShipComeToEuropePort(player.getName());
    }

    /**
     * Get new turn event when ship come from Europe to colonies.
     *
     * @param player required owner of ship
     * @return turn event object
     */
    public static TurnEvent getShipComeToHighSeas(final Player player) {
        return new TurnEventShipComeToHighSeas(player.getName());
    }

    /**
     * Get new turn event when famine killed some colonist in colony.
     *
     * @param player required owner colony plagued by famine
     * @param colony required plagued colony
     * @return turn event object
     */
    public static TurnEvent getFaminePlagueColony(final Player player,
            final Colony colony, final Unit victim) {
        return new TurnEventFaminePlagueColony(player.getName(), colony.getId(),
                colony.getName(), victim.getId(), victim.getType(),
                colony.isValid()
                        ? GoodsType.GOOD_TYPES.stream()
                                .map(colony.getWarehouse()::getGoods)
                                .collect(Collectors.toList())
                        : java.util.Collections.emptyList());
    }

    /**
     * Get new turn event when famine is about to kill some colonist in colony.
     *
     * @param player required owner colony plagued by famine
     * @param colony required plagued colony
     * @return turn event object
     */
    public static TurnEvent getFamineWillPlagueColony(final Player player,
            final Colony colony) {
        return new TurnEventFamineWillPlagueColony(player.getName(),
                colony.getId(), colony.getName());
    }

    /**
     * Get new turn event when new colony was destroyed.
     *
     * @param player required owner of destroyed colony
     * @param colony required destroyed colony name
     * @return turn event object
     */
    public static TurnEvent getColonyWasDestroyed(final Player player,
            final Colony colony) {
        return getColonyWasDestroyed(player, colony, 0);
    }

    public static TurnEvent getColonyWasDestroyed(final Player player,
            final Colony colony, final int lostShips) {
        return new TurnEventColonyWasDestroyed(player.getName(), colony.getId(),
                colony.getName(), lostShips);
    }

    /**
     * Get new turn event when new colony was lost. Some other player conquer
     * it.
     *
     * @param player required owner of lost colony
     * @param colony required destroyed colony name
     * @return turn event object
     */
    public static TurnEvent getColonyWasLost(final Player player,
            final Colony colony) {
        return new TurnEventColonyWasLost(player.getName(), colony.getId(),
                colony.getName());
    }

    /**
     * Get new turn event when new unit appears at Europe.
     *
     * @param player required owner of new unit
     * @return turn event object
     */
    public static TurnEvent getNewUnitInEurope(final Player player) {
        return new TurnEventNewUnitInEurope(player.getName());
    }

    public static TurnEvent getFounderArrived(final Player player,
            final Founder founder) {
        return new TurnEventFounderArrived(player.getName(), founder);
    }

    /** Report one actual school or productive-work graduation. */
    public static TurnEvent getEducationCompleted(final Player player,
            final Colony colony, final Unit student,
            final org.microcol.model.UnitType before,
            final boolean veteranBefore, final boolean workExperience) {
        return new TurnEventEducationCompleted(player.getName(), colony.getId(),
                colony.getName(), student.getId(), before, student.getType(),
                veteranBefore,
                ((org.microcol.model.unit.UnitFreeColonist) student)
                        .isVeteran(),
                workExperience);
    }

    /** Notice of a newly frozen Franklin proposal for the turn report. */
    public static TurnEvent getPeaceOffer(final Player player,
            final String rivalName, final int price) {
        return new TurnEventPeaceOffer(player.getName(), rivalName, price);
    }

    /**
     * Get new turn event when new unit appears in colony.
     *
     * @param player required owner of new unit
     * @param colony required colony
     * @return turn event object
     */
    public static TurnEvent getNewUnitInColony(final Player player,
            final Colony colony) {
        return new TurnEventNewUnitInColony(player.getName(), colony.getId(),
                colony.getName());
    }

    /**
     * Report lumber from a cleared forest that no warehouse took.
     *
     * @param player    required owner of the pioneer
     * @param lost      the discarded lumber
     * @param location  the square whose forest was cleared
     * @param recipient the colony whose warehouse was full, or null when no
     *                  colony was within reach
     * @return turn event object
     */
    public static TurnEvent getForestLumberLost(final Player player,
            final Goods lost, final Location location, final Colony recipient) {
        return new TurnEventForestLumberLost(player.getName(), lost, location,
                recipient == null ? null : recipient.getId(),
                recipient == null ? null : recipient.getName());
    }

    /**
     * Get new turn event when some goods is throws away because of limited free
     * space warehouse.
     *
     * @param player required owner of new unit
     * @param goods  define lost goods type and amount
     * @param colony required colony
     * @return turn event object
     */
    public static TurnEvent getGoodsWasThrowsAway(final Player player,
            final Goods goods, final Colony colony) {
        return new TurnEventGoodsWasThrownAway(player.getName(), colony.getId(),
                colony.getName(), goods);
    }

}
