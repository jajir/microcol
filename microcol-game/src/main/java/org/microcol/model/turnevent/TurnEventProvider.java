package org.microcol.model.turnevent;

import org.microcol.model.Colony;
import org.microcol.model.ColonyBuildingItemProgress;
import org.microcol.model.ColonyBuildingItemProgressConstruction;
import org.microcol.model.ColonyBuildingItemProgressUnit;
import org.microcol.model.Goods;
import org.microcol.model.Location;
import org.microcol.model.Player;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.microcol.model.GoodsType;
import org.microcol.model.Founder;
import org.microcol.model.LostCityRumorRules;
import org.microcol.model.NativeNation;
import org.microcol.model.NativeSettlement;
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

    /** Report why a go-to order of a unit was paused or canceled. */
    public static TurnEvent getGoToOrder(final Player owner,
            final TurnEventGoTo.Kind kind, final int unitId,
            final UnitType unitType, final String destinationName) {
        return new TurnEventGoTo(owner.getName(), kind, unitId, unitType,
                destinationName);
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

    /** Report a native course that granted its skill or ended in hostility. */
    public static TurnEvent getNativeTeaching(final Player player,
            final TurnEventNativeTeaching.Kind kind, final UnitType skill) {
        return new TurnEventNativeTeaching(player.getName(), kind, skill);
    }

    /**
     * Report a native convert that joined a colony or returned to its tribe.
     */
    public static TurnEvent getNativeConvert(final Player player,
            final TurnEventNativeConvert.Kind kind) {
        return new TurnEventNativeConvert(player.getName(), kind);
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
     * Notice that a hostile patrol slowed or stopped one of the player's ships.
     *
     * @param player   required owner of the intercepted ship
     * @param patrolId ID of the patrol that was selected for the check
     * @param stopped  whether the ship lost all remaining movement
     * @return turn event object
     */
    public static TurnEvent getPatrolInterception(final Player player,
            final int patrolId, final boolean stopped) {
        return new TurnEventPatrolInterception(player.getName(), patrolId,
                stopped);
    }

    /**
     * Notice that a coastal battery fired at one of the player's ships.
     *
     * @param player   required owner of the ship that was fired at
     * @param colonyId ID of the colony whose battery fired
     * @param shipId   ID of the ship that was fired at
     * @param hit      whether the shot damaged the ship
     * @return turn event object
     */
    public static TurnEvent getCoastalBattery(final Player player,
            final int colonyId, final int shipId, final boolean hit) {
        return new TurnEventCoastalBattery(player.getName(), colonyId, shipId,
                hit);
    }

    /**
     * Notice of one resolved lost-city rumor, with its actual result.
     *
     * @param player        required owner of the explorer
     * @param outcome       required result after the De Soto substitution
     * @param explorerId    ID of the explorer that entered the rumor
     * @param gold          gold awarded, zero unless the result is gold
     * @param nation        nation of the disturbed burial site, or null
     * @param deSotoAverted whether De Soto turned a bad result into empty
     * @return turn event object
     */
    public static TurnEvent getLostCityRumor(final Player player,
            final LostCityRumorRules.Outcome outcome, final int explorerId,
            final int gold, final NativeNation nation,
            final boolean deSotoAverted) {
        return new TurnEventLostCityRumor(player.getName(), outcome, explorerId,
                gold, nation, deSotoAverted);
    }

    /**
     * Durable record of one conquered native settlement (R-0174).
     *
     * @param player            required owner of the attacker
     * @param settlement        required conquered settlement, still registered
     * @param defendersDefeated defenders overcome, at least one
     * @return turn event object
     */
    public static TurnEvent getNativeConquest(final Player player,
            final NativeSettlement settlement, final int defendersDefeated) {
        return new TurnEventNativeConquest(player.getName(), settlement.getId(),
                settlement.getNation(), settlement.getLocation(),
                settlement.isCapital(), defendersDefeated);
    }

    /**
     * Notice for the owner of a pupil or missionary that died with a conquered
     * native settlement (R-0174).
     *
     * @param owner      required owner of the lost unit
     * @param settlement required conquered settlement, still registered
     * @param unitId     stable ID of the lost unit
     * @param type       type the unit had
     * @param pupil      true for a pupil, false for a missionary
     * @return turn event object
     */
    public static TurnEvent getNativeConquestLoss(final Player owner,
            final NativeSettlement settlement, final int unitId,
            final UnitType type, final boolean pupil) {
        return new TurnEventNativeConquestLoss(owner.getName(), unitId, type,
                pupil, settlement.getNation(), settlement.getId(),
                settlement.getLocation());
    }

    /** Notice that the Crown forced a war, possibly ending an alliance. */
    public static TurnEvent getCrownWar(final Player player,
            final String rivalName, final boolean allianceEnded) {
        return new TurnEventDiplomacy(player.getName(),
                allianceEnded ? TurnEventDiplomacy.Kind.CROWN_WAR_ALLIANCE_ENDED
                        : TurnEventDiplomacy.Kind.CROWN_WAR,
                rivalName, null);
    }

    /** Notice that an alliance obliged the player to defend a victim. */
    public static TurnEvent getAllyDefense(final Player player,
            final String aggressorName, final String victimName) {
        return new TurnEventDiplomacy(player.getName(),
                TurnEventDiplomacy.Kind.ALLY_DEFENSE, aggressorName,
                victimName);
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
