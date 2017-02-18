package org.microcol.gui;

import static org.junit.Assert.assertEquals;

import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.microcol.gui.Text.Language;

public class TextTest {

	private GamePreferences gamePreferences;

	@Test
	public void test_crate_english() throws Exception {
		EasyMock.expect(gamePreferences.getLanguage()).andReturn(Language.en);
		EasyMock.replay(gamePreferences);
		Text text = new Text(gamePreferences);

		assertEquals("About MicroCol", text.get("aboutDialog.caption"));
		EasyMock.verify(gamePreferences);
	}

	@Test
	public void test_crate_czech() throws Exception {
		EasyMock.expect(gamePreferences.getLanguage()).andReturn(Language.cz);
		EasyMock.replay(gamePreferences);
		Text text = new Text(gamePreferences);

		assertEquals("O MicroCol", text.get("aboutDialog.caption"));
		EasyMock.verify(gamePreferences);
	}

	@Test
	public void test_create_english_swith_to_czech() throws Exception {
		EasyMock.expect(gamePreferences.getLanguage()).andReturn(Language.en);
		EasyMock.replay(gamePreferences);
		Text text = new Text(gamePreferences);
		text.setLanguage(Language.cz);
		
		assertEquals("O MicroCol", text.get("aboutDialog.caption"));
		EasyMock.verify(gamePreferences);
	}

	@Before
	public void setup() {
		gamePreferences = EasyMock.createMock(GamePreferences.class);
	}

	@After
	public void tearDown() {
		gamePreferences = null;
	}

}
