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

    public void setAutosaveEnabled(final boolean enabled) {
        final javafx.scene.control.CheckBox check = getNodeFinder()
                .lookup("#autosaveEnabled")
                .queryAs(javafx.scene.control.CheckBox.class);
        if (check.isSelected() != enabled) {
            getRobot().clickOn(check);
            org.testfx.util.WaitForAsyncUtils.waitForFxEvents();
        }
    }

    public void verifyAutosaveEnabled(final boolean expected) {
        final javafx.scene.control.CheckBox check = getNodeFinder()
                .lookup("#autosaveEnabled")
                .queryAs(javafx.scene.control.CheckBox.class);
        org.junit.jupiter.api.Assertions.assertEquals(expected,
                check.isSelected());
        org.junit.jupiter.api.Assertions.assertEquals(expected,
                getContext().getClassFromGuice(
                        org.microcol.gui.preferences.GamePreferences.class)
                        .isAutosaveEnabled());
    }

    public void verifyAutosaveLabel(final String expected) {
        org.junit.jupiter.api.Assertions.assertEquals(expected,
                getLabeledById("autosaveEnabled").getText());
    }

}
