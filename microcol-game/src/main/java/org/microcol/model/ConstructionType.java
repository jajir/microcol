package org.microcol.model;

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
 */
public final class ConstructionType {

    private final String name;
    private final int requiredHammers;
    private final int requiredTools;
    private final ConstructionType upgradeTo;

    /**
     * Good type that is produced. When construction doesn't produce any goods
     * than it's null.
     */
    private final GoodType produce;

    /**
     * Good type that is consumed during production. When construction doesn't
     * consume any goods than it's null.
     */
    private final GoodType consumed;

    /**
     * How many goods is produces when one slot is occupied by on free colonist.
     */
    private final int productionPerTurn;

    /**
     * Basic production per turn. For example one bell is produced in each
     * colony each turn event when no unit is in construction.
     */
    private final int baseProductionPerTurn;

    /**
     * Construction production ratio. When building consume goods to produce
     * goods than this is production ration defined:
     * 
     * <pre>
     * consumptionPerTurn * productionRatio = productionPerTurn
     * </pre>
     * 
     * Value of consumptionPerTurn could be computed.
     */
    private final float productionRatio;

    private final int slotsForWorkers;
    private final int requiredColonyPopulation;

    public final static ConstructionType TOWN_HALL = ConstructionTypeBuilder.make()
            .setName("TOWN_HALL").setRequiredHammers(0).setRequiredTools(0)
            .setProduce(GoodType.BELL).setProductionPerTurn(3).setBaseProductionPerTurn(1)
            .setSlotsForWorkers(3).setUpgradeTo(null).setRequiredColonyPopulation(1).build();

    public final static ConstructionType LUMBER_MILL = ConstructionTypeBuilder.make()
            .setName("LUMBER_MILL").setRequiredHammers(52).setRequiredTools(0)
            .setConsumed(GoodType.LUMBER).setProduce(GoodType.HAMMERS).setProductionPerTurn(6)
            .setBaseProductionPerTurn(0).setSlotsForWorkers(3).setUpgradeTo(null)
            .setRequiredColonyPopulation(3).build();

    public final static ConstructionType CARPENTERS_SHOP = ConstructionTypeBuilder.make()
            .setName("CARPENTERS_SHOP").setRequiredHammers(0).setRequiredTools(0)
            .setConsumed(GoodType.LUMBER).setProduce(GoodType.HAMMERS).setProductionPerTurn(6)
            .setBaseProductionPerTurn(0).setSlotsForWorkers(3).setUpgradeTo(LUMBER_MILL)
            .setRequiredColonyPopulation(1).build();

    public final static ConstructionType IRON_WORKS = ConstructionTypeBuilder.make()
            .setName("IRON_WORKS").setRequiredHammers(240).setRequiredTools(100)
            .setConsumed(GoodType.ORE).setProduce(GoodType.TOOLS).setProductionPerTurn(9)
            .setBaseProductionPerTurn(0).setProductionRation(3 / 2F).setSlotsForWorkers(3)
            .setUpgradeTo(null).setRequiredColonyPopulation(8).build();

    public final static ConstructionType BLACKSMITHS_SHOP = ConstructionTypeBuilder.make()
            .setName("BLACKSMITHS_SHOP").setRequiredHammers(64).setRequiredTools(20)
            .setConsumed(GoodType.ORE).setProduce(GoodType.TOOLS).setProductionPerTurn(5)
            .setBaseProductionPerTurn(0).setSlotsForWorkers(3).setUpgradeTo(IRON_WORKS)
            .setRequiredColonyPopulation(4).build();

    public final static ConstructionType BLACKSMITHS_HOUSE = ConstructionTypeBuilder.make()
            .setName("BLACKSMITHS_HOUSE").setRequiredHammers(0).setRequiredTools(0)
            .setConsumed(GoodType.ORE).setProduce(GoodType.TOOLS).setProductionPerTurn(3)
            .setBaseProductionPerTurn(0).setSlotsForWorkers(3).setUpgradeTo(BLACKSMITHS_SHOP)
            .setRequiredColonyPopulation(1).build();

    public final static ConstructionType FORTRESS = ConstructionTypeBuilder.make()
            .setName("FORTRESS").setRequiredHammers(320).setRequiredTools(200).setProduce(null)
            .setProductionPerTurn(0).setBaseProductionPerTurn(0).setSlotsForWorkers(0)
            .setUpgradeTo(null).setRequiredColonyPopulation(8).build();

    public final static ConstructionType FORT = ConstructionTypeBuilder.make().setName("FORT")
            .setRequiredHammers(120).setRequiredTools(100).setProduce(null)
            .setProductionPerTurn(0).setBaseProductionPerTurn(0).setSlotsForWorkers(0)
            .setUpgradeTo(FORTRESS).setRequiredColonyPopulation(40).build();

    public final static ConstructionType STOCKADE = ConstructionTypeBuilder.make()
            .setName("STOCKADE").setRequiredHammers(64).setRequiredTools(0).setProduce(null)
            .setProductionPerTurn(0).setBaseProductionPerTurn(0).setSlotsForWorkers(0)
            .setUpgradeTo(FORT).setRequiredColonyPopulation(3).build();

    public final static ConstructionType CIGAR_FACTORY = ConstructionTypeBuilder.make()
            .setName("CIGAR_FACTORY").setRequiredHammers(160).setRequiredTools(100)
            .setConsumed(GoodType.TOBACCO).setProduce(GoodType.CIGARS).setProductionPerTurn(8)
            .setBaseProductionPerTurn(0).setSlotsForWorkers(3).setUpgradeTo(null)
            .setRequiredColonyPopulation(8).build();

    public final static ConstructionType TOBACCONISTS_SHOP = ConstructionTypeBuilder.make()
            .setName("TOBACCONISTS_SHOP").setRequiredHammers(64).setRequiredTools(20)
            .setConsumed(GoodType.TOBACCO).setProduce(GoodType.CIGARS).setProductionPerTurn(6)
            .setBaseProductionPerTurn(0).setSlotsForWorkers(3).setUpgradeTo(CIGAR_FACTORY)
            .setRequiredColonyPopulation(4).build();

    public final static ConstructionType TOBACCONISTS_HOUSE = ConstructionTypeBuilder.make()
            .setName("TOBACCONISTS_HOUSE").setRequiredHammers(0).setRequiredTools(0)
            .setConsumed(GoodType.TOBACCO).setProduce(GoodType.CIGARS).setSlotsForWorkers(3)
            .setProductionPerTurn(3).setBaseProductionPerTurn(0).setUpgradeTo(TOBACCONISTS_SHOP)
            .setRequiredColonyPopulation(1).build();

    public final static ConstructionType TEXTILE_MILL = ConstructionTypeBuilder.make()
            .setName("TEXTILE_MILL").setRequiredHammers(160).setRequiredTools(100)
            .setConsumed(GoodType.COTTON).setProduce(GoodType.SILK).setProductionPerTurn(8)
            .setBaseProductionPerTurn(0).setSlotsForWorkers(3).setUpgradeTo(null)
            .setRequiredColonyPopulation(8).build();

    public final static ConstructionType WEAVERS_SHOP = ConstructionTypeBuilder.make()
            .setName("WEAVERS_SHOP").setRequiredHammers(64).setRequiredTools(20)
            .setConsumed(GoodType.COTTON).setProduce(GoodType.SILK).setProductionPerTurn(6)
            .setBaseProductionPerTurn(0).setSlotsForWorkers(3).setUpgradeTo(TEXTILE_MILL)
            .setRequiredColonyPopulation(4).build();

    public final static ConstructionType WEAVERS_HOUSE = ConstructionTypeBuilder.make()
            .setName("WEAVERS_HOUSE").setRequiredHammers(0).setRequiredTools(0)
            .setConsumed(GoodType.COTTON).setProduce(GoodType.SILK).setProductionPerTurn(3)
            .setBaseProductionPerTurn(0).setSlotsForWorkers(3).setUpgradeTo(WEAVERS_SHOP)
            .setRequiredColonyPopulation(1).build();

    public final static ConstructionType RUM_FACTORY = ConstructionTypeBuilder.make()
            .setName("RUM_FACTORY").setRequiredHammers(160).setRequiredTools(100)
            .setConsumed(GoodType.SUGAR).setProduce(GoodType.RUM).setProductionPerTurn(8)
            .setBaseProductionPerTurn(0).setSlotsForWorkers(3).setUpgradeTo(null)
            .setRequiredColonyPopulation(8).build();

    public final static ConstructionType RUM_DISTILLERY = ConstructionTypeBuilder.make()
            .setName("RUM_DISTILLERY").setRequiredHammers(64).setRequiredTools(20)
            .setConsumed(GoodType.SUGAR).setProduce(GoodType.RUM).setProductionPerTurn(6)
            .setBaseProductionPerTurn(0).setSlotsForWorkers(3).setUpgradeTo(RUM_FACTORY)
            .setRequiredColonyPopulation(4).build();

    public final static ConstructionType RUM_DISTILLERS_HOUSE = ConstructionTypeBuilder.make()
            .setName("RUM_DISTILLERS_HOUSE").setRequiredHammers(0).setRequiredTools(0)
            .setConsumed(GoodType.SUGAR).setProduce(GoodType.RUM).setProductionPerTurn(3)
            .setBaseProductionPerTurn(0).setSlotsForWorkers(3).setUpgradeTo(RUM_DISTILLERY)
            .setRequiredColonyPopulation(1).build();

    public final static ConstructionType FUR_FACTORY = ConstructionTypeBuilder.make()
            .setName("FUR_FACTORY").setRequiredHammers(160).setRequiredTools(100)
            .setConsumed(GoodType.FUR).setProduce(GoodType.COAT).setProductionPerTurn(8)
            .setBaseProductionPerTurn(0).setSlotsForWorkers(3).setUpgradeTo(null)
            .setRequiredColonyPopulation(6).build();

    public final static ConstructionType FUR_TRADING_POST = ConstructionTypeBuilder.make()
            .setName("FUR_TRADING_POST").setRequiredHammers(56).setRequiredTools(20)
            .setConsumed(GoodType.FUR).setProduce(GoodType.COAT).setProductionPerTurn(6)
            .setBaseProductionPerTurn(0).setSlotsForWorkers(3).setUpgradeTo(FUR_FACTORY)
            .setRequiredColonyPopulation(3).build();

    public final static ConstructionType FUR_TRADERS_HOUSE = ConstructionTypeBuilder.make()
            .setName("FUR_TRADERS_HOUSE").setRequiredHammers(0).setRequiredTools(0)
            .setConsumed(GoodType.FUR).setProduce(GoodType.COAT).setProductionPerTurn(3)
            .setBaseProductionPerTurn(0).setSlotsForWorkers(3).setUpgradeTo(FUR_TRADING_POST)
            .setRequiredColonyPopulation(1).build();

    public final static ConstructionType ARSENAL = ConstructionTypeBuilder.make().setName("ARSENAL")
            .setRequiredHammers(240).setRequiredTools(100).setConsumed(GoodType.TOOLS)
            .setProduce(GoodType.MUSKET).setProductionPerTurn(8).setBaseProductionPerTurn(0)
            .setSlotsForWorkers(3).setUpgradeTo(null).setRequiredColonyPopulation(8).build();

    public final static ConstructionType MAGAZINE = ConstructionTypeBuilder.make()
            .setName("MAGAZINE").setRequiredHammers(120).setRequiredTools(50)
            .setConsumed(GoodType.TOOLS).setProduce(GoodType.MUSKET).setProductionPerTurn(6)
            .setBaseProductionPerTurn(0).setSlotsForWorkers(3).setUpgradeTo(ARSENAL)
            .setRequiredColonyPopulation(8).build();

    public final static ConstructionType ARMORY = ConstructionTypeBuilder.make().setName("ARMORY")
            .setRequiredHammers(52).setRequiredTools(0).setConsumed(GoodType.TOOLS)
            .setProduce(GoodType.MUSKET).setProductionPerTurn(3).setBaseProductionPerTurn(0)
            .setSlotsForWorkers(3).setUpgradeTo(MAGAZINE).setRequiredColonyPopulation(1).build();

    public final static ConstructionType SHIPYARD = ConstructionTypeBuilder.make()
            .setName("SHIPYARD").setRequiredHammers(240).setRequiredTools(100).setProduce(null)
            .setProductionPerTurn(0).setBaseProductionPerTurn(0).setSlotsForWorkers(0)
            .setUpgradeTo(null).setRequiredColonyPopulation(8).build();

    public final static ConstructionType DRYDOCK = ConstructionTypeBuilder.make().setName("DRYDOCK")
            .setRequiredHammers(80).setRequiredTools(50).setProduce(null).setProductionPerTurn(0)
            .setBaseProductionPerTurn(0).setSlotsForWorkers(0).setUpgradeTo(SHIPYARD)
            .setRequiredColonyPopulation(6).build();

    public final static ConstructionType DOCK = ConstructionTypeBuilder.make().setName("DOCK")
            .setRequiredHammers(52).setRequiredTools(0).setProduce(null).setProductionPerTurn(0)
            .setBaseProductionPerTurn(0).setSlotsForWorkers(0).setUpgradeTo(DRYDOCK)
            .setRequiredColonyPopulation(1).build();

    public final static ConstructionType UNIVERSITY = ConstructionTypeBuilder.make()
            .setName("UNIVERSITY").setRequiredHammers(200).setRequiredTools(100).setProduce(null)
            .setProductionPerTurn(0).setBaseProductionPerTurn(0).setSlotsForWorkers(3)
            .setUpgradeTo(null).setRequiredColonyPopulation(10).build();

    public final static ConstructionType COLLEGE = ConstructionTypeBuilder.make().setName("COLLEGE")
            .setRequiredHammers(160).setRequiredTools(50).setProduce(null).setProductionPerTurn(0)
            .setBaseProductionPerTurn(0).setSlotsForWorkers(3).setUpgradeTo(UNIVERSITY)
            .setRequiredColonyPopulation(8).build();

    public final static ConstructionType SCHOOLHOUSE = ConstructionTypeBuilder.make()
            .setName("SCHOOLHOUSE").setRequiredHammers(64).setRequiredTools(0).setProduce(null)
            .setProductionPerTurn(0).setBaseProductionPerTurn(0).setSlotsForWorkers(3)
            .setUpgradeTo(COLLEGE).setRequiredColonyPopulation(4).build();

    public final static ConstructionType WAREHOUSE_EXPANSION = ConstructionTypeBuilder.make()
            .setName("WAREHOUSE_EXPANSION").setRequiredHammers(80).setRequiredTools(20)
            .setProduce(null).setProductionPerTurn(0).setBaseProductionPerTurn(0)
            .setSlotsForWorkers(0).setUpgradeTo(null).setRequiredColonyPopulation(1).build();

    public final static ConstructionType WAREHOUSE = ConstructionTypeBuilder.make()
            .setName("WAREHOUSE").setRequiredHammers(80).setRequiredTools(0).setProduce(null)
            .setProductionPerTurn(0).setBaseProductionPerTurn(0).setSlotsForWorkers(0)
            .setUpgradeTo(WAREHOUSE_EXPANSION).setRequiredColonyPopulation(1).build();

    public final static ConstructionType BASIC_WAREHOUSE = ConstructionTypeBuilder.make()
            .setName("BASIC_WAREHOUSE").setRequiredHammers(0).setRequiredTools(0).setProduce(null)
            .setProductionPerTurn(0).setBaseProductionPerTurn(0).setSlotsForWorkers(0)
            .setUpgradeTo(WAREHOUSE).setRequiredColonyPopulation(1).build();

    public final static ConstructionType STABLES = ConstructionTypeBuilder.make().setName("STABLES")
            .setRequiredHammers(64).setRequiredTools(0).setProduce(null).setProductionPerTurn(0)
            .setBaseProductionPerTurn(0).setSlotsForWorkers(0).setUpgradeTo(null)
            .setRequiredColonyPopulation(1).build();

    public final static ConstructionType CATHEDRAL = ConstructionTypeBuilder.make()
            .setName("CATHEDRAL").setRequiredHammers(176).setRequiredTools(100)
            .setProduce(GoodType.CROSS).setProductionPerTurn(3).setBaseProductionPerTurn(1)
            .setSlotsForWorkers(0).setUpgradeTo(null).setRequiredColonyPopulation(8).build();

    public final static ConstructionType CHURCH = ConstructionTypeBuilder.make().setName("CHURCH")
            .setRequiredHammers(52).setRequiredTools(100).setProduce(GoodType.CROSS)
            .setProductionPerTurn(3).setBaseProductionPerTurn(1).setSlotsForWorkers(3)
            .setUpgradeTo(CATHEDRAL).setRequiredColonyPopulation(3).build();

    public final static ConstructionType NEWSPAPER = ConstructionTypeBuilder.make()
            .setName("NEWSPAPER").setRequiredHammers(120).setRequiredTools(50)
            .setProduce(GoodType.BELL).setProductionPerTurn(6).setBaseProductionPerTurn(0)
            .setSlotsForWorkers(0).setUpgradeTo(null).setRequiredColonyPopulation(4).build();

    public final static ConstructionType PRINTING_PRESS = ConstructionTypeBuilder.make()
            .setName("PRINTING_PRESS").setRequiredHammers(80).setRequiredTools(0)
            .setProduce(GoodType.BELL).setProductionPerTurn(9).setBaseProductionPerTurn(0)
            .setSlotsForWorkers(0).setUpgradeTo(NEWSPAPER).setRequiredColonyPopulation(1).build();

    public final static ConstructionType CUSTOM_HOUSE = ConstructionTypeBuilder.make()
            .setName("CUSTOM_HOUSE").setRequiredHammers(160).setRequiredTools(50).setProduce(null)
            .setProductionPerTurn(0).setBaseProductionPerTurn(0).setSlotsForWorkers(0)
            .setUpgradeTo(null).setRequiredColonyPopulation(0).build();

    public final static List<ConstructionType> ALL = ImmutableList.of(TOWN_HALL, LUMBER_MILL,
            CARPENTERS_SHOP, IRON_WORKS, BLACKSMITHS_SHOP, BLACKSMITHS_HOUSE, FORTRESS, FORT,
            STOCKADE, CIGAR_FACTORY, TOBACCONISTS_SHOP, TOBACCONISTS_HOUSE, TEXTILE_MILL,
            WEAVERS_SHOP, WEAVERS_HOUSE, RUM_FACTORY, RUM_DISTILLERY, RUM_DISTILLERS_HOUSE,
            FUR_FACTORY, FUR_TRADING_POST, FUR_TRADERS_HOUSE, ARSENAL, MAGAZINE, ARMORY, SHIPYARD,
            DRYDOCK, DOCK, UNIVERSITY, COLLEGE, SCHOOLHOUSE, WAREHOUSE_EXPANSION, WAREHOUSE,
            BASIC_WAREHOUSE, STABLES, CATHEDRAL, CHURCH, NEWSPAPER, PRINTING_PRESS, CUSTOM_HOUSE);

    /**
     * List of construction types that are in all newly builded colonies.
     */
    public final static List<ConstructionType> NEW_COLONY_CONSTRUCTIONS = ALL.stream()
            .filter(constructionType -> constructionType.isBuildFromFounding())
            .collect(ImmutableList.toImmutableList());

    public final static List<ConstructionType> WAREHOUSES = ImmutableList.of(WAREHOUSE_EXPANSION,
            WAREHOUSE, BASIC_WAREHOUSE);

    public static final List<GoodType> SOURCE_1 = GoodType.GOOD_TYPES.stream()
            .filter(goodType -> !isProducedGoodType(goodType))
            .collect(ImmutableList.toImmutableList());

    public static final List<GoodType> SOURCE_2 = GoodType.GOOD_TYPES.stream().filter(
            goodType -> !SOURCE_1.contains(goodType) && isProducedFromAny(goodType, SOURCE_1))
            .collect(ImmutableList.toImmutableList());

    public static final List<GoodType> SOURCE_3 = GoodType.GOOD_TYPES.stream().filter(
            goodType -> !SOURCE_2.contains(goodType) && isProducedFromAny(goodType, SOURCE_2))
            .collect(ImmutableList.toImmutableList());

    /**
     * It's secondary good type source just when good type is produces by some
     * facility without any additional inputs.
     * 
     * @param goodType
     *            required good type
     * @return if it's secondary good type source
     */
    private static boolean isProducedGoodType(final GoodType goodType) {
        return ALL.stream()
                .filter(construction -> construction.getProduce().isPresent()
                        && construction.getProduce().get().equals(goodType)
                        && construction.getConsumed().isPresent())
                .findAny().isPresent();
    }

    private static boolean isProducedFromAny(final GoodType goodType,
            final List<GoodType> goodTypes) {
        return ALL.stream()
                .filter(construction -> construction.getProduce().isPresent()
                        && construction.getProduce().get().equals(goodType)
                        && construction.getConsumed().isPresent()
                        && goodTypes.contains(construction.getConsumed().get()))
                .findAny().isPresent();
    }

    public static ConstructionType valueOf(final String strName) {
        final Optional<ConstructionType> oGoodType = ALL.stream()
                .filter(type -> type.name().equals(strName)).findFirst();
        if (oGoodType.isPresent()) {
            return oGoodType.get();
        } else {
            throw new IllegalArgumentException("There is no such type '" + strName + "'");
        }
    }

    private static class ConstructionTypeBuilder {

        private String name;
        private int requiredHammers;
        private int requiredTools;
        private ConstructionType upgradeTo;
        private GoodType produce;
        private GoodType consumed;
        private int productionPerTurn;
        private int baseProductionPerTurn = 0;
        private float productionRatio = 1F;
        private int slotsForWorkers;
        private int requiredColonyPopulation;

        private static ConstructionTypeBuilder make() {
            return new ConstructionTypeBuilder();
        }

        private ConstructionType build() {
            return new ConstructionType(name, requiredHammers, requiredTools, upgradeTo, produce,
                    consumed, productionPerTurn, baseProductionPerTurn, productionRatio,
                    slotsForWorkers, requiredColonyPopulation);
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

        private ConstructionTypeBuilder setProductionRation(final float productionRatio) {
            this.productionRatio = productionRatio;
            return this;
        }

        private ConstructionTypeBuilder setUpgradeTo(final ConstructionType upgradeTo) {
            this.upgradeTo = upgradeTo;
            return this;
        }

        private ConstructionTypeBuilder setProduce(final GoodType produce) {
            this.produce = produce;
            return this;
        }

        private ConstructionTypeBuilder setConsumed(final GoodType consumed) {
            this.consumed = consumed;
            return this;
        }

        private ConstructionTypeBuilder setProductionPerTurn(final int productionPerTurn) {
            this.productionPerTurn = productionPerTurn;
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

    }

    private ConstructionType(final String name, final int requiredHammers,
            final int requiredTools, final ConstructionType upgradeTo, final GoodType produce,
            final GoodType consumed, final int productionPerTurn, final int baseProductionPerTurn,
            final float productionRatio, final int slotsForWorkers,
            final int requiredColonyPopulation) {
        this.name = Preconditions.checkNotNull(name);
        this.requiredHammers = requiredHammers;
        this.requiredTools = requiredTools;
        this.upgradeTo = upgradeTo;
        this.produce = produce;
        this.consumed = consumed;
        this.productionPerTurn = productionPerTurn;
        this.baseProductionPerTurn = baseProductionPerTurn;
        this.productionRatio = productionRatio;
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

    public Optional<GoodType> getProduce() {
        if (produce == null) {
            return Optional.empty();
        } else {
            return Optional.of(produce);
        }
    }

    public Optional<GoodType> getConsumed() {
        if (consumed == null) {
            return Optional.empty();
        } else {
            return Optional.of(consumed);
        }
    }

    public int getProductionPerTurn() {
        return productionPerTurn;
    }

    public int getConsumptionPerTurn() {
        if (consumed == null) {
            return 0;
        } else {
            return (int) (productionPerTurn / productionRatio);
        }
    }

    public int getBaseProductionPerTurn() {
        return baseProductionPerTurn;
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

    public float getProductionRatio() {
        return productionRatio;
    }

}
