package org.microcol.model;

import java.util.List;
import java.util.Optional;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class ConstructionType {
	 
	public final static ConstructionType TOWN_HALL = ConstructionTypeBuilder.make()
			.setName("TOWN_HALL")
			.setBuildCostHammers(0)
			.setBuildCostTools(0)
			.setProduce(GoodType.BELL)
	 		.setSlotsForWorkers(3)
	 		.setUpgradeTo(null)
	 		.setRequiredColonyPopulation(1)
	 		.build();

	public final static ConstructionType LUMBER_MILL = ConstructionTypeBuilder.make()
			.setName("LUMBER_MILL")
			.setBuildCostHammers(52)
			.setBuildCostTools(0)
			.setProduce(GoodType.HAMMERS)
	 		.setSlotsForWorkers(3)
	 		.setUpgradeTo(null)
	 		.setRequiredColonyPopulation(3)
	 		.build();

	public final static ConstructionType CARPENTERS_SHOP = ConstructionTypeBuilder.make()
			.setName("CARPENTERS_SHOP")
			.setBuildCostHammers(0)
			.setBuildCostTools(0)
			.setProduce(GoodType.HAMMERS)
	 		.setSlotsForWorkers(3)
	 		.setUpgradeTo(LUMBER_MILL)
	 		.setRequiredColonyPopulation(1)
	 		.build();
  
	public final static ConstructionType IRON_WORKS = ConstructionTypeBuilder.make()
			.setName("IRON_WORKS")
			.setBuildCostHammers(240)
			.setBuildCostTools(100)
			.setProduce(GoodType.TOOLS)
	 		.setSlotsForWorkers(3)
	 		.setUpgradeTo(null)
	 		.setRequiredColonyPopulation(8)
	 		.build();
 
	public final static ConstructionType BLACKSMITHS_SHOP = ConstructionTypeBuilder.make()
			.setName("BLACKSMITHS_SHOP")
			.setBuildCostHammers(64)
			.setBuildCostTools(20)
			.setProduce(GoodType.TOOLS)
	 		.setSlotsForWorkers(3)
	 		.setUpgradeTo(IRON_WORKS)
	 		.setRequiredColonyPopulation(4)
	 		.build();

	public final static ConstructionType BLACKSMITHS_HOUSE = ConstructionTypeBuilder.make()
			.setName("BLACKSMITHS_HOUSE")
			.setBuildCostHammers(0)
			.setBuildCostTools(0)
			.setProduce(GoodType.TOOLS)
	 		.setSlotsForWorkers(3)
	 		.setUpgradeTo(BLACKSMITHS_SHOP)
	 		.setRequiredColonyPopulation(1)
	 		.build();
 
	public final static ConstructionType FORTRESS = ConstructionTypeBuilder.make()
			.setName("FORTRESS")
			.setBuildCostHammers(320)
			.setBuildCostTools(200)
			.setProduce(null)
	 		.setSlotsForWorkers(0)
	 		.setUpgradeTo(null)
	 		.setRequiredColonyPopulation(8)
	 		.build();
 
	public final static ConstructionType FORT = ConstructionTypeBuilder.make()
			.setName("FORT")
			.setBuildCostHammers(120)
			.setBuildCostTools(100)
			.setProduce(null)
	 		.setSlotsForWorkers(0)
	 		.setUpgradeTo(FORTRESS)
	 		.setRequiredColonyPopulation(40)
	 		.build();

	public final static ConstructionType STOCKADE = ConstructionTypeBuilder.make()
			.setName("STOCKADE")
			.setBuildCostHammers(64)
			.setBuildCostTools(0)
			.setProduce(null)
	 		.setSlotsForWorkers(0)
	 		.setUpgradeTo(FORT)
	 		.setRequiredColonyPopulation(3)
	 		.build();
	 
	public final static ConstructionType CIGAR_FACTORY = ConstructionTypeBuilder.make()
			.setName("CIGAR_FACTORY")
			.setBuildCostHammers(160)
			.setBuildCostTools(100)
			.setProduce(GoodType.CIGARS)
	 		.setSlotsForWorkers(3)
	 		.setUpgradeTo(null)
	 		.setRequiredColonyPopulation(8)
	 		.build();
 
	public final static ConstructionType TOBACCONISTS_SHOP = ConstructionTypeBuilder.make()
			.setName("TOBACCONISTS_SHOP")
			.setBuildCostHammers(64)
			.setBuildCostTools(20)
			.setProduce(GoodType.CIGARS)
	 		.setSlotsForWorkers(3)
	 		.setUpgradeTo(CIGAR_FACTORY)
	 		.setRequiredColonyPopulation(4)
	 		.build();
 
		public final static ConstructionType TOBACCONISTS_HOUSE = ConstructionTypeBuilder.make()
			.setName("TOBACCONISTS_HOUSE")
			.setBuildCostHammers(0)
			.setBuildCostTools(0)
			.setProduce(GoodType.CIGARS)
	 		.setSlotsForWorkers(3)
	 		.setUpgradeTo(TOBACCONISTS_SHOP)
	 		.setRequiredColonyPopulation(1)
	 		.build();

	public final static ConstructionType TEXTILE_MILL = ConstructionTypeBuilder.make()
			.setName("TEXTILE_MILL")
			.setBuildCostHammers(160)
			.setBuildCostTools(100)
			.setProduce(GoodType.SILK)
	 		.setSlotsForWorkers(3)
	 		.setUpgradeTo(null)
	 		.setRequiredColonyPopulation(8)
	 		.build();
 
	public final static ConstructionType WEAVERS_SHOP = ConstructionTypeBuilder.make()
			.setName("WEAVERS_SHOP")
			.setBuildCostHammers(64)
			.setBuildCostTools(20)
			.setProduce(GoodType.SILK)
	 		.setSlotsForWorkers(3)
	 		.setUpgradeTo(TEXTILE_MILL)
	 		.setRequiredColonyPopulation(4)
	 		.build();

	public final static ConstructionType WEAVERS_HOUSE = ConstructionTypeBuilder.make()
			.setName("WEAVERS_HOUSE")
			.setBuildCostHammers(0)
			.setBuildCostTools(0)
			.setProduce(GoodType.SILK)
	 		.setSlotsForWorkers(3)
	 		.setUpgradeTo(WEAVERS_SHOP)
	 		.setRequiredColonyPopulation(1)
	 		.build();

	public final static ConstructionType RUM_FACTORY = ConstructionTypeBuilder.make()
			.setName("RUM_FACTORY")
			.setBuildCostHammers(160)
			.setBuildCostTools(100)
			.setProduce(GoodType.RUM)
	 		.setSlotsForWorkers(3)
	 		.setUpgradeTo(null)
	 		.setRequiredColonyPopulation(8)
	 		.build();

	public final static ConstructionType RUM_DISTILLERY = ConstructionTypeBuilder.make()
			.setName("RUM_DISTILLERY")
			.setBuildCostHammers(64)
			.setBuildCostTools(20)
			.setProduce(GoodType.RUM)
	 		.setSlotsForWorkers(3)
	 		.setUpgradeTo(RUM_FACTORY)
	 		.setRequiredColonyPopulation(4)
	 		.build();

	public final static ConstructionType RUM_DISTILLERS_HOUSE = ConstructionTypeBuilder.make()
			.setName("RUM_DISTILLERS_HOUSE")
			.setBuildCostHammers(0)
			.setBuildCostTools(0)
			.setProduce(GoodType.RUM)
	 		.setSlotsForWorkers(3)
	 		.setUpgradeTo(RUM_DISTILLERY)
	 		.setRequiredColonyPopulation(1)
	 		.build();

	public final static ConstructionType FUR_FACTORY = ConstructionTypeBuilder.make()
			.setName("FUR_FACTORY")
			.setBuildCostHammers(160)
			.setBuildCostTools(100)
			.setProduce(GoodType.COAT)
	 		.setSlotsForWorkers(3)
	 		.setUpgradeTo(null)
	 		.setRequiredColonyPopulation(6)
	 		.build();
	 
	public final static ConstructionType FUR_TRADING_POST = ConstructionTypeBuilder.make()
			.setName("FUR_TRADING_POST")
			.setBuildCostHammers(56)
			.setBuildCostTools(20)
			.setProduce(GoodType.COAT)
	 		.setSlotsForWorkers(3)
	 		.setUpgradeTo(FUR_FACTORY)
	 		.setRequiredColonyPopulation(3)
	 		.build();
 
	public final static ConstructionType FUR_TRADERS_HOUSE = ConstructionTypeBuilder.make()
			.setName("FUR_TRADERS_HOUSE")
			.setBuildCostHammers(0)
			.setBuildCostTools(0)
			.setProduce(GoodType.COAT)
	 		.setSlotsForWorkers(3)
	 		.setUpgradeTo(FUR_TRADING_POST)
	 		.setRequiredColonyPopulation(1)
	 		.build();

	public final static ConstructionType ARSENAL = ConstructionTypeBuilder.make()
			.setName("ARSENAL")
			.setBuildCostHammers(240)
			.setBuildCostTools(100)
			.setProduce(GoodType.MUSKET)
	 		.setSlotsForWorkers(3)
	 		.setUpgradeTo(null)
	 		.setRequiredColonyPopulation(8)
	 		.build();

	public final static ConstructionType MAGAZINE = ConstructionTypeBuilder.make()
			.setName("MAGAZINE")
			.setBuildCostHammers(120)
			.setBuildCostTools(50)
			.setProduce(GoodType.MUSKET)
	 		.setSlotsForWorkers(3)
	 		.setUpgradeTo(ARSENAL)
	 		.setRequiredColonyPopulation(8)
	 		.build();

	public final static ConstructionType ARMORY = ConstructionTypeBuilder.make()
			.setName("ARMORY")
			.setBuildCostHammers(52)
			.setBuildCostTools(0)
			.setProduce(GoodType.MUSKET)
	 		.setSlotsForWorkers(3)
	 		.setUpgradeTo(MAGAZINE)
	 		.setRequiredColonyPopulation(1)
	 		.build();

	public final static ConstructionType SHIPYARD = ConstructionTypeBuilder.make()
			.setName("SHIPYARD")
			.setBuildCostHammers(240)
			.setBuildCostTools(100)
			.setProduce(null)
	 		.setSlotsForWorkers(0)
	 		.setUpgradeTo(null)
	 		.setRequiredColonyPopulation(8)
	 		.build();

	public final static ConstructionType DRYDOCK = ConstructionTypeBuilder.make()
			.setName("DRYDOCK")
			.setBuildCostHammers(80)
			.setBuildCostTools(50)
			.setProduce(null)
	 		.setSlotsForWorkers(0)
	 		.setUpgradeTo(SHIPYARD)
	 		.setRequiredColonyPopulation(6)
	 		.build();

	public final static ConstructionType DOCK = ConstructionTypeBuilder.make()
			.setName("DOCK")
			.setBuildCostHammers(52)
			.setBuildCostTools(0)
			.setProduce(null)
	 		.setSlotsForWorkers(0)
	 		.setUpgradeTo(DRYDOCK)
	 		.setRequiredColonyPopulation(1)
	 		.build();

	public final static ConstructionType UNIVERSITY = ConstructionTypeBuilder.make()
			.setName("UNIVERSITY")
			.setBuildCostHammers(200)
			.setBuildCostTools(100)
			.setProduce(null)
	 		.setSlotsForWorkers(3)
	 		.setUpgradeTo(null)
	 		.setRequiredColonyPopulation(10)
	 		.build();

	public final static ConstructionType COLLEGE = ConstructionTypeBuilder.make()
			.setName("COLLEGE")
			.setBuildCostHammers(160)
			.setBuildCostTools(50)
			.setProduce(null)
	 		.setSlotsForWorkers(3)
	 		.setUpgradeTo(UNIVERSITY)
	 		.setRequiredColonyPopulation(8)
	 		.build();

	public final static ConstructionType SCHOOLHOUSE = ConstructionTypeBuilder.make()
			.setName("SCHOOLHOUSE")
			.setBuildCostHammers(64)
			.setBuildCostTools(0)
			.setProduce(null)
	 		.setSlotsForWorkers(3)
	 		.setUpgradeTo(COLLEGE)
	 		.setRequiredColonyPopulation(4)
	 		.build();

	public final static ConstructionType WAREHOUSE_EXPANSION = ConstructionTypeBuilder.make()
			.setName("WAREHOUSE_EXPANSION")
			.setBuildCostHammers(80)
			.setBuildCostTools(20)
			.setProduce(null)
	 		.setSlotsForWorkers(0)
	 		.setUpgradeTo(null)
	 		.setRequiredColonyPopulation(1)
	 		.build();

	public final static ConstructionType WAREHOUSE = ConstructionTypeBuilder.make()
			.setName("WAREHOUSE")
			.setBuildCostHammers(80)
			.setBuildCostTools(0)
			.setProduce(null)
	 		.setSlotsForWorkers(0)
	 		.setUpgradeTo(WAREHOUSE_EXPANSION)
	 		.setRequiredColonyPopulation(1)
	 		.build();

	public final static ConstructionType BASIC_WAREHOUSE = ConstructionTypeBuilder.make()
			.setName("BASIC_WAREHOUSE")
			.setBuildCostHammers(0)
			.setBuildCostTools(0)
			.setProduce(null)
	 		.setSlotsForWorkers(0)
	 		.setUpgradeTo(WAREHOUSE)
	 		.setRequiredColonyPopulation(1)
	 		.build();

	public final static ConstructionType STABLES = ConstructionTypeBuilder.make()
			.setName("STABLES")
			.setBuildCostHammers(64)
			.setBuildCostTools(0)
			.setProduce(null)
	 		.setSlotsForWorkers(0)
	 		.setUpgradeTo(null)
	 		.setRequiredColonyPopulation(1)
	 		.build();

	public final static ConstructionType CATHEDRAL = ConstructionTypeBuilder.make()
			.setName("CATHEDRAL")
			.setBuildCostHammers(176)
			.setBuildCostTools(100)
			.setProduce(GoodType.CROSS)
	 		.setSlotsForWorkers(0)
	 		.setUpgradeTo(null)
	 		.setRequiredColonyPopulation(8)
	 		.build();

	public final static ConstructionType CHURCH = ConstructionTypeBuilder.make()
			.setName("CHURCH")
			.setBuildCostHammers(52)
			.setBuildCostTools(100)
			.setProduce(GoodType.CROSS)
	 		.setSlotsForWorkers(0)
	 		.setUpgradeTo(CATHEDRAL)
	 		.setRequiredColonyPopulation(3)
	 		.build();

	public final static ConstructionType NEWSPAPER = ConstructionTypeBuilder.make()
			.setName("NEWSPAPER")
			.setBuildCostHammers(120)
			.setBuildCostTools(50)
			.setProduce(GoodType.BELL)
	 		.setSlotsForWorkers(0)
	 		.setUpgradeTo(null)
	 		.setRequiredColonyPopulation(4)
	 		.build();

	public final static ConstructionType PRINTING_PRESS = ConstructionTypeBuilder.make()
			.setName("PRINTING_PRESS")
			.setBuildCostHammers(80)
			.setBuildCostTools(0)
			.setProduce(GoodType.BELL)
	 		.setSlotsForWorkers(0)
	 		.setUpgradeTo(NEWSPAPER)
	 		.setRequiredColonyPopulation(1)
	 		.build();

	public final static ConstructionType CUSTOM_HOUSE = ConstructionTypeBuilder.make()
			.setName("CUSTOM_HOUSE")
			.setBuildCostHammers(160)
			.setBuildCostTools(50)
			.setProduce(null)
	 		.setSlotsForWorkers(0)
	 		.setUpgradeTo(null)
	 		.setRequiredColonyPopulation(0)
	 		.build();

	public static List<ConstructionType> ALL = ImmutableList.of(
			TOWN_HALL,
			LUMBER_MILL,
			CARPENTERS_SHOP,
			IRON_WORKS,
			BLACKSMITHS_SHOP,
			BLACKSMITHS_HOUSE,
			FORTRESS,
			FORT,
			STOCKADE,
			CIGAR_FACTORY,
			TOBACCONISTS_SHOP,
			TOBACCONISTS_HOUSE,
			TEXTILE_MILL,
			WEAVERS_SHOP,
			WEAVERS_HOUSE,
			RUM_FACTORY,
			RUM_DISTILLERY,
			RUM_DISTILLERS_HOUSE,
			FUR_FACTORY,
			FUR_TRADING_POST,
			FUR_TRADERS_HOUSE,
			ARSENAL,
			MAGAZINE,
			ARMORY,
			SHIPYARD,
			DRYDOCK,
			DOCK,
			UNIVERSITY,
			COLLEGE,
			SCHOOLHOUSE,
			WAREHOUSE_EXPANSION,
			WAREHOUSE,
			BASIC_WAREHOUSE,
			STABLES,
			CATHEDRAL,
			CHURCH,
			NEWSPAPER,
			PRINTING_PRESS,
			CUSTOM_HOUSE);
	
	/**
	 * List of construction types that are in all newly builded colonies.
	 */
	public final static List<ConstructionType> NEW_COLONY_CONSTRUCTIONS = ALL.stream()
			.filter(constructionType -> constructionType.isBuildFromFounding())
			.collect(ImmutableList.toImmutableList());

	public final static List<ConstructionType> WAREHOUSES =  ImmutableList.of(
			WAREHOUSE_EXPANSION,
			WAREHOUSE,
			BASIC_WAREHOUSE);
	

	private static class ConstructionTypeBuilder {

		private String name;
		private int buildCostHammers;
		private int buildCostTools;
		private ConstructionType upgradeTo;
		private GoodType produce;
		private int slotsForWorkers;
		private int requiredColonyPopulation;

		private static ConstructionTypeBuilder make() {
			return new ConstructionTypeBuilder();
		}

		private ConstructionType build() {
			return new ConstructionType(name, buildCostHammers, buildCostTools, upgradeTo, produce, slotsForWorkers,
					requiredColonyPopulation);
		}

		private ConstructionTypeBuilder setName(final String name) {
			this.name = name;
			return this;
		}

		private ConstructionTypeBuilder setBuildCostHammers(final int buildCostHammers) {
			this.buildCostHammers = buildCostHammers;
			return this;
		}

		private ConstructionTypeBuilder setBuildCostTools(final int buildCostTools) {
			this.buildCostTools = buildCostTools;
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

		private ConstructionTypeBuilder setSlotsForWorkers(final int slotsForWorkers) {
			this.slotsForWorkers = slotsForWorkers;
			return this;
		}

		private ConstructionTypeBuilder setRequiredColonyPopulation(final int requiredColonyPopulation) {
			this.requiredColonyPopulation = requiredColonyPopulation;
			return this;
		}

	}

	private final String name;
	private final int buildCostHammers;
	private final int buildCostTools;
	private final ConstructionType upgradeTo;
	private final GoodType produce;
	private final int slotsForWorkers;
	private final int requiredColonyPopulation;

	private ConstructionType(final String name, final int buildCostHammers, final int buildCostTools,
			final ConstructionType upgradeTo, final GoodType produce, final int slotsForWorkers,
			final int requiredColonyPopulation) {
		this.name = Preconditions.checkNotNull(name);
		this.buildCostHammers = buildCostHammers;
		this.buildCostTools = buildCostTools;
		this.upgradeTo = upgradeTo;
		this.produce = produce;
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
	 * @return Return <code>true</code> when building type is in colony when it's
	 *         founded. When construction type have to be builded than return
	 *         <code>false</code>.
	 */
	public boolean isBuildFromFounding(){
		return buildCostHammers == 0 && buildCostTools == 0;
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(ConstructionType.class).add("name", name).toString();
	}

	public int getBuildCostHammers() {
		return buildCostHammers;
	}

	public int getBuildCostTools() {
		return buildCostTools;
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

	public int getSlotsForWorkers() {
		return slotsForWorkers;
	}

	public String name() {
		return name;
	}

	public boolean canBeConstructed() {
		return buildCostHammers > 0 && buildCostTools > 0;
	}

	public int getRequiredColonyPopulation() {
		return requiredColonyPopulation;
	}

}
