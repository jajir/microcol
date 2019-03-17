package org.microcol.model;

import static org.microcol.model.GoodsType.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Define construction types.
 * <p>
 * When consumed and produced good type is defined than number of consumed
 * resources is same as number of produced goods. It doesn't include other
 * production boosters.
 * </p>
 * <p>
 * Production and consumption of goods is defined for one construction slot per
 * one turn when there is working free colonist. When construction slot is
 * occupied by servant than production is lower and when there is expert
 * colonist than production is higher.
 * </p>
 */
public class ConstructionType {

    private final String name;
    private final int requiredHammers;
    private final int requiredTools;
    private final ConstructionType upgradeTo;

    /**
     * How many goods is produces per turn per one working place occupied by one
     * free colonist.
     * <p>
     * Could be null. When construction do not produce any goods.
     * </p>
     */
    private final Goods productionPerTurn;

    /**
     * How many goods is consumed per turn per one working place occupied by one
     * free colonist.
     * <p>
     * Could be null. When construction do not consume any goods.
     * </p>
     */
    private final Goods consumptionPerTurn;

    /**
     * Basic production per turn. For example one bell is produced in each
     * colony each turn event when no unit is in construction.
     * <p>
     * Could be null. When construction do not produce any goods without
     * colonists working in.
     * </p>
     */
    private final Goods baseProductionPerTurn;

    /**
     * How many place building contains for workers.
     */
    private final int slotsForWorkers;

    /**
     * How big should be population required for this building.
     */
    private final int requiredColonyPopulation;

    public final static ConstructionType TOWN_HALL = ConstructionTypeBuilder.make()
            .setName("TOWN_HALL").setProductionPerTurn(Goods.of(BELL, 3))
            .setBaseProductionPerTurn(1).setSlotsForWorkers(3).setUpgradeTo(null)
            .setRequiredColonyPopulation(1).build();

    public final static ConstructionType LUMBER_MILL = ConstructionTypeBuilder.make()
            .setName("LUMBER_MILL").setRequiredHammers(200).setRequiredTools(100)
            .setConsumptionPerTurn(Goods.of(LUMBER, 6)).setProductionPerTurn(Goods.of(HAMMERS, 6))
            .setSlotsForWorkers(3).setUpgradeTo(null).setRequiredColonyPopulation(3).build();

    public final static ConstructionType CARPENTERS_SHOP = ConstructionTypeBuilder.make()
            .setName("CARPENTERS_SHOP").setRequiredHammers(52)
            .setConsumptionPerTurn(Goods.of(LUMBER, 6)).setProductionPerTurn(Goods.of(HAMMERS, 6))
            .setSlotsForWorkers(3).setUpgradeTo(LUMBER_MILL).setRequiredColonyPopulation(3).build();

    public final static ConstructionType CARPENTERS_STAND = ConstructionTypeBuilder.make()
            .setName("CARPENTERS_STAND").setConsumptionPerTurn(Goods.of(LUMBER, 3))
            .setProductionPerTurn(Goods.of(HAMMERS, 3)).setSlotsForWorkers(3)
            .setUpgradeTo(CARPENTERS_SHOP).setRequiredColonyPopulation(1).build();

    public final static ConstructionType IRON_WORKS = ConstructionTypeBuilder.make()
            .setName("IRON_WORKS").setRequiredHammers(240).setRequiredTools(100)
            .setConsumptionPerTurn(Goods.of(ORE, 6)).setProductionPerTurn(Goods.of(TOOLS, 9))
            .setSlotsForWorkers(3).setUpgradeTo(null).setRequiredColonyPopulation(8).build();

    public final static ConstructionType BLACKSMITHS_SHOP = ConstructionTypeBuilder.make()
            .setName("BLACKSMITHS_SHOP").setRequiredHammers(64).setRequiredTools(20)
            .setConsumptionPerTurn(Goods.of(ORE, 5)).setProductionPerTurn(Goods.of(TOOLS, 5))
            .setSlotsForWorkers(3).setUpgradeTo(IRON_WORKS).setRequiredColonyPopulation(4).build();

    public final static ConstructionType BLACKSMITHS_HOUSE = ConstructionTypeBuilder.make()
            .setName("BLACKSMITHS_HOUSE").setConsumptionPerTurn(Goods.of(ORE, 3))
            .setProductionPerTurn(Goods.of(TOOLS, 3)).setSlotsForWorkers(3)
            .setUpgradeTo(BLACKSMITHS_SHOP).setRequiredColonyPopulation(1).build();

    public final static ConstructionType FORTRESS = ConstructionTypeBuilder.make()
            .setName("FORTRESS").setRequiredHammers(320).setRequiredTools(200).setSlotsForWorkers(0)
            .setUpgradeTo(null).setRequiredColonyPopulation(8).build();

    public final static ConstructionType FORT = ConstructionTypeBuilder.make().setName("FORT")
            .setRequiredHammers(120).setRequiredTools(100).setSlotsForWorkers(0)
            .setUpgradeTo(FORTRESS).setRequiredColonyPopulation(40).build();

    public final static ConstructionType STOCKADE = ConstructionTypeBuilder.make()
            .setName("STOCKADE").setRequiredHammers(64).setRequiredTools(0).setSlotsForWorkers(0)
            .setUpgradeTo(FORT).setRequiredColonyPopulation(3).build();

    public final static ConstructionType CIGAR_FACTORY = ConstructionTypeBuilder.make()
            .setName("CIGAR_FACTORY").setRequiredHammers(160).setRequiredTools(100)
            .setConsumptionPerTurn(Goods.of(TOBACCO, 8)).setProductionPerTurn(Goods.of(CIGARS, 8))
            .setSlotsForWorkers(3).setUpgradeTo(null).setRequiredColonyPopulation(8).build();

    public final static ConstructionType TOBACCONISTS_SHOP = ConstructionTypeBuilder.make()
            .setName("TOBACCONISTS_SHOP").setRequiredHammers(64).setRequiredTools(20)
            .setConsumptionPerTurn(Goods.of(TOBACCO, 6)).setProductionPerTurn(Goods.of(CIGARS, 6))
            .setSlotsForWorkers(3).setUpgradeTo(CIGAR_FACTORY).setRequiredColonyPopulation(4)
            .build();

    public final static ConstructionType TOBACCONISTS_HOUSE = ConstructionTypeBuilder.make()
            .setName("TOBACCONISTS_HOUSE").setRequiredHammers(0).setRequiredTools(0)
            .setConsumptionPerTurn(Goods.of(TOBACCO, 3)).setProductionPerTurn(Goods.of(CIGARS, 3))
            .setUpgradeTo(TOBACCONISTS_SHOP).setSlotsForWorkers(3).setRequiredColonyPopulation(1)
            .build();

    public final static ConstructionType TEXTILE_MILL = ConstructionTypeBuilder.make()
            .setName("TEXTILE_MILL").setRequiredHammers(160).setRequiredTools(100)
            .setConsumptionPerTurn(Goods.of(COTTON, 8)).setProductionPerTurn(Goods.of(SILK, 8))
            .setSlotsForWorkers(3).setUpgradeTo(null).setRequiredColonyPopulation(8).build();

    public final static ConstructionType WEAVERS_SHOP = ConstructionTypeBuilder.make()
            .setName("WEAVERS_SHOP").setRequiredHammers(64).setRequiredTools(20)
            .setConsumptionPerTurn(Goods.of(COTTON, 6)).setProductionPerTurn(Goods.of(SILK, 6))
            .setSlotsForWorkers(3).setUpgradeTo(TEXTILE_MILL).setRequiredColonyPopulation(4)
            .build();

    public final static ConstructionType WEAVERS_HOUSE = ConstructionTypeBuilder.make()
            .setName("WEAVERS_HOUSE").setRequiredHammers(0).setRequiredTools(0)
            .setConsumptionPerTurn(Goods.of(COTTON, 3)).setProductionPerTurn(Goods.of(SILK, 3))
            .setSlotsForWorkers(3).setUpgradeTo(WEAVERS_SHOP).setRequiredColonyPopulation(1)
            .build();

    public final static ConstructionType RUM_FACTORY = ConstructionTypeBuilder.make()
            .setName("RUM_FACTORY").setRequiredHammers(160).setRequiredTools(100)
            .setConsumptionPerTurn(Goods.of(SUGAR, 8)).setProductionPerTurn(Goods.of(RUM, 8))
            .setSlotsForWorkers(3).setUpgradeTo(null).setRequiredColonyPopulation(8).build();

    public final static ConstructionType RUM_DISTILLERY = ConstructionTypeBuilder.make()
            .setName("RUM_DISTILLERY").setRequiredHammers(64).setRequiredTools(20)
            .setConsumptionPerTurn(Goods.of(SUGAR, 6)).setProductionPerTurn(Goods.of(RUM, 6))
            .setSlotsForWorkers(3).setUpgradeTo(RUM_FACTORY).setRequiredColonyPopulation(4).build();

    public final static ConstructionType RUM_DISTILLERS_HOUSE = ConstructionTypeBuilder.make()
            .setName("RUM_DISTILLERS_HOUSE").setRequiredHammers(0).setRequiredTools(0)
            .setConsumptionPerTurn(Goods.of(SUGAR, 3)).setProductionPerTurn(Goods.of(RUM, 3))
            .setSlotsForWorkers(3).setUpgradeTo(RUM_DISTILLERY).setRequiredColonyPopulation(1)
            .build();

    public final static ConstructionType FUR_FACTORY = ConstructionTypeBuilder.make()
            .setName("FUR_FACTORY").setRequiredHammers(160).setRequiredTools(100)
            .setConsumptionPerTurn(Goods.of(FUR, 8)).setProductionPerTurn(Goods.of(COAT, 8))
            .setSlotsForWorkers(3).setUpgradeTo(null).setRequiredColonyPopulation(6).build();

    public final static ConstructionType FUR_TRADERS_HOUSE = ConstructionTypeBuilder.make()
            .setName("FUR_TRADERS_HOUSE").setRequiredHammers(0).setRequiredTools(0)
            .setConsumptionPerTurn(Goods.of(FUR, 6)).setProductionPerTurn(Goods.of(COAT, 6))
            .setSlotsForWorkers(3).setUpgradeTo(FUR_FACTORY).setRequiredColonyPopulation(1).build();

    public final static ConstructionType FUR_TRADING_POST = ConstructionTypeBuilder.make()
            .setName("FUR_TRADING_POST").setRequiredHammers(56).setRequiredTools(20)
            .setConsumptionPerTurn(Goods.of(FUR, 3)).setProductionPerTurn(Goods.of(COAT, 3))
            .setSlotsForWorkers(3).setUpgradeTo(FUR_TRADERS_HOUSE).setRequiredColonyPopulation(3)
            .build();

    public final static ConstructionType ARSENAL = ConstructionTypeBuilder.make().setName("ARSENAL")
            .setRequiredHammers(240).setRequiredTools(100).setConsumptionPerTurn(Goods.of(TOOLS, 8))
            .setProductionPerTurn(Goods.of(MUSKET, 8)).setSlotsForWorkers(3).setUpgradeTo(null)
            .setRequiredColonyPopulation(8).build();

    public final static ConstructionType MAGAZINE = ConstructionTypeBuilder.make()
            .setName("MAGAZINE").setRequiredHammers(120).setRequiredTools(50)
            .setConsumptionPerTurn(Goods.of(TOOLS, 6)).setProductionPerTurn(Goods.of(MUSKET, 6))
            .setSlotsForWorkers(3).setUpgradeTo(ARSENAL).setRequiredColonyPopulation(8).build();

    public final static ConstructionType ARMORY = ConstructionTypeBuilder.make().setName("ARMORY")
            .setRequiredHammers(52).setRequiredTools(0).setConsumptionPerTurn(Goods.of(TOOLS, 3))
            .setProductionPerTurn(Goods.of(MUSKET, 3)).setSlotsForWorkers(3).setUpgradeTo(MAGAZINE)
            .setRequiredColonyPopulation(1).build();

    public final static ConstructionType SHIPYARD = ConstructionTypeBuilder.make()
            .setName("SHIPYARD").setRequiredHammers(240).setRequiredTools(100).setSlotsForWorkers(0)
            .setUpgradeTo(null).setRequiredColonyPopulation(8).build();

    public final static ConstructionType DRYDOCK = ConstructionTypeBuilder.make().setName("DRYDOCK")
            .setRequiredHammers(80).setRequiredTools(50).setSlotsForWorkers(0)
            .setUpgradeTo(SHIPYARD).setRequiredColonyPopulation(6).build();

    public final static ConstructionType DOCK = ConstructionTypeBuilder.make().setName("DOCK")
            .setRequiredHammers(52).setRequiredTools(0).setSlotsForWorkers(0).setUpgradeTo(DRYDOCK)
            .setRequiredColonyPopulation(1).build();

    public final static ConstructionType UNIVERSITY = ConstructionTypeBuilder.make()
            .setName("UNIVERSITY").setRequiredHammers(200).setRequiredTools(100)
            .setSlotsForWorkers(3).setUpgradeTo(null).setRequiredColonyPopulation(10).build();

    public final static ConstructionType COLLEGE = ConstructionTypeBuilder.make().setName("COLLEGE")
            .setRequiredHammers(160).setRequiredTools(50).setSlotsForWorkers(3)
            .setUpgradeTo(UNIVERSITY).setRequiredColonyPopulation(8).build();

    public final static ConstructionType SCHOOLHOUSE = ConstructionTypeBuilder.make()
            .setName("SCHOOLHOUSE").setRequiredHammers(64).setRequiredTools(0).setSlotsForWorkers(3)
            .setUpgradeTo(COLLEGE).setRequiredColonyPopulation(4).build();

    public final static ConstructionType WAREHOUSE_EXPANSION = ConstructionTypeBuilder.make()
            .setName("WAREHOUSE_EXPANSION").setRequiredHammers(80).setRequiredTools(20)
            .setSlotsForWorkers(0).setUpgradeTo(null).setRequiredColonyPopulation(1).build();

    public final static ConstructionType WAREHOUSE = ConstructionTypeBuilder.make()
            .setName("WAREHOUSE").setRequiredHammers(80).setRequiredTools(0).setSlotsForWorkers(0)
            .setUpgradeTo(WAREHOUSE_EXPANSION).setRequiredColonyPopulation(1).build();

    public final static ConstructionType WAREHOUSE_BASIC = ConstructionTypeBuilder.make()
            .setName("BASIC_WAREHOUSE").setRequiredHammers(0).setRequiredTools(0)
            .setSlotsForWorkers(0).setUpgradeTo(WAREHOUSE).setRequiredColonyPopulation(1).build();

    public final static ConstructionType LARGE_STABLES = ConstructionTypeBuilder.make()
            .setName("LARGE_STABLES").setRequiredHammers(149).setRequiredTools(80)
            .setSlotsForWorkers(0).setUpgradeTo(null).setRequiredColonyPopulation(1).build();

    public final static ConstructionType STABLES = ConstructionTypeBuilder.make().setName("STABLES")
            .setRequiredHammers(64).setRequiredTools(0).setSlotsForWorkers(0)
            .setUpgradeTo(LARGE_STABLES).setRequiredColonyPopulation(1).build();

    public final static ConstructionType CATHEDRAL = ConstructionTypeBuilder.make()
            .setName("CATHEDRAL").setRequiredHammers(176).setRequiredTools(100)
            .setProductionPerTurn(Goods.of(CROSS, 3)).setBaseProductionPerTurn(1)
            .setSlotsForWorkers(3).setUpgradeTo(null).setRequiredColonyPopulation(8).build();

    public final static ConstructionType CHURCH = ConstructionTypeBuilder.make().setName("CHURCH")
            .setRequiredHammers(52).setRequiredTools(100).setProductionPerTurn(Goods.of(CROSS, 3))
            .setBaseProductionPerTurn(1).setSlotsForWorkers(3).setUpgradeTo(CATHEDRAL)
            .setRequiredColonyPopulation(3).build();

    public final static ConstructionType CHAPEL = ConstructionTypeBuilder.make().setName("CHAPEL")
            .setProductionPerTurn(Goods.of(CROSS, 1)).setBaseProductionPerTurn(1)
            .setSlotsForWorkers(3).setUpgradeTo(CHURCH).setRequiredColonyPopulation(3).build();

    public final static ConstructionType NEWSPAPER = ConstructionTypeBuilder.make()
            .setName("NEWSPAPER").setRequiredHammers(120).setRequiredTools(50)
            .setProductionPerTurn(Goods.of(BELL, 6)).setSlotsForWorkers(0).setUpgradeTo(null)
            .setRequiredColonyPopulation(4).build();

    public final static ConstructionType PRINTING_PRESS = ConstructionTypeBuilder.make()
            .setName("PRINTING_PRESS").setRequiredHammers(80).setRequiredTools(0)
            .setProductionPerTurn(Goods.of(BELL, 9)).setSlotsForWorkers(0).setUpgradeTo(NEWSPAPER)
            .setRequiredColonyPopulation(1).build();

    public final static ConstructionType CUSTOM_HOUSE = ConstructionTypeBuilder.make()
            .setName("CUSTOM_HOUSE").setRequiredHammers(160).setRequiredTools(50)
            .setSlotsForWorkers(0).setUpgradeTo(null).setRequiredColonyPopulation(0).build();

    public final static List<ConstructionType> ALL = ImmutableList.of(TOWN_HALL, LUMBER_MILL,
            CARPENTERS_SHOP, CARPENTERS_STAND, IRON_WORKS, BLACKSMITHS_SHOP, BLACKSMITHS_HOUSE,
            FORTRESS, FORT, STOCKADE, CIGAR_FACTORY, TOBACCONISTS_SHOP, TOBACCONISTS_HOUSE,
            TEXTILE_MILL, WEAVERS_SHOP, WEAVERS_HOUSE, RUM_FACTORY, RUM_DISTILLERY,
            RUM_DISTILLERS_HOUSE, FUR_FACTORY, FUR_TRADING_POST, FUR_TRADERS_HOUSE, ARSENAL,
            MAGAZINE, ARMORY, SHIPYARD, DRYDOCK, DOCK, UNIVERSITY, COLLEGE, SCHOOLHOUSE,
            WAREHOUSE_EXPANSION, WAREHOUSE, WAREHOUSE_BASIC, STABLES, LARGE_STABLES, CHAPEL,
            CATHEDRAL, CHURCH, NEWSPAPER, PRINTING_PRESS, CUSTOM_HOUSE);

    /**
     * List of construction types that are in all newly builded colonies.
     */
    public final static List<ConstructionType> NEW_COLONY_CONSTRUCTIONS = ALL.stream()
            .filter(constructionType -> constructionType.isBuildFromFounding())
            .collect(ImmutableList.toImmutableList());

    public final static List<ConstructionType> WAREHOUSES = ImmutableList.of(WAREHOUSE_EXPANSION,
            WAREHOUSE, WAREHOUSE_BASIC);

    public static final List<GoodsType> SOURCE_1 = GOOD_TYPES.stream()
            .filter(goodsType -> !isProducedGoodsType(goodsType))
            .collect(ImmutableList.toImmutableList());

    public static final List<GoodsType> SOURCE_2 = GOOD_TYPES.stream().filter(
            goodsType -> !SOURCE_1.contains(goodsType) && isProducedFromAny(goodsType, SOURCE_1))
            .collect(ImmutableList.toImmutableList());

    public static final List<GoodsType> SOURCE_3 = GOOD_TYPES.stream().filter(
            goodsType -> !SOURCE_2.contains(goodsType) && isProducedFromAny(goodsType, SOURCE_2))
            .collect(ImmutableList.toImmutableList());

    /**
     * It's secondary good type source just when good type is produces by some
     * facility without any additional inputs.
     * 
     * @param goodsType
     *            required good type
     * @return if it's secondary good type source
     */
    private static boolean isProducedGoodsType(final GoodsType goodsType) {
        return ALL.stream()
                .filter(construction -> construction.getProduce().isPresent()
                        && construction.getProduce().get().equals(goodsType)
                        && construction.getConsumed().isPresent())
                .findAny().isPresent();
    }

    private static boolean isProducedFromAny(final GoodsType goodsType,
            final List<GoodsType> goodsTypes) {
        return ALL.stream()
                .filter(construction -> construction.getProduce().isPresent()
                        && construction.getProduce().get().equals(goodsType)
                        && construction.getConsumed().isPresent()
                        && goodsTypes.contains(construction.getConsumed().get()))
                .findAny().isPresent();
    }

    public static ConstructionType valueOf(final String strName) {
        final Optional<ConstructionType> oGoodsType = ALL.stream()
                .filter(type -> type.name().equals(strName)).findFirst();
        if (oGoodsType.isPresent()) {
            return oGoodsType.get();
        } else {
            throw new IllegalArgumentException("There is no such type '" + strName + "'");
        }
    }

    /**
     * Builder for construction type.
     */
    private static class ConstructionTypeBuilder {

        private String name;
        private ConstructionType upgradeTo;
        private Goods productionPerTurn;
        private Goods consumptionPerTurn;
        private int baseProductionPerTurn = 0;
        private int slotsForWorkers;
        private int requiredHammers;
        private int requiredTools;
        private int requiredColonyPopulation;

        private static ConstructionTypeBuilder make() {
            return new ConstructionTypeBuilder();
        }

        private ConstructionType build() {
            return new ConstructionType(name, requiredHammers, requiredTools, upgradeTo,
                    consumptionPerTurn, productionPerTurn, baseProductionPerTurn, slotsForWorkers,
                    requiredColonyPopulation);
        }

        private ConstructionTypeBuilder setName(final String name) {
            this.name = name;
            return this;
        }

        private ConstructionTypeBuilder setRequiredHammers(final int buildCostHammers) {
            this.requiredHammers = buildCostHammers;
            return this;
        }

        private ConstructionTypeBuilder setRequiredTools(final int buildCostTools) {
            this.requiredTools = buildCostTools;
            return this;
        }

        private ConstructionTypeBuilder setUpgradeTo(final ConstructionType upgradeTo) {
            this.upgradeTo = upgradeTo;
            return this;
        }

        private ConstructionTypeBuilder setBaseProductionPerTurn(final int baseProductionPerTurn) {
            this.baseProductionPerTurn = baseProductionPerTurn;
            return this;
        }

        private ConstructionTypeBuilder setSlotsForWorkers(final int slotsForWorkers) {
            this.slotsForWorkers = slotsForWorkers;
            return this;
        }

        private ConstructionTypeBuilder setRequiredColonyPopulation(
                final int requiredColonyPopulation) {
            this.requiredColonyPopulation = requiredColonyPopulation;
            return this;
        }

        private ConstructionTypeBuilder setProductionPerTurn(Goods productionPerTurn) {
            this.productionPerTurn = productionPerTurn;
            return this;
        }

        private ConstructionTypeBuilder setConsumptionPerTurn(Goods consumptionPerTurn) {
            this.consumptionPerTurn = consumptionPerTurn;
            return this;
        }

    }

    private ConstructionType(final String name, final int requiredHammers, final int requiredTools,
            final ConstructionType upgradeTo, final Goods consumptionPerTurn,
            final Goods productionPerTurn, final int baseProductionPerTurn,
            final int slotsForWorkers, final int requiredColonyPopulation) {
        this.name = Preconditions.checkNotNull(name);
        this.requiredHammers = requiredHammers;
        this.requiredTools = requiredTools;
        this.upgradeTo = upgradeTo;
        this.productionPerTurn = productionPerTurn;
        this.consumptionPerTurn = consumptionPerTurn;

        if (consumptionPerTurn != null) {
            Preconditions.checkArgument(this.productionPerTurn != null,
                    "When consumption is defined than production have to be.");
        }

        if (baseProductionPerTurn != 0) {
            this.baseProductionPerTurn = Goods.of(this.productionPerTurn.getType(),
                    baseProductionPerTurn);
        } else {
            this.baseProductionPerTurn = null;
        }

        this.slotsForWorkers = slotsForWorkers;
        this.requiredColonyPopulation = requiredColonyPopulation;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof ConstructionType) {
            final ConstructionType other = (ConstructionType) obj;
            return name.equals(other.name);
        }
        return false;
    }

    /**
     * Say if construction type is in colony when is newly build.
     *
     * @return Return <code>true</code> when building type is in colony when
     *         it's founded. When construction type have to be builded than
     *         return <code>false</code>.
     */
    public boolean isBuildFromFounding() {
        return requiredHammers == 0 && requiredTools == 0;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(ConstructionType.class).add("name", name).toString();
    }

    public int getRequiredHammers() {
        return requiredHammers;
    }

    public int getRequiredTools() {
        return requiredTools;
    }

    public Optional<ConstructionType> getUpgradeTo() {
        if (upgradeTo == null) {
            return Optional.empty();
        } else {
            return Optional.of(upgradeTo);
        }
    }

    /**
     * Return list of construction type that could be builded from this. This
     * type is always included as first.
     *
     * @return Return list of construction types representing upgrading queue.
     */
    public List<ConstructionType> getUpgradeChain() {
        final List<ConstructionType> out = new ArrayList<>();
        ConstructionType tmp = this;
        out.add(tmp);
        while (tmp.getUpgradeTo().isPresent()) {
            tmp = tmp.getUpgradeTo().get();
            out.add(tmp);
        }
        return ImmutableList.copyOf(out);
    }

    /**
     * Return list of construction type that was builded to build this type.
     * This type is always included as first.
     *
     * @return Return list of construction types representing required upgrading
     *         queue.
     */
    public List<ConstructionType> getUpgradeFromChain() {
        final List<ConstructionType> out = new ArrayList<>();
        ConstructionType tmp = this;
        out.add(tmp);
        while (tmp.getUpgradeFrom().isPresent()) {
            tmp = tmp.getUpgradeFrom().get();
            out.add(tmp);
        }
        return ImmutableList.copyOf(out);
    }

    /**
     * Get construction that could be upgraded to this construction type.
     *
     * @return required construction type
     */
    public Optional<ConstructionType> getUpgradeFrom() {
        return ALL.stream().filter(
                type -> type.getUpgradeTo().isPresent() && type.getUpgradeTo().get() == this)
                .findFirst();
    }

    /**
     * Get produced goods type.
     * <p>
     * Later it should be depreciated and replaced by
     * {@link #getProductionPerTurn()}.
     * </p>
     * 
     * @return produced goods types
     */
    private Optional<GoodsType> getProduce() {
        if (productionPerTurn == null) {
            return Optional.empty();
        } else {
            return Optional.of(productionPerTurn.getType());
        }
    }

    public Optional<Goods> getProductionPerTurn() {
        if (productionPerTurn == null) {
            return Optional.empty();
        } else {
            return Optional.of(productionPerTurn);
        }
    }

    /**
     * Get consumed goods type.
     * <p>
     * Later it should be depreciated and replaced by
     * {@link #getConsumptionPerTurn()}.
     * </p>
     * 
     * @return consumed goods types
     */
    private Optional<GoodsType> getConsumed() {
        if (consumptionPerTurn == null) {
            return Optional.empty();
        } else {
            return Optional.of(consumptionPerTurn.getType());
        }
    }

    public Optional<Goods> getConsumptionPerTurn() {
        if (consumptionPerTurn == null) {
            return Optional.empty();
        } else {
            return Optional.of(consumptionPerTurn);
        }
    }

    public int getBaseProductionPerTurn() {
        if (baseProductionPerTurn == null) {
            return 0;
        } else {
            return baseProductionPerTurn.getAmount();
        }
    }

    public int getSlotsForWorkers() {
        return slotsForWorkers;
    }

    public String name() {
        return name;
    }

    public boolean canBeConstructed() {
        return requiredHammers > 0 && requiredTools > 0;
    }

    public int getRequiredColonyPopulation() {
        return requiredColonyPopulation;
    }

    /**
     * Construction production ratio. When building consume goods to produce
     * goods than this is production ration defined:
     * 
     * <pre>
     * consumptionPerTurn * productionRatio = productionPerTurn
     * </pre>
     * 
     * @return Consumption ratio. When construction doesn't consume anything
     *         than return 1.
     */
    public float getProductionRatio() {
        if (consumptionPerTurn == null) {
            return 1;
        } else {
            return productionPerTurn.getAmount() / (float) consumptionPerTurn.getAmount();
        }
    }

}
