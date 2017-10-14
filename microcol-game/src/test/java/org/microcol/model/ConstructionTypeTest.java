package org.microcol.model;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import mockit.Expectations;
import mockit.Mocked;

@RunWith(Parameterized.class)
public class ConstructionTypeTest {
	
	@Parameters(name = "{index}: construction type = {0}, productionPerTurn = {1}, baseProductionPerTurn = {2}, consumptionPerTurn = {3}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
			{ConstructionType.ARMORY, 3, 0, 3},
			{ConstructionType.LUMBER_MILL, 6, 0, 6},
			{ConstructionType.TOWN_HALL, 3, 1, 0},
			{ConstructionType.CHURCH, 3, 1, 0},
		});
	}

	@Parameter(0)
	public ConstructionType constructionType;

	@Parameter(1)
	public Integer productionPerTurn;

	@Parameter(2)
	public Integer baseProductionPerTurn;

	@Parameter(3)
	public Integer consumptionPerTurn;
	
	private @Mocked Colony colony;

	@Test
	public void test_verify_construction_type_production() throws Exception {
		assertEquals(consumptionPerTurn.intValue(), constructionType.getConsumptionPerTurn());
		assertEquals(productionPerTurn.intValue(), constructionType.getProductionPerTurn());
		assertEquals(baseProductionPerTurn.intValue(), constructionType.getBaseProductionPerTurn());
	}
	
	@Test
	public void test_verify_that_production_is_correct() throws Exception {
		final ConstructionProduction prod = constructionType.getConstructionProduction(colony);
		
		assertEquals(consumptionPerTurn.intValue(), prod.getConsumptionPerTurn());
		assertEquals(productionPerTurn.intValue(), prod.getProductionPerTurn());
		assertEquals(baseProductionPerTurn.intValue(), prod.getBaseProductionPerTurn());
		if(constructionType.getConsumed().isPresent()){
			assertEquals(constructionType.getConsumed().get(), prod.getConsumedGoods());
		}else{
			assertEquals(null, prod.getConsumedGoods());
			assertEquals(0, prod.getConsumptionPerTurn());
		}
		assertEquals(constructionType.getProduce().get(), prod.getProducedGoods());
	}
	
	@Test
	public void test_verify_production_multiplier() throws Exception {
		final ConstructionProduction prod = constructionType.getConstructionProduction(colony);
		
		final ConstructionProduction test = prod.multiply(2F);
		final int consumption = (int)(constructionType.getConsumptionPerTurn() * 2F);
		final int production = (int)(constructionType.getProductionPerTurn() * 2F);
		assertEquals(consumption, test.getConsumptionPerTurn());
		assertEquals(production, test.getProductionPerTurn());
	}
	
	@Test
	public void test_limit_consumpton_by_warehouse(final @Mocked ColonyWarehouse warehouse) throws Exception {
		final ConstructionProduction prod = constructionType.getConstructionProduction(colony);
		if(consumptionPerTurn == 0){
			//consumption is 0 so no limits is apply
		}else{
			new Expectations() {{
				warehouse.getGoodAmmount(constructionType.getConsumed().get()); result = 1; times = 1;
			}};
			final ConstructionProduction test = prod.limit(warehouse);
			assertEquals(1, test.getConsumptionPerTurn());
			assertEquals(1, test.getProductionPerTurn());
		}
	}
	
	@Test
	public void test_consume(final @Mocked ColonyWarehouse warehouse) throws Exception {
		final ConstructionProduction prod = constructionType.getConstructionProduction(colony);
		new Expectations() {{
			if(consumptionPerTurn != 0){
				warehouse.removeFromWarehouse(constructionType.getConsumed().get(), consumptionPerTurn);
			}
			warehouse.addToWarehouse(constructionType.getProduce().get(), productionPerTurn + baseProductionPerTurn);
		}};
		prod.consume(warehouse);
	}
	
}
