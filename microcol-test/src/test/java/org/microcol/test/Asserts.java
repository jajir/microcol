package org.microcol.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.testfx.api.FxAssert;
import org.testfx.service.finder.NodeFinder;

import javafx.scene.control.RadioButton;

public class Asserts {

    static void verifyRadioButton(final String buttonId, boolean isExpectedSelected) {
	final String id = "#" + buttonId;
	final RadioButton rb = getNodeFinder().lookup(id).queryAs(RadioButton.class);
	if (isExpectedSelected) {
	    assertTrue(rb.isSelected());
	} else {
	    assertFalse(rb.isSelected());
	}
    }

    public static NodeFinder getNodeFinder() {
	return FxAssert.assertContext().getNodeFinder();
    }
}
