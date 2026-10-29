package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;
import org.microcol.model.*;
import org.microcol.model.unit.UnitFreeColonist;
import org.testfx.util.WaitForAsyncUtils;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;

/** Review and settle tribute through the political report. */
public final class TributePage extends AbstractScreen {
    public TributePage(TestContext context) {
        super(context);
    }

    public void review() {
        clickOnButtonWithId("tribute-0-1");
    }

    public void cancel() {
        clickOnButtonWithId("tributeCancel");
    }

    public void later() {
        clickOnButtonWithId("tributeLater");
    }

    public void confirm() {
        clickOnButtonWithId("tributeConfirm");
    }

    public void escape() {
        getRobot().push(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void confirmTwice() {
        Button button = getButtonById("tributeConfirm");
        getRobot().push(KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();
        getRobot().interact(button::fire);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void weakenAndMakePeace() {
        getRobot().interact(() -> {
            ((UnitFreeColonist) getModel().getUnitById(1))
                    .setHoldingGuns(false);
            getModel().acceptPeace(getModel().getPlayerByName("Dutch"),
                    getModel().getPlayerByName("France"));
        });
    }

    public void setGold(String nation, int amount) {
        getRobot().interact(
                () -> getModel().getPlayerByName(nation).setGold(amount));
    }

    public void verifyQuote(int amount, int gold, int after, String outcome) {
        String text = getLabeledById("tributeDetail").getText();
        assertTrue(text
                .startsWith("Demand tribute from France.\nExpected payment: "
                        + amount + " gold.\nYour treasury when quoted: " + gold
                        + " gold.\nAfter payment: " + after + " gold."));
        assertTrue(text.contains(outcome));
        assertFalse(getButtonById("tributeConfirm").isDisabled());
    }

    public void verifyState(int actorGold, int rivalGold,
            EuropeanRelationState relation, boolean pending, int remaining) {
        Model model = getModel();
        Player actor = model.getPlayerByName("Dutch");
        Player rival = model.getPlayerByName("France");
        assertEquals(actorGold, actor.getGold());
        assertEquals(rivalGold, rival.getGold());
        assertEquals(relation, model.getEuropeanRelation(actor, rival));
        assertEquals(relation, model.getEuropeanRelation(rival, actor));
        assertEquals(pending,
                model.getPendingTributeDemand(actor, rival).isPresent());
        assertEquals(remaining, model.getTributeTurnsRemaining(actor, rival));
    }

    public void verifyCooldown(int remaining) {
        Button action = getButtonById("tribute-0-1");
        assertTrue(action.isDisabled());
        assertEquals("Another demand is available in " + remaining + " turns.",
                action.getAccessibleHelp());
    }

    public void verifyChanged() {
        assertTrue(getLabeledById("tributeFeedback").getText()
                .contains("The contact or terms changed"));
        assertTrue(getButtonById("tributeConfirm").isDisabled());
    }
}
