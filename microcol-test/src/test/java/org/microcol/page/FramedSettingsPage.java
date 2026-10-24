package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import org.microcol.gui.preferences.GamePreferences;
import org.microcol.gui.util.FramedCheckBoxSkin;
import org.microcol.gui.util.FramedRadioButtonSkin;
import org.microcol.gui.util.RulerSliderSkin;
import org.testfx.util.WaitForAsyncUtils;

import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Labeled;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/** Player input and visual observations on the real Settings screen. */
public final class FramedSettingsPage extends AbstractScreen {
    public FramedSettingsPage(final TestContext context) {
        super(context);
    }

    private Control control(final String id) {
        return (Control) getNodeFinder().lookup("#" + id).query();
    }

    public void click(final String id) {
        getRobot().clickOn(control(id));
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void press(final String id, final KeyCode key) {
        getRobot().interact(() -> control(id).requestFocus());
        getRobot().push(key);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void dragSlider(final String id, final double fraction) {
        final Slider slider = (Slider) control(id);
        final Region track = (Region) slider.lookup(".track");
        final Bounds bounds = track.localToScreen(track.getBoundsInLocal());
        getRobot().moveTo(slider.lookup(".thumb")).press(MouseButton.PRIMARY)
                .moveTo(bounds.getMinX() + fraction * bounds.getWidth(),
                        (bounds.getMinY() + bounds.getMaxY()) / 2)
                .release(MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void setDisabled(final String id, final boolean disabled) {
        getRobot().interact(() -> control(id).setDisable(disabled));
    }

    public void clickSliderEnd(final String id) {
        final Region track = (Region) control(id).lookup(".track");
        final Bounds bounds = track.localToScreen(track.getBoundsInLocal());
        getRobot().clickOn(bounds.getMaxX() - 1,
                (bounds.getMinY() + bounds.getMaxY()) / 2);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyRulerThumb(final String id, final String expectedFill) {
        getRobot().interact(() -> {
            final Control c = control(id);
            final Bounds thumb = c.lookup(".thumb").getBoundsInParent();
            final Canvas drawing = (Canvas) c
                    .lookup(".settings-control-drawing");
            assertEquals(Color.web(expectedFill),
                    drawingColor(drawing,
                            (int) ((thumb.getMinX() + thumb.getMaxX()) / 2) - 5,
                            (int) thumb.getMinY() + 12));
        });
    }

    private Color drawingColor(final Canvas drawing, final int x, final int y) {
        final javafx.scene.image.WritableImage image = snapshotScene(
                drawing.getScene());
        final javafx.geometry.Point2D point = drawing.localToScene(x, y);
        return image.getPixelReader().getColor((int) point.getX(),
                (int) point.getY());
    }

    public void hover(final String id) {
        getRobot().moveTo(control(id));
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

    public void verifyAppearance() {
        getRobot().interact(() -> {
            for (final String id : new String[] { "settingGrid",
                    "settingFightAdvisor", "settingTurnSummary",
                    "settingOpenEuropeOnArrival", "autosaveEnabled", "rbCzech",
                    "rbEnglish", "settingVolume", "settingAnimationSpeed" }) {
                final Control c = control(id);
                assertTrue(c.getSkin() instanceof FramedCheckBoxSkin
                        || c.getSkin() instanceof FramedRadioButtonSkin
                        || c.getSkin() instanceof RulerSliderSkin, id);
                final Canvas drawing = (Canvas) c
                        .lookup(".settings-control-drawing");
                assertNotNull(drawing, id);
                assertTrue(c.getScene().getWidth() <= c.getScene().getWindow()
                        .getWidth());
                assertTrue(c.getScene().getHeight() <= c.getScene().getWindow()
                        .getHeight());
                assertTrue(drawing.isMouseTransparent());
                assertEquals(c.getWidth(), drawing.getWidth());
                if (c instanceof Labeled) {
                    final Labeled label = (Labeled) c;
                    assertEquals("Playpen Sans", label.getFont().getFamily());
                    assertEquals(label.getText(),
                            ((Text) c.lookup(".text")).getText(), id);
                }
                final Bounds b = c.localToScene(c.getBoundsInLocal());
                assertTrue(
                        c.getWidth() > 0
                                && c.getWidth() <= c.getScene().getWidth(),
                        id + " width=" + c.getWidth());
                assertTrue(b.getMinY() >= 0
                        && b.getMaxY() <= c.getScene().getHeight(), id);
            }
        });
    }

    public void verifyChecks(final boolean grid, final boolean fight,
            final boolean autosave) {
        final GamePreferences prefs = getContext()
                .getClassFromGuice(GamePreferences.class);
        assertEquals(grid, ((CheckBox) control("settingGrid")).isSelected());
        assertEquals(grid, prefs.isGridShown());
        assertEquals(fight,
                ((CheckBox) control("settingFightAdvisor")).isSelected());
        assertEquals(fight, prefs.isShowFightAdvisor());
        assertEquals(autosave,
                ((CheckBox) control("autosaveEnabled")).isSelected());
        assertEquals(autosave, prefs.isAutosaveEnabled());
    }

    /**
     * Checks the routine turn summary checkbox against the stored preference.
     */
    public void verifyTurnSummary(final boolean enabled) {
        final GamePreferences prefs = getContext()
                .getClassFromGuice(GamePreferences.class);
        assertEquals(enabled,
                ((CheckBox) control("settingTurnSummary")).isSelected());
        assertEquals(enabled, prefs.isShowTurnSummary());
    }

    /** Checks the Europe arrival checkbox against the stored preference. */
    public void verifyOpenEuropeOnArrival(final boolean enabled) {
        final GamePreferences prefs = getContext()
                .getClassFromGuice(GamePreferences.class);
        assertEquals(enabled, ((CheckBox) control("settingOpenEuropeOnArrival"))
                .isSelected());
        assertEquals(enabled, prefs.isOpenEuropeOnArrival());
    }

    /** Make the real Settings window small to check its readable layout. */
    public void resizeWindow(final int width, final int height) {
        getRobot().interact(() -> {
            getPrimaryStage().setWidth(width);
            getPrimaryStage().setHeight(height);
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyLanguage(final boolean czech) {
        assertEquals(czech, ((RadioButton) control("rbCzech")).isSelected());
        assertEquals(!czech, ((RadioButton) control("rbEnglish")).isSelected());
    }

    public void verifySliders(final int volume, final int speed) {
        final GamePreferences prefs = getContext()
                .getClassFromGuice(GamePreferences.class);
        assertEquals(volume, ((Slider) control("settingVolume")).getValue(),
                .001);
        assertEquals(speed,
                ((Slider) control("settingAnimationSpeed")).getValue(), .001);
        assertEquals(volume, prefs.getVolume());
        assertEquals(speed, prefs.getAnimationSpeed());
    }

    public void verifyFocused(final String id) {
        assertTrue(control(id).isFocused());
    }

    public void verifyFrameFill(final String id, final String expected) {
        getRobot().interact(() -> {
            final Control c = control(id);
            c.getScene().getRoot().layout();
            final Region box = (Region) c.lookup(".box");
            final Canvas drawing = (Canvas) c
                    .lookup(".settings-control-drawing");
            assertEquals(Color.web(expected),
                    drawingColor(drawing, (int) box.getLayoutX() + 4,
                            (int) box.getLayoutY() + 16),
                    "hover=" + c.isHover() + "; pressed=" + c.isPressed()
                            + "; armed=" + ((CheckBox) c).isArmed() + "; width="
                            + c.getWidth() + "; height=" + c.getHeight());
        });
    }

    public void verifyRadioDot(final String id, final boolean selected) {
        getRobot().interact(() -> {
            final Control c = control(id);
            final Region radio = (Region) c.lookup(".radio");
            final Canvas drawing = (Canvas) c
                    .lookup(".settings-control-drawing");
            assertEquals(Color.web(selected ? "#000000" : "#fff9ec"),
                    drawingColor(drawing, (int) radio.getLayoutX() + 16,
                            (int) radio.getLayoutY() + 16));
        });
    }
}
