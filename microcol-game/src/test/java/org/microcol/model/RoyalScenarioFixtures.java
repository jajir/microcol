package org.microcol.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.microcol.model.RoyalRoster.Family;
import org.microcol.model.RoyalRoster.State;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.CargoSlotPo;
import org.microcol.model.store.ColonyPo;
import org.microcol.model.store.ColonySightingPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlaceCargoSlotPo;
import org.microcol.model.store.PlaceEuropePortPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.RoyalMemberPo;
import org.microcol.model.store.RoyalRosterPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.UnitActionNoAction;
import org.microcol.model.unit.UnitArtillery;
import org.microcol.model.unit.UnitFreeColonist;

/**
 * Prepared saved games for the Crown's deployment (R-0186). They start from the
 * authored Dutch campaign, whose island has four coastal colonies and an east
 * sea lane, and change only saved data before it is loaded.
 */
public final class RoyalScenarioFixtures {

    public static final String KING = "Dutch's King";

    public static final String REBELS = "Dutch";

    /** Unit identity of the prepared carrier, above every authored unit. */
    public static final int CARRIER_ID = 100;

    /** First identity of a prepared passenger, in the order they are named. */
    public static final int FIRST_PASSENGER_ID = 101;

    private static final String CAMPAIGN = "/maps/default-campaign-mission-2-thrive.microcol";

    private static final int STORED_GALLEON_ID = 3;

    private RoyalScenarioFixtures() {
    }

    /**
     * A new campaign in which the Dutch have declared independence or not, with
     * the fresh force of the difficulty in the Crown. The Crown knows the
     * colonies its subjects hold, as it does after a real declaration. The
     * Dutch galleon that would arrive on the Crown's sea lane waits in Europe,
     * because the human player of a test never moves it.
     *
     * @param difficulty required difficulty
     * @param declared   whether the Dutch have declared independence
     * @return the saved game, not yet loaded
     */
    public static GameModelPo crown(final Difficulty difficulty,
            final boolean declared) {
        final GameModelPo data = new GameModelPoDao()
                .loadFromClassPath(CAMPAIGN);
        final ModelPo model = data.getModel();
        model.setDifficulty(difficulty);
        PlayerPo crown = null;
        for (final PlayerPo player : model.getGameManager().getPlayers()) {
            if (KING.equals(player.getName())) {
                player.setRoyalRoster(RoyalRoster.initial(difficulty));
                crown = player;
            } else if (REBELS.equals(player.getName())) {
                if (declared) {
                    RebellionFixtures.declare(model, player);
                } else {
                    RebellionFixtures.undeclare(player);
                }
            }
        }
        final UnitPo galleon = unit(model, STORED_GALLEON_ID);
        galleon.setPlaceHighSeas(null);
        galleon.setPlaceEuropePort(new PlaceEuropePortPo());
        for (final ColonyPo colony : model.getColonies()) {
            final ColonySightingPo known = new ColonySightingPo();
            known.setLocation(colony.getLocation());
            known.setColonyName(colony.getName());
            known.setOwnerName(colony.getOwnerName());
            known.setObservedTurn(0);
            crown.getColonySightings().add(known);
        }
        // A declared nation's galleon waiting in Europe is Crown-seized
        // (R-0187).
        ClosedEuropeFixtures.seizeHomeEurope(model);
        return data;
    }

    /** A declared campaign with the fresh Conquistador force. */
    public static GameModelPo declaredCrown() {
        return crown(Difficulty.CONQUISTADOR, true);
    }

    /**
     * A war that is already under way. The Crown has the Governor force of
     * fourteen ground members and two men-of-war, but only the named members
     * survive: the reserve waits in Europe, the aboard members sit in the hold
     * of the carrier, and every other ground member and the second ship are
     * lost. The carrier is the first man-of-war, or lost as well.
     *
     * @param carrierAt       sea square of the carrier on the map, or
     *                        {@code null} for the European port
     * @param carrierSurvives whether the carrier is alive
     * @param reserveIds      ground members waiting in Europe
     * @param aboardIds       infantry or cavalry members in the carrier's hold
     * @return the saved game, not yet loaded
     */
    public static GameModelPo midWar(final Location carrierAt,
            final boolean carrierSurvives, final int[] reserveIds,
            final int[] aboardIds) {
        final GameModelPo data = crown(Difficulty.GOVERNOR, true);
        final ModelPo model = data.getModel();
        final PlayerPo crown = model.getGameManager().getPlayers().stream()
                .filter(player -> KING.equals(player.getName())).findFirst()
                .orElseThrow();
        final RoyalRosterPo roster = RoyalRoster.initial(Difficulty.GOVERNOR);
        boolean firstShip = true;
        for (final RoyalMemberPo member : roster.getMembers()) {
            if (member.getFamily() == Family.MAN_OF_WAR) {
                if (firstShip && carrierSurvives) {
                    member.setState(State.SHIPPED);
                    member.setUnitId(CARRIER_ID);
                } else {
                    member.setState(State.DESTROYED);
                }
                firstShip = false;
            } else if (contains(reserveIds, member.getId())) {
                member.setState(State.RESERVE);
            } else if (contains(aboardIds, member.getId())) {
                member.setState(State.SHIPPED);
                member.setUnitId(FIRST_PASSENGER_ID
                        + indexOf(aboardIds, member.getId()));
            } else {
                member.setState(State.DESTROYED);
            }
        }
        crown.setRoyalRoster(roster);
        if (carrierSurvives) {
            model.getUnits().add(carrier(carrierAt, aboardIds));
            for (final int id : aboardIds) {
                model.getUnits().add(passenger(roster, id, aboardIds));
            }
        }
        return data;
    }

    /**
     * Every way the Crown's deployed force could differ from its roster. Each
     * shipped member names one live unit of the Crown with its own family, no
     * unit is named twice, and the Crown has no man-of-war or royal regular
     * outside the roster. Other Crown units are allowed: they are colonists,
     * soldiers and guns taken with a captured colony, which never become royal
     * forces.
     *
     * @param model required live game
     * @return descriptions of the differences, empty when the force is exact
     */
    public static List<String> deploymentViolations(final Model model) {
        final PlayerKing king = (PlayerKing) model.getPlayerByName(KING);
        final RoyalRoster roster = king.getRoyalRoster();
        final List<String> out = new ArrayList<>();
        final Set<Integer> bound = new HashSet<>();
        for (final RoyalRoster.Member member : roster.getMembers()) {
            if (member.getState() != State.SHIPPED) {
                continue;
            }
            final int unitId = member.getUnitId().orElse(-1);
            if (!bound.add(unitId)) {
                out.add("unit " + unitId + " is bound twice");
            }
            final Unit unit = model.tryGetUnitById(unitId).orElse(null);
            if (unit == null) {
                out.add("member " + member.getId() + " names a missing unit");
            } else if (unit.getOwner() != king) {
                out.add("member " + member.getId() + " names a unit of "
                        + unit.getOwner().getName());
            } else if (!isFamily(member.getFamily(), unit)) {
                out.add("member " + member.getId() + " is not a "
                        + member.getFamily() + ": " + unit);
            }
        }
        for (final Unit unit : king.getAllUnits()) {
            if (bound.contains(unit.getId())) {
                continue;
            }
            if (unit.getType() == UnitType.MAN_OF_WAR) {
                out.add("man-of-war " + unit.getId()
                        + " is outside the roster");
            }
            if (unit instanceof UnitFreeColonist && ((UnitFreeColonist) unit)
                    .getMilitaryRole() == MilitaryRole.ROYAL) {
                out.add("royal regular " + unit.getId()
                        + " is outside the roster");
            }
        }
        return out;
    }

    /**
     * @return Crown units bound to roster members; an exact count of the
     *         deployed royal force, without captured colonists
     */
    public static List<Unit> boundUnits(final Model model) {
        final PlayerKing king = (PlayerKing) model.getPlayerByName(KING);
        final RoyalRoster roster = king.getRoyalRoster();
        return king.getAllUnits().stream()
                .filter(unit -> roster.findByUnitId(unit.getId()).isPresent())
                .collect(Collectors.toList());
    }

    private static boolean isFamily(final Family family, final Unit unit) {
        switch (family) {
        case MAN_OF_WAR:
            return unit.getType() == UnitType.MAN_OF_WAR;
        case ARTILLERY:
            return unit instanceof UnitArtillery;
        case INFANTRY:
        case CAVALRY:
            return unit instanceof UnitFreeColonist
                    && ((UnitFreeColonist) unit)
                            .getMilitaryRole() == MilitaryRole.ROYAL
                    && ((UnitFreeColonist) unit)
                            .isMounted() == (family == Family.CAVALRY);
        default:
            return false;
        }
    }

    /**
     * Turn the squares around a sea square into land, so that a ship standing
     * there has no legal move.
     *
     * @param data required saved game
     * @param at   required square of the ship
     */
    public static void wallAround(final GameModelPo data, final Location at) {
        final Map<Location, TerrainType> terrain = data.getModel().getMap()
                .getTerrainMap();
        for (final Location neighbour : at.getNeighbors()) {
            terrain.put(neighbour, TerrainType.PRAIRIE);
        }
        data.getModel().getMap().setTerrainType(terrain);
    }

    private static UnitPo carrier(final Location at, final int[] aboardIds) {
        final UnitPo ship = new UnitPo();
        ship.setId(CARRIER_ID);
        ship.setOwnerId(KING);
        ship.setType(UnitType.MAN_OF_WAR);
        ship.setAction(new UnitActionNoAction().save());
        ship.setAvailableMoves(UnitType.MAN_OF_WAR.getSpeed());
        ship.setDepartureLocation(Location.of(49, 15));
        ship.setDepartureDirection(Direction.east);
        if (at == null) {
            ship.setPlaceEuropePort(new PlaceEuropePortPo());
        } else {
            final PlaceMapPo place = new PlaceMapPo();
            place.setLocation(at);
            place.setOrientation(Direction.west);
            ship.setPlaceMap(place);
        }
        for (int i = 0; i < aboardIds.length; i++) {
            final CargoSlotPo slot = new CargoSlotPo();
            slot.setUnitId(FIRST_PASSENGER_ID + i);
            ship.getCargo().getSlots().add(slot);
        }
        return ship;
    }

    private static UnitPo passenger(final RoyalRosterPo roster, final int id,
            final int[] aboardIds) {
        final Family family = roster.getMembers().stream()
                .filter(member -> member.getId() == id).findFirst()
                .orElseThrow().getFamily();
        if (family != Family.INFANTRY && family != Family.CAVALRY) {
            throw new IllegalArgumentException(
                    "Only infantry and cavalry are prepared aboard");
        }
        final UnitPo soldier = new UnitPo();
        soldier.setId(FIRST_PASSENGER_ID + indexOf(aboardIds, id));
        soldier.setOwnerId(KING);
        soldier.setType(UnitType.COLONIST);
        soldier.setHoldingGuns(true);
        soldier.setMounted(family == Family.CAVALRY);
        soldier.setMilitaryRole(MilitaryRole.ROYAL);
        soldier.setWorkUpdates(0);
        soldier.setMissionary(false);
        soldier.setAction(new UnitActionNoAction().save());
        soldier.setAvailableMoves(UnitType.COLONIST.getSpeed());
        soldier.setPlaceCargoSlot(new PlaceCargoSlotPo());
        return soldier;
    }

    private static UnitPo unit(final ModelPo model, final int id) {
        return model.getUnits().stream().filter(unit -> unit.getId() == id)
                .findFirst().orElseThrow();
    }

    private static boolean contains(final int[] ids, final int id) {
        return indexOf(ids, id) >= 0;
    }

    private static int indexOf(final int[] ids, final int id) {
        for (int i = 0; i < ids.length; i++) {
            if (ids[i] == id) {
                return i;
            }
        }
        return -1;
    }
}
