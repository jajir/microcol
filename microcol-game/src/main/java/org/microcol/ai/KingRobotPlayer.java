package org.microcol.ai;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.microcol.gui.screen.game.gamepanel.AnimationManager;
import org.microcol.model.Colony;
import org.microcol.model.Continent;
import org.microcol.model.ContinentTool;
import org.microcol.model.Continents;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Path;
import org.microcol.model.PlaceHighSea;
import org.microcol.model.Player;
import org.microcol.model.PlayerKing;
import org.microcol.model.RoyalRoster;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.microcol.model.unit.Ship;
import org.microcol.model.unit.UnitWithCargo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public final class KingRobotPlayer extends AbstractRobotPlayer {

    private final static float KINGS_MILITARY_STREGTH_GAME_OVER = 5;

    /** Holds of one man-of-war, the most it can carry in one wave. */
    private final static int CARRIER_HOLDS = UnitType.MAN_OF_WAR
            .getCargoCapacity();

    private final SimpleUnitBehavior simpleUnitBehavior = new SimpleUnitBehavior();

    private final Player whosKingThisPlayerIs;

    private final PlayerKing king;

    private final ContinentTool continentTool = new ContinentTool();

    private final ContinentTargets continentTargets;

    public KingRobotPlayer(final Model model, final PlayerKing playerKing,
            final AnimationManager animationManager) {
        super(model, playerKing, animationManager);
        continentTargets = new ContinentTargets(model);
        this.king = Preconditions.checkNotNull(playerKing);
        this.whosKingThisPlayerIs = Preconditions
                .checkNotNull(playerKing.getWhosKingThisPlayerIs());
    }

    /**
     * The existing campaign rule: after the expedition was sent, a Crown whose
     * military strength falls below the limit is conquered. A Crown that still
     * has reserve ground units and a man-of-war to bring them is not conquered,
     * because that army is still on its way. A reserve stranded without any
     * carrier does not hold the rule back. This is not a victory rule of its
     * own; the mission reads the same flag as before.
     */
    private boolean isKinngCoquered() {
        if (!isRefWasSend()) {
            return false;
        }
        final RoyalRoster roster = king.getRoyalRoster();
        if (roster.hasSailed() && roster.hasDeliverableReserve()) {
            return false;
        }
        final int miltaryStreng = getPlayer().getPlayerStatistics()
                .getMilitaryStrength().getMilitaryStrength();
        return miltaryStreng < KINGS_MILITARY_STREGTH_GAME_OVER;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                .add("whosKingThisPlayerIs", whosKingThisPlayerIs).toString();
    }

    @Override
    protected void turnStarted() {
        if (whosKingThisPlayerIs.isDeclaredIndependence()) {
            if (mayDeployRoster()) {
                dispatchReserveShips();
                reloadReturnedShips();
            }
            if (!isRefWasSend() && king.getRoyalRoster().hasSailed()) {
                setRefWasSend(true);
            }
        }
        // A declared revolution ends only by its own loss or victory rules, so
        // the legacy military-score flag is never set for it (R-0191).
        if (!whosKingThisPlayerIs.isDeclaredIndependence()
                && isKinngCoquered()) {
            getPlayer().getExtraData().put("kingWasConquered",
                    Boolean.TRUE.toString());
        }
    }

    /**
     * A scenario that marks its expedition as sent while the roster never
     * sailed holds that expedition on the map as authored units. Deploying the
     * roster as well would create a second army, so it stays in reserve.
     */
    private boolean mayDeployRoster() {
        return !isRefWasSend() || king.getRoyalRoster().hasSailed();
    }

    @Override
    void moveUnit(final Unit unit) {
        if (!unit.isAtPlaceLocation()) {
            return;
        }
        if (unit.getType().isShip()) {
            if (unit.canHoldCargo()) {
                performMoveUnit((UnitWithCargo) unit);
            }
        } else {
            performSeekAndDestroy(unit,
                    continentTool.findContinents(getModel()));
        }
    }

    /**
     * Each reserve man-of-war with a reserve ground unit sails this royal turn.
     * The model checks every condition and creates the passengers from the
     * roster, so this only asks for the dispatch that is allowed.
     */
    private void dispatchReserveShips() {
        for (final RoyalRoster.Member member : king.getRoyalRoster()
                .getMembers()) {
            if (getModel().canDispatchRoyalShip(king, member.getId())) {
                getModel().dispatchRoyalShip(king, member.getId());
            }
        }
    }

    /**
     * A carrier that came back to the European port takes the next reserve
     * units and sails again. The same ship and the same roster members are
     * used; nothing is created for the ship itself.
     */
    private void reloadReturnedShips() {
        for (final Unit unit : king.getAllUnits()) {
            if (unit instanceof UnitWithCargo
                    && unit.getType() == UnitType.MAN_OF_WAR
                    && unit.isAtEuropePort() && getModel()
                            .canReloadRoyalShip(king, (UnitWithCargo) unit)) {
                getModel().reloadRoyalShip(king, (UnitWithCargo) unit);
            }
        }
    }

    private void performMoveUnit(final UnitWithCargo ship) {
        final Optional<RoyalRoster.Member> member = king.getRoyalRoster()
                .findByUnitId(ship.getId());
        if (member.isEmpty()) {
            performAuthoredMove(ship);
            return;
        }
        if (ship.getCargo().isEmpty()) {
            sailHomeForReserve(ship);
            return;
        }
        final Optional<Colony> target = sailTowardLanding(ship, member.get());
        if (target.isPresent()
                && ship.getLocation().isNeighbor(target.get().getLocation())
                && isPossibleToDisembark(ship)) {
            disembarkUnit(ship);
        }
    }

    /**
     * A Crown ship that is not a roster member belongs to the scenario. It
     * keeps the movement it always had and is not part of the roster, so its
     * troops are never counted twice.
     */
    private void performAuthoredMove(final UnitWithCargo ship) {
        if (ship.getCargo().isEmpty()) {
            return;
        }
        if (isPossibleToDisembark(ship)) {
            disembarkUnit(ship);
        } else {
            final Continents continents = continentTool
                    .findContinents(getModel());
            tryToReachSomeContinent(ship, continentTargets
                    .getContinentsToAttack(continents, whosKingThisPlayerIs));
            if (isPossibleToDisembark(ship)) {
                disembarkUnit(ship);
            }
        }
    }

    /**
     * Choose and sail toward the landing target of one man-of-war. The
     * remembered target stays while the Crown still knows it as a rebel coastal
     * colony with a legal landing; otherwise the nearest known one is chosen.
     * The route is searched once this turn. A turn without a route is counted,
     * and only after {@link RoyalRoster#BLOCKED_TURNS_BEFORE_RETARGET} such
     * turns the next nearest known target is tried; without one the ship keeps
     * its target and waits where it is.
     *
     * @return target chosen for this turn, empty when none is known
     */
    private Optional<Colony> sailTowardLanding(final UnitWithCargo ship,
            final RoyalRoster.Member member) {
        final List<Colony> known = knownLandingTargets();
        final Optional<Colony> remembered = member.getTargetColonyId()
                .flatMap(id -> getModel().getColonyById(id))
                .filter(known::contains);
        Optional<Colony> chosen = remembered.isPresent() ? remembered
                : nearestTarget(known, ship, Optional.empty());
        if (chosen.isEmpty()) {
            getModel().setRoyalTransportPlan(king, ship, Optional.empty(), 0);
            return Optional.empty();
        }
        if (ship.getLocation().isNeighbor(chosen.get().getLocation())
                && isPossibleToDisembark(ship)) {
            getModel().setRoyalTransportPlan(king, ship, chosen, 0);
            return chosen;
        }
        final Optional<List<Location>> route = routeToLanding(ship,
                chosen.get());
        if (route.isEmpty()) {
            int blockedTurns = member.getBlockedTurns() + 1;
            if (blockedTurns >= RoyalRoster.BLOCKED_TURNS_BEFORE_RETARGET) {
                final Optional<Colony> another = nearestTarget(known, ship,
                        chosen.map(Colony::getId));
                if (another.isPresent()) {
                    chosen = another;
                }
                blockedTurns = 0;
            }
            getModel().setRoyalTransportPlan(king, ship, chosen, blockedTurns);
            return chosen;
        }
        getModel().setRoyalTransportPlan(king, ship, chosen, 0);
        getModel().moveUnitAsFarAsPossible(ship, Path.of(route.get()));
        return chosen;
    }

    /**
     * Colonies that the Crown knows as held by the nation it rules, and that
     * have a legal landing beside them. Knowledge is the Crown's own: what its
     * units see now, what it saw before, and the colonies of its subjects that
     * it knew when they declared independence.
     */
    private List<Colony> knownLandingTargets() {
        return king.getKnownColonies().stream()
                .filter(sighting -> sighting.getOwner() == whosKingThisPlayerIs)
                .map(sighting -> getModel().getColonyAt(sighting.getLocation()))
                .flatMap(Optional::stream)
                .filter(colony -> !landingSeas(colony).isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * @param known    colonies the Crown may target
     * @param ship     required carrying man-of-war
     * @param excluded colony that must not be chosen, if any
     * @return the known colony nearest to the ship; ties go to the lower
     *         identity, then to the lower coordinates
     */
    private Optional<Colony> nearestTarget(final List<Colony> known,
            final UnitWithCargo ship, final Optional<Integer> excluded) {
        return known.stream()
                .filter(colony -> excluded.isEmpty()
                        || excluded.get().intValue() != colony.getId())
                .min(Comparator
                        .comparingInt((Colony colony) -> colony.getLocation()
                                .getDistance(ship.getLocation()))
                        .thenComparingInt(Colony::getId)
                        .thenComparingInt(colony -> colony.getLocation().getX())
                        .thenComparingInt(
                                colony -> colony.getLocation().getY()));
    }

    /**
     * Sea squares beside the colony with a legal landing square beyond them.
     */
    private List<Location> landingSeas(final Colony colony) {
        return colony.getLocation().getNeighbors().stream()
                .filter(location -> getModel().isValid(location) && getModel()
                        .getMap().getTerrainTypeAt(location).isSea())
                .filter(sea -> sea.getNeighbors().stream()
                        .anyMatch(this::isLegalLandingSquare))
                .collect(Collectors.toList());
    }

    /**
     * A ground unit may stand on this square as far as the Crown can tell:
     * walkable land with no known colony and no foreign unit in its sight. The
     * model still decides the actual landing.
     */
    private boolean isLegalLandingSquare(final Location location) {
        return getModel().isValid(location)
                && getModel().getMap().getTerrainTypeAt(location).isLand()
                && UnitType.COLONIST.canMoveAtTerrain(
                        getModel().getMap().getTerrainTypeAt(location))
                && king.getKnownColonyAt(location).isEmpty()
                && king.getObservedUnitsAt(location).stream()
                        .noneMatch(unit -> unit.getOwner() != king);
    }

    /**
     * @return shortest legal route to a sea square beside the colony; empty
     *         when none exists or the ship already stands at a landing square
     */
    private Optional<List<Location>> routeToLanding(final UnitWithCargo ship,
            final Colony colony) {
        final List<Location> seas = landingSeas(colony);
        if (seas.contains(ship.getLocation())) {
            return Optional.empty();
        }
        return seas.stream().map(sea -> ship.getPath(sea))
                .filter(Optional::isPresent).map(Optional::get).min(Comparator
                        .comparingInt((List<Location> path) -> path.size()));
    }

    /**
     * An empty carrier sails back to its sea lane and crosses to Europe, but
     * only while ground units wait in reserve that no other carrier on its way
     * home or already in port can take. It never leaves the legal sea route and
     * never crosses with nothing to fetch.
     */
    private void sailHomeForReserve(final UnitWithCargo ship) {
        if (!needsAnotherCarrier() || !(ship instanceof Ship)) {
            return;
        }
        final Optional<Location> lane = ((Ship) ship).getDepartureLocation();
        if (lane.isEmpty()) {
            return;
        }
        if (!ship.getLocation().equals(lane.get())) {
            ship.getPath(lane.get()).filter(route -> !route.isEmpty())
                    .ifPresent(route -> getModel().moveUnitAsFarAsPossible(ship,
                            Path.of(route)));
        }
        if (ship.isAtPlaceLocation() && ship.getLocation().equals(lane.get())
                && ship.getActionPoints() > 0
                && ship.getAction().isNoAction()) {
            ship.placeToHighSeas(true);
        }
    }

    /**
     * @return whether the reserve is larger than what the carriers already
     *         waiting in Europe or crossing toward it can load
     */
    private boolean needsAnotherCarrier() {
        final RoyalRoster roster = king.getRoyalRoster();
        // Only bound carriers count; an authored ship of the scenario is not
        // part of the roster and never fetches its reserve.
        final long reserve = roster.getMembers().stream()
                .filter(member -> member.getFamily().isGround()
                        && member.getState() == RoyalRoster.State.RESERVE)
                .count();
        final long homeward = king.getAllUnits().stream()
                .filter(unit -> unit.getType() == UnitType.MAN_OF_WAR
                        && roster.findByUnitId(unit.getId()).isPresent())
                .filter(unit -> unit.isAtEuropePort()
                        || unit.getPlace() instanceof PlaceHighSea
                                && ((PlaceHighSea) unit.getPlace())
                                        .isTravelToEurope())
                .count();
        return reserve > homeward * CARRIER_HOLDS;
    }

    private void performSeekAndDestroy(final Unit unit,
            final Continents continents) {
        final Optional<Location> oLoc = continentTargets
                .getClosesEnemyCityToAttack(
                        continents.getContinentWhereIsUnitPlaced(unit),
                        unit.getLocation(), whosKingThisPlayerIs);
        if (oLoc.isPresent()) {
            final Optional<List<Location>> oPath = unit.getPath(oLoc.get(),
                    true);
            if (oPath.isPresent() && !oPath.get().isEmpty()) {
                getModel().moveUnitAsFarAsPossible(unit, Path.of(oPath.get()));
            }
        }
        // A colony is taken only by winning its defense battle.
        simpleUnitBehavior.tryToFight(unit);
    }

    private void tryToReachSomeContinent(final Unit ship,
            final List<Continent> continentsToAttack) {
        final Optional<List<Location>> oPath = findFasterPathToContinent(ship,
                continentsToAttack.get(0));
        if (oPath.isPresent()) {
            getModel().moveUnitAsFarAsPossible(ship, Path.of(oPath.get()));
        }
    }

    private Optional<List<Location>> findFasterPathToContinent(final Unit ship,
            final Continent continent) {
        return continent.getLocations().stream()
                .map(loc -> ship.getPath(loc, true))
                .filter(oPath -> oPath.isPresent()).map(oPath -> oPath.get())
                .sorted((list1, list2) -> list1.size() - list2.size())
                .findFirst();
    }

    private void disembarkUnit(final UnitWithCargo ship) {
        final Location location = canDisembarkAt(ship).get(0);
        ship.getCargo().getSlots().forEach(slot -> {
            if (slot.isLoadedUnit()) {
                final Unit unit = slot.getUnit().get();
                if (unit.canUnitDisembarkAt(location)) {
                    unit.disembarkToLocation(location);
                }
            }
        });
    }

    private boolean isPossibleToDisembark(final UnitWithCargo ship) {
        return !canDisembarkAt(ship).isEmpty();
    }

    private List<Location> canDisembarkAt(final UnitWithCargo ship) {
        return ship.getLocation().getNeighbors().stream()
                .filter(loc -> getModel().isValid(loc))
                .filter(loc -> ship.isPossibleToDisembarkAt(loc, true))
                .collect(Collectors.toList());
    }

    /**
     * Royal Expedition Forces was send to colonies after declaring
     * independence.
     *
     * @return the refWasSend
     */
    public boolean isRefWasSend() {
        return Boolean
                .valueOf((String) getPlayer().getExtraData().get("refWasSend"));
    }

    /**
     * Royal Expedition Forces was send to colonies after declaring
     * independence.
     *
     * @param refWasSend the refWasSend to set
     */
    public void setRefWasSend(final boolean refWasSend) {
        getPlayer().getExtraData().put("refWasSend",
                String.valueOf(refWasSend));
    }
}
