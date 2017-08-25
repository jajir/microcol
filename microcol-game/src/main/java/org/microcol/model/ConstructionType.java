package org.microcol.model;

import java.util.Optional;

public class ConstructionType {
	 
	public final static ConstructionType TOWN_HALL = ConstructionTypeBuilder.make()
				.setBuildCostHammers(0)
				.setBuildCostTools(0)
				.setProduce(GoodType.BELL)
		 		.setSlotsForWorkers(3)
		 		.setUpgradeTo(null)
		 		.setRequiredTownPopulation(1)
		 		.build();

	public final static ConstructionType LUMBER_MILL = ConstructionTypeBuilder.make()
				.setBuildCostHammers(52)
				.setBuildCostTools(0)
				.setProduce(GoodType.HAMMERS)
		 		.setSlotsForWorkers(3)
		 		.setUpgradeTo(null)
		 		.setRequiredTownPopulation(3)
		 		.build();

	public final static ConstructionType CARPENTERS_SHOP = ConstructionTypeBuilder.make()
				.setBuildCostHammers(0)
				.setBuildCostTools(0)
				.setProduce(GoodType.HAMMERS)
		 		.setSlotsForWorkers(3)
		 		.setUpgradeTo(LUMBER_MILL)
		 		.setRequiredTownPopulation(1)
		 		.build();
  
	public final static ConstructionType IRON_WORKS = ConstructionTypeBuilder.make()
				.setBuildCostHammers(240)
				.setBuildCostTools(100)
				.setProduce(GoodType.TOOLS)
		 		.setSlotsForWorkers(3)
		 		.setUpgradeTo(null)
		 		.setRequiredTownPopulation(8)
		 		.build();
 
	public final static ConstructionType BLACKSMITHS_SHOP = ConstructionTypeBuilder.make()
				.setBuildCostHammers(64)
				.setBuildCostTools(20)
				.setProduce(GoodType.TOOLS)
		 		.setSlotsForWorkers(3)
		 		.setUpgradeTo(IRON_WORKS)
		 		.setRequiredTownPopulation(4)
		 		.build();

	public final static ConstructionType BLACKSMITHS_HOUSE = ConstructionTypeBuilder.make()
				.setBuildCostHammers(0)
				.setBuildCostTools(0)
				.setProduce(GoodType.TOOLS)
		 		.setSlotsForWorkers(3)
		 		.setUpgradeTo(BLACKSMITHS_SHOP)
		 		.setRequiredTownPopulation(1)
		 		.build();
 
	public final static ConstructionType FORTRESS = ConstructionTypeBuilder.make()
				.setBuildCostHammers(320)
				.setBuildCostTools(200)
				.setProduce(null)
		 		.setSlotsForWorkers(0)
		 		.setUpgradeTo(null)
		 		.setRequiredTownPopulation(8)
		 		.build();
 
	public final static ConstructionType FORT = ConstructionTypeBuilder.make()
				.setBuildCostHammers(120)
				.setBuildCostTools(100)
				.setProduce(null)
		 		.setSlotsForWorkers(0)
		 		.setUpgradeTo(FORTRESS)
		 		.setRequiredTownPopulation(40)
		 		.build();

	public final static ConstructionType STOCKADE = ConstructionTypeBuilder.make()
				.setBuildCostHammers(64)
				.setBuildCostTools(0)
				.setProduce(null)
		 		.setSlotsForWorkers(0)
		 		.setUpgradeTo(FORT)
		 		.setRequiredTownPopulation(3)
		 		.build();
	 
	public final static ConstructionType CIGAR_FACTORY = ConstructionTypeBuilder.make()
				.setBuildCostHammers(160)
				.setBuildCostTools(100)
				.setProduce(GoodType.CIGARS)
		 		.setSlotsForWorkers(3)
		 		.setUpgradeTo(null)
		 		.setRequiredTownPopulation(8)
		 		.build();
 
	public final static ConstructionType TOBACCONISTS_SHOP = ConstructionTypeBuilder.make()
				.setBuildCostHammers(64)
				.setBuildCostTools(20)
				.setProduce(GoodType.CIGARS)
		 		.setSlotsForWorkers(3)
		 		.setUpgradeTo(CIGAR_FACTORY)
		 		.setRequiredTownPopulation(4)
		 		.build();
 
		public final static ConstructionType TOBACCONISTS_HOUSE = ConstructionTypeBuilder.make()
				.setBuildCostHammers(0)
				.setBuildCostTools(0)
				.setProduce(GoodType.CIGARS)
		 		.setSlotsForWorkers(3)
		 		.setUpgradeTo(TOBACCONISTS_SHOP)
		 		.setRequiredTownPopulation(1)
		 		.build();

	public final static ConstructionType TEXTILE_MILL = ConstructionTypeBuilder.make()
				.setBuildCostHammers(160)
				.setBuildCostTools(100)
				.setProduce(GoodType.SILK)
		 		.setSlotsForWorkers(3)
		 		.setUpgradeTo(null)
		 		.setRequiredTownPopulation(8)
		 		.build();
 
	public final static ConstructionType WEAVERS_SHOP = ConstructionTypeBuilder.make()
				.setBuildCostHammers(64)
				.setBuildCostTools(20)
				.setProduce(GoodType.SILK)
		 		.setSlotsForWorkers(3)
		 		.setUpgradeTo(TEXTILE_MILL)
		 		.setRequiredTownPopulation(4)
		 		.build();

	public final static ConstructionType WEAVERS_HOUSE = ConstructionTypeBuilder.make()
				.setBuildCostHammers(0)
				.setBuildCostTools(0)
				.setProduce(GoodType.SILK)
		 		.setSlotsForWorkers(3)
		 		.setUpgradeTo(WEAVERS_SHOP)
		 		.setRequiredTownPopulation(1)
		 		.build();

	public final static ConstructionType RUM_FACTORY = ConstructionTypeBuilder.make()
				.setBuildCostHammers(160)
				.setBuildCostTools(100)
				.setProduce(GoodType.RUM)
		 		.setSlotsForWorkers(3)
		 		.setUpgradeTo(null)
		 		.setRequiredTownPopulation(8)
		 		.build();

	public final static ConstructionType RUM_DISTILLERY = ConstructionTypeBuilder.make()
				.setBuildCostHammers(64)
				.setBuildCostTools(20)
				.setProduce(GoodType.RUM)
		 		.setSlotsForWorkers(3)
		 		.setUpgradeTo(RUM_FACTORY)
		 		.setRequiredTownPopulation(4)
		 		.build();

	public final static ConstructionType RUM_DISTILLERS_HOUSE = ConstructionTypeBuilder.make()
				.setBuildCostHammers(0)
				.setBuildCostTools(0)
				.setProduce(GoodType.RUM)
		 		.setSlotsForWorkers(3)
		 		.setUpgradeTo(RUM_DISTILLERY)
		 		.setRequiredTownPopulation(1)
		 		.build();

	public final static ConstructionType FUR_FACTORY = ConstructionTypeBuilder.make()
				.setBuildCostHammers(160)
				.setBuildCostTools(100)
				.setProduce(GoodType.COAT)
		 		.setSlotsForWorkers(3)
		 		.setUpgradeTo(null)
		 		.setRequiredTownPopulation(6)
		 		.build();
	 
	public final static ConstructionType FUR_TRADING_POST = ConstructionTypeBuilder.make()
				.setBuildCostHammers(56)
				.setBuildCostTools(20)
				.setProduce(GoodType.COAT)
		 		.setSlotsForWorkers(3)
		 		.setUpgradeTo(FUR_FACTORY)
		 		.setRequiredTownPopulation(3)
		 		.build();
 
	public final static ConstructionType FUR_TRADERS_HOUSE = ConstructionTypeBuilder.make()
				.setBuildCostHammers(0)
				.setBuildCostTools(0)
				.setProduce(GoodType.COAT)
		 		.setSlotsForWorkers(3)
		 		.setUpgradeTo(FUR_TRADING_POST)
		 		.setRequiredTownPopulation(1)
		 		.build();

	public final static ConstructionType ARSENAL = ConstructionTypeBuilder.make()
				.setBuildCostHammers(240)
				.setBuildCostTools(100)
				.setProduce(GoodType.MUSKET)
		 		.setSlotsForWorkers(3)
		 		.setUpgradeTo(null)
		 		.setRequiredTownPopulation(8)
		 		.build();

	public final static ConstructionType MAGAZINE = ConstructionTypeBuilder.make()
				.setBuildCostHammers(120)
				.setBuildCostTools(50)
				.setProduce(GoodType.MUSKET)
		 		.setSlotsForWorkers(3)
		 		.setUpgradeTo(ARSENAL)
		 		.setRequiredTownPopulation(8)
		 		.build();

	public final static ConstructionType ARMORY = ConstructionTypeBuilder.make()
				.setBuildCostHammers(52)
				.setBuildCostTools(0)
				.setProduce(GoodType.MUSKET)
		 		.setSlotsForWorkers(3)
		 		.setUpgradeTo(MAGAZINE)
		 		.setRequiredTownPopulation(1)
		 		.build();

	public final static ConstructionType SHIPYARD = ConstructionTypeBuilder.make()
				.setBuildCostHammers(240)
				.setBuildCostTools(100)
				.setProduce(null)
		 		.setSlotsForWorkers(0)
		 		.setUpgradeTo(null)
		 		.setRequiredTownPopulation(8)
		 		.build();

	public final static ConstructionType DRYDOCK = ConstructionTypeBuilder.make()
				.setBuildCostHammers(80)
				.setBuildCostTools(50)
				.setProduce(null)
		 		.setSlotsForWorkers(0)
		 		.setUpgradeTo(SHIPYARD)
		 		.setRequiredTownPopulation(6)
		 		.build();

	public final static ConstructionType DOCK = ConstructionTypeBuilder.make()
				.setBuildCostHammers(52)
				.setBuildCostTools(0)
				.setProduce(null)
		 		.setSlotsForWorkers(0)
		 		.setUpgradeTo(DRYDOCK)
		 		.setRequiredTownPopulation(1)
		 		.build();

	public final static ConstructionType UNIVERSITY = ConstructionTypeBuilder.make()
				.setBuildCostHammers(200)
				.setBuildCostTools(100)
				.setProduce(null)
		 		.setSlotsForWorkers(3)
		 		.setUpgradeTo(null)
		 		.setRequiredTownPopulation(10)
		 		.build();

	public final static ConstructionType COLLEGE = ConstructionTypeBuilder.make()
				.setBuildCostHammers(160)
				.setBuildCostTools(50)
				.setProduce(null)
		 		.setSlotsForWorkers(3)
		 		.setUpgradeTo(UNIVERSITY)
		 		.setRequiredTownPopulation(8)
		 		.build();

	public final static ConstructionType SCHOOLHOUSE = ConstructionTypeBuilder.make()
				.setBuildCostHammers(64)
				.setBuildCostTools(0)
				.setProduce(null)
		 		.setSlotsForWorkers(3)
		 		.setUpgradeTo(COLLEGE)
		 		.setRequiredTownPopulation(4)
		 		.build();

	public final static ConstructionType WAREHOUSE_EXPANSION = ConstructionTypeBuilder.make()
				.setBuildCostHammers(80)
				.setBuildCostTools(20)
				.setProduce(null)
		 		.setSlotsForWorkers(0)
		 		.setUpgradeTo(null)
		 		.setRequiredTownPopulation(1)
		 		.build();

	public final static ConstructionType WAREHOUSE = ConstructionTypeBuilder.make()
				.setBuildCostHammers(80)
				.setBuildCostTools(0)
				.setProduce(null)
		 		.setSlotsForWorkers(0)
		 		.setUpgradeTo(WAREHOUSE_EXPANSION)
		 		.setRequiredTownPopulation(1)
		 		.build();

	public final static ConstructionType STABLES = ConstructionTypeBuilder.make()
				.setBuildCostHammers(64)
				.setBuildCostTools(0)
				.setProduce(null)
		 		.setSlotsForWorkers(0)
		 		.setUpgradeTo(null)
		 		.setRequiredTownPopulation(1)
		 		.build();

	public final static ConstructionType CATHEDRAL = ConstructionTypeBuilder.make()
				.setBuildCostHammers(176)
				.setBuildCostTools(100)
				.setProduce(GoodType.CROSS)
		 		.setSlotsForWorkers(0)
		 		.setUpgradeTo(null)
		 		.setRequiredTownPopulation(8)
		 		.build();

	public final static ConstructionType CHURCH = ConstructionTypeBuilder.make()
				.setBuildCostHammers(52)
				.setBuildCostTools(100)
				.setProduce(GoodType.CROSS)
		 		.setSlotsForWorkers(0)
		 		.setUpgradeTo(CATHEDRAL)
		 		.setRequiredTownPopulation(3)
		 		.build();

	public final static ConstructionType NEWSPAPER = ConstructionTypeBuilder.make()
				.setBuildCostHammers(120)
				.setBuildCostTools(50)
				.setProduce(GoodType.BELL)
		 		.setSlotsForWorkers(0)
		 		.setUpgradeTo(null)
		 		.setRequiredTownPopulation(4)
		 		.build();

	public final static ConstructionType PRINTING_PRESS = ConstructionTypeBuilder.make()
				.setBuildCostHammers(80)
				.setBuildCostTools(0)
				.setProduce(GoodType.BELL)
		 		.setSlotsForWorkers(0)
		 		.setUpgradeTo(NEWSPAPER)
		 		.setRequiredTownPopulation(1)
		 		.build();

	public final static ConstructionType CUSTOM_HOUSE = ConstructionTypeBuilder.make()
				.setBuildCostHammers(160)
				.setBuildCostTools(50)
				.setProduce(null)
		 		.setSlotsForWorkers(0)
		 		.setUpgradeTo(null)
		 		.setRequiredTownPopulation(0)
		 		.build();

	private static class ConstructionTypeBuilder {

		private int buildCostHammers;
		private int buildCostTools;
		private ConstructionType upgradeTo;
		private GoodType produce;
		private int slotsForWorkers;
		private int requiredTownPopulation;

		private static ConstructionTypeBuilder make() {
			return new ConstructionTypeBuilder();
		}

		private ConstructionType build() {
			return new ConstructionType(buildCostHammers, buildCostTools, upgradeTo, produce, slotsForWorkers,
					requiredTownPopulation);
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

		private ConstructionTypeBuilder setRequiredTownPopulation(final int requiredTownPopulation) {
			this.requiredTownPopulation = requiredTownPopulation;
			return this;
		}

	}

	private final int buildCostHammers;
	private final int buildCostTools;
	private final ConstructionType upgradeTo;
	private final GoodType produce;
	private final int slotsForWorkers;
	private final int requiredTownPopulation;

	private ConstructionType(final int buildCostHammers, final int buildCostTools, final ConstructionType upgradeTo,
			final GoodType produce, final int slotsForWorkers, final int requiredTownPopulation) {
		this.buildCostHammers = buildCostHammers;
		this.buildCostTools = buildCostTools;
		this.upgradeTo = upgradeTo;
		this.produce = produce;
		this.slotsForWorkers = slotsForWorkers;
		this.requiredTownPopulation = requiredTownPopulation;
	}

	public int getBuildCostHammers() {
		return buildCostHammers;
	}

	public int getBuildCostTools() {
		return buildCostTools;
	}

	public Optional<ConstructionType> getUpgradeTo() {
		return Optional.of(upgradeTo);
	}

	public Optional<GoodType> getProduce() {
		return Optional.of(produce);
	}

	public int getSlotsForWorkers() {
		return slotsForWorkers;
	}

	public boolean canBeConstructed() {
		return buildCostHammers > 0 && buildCostTools > 0;
	}

	public int getRequiredTownPopulation() {
		return requiredTownPopulation;
	}

}
