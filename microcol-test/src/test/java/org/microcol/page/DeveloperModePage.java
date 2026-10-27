package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.TimeUnit;
import org.microcol.gui.preferences.DeveloperMode;
import org.microcol.gui.preferences.SettingService;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.screen.game.components.StatusBarView;
import org.microcol.gui.screen.game.gamepanel.ModeController;
import org.microcol.model.Location;
import org.testfx.util.WaitForAsyncUtils;
import com.google.gson.Gson;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

/** Player actions and explicit observations for runtime diagnostics. */
public final class DeveloperModePage extends AbstractScreen {
    private String savedGame;

    public DeveloperModePage(final TestContext context) {
        super(context);
    }

    public void setModeInSettings(final boolean enabled) {
        final CheckBox checkbox = getNodeFinder()
                .lookup("#settingDeveloperMode").queryAs(CheckBox.class);
        if (checkbox.isSelected() != enabled) {
            getRobot().clickOn(checkbox);
        }
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void pressF3() {
        getRobot().push(KeyCode.F3);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void holdF3() {
        getRobot().press(KeyCode.F3).sleep(1200, TimeUnit.MILLISECONDS);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void releaseF3() {
        getRobot().release(KeyCode.F3);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void openGameSettings() {
        getRobot().push(KeyCode.ESCAPE);
        getRobot().clickOn("#menuSettings");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void backToGame() {
        getRobot().clickOn("#buttonBack");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void leaveMap() {
        getRobot().moveTo("#labelGold");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void rememberGame() {
        getRobot().interact(
                () -> savedGame = new Gson().toJson(getModel().save()));
    }

    public String description() {
        return getLabeledById(StatusBarView.STATUS_BAR_LABEL_ID).getText();
    }

    public void verifyState(final boolean enabled) {
        assertEquals(enabled, getContext()
                .getClassFromGuice(DeveloperMode.class).isEnabled());
        assertEquals(enabled,
                getContext().getClassFromGuice(SettingService.class).load()
                        .isDeveloperMode());
    }

    public void verifySettings(final boolean enabled, final String label) {
        verifyState(enabled);
        final CheckBox checkbox = getNodeFinder()
                .lookup("#settingDeveloperMode").queryAs(CheckBox.class);
        assertEquals(enabled, checkbox.isSelected());
        assertEquals(label, checkbox.getText());
    }

    public void verifyHover(final Location location, final boolean enabled,
            final String ordinary) {
        final String prefix = "x=" + location.getX() + ", y=" + location.getY()
                + " · ";
        assertEquals(enabled ? prefix + ordinary : ordinary, description());
    }

    public void verifyUnknownTile(final Location location) {
        assertFalse(getContext().getClassFromGuice(GameModelController.class)
                .getHumanPlayer().isVisible(location));
        verifyHover(location, true, "This area is still hidden");
    }

    public void verifyNoCoordinates() {
        assertFalse(description().contains("x="), description());
        assertFalse(description().contains("y="), description());
    }

    public void verifyNoIndicator() {
        assertTrue(
                getNodeFinder().lookup("#developerModeIndicator").queryAll()
                        .isEmpty(),
                "Developer status indicator must not be created");
    }

    public void verifyGameUnchanged() {
        getRobot().interact(() -> assertEquals(savedGame,
                new Gson().toJson(getModel().save())));
    }

    public void verifyMoveMode(final boolean enabled) {
        assertEquals(enabled, getContext()
                .getClassFromGuice(ModeController.class).isMoveMode());
    }

    public void verifyText(final String id, final String expected) {
        assertEquals(expected, getNodeFinder().lookup("#" + id)
                .queryAs(TextField.class).getText());
    }

    public void pressModifiedF3() {
        getRobot().push(KeyCode.SHIFT, KeyCode.F3);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifySearchText(final String expected) {
        verifyText("colonizopediaSearch", expected);
    }
}
