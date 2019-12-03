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
public abstract class Player {

    public static final int CANT_DECLARE_INDEPENDENCE_BEFORE_YEAR = 1700;

    public static final int REQUIRED_NUMBER_OF_COLONYES = 3;

    private final Model model;

    private final String name;

    private final boolean computer;

    private boolean declaredIndependence;

    private int gold;

    private final Map<String, Object> extraData = new HashMap<>();

    private final Visibility visibility;

    protected Player(final String name, final boolean computer, final int initialGold,
            final Model model, final boolean declaredIndependence,
            final Map<String, Object> extraData, Set<Location> visible) {
        this.model = Preconditions.checkNotNull(model);
        this.name = Preconditions.checkNotNull(name);
        this.computer = computer;
        this.gold = initialGold;
        this.declaredIndependence = declaredIndependence;
        this.extraData.putAll(Preconditions.checkNotNull(extraData));
        this.visibility = new Visibility(Preconditions.checkNotNull(visible, "Visible is null"));
    }

    public static Player make(final PlayerPo playerPo, final Model model,
            final PlayerStore playerStore) {
        Preconditions.checkNotNull(playerPo.getVisible(), "Visible is null during creating '%s'",
                playerPo.getName());
        if (playerPo.getWhosKingThisPlayerIs() == null) {
            return new PlayerHuman(playerPo.getName(), playerPo.isComputer(), playerPo.getGold(),
                    model, playerPo.isDeclaredIndependence(), playerPo.getExtraData(),
                    playerPo.getVisible().getVisibilitySet());
        } else {
            final Player subdued = playerStore.getPlayerByName(playerPo.getWhosKingThisPlayerIs());
            return new PlayerKing(playerPo.getName(), playerPo.isComputer(), playerPo.getGold(),
                    model, playerPo.isDeclaredIndependence(), subdued, playerPo.getExtraData(),
                    playerPo.getVisible().getVisibilitySet(), playerPo.getKingsTaxPercentage());
        }
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
    public void revealMapForUnit(final Unit unit) {
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
        visibility.store(out.getVisible(), model.getMap().getMaxLocationX(), model.getMap().getMaxLocationY());
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
        return this instanceof PlayerKing;
    }

    public List<Unit> getUnits() {
        return model.getUnitsOwnedBy(this, false);
    }

    public List<Unit> getAllUnits() {
        return model.getUnitsOwnedBy(this, true);
    }

    public int getNumberOfMilitaryUnits() {
        return (int) getAllUnits().stream().filter(unit -> unit.getType().canAttack()).count();
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

        final Player player = (Player) object;

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
        Preconditions.checkArgument(newGoldValue >= 0, "New gold value '%s' is less than zero",
                newGoldValue);
        final int oldValue = gold;
        this.gold = newGoldValue;
        model.fireGoldWasChanged(this, oldValue, newGoldValue);
    }

    public void buyGoods(final Goods goods) {
        Preconditions.checkNotNull(goods);
        float initialPrice = goods.getAmount()
                * model.getEurope().getGoodsTradeForType(goods.getType()).getBuyPrice();
        int price = (int) (initialPrice + getKingsTaxFor(initialPrice));
        verifyAvailibilityOFGold(price);
        setGold(getGold() - price);
    }

    public void sellGoods(final Goods goods) {
        Preconditions.checkNotNull(goods);
        float initialPrice = goods.getAmount()
                * model.getEurope().getGoodsTradeForType(goods.getType()).getSellPrice();
        int price = (int) (initialPrice - getKingsTaxFor(initialPrice));
        setGold(getGold() + price);
    }

    private float getKingsTaxFor(final float initialPrice) {
        return model.getKingsTaxForPlayer(this) * initialPrice / 100F;
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
        Preconditions.checkState(isPossibleToDecalareIndependence(),
                "It's not possible to declare independence");
        if (model.fireBeforeDeclaringIndependence(this)) {
            declaredIndependence = true;
            model.fireIndependenceWasDeclared(this);
        }
    }

    public boolean isPossibleToDecalareIndependence() {
        if (isDeclaredIndependence()) {
            // Already was declared.
            return false;
        }
        if (model.getCalendar().getCurrentYear() < CANT_DECLARE_INDEPENDENCE_BEFORE_YEAR) {
            // Too early to declare independence.
            return false;
        }
        if (model.getColonies(this).size() < REQUIRED_NUMBER_OF_COLONYES) {
            // Not enough colonies.
            return false;
        }
        return true;
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
