package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.function.Consumer;

import org.microcol.gui.gamemenu.MenuHolderPanel;

import javafx.scene.control.Labeled;

public abstract class AbstractMenuScreen extends AbstractScreen {

    AbstractMenuScreen(final TestContext context) {
	super(context);
    }

    /**
     * VErify that menu screen title contains given string.
     * 
     * @param title required string
     */
    public void verifyThatMainTitleContains(final String title) {
	verifyMainTitle(realTitle -> {
	    if (!realTitle.contains(title)) {
		fail(String.format("Real screen title '%s' doesn't contains string '%s'", realTitle, title));
	    }
	});
    }

    /**
     * Verify title in main screens.
     * 
     * @param validation required validation consumer
     */
    protected void verifyMainTitle(final Consumer<String> validation) {
	final String id = "#" + MenuHolderPanel.MAIN_TITLE_ID;
	final Labeled labeled = getNodeFinder().lookup(id).queryLabeled();
	assertNotNull(labeled, String.format("There is no element with id '%s'", MenuHolderPanel.MAIN_TITLE_ID));
	validation.accept(labeled.getText());
    }
}
