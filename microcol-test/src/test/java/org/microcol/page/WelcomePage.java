package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.microcol.gui.gamemenu.ButtonsPanelView;

public class WelcomePage extends AbstractMenuScreen {

    public static WelcomePage of(final TestContext context) {
	return new WelcomePage(context);
    }

    WelcomePage(final TestContext context) {
	super(context);
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

}
