package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;
import org.microcol.model.Player;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.testfx.util.WaitForAsyncUtils;
import javafx.scene.control.Button;

/** Confirmed ship prices and persistent purchase history. */
public final class ShipPurchasePage extends AbstractScreen {
    private static final String REJECTION = "This purchase is no longer available. Reopen the offer on your turn while you have access to Europe.";

    public ShipPurchasePage(TestContext context) {
        super(context);
    }

    public void confirmCaravel(boolean duplicate) {
        new ExpertPurchasePage(getContext()).revealSpecialist("CARAVEL");
        Button button = getButtonById("buy-unit-CARAVEL");
        getRobot().clickOn(button);
        WaitForAsyncUtils.waitForFxEvents();
        if (duplicate) {
            getRobot().interact(button::fire);
            WaitForAsyncUtils.waitForFxEvents();
        }
    }

    public void verifyQuote(String type, int price, boolean available) {
        assertEquals("Buy for " + price,
                getButtonById("buy-unit-" + type).getText());
        assertEquals(!available,
                getButtonById("buy-unit-" + type).isDisabled());
        assertEquals(available ? "" : "Not enough gold.",
                getLabeledById("buy-unit-reason-" + type).getText());
    }

    public void verifyHistory(int count, int gold, int price) {
        Player player = getModel().getPlayerByName("Dutch");
        assertEquals(count, player.save().getUnitPurchaseCounts()
                .getOrDefault("CARAVEL", 0));
        assertEquals(gold, player.getGold());
        assertEquals(price, player.getUnitPurchasePrice(UnitType.CARAVEL));
        assertEquals(2000, player.getUnitPurchasePrice(UnitType.MERCHANTMAN));
        assertEquals(2200,
                player.getUnitPurchasePrice(UnitType.MASTER_BLACKSMITH));
        assertEquals(count, player.getAllUnits().stream()
                .filter(unit -> unit.getType() == UnitType.CARAVEL).count());
    }

    public void verifyShip(int id) {
        Unit ship = getModel().getUnitById(id);
        assertSame(UnitType.CARAVEL, ship.getType());
        assertTrue(ship.isAtEuropePort());
        assertSame(getModel().getPlayerByName("Dutch"), ship.getOwner());
    }

    public void verifyStaleWarning() {
        assertTrue(
                getNodeFinder().lookup(REJECTION).queryLabeled().isVisible());
    }

    public void closeStaleWarning() {
        javafx.scene.Parent root = getNodeFinder().lookup(REJECTION)
                .queryLabeled().getScene().getRoot();
        getRobot().clickOn(
                getNodeFinder().from(root).lookup("#buttonOk").queryButton());
        WaitForAsyncUtils.waitForFxEvents();
    }
}
