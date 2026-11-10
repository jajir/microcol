package org.akropia.page;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.akropia.gui.screen.europe.EuropeButtonsPanel;
import org.akropia.gui.screen.game.components.StatusBarView;
import org.akropia.model.EuropeanNation;
import org.akropia.model.Goods;
import org.akropia.model.GoodsTrade;
import org.akropia.model.GoodsType;
import org.akropia.model.Player;
import org.akropia.model.unit.UnitGalleon;
import org.akropia.model.unit.UnitWithCargo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;

/**
 * Object allows to test one unit shown in right panel.
 */
public class EuropePortScreen extends AbstractScreen {

    private final Logger logger = LoggerFactory
            .getLogger(EuropePortScreen.class);

    private final PageComponentPanelDock pageComponentPanelDock;

    public static EuropePortScreen of(final TestContext context) {
        return of(context, "Home port");
    }

    public static EuropePortScreen of(final TestContext context,
            final String expectedName) {
        return new EuropePortScreen(context, expectedName);
    }

    private EuropePortScreen(final TestContext context,
            final String expectedName) {
        super(context);
        pageComponentPanelDock = PageComponentPanelDock.of(context);
        verifyEuropePortName(expectedName);
    }

    public void verifyEuropePortName(final String expectedNamePart) {
        final String cssClass = ".screen-toolbar .screen-title";
        final List<Labeled> list = getListOfNodes(cssClass);
        final Labeled labeled = list.get(0);
        logger.info("Colony name: " + labeled.getText());
        assertTrue(labeled.getText().contains(expectedNamePart),
                String.format(
                        "Text '%s' should appear in Home port port name '%s'",
                        expectedNamePart, labeled.getText()));
    }

    public void verifyGold(final int expectedAmountOfGold) {
        final Labeled labeledGold = getLabeledById("labelGold");
        final String gold = labeledGold.getText().replace("\u00a0", "");
        final int realGold = Integer.valueOf(gold);
        assertEquals(expectedAmountOfGold, realGold,
                String.format("Expected gold was '%s' but really is '%s'",
                        expectedAmountOfGold, realGold));
    }

    /**
     * Simulate another action buying goods into the first ship's hold while a
     * purchase choice is open.
     */
    public void competingPurchase(final int cargoSlotIndex,
            final GoodsType goodsType, final int amount) throws Exception {
        WaitForAsyncUtils.asyncFx(() -> {
            getShipsInPort().get(0).getCargo().getSlotByIndex(cargoSlotIndex)
                    .buyAndStoreFromEuropePort(Goods.of(goodsType, amount));
            // The harbor shows the changed hold, as after any other action.
            getContext()
                    .getClassFromGuice(
                            org.akropia.gui.screen.europe.EuropePanel.class)
                    .repaint();
        }).get(5, java.util.concurrent.TimeUnit.SECONDS);
        // The treasury label follows the gold event on the next FX pulse.
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void verifyGoodsTransferChangedWarning(final String expectedText) {
        final List<javafx.scene.Node> labels = getListOfNodes(".label");
        assertTrue(
                labels.stream().filter(node -> node instanceof Labeled)
                        .anyMatch(node -> expectedText
                                .equals(((Labeled) node).getText())),
                String.format("Warning '%s' was not shown", expectedText));
    }

    public void closeGoodsTransferWarning() {
        clickOnButtonWithId("buttonOk");
    }

    public void verifyEuropeGoldChangedWarning(final String expectedText) {
        final List<javafx.scene.Node> labels = getListOfNodes(".label");
        assertTrue(
                labels.stream().filter(node -> node instanceof Labeled)
                        .anyMatch(node -> expectedText
                                .equals(((Labeled) node).getText())),
                String.format("Warning '%s' was not shown", expectedText));
    }

    public void verifyEuropePriceChangedWarning(final String expectedText) {
        final List<javafx.scene.Node> labels = getListOfNodes(".label");
        assertTrue(
                labels.stream().filter(node -> node instanceof Labeled)
                        .anyMatch(node -> expectedText
                                .equals(((Labeled) node).getText())),
                String.format("Warning '%s' was not shown", expectedText));
    }

    /**
     * @param goodsType    required tradable good shown in the port
     * @param expectedText expected bid and ask, for example "3/5"
     */
    public void verifyGoodsBidAndAsk(final GoodsType goodsType,
            final String expectedText) {
        final Labeled price = getLabeledById("europePrice" + goodsType.name());
        assertEquals(expectedText, price.getText().split("\n")[0],
                String.format("Bid and ask of %s", goodsType.name()));
    }

    /**
     * Model-backed check of a national market that this port does not show.
     *
     * @param nation       European nation whose market is compared
     * @param goodsType    tradable good in that national market
     * @param expectedText expected bid and ask, for example "2/4"
     */
    public void verifyNationalGoodsBidAndAsk(final EuropeanNation nation,
            final GoodsType goodsType, final String expectedText) {
        final GoodsTrade trade = getModel().getEurope()
                .getGoodsTradeForType(goodsType, nation);
        final String bidAndAsk = trade.getSellPrice() + "/"
                + trade.getBuyPrice();
        assertEquals(expectedText, bidAndAsk, String.format(
                "Bid and ask of %s in %s market", goodsType.name(), nation));
    }

    private Player getHuman() {
        return getModel().getPlayers().stream().filter(Player::isHuman)
                .findAny().orElseThrow(() -> new IllegalStateException(
                        "There is no human player in game model."));
    }

    private List<UnitWithCargo> getShipsInPort() {
        return getModel().getEurope().getPort().getShipsInPort(getHuman());
    }

    public void verifyGoodsInShip(final int cargoSlotIndex,
            final Goods expectedGoods) {
        final List<UnitWithCargo> ships = getShipsInPort();
        assertEquals(1, ships.size());
        new ShipWrapper((UnitGalleon) ships.get(0))
                .verifyCargoGoods(cargoSlotIndex, expectedGoods);
    }

    public void verifyCargoSlotEmpty(final int shipIndex,
            final int cargoSlotIndex) {
        new ShipWrapper(getShipsInPort().get(shipIndex))
                .verifyCargoSlotEmpty(cargoSlotIndex);
    }

    public void verifyShip(int id, org.akropia.model.UnitType type, int holds) {
        UnitWithCargo ship = (UnitWithCargo) getModel().getUnitById(id);
        assertSame(type, ship.getType());
        assertSame(getModel().getPlayerByName("Dutch"), ship.getOwner());
        assertEquals(holds, ship.getCargo().getSlots().size());
        javafx.scene.control.ToggleButton button = pageComponentPanelDock
                .getUnitsInPort().stream()
                .filter(node -> node.getUserData() == ship).findFirst()
                .orElseThrow();
        assertTrue(button.isVisible());
        assertTrue(button.getGraphic() instanceof javafx.scene.image.ImageView);
        javafx.scene.image.ImageView icon = (javafx.scene.image.ImageView) button
                .getGraphic();
        assertFalse(icon.getImage().isError());
        assertEquals(45, icon.getFitWidth(), 0.01);
        assertTrue(button.getStyleClass().contains("harbor-unit"));
        assertEquals(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY,
                button.getContentDisplay());
    }

    public void verifyAvailableHoldCount(int holds) {
        List<javafx.scene.layout.StackPane> crates = pageComponentPanelDock
                .getListOfCrates();
        assertEquals(6, crates.size());
        for (int index = 0; index < crates.size(); index++) {
            final javafx.scene.shape.SVGPath face = getNodeFinder()
                    .from(crates.get(index)).lookup(".harbor-crate-face")
                    .queryAs(javafx.scene.shape.SVGPath.class);
            assertEquals(javafx.scene.paint.Color.web("#C99638"),
                    face.getFill());
            assertEquals(javafx.scene.paint.Color.web("#25231F"),
                    face.getStroke());
            assertTrue(face.getBoundsInLocal().getWidth() <= 45);
            assertTrue(face.getBoundsInLocal().getHeight() <= 45);
            assertEquals(index >= holds,
                    crates.get(index).getPseudoClassStates().contains(
                            javafx.css.PseudoClass.getPseudoClass("closed")));
        }
    }

    public BuyUnitScreen openBuyUnitScreen() {
        final Button buttonNextTurn = getButtonById(
                EuropeButtonsPanel.BUY_BUTTON_ID);
        getRobot().clickOn(buttonNextTurn);
        WaitForAsyncUtils.waitForFxEvents();
        return BuyUnitScreen.of(getContext());
    }

    public EuropePortScreen selectUnitFromPort(final int unitIndex) {
        showCargoAndGoods();
        pageComponentPanelDock.selectUnitFromPort(unitIndex);
        return this;
    }

    public void verifyNumberOfShipsInPort(final int expectedCount) {
        pageComponentPanelDock.verifyNumberOfShipsInPort(expectedCount);
    }

    public ScreenMarketBuy buyGoodsAndMoveItToCargoSlotWithPressedControl(
            final int indexOfGoods, final int indexOfCargoSlot) {
        getRobot().press(getGoodsAmountModifiers());
        buyGoodsAndMoveItToCargoSlot(indexOfGoods, indexOfCargoSlot);
        getRobot().release(getGoodsAmountModifiers());
        WaitForAsyncUtils.waitForFxEvents();
        return ScreenMarketBuy.of(getContext());
    }

    public void buyGoodsAndMoveItToCargoSlot(final int indexOfGoods,
            final int indexOfCargoSlot) {
        dragMarketGoodsToHold(indexOfGoods, indexOfCargoSlot);
        getRobot().drop().release(MouseButton.PRIMARY);
    }

    /**
     * A normal drag from the market to a hold opens the purchase choice.
     */
    public ScreenMarketBuy openPurchaseChoice(final int indexOfGoods,
            final int indexOfCargoSlot) {
        buyGoodsAndMoveItToCargoSlot(indexOfGoods, indexOfCargoSlot);
        WaitForAsyncUtils.waitForFxEvents();
        return ScreenMarketBuy.of(getContext());
    }

    /**
     * Hold the platform quick-buy key (Command on macOS, Ctrl elsewhere) for
     * the whole drag, including the drop.
     */
    public void quickBuyGoods(final int indexOfGoods,
            final int indexOfCargoSlot) {
        getRobot().press(getQuickBuyModifier());
        buyGoodsAndMoveItToCargoSlot(indexOfGoods, indexOfCargoSlot);
        getRobot().release(getQuickBuyModifier());
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Start a plain drag and press the quick-buy key only over the hold, before
     * the drop.
     */
    public void quickBuyWithKeyPressedDuringDrag(final int indexOfGoods,
            final int indexOfCargoSlot) {
        showCargoAndGoods();
        dragMarketGoodsToHold(indexOfGoods, indexOfCargoSlot);
        getRobot().press(getQuickBuyModifier());
        // A small move lets the platform drag report the new modifier.
        getRobot().moveBy(2, 0);
        getRobot().drop();
        getRobot().release(getQuickBuyModifier());
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Start the drag with the quick-buy key held and release it over the hold,
     * before the drop; the drop opens the purchase choice.
     */
    public ScreenMarketBuy openPurchaseChoiceAfterReleasingQuickBuyKey(
            final int indexOfGoods, final int indexOfCargoSlot) {
        showCargoAndGoods();
        getRobot().press(getQuickBuyModifier());
        dragMarketGoodsToHold(indexOfGoods, indexOfCargoSlot);
        getRobot().release(getQuickBuyModifier());
        // A small move lets the platform drag report the released modifier.
        getRobot().moveBy(2, 0);
        getRobot().drop();
        WaitForAsyncUtils.waitForFxEvents();
        return ScreenMarketBuy.of(getContext());
    }

    /**
     * Simulate another action changing the treasury while a purchase choice is
     * open, or prepare it before the purchase.
     */
    public void changeTreasury(final int gold) throws Exception {
        WaitForAsyncUtils.asyncFx(() -> getHuman().setGold(gold)).get(5,
                java.util.concurrent.TimeUnit.SECONDS);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Simulate the Crown changing the tax while a purchase choice is open, or
     * prepare it before the purchase.
     */
    public void changeCrownTax(final int percentage) throws Exception {
        WaitForAsyncUtils
                .asyncFx(() -> getModel().getKingOf(getHuman()).orElseThrow()
                        .setKingsTaxPercentage(percentage))
                .get(5, java.util.concurrent.TimeUnit.SECONDS);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * @param expectedText exact status bar text after a purchase drop
     */
    public void verifyStatusMessage(final String expectedText) {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(expectedText,
                getLabeledById(StatusBarView.STATUS_BAR_LABEL_ID).getText());
    }

    /** No purchase choice opened; the harbor stays shown. */
    public void verifyNoPurchaseChoice() {
        WaitForAsyncUtils.waitForFxEvents();
        assertFalse(isCssIdShown("marketSlider"),
                "The purchase choice is not shown");
    }

    public ScreenMarketSell sellGoodsWithPressedControl(
            final int indexOfCargoSlot, final int indexOfGoods) {
        getRobot().press(getGoodsAmountModifiers());
        sellGoods(indexOfCargoSlot, indexOfGoods);
        getRobot().release(getGoodsAmountModifiers());
        WaitForAsyncUtils.waitForFxEvents();
        return ScreenMarketSell.of(getContext());
    }

    public void sellGoods(final int indexOfCargoSlot, final int indexOfGoods) {
        final javafx.scene.Node crate = pageComponentPanelDock.getListOfCrates()
                .get(indexOfCargoSlot);
        final javafx.scene.Node goods = getGoodsImage(indexOfGoods);
        revealGameContent(crate);
        getRobot().drag(crate, MouseButton.PRIMARY);
        revealGameContent(goods);
        getRobot().dropTo(goods).release(MouseButton.PRIMARY);
    }

    /** A plain drag between holds of the selected ship moves all goods. */
    public void moveCargo(final int source, final int target) {
        showCargoAndGoods();
        getRobot()
                .drag(pageComponentPanelDock.getListOfCrates().get(source),
                        MouseButton.PRIMARY)
                .dropTo(pageComponentPanelDock.getListOfCrates().get(target))
                .release(MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public DialogChooseGoodsAmount moveCargoWithPressedControl(final int source,
            final int target) {
        showCargoAndGoods();
        getRobot().press(getGoodsAmountModifiers());
        getRobot()
                .drag(pageComponentPanelDock.getListOfCrates().get(source),
                        MouseButton.PRIMARY)
                .dropTo(pageComponentPanelDock.getListOfCrates().get(target))
                .release(MouseButton.PRIMARY);
        getRobot().release(getGoodsAmountModifiers());
        return DialogChooseGoodsAmount.of(getContext());
    }

    private void dragMarketGoodsToHold(final int goodsIndex,
            final int cargoIndex) {
        final javafx.scene.Node goods = getGoodsImage(goodsIndex);
        final javafx.scene.Node crate = pageComponentPanelDock.getListOfCrates()
                .get(cargoIndex);
        revealGameContent(goods);
        getRobot().drag(goods, MouseButton.PRIMARY);
        // Narrow windows cannot show the last market good and first hold
        // together.
        revealGameContent(crate);
        getRobot().moveTo(crate);
    }

    private void showCargoAndGoods() {
        final ScrollPane scroll = getNodeFinder().lookup(".screen-scroll")
                .queryAs(ScrollPane.class);
        getRobot().interact(() -> scroll.setVvalue(scroll.getVmax()));
        WaitForAsyncUtils.waitForFxEvents();
    }

    private javafx.scene.Node getGoodsImage(final int index) {
        final javafx.scene.layout.Pane image = (javafx.scene.layout.Pane) getListOfGoodsToBuy()
                .get(index).getChildren().get(0);
        return image.getChildren().get(0);
    }

    public List<VBox> getListOfGoodsToBuy() {
        return getListOfNodes(".panelGoods");
    }

    public GamePage close() {
        final Button buttonNextTurn = getButtonById(
                EuropeButtonsPanel.CLOSE_BUTTON_ID);
        getRobot().clickOn(buttonNextTurn);
        return GamePage.of(getContext());
    }

}
