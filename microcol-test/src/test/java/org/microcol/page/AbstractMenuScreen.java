package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.function.Consumer;

import org.microcol.gui.util.TitledPage;

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
	final String cssClass = "." + TitledPage.MAIN_TITLE_STYLE_CLASS;
	final List<Labeled> list = getListOfNodes(cssClass);
	assertEquals(1, list.size());
	final Labeled labeled = list.get(0);
	assertNotNull(labeled, String.format("There is no element with id '%s'", TitledPage.MAIN_TITLE_STYLE_CLASS));
	validation.accept(labeled.getText());
    }
}
