package org.microcol.test;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.gui.util.FontService;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Shared menu appearance must retain JavaFX keyboard and dismissal behavior.
 */
@ExtendWith(ApplicationExtension.class)
@Tag("local")
public class ContextMenuStyleTest {
    private final AtomicInteger actions = new AtomicInteger();
    private ContextMenu menu;
    private Button owner;

    @Start
    void start(final Stage stage) {
        new FontService();
        menu = new ContextMenu();
        owner = new Button("Menu");
        final MenuItem action = new MenuItem(
                "A long action / Vybrat práci kolonisty");
        action.setAccelerator(
                new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN));
        action.setOnAction(event -> actions.incrementAndGet());
        final MenuItem unavailable = new MenuItem("Unavailable / Nedostupné");
        unavailable.setDisable(true);
        unavailable.setOnAction(event -> actions.addAndGet(100));
        menu.getItems().addAll(action, unavailable);
        owner.setContextMenu(menu);
        stage.setScene(new Scene(new StackPane(owner), 1000, 700));
        stage.show();
        stage.requestFocus();
        owner.requestFocus();
    }

    @Test
    void game_menus_keep_keys_readable_and_keyboard_activation(
            final FxRobot robot) throws Exception {
        verifyMenu(robot, "MicroCol.css");
    }

    @Test
    void dialog_menus_share_style_and_keyboard_activation(final FxRobot robot)
            throws Exception {
        verifyMenu(robot, "dialogs.css");
    }

    private void verifyMenu(final FxRobot robot, final String stylesheet)
            throws Exception {
        robot.interact(() -> {
            owner.getScene().getStylesheets().add(getClass()
                    .getResource("/gui/" + stylesheet).toExternalForm());
            final javafx.geometry.Rectangle2D screen = Screen.getPrimary()
                    .getVisualBounds();
            menu.show(owner, screen.getMaxX() - 4, screen.getMaxY() - 4);
        });
        WaitForAsyncUtils.waitForFxEvents();
        robot.interact(() -> {
            final Region panel = (Region) menu.getSkin().getNode();
            assertEquals(Color.web("#fff9ec"),
                    panel.getBackground().getFills().get(0).getFill());
            assertNotNull(panel.getShape());
            final Label key = (Label) panel.lookup(".accelerator-text");
            assertNotNull(key);
            assertEquals("Ctrl+D", key.getText());
            assertEquals("Playpen Sans", key.getFont().getFamily());
            assertEquals(14, key.getFont().getSize());
            assertFalse(key.getBorder().isEmpty());
            final Region row = (Region) key.getParent();
            final Label caption = (Label) row.getChildrenUnmodifiable().stream()
                    .filter(node -> node instanceof Label && node != key)
                    .findFirst().orElseThrow();
            assertEquals(menu.getItems().get(0).getText(), caption.getText());
            final Bounds labelBounds = caption
                    .localToScreen(caption.getBoundsInLocal());
            final Bounds keyBounds = key.localToScreen(key.getBoundsInLocal());
            assertTrue(labelBounds.getMaxX() < keyBounds.getMinX());
            assertTrue(
                    Screen.getPrimary().getVisualBounds().contains(menu.getX(),
                            menu.getY(), menu.getWidth(), menu.getHeight()));
            assertEquals(1, panel.lookupAll(".accelerator-text").size());
        });
        final Path output = Path.of("target", "warehouse-qa",
                "context-menu-" + stylesheet + ".png");
        Files.createDirectories(output.getParent());
        robot.robotContext().getCaptureSupport().saveImage(
                robot.capture(menu.getSkin().getNode()).getImage(), output);

        robot.push(KeyCode.ESCAPE);
        waitForMenu(false);
        robot.interact(() -> assertFalse(menu.isShowing()));
        assertEquals(0, actions.get());
        robot.interact(() -> owner.getScene().getWindow().requestFocus());
        robot.clickOn(owner, MouseButton.SECONDARY);
        waitForMenu(true);
        robot.interact(() -> assertTrue(menu.isShowing()));
        final Point2D outside = owner.getScene().getRoot().localToScreen(20,
                20);
        robot.interact(
                () -> assertFalse(new Rectangle2D(menu.getX(), menu.getY(),
                        menu.getWidth(), menu.getHeight()).contains(outside)));
        robot.clickOn(outside.getX(), outside.getY());
        waitForMenu(false);
        robot.interact(() -> assertFalse(menu.isShowing()));
        assertEquals(0, actions.get());
        robot.clickOn(owner, MouseButton.SECONDARY);
        waitForMenu(true);
        robot.push(KeyCode.DOWN).push(KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(1, actions.get());
        robot.clickOn(owner).push(KeyCode.CONTROL, KeyCode.D);
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(2, actions.get());
    }

    private void waitForMenu(final boolean showing) throws Exception {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> WaitForAsyncUtils
                .waitForAsyncFx(1000, () -> menu.isShowing() == showing));
    }
}
