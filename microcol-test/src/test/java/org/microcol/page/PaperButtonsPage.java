package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.gui.util.ActionButtons;
import org.microcol.gui.image.ButtonIcon;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.skin.ButtonSkin;
import javafx.scene.control.skin.ToggleButtonSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/** Observations and native input for the shared button appearance. */
public final class PaperButtonsPage extends AbstractScreen {
    private Stage samples;
    private final AtomicInteger actions = new AtomicInteger();

    public PaperButtonsPage(final TestContext context) {
        super(context);
    }

    public void showSamples() {
        getRobot().interact(() -> {
            final Button ok = ActionButtons.button(ButtonIcon.CONFIRM, "OK");
            ok.setId("woodOk");
            ok.setDefaultButton(true);
            ok.setOnAction(event -> actions.incrementAndGet());
            final Button back = ActionButtons.button(ButtonIcon.BACK, "Back");
            back.setId("woodBack");
            back.setCancelButton(true);
            back.setOnAction(event -> samples.close());
            final Button longButton = ActionButtons.button(ButtonIcon.NEXT,
                    "Souhlasím, další krok");
            longButton.setId("woodLong");
            final Button disabled = ActionButtons.button(ButtonIcon.STOP,
                    "Nedostupné");
            disabled.setId("woodDisabled");
            disabled.setDisable(true);
            disabled.setOnAction(event -> actions.incrementAndGet());
            final ToggleButton toggle = ActionButtons.toggle(ButtonIcon.CONFIRM,
                    "Výběr");
            toggle.setId("woodToggle");
            final VBox content = new VBox(12,
                    new HBox(12, ok, back, longButton),
                    new HBox(12, toggle, disabled));
            content.setId("woodSamples");
            content.setFocusTraversable(true);
            content.setPrefSize(560, 180);
            final AbstractMessageWindow dialog = new AbstractMessageWindow(
                    getContext().getClassFromGuice(ViewUtil.class),
                    getContext().getClassFromGuice(I18n.class)) {
            };
            dialog.init(content);
            samples = (Stage) dialog.getScene().getWindow();
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

    public void verifyPaperButton(final String id) {
        getRobot().interact(() -> verifyPaperButton(
                (ButtonBase) getNodeFinder().lookup("#" + id).query()));
    }

    private void verifyPaperButton(final ButtonBase button) {
        assertTrue(button.getSkin() instanceof ButtonSkin
                || button.getSkin() instanceof ToggleButtonSkin || button
                        .getSkin() instanceof javafx.scene.control.skin.MenuButtonSkin);
        assertEquals("Playpen Sans Regular", button.getFont().getName());
        assertEquals(14, button.getFont().getSize());
        assertEquals(new javafx.geometry.Insets(6, 3, 7, 3),
                button.getPadding(), button.getId());
        assertFalse(button.getBorder().getImages().isEmpty());
        assertTrue(button.getBorder().getImages().get(0).getImage().getUrl()
                .endsWith("game-toolbar-frame.png"));
        assertNotNull(button.getGraphic(),
                "Every action button has an icon: " + button.getId());
        assertNull(button.lookup(".wooden-button-drawing"));
        if (!button.getText().isEmpty() && button
                .getContentDisplay() != javafx.scene.control.ContentDisplay.GRAPHIC_ONLY) {
            final Text text = (Text) button.lookup(".text");
            assertNotNull(text);
            assertEquals(button.getText(), text.getText(),
                    "Full button label: " + button.getId());
        }
    }

    public void verifyVisibleButtonStyles() {
        getRobot().interact(() -> {
            final java.util.Set<javafx.scene.Node> controls = new java.util.HashSet<>();
            for (final javafx.stage.Window window : javafx.stage.Window
                    .getWindows()) {
                if (!window.isShowing() || window.getScene() == null)
                    continue;
                for (final String selector : java.util.List.of(".button",
                        ".toggle-button", ".menu-button")) {
                    controls.addAll(
                            window.getScene().getRoot().lookupAll(selector));
                }
            }
            int verified = 0;
            for (final javafx.scene.Node node : controls) {
                if (node instanceof ButtonBase && node.isVisible()
                        && node.isManaged()) {
                    verifyPaperButton((ButtonBase) node);
                    verified++;
                }
            }
            assertTrue(verified > 0, "At least one real button was checked");
        });
    }

    public void verifyToolbarButton(final String id) {
        verifyPaperButton(id);
        getRobot().interact(() -> {
            final Button button = getButtonById(id);
            assertEquals(ButtonSkin.class, button.getSkin().getClass());
            assertTrue(button.getBorder().getImages().get(0).getImage().getUrl()
                    .endsWith("game-toolbar-frame.png"));
            final javafx.scene.image.ImageView icon = (javafx.scene.image.ImageView) button
                    .getGraphic();
            assertEquals(32, icon.getFitHeight(), 0.01);
            assertEquals(32, icon.getFitWidth(), 0.01);
            assertEquals(57, button.getHeight(), 0.01);
            assertEquals(14, button.getFont().getSize(), 0.01);
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
                verifyPaperButton(ship);
                assertTrue(ship
                        .getGraphic() instanceof javafx.scene.image.ImageView);
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
            assertEquals(Color.web(expectedColor),
                    button.getBackground().getFills().get(0).getFill());
            assertEquals(expectedArmed ? 1 : 0, button.getTranslateY());
            assertEquals(expectedArmed, button.isArmed());
        });
    }

    public void verifyWidthsAndInk() {
        getRobot().interact(() -> {
            final Button shortButton = getButtonById("woodOk");
            final Button longButton = getButtonById("woodLong");
            assertTrue(longButton.getWidth() > shortButton.getWidth() + 80);
            assertEquals(shortButton.getHeight(), longButton.getHeight());
            assertEquals(shortButton.getBorder().getImages().get(0).getImage(),
                    longButton.getBorder().getImages().get(0).getImage());
            assertEquals(shortButton.getBorder().getImages().get(0).getWidths(),
                    longButton.getBorder().getImages().get(0).getWidths());
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

    public void resizeSamples(final double width, final double height) {
        getRobot().interact(() -> {
            samples.setWidth(width);
            samples.setHeight(height);
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyModalFrame(final String contentId) {
        getRobot().interact(() -> {
            final Scene scene = getNodeFinder().lookup("#" + contentId).query()
                    .getScene();
            final Stage stage = (Stage) scene.getWindow();
            final Region frame = (Region) scene.getRoot();
            assertEquals(StageStyle.TRANSPARENT, stage.getStyle());
            assertEquals(Modality.WINDOW_MODAL, stage.getModality());
            assertEquals(getPrimaryStage(), stage.getOwner());
            assertEquals(Color.TRANSPARENT, scene.getFill());
            assertTrue(frame.getStyleClass().contains("modal-frame"));
            assertNotNull(frame.getClip());
            final javafx.geometry.Bounds content = frame
                    .lookup(".modal-content").getBoundsInParent();
            for (final double x : new double[] { content.getMinX(),
                    content.getMaxX() }) {
                for (final double y : new double[] { content.getMinY(),
                        content.getMaxY() }) {
                    assertTrue(
                            frame.getClip().contains(
                                    frame.getClip().parentToLocal(x, y)),
                            "The frame leaves all existing controls inside its outline");
                }
            }
            final Region ink = (Region) frame.lookup(".modal-outline");
            assertEquals(2,
                    ink.getBorder().getStrokes().get(0).getWidths().getTop(),
                    0.001);
            final WritableImage image = snapshotScene(scene);
            final PixelReader pixels = image.getPixelReader();
            final int right = (int) image.getWidth() - 1;
            final int bottom = (int) image.getHeight() - 1;
            assertEquals(0, pixels.getColor(0, 0).getOpacity(), 0.01);
            assertEquals(0, pixels.getColor(right, 0).getOpacity(), 0.01);
            assertEquals(0, pixels.getColor(0, bottom).getOpacity(), 0.01);
            assertEquals(0, pixels.getColor(right, bottom).getOpacity(), 0.01);
            assertEquals(1, pixels.getColor(right / 2, bottom / 2).getOpacity(),
                    0.01);
            // Measure the first ink run, before any content or button below it.
            int firstInk = -1;
            int inkPixels = 0;
            for (int y = 0; y < image.getHeight() / 4; y++) {
                final Color pixel = pixels.getColor(right / 2, y);
                if (pixel.getOpacity() > 0.5 && pixel.getRed() < 0.35) {
                    if (firstInk < 0) {
                        firstInk = y;
                    }
                    inkPixels++;
                } else if (firstInk >= 0) {
                    break;
                }
            }
            assertTrue(firstInk >= 0 && firstInk < image.getHeight() * 0.06,
                    "The outer ink is visible near the top edge");
            assertTrue(inkPixels >= 1 && inkPixels <= 3,
                    "The 2px outline stays thin after resizing: " + inkPixels);
        });
    }

    public void captureModalOnScreen(final String fileName)
            throws java.io.IOException {
        final java.nio.file.Path output = java.nio.file.Path.of("target",
                "warehouse-qa", fileName);
        java.nio.file.Files.createDirectories(output.getParent());
        getRobot().robotContext().getCaptureSupport()
                .saveImage(getRobot().capture(new javafx.geometry.Rectangle2D(
                        getPrimaryStage().getX(), getPrimaryStage().getY(),
                        getPrimaryStage().getWidth(),
                        getPrimaryStage().getHeight())).getImage(), output);
    }

    public void verifyTransparentSurroundings(final String contentId) {
        getRobot().interact(() -> {
            final Stage modal = (Stage) getNodeFinder().lookup("#" + contentId)
                    .query().getScene().getWindow();
            final Scene owner = getPrimaryStage().getScene();
            final javafx.geometry.Point2D origin = owner.getRoot()
                    .localToScreen(0, 0);
            final WritableImage backdrop = snapshotScene(owner);
            final javafx.scene.robot.Robot screen = new javafx.scene.robot.Robot();
            for (final double x : new double[] { modal.getX(),
                    modal.getX() + modal.getWidth() - 1 }) {
                for (final double y : new double[] { modal.getY(),
                        modal.getY() + modal.getHeight() - 1 }) {
                    final Color expected = backdrop.getPixelReader().getColor(
                            (int) Math.round(x - origin.getX()),
                            (int) Math.round(y - origin.getY()));
                    final Color actual = screen.getPixelColor(x, y);
                    assertEquals(expected.getRed(), actual.getRed(), 0.02,
                            "Native modal corner shows the owner, not a plate");
                    assertEquals(expected.getGreen(), actual.getGreen(), 0.02);
                    assertEquals(expected.getBlue(), actual.getBlue(), 0.02);
                }
            }
        });
    }
}
