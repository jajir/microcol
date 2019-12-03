package org.microcol.ai;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.microcol.gui.screen.game.gamepanel.AnimationManager;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Path;
import org.microcol.model.Player;
import org.microcol.model.PlayerKing;
import org.microcol.model.Unit;
import org.microcol.model.unit.UnitWithCargo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public final class KingRobotPlayer extends AbstractRobotPlayer {

    private final static float KINGS_MILITARY_STREGTH_COEFICIENT = 2F / 3F;

    private final static float KINGS_MILITARY_STREGTH_CAP = 15;

    private final static float KINGS_MILITARY_STREGTH_GAME_OVER = 5;

    private final SimpleUnitBehavior simpleUnitBehavior = new SimpleUnitBehavior();

    private final Player whosKingThisPlayerIs;

    private final ContinentTool continentTool = new ContinentTool();

    public KingRobotPlayer(final Model model, final PlayerKing playerKing,
            final AnimationManager animationManager) {
        super(model, playerKing, animationManager);
        this.whosKingThisPlayerIs = Preconditions.checkNotNull(playerKing.getWhosKingThisPlayerIs());
    }

    private boolean isKinngCoquered() {
        if (isRefWasSend()) {
            final int miltaryStreng = getPlayer().getPlayerStatistics().getMilitaryStrength()
                    .getMilitaryStrength();
            return miltaryStreng < KINGS_MILITARY_STREGTH_GAME_OVER;
        }
        return false;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                .add("whosKingThisPlayerIs", whosKingThisPlayerIs).toString();
    }

    @Override
    protected void turnStarted() {
        if (whosKingThisPlayerIs.isDeclaredIndependence() && !isRefWasSend()) {
            createRoyalArmyForces();
            setRefWasSend(true);
        }
        if (isKinngCoquered()) {
            getPlayer().getExtraData().put("kingWasConquered", Boolean.TRUE.toString());
        }
    }

    @Override
    void moveUnit(final Unit unit) {
        final Continents continents = continentTool.findContinents(getModel(),
                whosKingThisPlayerIs);
        if (unit.isAtPlaceLocation()) {
            if (unit.getType().isShip()) {
                performMoveUnit(unit, continents);
            } else {
                if (unit.isAtPlaceLocation()) {
                    performSeekAndDestroy(unit, continents);
                }
            }
        }
    }

    private void performMoveUnit(final Unit unit, final Continents continents) {
        if (unit.canHoldCargo()) {
            final UnitWithCargo unitWithCargo = (UnitWithCargo) unit;
            if (!unitWithCargo.getCargo().isEmpty()) {
                if (isPossibleToDisembark(unitWithCargo)) {
                    disembarkUnit(unitWithCargo);
                } else {
                    tryToReachSomeContinent(unitWithCargo,
                            continents.getContinentsToAttack(whosKingThisPlayerIs));
                    if (isPossibleToDisembark(unitWithCargo)) {
                        disembarkUnit(unitWithCargo);
                    }
                }
            }
        }
    }

    private void performSeekAndDestroy(final Unit unit, final Continents continents) {
        final Optional<Location> oLoc = continents.getContinentWhereIsUnitPlaced(unit)
                .getClosesEnemyCityToAttack(unit.getLocation(), whosKingThisPlayerIs);
        if (oLoc.isPresent()) {
            final Optional<List<Location>> oPath = unit.getPath(oLoc.get(), true);
            if (oPath.isPresent() && !oPath.get().isEmpty()) {
                getModel().moveUnitAsFarAsPossible(unit, Path.of(oPath.get()));
            } else {
                simpleUnitBehavior.tryToCaptureColony(getModel(), unit, oLoc.get());
            }
        }
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
        return continent.getLocations().stream().map(loc -> ship.getPath(loc, true))
                .filter(oPath -> oPath.isPresent()).map(oPath -> oPath.get())
                .sorted((list1, list2) -> list1.size() - list2.size()).findFirst();
    }

    private void disembarkUnit(final UnitWithCargo ship) {
        final Location location = canDisembarkAt(ship).get(0);
        ship.getCargo().getSlots().forEach(slot -> {
            if (slot.isLoadedUnit()) {
                final Unit unit = slot.getUnit().get();
                unit.disembarkToLocation(location);
//                slot.disembark(location);
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

    private void createRoyalArmyForces() {
        int militaryForceToDeploy = getKingsMilitaryStrength();
        UnitWithCargo cargoShip = null;
        while (militaryForceToDeploy > 0) {
            // create unit, is there ship to load it in? if no, crate new and
            // load it
            if (cargoShip == null || cargoShip.getCargo().isFull()) {
                cargoShip = getModel().createCargoShipForKing(getPlayer());
            }
            getModel().createRoyalExpeditionForceUnit(getPlayer(), cargoShip);
            militaryForceToDeploy--;
        }
    }

    private int getKingsMilitaryStrength() {
        return (int) Math.min(
                (whosKingThisPlayerIs.getPlayerStatistics().getMilitaryStrength()
                        .getMilitaryStrength() * KINGS_MILITARY_STREGTH_COEFICIENT),
                KINGS_MILITARY_STREGTH_CAP);
    }

    /**
     * Royal Expedition Forces was send to colonies after declaring
     * independence.
     * 
     * @return the refWasSend
     */
    public boolean isRefWasSend() {
        return Boolean.valueOf((String) getPlayer().getExtraData().get("refWasSend"));
    }

    /**
     * Royal Expedition Forces was send to colonies after declaring
     * independence.
     * 
     * @param refWasSend
     *            the refWasSend to set
     */
    public void setRefWasSend(final boolean refWasSend) {
        getPlayer().getExtraData().put("refWasSend", String.valueOf(refWasSend));
    }
}
