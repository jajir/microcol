package org.microcol.model;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.microcol.model.store.PlayerPo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public final class Player {
	
	//TODO make model final
	private Model model;
	private final String name;
	private final boolean computer;
	private int gold;

	@Deprecated
	Player(final String name, final boolean computer, final int initialGold) {
		this(name, computer, initialGold, null);
	}

	Player(final String name, final boolean computer, final int initialGold, final Model model) {
		this.name = Preconditions.checkNotNull(name);
		this.computer = computer;
		this.gold = initialGold;
		this.model = model;
	}
	
	public static Player make(final PlayerPo player, final Model model){
		return new Player(player.getName(), player.isComputer(), player.getGold(), model);
	}
	
	public PlayerPo save(){
		final PlayerPo out = new PlayerPo();
		out.setName(name);
		out.setComputer(computer);
		out.setGold(gold);
		return out;
	}

	void setModel(final Model model) {
		this.model = Preconditions.checkNotNull(model);
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

	public List<Unit> getUnits() {
		return model.getUnits(this, false);
	}

	public List<Unit> getAllUnits() {
		return model.getUnits(this, true);
	}

	public List<Unit> getUnits(final boolean includeStored) {
		return model.getUnits(this, includeStored);
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

	public List<Unit> getEnemyUnits(final boolean includeStored) {
		return model.getEnemyUnits(this, includeStored);
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
		return MoreObjects.toStringHelper(this).add("name", name).add("computer", computer).toString();
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

	public void buy(final GoodAmount goodAmount) {
		int price = goodAmount.getAmount()
				* model.getEurope().getGoodTradeForType(goodAmount.getGoodType()).getBuyPrice();
		if (getGold() - price < 0) {
			throw new NotEnoughtGoldException(
					String.format("You can't buy this item. You need %s and you have %s", price, getGold()));
		}
		setGold(getGold() - price);
	}

	public void sell(final GoodAmount goodAmount) {
		int price = goodAmount.getAmount()
				* model.getEurope().getGoodTradeForType(goodAmount.getGoodType()).getBuyPrice();
		setGold(getGold() + price);
	}
	
	public void buy(final UnitType unitType){
		int price = unitType.getEuropePrice();
		if (getGold() - price < 0) {
			throw new NotEnoughtGoldException(
					String.format("You can't buy this item. You need %s and you have %s", price, getGold()));
		}
		//TODO buy it and place to Europe pier or dock. 
		setGold(getGold() - price);
	}
	
}
