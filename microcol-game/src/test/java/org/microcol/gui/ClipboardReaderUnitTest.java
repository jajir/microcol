package org.microcol.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.microcol.gui.util.ClipboardReader;
import org.microcol.gui.util.ClipboardReader.ParsingResult;
import org.microcol.model.Model;
import org.microcol.model.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.input.Dragboard;
import mockit.Expectations;
import mockit.Mocked;

@RunWith(Parameterized.class)
public class ClipboardReaderUnitTest {

	private final Logger logger = LoggerFactory.getLogger(ClipboardReaderUnitTest.class);

	private @Mocked Model model;

	private @Mocked Dragboard db;

	private @Mocked Unit unit;
	
	@Parameters(name = "{index}: zadani = {0}, returnMockedUnit = {1}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
			{"Unit,232,FromUnit,12,2", true,  true},
			{"Unit,232,FromUnIT,12,2", true,  false},
			{"Unit,232,FromUnit,1,bl", true,  false},
			{"Unit,232,FromUnit,bl,2", true,  false},
			{"Unit,232,FromUnit,12",   true,  false},
			{"Unit,232,,,",            true,  false},
			{"Unit,232,",              true,  false},
			{"Unit,232",               true,  false},
			{"UnIT,232,FromUnit,12,2", false, false},
			{"Unit,blee",              false, false},
			{"Unit,",                  false, false},
			{"Unit",                   false, false},
			{",,",                     false, false},
			{"",                       false, false},
			{null,                     false, false},
		});
	}
	
	public @Parameter(0) String zadani;

	public @Parameter(1) boolean returnMockedUnit;

	public @Parameter(2) boolean isFromUnitDefined;
	
	@Test
	public void test() throws Exception {
		logger.debug("Clipboard '" + zadani + "', unit is readed '" + returnMockedUnit + "', trasfer from is readed '"
				+ isFromUnitDefined + "'");
		new Expectations() {{
			db.getString(); result = zadani;
			if(returnMockedUnit){
				model.getUnitById(232); result = unit;
			}
		}};
		
		
		final ParsingResult parsingResult = ClipboardReader.make(model, db);
		if (returnMockedUnit) {
			assertTrue(parsingResult.getUnit().isPresent());
			if (isFromUnitDefined) {
				assertReadTransferFrom(true, parsingResult);
			}else{
				assertReadTransferFrom(false, parsingResult);				
			}
		} else {
			assertFalse(parsingResult.getUnit().isPresent());
			assertReadTransferFrom(false, parsingResult);
		}
		assertFalse(parsingResult.getGoods().isPresent());
	}
	
	private void assertReadTransferFrom(final boolean isPresent, final ParsingResult parsingResult) {
		final AtomicBoolean ret = new AtomicBoolean();
		parsingResult.tryReadUnit((unit, transferFrom) -> {
			ret.set(transferFrom.isPresent());
		});
		assertEquals(isPresent, ret.get());
	}
	
}
