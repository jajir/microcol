package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.PlaceHighSea;
import org.microcol.model.unit.Ship;
import org.microcol.model.unit.UnitFreeColonist;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;

/** Player controls and observations for one saved Atlantic route. */
public final class VoyageReversalPage extends AbstractScreen {
    public VoyageReversalPage(final TestContext context) {
        super(context);
    }

    public void openReverse() {
        getRobot().clickOn("#voyage-reverse-3");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void openReverseWithKeyboard() {
        final Button button = getNodeFinder().lookup("#voyage-reverse-3")
                .queryAs(Button.class);
        getRobot().interact(button::requestFocus);
        getRobot().push(KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void cancelReverse() {
        getRobot().clickOn("#voyageReverseCancel");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void escapeReverse() {
        getRobot().push(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void confirmReverse() {
        getRobot().clickOn("#voyageReverseConfirm");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void closeStaleFeedback() {
        getRobot().clickOn("#buttonOk");
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyTransit(final boolean toEurope, final int position,
            final int remaining, final String expectedLabel) {
        final Ship ship = (Ship) getModel().getUnitById(3);
        final PlaceHighSea voyage = (PlaceHighSea) ship.getPlace();
        assertEquals(toEurope, voyage.isTravelToEurope());
        assertTrue(voyage.isOriginalToEurope());
        assertEquals(position, voyage.getPosition());
        assertEquals(remaining, voyage.getRemainigTurns());
        assertEquals(4, voyage.getOriginalTurns());
        assertEquals(1, voyage.getWeather());
        assertEquals(Location.of(2, 17),
                ship.getDepartureLocation().orElseThrow());
        assertEquals(Goods.of(GoodsType.ORE, 7),
                ship.getCargo().getSlotByIndex(0).getGoods().orElseThrow());
        assertEquals(4, ship.getCargo().getSlotByIndex(1).getUnit()
                .orElseThrow().getId());
        assertEquals(80,
                ((UnitFreeColonist) getModel().getUnitById(4)).getTools());
        assertEquals(expectedLabel, getNodeFinder()
                .lookup("#voyage-remaining-3").queryAs(Label.class).getText());
    }

    public void verifyReverseAction(final String expected) {
        final Button button = getNodeFinder().lookup("#voyage-reverse-3")
                .queryAs(Button.class);
        assertEquals(expected, button.getText());
        assertFalse(button.isDisabled());
    }

    public void verifyConfirmation(final String title, final String question) {
        assertEquals(title, getNodeFinder().lookup("#voyageReverseTitle")
                .queryAs(Label.class).getText());
        assertEquals(question, getNodeFinder().lookup("#voyageReverseQuestion")
                .queryAs(Label.class).getText());
        assertTrue(getNodeFinder().lookup("#voyageReverseConfirm")
                .queryAs(Button.class).isVisible());
    }

    public void verifyStaleFeedback(final String expected) {
        assertEquals(expected, getNodeFinder().lookup(expected)
                .queryAs(Label.class).getText());
    }

    public void verifyReturnedToSource() {
        final Ship ship = (Ship) getModel().getUnitById(3);
        assertTrue(ship.isAtPlaceLocation());
        assertEquals(Location.of(2, 17), ship.getLocation());
        assertEquals(Goods.of(GoodsType.ORE, 7),
                ship.getCargo().getSlotByIndex(0).getGoods().orElseThrow());
        assertEquals(4, ship.getCargo().getSlotByIndex(1).getUnit()
                .orElseThrow().getId());
        assertEquals(80,
                ((UnitFreeColonist) getModel().getUnitById(4)).getTools());
    }

    public void verifyWaitingAtSource() {
        final PlaceHighSea voyage = (PlaceHighSea) getModel().getUnitById(3)
                .getPlace();
        assertFalse(voyage.isTravelToEurope());
        assertEquals(0, voyage.getPosition());
        assertEquals(0, voyage.getRemainigTurns());
        assertEquals("Waiting: the arrival square is occupied.", getNodeFinder()
                .lookup("#voyage-remaining-3").queryAs(Label.class).getText());
        final Ship ship = (Ship) getModel().getUnitById(3);
        assertEquals(Goods.of(GoodsType.ORE, 7),
                ship.getCargo().getSlotByIndex(0).getGoods().orElseThrow());
        assertEquals(4, ship.getCargo().getSlotByIndex(1).getUnit()
                .orElseThrow().getId());
        assertEquals(80,
                ((UnitFreeColonist) getModel().getUnitById(4)).getTools());
    }
}
