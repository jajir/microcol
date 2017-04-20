package org.microcol.gui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.Text.Language;

public class TextTest {

	@Test
	public void test_crate_english() throws Exception {
		Text text = new Text(Language.en.getLocale());

		assertEquals("About MicroCol", text.get("aboutDialog.caption"));
	}

	@Test
	public void test_crate_czech() throws Exception {
		Text text = new Text(Language.cz.getLocale());

		assertEquals("O MicroCol", text.get("aboutDialog.caption"));
	}

	@Test
	public void test_create_english_swith_to_czech() throws Exception {
		Text text = new Text(Language.en.getLocale());
		text.setLocale(Language.cz.getLocale());

		assertEquals("O MicroCol", text.get("aboutDialog.caption"));
	}

}
