package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.TimeUnit;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Model;
import org.microcol.model.unit.Ship;
import org.testfx.util.WaitForAsyncUtils;

/**
 * Controlled saved randomness and explicit observations of the naval journey.
 */
public final class NavalBattlePage extends AbstractScreen {
    public NavalBattlePage(TestContext context) {
        super(context);
    }

    public void chooseSavedSeed(long seed) throws Exception {
        var field = Model.class.getDeclaredField("combatRandomState");
        field.setAccessible(true);
        field.setLong(getModel(), seed);
    }

    public void changeReviewedCargo() {
        getRobot().interact(() -> ((Ship) getModel().getUnitById(0)).getCargo()
                .getSlotByIndex(0).removeCargo(Goods.of(GoodsType.ORE, 1)));
    }

    public void verifyOutcomeText(String... fragments) throws Exception {
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> !getListOfNodes("#navalBattleOutcome").isEmpty());
        String text = getLabeledById("navalBattleOutcome").getText();
        for (String fragment : fragments) {
            assertTrue(text.contains(fragment), text);
        }
        assertTrue(
                getButtonById("buttonOk").getScene().getWindow().isShowing());
    }

    public void closeOutcome() throws Exception {
        var window = WaitForAsyncUtils.waitForAsyncFx(5000,
                () -> getLabeledById("navalBattleOutcome").getScene()
                        .getWindow());
        getRobot().push(javafx.scene.input.KeyCode.ESCAPE);
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> WaitForAsyncUtils
                .waitForAsyncFx(5000, () -> !window.isShowing()));
    }

    public void verifyAttackerPrizeAndSinking() {
        Ship ship = (Ship) getModel().getUnitById(0);
        assertEquals(0, ship.getActionPoints());
        assertEquals(Goods.of(GoodsType.ORE, 100),
                ship.getCargo().getSlotByIndex(0).getGoods().orElseThrow());
        assertEquals(Goods.of(GoodsType.FUR, 60),
                ship.getCargo().getSlotByIndex(2).getGoods().orElseThrow());
        assertEquals(Goods.of(GoodsType.ORE, 2),
                ship.getCargo().getSlotByIndex(3).getGoods().orElseThrow());
        assertEquals(9, ship.getCargo().getSlotByIndex(1).getUnit()
                .orElseThrow().getId());
        assertTrue(getModel().tryGetUnitById(9).isPresent());
        assertTrue(getModel().tryGetUnitById(6).isEmpty());
        assertTrue(getModel().tryGetUnitById(8).isEmpty());
    }

    public void verifyRepairOverview(String expected) {
        assertEquals(expected, getLabeledById("shipRepair-0").getText());
    }

    public void verifyAttackerDamaged() {
        Ship ship = (Ship) getModel().getUnitById(0);
        assertTrue(ship.isDamaged());
        assertEquals(3,
                ship.getRepair().orElseThrow().getRemainingOwnerUpdates());
        assertEquals(0, ship.getActionPoints());
        assertEquals(0, getModel().getUnitById(6).getActionPoints());
        assertTrue(ship.getCargo().getSlotByIndex(0).getGoods().isEmpty());
        assertEquals(9, ship.getCargo().getSlotByIndex(1).getUnit()
                .orElseThrow().getId());
        assertTrue(getModel().tryGetUnitById(9).isPresent());
    }
}
