package org.microcol.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.awt.Dimension;

import javax.swing.JViewport;

import org.easymock.classextension.EasyMock;
import org.junit.Test;
import org.microcol.gui.panelview.Area;
import org.microcol.model.World;

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
		Area area = makeArea(222, 222, 800, 600, 500, 500);

		Point po = area.getCenterAreaTo(Point.of(1000, 1000));

		assertEquals(618, po.getX());
		assertEquals(718, po.getY());
	}

	private Area makeArea(final int viewTopLeftCornerX, final int viewTopLeftCornerY, final int viewWidth,
			final int viewHeight, final int maxMapLocationX, final int maxMapLocationY) {
		JViewport jViewport = EasyMock.createMock(JViewport.class);
		EasyMock.expect(jViewport.getExtentSize()).andReturn(new Dimension(viewWidth, viewHeight));
		EasyMock.expect(jViewport.getViewPosition())
				.andReturn(new java.awt.Point(viewTopLeftCornerX, viewTopLeftCornerY));

		World map = EasyMock.createMock(World.class);
		EasyMock.expect(map.getMaxX()).andReturn(maxMapLocationX);
		EasyMock.expect(map.getMaxY()).andReturn(maxMapLocationY);

		EasyMock.replay(jViewport, map);
		Area out = new Area(jViewport, map);
		EasyMock.verify(jViewport, map);
		assertNotNull(out);
		return out;
	}

}
