package org.microcol.gui;

import java.util.Arrays;
import java.util.Collection;
import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.base.Preconditions;

@RunWith(Parameterized.class)
public class RectangleTest {
	
	@Parameters(name = "{index}: Reactangle={0}, Point (x = {1}, y = {2}) should be inside {3} rectangle")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
			{ Rectangle.of(Point.of(10, 10), Point.of(30, 30)), 0, 0, Boolean.FALSE },
			{ Rectangle.of(Point.of(10, 10), Point.of(30, 30)), 100, 100, Boolean.FALSE },
			{ Rectangle.of(Point.of(10, 10), Point.of(30, 30)), 10, 10, Boolean.TRUE },
			{ Rectangle.of(Point.of(10, 10), Point.of(30, 30)), 20, 20, Boolean.TRUE }
		});
	}
	
	@Parameter(0)
	public Rectangle rectangle;

	@Parameter(1)
	public int x;

	@Parameter(2)
	public int y;
	
	@Parameter(3)
	public boolean shouldBeIn;
	
	@Test
	public void testName() throws Exception {
		Preconditions.checkNotNull(rectangle);
		Preconditions.checkNotNull(shouldBeIn);
		assertEquals(shouldBeIn, rectangle.isIn(Point.of(x, y)));
	}

}
