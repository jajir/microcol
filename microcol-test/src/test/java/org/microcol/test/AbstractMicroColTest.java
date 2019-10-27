package org.microcol.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.microcol.MicroColApplication;
import org.microcol.gui.FileSelectingService;
import org.microcol.gui.preferences.GamePreferences;
import org.microcol.gui.screen.game.gamepanel.CursorService;
import org.microcol.mock.CursorServiceNoOpp;
import org.microcol.model.CargoSlot;
import org.microcol.model.Goods;
import org.microcol.model.Model;
import org.microcol.model.Player;
import org.microcol.model.unit.UnitWithCargo;
import org.microcol.page.TestContext;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.service.finder.NodeFinder;

import com.google.inject.Binder;
import com.google.inject.Module;

import javafx.stage.Stage;

public abstract class AbstractMicroColTest {

    private final Logger logger = LoggerFactory.getLogger(AbstractMicroColTest.class);

    private TestContext context;

    AbstractMicroColTest() {
	/*
	 * Following code rewrite user home directory where is stored MicroCol game
	 * saves and configuration file. It allows to set special configuration for
	 * tests.
	 */
	System.setProperty("user.home", "src/test/scenarios/");
    }

    @BeforeEach
    void beforeEach(final FxRobot robot) {
	getContext().setRobot(robot);
    }

    /**
     * Method that initialize MicroCol game.
     * <p>
     * MicroCol will by started in development mode and with fake file loading
     * service.
     * </p>
     * 
     * @param primaryStage       required primary stage
     * @param clazz              required class of junit test
     * @param microcolFileToLoad required file which will be loaded in game
     * @throws Exception
     */
    protected void initialize(final Stage primaryStage, final Class<?> clazz, final File microcolFileToLoad)
	    throws Exception {
	initialize(primaryStage, clazz, binder -> {
	    binder.bind(CursorService.class).toInstance(new CursorServiceNoOpp());
	    FileSelectingService fileSelectingService = Mockito.mock(FileSelectingService.class);
	    Mockito.when(fileSelectingService.loadFile(Mockito.any(File.class))).thenReturn(microcolFileToLoad);
	    binder.bind(FileSelectingService.class).toInstance(fileSelectingService);
	});
    }

    /**
     * Method that initialize MicroCol game.
     * <p>
     * MicroCol will by started in development mode and with fake file loading
     * service.
     * </p>
     * 
     * @param primaryStage    required primary stage
     * @param clazz           required class of junit test
     * @param overridenModule guice module where should be overridden MicroCol
     *                        services
     * @throws Exception
     */
    protected void initialize(final Stage primaryStage, final Class<?> clazz, final Module overridenModule)
	    throws Exception {
	logger.info("Starting MicroCol UI test " + clazz.getName());
	System.setProperty(GamePreferences.SYSTEM_PROPERTY_DEVELOPMENT, Boolean.TRUE.toString());
	final MicroColApplication microCol = new MicroColApplication(overridenModule);
	microCol.start(primaryStage);
	context = new TestContext(microCol, primaryStage);
    }

    /**
     * Allows to custom guice in every test case.
     *
     * @param binder required guice binder
     */
    protected void bind(final Binder binder) {
	// by default do nothing
    }

    public MicroColApplication getMicroCol() {
	return context.getMicroCol();
    }

    public Stage getPrimaryStage() {
	return context.getPrimaryStage();
    }

    public TestContext getContext() {
	return context;
    }

    public NodeFinder getNodeFinder() {
	return FxAssert.assertContext().getNodeFinder();
    }

    public Model getModel() {
	return getContext().getModel();
    }

    public Player getHumanPlayer() {
	return getModel().getPlayers().stream().filter(player -> player.isHuman()).findAny()
		.orElseThrow(() -> new IllegalStateException("There is no human player in game model."));
    }

    public void verifyNumberOfGoodsInShip(final UnitWithCargo ship, final int cargoSlotIndex,
	    final Goods expectedGoods) {
	final CargoSlot cargoSlot = ship.getCargo().getSlotByIndex(cargoSlotIndex);

	if (expectedGoods.isZero()) {
	    assertFalse(cargoSlot.getGoods().isPresent(), "Cargo slot should be empty");
	} else {
	    assertTrue(cargoSlot.getGoods().isPresent(), "Cargo slot should not be empty");
	    assertEquals(expectedGoods.getType(), cargoSlot.getGoods().get().getType());
	    assertEquals(expectedGoods.getAmount(), cargoSlot.getGoods().get().getAmount());
	}
    }

}
