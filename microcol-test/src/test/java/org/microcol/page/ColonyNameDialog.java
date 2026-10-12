package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import org.microcol.model.Colony;
import org.microcol.model.Location;
import org.microcol.model.Unit;
import org.testfx.util.WaitForAsyncUtils;

import javafx.css.PseudoClass;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

public final class ColonyNameDialog extends AbstractScreen {

    public ColonyNameDialog(final TestContext context) {
        super(context);
    }

    private TextField input() {
        return getNodeFinder().lookup("#colonyNameInput")
                .queryAs(TextField.class);
    }

    public void enterName(final String value) {
        getRobot().clickOn(input()).push(KeyCode.END)
                .eraseText(input().getText().length()).write(value);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void generate() {
        clickOnButtonWithId("colonyNameGenerate");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void save() {
        clickOnButtonWithId("colonyNameSave");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void cancel() {
        clickOnButtonWithId("colonyNameCancel");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void escape() {
        getRobot().push(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void confirmWithEnter() {
        getRobot().clickOn(input()).push(KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyDraft(final String expected) {
        assertEquals(expected, input().getText());
    }

    public void verifyValidation(final String expectedMessage,
            final boolean expectedInvalid) {
        assertEquals(expectedMessage,
                getLabeledById("colonyNameError").getText());
        assertEquals(expectedInvalid,
                getButtonById("colonyNameSave").isDisabled());
        assertEquals(expectedInvalid, input().getPseudoClassStates()
                .contains(PseudoClass.getPseudoClass("invalid")));
    }

    public void verifyFoundingUnchanged(final Unit founder,
            final Location expectedLocation, final int expectedMoves,
            final int expectedCount, final int expectedNextId) {
        assertTrue(founder.isAtPlaceLocation());
        assertEquals(expectedLocation, founder.getLocation());
        assertEquals(expectedMoves, founder.getActionPoints());
        assertEquals(expectedCount, getModel().getColonies().size());
        assertEquals(expectedNextId, getModel().save().getNextColonyId());
        assertTrue(getModel().getColonyAt(expectedLocation).isEmpty());
    }

    public void verifyFounded(final Unit founder, final int expectedId,
            final String expectedName, final Location expectedLocation,
            final int expectedCount) {
        final Colony colony = getModel().getColonyById(expectedId)
                .orElseThrow();
        assertEquals(expectedName, colony.getName());
        assertEquals(expectedLocation, colony.getLocation());
        assertSame(founder.getOwner(), colony.getOwner());
        assertTrue(colony.getColonyFields().stream()
                .anyMatch(field -> field.getUnit().orElse(null) == founder));
        assertEquals(expectedCount, getModel().getColonies().size());
        assertEquals(expectedName, getLabeledById("colonyName").getText());
    }

    public void verifyStoredName(final int expectedId,
            final String expectedName) {
        assertEquals(expectedName,
                getModel().getColonyById(expectedId).orElseThrow().getName());
    }
}
