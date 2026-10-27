package org.microcol.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.model.Unit;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.FreePlaySetupPage;
import org.microcol.page.GamePage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

/** The initially focused galleon can move without reselecting its tile. */
@ExtendWith(ApplicationExtension.class)
public class TC_193_F15_initial_ship_movement_test
        extends AbstractMicroColTest {
    private static final File FIXTURE = new File(
            "src/test/scenarios/F15-free-play-audit.microcol");
    private static final Location START = Location.of(45, 15);
    private static final Location DESTINATION = Location.of(44, 15);
    private static final Location EMPTY_TILE = Location.of(45, 14);
    private static final int GALLEON_ID = 3;

    @Start
    private void start(final Stage stage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".microcol", "autosave.microcol"));
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_initial_galleon_moves_with_m_without_reselection()
            throws Exception {
        // Start Original world through the menu; do not click on the map.
        final FreePlaySetupPage setup = WelcomePage.of(getContext())
                .openFreePlaySetup();
        setup.selectMap(0);
        final GamePage game = setup.start();
        final Unit ship = getModel().getUnitById(GALLEON_ID);
        game.verifyThatSelectedTileIs(START);
        game.verifyUnitLocation(ship, START);

        // M and a destination click must move the initially focused galleon.
        new FreePlayAuditPage(getContext()).press(KeyCode.M);
        game.selectTile(DESTINATION);
        game.waitWhileMoving();
        game.verifyUnitLocation(ship, DESTINATION);
        game.verifyNumberOfUnitsAt(START, 0);
    }

    @Test
    @Tag("local")
    void TC_02_initial_galleon_moves_with_mouse_without_reselection()
            throws Exception {
        // Start Original world through the menu; do not reselect the ship.
        final FreePlaySetupPage setup = WelcomePage.of(getContext())
                .openFreePlaySetup();
        setup.selectMap(0);
        final GamePage game = setup.start();
        final Unit ship = getModel().getUnitById(GALLEON_ID);
        game.verifyThatSelectedTileIs(START);
        game.verifyUnitLocation(ship, START);

        // Drag the initially focused galleon to the adjacent sea tile.
        game.moveMouseAtLocation(START);
        game.dragToLocation(DESTINATION);
        game.waitWhileMoving();
        game.verifyUnitLocation(ship, DESTINATION);
        game.verifyNumberOfUnitsAt(START, 0);
    }

    @Test
    @Tag("local")
    void TC_03_reselected_galleon_moves_with_m() throws Exception {
        // Control: use the reported workaround on the same fresh world.
        final FreePlaySetupPage setup = WelcomePage.of(getContext())
                .openFreePlaySetup();
        setup.selectMap(0);
        final GamePage game = setup.start();
        final Unit ship = getModel().getUnitById(GALLEON_ID);
        game.selectTile(EMPTY_TILE);
        game.selectTile(START);

        // The same keyboard command and destination must now work.
        new FreePlayAuditPage(getContext()).press(KeyCode.M);
        game.selectTile(DESTINATION);
        game.waitWhileMoving();
        game.verifyUnitLocation(ship, DESTINATION);
        game.verifyNumberOfUnitsAt(START, 0);
    }

    @Test
    @Tag("local")
    void TC_04_reselected_galleon_moves_with_mouse() throws Exception {
        // Control: use the reported workaround on the same fresh world.
        final FreePlaySetupPage setup = WelcomePage.of(getContext())
                .openFreePlaySetup();
        setup.selectMap(0);
        final GamePage game = setup.start();
        final Unit ship = getModel().getUnitById(GALLEON_ID);
        game.selectTile(EMPTY_TILE);
        game.selectTile(START);

        // The same drag and destination must now work.
        game.moveMouseAtLocation(START);
        game.dragToLocation(DESTINATION);
        game.waitWhileMoving();
        game.verifyUnitLocation(ship, DESTINATION);
        game.verifyNumberOfUnitsAt(START, 0);
    }
}
