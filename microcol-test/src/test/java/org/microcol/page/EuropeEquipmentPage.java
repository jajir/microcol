package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.stream.Collectors;
import org.microcol.model.PlaceCargoSlot;
import org.microcol.model.PlaceHighSea;
import org.microcol.model.UnitType;
import org.microcol.model.unit.UnitFreeColonist;
import org.microcol.model.unit.UnitWithCargo;
import org.testfx.util.WaitForAsyncUtils;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;

/** Player gestures and observations for the European equipment offer. */
public final class EuropeEquipmentPage extends AbstractScreen {
    public EuropeEquipmentPage(TestContext context) {
        super(context);
    }

    public void open(int unitId, String action) {
        getRobot().clickOn("#europe-pier-unit-" + unitId).clickOn(action);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void openContextMenu(int unitId) {
        getRobot().clickOn("#europe-pier-unit-" + unitId,
                MouseButton.SECONDARY);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void openWithKeyboard(int unitId, int choice) {
        getRobot().clickOn("#europe-pier-unit-" + unitId).push(KeyCode.ESCAPE)
                .push(KeyCode.ENTER);
        for (int i = 0; i <= choice; i++)
            getRobot().push(KeyCode.DOWN);
        getRobot().push(KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void confirm() {
        clickOnButtonWithId("europeEquipmentConfirm");
    }

    public void confirmWithEnter() {
        getRobot().push(KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void cancel() {
        clickOnButtonWithId("europeEquipmentCancel");
    }

    public void escape() {
        getRobot().push(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyMarketUnavailable(String message) {
        assertTrue(getNodeFinder().lookup("#europeMarketGoods").query()
                .isDisabled());
        assertTrue(isCssIdVisible("europeMarketRestriction"));
        assertEquals(message,
                getLabeledById("europeMarketRestriction").getText());
    }

    public void verifyMenu(String... choices) {
        assertEquals(List.of(choices),
                getListOfNodes(".context-menu .label").stream()
                        .map(n -> ((Label) n).getText())
                        .collect(Collectors.toList()));
    }

    public void verifyChoice(String caption, boolean enabled, String message) {
        assertTrue(isCssIdVisible("europeEquipmentDialog"));
        assertEquals(caption,
                getLabeledById("europeEquipmentCaption").getText());
        assertEquals(!enabled,
                getButtonById("europeEquipmentConfirm").isDisabled());
        assertEquals(message,
                getLabeledById("europeEquipmentMessage").getText());
    }

    public void verifyQuote(String expected) {
        assertEquals(expected, getLabeledById("europeEquipmentCost").getText());
    }

    public void verifyState(int id, int gold, boolean guns, boolean horses,
            int tools, Class<?> placeType) {
        UnitFreeColonist unit = (UnitFreeColonist) getModel().getUnitById(id);
        assertEquals(id, unit.getId());
        assertSame(getModel().getPlayerByName("Dutch"), unit.getOwner());
        assertEquals(UnitType.COLONIST, unit.getType());
        assertEquals(gold, unit.getOwner().getGold());
        assertEquals(guns, unit.isHoldingGuns());
        assertEquals(horses, unit.isMounted());
        assertEquals(tools, unit.getTools());
        assertTrue(placeType.isInstance(unit.getPlace()),
                "Expected " + placeType.getSimpleName());
        assertEquals(1, getModel().getAllUnits().stream()
                .filter(u -> u.getId() == id).count());
    }

    public void embark(int id) throws Exception {
        getRobot().drag("#europe-pier-unit-" + id, MouseButton.PRIMARY)
                .dropTo(PageComponentPanelDock.of(getContext())
                        .getListOfCrates().get(0));
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> getModel().getUnitById(id).isAtCargoSlot());
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void depart(int shipId) throws Exception {
        Parent panel = getNodeFinder().lookup(".to-new-world")
                .queryAs(Parent.class);
        HBox target = (HBox) panel.getChildrenUnmodifiable().stream()
                .filter(node -> node instanceof HBox).findFirst().orElseThrow();
        getRobot().drag(
                PageComponentPanelDock.of(getContext()).getUnitsInPort().get(0),
                MouseButton.PRIMARY).dropTo(target);
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> getModel().getUnitById(shipId).isAtHighSea());
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyPassenger(int id, int shipId) {
        UnitFreeColonist unit = (UnitFreeColonist) getModel().getUnitById(id);
        assertTrue(unit.getPlace() instanceof PlaceCargoSlot);
        assertEquals(shipId, unit.getPlaceCargoSlot().getOwnerUnit().getId());
        assertSame(unit, ((UnitWithCargo) getModel().getUnitById(shipId))
                .getCargo().getSlotByIndex(0).getUnit().orElseThrow());
    }

    public void verifyVoyage(int shipId, int turns) {
        assertTrue(getModel().getUnitById(shipId)
                .getPlace() instanceof PlaceHighSea);
        PlaceHighSea sea = (PlaceHighSea) getModel().getUnitById(shipId)
                .getPlace();
        assertFalse(sea.isTravelToEurope());
        assertEquals(turns, sea.getRemainigTurns());
    }

    public void verifyArrived(int shipId) {
        assertTrue(getModel().getUnitById(shipId).isAtPlaceLocation());
    }
}
