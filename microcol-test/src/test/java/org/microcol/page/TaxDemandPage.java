package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.microcol.gui.FileSelectingService;
import org.microcol.gui.screen.game.components.ButtonsGamePanel;
import org.microcol.model.Colony;
import org.microcol.model.Difficulty;
import org.microcol.model.GoodsType;
import org.microcol.model.Player;
import org.microcol.model.TaxState;
import org.testfx.util.WaitForAsyncUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javafx.scene.control.ComboBox;

/**
 * The Crown's tax demand as the player meets it: a derived scenario one update
 * before the demand, the actual dialog, the Europe reopen action and the
 * observable outcome of each answer.
 */
public final class TaxDemandPage extends AbstractScreen {
    public static final File CORE_SOURCE = new File(
            "src/test/scenarios/F64-la-salle-stockade.microcol");

    /** Describes how a source scenario is changed into scenario F70. */
    public static final class Fixture {
        private final File source;
        private final String name;
        private boolean due;
        private Map<GoodsType, Integer> warehouse;
        private Map<GoodsType, Integer> arrears = Map.of();
        private List<GoodsType> exports;
        private boolean independence;
        private boolean expandedWarehouse;
        private Integer gold;

        public Fixture(final File source, final String name) {
            this.source = source;
            this.name = name;
        }

        /** The next update of the Dutch falls due for a demand. */
        public Fixture due() {
            due = true;
            return this;
        }

        /** Only the given tradable stock remains in the first colony. */
        public Fixture warehouse(final Map<GoodsType, Integer> stock) {
            warehouse = new LinkedHashMap<>(stock);
            return this;
        }

        /**
         * The Dutch already owe the given arrears, so those goods are closed.
         */
        public Fixture arrears(final Map<GoodsType, Integer> owed) {
            arrears = new LinkedHashMap<>(owed);
            return this;
        }

        public Fixture exports(final List<GoodsType> selected) {
            exports = List.copyOf(selected);
            return this;
        }

        public Fixture independence() {
            independence = true;
            return this;
        }

        /** The first colony's warehouse holds 300 of a commodity. */
        public Fixture expandedWarehouse() {
            expandedWarehouse = true;
            return this;
        }

        /** The Dutch treasury starts with exactly this much gold. */
        public Fixture gold(final int amount) {
            gold = amount;
            return this;
        }
    }

    public TaxDemandPage(final TestContext context) {
        super(context);
    }

    private Player dutch() {
        return getModel().getPlayerByName("Dutch");
    }

    private Colony colony() {
        return getModel().getColonyById(0).orElseThrow();
    }

    /** Write the derived scenario and make it the next game to load. */
    public void prepare(final Fixture fixture) throws Exception {
        final JsonObject saved = new Gson().fromJson(
                Files.readString(fixture.source.toPath()), JsonObject.class);
        final JsonObject model = saved.getAsJsonObject("model");
        final JsonObject player = model.getAsJsonObject("gameManager")
                .getAsJsonArray("players").get(0).getAsJsonObject();
        final JsonObject tax = player.getAsJsonObject("tax");
        if (fixture.due) {
            final Difficulty difficulty = Difficulty
                    .valueOf(model.get("difficulty").getAsString());
            tax.addProperty("activated", true);
            tax.addProperty("updates", TaxState.interval(difficulty) - 1);
        }
        final JsonObject owed = new JsonObject();
        fixture.arrears.forEach(
                (type, amount) -> owed.addProperty(type.name(), amount));
        tax.add("arrears", owed);
        if (fixture.independence) {
            player.addProperty("declaredIndependence", true);
        }
        if (fixture.gold != null) {
            player.addProperty("gold", fixture.gold);
        }
        final JsonObject colony = model.getAsJsonArray("colonies").get(0)
                .getAsJsonObject();
        if (fixture.expandedWarehouse) {
            for (final var building : colony.getAsJsonArray("constructions")) {
                final JsonObject construction = building.getAsJsonObject();
                if ("WAREHOUSE"
                        .equals(construction.get("type").getAsString())) {
                    construction.addProperty("type", "WAREHOUSE_EXPANSION");
                }
            }
        }
        if (fixture.warehouse != null) {
            final JsonObject stock = colony.getAsJsonObject("colonyWarehouse");
            for (final GoodsType type : GoodsType.BUYABLE_GOOD_TYPES) {
                stock.addProperty(type.name(),
                        fixture.warehouse.getOrDefault(type, 0));
            }
        }
        if (fixture.exports != null) {
            final JsonArray selected = new JsonArray();
            fixture.exports.forEach(type -> selected.add(type.name()));
            colony.add("exportGoods", selected);
        }
        final Path directory = Path.of("target", "f70");
        Files.createDirectories(directory);
        final File file = directory.resolve(fixture.name + ".microcol")
                .toFile();
        Files.writeString(file.toPath(), saved.toString());
        org.mockito.Mockito
                .when(getContext().getClassFromGuice(FileSelectingService.class)
                        .loadFile(org.mockito.ArgumentMatchers.any(File.class)))
                .thenReturn(file);
    }

    // --- player gestures ---------------------------------------------------

    /** End the turn; the Crown's demand arrives with the next update. */
    public void startUpdate() throws Exception {
        clickOnButtonWithId(ButtonsGamePanel.BUTTON_NEXT_TURN_ID);
        waitForDemand();
    }

    public void waitForDemand() throws Exception {
        WaitForAsyncUtils.waitFor(30, TimeUnit.SECONDS,
                () -> getModel().isAwaitingTaxDecision() && getNodeFinder()
                        .lookup("#taxDemandDialog").tryQuery().isPresent());
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void accept() {
        clickOnButtonWithId("taxDemandAccept");
    }

    public void protest() {
        clickOnButtonWithId("taxDemandProtest");
    }

    /** Close the window without answering. */
    public void defer() {
        clickOnButtonWithId("taxDemandLater");
    }

    public void escape() {
        getRobot().push(javafx.scene.input.KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void openEurope() {
        clickOnButtonWithId(ButtonsGamePanel.BUTTON_EUROPE_ID);
    }

    /** Use the discoverable Europe action to open the deferred demand again. */
    public void reopen() throws Exception {
        clickOnButtonWithId("taxDemandButtonId");
        waitForDemand();
    }

    public void returnToMap() {
        clickOnButtonWithId("closeButtonId");
    }

    /** Continue the saved game that was left with the demand unanswered. */
    public void continuePending() throws Exception {
        clickOnButtonWithId("buttonContinue");
        waitForDemand();
    }

    /** Wait for the answered update and leave the Europe or report screen. */
    public void finishUpdate() throws Exception {
        WaitForAsyncUtils.waitFor(30, TimeUnit.SECONDS,
                () -> getModel().isGameRunning()
                        && getModel().getCurrentPlayer().isHuman());
        WaitForAsyncUtils.waitForFxEvents();
        if (getModel().isTurnEventsMessagesEmpty(dutch())) {
            returnToMap();
        } else {
            WaitForAsyncUtils.waitFor(30, TimeUnit.SECONDS,
                    () -> getNodeFinder().lookup(".turn-event").tryQuery()
                            .isPresent());
            ScreenTurnReport.of(getContext()).close();
        }
    }

    /** Another command removes stock while the window is open. */
    public void removeStockElsewhere(final GoodsType type, final int amount) {
        getRobot().interact(() -> colony().getWarehouse()
                .removeGoods(org.microcol.model.Goods.of(type, amount)));
    }

    /** The Dutch treasury as the player currently holds it. */
    public int gold() {
        return dutch().getGold();
    }

    /**
     * The exact total the model quotes for a market trade of the Dutch, used as
     * the expected gold change of a real Europe trade.
     */
    public int quoteMarketPrice(final GoodsType type, final int amount,
            final boolean purchase) {
        return dutch().quoteEuropeTrade(
                org.microcol.model.Goods.of(type, amount), purchase).getTotal();
    }

    /** Use the Europe action that pays back taxes of boycotted goods. */
    public void openBackTaxes() throws Exception {
        clickOnButtonWithId("backTaxButtonId");
        WaitForAsyncUtils.waitFor(30, TimeUnit.SECONDS, () -> getNodeFinder()
                .lookup("#backTaxDialog").tryQuery().isPresent());
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Select the boycotted commodity shown by the payment dialog. */
    @SuppressWarnings("unchecked")
    public void chooseBackTaxCommodity(final GoodsType type) {
        final ComboBox<GoodsType> commodity = (ComboBox<GoodsType>) getNodeFinder()
                .lookup("#backTaxCommodity").queryAs(ComboBox.class);
        getRobot().interact(() -> commodity.getSelectionModel().select(type));
        WaitForAsyncUtils.waitForFxEvents();
    }

    public void payBackTax() {
        clickOnButtonWithId("backTaxPay");
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Close the payment dialog without paying. */
    public void cancelBackTax() {
        clickOnButtonWithId("backTaxCancel");
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Another command pays the same commodity while the dialog is open. */
    public void payBackTaxElsewhere(final GoodsType type) {
        getRobot().interact(() -> dutch()
                .payTaxArrears(dutch().quoteTaxArrears(type).orElseThrow()));
    }

    // --- explicit observations --------------------------------------------

    public void verifyDemandDialog(final String rates,
            final String acceptExplain, final String protestDetail,
            final boolean protestEnabled) {
        assertEquals(rates, getLabeledById("taxDemandRates").getText());
        assertEquals(acceptExplain,
                getLabeledById("taxDemandAcceptExplain").getText());
        assertEquals(protestDetail,
                getLabeledById("taxDemandProtestDetail").getText());
        assertFalse(getButtonById("taxDemandAccept").isDisabled());
        assertEquals(!protestEnabled,
                getButtonById("taxDemandProtest").isDisabled());
        assertFalse(getButtonById("taxDemandLater").isDisabled());
    }

    public void verifyFeedback(final String expected) {
        assertEquals(expected, getLabeledById("taxDemandFeedback").getText());
    }

    /** The turn is paused at the demand and nothing has been updated. */
    public void verifyPaused(final int rate, final long libertyBefore) {
        assertTrue(getModel().isAwaitingTaxDecision());
        assertFalse(getModel().isGameRunning());
        assertFalse(getModel().isAwaitingImmigrationChoice());
        assertTrue(dutch().getTaxDemand().isPresent());
        assertEquals(rate, dutch().getTaxRate());
        assertEquals(libertyBefore, colony().getLiberty());
        assertEquals(0,
                colony().getWarehouse().getGoods(GoodsType.BELL).getAmount());
        assertEquals(1, dutch().save().getTax().getIssued());
    }

    public void verifyDialogOpen(final boolean open) {
        assertEquals(open, isCssIdVisible("taxDemandDialog"));
    }

    public void verifyReopenAction(final boolean visible) {
        assertEquals(visible, getButtonById("taxDemandButtonId").isVisible());
    }

    public void verifyEuropeActionsBlocked() {
        assertTrue(getNodeFinder().lookup("#europeMarketGoods").query()
                .isDisabled());
        assertTrue(getButtonById("recruitButtonId").isDisabled());
        assertTrue(getButtonById("buyButtonId").isDisabled());
    }

    public void verifyTurnCommandsUnavailable() {
        assertTrue(getButtonById(ButtonsGamePanel.BUTTON_NEXT_TURN_ID)
                .isDisabled());
        assertFalse(
                getButtonById(ButtonsGamePanel.BUTTON_EUROPE_ID).isDisabled());
        assertFalse(getButtonById("exit").isDisabled());
    }

    public void verifyStock(final GoodsType type, final int amount) {
        assertEquals(amount,
                colony().getWarehouse().getGoods(type).getAmount());
    }

    /** The rate and the exact set of owed commodities. */
    public void verifyTax(final int rate, final Map<GoodsType, Integer> owed) {
        assertEquals(rate, dutch().getTaxRate());
        assertEquals(owed, dutch().getTaxArrears());
        assertTrue(dutch().getTaxDemand().isEmpty());
    }

    /**
     * The colony holds the given protest liberty plus the bells its resumed
     * update produced, and the Congress gained only those produced bells.
     */
    public void verifyResumed(final long protestLiberty,
            final int congressBellsBefore) {
        assertTrue(getModel().isGameRunning());
        assertFalse(getModel().isAwaitingTaxDecision());
        final int produced = colony().getWarehouse().getGoods(GoodsType.BELL)
                .getAmount();
        assertEquals(protestLiberty + produced, colony().getLiberty());
        assertEquals(congressBellsBefore + produced,
                dutch().getCongressBells());
    }

    /** Earned arrivals and cross progress before and after the demand. */
    public void verifyEarnedArrivals(final int earned, final int crosses) {
        assertEquals(earned,
                dutch().save().getRecruitment().getEarnedImmigrants());
        assertEquals(crosses, dutch().save().getRecruitment().getCrosses());
    }

    /**
     * An ordinary export sold the exported goods down to the retained 100 for
     * exactly the expected treasury, while the boycotted goods were not sold
     * although their stock is above the export threshold.
     */
    public void verifyBoycottedExportSkipped(final GoodsType exported,
            final GoodsType skipped, final int skippedStock,
            final int expectedGold) {
        assertEquals(100,
                colony().getWarehouse().getGoods(exported).getAmount());
        assertEquals(skippedStock,
                colony().getWarehouse().getGoods(skipped).getAmount());
        assertTrue(skippedStock > 100);
        assertEquals(expectedGold, dutch().getGold());
        assertTrue(dutch().isBoycotted(skipped));
    }

    public void verifyBoycottMarked(final GoodsType type,
            final boolean marked) {
        assertEquals(marked, getNodeFinder().lookup("#europeGood" + type.name())
                .query().getStyleClass().contains("boycotted"));
        assertEquals(marked, dutch().isBoycotted(type));
    }

    public void verifyColonyLiberty(final long expected) {
        assertEquals(expected, colony().getLiberty());
    }

    /** The demand waits, and acceptance and protest are both offered. */
    public void verifyDemandOffersProtest() {
        assertTrue(getModel().isAwaitingTaxDecision());
        assertFalse(getButtonById("taxDemandAccept").isDisabled());
        assertFalse(getButtonById("taxDemandProtest").isDisabled());
    }

    public void verifyGold(final int expected) {
        assertEquals(expected, dutch().getGold());
    }

    public void verifyBackTaxButton(final boolean visible,
            final boolean disabled) {
        assertEquals(visible, getButtonById("backTaxButtonId").isVisible());
        assertEquals(disabled, getButtonById("backTaxButtonId").isDisabled());
    }

    public void verifyBackTaxDialogOpen(final boolean open) {
        assertEquals(open, isCssIdVisible("backTaxDialog"));
    }

    /** The exact payment lines and whether the pay action may be used. */
    public void verifyBackTaxDialog(final String arrears, final String restore,
            final String treasury, final String insufficient,
            final boolean payEnabled) {
        assertEquals(arrears, getLabeledById("backTaxArrears").getText());
        assertEquals(restore, getLabeledById("backTaxRestore").getText());
        assertEquals(treasury, getLabeledById("backTaxTreasury").getText());
        assertEquals(insufficient,
                getLabeledById("backTaxInsufficient").getText());
        assertEquals(!payEnabled, getButtonById("backTaxPay").isDisabled());
        assertFalse(getButtonById("backTaxCancel").isDisabled());
    }

    public void verifyBackTaxFeedback(final String expected) {
        assertEquals(expected, getLabeledById("backTaxFeedback").getText());
    }
}
