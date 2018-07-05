package org.microcol.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.VisibilityPo;
import org.microcol.model.turnevent.TurnEventProvider;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Player model class. Class doesn't perform itself it just provide methods to
 * implement players behavior somewhere else.
 */
public final class Player {

    private final Model model;

    private final String name;

    private final boolean computer;

    /**
     * If it's not null than it's king player.
     */
    private final Player whosKingThisPlayerIs;

    private boolean declaredIndependence;

    private int gold;

    private final Map<String, Object> extraData = new HashMap<>();

    private final Visibility visibility;

    private Player(final String name, final boolean computer, final int initialGold,
            final Model model, final boolean declaredIndependence,
            final Player whosKingThisPlayerIs, final Map<String, Object> extraData,
            Set<Location> visible) {
        this.model = Preconditions.checkNotNull(model);
        this.name = Preconditions.checkNotNull(name);
        this.computer = computer;
        this.gold = initialGold;
        this.declaredIndependence = declaredIndependence;
        this.whosKingThisPlayerIs = whosKingThisPlayerIs;
        this.extraData.putAll(Preconditions.checkNotNull(extraData));
        this.visibility = new Visibility(Preconditions.checkNotNull(visible, "Visible is null"));
    }

    public static Player make(final PlayerPo player, final Model model,
            final PlayerStore playerStore) {
        Player subdued = null;
        if (player.getWhosKingThisPlayerIs() != null) {
            subdued = playerStore.getPlayerByName(player.getWhosKingThisPlayerIs());
        }
        Preconditions.checkNotNull(player.getVisible(), "Visible is null during creating '%s'",
                player.getName());
        return new Player(player.getName(), player.isComputer(), player.getGold(), model,
                player.isDeclaredIndependence(), subdued, player.getExtraData(),
                player.getVisible().getVisibilitySet());
    }

    public boolean isVisible(final Location location) {
        model.isValid(location);
        return visibility.isVisible(location);
    }

    /**
     * Method reveals map visible for given unit.
     * 
     * @param unit
     *            required unit. Unit have to be on map.
     */
    void revealMapForUnit(final Unit unit) {
        Preconditions.checkNotNull(unit, "Unit is null");
        Preconditions.checkState(unit.getOwner().equals(this),
                "Unit's owher '%s' is to same as '%s'", unit.getOwner(), this);
        visibility.makeVisibleMapForUnit(unit);
    }

    public PlayerPo save() {
        final PlayerPo out = new PlayerPo();
        out.setName(name);
        out.setComputer(computer);
        out.setGold(gold);
        out.setDeclaredIndependence(declaredIndependence);
        out.getExtraData().putAll(extraData);
        out.setVisible(new VisibilityPo());
        visibility.store(out.getVisible(), model.getMap().getMaxX(), model.getMap().getMaxY());
        return out;
    }

    public String getName() {
        return name;
    }

    public boolean isComputer() {
        return computer;
    }

    public boolean isHuman() {
        return !computer;
    }

    public boolean isKing() {
        return whosKingThisPlayerIs != null;
    }

    public List<Unit> getUnits() {
        return model.getUnitsOwnedBy(this, false);
    }

    public List<Unit> getAllUnits() {
        return model.getUnitsOwnedBy(this, true);
    }

    public Map<Location, List<Unit>> getUnitsAt() {
        return model.getUnitsAt(this);
    }

    public List<Unit> getUnitsAt(final Location location) {
        return model.getUnitsAt(this, location);
    }

    public Optional<Colony> getColoniesAt(final Location location) {
        return model.getColoniesAt(location, this);
    }

    public List<Colony> getColonies() {
        return model.getColonies(this);
    }

    public List<Unit> getEnemyUnits() {
        return model.getEnemyUnits(this, false);
    }

    public Map<Location, List<Unit>> getEnemyUnitsAt() {
        return model.getEnemyUnitsAt(this);
    }

    public List<Unit> getEnemyUnitsAt(final Location location) {
        return model.getEnemyUnitsAt(this, location);
    }

    void startTurn() {
        getAllUnits().forEach(unit -> unit.startTurn());
        getColonies().forEach(colony -> colony.startTurn());
        if (isHuman()) {
            optionallyAddNewUnitInEuropePort();
        }
    }

    private void optionallyAddNewUnitInEuropePort() {
        final int turnNo = model.getCalendar().getNumberOfPlayedTurns();
        if (turnNo > 6 && turnNo <= 70 && turnNo % 11 == 0) {
            // add free colonist to Europe port.
            model.addUnitInEurope(UnitType.COLONIST, this);
            model.getTurnEventStore().add(TurnEventProvider.getNewUnitInEurope(this));
        }
    }

    public void endTurn() {
        model.checkGameRunning();
        model.checkCurrentPlayer(this);
        model.endTurn();
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }

        if (!(object instanceof Player)) {
            return false;
        }

        Player player = (Player) object;

        return name.equals(player.name) && computer == player.computer;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("name", name).add("computer", computer)
                .toString();
    }

    /**
     * Get information if it's possible to sail to given location. Method verify
     * that given location is empty sea or sea occupied by player's ships.
     * 
     * @param target
     *            required target location
     * @return return <code>true</code> when it's possible to sail at given
     *         location otherwise return <code>false</code>.
     */
    public boolean isPossibleToSailAt(final Location target) {
        final TerrainType t = model.getMap().getTerrainTypeAt(target);
        if (t == TerrainType.OCEAN) {
            return isItPlayersUnits(model.getUnitsAt(target));
        } else {
            return false;
        }
    }

    /**
     * Find if list of units could belong to this user.
     * 
     * @param units
     *            required list of units
     * @return return <code>false</code> when list contains at least one unit
     *         which not belongs to this player otherwise return
     *         <code>true</code>.
     */
    public boolean isItPlayersUnits(final List<Unit> units) {
        return !units.stream().filter(unit -> !unit.getOwner().equals(this)).findAny().isPresent();
    }

    public int getGold() {
        return gold;
    }

    public void setGold(final int newGoldValue) {
        final int oldValue = gold;
        this.gold = newGoldValue;
        model.fireGoldWasChanged(this, oldValue, newGoldValue);
    }

    public void buy(final GoodsAmount goodAmount) {
        int price = goodAmount.getAmount()
                * model.getEurope().getGoodTradeForType(goodAmount.getGoodType()).getBuyPrice();
        verifyAvailibilityOFGold(price);
        setGold(getGold() - price);
    }

    public void sell(final GoodsAmount goodAmount) {
        int price = goodAmount.getAmount()
                * model.getEurope().getGoodTradeForType(goodAmount.getGoodType()).getBuyPrice();
        setGold(getGold() + price);
    }

    public void buy(final UnitType unitType) {
        int price = unitType.getEuropePrice();
        verifyAvailibilityOFGold(price);
        setGold(getGold() - price);
        model.addUnitInEurope(unitType, this);
    }

    private void verifyAvailibilityOFGold(final int price) {
        if (getGold() - price < 0) {
            throw new NotEnoughtGoldException(String.format(
                    "You can't buy this item. You need %s and you have %s", price, getGold()));
        }
    }

    /**
     * @return the declaredIndependence
     */
    public boolean isDeclaredIndependence() {
        return declaredIndependence;
    }

    public void declareIndependence() {
        Preconditions.checkState(!declaredIndependence, "Independence was already declared");
        if (model.fireBeforeDeclaringIndependence(this)) {
            declaredIndependence = true;
            model.fireIndependenceWasDeclared(this);
        }
    }

    /**
     * @return the whosKingThisPlayerIs
     */
    public Player getWhosKingThisPlayerIs() {
        return whosKingThisPlayerIs;
    }

    /**
     * @return the extraData
     */
    public Map<String, Object> getExtraData() {
        return extraData;
    }

    public PlayerStatistics getPlayerStatistics() {
        final PlayerStatistics out = new PlayerStatistics(getGold());

        model.getColonies(this).forEach(out.getGoodsStatistics()::addColonyData);
        model.getUnitsOwnedBy(this, true).forEach(out.getGoodsStatistics()::addUnitData);
        model.getUnitsOwnedBy(this, true).forEach(out.getMilitaryStrength()::addUnitData);

        return out;
    }
}
