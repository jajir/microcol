package org.microcol.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.easymock.EasyMock;
import org.junit.Test;
import org.microcol.gui.panelview.Area;
import org.microcol.gui.panelview.GamePanelView;
import org.microcol.gui.panelview.VisibleArea;
import org.microcol.model.Location;
import org.microcol.model.WorldMap;

/**
 * Tests for {@link Area}.
 *
 */
public class AreaTest {

	@Test
	public void test_small_map_huge_view() {
		Area area = makeArea(0, 0, 320, 237, 3, 3);

		assertEquals(1, area.getTopLeft().getX());
		assertEquals(1, area.getTopLeft().getY());

		assertEquals(3, area.getBottomRight().getX());
		assertEquals(3, area.getBottomRight().getY());

		assertEquals(3, area.getWidth());
		assertEquals(3, area.getHeight());
	}

	@Test
	public void test_constructor() {
		Area area = makeArea(222, 222, 800, 600, 500, 500);

		assertEquals(7, area.getTopLeft().getX());
		assertEquals(7, area.getTopLeft().getY());

		assertEquals(31, area.getBottomRight().getX());
		assertEquals(25, area.getBottomRight().getY());

		assertEquals(25, area.getWidth());
		assertEquals(19, area.getHeight());
	}

	@Test
	public void test_getCenterAreaTo_middle_of_map() throws Exception {
		Area area = makeArea(222, 222, 800, 600, 100 * GamePanelView.TILE_WIDTH_IN_PX,
				100 * GamePanelView.TILE_WIDTH_IN_PX);

		Point po = area.getCenterToLocation(Location.of(50, 50));

		assertEquals(618, po.getX());
		assertEquals(718, po.getY());
	}

	private Area makeArea(final int viewTopLeftCornerX, final int viewTopLeftCornerY, final int viewWidth,
			final int viewHeight, final int maxMapLocationX, final int maxMapLocationY) {
		// final VisibleArea bounds = new VisibleArea(viewTopLeftCornerX,
		// viewTopLeftCornerY, viewWidth, viewHeight);
		final VisibleArea visibleArea = EasyMock.createMock(VisibleArea.class);
		final Point topLeft = Point.of(viewTopLeftCornerX, viewTopLeftCornerY);
		final Point bottomRight = topLeft.add(viewWidth, viewHeight);
		EasyMock.expect(visibleArea.getTopLeft()).andReturn(topLeft);
		EasyMock.expect(visibleArea.getBottomRight()).andReturn(bottomRight);

		final WorldMap map = EasyMock.createMock(WorldMap.class);
		EasyMock.expect(map.getMaxX()).andReturn(maxMapLocationX);
		EasyMock.expect(map.getMaxY()).andReturn(maxMapLocationY);

		EasyMock.replay(map, visibleArea);
		Area out = new Area(visibleArea, map);
		EasyMock.verify(map, visibleArea);
		assertNotNull(out);
		return out;
	}

}
