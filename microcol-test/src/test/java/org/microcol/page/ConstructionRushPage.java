package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;
import org.microcol.model.*;

/** Price confirmation and rejection through the actual construction dialog. */
public final class ConstructionRushPage extends AbstractScreen {
    public ConstructionRushPage(TestContext context) {
        super(context);
    }

    private Colony colony() {
        return getModel().getColonyById(0).orElseThrow();
    }

    public void setToolAsk(int price) {
        getRobot().interact(() -> getModel().getEurope()
                .getGoodsTradeForType(GoodsType.TOOLS).setBuyPrice(price));
    }

    public void setGold(int gold) {
        getRobot().interact(() -> colony().getOwner().setGold(gold));
    }

    public void addHammer() {
        getRobot().interact(() -> colony().getWarehouse()
                .addGoods(Goods.of(GoodsType.HAMMERS, 1)));
    }

    public void addTools(int amount) {
        getRobot().interact(() -> colony().getWarehouse()
                .addGoods(Goods.of(GoodsType.TOOLS, amount)));
    }

    public void open() {
        clickOnButtonWithId("constructionRush");
    }

    public void confirm() {
        clickOnButtonWithId("constructionRushConfirm");
    }

    public void cancel() {
        clickOnButtonWithId("constructionRushCancel");
    }

    public void verifyQuote(String project, int hammersUsed, int toolsUsed,
            int missingHammers, int missingTools, int gold, int after,
            boolean enabled) {
        assertEquals("Complete: " + project + "\nUse from colony: "
                + hammersUsed + " hammers, " + toolsUsed
                + " tools.\nBuy for this project: " + missingHammers
                + " hammers, " + missingTools + " tools.\nTotal including tax: "
                + gold + " gold.\nTreasury after purchase: " + after + " gold.",
                getLabeledById("constructionRushDetail").getText());
        assertEquals(!enabled,
                getButtonById("constructionRushConfirm").isDisabled());
    }

    /** No offer exists: the dialog explains why and cannot be confirmed. */
    public void verifyUnavailable(String expected) {
        assertEquals(expected,
                getLabeledById("constructionRushDetail").getText());
        assertTrue(getButtonById("constructionRushConfirm").isDisabled());
    }

    public void verifyFeedback(String expected, boolean enabled) {
        assertTrue(getLabeledById("constructionRushFeedback").getText()
                .contains(expected));
        assertEquals(!enabled,
                getButtonById("constructionRushConfirm").isDisabled());
    }

    public void verifyStocks(int gold, int hammers, int tools) {
        assertEquals(gold, colony().getOwner().getGold());
        assertEquals(hammers, colony().getWarehouse()
                .getGoods(GoodsType.HAMMERS).getAmount());
        assertEquals(tools,
                colony().getWarehouse().getGoods(GoodsType.TOOLS).getAmount());
    }
}
