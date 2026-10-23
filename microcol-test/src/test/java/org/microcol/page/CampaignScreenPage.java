package org.microcol.page;

import javafx.scene.Node;
import javafx.scene.control.Button;

import org.testfx.util.WaitForAsyncUtils;

/**
 * The campaign screen that the game shows after a mission or a game ends.
 */
public final class CampaignScreenPage extends AbstractMenuScreen {

    public CampaignScreenPage(final TestContext context) {
        super(context);
    }

    /** The screen is the campaign menu, not the game or the main menu. */
    public void verifyCampaignScreen() {
        verifyThatMainTitleContains("Campaign");
    }

    /** Leave the campaign screen for the main menu. */
    public WelcomePage back() {
        final Button back = getNodeFinder()
                .lookup((Node node) -> node instanceof Button
                        && "Back".equals(((Button) node).getText()))
                .queryAs(Button.class);
        getRobot().clickOn(back);
        WaitForAsyncUtils.waitForFxEvents();
        return WelcomePage.of(getContext());
    }
}
