package org.microcol.gui;

import static org.junit.Assert.assertFalse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.microcol.gui.panelview.ViewState;

public class ViewStateTest {

	private ViewState viewState;

	@Test
	public void test_getInitialValues() throws Exception {
		assertFalse(viewState.getSelectedTile().isPresent());
	}

	@Before
	public void setUp() {
		viewState = new ViewState();
	}

	@After
	public void tearDown() {
		viewState = null;
	}

}
