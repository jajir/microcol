package org.microcol.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.function.Consumer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.MicroCol;
import org.microcol.gui.gamemenu.ButtonsPanelView;
import org.microcol.gui.gamemenu.MenuHolderPanel;
import org.microcol.gui.gamemenu.SettingButtonsView;
import org.microcol.gui.gamemenu.SettingLanguageView;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.service.query.EmptyNodeQueryException;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class StartMicrocolTest {

    private final static String BUTTON_SETTING_ID_NOT_EXISTING = "#buttonSetting-notExisting";

    @Start
    private void start(final Stage primaryStage) throws Exception {
	MicroCol microCol = new MicroCol();
	microCol.start(primaryStage);
    }

    @Test
    void verify_exception_is_throws_when_object_isnt_exists(final FxRobot robot) throws Exception {
	Assertions.assertThrows(EmptyNodeQueryException.class, () -> {
	    FxAssert.verifyThat(BUTTON_SETTING_ID_NOT_EXISTING, obj -> {
		return true;
	    });
	});
    }

    @Test
    void verify_setting_page_is_available(final FxRobot robot) throws Exception {
	openSetting(robot);

	clickOnButtonWithId(robot, SettingButtonsView.BUTTON_BACK_ID);
	verifyMainScreen();
    }

    @Test
    void verify_language_is_changed_immediatelly(final FxRobot robot) throws Exception {
	openSetting(robot);
	
	//verify that english is selected
	verifyRadioButton(SettingLanguageView.RB_CZECH_ID, false);
	verifyRadioButton(SettingLanguageView.RB_ENGLISH_ID, true);
	
	//select czech
	clickOnButtonWithId(robot, SettingLanguageView.RB_CZECH_ID);
	
	//verify czech title
	verifyThatMainTitleContains("Nastaven");
	verifyRadioButton(SettingLanguageView.RB_CZECH_ID, true);
	verifyRadioButton(SettingLanguageView.RB_ENGLISH_ID, false);

	//select english
	clickOnButtonWithId(robot, SettingLanguageView.RB_ENGLISH_ID);
	
	//verify english
	verifyRadioButton(SettingLanguageView.RB_CZECH_ID, false);
	verifyRadioButton(SettingLanguageView.RB_ENGLISH_ID, true);
    }
    
    private void verifyRadioButton(final String buttonId, boolean expectedIsSelected) {
	final String id = "#" + buttonId;
	FxAssert.verifyThat(id, obj -> {
	    final RadioButton rb = (RadioButton) obj;
	    if(expectedIsSelected) {
		assertTrue(rb.isSelected());		
	    }else {
		assertFalse(rb.isSelected());
	    }
	    return true;
	});
    }

    private void openSetting(final FxRobot robot) {
	verifyMainScreen();
	clickOnButtonWithId(robot, ButtonsPanelView.BUTTON_SETTING_ID);
	verifyThatMainTitleContains("Setting");
    }

    private void clickOnButtonWithId(final FxRobot robot, final String buttonId) {
	final String id = "#" + buttonId;
	robot.clickOn(id);
	WaitForAsyncUtils.waitForFxEvents();
    }

    private void verifyMainScreen() {
	verifyMainTitle(realTitle -> {
	    assertEquals("MicroCol", realTitle);
	});
    }

    private void verifyThatMainTitleContains(final String title) {
	verifyMainTitle(realTitle -> {
	    if (!realTitle.contains(title)) {
		fail(String.format("Real screen title '%s' doesn't contains string '%s'", realTitle, title));
	    }
	});
    }

    private void verifyMainTitle(final Consumer<String> validation) {
	final String id = "#" + MenuHolderPanel.MAIN_TITLE_ID;
	FxAssert.verifyThat(id, obj -> {
	    final Label mainTitle = (Label) obj;
	    validation.accept(mainTitle.getText());
	    return true;
	});
    }

}
