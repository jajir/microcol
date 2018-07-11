package org.microcol.ai;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.microcol.gui.gamepanel.AnimationManager;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Path;
import org.microcol.model.Player;
import org.microcol.model.Unit;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public final class KingPlayer extends AbstractRobotPlayer {

    private final SimpleUnitBehavior simpleUnitBehavior = new SimpleUnitBehavior();

    private final Player whosKingThisPlayerIs;

    private final ContinentTool continentTool = new ContinentTool();

    private final static float KINGS_MILITARY_STREGTH_COEFICIENT = 2F / 3F;

    public KingPlayer(final Model model, final Player player,
            final AnimationManager animationManager) {
        super(model, player, animationManager);
        this.whosKingThisPlayerIs = Preconditions.checkNotNull(player.getWhosKingThisPlayerIs());
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
        if (!unit.getCargo().isEmpty()) {
            if (isPossibleToDisembark(unit)) {
                disembarkUnit(unit);
            } else {
                tryToReachSomeContinent(unit, continents.getContinentsToAttack());
                if (isPossibleToDisembark(unit)) {
                    disembarkUnit(unit);
                }
            }
        }
    }

    private void performSeekAndDestroy(final Unit unit, final Continents continents) {
        final Optional<Location> oLoc = continents.getContinentWhereIsUnitPlaced(unit)
                .getClosesEnemyCityToAttack(unit.getLocation());
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
            Continent continent) {
        return continent.getLocations().stream().map(loc -> ship.getPath(loc, true))
                .filter(oPath -> oPath.isPresent()).map(oPath -> oPath.get())
                .sorted((list1, list2) -> list1.size() - list2.size()).findFirst();
    }

    private void disembarkUnit(final Unit ship) {
        final Location location = canDisembartAt(ship).get(0);
        ship.getCargo().getSlots().forEach(slot -> {
            if (slot.isLoadedUnit()) {
                slot.unload(location);
            }
        });
    }

    private boolean isPossibleToDisembark(final Unit ship) {
        return !canDisembartAt(ship).isEmpty();
    }

    private List<Location> canDisembartAt(final Unit ship) {
        return ship.getLocation().getNeighbors().stream()
                .filter(loc -> ship.isPossibleToDisembarkAt(loc, true))
                .collect(Collectors.toList());
    }

    private void createRoyalArmyForces() {
        int militaryForceToDeploy = getKingsMilitaryStrength();
        Unit cargoShip = null;
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
        return (int) (whosKingThisPlayerIs.getPlayerStatistics().getMilitaryStrength()
                .getMilitaryStrength() * KINGS_MILITARY_STREGTH_COEFICIENT);
    }

    /**
     * Royal Expedition Forces was send to colonies after declaring independence.
     * 
     * @return the refWasSend
     */
    public boolean isRefWasSend() {
        return Boolean.valueOf((String) getPlayer().getExtraData().get("refWasSend"));
    }

    /**
     * Royal Expedition Forces was send to colonies after declaring independence.
     * 
     * @param refWasSend
     *            the refWasSend to set
     */
    public void setRefWasSend(boolean refWasSend) {
        getPlayer().getExtraData().put("refWasSend", String.valueOf(refWasSend));
    }
}
