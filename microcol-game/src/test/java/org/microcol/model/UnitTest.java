package org.microcol.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.function.Function;

import org.junit.Test;
import org.microcol.model.unit.UnitAction;
import org.microcol.model.unit.UnitActionNoAction;

import com.google.common.collect.Lists;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;

/**
 * Verify basic unit operations.
 * <p>
 * This test shows how to mock all unit's private final members.
 * </p>
 */
public class UnitTest {
	
	@Tested
	private Unit unit;
	
	@Injectable
	private Function<Unit, Cargo> cargoProvider;
	
	@Injectable
	private Model model;
	
	@Injectable(value = "4")
	private Integer id;
	
	@Injectable
	private Function<Unit, Place> placeBuilder;
	
	@Injectable
	private UnitType unitType;
	
	@Injectable
	private Player owner;
	
	@Injectable(value="3")
	private int availableMoves;
	
	@Injectable
	private UnitAction unitAction = new UnitActionNoAction();
	
	@Test
	public void test_isSameOwner_emptyList() throws Exception {
		assertEquals(4, unit.getId());
		
		boolean ret = unit.isSameOwner(Lists.newArrayList());
		
		assertTrue(ret);
	}
	
	@Test
	public void test_isSameOwner_oneUnit_different(final @Injectable Unit u1) throws Exception {
		assertNotNull(unit);
		assertNotNull(u1);
		new Expectations() {{
			u1.getOwner(); result = null;
		}};
		
		boolean ret = unit.isSameOwner(Lists.newArrayList(u1));
		
		assertFalse(ret);
	}
	
	@Test
	public void test_isSameOwner_oneUnit_sameOwner(final @Injectable Unit u1) throws Exception {
		assertNotNull(unit);
		assertNotNull(u1);
		new Expectations() {{
			u1.getOwner(); result = owner;
		}};
		
		boolean ret = unit.isSameOwner(Lists.newArrayList(u1));
		
		assertTrue(ret);
	}
	
	@Test
	public void test_isSameOwner_twoUnit_bothSameOwner(final @Injectable Unit u1, final @Injectable Unit u2) throws Exception {
		assertNotNull(unit);
		assertNotNull(u1);
		new Expectations() {{
			u1.getOwner(); result = owner;
			u2.getOwner(); result = owner;
		}};
		
		boolean ret = unit.isSameOwner(Lists.newArrayList(u1, u2));
		
		assertTrue(ret);
	}
	
	@Test
	public void test_isSameOwner_twoUnit_secondIsDifferentOwner(final @Injectable Unit u1, final @Injectable Unit u2) throws Exception {
		assertNotNull(unit);
		assertNotNull(u1);
		new Expectations() {{
			u1.getOwner(); result = owner;
			u2.getOwner(); result = null;
		}};
		
		boolean ret = unit.isSameOwner(Lists.newArrayList(u1, u2));
		
		assertFalse(ret);
	}
	
}
