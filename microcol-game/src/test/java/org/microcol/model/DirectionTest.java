package org.microcol.model;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * Verify direction enumeration definition.
 */
@RunWith(Parameterized.class)
public class DirectionTest {

    @Parameters(name = "{index}: vector = {0}, isNorth = {1}, isEast = {2}, isSouth = {3}, isWest = {4}, direction = {5}")
    public static Collection<Object[]> data() {
	return Arrays.asList(
		new Object[][] {
		    	{ Location.of(0, -1), true, false, false, false, Direction.north },
			{ Location.of(1, -1), true, true, false, false, Direction.northEast },
			{ Location.of(1, 0), false, true, false, false, Direction.east },
			{ Location.of(1, 1), false, true, true, false, Direction.southEast },
			{ Location.of(0, 1), false, false, true, false, Direction.south },
			{ Location.of(-1, 1), false, false, true, true, Direction.southWest },
			{ Location.of(-1, 0), false, false, false, true, Direction.west },
			{ Location.of(-1, -1), true, false, false, true, Direction.northWest }});
    }

    @Parameter(0)
    public Location location;

    @Parameter(1)
    public boolean isNorth;

    @Parameter(2)
    public boolean isEast;

    @Parameter(3)
    public boolean isSouth;

    @Parameter(4)
    public boolean isWest;

    @Parameter(5)
    public Direction direction;

    @Test
    public void test_diOrection() {
	Direction found = Direction.valueOf(location);
	Assert.assertNotNull(found);
	Assert.assertEquals(direction, found);
	Assert.assertEquals(isNorth, found.isOrientedNorth());
	Assert.assertEquals(isEast, found.isOrientedEast());
	Assert.assertEquals(isSouth, found.isOrientedSouth());
	Assert.assertEquals(isWest, found.isOrientedWest());
    }

}
