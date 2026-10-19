package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.microcol.gui.MainStageBuilder;
import org.microcol.gui.util.WoodenButtonSkin;
import org.microcol.gui.util.WoodenToggleButtonSkin;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.skin.ButtonSkin;
import javafx.scene.control.skin.ToggleButtonSkin;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/** Observations and native input for the shared button appearance. */
public final class WoodenButtonsPage extends AbstractScreen {
    private Stage samples;
    private final AtomicInteger actions = new AtomicInteger();

    public WoodenButtonsPage(final TestContext context) {
        super(context);
    }

    public void showSamples() {
        getRobot().interact(() -> {
            final Button ok = new Button("OK");
            ok.setId("woodOk");
            ok.setDefaultButton(true);
            ok.setOnAction(event -> actions.incrementAndGet());
            final Button back = new Button("Back");
            back.setId("woodBack");
            back.setCancelButton(true);
            back.setOnAction(event -> samples.close());
            final Button longButton = new Button("Souhlasím, další krok");
            longButton.setId("woodLong");
            final Button disabled = new Button("Nedostupné");
            disabled.setId("woodDisabled");
            disabled.setDisable(true);
            disabled.setOnAction(event -> actions.incrementAndGet());
            final ToggleButton toggle = new ToggleButton("Výběr");
            toggle.setId("woodToggle");
            final VBox content = new VBox(12,
                    new HBox(12, ok, back, longButton),
                    new HBox(12, toggle, disabled));
            content.setId("woodSamples");
            content.setFocusTraversable(true);
            // Match production dialogs, which specify a content width.
            content.setPrefWidth(560);
            samples = new Stage(StageStyle.UNDECORATED);
            samples.initOwner(getPrimaryStage());
            samples.initModality(Modality.WINDOW_MODAL);
            final Scene scene = new Scene(content, 560, 180);
            scene.getStylesheets().add(MainStageBuilder.STYLE_SHEET_DIALOGS);
            samples.setScene(scene);
            samples.show();
            samples.requestFocus();
        });
        getRobot().moveTo("#woodSamples");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void hover(final String id) {
        getRobot().moveTo("#" + id);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void holdMouse() {
        getRobot().press(MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void releaseMouse() {
        getRobot().release(MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void press(final KeyCode key) {
        getRobot().push(key);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void focus(final String id) {
        getRobot().interact(
                () -> getNodeFinder().lookup("#" + id).query().requestFocus());
    }

    public void click(final String id) {
        getRobot().clickOn("#" + id);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyWoodenButton(final String id) {
        getRobot().interact(() -> {
            final ButtonBase button = (ButtonBase) getNodeFinder()
                    .lookup("#" + id).query();
            assertTrue(button.getSkin() instanceof WoodenButtonSkin
                    || button.getSkin() instanceof WoodenToggleButtonSkin);
            assertEquals("Playpen Sans Light", button.getFont().getFamily());
            assertEquals(18, button.getFont().getSize());
            assertEquals(new javafx.geometry.Insets(12, 24, 14, 24),
                    button.getPadding());
            assertTrue(button.lookup(".wooden-button-drawing")
                    .isMouseTransparent());
            final Text text = (Text) button.lookup(".text");
            assertEquals(button.getText(), text.getText(),
                    "Label must not be truncated at " + button.getWidth()
                            + " x " + button.getHeight() + "; pref="
                            + button.prefWidth(-1) + "; font="
                            + button.getFont() + "; insets="
                            + button.getInsets() + "; scene="
                            + button.getScene().getWidth() + "; parent="
                            + button.getParent().getLayoutBounds());
            assertTrue(text.getBoundsInParent().getMaxX() <= button.getWidth());
            assertTrue(text.getBoundsInParent().getMinY() >= 0);
            assertTrue(
                    text.getBoundsInParent().getMaxY() <= button.getHeight());
        });
    }

    public void verifyOriginalImageButton(final String id) {
        getRobot().interact(() -> {
            final Button button = getButtonById(id);
            assertEquals(ButtonSkin.class, button.getSkin().getClass());
            assertFalse(button.getBackground().getImages().isEmpty());
            assertNull(button.lookup(".wooden-button-drawing"));
        });
    }

    public void verifyOriginalShipButtons() {
        getRobot().interact(() -> {
            final java.util.List<ToggleButton> ships = getListOfNodes(
                    ".paneShip");
            assertFalse(ships.isEmpty());
            for (final ToggleButton ship : ships) {
                assertEquals(ToggleButtonSkin.class, ship.getSkin().getClass());
                assertFalse(ship.getBackground().getImages().isEmpty());
                assertNull(ship.lookup(".wooden-button-drawing"));
            }
        });
    }

    public void verifyFill(final String id, final String expectedColor,
            final boolean expectedArmed) {
        getRobot().interact(() -> {
            final ButtonBase button = (ButtonBase) getNodeFinder()
                    .lookup("#" + id).query();
            button.getScene().getRoot().layout();
            final Canvas canvas = (Canvas) button
                    .lookup(".wooden-button-drawing");
            final WritableImage image = canvas.snapshot(null, null);
            assertEquals(Color.web(expectedColor), image.getPixelReader()
                    .getColor(9, (int) (canvas.getHeight() / 2)));
            assertEquals(expectedArmed, button.isArmed());
        });
    }

    public void verifyWidthsAndInk() {
        getRobot().interact(() -> {
            final Button shortButton = getButtonById("woodOk");
            final Button longButton = getButtonById("woodLong");
            assertTrue(longButton.getWidth() > shortButton.getWidth() + 80);
            assertEquals(shortButton.getHeight(), longButton.getHeight());
            for (final Button button : new Button[] { shortButton,
                    longButton }) {
                final Canvas canvas = (Canvas) button
                        .lookup(".wooden-button-drawing");
                final WritableImage image = canvas.snapshot(null, null);
                final int y = (int) (button.getHeight() / 2);
                // The unscaled side is black at the same logical x for both
                // widths.
                assertEquals(Color.BLACK,
                        image.getPixelReader().getColor(4, y));
                assertEquals(Color.web("#dfbd91"),
                        image.getPixelReader().getColor(9, y));
            }
        });
    }

    public void changeSampleLabel() {
        getRobot().interact(
                () -> getButtonById("woodLong").setText("Potvrdit další krok"));
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyFocused(final String id) {
        assertTrue(getNodeFinder().lookup("#" + id).query().isFocused());
    }

    public void verifySelected(final boolean expected) {
        assertEquals(expected,
                ((ToggleButton) getNodeFinder().lookup("#woodToggle").query())
                        .isSelected());
    }

    public void verifyActionCount(final int expected) {
        assertEquals(expected, actions.get());
    }

    public void verifySamplesClosed() {
        assertFalse(samples.isShowing());
    }
}
