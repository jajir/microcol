package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.microcol.gui.screen.menu.ButtonsPanelView;
import org.testfx.util.WaitForAsyncUtils;

public class WelcomePage extends AbstractMenuScreen {

    public static WelcomePage of(final TestContext context) {
        return new WelcomePage(context);
    }

    WelcomePage(final TestContext context) {
        super(context);
        WaitForAsyncUtils.waitForFxEvents();
        verifyMainScreen();
    }

    public void resizeWindow(final double width, final double height) {
        getRobot().interact(() -> {
            getContext().getPrimaryStage().setWidth(width);
            getContext().getPrimaryStage().setHeight(height);
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * The center image, repeated edges and canvas fills use identical pigments.
     */
    public void verifyMenuPalette() {
        getRobot().interact(() -> {
            final javafx.scene.image.WritableImage frame = snapshotScene(
                    getContext().getPrimaryStage().getScene());
            final int cx = (int) frame.getWidth() / 2;
            final int cy = (int) frame.getHeight() / 2;
            final int[] rows = { cy - 300, cy, cy + 300 };
            final int[] colors = { 0xFFF9EC, 0x649B97, 0xC5C18A };
            for (int i = 0; i < rows.length; i++) {
                for (int x : new int[] { cx + 300, cx + 389, cx + 393,
                        (int) frame.getWidth() - 10 }) {
                    assertEquals(colors[i],
                            frame.getPixelReader().getArgb(x, rows[i])
                                    & 0xFFFFFF,
                            "Continuous menu pigment at " + x + "," + rows[i]);
                }
            }
        });
    }

    public SettingPage openSetting() {
        verifyMainScreen();
        clickOnButtonWithId(ButtonsPanelView.BUTTON_SETTING_ID);
        return SettingPage.of(getContext());
    }

    public WelcomePage verifyMainScreen() {
        verifyMainTitle(realTitle -> {
            assertEquals("MicroCol", realTitle);
        });
        return this;
    }

    public GamePage loadGame() {
        clickOnButtonWithId(ButtonsPanelView.BUTTON_LOAD_ID);
        return GamePage.of(getContext());
    }

    public GamePage startFreeGame() {
        return openFreePlaySetup().start();
    }

    public FreePlaySetupPage openFreePlaySetup() {
        clickOnButtonWithId(ButtonsPanelView.BUTTON_START_FREE_GAME_ID);
        return new FreePlaySetupPage(getContext());
    }

}
