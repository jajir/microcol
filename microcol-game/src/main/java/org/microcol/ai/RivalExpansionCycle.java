package org.microcol.ai;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.microcol.model.CargoSlot;
import org.microcol.model.Colony;
import org.microcol.model.ColonyJobChoice;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.MicroColException;
import org.microcol.model.Model;
import org.microcol.model.Path;
import org.microcol.model.Player;
import org.microcol.model.RivalExpansionObjective;
import org.microcol.model.RivalExpansionObjective.Stage;
import org.microcol.model.Unit;
import org.microcol.model.unit.Ship;
import org.microcol.model.unit.UnitFreeColonist;
import org.slf4j.LoggerFactory;

/**
 * A rival's single new-colony expansion at a time. A rival with one colony may
 * start the second one; with more colonies a further one needs every colony to
 * meet the reserves (see {@link #hasNewColonyReserves}). A spare unarmed
 * colonist is released onto the colony square when the residents, food and
 * transport allow it. When a known legal site is reachable by land, the founder
 * walks there. Otherwise a carrier in the colony port takes the founder to a
 * known legal site by sea. The founder founds once it stands on the site with
 * movement. The objective is saved in the player. Every step revalidates live
 * ownership, placement and legality through the ordinary model operations. A
 * refused step waits for the next owner turn, and a stale participant retires
 * the objective.
 */
final class RivalExpansionCycle {

    /** Residents that stay in the colony after the founder leaves. */
    private static final int MINIMUM_RESIDENTS = 2;

    /** Food in stock must cover this many turns of the remaining residents. */
    private static final int FOOD_TURNS = 5;

    /**
     * Each pass boards, moves, or founds, so this is a safety bound.
     */
    private static final int MAX_PASSES = 64;

    private final AbstractRobotPlayer robot;

    private final RivalTradeCycle rivalTrade;

    /**
     * Carrier kept in the colony port for this owner turn: a known sea site
     * exists, but the reserves do not allow a release yet. Without it the
     * ordinary unit moves sail the idle ship away, and a later turn whose
     * reserves allow the release finds no transport in port. It is chosen again
     * at every turn start, so it is not saved.
     */
    private Integer heldCarrierId;

    RivalExpansionCycle(final AbstractRobotPlayer robot,
            final RivalTradeCycle rivalTrade) {
        this.robot = robot;
        this.rivalTrade = rivalTrade;
    }

    /**
     * @param unit required unit of this robot
     * @return whether the unit is the founder or carrier of the active
     *         expansion, so the ordinary unit loop and workforce leave it alone
     */
    boolean isParticipant(final Unit unit) {
        return player().getRivalExpansionObjective()
                .filter(objective -> objective.getFounderId() == unit.getId()
                        || objective.getCarrierId()
                                .filter(id -> id == unit.getId()).isPresent())
                .isPresent();
    }

    /**
     * One owner-turn pass: retire a stale expansion, release a founder when
     * none is active, then advance while each pass makes progress.
     */
    void advance() {
        heldCarrierId = null;
        if (player().getRivalExpansionObjective().isPresent()
                && resolve().isEmpty()) {
            player().clearRivalExpansionObjective();
        }
        if (player().getRivalExpansionObjective().isEmpty()) {
            prepare();
        }
        boolean progressed = true;
        for (int pass = 0; pass < MAX_PASSES && progressed
                && robot.isActiveTurn(); pass++) {
            progressed = advanceOnce();
        }
    }

    private Model model() {
        return robot.getModel();
    }

    private Player player() {
        return robot.getPlayer();
    }

    /**
     * Live colony, founder and, for a sea journey, carrier of the saved
     * expansion; empty when any of them is gone or no longer belongs to this
     * player.
     */
    private Optional<Leg> resolve() {
        final Optional<RivalExpansionObjective> objective = player()
                .getRivalExpansionObjective();
        if (objective.isEmpty()) {
            return Optional.empty();
        }
        final Optional<Colony> colony = model()
                .getColonyById(objective.get().getColonyId())
                .filter(Colony::isValid)
                .filter(candidate -> candidate.getOwner() == player());
        final Optional<Unit> founder = model()
                .tryGetUnitById(objective.get().getFounderId())
                .filter(unit -> unit.getOwner() == player())
                .filter(unit -> unit.getType().canBuildColony());
        final Optional<Ship> carrier = objective.get().getCarrierId()
                .flatMap(id -> model().tryGetUnitById(id)
                        .filter(unit -> unit instanceof Ship)
                        .filter(unit -> unit.getOwner() == player())
                        .map(Ship.class::cast));
        if (colony.isEmpty() || founder.isEmpty()
                || objective.get().getCarrierId().isPresent()
                        && carrier.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(
                new Leg(objective.get(), colony.get(), founder.get(), carrier));
    }

    /**
     * Release one spare founder from a colony. The first colony in ID order
     * with spare founders takes the turn. A second colony, or a later one,
     * needs every colony to meet the reserves first, so the rival never founds
     * beside a colony that is short of food or tools. A known legal site that
     * the founder can walk to comes first, because it needs no transport. Only
     * otherwise does an idle carrier in the colony port take the founder by
     * sea. Nothing is released when any part is missing, so the rival waits
     * finitely without weakening its colonies.
     */
    private void prepare() {
        if (!robot.isActiveTurn()) {
            return;
        }
        final List<Colony> colonies = model().getColonies(player()).stream()
                .sorted(Comparator.comparingInt(Colony::getId))
                .collect(Collectors.toList());
        if (colonies.isEmpty()
                || !colonies.stream().allMatch(Colony::isValid)) {
            return;
        }
        if (colonies.size() > 1
                && !colonies.stream().allMatch(this::hasNewColonyReserves)) {
            return;
        }
        for (final Colony colony : colonies) {
            final List<Unit> founders = spareFounders(colony);
            if (!founders.isEmpty() && prepareFrom(colony, founders)) {
                // A transport held for a refused colony is not needed now.
                heldCarrierId = null;
                return;
            }
        }
    }

    /**
     * Every existing colony keeps a nonnegative food balance, five turns of its
     * residents' food in stock, and its tool target met with the tools aboard
     * the active carrier counted once.
     *
     * @param colony required colony of this robot
     * @return whether the colony supports one more colony
     */
    boolean hasNewColonyReserves(final Colony colony) {
        final int consumption = colony.getUnitsInsideColony().stream()
                .mapToInt(unit -> unit.getType().getAteFoodPerTurn()).sum();
        final int net = colony.getGoodsStats().getStatsByType(GoodsType.CORN)
                .getNetProduction();
        return net >= 0
                && colony.getWarehouse().getGoods(GoodsType.CORN)
                        .getAmount() >= FOOD_TURNS * consumption
                && rivalTrade.isToolSupplyMet(colony);
    }

    /**
     * Starts a release from one colony with spare founders.
     *
     * @return whether this colony took the turn, by walking or by a carrier in
     *         its port; false when no site or transport exists
     */
    private boolean prepareFrom(final Colony colony,
            final List<Unit> founders) {
        final Unit probe = founders.get(0);
        final Optional<Location> walkSite = FirstColonyPlanner
                .chooseSite(model(), probe, probe, colony.getLocation());
        if (walkSite.isPresent()) {
            return release(colony, founders, Optional.empty(), walkSite.get());
        }
        final Optional<Ship> carrier = model().getUnitsAt(colony.getLocation())
                .stream().filter(unit -> unit instanceof Ship)
                .map(Ship.class::cast).filter(this::isEligibleCarrier)
                .min(Comparator.comparingInt(Ship::getId));
        if (carrier.isEmpty()) {
            return false;
        }
        final Optional<Location> site = FirstColonyPlanner.chooseSite(model(),
                probe, carrier.get(), carrier.get().getLocation());
        if (site.isEmpty()) {
            return false;
        }
        if (release(colony, founders, carrier, site.get())) {
            return true;
        }
        // The reserves refused this colony; keep its transport ready, and let
        // the next colony in ID order try its own founders and transport.
        heldCarrierId = carrier.get().getId();
        return false;
    }

    /**
     * @param unit required unit of this robot
     * @return whether the unit is the carrier kept in the colony port for this
     *         turn, so the ordinary unit loop leaves it there
     */
    boolean isHeldCarrier(final Unit unit) {
        return heldCarrierId != null && heldCarrierId == unit.getId();
    }

    /**
     * Places the first founder whose release passes the reserves. A walking
     * journey starts travelling at once; a sea journey waits to board.
     */
    /**
     * Places the first founder whose release passes the reserves. A walking
     * journey starts travelling at once; a sea journey waits to board. A
     * carrier taken from the parked ones leaves the supply rotation.
     *
     * @return whether a founder was released
     */
    private boolean release(final Colony colony, final List<Unit> founders,
            final Optional<Ship> carrier, final Location site) {
        for (final Unit founder : founders) {
            if (!canRelease(colony, founder)) {
                continue;
            }
            try {
                founder.placeToMap(colony.getLocation());
            } catch (IllegalArgumentException | IllegalStateException
                    | MicroColException refused) {
                // The shared rules refused the release; nothing changed.
                LoggerFactory.getLogger(RivalExpansionCycle.class)
                        .debug("Rival founder release was refused", refused);
                return false;
            }
            player().setRivalExpansionObjective(new RivalExpansionObjective(
                    colony.getId(), founder.getId(),
                    carrier.map(Ship::getId).orElse(null), site,
                    carrier.isPresent() ? Stage.BOARDING : Stage.TRAVEL));
            carrier.ifPresent(
                    ship -> player().unparkRivalCarrier(ship.getId()));
            return true;
        }
        return false;
    }

    /**
     * Own idle civilian residents that are not teaching and carry no guns or
     * mounts. Residents who do not produce food come first, then identity.
     */
    private List<Unit> spareFounders(final Colony colony) {
        final Set<Integer> teaching = new HashSet<>();
        colony.getEducationCourses().forEach(course -> {
            teaching.add(course.getStudentId());
            if (course.getTeacherId() != null) {
                teaching.add(course.getTeacherId());
            }
        });
        return colony.getUnitsInsideColony().stream()
                .filter(unit -> unit instanceof UnitFreeColonist
                        && unit.getOwner() == player()
                        && unit.getAction().isNoAction()
                        && !((UnitFreeColonist) unit).isHoldingGuns()
                        && !((UnitFreeColonist) unit).isMounted()
                        && !teaching.contains(unit.getId())
                        && unit.getType().canBuildColony())
                .sorted(Comparator
                        .comparing((Unit unit) -> producesFood(unit) ? 1 : 0)
                        .thenComparingInt(Unit::getId))
                .collect(Collectors.toList());
    }

    private static boolean producesFood(final Unit unit) {
        return unit.isAtPlaceColonyField() && unit.getPlaceColonyField()
                .getProducedGoodsType() == GoodsType.CORN;
    }

    /**
     * Reserves after the founder leaves: two residents or the fortified minimum
     * from the colony rules, a nonnegative food balance, and food in stock for
     * five turns of the remaining residents.
     */
    private boolean canRelease(final Colony colony, final Unit founder) {
        final List<Unit> residents = colony.getUnitsInsideColony();
        if (residents.size() - 1 < MINIMUM_RESIDENTS
                || !founder.canLeaveColony()) {
            return false;
        }
        // The offers list a current-field corn choice for any field that could
        // grow corn, so only a founder that grows corn takes food away.
        final int produced = !producesFood(founder) ? 0
                : colony.getJobChoices(founder).stream()
                        .filter(choice -> choice
                                .getKind() == ColonyJobChoice.Kind.CURRENT_FIELD
                                && choice.getGoodsType() == GoodsType.CORN)
                        .mapToInt(choice -> choice.getPotential()
                                .map(Goods::getAmount).orElse(0))
                        .sum();
        final int net = colony.getGoodsStats().getStatsByType(GoodsType.CORN)
                .getNetProduction();
        if (net - produced + founder.getType().getAteFoodPerTurn() < 0) {
            return false;
        }
        final int remainingFood = residents.stream()
                .filter(unit -> unit != founder)
                .mapToInt(unit -> unit.getType().getAteFoodPerTurn()).sum();
        return colony.getWarehouse().getGoods(GoodsType.CORN)
                .getAmount() >= FOOD_TURNS * remainingFood;
    }

    /** Own ship in the colony port, idle, undamaged, empty and not trading. */
    private boolean isEligibleCarrier(final Ship ship) {
        return ship.getOwner() == player() && ship.isAtPlaceLocation()
                && ship.getAction().isNoAction() && !ship.isDamaged()
                && !rivalTrade.isCarrier(ship) && ship.getCargo().getSlots()
                        .stream().allMatch(CargoSlot::isEmpty);
    }

    /** @return whether this pass boarded, moved or founded */
    private boolean advanceOnce() {
        final Optional<Leg> resolved = resolve();
        if (resolved.isEmpty()) {
            player().clearRivalExpansionObjective();
            return false;
        }
        final Leg leg = resolved.get();
        if (leg.objective.getStage() == Stage.BOARDING) {
            return board(leg);
        }
        return travel(leg);
    }

    /**
     * The released founder steps into the carrier waiting in the same port.
     * Anything else retires the expansion, so the founder returns to ordinary
     * orders on a later turn.
     */
    private boolean board(final Leg leg) {
        final Unit founder = leg.founder;
        if (leg.carrier.isEmpty()) {
            player().clearRivalExpansionObjective();
            return false;
        }
        final Ship carrier = leg.carrier.get();
        final Location port = leg.colony.getLocation();
        if (founder.isAtCargoSlot()) {
            if (founder.getPlaceCargoSlot().getOwnerUnit() == carrier) {
                setStage(leg, Stage.TRAVEL);
                return true;
            }
            player().clearRivalExpansionObjective();
            return false;
        }
        if (!founder.isAtPlaceLocation() || !founder.getLocation().equals(port)
                || !carrier.isAtPlaceLocation()
                || !carrier.getLocation().equals(port)) {
            player().clearRivalExpansionObjective();
            return false;
        }
        final Optional<CargoSlot> slot = carrier.getCargo().getSlots().stream()
                .filter(founder::canEmbark).findFirst();
        if (slot.isEmpty()) {
            player().clearRivalExpansionObjective();
            return false;
        }
        try {
            founder.embarkFromLocation(slot.get());
        } catch (IllegalArgumentException | IllegalStateException
                | MicroColException refused) {
            LoggerFactory.getLogger(RivalExpansionCycle.class)
                    .debug("Rival founder boarding was refused", refused);
            player().clearRivalExpansionObjective();
            return false;
        }
        setStage(leg, Stage.TRAVEL);
        return true;
    }

    /**
     * One ordinary step toward the saved site. A founder aboard the carrier
     * steps ashore onto land; a founder on land walks, with or without a ship.
     * A blocked route is recovered by {@link #replan}.
     */
    private boolean travel(final Leg leg) {
        final Unit founder = leg.founder;
        final Location site = leg.objective.getSite();
        if (founder.isAtCargoSlot() && leg.carrier.filter(
                ship -> founder.getPlaceCargoSlot().getOwnerUnit() == ship)
                .isEmpty()) {
            player().clearRivalExpansionObjective();
            return false;
        }
        if (founder.isAtPlaceLocation() && founder.getLocation().equals(site)) {
            return found(leg);
        }
        final Unit mover = founder.isAtCargoSlot() ? leg.carrier.get()
                : founder;
        if (!mover.isAtPlaceLocation() || !mover.getAction().isNoAction()) {
            return false;
        }
        final Optional<Location> step = FirstColonyPlanner
                .nextStepToSite(model(), founder, mover, site);
        if (step.isEmpty()) {
            return replan(leg, mover);
        }
        final Location target = step.get();
        if (founder.isAtCargoSlot()
                && model().getMap().getTerrainTypeAt(target).isLand()) {
            if (!founder.canUnitDisembarkAt(target)) {
                return false;
            }
            try {
                founder.disembarkToLocation(target);
            } catch (IllegalArgumentException | IllegalStateException
                    | MicroColException refused) {
                LoggerFactory.getLogger(RivalExpansionCycle.class)
                        .debug("Rival founder landing was refused", refused);
                return false;
            }
            robot.waitForAnimations();
            return true;
        }
        if (!mover.canAffordStepTo(target)) {
            return false;
        }
        return move(mover, target);
    }

    /**
     * No known route reaches the saved site. Another known legal site that the
     * mover reaches now replaces it, with the same founder and carrier. A site
     * that is no longer legal, with no replacement, retires the expansion.
     * Otherwise the route is only blocked, for example by a foreign ship in a
     * channel: the objective, founder and carrier wait unchanged and the next
     * owner turn tries again.
     *
     * @return whether the site changed, so the next pass steps toward it
     */
    private boolean replan(final Leg leg, final Unit mover) {
        final Location site = leg.objective.getSite();
        final Optional<Location> other = FirstColonyPlanner.chooseSite(model(),
                leg.founder, mover, mover.getLocation());
        if (other.isPresent() && !other.get().equals(site)) {
            player().setRivalExpansionObjective(
                    leg.objective.withSite(other.get()));
            return true;
        }
        if (!model().canFoundColonyAt(leg.founder, site)) {
            player().clearRivalExpansionObjective();
        }
        return false;
    }

    private boolean move(final Unit mover, final Location step) {
        try {
            model().moveUnit(mover, Path.of(List.of(step)));
        } catch (IllegalArgumentException | IllegalStateException
                | MicroColException refused) {
            // The shared rules refused the step; nothing moved.
            LoggerFactory.getLogger(RivalExpansionCycle.class)
                    .debug("Rival expansion step was refused", refused);
            return false;
        }
        robot.waitForAnimations();
        return true;
    }

    /**
     * The founder on the site founds once it has movement left and the site is
     * still legal. A site that became illegal retires the expansion.
     */
    private boolean found(final Leg leg) {
        final Unit founder = leg.founder;
        final Location site = leg.objective.getSite();
        if (!model().canFoundColonyAt(founder, site)) {
            player().clearRivalExpansionObjective();
            return false;
        }
        if (!model().canUnitBuildColony(founder)) {
            return false;
        }
        final Set<String> names = model().getColonies().stream()
                .map(Colony::getName).collect(Collectors.toSet());
        try {
            model().buildColony(player(), founder,
                    model().suggestColonyName(names));
        } catch (IllegalArgumentException | IllegalStateException
                | MicroColException refused) {
            LoggerFactory.getLogger(RivalExpansionCycle.class)
                    .debug("Rival colony founding was refused", refused);
        }
        player().clearRivalExpansionObjective();
        return false;
    }

    private void setStage(final Leg leg, final Stage stage) {
        player().setRivalExpansionObjective(leg.objective.withStage(stage));
    }

    /**
     * The saved expansion with its live colony, founder and, for a sea journey,
     * carrier.
     */
    private static final class Leg {
        private final RivalExpansionObjective objective;
        private final Colony colony;
        private final Unit founder;
        private final Optional<Ship> carrier;

        private Leg(final RivalExpansionObjective objective,
                final Colony colony, final Unit founder,
                final Optional<Ship> carrier) {
            this.objective = objective;
            this.colony = colony;
            this.founder = founder;
            this.carrier = carrier;
        }
    }
}
