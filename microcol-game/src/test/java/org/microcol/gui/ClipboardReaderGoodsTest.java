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
public class ClipboardReaderGoodsTest {

	private final Logger logger = LoggerFactory.getLogger(ClipboardReaderGoodsTest.class);

	private @Mocked Model model;

	private @Mocked Dragboard db;

	private @Mocked Unit unit;
	
	@Parameters(name = "{index}: isGoodDefined = {0}, isFromUnitDefined = {1}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
			{"Goods,COTTON,75,FromUnit,12,2",   true,  true},
			{"Goods,COTTON,75,FromUnIT,12,2",   true,  false},
			{"Goods,COTTON,75,FromUnit,1,bl",   true,  false},
			{"Goods,COTTON,75,FromUnit,bl,2",   true,  false},
			{"Goods,COTTON,75,FromUnit,12",     true,  false},
			{"Goods,COTTON,75,,,",              true,  false},
			{"Goods,COTTON,75,",                true,  false},
			{"Goods,COTTON,75",                 true,  false},
			{"Goods,COTTON,blee,FromUnit,12,2", false, false},
			{"Goods,COTTON,",                   false, false},
			{"Goods,KACHNA,75,",                false, false},
			{"GooDS,,75",                       false, false},
			{",,",                              false, false},
			{"",                                false, false},
			{null,                              false, false},
		});
	}
	
	public @Parameter(0) String zadani;

	public @Parameter(1) boolean isGoodDefined;

	public @Parameter(2) boolean isFromUnitDefined;
	
	@Test
	public void test() throws Exception {
		logger.debug("Clipboard '" + zadani + "', isGoodDefined '" + isGoodDefined + "', isFromUnitDefined '"
				+ isFromUnitDefined + "'");
		new Expectations() {{
			db.getString(); result = zadani;
			if(isFromUnitDefined){
				model.getUnitById(12); result = unit;
			}
		}};
		
		
		final ParsingResult parsingResult = ClipboardReader.make(model, db);
		if (isGoodDefined) {
			assertTrue(parsingResult.getGoods().isPresent());
			if (isFromUnitDefined) {
				assertReadTransferFrom(true, parsingResult);
			}else{
				assertReadTransferFrom(false, parsingResult);				
			}
		} else {
			assertFalse(parsingResult.getGoods().isPresent());
			assertReadTransferFrom(false, parsingResult);
		}
		assertFalse(parsingResult.getUnit().isPresent());
	}
	
	private void assertReadTransferFrom(final boolean isPresent, final ParsingResult parsingResult) {
		final AtomicBoolean ret = new AtomicBoolean();
		parsingResult.tryReadGood((good, transferFrom) -> {
			ret.set(transferFrom.isPresent());
		});
		assertEquals(isPresent, ret.get());
	}
	
}
