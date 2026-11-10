package org.akropia.page;

import static org.junit.jupiter.api.Assertions.*;

import org.akropia.gui.preferences.GamePreferences;
import org.akropia.gui.util.FramedCheckBoxSkin;
import org.akropia.gui.util.FramedRadioButtonSkin;
import org.akropia.gui.util.RulerSliderSkin;
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

    private static javafx.scene.control.ScrollPane scrollOf(
            final javafx.scene.Node node) {
        javafx.scene.Node ancestor = node.getParent();
        while (ancestor != null
                && !(ancestor instanceof javafx.scene.control.ScrollPane)) {
            ancestor = ancestor.getParent();
        }
        return (javafx.scene.control.ScrollPane) ancestor;
    }

    public void click(final String id) {
        revealGameContent(control(id));
        getRobot().clickOn(control(id));
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void press(final String id, final KeyCode key) {
        revealGameContent(control(id));
        getRobot().interact(() -> control(id).requestFocus());
        getRobot().push(key);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void dragSlider(final String id, final double fraction) {
        final Slider slider = (Slider) control(id);
        revealGameContent(slider);
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
        revealGameContent(control(id));
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
        revealGameContent(control(id));
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

    public void verifyCommonAxis() {
        getRobot().interact(() -> {
            final double axis = control("rbCzech")
                    .localToScene(control("rbCzech").getLayoutBounds())
                    .getMinX();
            for (final String id : new String[] { "settingGrid",
                    "settingFightAdvisor", "autosaveEnabled",
                    "settingTurnSummary", "settingOpenEuropeOnArrival",
                    "settingVolume", "settingEffectsVolume",
                    "settingAnimationSpeed", "settingMapScrollSpeed",
                    "settingKeyboardShortcuts", "settingDeveloperMode" }) {
                final Control input = control(id);
                final javafx.scene.control.Label caption = (javafx.scene.control.Label) ((javafx.scene.layout.HBox) input
                        .getParent()).getChildren().get(0);
                final Bounds bounds = input
                        .localToScene(input.getLayoutBounds());
                final Bounds labelBounds = caption
                        .localToScene(caption.getLayoutBounds());
                assertEquals(axis, bounds.getMinX(), 1, id + " input axis");
                assertEquals(axis - 12, labelBounds.getMaxX(), 1,
                        id + " caption axis");
                assertTrue(
                        caption.localToScene(caption.getBoundsInLocal())
                                .getMaxX() < axis,
                        id + " text stays before the input");
                if (!caption.getText().isEmpty()) {
                    assertEquals(caption.getText(),
                            ((Text) caption.lookup(".text")).getText(),
                            id + " caption is not truncated");
                }
                final Bounds rowBounds = input.getParent()
                        .localToScene(input.getParent().getLayoutBounds());
                assertTrue(labelBounds.getMaxY() <= rowBounds.getMaxY() + 1,
                        id + " wrapped caption fits its row");
                assertTrue(bounds.getMaxY() <= rowBounds.getMaxY() + 1,
                        id + " input fits its row");
                assertTrue(caption.isWrapText(), id);
                assertEquals(javafx.geometry.Pos.TOP_RIGHT,
                        caption.getAlignment(), id);
                assertTrue(bounds.getMaxX() <= input.getScene().getWidth(), id);
                assertTrue(
                        caption.getHeight() >= caption
                                .prefHeight(caption.getWidth()) - 1,
                        id + " caption is not clipped");
            }
        });
    }

    public void verifyAppearance() {
        getRobot().interact(() -> {
            final Slider scroll = (Slider) control("settingMapScrollSpeed");
            scroll.getScene().getRoot().layout();
            final Slider animation = (Slider) control("settingAnimationSpeed");
            for (double tick = scroll.getMin(); tick <= scroll
                    .getMax(); tick++) {
                final String expected = tick == scroll.getMin()
                        ? animation.getLabelFormatter()
                                .toString(animation.getMin())
                        : tick == scroll.getMax()
                                ? animation.getLabelFormatter().toString(
                                        animation.getMax())
                                : null;
                assertEquals(expected,
                        scroll.getLabelFormatter().toString(tick));
            }
            final javafx.scene.layout.VBox developer = (javafx.scene.layout.VBox) scroll
                    .getScene().lookup("#developerSettings");
            assertEquals(1, developer.getChildren().size());
        });
        verifyCommonAxis();
        getRobot().interact(() -> {
            final javafx.scene.Node heading = java.util.Optional
                    .ofNullable(
                            control("rbCzech").getScene().lookup(".main-title"))
                    .orElse(control("rbCzech").getScene()
                            .lookup(".screen-title"));
            assertNotNull(heading, "Settings heading in either host");
            final Control first = control("rbCzech");
            final javafx.scene.control.ScrollPane contentScroll = scrollOf(
                    first);
            final javafx.scene.Node visibleContent = contentScroll == null
                    ? first
                    : contentScroll.lookup(".viewport");
            assertTrue(
                    heading.localToScene(heading.getBoundsInLocal())
                            .getMaxY() <= visibleContent
                                    .localToScene(
                                            visibleContent.getBoundsInLocal())
                                    .getMinY(),
                    "The visible Settings content must remain below the heading");
            for (final String id : new String[] { "settingDeveloperMode",
                    "settingGrid", "settingFightAdvisor", "settingTurnSummary",
                    "settingOpenEuropeOnArrival", "autosaveEnabled", "rbCzech",
                    "rbEnglish", "settingVolume", "settingEffectsVolume",
                    "settingAnimationSpeed" }) {
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
                    if (c instanceof CheckBox) {
                        final javafx.scene.control.Label caption = (javafx.scene.control.Label) control(
                                id + "Caption");
                        assertEquals(label.getText(), caption.getText(), id);
                        assertEquals(c, caption.getLabelFor(), id);
                    } else {
                        assertEquals(label.getText(),
                                ((Text) c.lookup(".text")).getText(), id);
                    }
                }
                final Bounds b = c.localToScene(c.getBoundsInLocal());
                assertTrue(
                        c.getWidth() > 0
                                && c.getWidth() <= c.getScene().getWidth(),
                        id + " width=" + c.getWidth());
                // Scrolled Settings keep every control inside the content
                // and the visible viewport inside the window.
                final javafx.scene.control.ScrollPane scroll = scrollOf(c);
                final Bounds area = scroll == null
                        ? new javafx.geometry.BoundingBox(0, 0,
                                c.getScene().getWidth(),
                                c.getScene().getHeight())
                        : scroll.getContent().localToScene(
                                scroll.getContent().getBoundsInLocal());
                assertTrue(b.getMinY() >= area.getMinY() - 1
                        && b.getMaxY() <= area.getMaxY() + 1, id);
                if (scroll != null) {
                    final Bounds viewport = scroll
                            .localToScene(scroll.getBoundsInLocal());
                    assertTrue(viewport.getMinY() >= 0
                            && viewport.getMaxY() <= c.getScene().getHeight(),
                            id);
                    assertTrue(b.getMaxX() <= viewport.getMaxX() + 1,
                            id + " stays without horizontal scrolling");
                }
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

    public void verifyMapScrollSpeed(final int expected) {
        assertEquals(expected,
                ((Slider) control("settingMapScrollSpeed")).getValue(), .001);
        assertEquals(expected, getContext()
                .getClassFromGuice(GamePreferences.class).getMapScrollSpeed());
        final org.akropia.gui.WasdController wasd = getContext()
                .getClassFromGuice(org.akropia.gui.WasdController.class);
        getRobot().interact(
                () -> wasd.onKeyPressed(new javafx.scene.input.KeyEvent(
                        javafx.scene.input.KeyEvent.KEY_PRESSED, "", "",
                        KeyCode.D, false, false, false, false)));
        try {
            assertEquals(org.akropia.gui.Point.of(expected * 2, 0),
                    wasd.getDiff());
        } finally {
            getRobot().interact(
                    () -> wasd.onKeyReleased(new javafx.scene.input.KeyEvent(
                            javafx.scene.input.KeyEvent.KEY_RELEASED, "", "",
                            KeyCode.D, false, false, false, false)));
        }
    }

    public void verifySliders(final int musicVolume, final int speed) {
        final GamePreferences prefs = getContext()
                .getClassFromGuice(GamePreferences.class);
        assertEquals(musicVolume,
                ((Slider) control("settingVolume")).getValue(), .001);
        assertEquals(speed,
                ((Slider) control("settingAnimationSpeed")).getValue(), .001);
        assertEquals(musicVolume, prefs.getMusicVolume());
        assertEquals(speed, prefs.getAnimationSpeed());
    }

    /**
     * Checks both volume sliders, the current values they show and the stored
     * preferences.
     */
    public void verifyVolumes(final int music, final int effects,
            final String musicValue) {
        final GamePreferences prefs = getContext()
                .getClassFromGuice(GamePreferences.class);
        assertEquals(music, ((Slider) control("settingVolume")).getValue(),
                .001);
        assertEquals(effects,
                ((Slider) control("settingEffectsVolume")).getValue(), .001);
        assertEquals(musicValue,
                ((Labeled) control("settingVolumeValue")).getText());
        assertTrue(control("settingEffectsVolume").getScene().getRoot()
                .lookupAll("#settingEffectsVolumeValue").isEmpty());
        assertEquals(music, prefs.getMusicVolume());
        assertEquals(effects, prefs.getEffectsVolume());
    }

    /**
     * Checks the volume captions and that each slider names its silent
     * endpoint, in the normal Settings font.
     */
    public void verifyVolumeTexts(final String musicCaption,
            final String effectsCaption, final String silent) {
        getRobot().interact(() -> {
            // Tick labels are rebuilt in the axis layout after a language
            // change.
            control("settingVolume").getScene().getRoot().layout();
            verifyVolumeRow("settingVolume", musicCaption, silent);
            verifyVolumeRow("settingEffectsVolume", effectsCaption, silent);
        });
    }

    private void verifyVolumeRow(final String id, final String caption,
            final String silent) {
        final Labeled label = (Labeled) control(id + "Caption");
        assertEquals(caption, label.getText());
        assertEquals("Playpen Sans", label.getFont().getFamily());
        assertTrue(label.isVisible() && label.getWidth() > 0, id);
        assertTrue(
                control(id).lookupAll("Text").stream()
                        .anyMatch(node -> node.isVisible()
                                && silent.equals(((Text) node).getText())),
                id + " shows its silent endpoint");
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
