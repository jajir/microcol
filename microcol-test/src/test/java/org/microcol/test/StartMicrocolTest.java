package org.microcol.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.MicroCol;
import org.microcol.gui.FileSelectingService;
import org.microcol.gui.Point;
import org.microcol.gui.StatusBarView;
import org.microcol.gui.buttonpanel.ButtonsGamePanel;
import org.microcol.gui.colony.ColonyButtonsPanel;
import org.microcol.gui.colony.ColonyPanel;
import org.microcol.gui.colony.PanelUnitWithContextMenu;
import org.microcol.gui.gamemenu.ButtonsPanelView;
import org.microcol.gui.gamemenu.MenuHolderPanel;
import org.microcol.gui.gamemenu.SettingButtonsView;
import org.microcol.gui.gamemenu.SettingLanguageView;
import org.microcol.gui.gamepanel.Area;
import org.microcol.gui.gamepanel.CursorService;
import org.microcol.gui.gamepanel.GamePanelView;
import org.microcol.gui.util.GamePreferences;
import org.microcol.gui.util.PanelDock;
import org.microcol.gui.util.PanelDockCrate;
import org.microcol.model.Location;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.robot.Motion;
import org.testfx.service.finder.NodeFinder;
import org.testfx.service.query.EmptyNodeQueryException;
import org.testfx.util.WaitForAsyncUtils;

import com.google.common.base.Preconditions;

import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class StartMicrocolTest {

    private final static Point TILE_CENTER = Point.of(GamePanelView.TILE_WIDTH_IN_PX, GamePanelView.TILE_WIDTH_IN_PX)
	    .divide(2);

    private final Logger logger = LoggerFactory.getLogger(StartMicrocolTest.class);

    private final static String BUTTON_SETTING_ID_NOT_EXISTING = "#buttonSetting-notExisting";

    private final static File verifyLoadingUnloading = new File(
	    "src/test/scenarios/test-verify-loading-unloading.microcol");

    private MicroCol microCol;

    private Stage stage;

    @Start
    @Tag("ci")
    private void start(final Stage primaryStage) throws Exception {
	logger.info("Starting MicroCol");
	this.stage = primaryStage;
	System.setProperty(GamePreferences.SYSTEM_PROPERTY_DEVELOPMENT, Boolean.TRUE.toString());
	microCol = new MicroCol(binder -> {
	    FileSelectingService fileSelectingService = Mockito.mock(FileSelectingService.class);
	    Mockito.when(fileSelectingService.loadFile(Mockito.any(File.class))).thenReturn(verifyLoadingUnloading);
	    binder.bind(FileSelectingService.class).toInstance(fileSelectingService);
	    binder.bind(CursorService.class).toInstance(new CursorServiceNoOpp());
	});
	microCol.start(primaryStage);
    }

    @Test
    @Tag("ci")
    void verify_that_test_files_exists() {
	assertTrue(verifyLoadingUnloading.exists(),
		String.format("File '%s' doesn't exists", verifyLoadingUnloading.getAbsoluteFile()));
	assertTrue(verifyLoadingUnloading.isFile(),
		String.format("File '%s' is directory", verifyLoadingUnloading.getAbsoluteFile()));
    }

    @Test
    @Tag("ci")
    void verify_exception_is_throws_when_object_isnt_exists(final FxRobot robot) throws Exception {
	Assertions.assertThrows(EmptyNodeQueryException.class, () -> {
	    FxAssert.verifyThat(BUTTON_SETTING_ID_NOT_EXISTING, obj -> {
		return true;
	    });
	});
    }

    @Test
    @Tag("ci")
    void verify_setting_page_is_available(final FxRobot robot) throws Exception {
	verifyMainScreen();
	openSetting(robot);

	clickOnButtonWithId(robot, SettingButtonsView.BUTTON_BACK_ID);
	verifyMainScreen();
    }

    @Test
    @Tag("ci")
    void verify_language_is_changed_immediatelly(final FxRobot robot) throws Exception {
	verifyMainScreen();
	openSetting(robot);

	// verify that english is selected
	verifyRadioButton(SettingLanguageView.RB_CZECH_ID, false);
	verifyRadioButton(SettingLanguageView.RB_ENGLISH_ID, true);

	// select czech
	clickOnButtonWithId(robot, SettingLanguageView.RB_CZECH_ID);

	// verify czech title
	verifyThatMainTitleContains("Nastaven");
	verifyRadioButton(SettingLanguageView.RB_CZECH_ID, true);
	verifyRadioButton(SettingLanguageView.RB_ENGLISH_ID, false);

	// select english
	clickOnButtonWithId(robot, SettingLanguageView.RB_ENGLISH_ID);

	// verify english
	verifyRadioButton(SettingLanguageView.RB_CZECH_ID, false);
	verifyRadioButton(SettingLanguageView.RB_ENGLISH_ID, true);
    }

    @Test
    @Tag("ci")
    void verify_moving_with_unit(final FxRobot robot) throws Exception {
	// TODO move with ship. make from this TC
	// TODO verify, selecting moving unit from right panel.
	// TODO verify moving with ship at land
	// TODO verify moving with normal unit at see.
	// TODO verify that action points could be exhausted.
	verifyMainScreen();
	clickOnButtonWithId(robot, ButtonsPanelView.BUTTON_LOAD_ID);
	moveMouseAtLocation(robot, Location.of(22, 12));
	dragMouseAtLocation(robot, Location.of(24, 11));
//	Thread.sleep(1000 * 8);
    }

    @Test
    void TC_01_embark_disembark(final FxRobot robot) throws Exception {
	verifyMainScreen();

	// load predefined game
	loadPreparedGame(robot);

	// go at main game panel
	openColonyAt(robot, Location.of(22, 12), "Delft");

	// Verify that there is just one ship in port
	assertEquals(1, getUnitsInPort().size(), String
		.format("Expected numebr of shipd is '1' but really there is '%s' ships.", getUnitsInPort().size()));

	// Select first ship to drag to ship.
	robot.clickOn(getUnitsInPort().get(0));

	// drag first units to crate 0
	robot.drag(getUnitsAtPier().get(0), MouseButton.PRIMARY).dropTo(getListOfCrates().get(0))
		.release(MouseButton.PRIMARY);

	// drag second units to crate 1
	robot.drag(getUnitsAtPier().get(0), MouseButton.PRIMARY).dropTo(getListOfCrates().get(1))
		.release(MouseButton.PRIMARY);

	// return back to main game screen.
	buttonCloseClick(robot);

	// move ship few tiles to the right
	moveMouseAtLocation(robot, Location.of(22, 12));
	dragMouseAtLocation(robot, Location.of(24, 11));
	waitWhileMoving();

	// press next turn.
	buttonNextTurnClick(robot);

	// disembark units
	moveMouseAtLocation(robot, Location.of(24, 11));
	dragMouseAtLocation(robot, Location.of(24, 12));

	// TODO verify that units are at expected location.
	// Thread.sleep(1000 * 8);
    }

    private void loadPreparedGame(final FxRobot robot) {
	clickOnButtonWithId(robot, ButtonsPanelView.BUTTON_LOAD_ID);
	// try to find next turn button, when it's not there than game was not loaded.
	getButtoonById(ButtonsGamePanel.BUTTON_NEXT_TURN_ID);
    }

    private List<ToggleButton> getUnitsInPort() {
	final String cssClass = "." + PanelDock.SHIP_IN_PORT_STYLE;
	final Set<ToggleButton> ships = getNodeFinder().lookup(cssClass).queryAll();
	return new ArrayList<ToggleButton>(ships);
    }

    private List<StackPane> getListOfCrates() {
	final String cssClass = "." + PanelDockCrate.CRATE_CLASS;
	final Set<StackPane> cratesSet = getNodeFinder().lookup(cssClass).queryAll();
	return new ArrayList<StackPane>(cratesSet);
    }

    /**
     * Wait until next turn button is available again. Unavailable next turn button
     * meant that there is some animation in progress.
     * 
     * @throws Exception
     */
    private void waitWhileMoving() throws Exception {
	WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS, () -> {
	    final NodeFinder nodeFinder = FxAssert.assertContext().getNodeFinder();
	    final Button buttonNextTurn = nodeFinder.lookup("#" + ButtonsGamePanel.BUTTON_NEXT_TURN_ID).queryButton();
	    return !buttonNextTurn.isDisabled();
	});
    }

    private void buttonCloseClick(final FxRobot robot) {
	final Button buttonNextTurn = getButtoonById(ColonyButtonsPanel.CLOSE_BUTTON_ID);
	robot.clickOn(buttonNextTurn);
    }

    private void buttonNextTurnClick(final FxRobot robot) {
	final Button buttonNextTurn = getButtoonById(ButtonsGamePanel.BUTTON_NEXT_TURN_ID);
	robot.clickOn(buttonNextTurn);
    }

    // Select units to drag to ship.
    private List<Pane> getUnitsAtPier() {
	final NodeFinder nodeFinder = FxAssert.assertContext().getNodeFinder();
	final Set<Pane> unitsSet = nodeFinder.lookup("." + PanelUnitWithContextMenu.UNIT_AT_PIER_STYLE).queryAll();
	return new ArrayList<Pane>(unitsSet);
    }

    private void openColonyAt(final FxRobot robot, final Location colonyLocation, final String expectedNamePart) {
	moveMouseAtLocation(robot, colonyLocation);
	robot.clickOn(MouseButton.PRIMARY);
	final Labeled labeled = getLabeledById(ColonyPanel.COLONY_NAME_ID);
	logger.info("Colony name: " + labeled.getText());
	assertTrue(labeled.getText().contains(expectedNamePart),
		String.format("Text '%s' should appear in colony name '%s'", expectedNamePart, labeled.getText()));
    }

    private void moveMouseAtLocation(final FxRobot robot, final Location location) {
	verifyThatTileIsVisible(location);
	final Point p = getArea().convertToPoint(location).add(stage.getX(), stage.getY()).add(TILE_CENTER);
	robot.moveTo(p.getX(), p.getY(), Motion.DEFAULT);
	verifyThatStatusBarContains(String.valueOf(location.getX()));
	verifyThatStatusBarContains(String.valueOf(location.getY()));
    }

    private void dragMouseAtLocation(final FxRobot robot, final Location location) throws Exception {
	verifyThatTileIsVisible(location);
	final Point p = getArea().convertToPoint(location).add(stage.getX(), stage.getY()).add(TILE_CENTER);
	robot.drag(MouseButton.PRIMARY).drag(p.getX(), p.getY()).release(MouseButton.PRIMARY);
	verifyThatStatusBarContains(String.valueOf(location.getX()));
	verifyThatStatusBarContains(String.valueOf(location.getY()));
    }

    private void verifyThatStatusBarContains(final String string) {
	final Labeled label = getLabeledById(StatusBarView.STATUS_BAR_LABEL_ID);
	logger.info("Status bar: " + label.getText());
	assertTrue(label.getText().contains(string),
		String.format("Text '%s' should appear in status bar text '%s'", string, label.getText()));
    }

    private void verifyThatTileIsVisible(final Location location) {
	assertTrue(getArea().isLocationVisible(location));
    }

    private Area getArea() {
	final GamePanelView gamePanelView = getClassFromGuice(GamePanelView.class);
	return gamePanelView.getArea();
    }

    private <T> T getClassFromGuice(final Class<T> clazz) {
	final T t = microCol.getInjector().getInstance(clazz);
	return Preconditions.checkNotNull(t);
    }

    private void verifyRadioButton(final String buttonId, boolean isExpectedSelected) {
	final String id = "#" + buttonId;
	final RadioButton rb = getNodeFinder().lookup(id).queryAs(RadioButton.class);
	if (isExpectedSelected) {
	    assertTrue(rb.isSelected());
	} else {
	    assertFalse(rb.isSelected());
	}
    }

    private void openSetting(final FxRobot robot) {
	verifyMainScreen();
	clickOnButtonWithId(robot, ButtonsPanelView.BUTTON_SETTING_ID);
	verifyThatMainTitleContains("Setting");
    }

    private void clickOnButtonWithId(final FxRobot robot, final String buttonId) {
	final String id = "#" + buttonId;
	robot.clickOn(id);
	WaitForAsyncUtils.waitForFxEvents();
    }

    private void verifyMainScreen() {
	verifyMainTitle(realTitle -> {
	    assertEquals("MicroCol", realTitle);
	});
    }

    private void verifyThatMainTitleContains(final String title) {
	verifyMainTitle(realTitle -> {
	    if (!realTitle.contains(title)) {
		fail(String.format("Real screen title '%s' doesn't contains string '%s'", realTitle, title));
	    }
	});
    }

    private void verifyMainTitle(final Consumer<String> validation) {
	final String id = "#" + MenuHolderPanel.MAIN_TITLE_ID;
	final Labeled labeled = getNodeFinder().lookup(id).queryLabeled();
	assertNotNull(labeled, String.format("There is no element with id '%s'", MenuHolderPanel.MAIN_TITLE_ID));
	validation.accept(labeled.getText());
    }

    private Labeled getLabeledById(final String cssId) {
	final String id = "#" + cssId;
	final Labeled label = getNodeFinder().lookup(id).queryLabeled();
	assertNotNull(label, String.format("unable to find labeled by id '%s'", cssId));
	return label;
    }

    private Button getButtoonById(final String cssId) {
	final String id = "#" + cssId;
	final Button label = getNodeFinder().lookup(id).queryButton();
	assertNotNull(label, String.format("unable to find button by id '%s'", cssId));
	return label;
    }

    private NodeFinder getNodeFinder() {
	return FxAssert.assertContext().getNodeFinder();
    }

}
