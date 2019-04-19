package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.microcol.gui.util.ButtonBarOk;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;

/**
 * Object allows to test one unit shown in right panel.
 */
public class DialogMessagePage extends AbstractScreen {

    public static DialogMessagePage of(final TestContext context) {
	return new DialogMessagePage(context);

    }

    private DialogMessagePage(final TestContext context) {
	super(context);
	assertNotNull(getButtonOk());
    }

    public void close() {
	getRobot().clickOn(getButtonOk(), MouseButton.PRIMARY);
	WaitForAsyncUtils.waitForFxEvents();
    }

    public Button getButtonOk() {
	return getNodeFinder().lookup("#" + ButtonBarOk.BUTTON_OK_ID).queryAs(Button.class);
    }

}
