package org.microcol.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

/**
 * Represents place where colony store goods.
 */
public class ColonyWarehouse {

    private final Logger logger = LoggerFactory.getLogger(ColonyWarehouse.class);

    private final Colony colony;

    private final Map<GoodsType, Integer> warehouse;

    ColonyWarehouse(final Colony colony, final Map<String, Integer> initialGoods) {
        this.colony = colony;
        Preconditions.checkNotNull(initialGoods);
        this.warehouse = new HashMap<>();
        initialGoods
                .forEach((goodName, amount) -> warehouse.put(GoodsType.valueOf(goodName), amount));
        validation();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(ColonyWarehouse.class)
                .add("Colony name", colony.getName()).toString();
    }

    /**
     * Verify that no goods exceed warehouse capacity.
     */
    private void validation() {
        for (final Entry<GoodsType, Integer> entry : warehouse.entrySet()) {
            final GoodsType type = entry.getKey();
            final Integer amount = entry.getValue();
            final Goods capacity = getStorageCapacity(type);
            Preconditions.checkArgument(amount <= capacity.getAmount(),
                    "Can't create warehouse with %s of %s goods because capacity is %s", amount,
                    type, capacity.getAmount());
        }
    }

    Map<String, Integer> save() {
        return warehouse.entrySet().stream().collect(ImmutableMap
                .toImmutableMap(entry -> entry.getKey().name(), entry -> entry.getValue()));
    }

    public Goods getGoods(final GoodsType goodsType) {
        Preconditions.checkNotNull(goodsType, "GoodsType is null");
        if (warehouse.get(goodsType) == null) {
            return Goods.of(goodsType);
        } else {
            return Goods.of(goodsType, warehouse.get(goodsType));
        }
    }

    /**
     * For given goods type return man goods that could be moved with given
     * limit.
     *
     * @param goodsType
     *            required goods type
     * @param limit
     *            max transferable goods amount
     * @return really available transferable goods
     */
    public Goods getTransferableGoods(final GoodsType goodsType, final int limit) {
        final Goods goods = getGoods(goodsType);
        if (goods.getAmount() > limit) {
            return Goods.of(goodsType, limit);
        } else {
            return goods;
        }
    }

    private ConstructionType getConstructionType() {
        return colony.getWarehouseType();
    }

    public void addGoods(final Goods goods) {
        Preconditions.checkNotNull(goods);
        setGoods(getGoods(goods.getType()).add(goods));
    }

    public void moveToWarehouse(final Goods goods, final CargoSlot fromCargoSlot) {
        Preconditions.checkNotNull(goods);
        Preconditions.checkNotNull(fromCargoSlot);

        Preconditions.checkArgument(fromCargoSlot.getGoods().isPresent());
        Preconditions
                .checkArgument(goods.getType().equals(fromCargoSlot.getGoods().get().getType()));
        Preconditions
                .checkArgument(fromCargoSlot.getGoods().get().getAmount() >= goods.getAmount());

        addGoods(goods);
        fromCargoSlot.removeCargo(goods);
    }

    /**
     * Add goods to warehouse. When inserted goods exceed warehouse capacity
     * than remaining goods is throws away.
     * 
     * @param goods
     *            required goods
     * @param thrownAwayGoodsConsumer
     *            optional consumer which is called when some goods is throws
     *            away.
     * @return throws away goods. When no goods was thrown away than it returns
     *         goods with 0 amount
     */
    public Goods addGoodsWithThrowingAway(final Goods goods,
            final Consumer<Goods> thrownAwayGoodsConsumer) {
        Preconditions.checkNotNull(goods);
        final GoodsType goodsType = goods.getType();
        final Goods remainingCapacity = getStorageCapacity(goodsType)
                .substract(getGoods(goodsType));
        if (remainingCapacity.isGreaterOrEqualsThan(goods)) {
            // goods fit to warehouse.
            addGoods(goods);
            return Goods.of(goods.getType());
        } else {
            final Goods thrownAway = goods.substract(remainingCapacity);
            addGoods(remainingCapacity);
            if (thrownAwayGoodsConsumer != null) {
                thrownAwayGoodsConsumer.accept(thrownAway);
            }
            return thrownAway;
        }
    }

    public void removeGoods(final Goods goods) {
        Preconditions.checkNotNull(goods);
        final Goods current = getGoods(goods.getType());
        Preconditions.checkArgument(goods.getAmount() <= current.getAmount(),
                "Can't remove %s of %s, because in warehouse is currently just %s of %s",
                goods.getAmount(), goods.getType(), current.getAmount(), goods.getType());
        setGoods(current.substract(goods));
    }

    void setGoodsToZero(final GoodsType goodsType) {
        setGoods(Goods.of(goodsType));
    }

    void setGoods(final Goods goods) {
        Preconditions.checkNotNull(goods);
        Preconditions.checkArgument(
                goods.getAmount() <= getStorageCapacity(goods.getType()).getAmount(),
                "%s can't be stored because storage limit for %s is %s", goods.getAmount(),
                goods.getType(), getStorageCapacity(goods.getType()));
        logger.debug("Setting goods to {}", goods);
        warehouse.put(goods.getType(), goods.getAmount());
    }

    Goods getStorageCapacity(final GoodsType goodsType) {
        return Goods.of(goodsType, getStorageCapacity(goodsType, getConstructionType()));
    }

    int getStorageCapacity(final GoodsType goodsType, final ConstructionType constructionType) {
        Preconditions.checkNotNull(goodsType);
        Preconditions.checkNotNull(constructionType);
        if (GoodsType.BUYABLE_GOOD_TYPES.contains(goodsType)) {
            if (goodsType == GoodsType.CORN) {
                return 200;
            }
            if (constructionType.equals(ConstructionType.WAREHOUSE_BASIC)) {
                return 100;
            } else if (constructionType.equals(ConstructionType.WAREHOUSE)) {
                return 200;
            } else if (constructionType.equals(ConstructionType.WAREHOUSE_EXPANSION)) {
                return 300;
            } else {
                throw new IllegalStateException(
                        String.format("Unable to get maximum storage for construction type (%s)",
                                constructionType));
            }
        } else {
            return Integer.MAX_VALUE;
        }
    }

    void saveStatisticsTo(final PlayerGoodsStatistics statistics) {
        warehouse.entrySet().forEach(statistics::addEntry);
    }

}
