package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.UnitType;
import org.microcol.model.unit.UnitFreeColonist;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

public final class EquipmentPage extends AbstractScreen {
    public EquipmentPage(final TestContext context) {
        super(context);
    }

    public void open(final String action) {
        getRobot().clickOn("#colonyUnit-4", MouseButton.SECONDARY)
                .clickOn(action);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void openWithKeyboard(final int choice) {
        getRobot().clickOn("#colonyUnit-4").push(KeyCode.SHIFT, KeyCode.F10);
        for (int i = 0; i < choice; i++)
            getRobot().push(KeyCode.DOWN);
        getRobot().push(KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void confirm() {
        clickOnButtonWithId("equipmentConfirm");
    }

    public void confirmWithEnter() {
        getRobot().push(KeyCode.ENTER);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void cancel() {
        clickOnButtonWithId("equipmentCancel");
    }

    public void escape() {
        getRobot().push(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyChoice(final String caption, final boolean canConfirm,
            final String message) {
        assertTrue(isCssIdVisible("equipmentDialog"));
        assertEquals(caption, getLabeledById("equipmentCaption").getText());
        assertEquals(!canConfirm,
                getButtonById("equipmentConfirm").isDisabled());
        assertEquals(message, getLabeledById("equipmentMessage").getText());
    }

    public void verifyCostContains(final String expected) {
        assertTrue(
                getLabeledById("equipmentCost").getText().contains(expected));
    }

    public void verifyState(final int muskets, final int horses,
            final int tools, final boolean armed, final boolean mounted,
            final int heldTools) {
        final UnitFreeColonist unit = (UnitFreeColonist) getModel()
                .getUnitById(4);
        final org.microcol.model.ColonyWarehouse warehouse = getModel()
                .getColonyAt(Location.of(24, 16)).orElseThrow().getWarehouse();
        assertEquals(muskets, warehouse.getGoods(GoodsType.MUSKET).getAmount());
        assertEquals(horses, warehouse.getGoods(GoodsType.HORSE).getAmount());
        assertEquals(tools, warehouse.getGoods(GoodsType.TOOLS).getAmount());
        assertEquals(armed, unit.isHoldingGuns());
        assertEquals(mounted, unit.isMounted());
        assertEquals(heldTools, unit.getTools());
        assertEquals(UnitType.COLONIST, unit.getType());
        assertSame(getModel().getPlayerByName("Dutch"), unit.getOwner());
        assertEquals(10000, unit.getOwner().getGold());
        assertEquals(18, getModel().getCalendar().getNumberOfPlayedTurns());
    }
}
