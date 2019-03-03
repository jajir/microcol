package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.gui.FileSelectingService;
import org.microcol.page.EuropePortScreen;
import org.microcol.page.GamePage;
import org.microcol.page.WelcomePage;
import org.mockito.Mockito;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import com.google.inject.Binder;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TC_06_buy_and_sell_goods_test extends AbstractMicroColTest {

    private final static File testFileName = new File("src/test/scenarios/T06-buy-and-sell-goods.microcol");

    @Start
    private void start(final Stage primaryStage) throws Exception {
	initialize(primaryStage, getClass());
    }

    @Override
    protected void bind(final Binder binder) {
	FileSelectingService fileSelectingService = Mockito.mock(FileSelectingService.class);
	Mockito.when(fileSelectingService.loadFile(Mockito.any(File.class))).thenReturn(testFileName);
	binder.bind(FileSelectingService.class).toInstance(fileSelectingService);
    }

    @Test
    void TC_05_moving_goods(final FxRobot robot) throws Exception {
	// open MicroCol and load defined game
	GamePage gamePage = WelcomePage.of(getContext()).loadGame();
	WaitForAsyncUtils.waitForFxEvents();

	// Open Europe port
	final EuropePortScreen europePort = gamePage.openEuroperPort();

	//Close Europe port
	gamePage = europePort.close();
	
	//TODO Buy ship, verify that gold was lovered by unit price. make it as separte test.
	
	//TODO buy food, with control, limit foot to 63, verify food in cargo slot
	
	//TODO sell foot with pressed controll, sell just foot to 27, verify that 26 remains. 

    }

}
