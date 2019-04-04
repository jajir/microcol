package org.microcol.page;

import org.microcol.gui.screen.setting.SettingButtonsView;
import org.microcol.gui.screen.setting.SettingLanguageView;

public class SettingPage extends AbstractMenuScreen {

    public static SettingPage of(final TestContext context) {
	return new SettingPage(context);
    }

    private SettingPage(final TestContext context) {
	super(context);
    }

    public WelcomePage goBack() {
	clickOnButtonWithId(SettingButtonsView.BUTTON_BACK_ID);
	return WelcomePage.of(getContext());
    }
    
    public void setCzechLanguage() {
	clickOnButtonWithId(SettingLanguageView.RB_CZECH_ID);
    }
    
    public void setEnglishLanguage() {
	clickOnButtonWithId(SettingLanguageView.RB_ENGLISH_ID);
    }

}
