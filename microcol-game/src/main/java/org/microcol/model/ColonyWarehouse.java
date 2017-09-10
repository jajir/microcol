package org.microcol.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.stream.JsonParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Represents place where colony store goods.
 */
public class ColonyWarehouse {

	private final Logger logger = LoggerFactory.getLogger(ColonyWarehouse.class);

	private final Colony colony;

	private final Map<GoodType, Integer> goodAmounts = new HashMap<>();

	ColonyWarehouse(final Colony colony) {
		this.colony = colony;
	}

	static List<Colony> load(final JsonParser parser, final List<Player> players) {
		// TODO JJ NYI
		return Lists.newArrayList();
	}

	public Integer getGoodAmmount(final GoodType goodType) {
		Preconditions.checkNotNull(goodType);
		Integer amount = goodAmounts.get(goodType);
		if (amount == null) {
			return 0;
		}
		return amount;
	}

	public Integer getTransferableGoodsAmount(final GoodType goodType) {
		Integer amount = goodAmounts.get(goodType);
		if (amount > 100) {
			return 100;
		}
		return amount;
	}

	private ConstructionType getConstructionType() {
		return colony.getWarehouseType();
	}

	public void putToWarehouse(final GoodType goodType, final int amount) {
		goodAmounts.put(goodType, amount);
	}

	public void moveToWarehouse(final GoodType goodType, final int amount, final CargoSlot fromCargoSlot) {
		Preconditions.checkNotNull(goodType);
		Preconditions.checkNotNull(fromCargoSlot);
		Preconditions.checkArgument(amount >= 0, "amount can't less than 0");
		final Integer newAmount = getGoodAmmount(goodType) + amount;
		final int limit = getMaxStorageSpace(goodType, getConstructionType());
		if (newAmount > limit) {
			logger.warn(String.format("Good (%s) ammount (%s) exceed warehouse limit (%s)", goodType.name(), newAmount,
					limit));
		}
		fromCargoSlot.removeCargo(goodType, amount);
		goodAmounts.put(goodType, newAmount);
	}

	public void removeFromWarehouse(final GoodType goodType, final int ammount) {
		Preconditions.checkArgument(ammount >= 0, "amount can't less than 0");
		final Integer newAmount = getGoodAmmount(goodType) - ammount;
		final int limit = 0;
		Preconditions.checkArgument(newAmount >= limit,
				"Good (%s) ammount (%s) can't be less than warehjouse limit (%s)", goodType.name(), newAmount, limit);
		goodAmounts.put(goodType, newAmount);
	}

	int getMaxStorageSpace(final GoodType goodType, final ConstructionType constructionType) {
		if (goodType == GoodType.CORN) {
			return 200;
		}
		if (constructionType.equals(ConstructionType.BASIC_WAREHOUSE)) {
			return 100;
		} else if (constructionType.equals(ConstructionType.WAREHOUSE)) {
			return 200;
		} else if (constructionType.equals(ConstructionType.WAREHOUSE_EXPANSION)) {
			return 300;
		} else {
			throw new IllegalStateException(
					String.format("Unable to get maximum storage for construction type (%s)", constructionType));
		}
	}

}
