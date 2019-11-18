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
	clickOnButtonWithId(ButtonsPanelView.BUTTON_START_FREE_GAME_ID);
	return GamePage.of(getContext());
    }

}
