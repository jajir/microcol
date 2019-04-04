package org.microcol.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.gui.FileSelectingService;
import org.microcol.gui.screen.setting.SettingLanguageView;
import org.microcol.model.Location;
import org.microcol.page.GamePage;
import org.microcol.page.SettingPage;
import org.microcol.page.WelcomePage;
import org.mockito.Mockito;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.service.query.EmptyNodeQueryException;

import com.google.inject.Binder;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class SimpleOperationsTest extends AbstractMicroColTest {

    private final static String BUTTON_SETTING_ID_NOT_EXISTING = "#buttonSetting-notExisting";

    private final static File verifyLoadingUnloading = new File(
	    "src/test/scenarios/test-verify-loading-unloading.microcol");

    @Start
    void start(final Stage primaryStage) throws Exception {
	initialize(primaryStage, getClass());
    }

    @Override
    protected void bind(Binder binder) {
	FileSelectingService fileSelectingService = Mockito.mock(FileSelectingService.class);
	Mockito.when(fileSelectingService.loadFile(Mockito.any(File.class))).thenReturn(verifyLoadingUnloading);
	binder.bind(FileSelectingService.class).toInstance(fileSelectingService);
    }

    @Test
    @Tag("ci")
    void verify_that_test_files_exists() {
	assertTrue(verifyLoadingUnloading.exists(),
		String.format("File '%s' doesn't exists", verifyLoadingUnloading.getAbsoluteFile()));
	assertTrue(verifyLoadingUnloading.isFile(),
		String.format("File '%s' is directory", verifyLoadingUnloading.getAbsoluteFile()));
    }

    @Test
    @Tag("ci")
    void verify_exception_is_throws_when_object_isnt_exists() throws Exception {
	Assertions.assertThrows(EmptyNodeQueryException.class, () -> {
	    FxAssert.verifyThat(BUTTON_SETTING_ID_NOT_EXISTING, obj -> {
		return true;
	    });
	});
    }

    @Test
    @Tag("ci")
    void verify_setting_page_is_available() throws Exception {
	SettingPage settingPage = WelcomePage.of(getContext()).openSetting();

	settingPage.goBack().verifyMainScreen();
    }

    @Test
    @Tag("ci")
    void verify_language_is_changed_immediatelly(final FxRobot robot) throws Exception {
	assertNotNull(robot);
	SettingPage settingPage = WelcomePage.of(getContext()).openSetting();

	// verify that english is selected
	Asserts.verifyRadioButton(SettingLanguageView.RB_CZECH_ID, false);
	Asserts.verifyRadioButton(SettingLanguageView.RB_ENGLISH_ID, true);

	// select czech
	settingPage.setCzechLanguage();

	// verify czech title
	settingPage.verifyThatMainTitleContains("Nastaven");
	Asserts.verifyRadioButton(SettingLanguageView.RB_CZECH_ID, true);
	Asserts.verifyRadioButton(SettingLanguageView.RB_ENGLISH_ID, false);

	// select english
	settingPage.setEnglishLanguage();

	// verify english
	Asserts.verifyRadioButton(SettingLanguageView.RB_CZECH_ID, false);
	Asserts.verifyRadioButton(SettingLanguageView.RB_ENGLISH_ID, true);

	// back to main screen
	settingPage.goBack().verifyMainScreen();
    }

    /**
     * Just such very simple test could run at CI. More complicated test fail in
     * headless mode.
     * 
     * @throws Exception
     */
    @Test
    @Tag("ci")
    void verify_moving_with_unit() throws Exception {
	final GamePage gamePage = WelcomePage.of(getContext()).loadGame();

	gamePage.moveMouseAtLocation(Location.of(22, 12));
	gamePage.dragMouseAtLocation(Location.of(24, 11));
    }

}
