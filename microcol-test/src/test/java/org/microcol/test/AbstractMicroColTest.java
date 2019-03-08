package org.microcol.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.microcol.MicroCol;
import org.microcol.gui.Point;
import org.microcol.gui.preferences.GamePreferences;
import org.microcol.gui.screen.game.gamepanel.CursorService;
import org.microcol.gui.screen.game.gamepanel.GamePanelView;
import org.microcol.mock.CursorServiceNoOpp;
import org.microcol.model.CargoSlot;
import org.microcol.model.Goods;
import org.microcol.model.Model;
import org.microcol.model.Player;
import org.microcol.model.unit.UnitWithCargo;
import org.microcol.page.TestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.service.finder.NodeFinder;

import com.google.inject.Binder;

import javafx.stage.Stage;

public abstract class AbstractMicroColTest {

    protected final static Point TILE_CENTER = Point.of(GamePanelView.TILE_WIDTH_IN_PX, GamePanelView.TILE_WIDTH_IN_PX)
	    .divide(2);

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
    void beforeEachTest(final FxRobot robot) {
	getContext().setRobot(robot);
    }

    /**
     * Method that initialize MicroCol game.
     * 
     * @param primaryStage required primary stage
     * @throws Exception
     */
    protected void initialize(final Stage primaryStage, final Class<?> clazz) throws Exception {
	logger.info("Starting MicroCol UI test " + clazz.getName());
	System.setProperty(GamePreferences.SYSTEM_PROPERTY_DEVELOPMENT, Boolean.TRUE.toString());
	final MicroCol microCol = new MicroCol(binder -> {
	    binder.bind(CursorService.class).toInstance(new CursorServiceNoOpp());
	    bind(binder);
	});
	microCol.start(primaryStage);
	context = new TestContext(microCol, primaryStage);
    }

    /**
     * Allows to custom guice in every test case.
     *
     * @param binder required guice binder
     */
    abstract protected void bind(final Binder binder);

    protected MicroCol getMicroCol() {
	return context.getMicroCol();
    }

    protected Stage getPrimaryStage() {
	return context.getPrimaryStage();
    }

    protected TestContext getContext() {
	return context;
    }

    protected NodeFinder getNodeFinder() {
	return FxAssert.assertContext().getNodeFinder();
    }

    protected Model getModel() {
	return getContext().getModel();
    }

    protected Player getHumanPlayer() {
	return getModel().getPlayers().stream().filter(player -> player.isHuman()).findAny()
		.orElseThrow(() -> new IllegalStateException("There is no human player in game model."));
    }

    protected void verifyNumberOfGoodsInShip(final UnitWithCargo ship, final int cargoSlotIndex,
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
