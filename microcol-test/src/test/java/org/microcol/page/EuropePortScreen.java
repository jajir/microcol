package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.microcol.gui.screen.europe.EuropeButtonsPanel;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Player;
import org.microcol.model.unit.UnitGalleon;
import org.microcol.model.unit.UnitWithCargo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.control.Button;
import javafx.scene.control.Labeled;
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
        return of(context, "Europe");
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
        final String cssClass = ".label-title";
        final List<Labeled> list = getListOfNodes(cssClass);
        final Labeled labeled = list.get(0);
        logger.info("Colony name: " + labeled.getText());
        assertTrue(labeled.getText().contains(expectedNamePart),
                String.format(
                        "Text '%s' should appear in Europe port name '%s'",
                        expectedNamePart, labeled.getText()));
    }

    public void verifyGold(final int expectedAmountOfGold) {
        final Labeled labeledGold = getLabeledById("labelGold");
        String gold = labeledGold.getText();
        gold = gold.substring(gold.lastIndexOf(" ") + 1);
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
        }).get(5, java.util.concurrent.TimeUnit.SECONDS);
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

    private List<UnitWithCargo> getShipsInPort() {
        final Player human = getModel().getPlayers().stream()
                .filter(Player::isHuman).findAny()
                .orElseThrow(() -> new IllegalStateException(
                        "There is no human player in game model."));
        return getModel().getEurope().getPort().getShipsInPort(human);
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

    public void verifyShip(int id, org.microcol.model.UnitType type,
            int holds) {
        UnitWithCargo ship = (UnitWithCargo) getModel().getUnitById(id);
        assertSame(type, ship.getType());
        assertSame(getModel().getPlayerByName("Dutch"), ship.getOwner());
        assertEquals(holds, ship.getCargo().getSlots().size());
        javafx.scene.control.ToggleButton button = pageComponentPanelDock
                .getUnitsInPort().stream()
                .filter(node -> node.getUserData() == ship).findFirst()
                .orElseThrow();
        assertTrue(button.isVisible());
        assertFalse(button.getBackground().getImages().isEmpty());
        assertFalse(
                button.getBackground().getImages().get(0).getImage().isError());
    }

    public void verifyAvailableHoldCount(int holds) {
        org.microcol.gui.image.ImageProvider images = getContext()
                .getClassFromGuice(org.microcol.gui.image.ImageProvider.class);
        List<javafx.scene.layout.StackPane> crates = pageComponentPanelDock
                .getListOfCrates();
        assertEquals(6, crates.size());
        for (int index = 0; index < crates.size(); index++) {
            javafx.scene.image.ImageView crate = getNodeFinder()
                    .from(crates.get(index)).lookup(".crate")
                    .queryAs(javafx.scene.image.ImageView.class);
            assertSame(images.getImage(index < holds
                    ? org.microcol.gui.image.ImageProvider.IMG_CRATE_OPEN
                    : org.microcol.gui.image.ImageProvider.IMG_CRATE_CLOSED),
                    crate.getImage());
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
        getRobot()
                .drag(getListOfGoodsToBuy().get(indexOfGoods),
                        MouseButton.PRIMARY)
                .dropTo(pageComponentPanelDock.getListOfCrates()
                        .get(indexOfCargoSlot))
                .release(MouseButton.PRIMARY);
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
        getRobot()
                .drag(pageComponentPanelDock.getListOfCrates()
                        .get(indexOfCargoSlot), MouseButton.PRIMARY)
                .dropTo(getListOfGoodsToBuy().get(indexOfGoods))
                .release(MouseButton.PRIMARY);
    }

    public DialogChooseGoodsAmount moveCargoWithPressedControl(final int source,
            final int target) {
        getRobot().press(getGoodsAmountModifiers());
        getRobot()
                .drag(pageComponentPanelDock.getListOfCrates().get(source),
                        MouseButton.PRIMARY)
                .dropTo(pageComponentPanelDock.getListOfCrates().get(target))
                .release(MouseButton.PRIMARY);
        getRobot().release(getGoodsAmountModifiers());
        return DialogChooseGoodsAmount.of(getContext());
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
