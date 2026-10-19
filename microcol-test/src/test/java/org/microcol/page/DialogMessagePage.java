package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.TimeUnit;

import org.microcol.gui.util.ButtonBarOk;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
    }

    public void verifyReady() {
        assertNotNull(getButtonOk());
    }

    public void close() {
        getRobot().clickOn(getButtonOk(), MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyDialogTextContains(final String expectedPart)
            throws Exception {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> findTextContaining(expectedPart) != null);
        assertNotNull(findTextContaining(expectedPart),
                "No dialog text contains: " + expectedPart);
    }

    public Button getButtonOk() {
        return getNodeFinder().lookup("#" + ButtonBarOk.BUTTON_OK_ID)
                .queryAs(Button.class);
    }

    private Label findTextContaining(final String expectedPart) {
        return getNodeFinder()
                .lookup((Node node) -> node instanceof Label
                        && ((Label) node).getText() != null
                        && ((Label) node).getText().contains(expectedPart))
                .queryAs(Label.class);
    }

}
