package org.microcol.model;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class GoodTradeTest {
	
	@Parameters(name = "{index}: sellPrice = {0}, buyPrice = {1}, availableGold = {2}, expectedAmount = {3}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
			// [0, 0, 0, 0]
			{9, 12, 10000, 100},
			{9, 12, 120, 10},
			{9, 12, 131, 10},
		});
	}

	@Parameter(0)
	public int sellPrice;

	@Parameter(1)
	public int buyPrice;

	@Parameter(2)
	public int availableGold;

	@Parameter(3)
	public int expectedAmount;
			
	@Test
	public void test_getAvailableAmountFor() throws Exception {
		GoodTrade gt = new GoodTrade(GoodType.CIGARS, sellPrice, buyPrice);
		
		GoodAmount ga = gt.getAvailableAmountFor(availableGold);
		
		assertEquals(GoodType.CIGARS, ga.getGoodType());
		assertEquals(expectedAmount, ga.getAmount());
	}
	
}
