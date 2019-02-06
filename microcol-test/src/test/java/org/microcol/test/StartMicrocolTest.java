package org.microcol.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.MicroCol;
import org.microcol.gui.gamemenu.SettingButtonsView;
import org.testfx.api.FxAssert;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.service.query.EmptyNodeQueryException;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.control.Button;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class StartMicrocolTest {

    private final static String BUTTON_SETTING_ID = "#buttonSetting";

    private final static String BUTTON_SETTING_ID_NOT_EXISTING = "#buttonSetting-notExisting";

    @Start
    private void start(final Stage primaryStage) throws Exception {
	MicroCol microCol = new MicroCol();
	microCol.start(primaryStage);
    }

    @Test
    void node_doesnt_exists(final FxRobot robot) throws Exception {
	Assertions.assertThrows(EmptyNodeQueryException.class, () -> {
	    FxAssert.verifyThat(BUTTON_SETTING_ID_NOT_EXISTING, obj -> {
		return true;
	    });
	});
    }

    @Test
    void when_button_is_clicked_text_changes(final FxRobot robot) throws Exception {
	// verify presence of button
	FxAssert.verifyThat(BUTTON_SETTING_ID, obj -> {
	    final Button button = (Button) obj;
	    assertEquals("Setting", button.getText());
	    return true;
	});

	robot.clickOn(BUTTON_SETTING_ID);
	WaitForAsyncUtils.waitForFxEvents();

	robot.clickOn("#" + SettingButtonsView.BUTTON_BACK_ID);

	Thread.sleep(1000);
    }

}
