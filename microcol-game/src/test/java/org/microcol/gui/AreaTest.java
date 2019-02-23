package org.microcol.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.microcol.gui.screen.game.gamepanel.Area;
import org.microcol.gui.screen.game.gamepanel.GamePanelView;
import org.microcol.gui.screen.game.gamepanel.VisibleArea;
import org.microcol.model.Location;
import org.microcol.model.WorldMap;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;

/**
 * Tests for {@link Area}.
 *
 */
@RunWith(JMockit.class)
public class AreaTest {

	private @Mocked VisibleArea visibleArea;
	
	private @Mocked WorldMap map;

	@Test
	public void test_small_map_huge_view() {
		Area area = makeArea(0, 0, 320, 237, 3, 3);

		assertEquals(1, area.getTopLeft().getX());
		assertEquals(1, area.getTopLeft().getY());

		assertEquals(3, area.getBottomRight().getX());
		assertEquals(3, area.getBottomRight().getY());
	}

	@Test
	public void test_constructor() {
		Area area = makeArea(222, 222, 800, 600, 500, 500);

		assertEquals(5, area.getTopLeft().getX());
		assertEquals(5, area.getTopLeft().getY());

		assertEquals(24, area.getBottomRight().getX());
		assertEquals(20, area.getBottomRight().getY());
	}

	@Test
	public void test_getCenterAreaTo_middle_of_map() throws Exception {
		Area area = makeArea(222, 222, 800, 600, 100 * GamePanelView.TILE_WIDTH_IN_PX,
				100 * GamePanelView.TILE_WIDTH_IN_PX);
		new Expectations() {{
			visibleArea.getCanvasWidth();result=800;times=1;
			visibleArea.getCanvasHeight();result=600;times=1;
			visibleArea.scrollToPoint(Point.of(1850, 1950));result=Point.of(1351, 1451);
		}};
		
		Point po = area.getCenterToLocation(Location.of(50, 50));
		
		new Verifications() {{
			visibleArea.scrollToPoint(Point.of(1850, 1950));
		}};
		
		assertEquals(1351, po.getX());
		assertEquals(1451, po.getY());
	}

	private Area makeArea(final int viewTopLeftCornerX, final int viewTopLeftCornerY, final int viewWidth,
			final int viewHeight, final int maxMapLocationX, final int maxMapLocationY) {
		final Point topLeft = Point.of(viewTopLeftCornerX, viewTopLeftCornerY);
		final Point bottomRight = topLeft.add(viewWidth, viewHeight);
		new Expectations() {{
			visibleArea.getTopLeft();result=topLeft;times=1;
			visibleArea.getBottomRight();result=bottomRight;times=1;
		}};
		
		new Expectations() {{
			map.getMaxX();result=maxMapLocationX;times=1;
			map.getMaxY();result=maxMapLocationY;times=1;
		}};
		Area out = new Area(visibleArea, map);
		assertNotNull(out);
		return out;
	}

}
