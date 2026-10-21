package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.microcol.model.EuropeanNation;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.Goods;
import org.microcol.model.NativeMarketRules;
import org.microcol.model.NativeMarketView;
import org.microcol.model.NativeNation;
import org.microcol.model.NativePurchaseQuote;
import org.microcol.model.NativeSaleQuote;
import org.microcol.model.NativeSettlement;
import org.microcol.model.unit.UnitWithCargo;
import org.testfx.util.WaitForAsyncUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

/**
 * Land introduction, market admission and market inspection of a native
 * settlement (R-0159) through the visit chooser. The initial position is a
 * current-schema fixture derived from the native turns scenario: a Dutch scout,
 * a loaded wagon and a loaded ship stand on legal squares beside one native
 * settlement. Only the fixture setup writes saved data; every action here is a
 * real click or gesture, and the checks read the model without changing it.
 */
public final class NativeMarketPage extends AbstractScreen {

    private static final String CONTENT_ID = "nativeVisitContent";

    private static final String MARKET_CONTACT_ID = "nativeMarketContact";

    private static final String MARKET_ADMISSION_ID = "nativeMarketAdmission";

    private static final String INTRODUCE_BUTTON_ID = "nativeMarketIntroduce";

    private static final String OPEN_MARKET_BUTTON_ID = "nativeMarketOpen";

    private static final String EXPEDITION_ID = "nativeMarketExpedition";

    private static final String OFFER_ID_PREFIX = "nativeMarketOffer_";

    private static final String WANTED_ID = "nativeMarketWanted";

    private static final String TREASURY_ID = "nativeMarketTreasury";

    private static final String SALE_PREVIEW_ID = "nativeSalePreview";

    private static final String SALE_HAGGLE_STATE_ID = "nativeSaleHaggleState";

    private static final String SALE_NOTICE_ID = "nativeSaleNotice";

    private static final String SALE_SELL_BUTTON_ID = "nativeSaleSell";

    private static final String SALE_GIFT_BUTTON_ID = "nativeSaleGift";

    private static final String SALE_HAGGLE_BUTTON_ID = "nativeSaleHaggle";

    private static final String CONFIRM_CONTENT_ID = "nativeSaleContent";

    private static final String CONFIRM_LINE_ID = "nativeSaleLine";

    private static final String CONFIRM_BUTTON_ID = "nativeSaleConfirm";

    private static final String CONFIRM_BACK_ID = "nativeSaleBack";

    private static final String SALE_QUANTITY_ID = "nativeSaleQuantity";

    private static final String SALE_QUANTITY_VALUE_ID = "nativeSaleQuantityValue";

    private static final String PURCHASE_HEADER_ID = "nativePurchaseHeader";

    private static final String PURCHASE_GOODS_ID = "nativePurchaseGoods";

    private static final String PURCHASE_QUANTITY_ID = "nativePurchaseQuantity";

    private static final String PURCHASE_QUANTITY_VALUE_ID = "nativePurchaseQuantityValue";

    private static final String PURCHASE_PREVIEW_ID = "nativePurchasePreview";

    private static final String PURCHASE_HAGGLE_STATE_ID = "nativePurchaseHaggleState";

    private static final String PURCHASE_BUY_BUTTON_ID = "nativePurchaseBuy";

    private static final String PURCHASE_HAGGLE_BUTTON_ID = "nativePurchaseHaggle";

    private static final String PURCHASE_DECLINE_BUTTON_ID = "nativePurchaseDecline";

    private static final String PURCHASE_DECLINED_EN = "The purchase was declined. Nothing was bought.";

    private static final String PURCHASE_CONFIRM_CONTENT_ID = "nativePurchaseContent";

    private static final String PURCHASE_CONFIRM_LINE_ID = "nativePurchaseLine";

    private static final String PURCHASE_CONFIRM_BUTTON_ID = "nativePurchaseConfirm";

    private static final String PURCHASE_CONFIRM_BACK_ID = "nativePurchaseBack";

    private static final String PURCHASE_HAGGLE_ACCEPTED_EN = "Discount: the natives accepted, so the price is 10% lower.";

    private static final String PURCHASE_HAGGLE_REFUSED_EN = "Discount: the natives refused, so the original price stays.";

    private static final String UNAVAILABLE_SLOT_EN = "Sale slot unavailable: no more goods grow here.";

    /** Enough left-arrow presses to reach the minimum of any piece slider. */
    private static final int SLIDER_TO_MINIMUM_PRESSES = 120;

    private static final String HAGGLE_ACCEPTED_EN = "Haggle: the natives accepted, so the price includes the +10%.";

    private static final String HAGGLE_REFUSED_EN = "Haggle: the natives refused, so the original price stays.";

    private final FogPage map;

    public NativeMarketPage(final TestContext context) {
        super(context);
        map = new FogPage(context);
    }

    /**
     * Load the native turns scenario with a Dutch scout, a loaded wagon and a
     * loaded ship on legal squares beside one native settlement: land units on
     * land, the ship on sea or docked in the Dutch colony. Terrain is kept as
     * saved. The tribal anger of the named nation toward the Dutch is set to
     * uneasy; without a name every nation keeps its saved content anger. Only
     * the saved starting position changes here.
     *
     * @param uneasyNation native nation name to make uneasy, or null for none
     */
    public void loadFixture(final File source, final String uneasyNation,
            final int scoutId, final Location scoutAt, final int wagonId,
            final Location wagonAt, final int shipId, final Location shipAt)
            throws Exception {
        final Gson gson = new Gson();
        final JsonObject game = gson.fromJson(Files.readString(source.toPath()),
                JsonObject.class);
        final JsonObject model = game.getAsJsonObject("model");
        for (final JsonElement element : model
                .getAsJsonArray("nativeTribalAnger")) {
            final JsonObject entry = element.getAsJsonObject();
            if (entry.get("nation").getAsString().equals(uneasyNation)
                    && "DUTCH".equals(entry.get("european").getAsString())) {
                entry.addProperty("value", 30);
            }
        }
        final JsonArray units = model.getAsJsonArray("units");
        units.add(colonist(scoutId, scoutAt));
        units.add(carrier(wagonId, "WAGON", wagonAt));
        units.add(carrier(shipId, "CARAVEL", shipAt));
        final Path fixture = Files.createTempFile("native-market-",
                ".microcol");
        try {
            Files.writeString(fixture, gson.toJson(game));
            new AttackPage(getContext()).replaceGame(fixture.toFile());
        } finally {
            Files.deleteIfExists(fixture);
        }
    }

    private static JsonObject unit(final int id, final String type,
            final Location at) {
        final JsonObject location = new JsonObject();
        location.addProperty("x", at.getX());
        location.addProperty("y", at.getY());
        final JsonObject place = new JsonObject();
        place.add("location", location);
        place.addProperty("orientation", "west");
        final JsonObject action = new JsonObject();
        action.addProperty("type", "noAction");
        final JsonObject empty = new JsonObject();
        empty.add("slots", new JsonArray());
        final JsonObject unit = new JsonObject();
        unit.addProperty("id", id);
        unit.addProperty("type", type);
        unit.addProperty("ownerId", "Dutch");
        unit.add("placeMap", place);
        unit.add("cargo", empty);
        unit.add("action", action);
        unit.addProperty("movementThirds", 3);
        unit.addProperty("tools", 0);
        unit.addProperty("holdingGuns", false);
        unit.addProperty("mounted", false);
        return unit;
    }

    /** The ordinary scout: a mounted colonist without guns, on land. */
    private static JsonObject colonist(final int id, final Location at) {
        final JsonObject unit = unit(id, "COLONIST", at);
        unit.addProperty("workUpdates", 0);
        unit.addProperty("missionary", false);
        unit.addProperty("mounted", true);
        return unit;
    }

    /** A wagon or ship carrying one load of corn, so it holds goods. */
    private static JsonObject carrier(final int id, final String type,
            final Location at) {
        final JsonObject unit = unit(id, type, at);
        unit.add("cargo", cargo());
        return unit;
    }

    private static JsonObject cargo() {
        final JsonObject corn = new JsonObject();
        corn.addProperty("goodsType", "CORN");
        corn.addProperty("amount", 50);
        // Both wagon and caravel hold two slots: the corn, then one empty slot.
        final JsonArray slots = new JsonArray();
        slots.add(corn);
        slots.add(new JsonObject());
        final JsonObject cargo = new JsonObject();
        cargo.add("slots", slots);
        return cargo;
    }

    /** Select the visitor and drag onto the settlement, then release. */
    public void enterSettlement(final Location from, final Location settlement)
            throws Exception {
        map.preview(from, settlement);
        getRobot().release(MouseButton.PRIMARY);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Close the chooser with the keyboard, which changes nothing. */
    public void escape() {
        getRobot().push(KeyCode.ESCAPE);
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Introduce the player's nation by land; the chooser then reopens. */
    public void introduce() {
        getRobot().clickOn(chooserButton(INTRODUCE_BUTTON_ID));
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Open the market for the carrier; the chooser then reopens. */
    public void openMarket() {
        getRobot().clickOn(chooserButton(OPEN_MARKET_BUTTON_ID));
        WaitForAsyncUtils.waitForFxEvents();
    }

    private Button chooserButton(final String id) {
        return getNodeFinder().lookup("#" + CONTENT_ID + " #" + id)
                .queryButton();
    }

    public void captureChooser(final String fileName) throws Exception {
        captureDialog(CONTENT_ID, fileName);
    }

    /** The contact line names whether the tribe knows the nation. */
    public void verifyContact(final String expected) {
        assertEquals(expected, getLabeledById(MARKET_CONTACT_ID).getText());
    }

    /**
     * The admission line names the first failed gate, or that trade is open.
     */
    public void verifyAdmission(final String expected) {
        assertEquals(expected, getLabeledById(MARKET_ADMISSION_ID).getText());
    }

    public void verifyIntroductionOffered(final boolean expected) {
        assertEquals(expected, isCssIdShown(INTRODUCE_BUTTON_ID));
    }

    /**
     * Refused visitors see no stock, wanted goods, treasury or market button.
     */
    public void verifyMarketHidden() {
        assertFalse(isCssIdShown(OPEN_MARKET_BUTTON_ID));
        assertFalse(isCssIdShown(OFFER_ID_PREFIX + 0));
        assertFalse(isCssIdShown(WANTED_ID));
        assertFalse(isCssIdShown(TREASURY_ID));
    }

    /**
     * An admitted visitor sees the offered goods with their stock, the empty
     * slot marked unavailable, the wanted goods and the treasury.
     */
    public void verifyMarketShown(final String offerFirst,
            final String offerSecond, final String unavailable,
            final String wanted, final String treasury) {
        assertTrue(isCssIdShown(OPEN_MARKET_BUTTON_ID));
        assertEquals(offerFirst, getLabeledById(OFFER_ID_PREFIX + 0).getText());
        assertEquals(offerSecond,
                getLabeledById(OFFER_ID_PREFIX + 1).getText());
        assertEquals(unavailable,
                getLabeledById(OFFER_ID_PREFIX + 2).getText());
        assertEquals(wanted, getLabeledById(WANTED_ID).getText());
        assertEquals(treasury, getLabeledById(TREASURY_ID).getText());
    }

    public void verifyOpenMarketShown(final boolean expected) {
        assertEquals(expected, isCssIdShown(OPEN_MARKET_BUTTON_ID));
    }

    public void verifyExpeditionLine(final String expected) {
        assertEquals(expected, getLabeledById(EXPEDITION_ID).getText());
    }

    public void verifyChooserClosed() {
        assertFalse(isCssIdVisible(CONTENT_ID));
    }

    /** Model: the tribe knows the nation, so the introduction took effect. */
    public void verifyIntroduced(final EuropeanNation european,
            final NativeNation nation) {
        assertTrue(getModel().isNativeContacted(european, nation));
    }

    /** Model: the tribe still does not know the nation. */
    public void verifyNotIntroduced(final EuropeanNation european,
            final NativeNation nation) {
        assertFalse(getModel().isNativeContacted(european, nation));
    }

    /** Model: the carrier's expedition is open at its square right now. */
    public void verifyExpeditionOpenFor(final int unitId, final Location at) {
        assertTrue(view(unitId, at).isExpeditionOpen());
    }

    /** Model: the carrier has no open expedition at this settlement. */
    public void verifyExpeditionClosedFor(final int unitId, final Location at) {
        assertFalse(view(unitId, at).isExpeditionOpen());
    }

    /** Model: the carrier still stands where it stood before the chooser. */
    public void verifyCarrierStays(final int unitId, final Location expected) {
        assertEquals(expected, getModel().getUnitById(unitId).getLocation());
    }

    /** Model: the stock of an offered good of the settlement at this square. */
    public void verifyStock(final Location settlement, final GoodsType goods,
            final int expected) {
        assertEquals(expected, knownSettlement(settlement).getStock(goods));
    }

    /** Model: the settlement treasury at this square. */
    public void verifyTreasury(final Location settlement, final int expected) {
        assertEquals(expected, knownSettlement(settlement).getTreasury());
    }

    /**
     * Sell the shown terms; the confirmation opens after the chooser closes.
     */
    public void sell() {
        getRobot().clickOn(chooserButton(SALE_SELL_BUTTON_ID));
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Give the shown goods away; the confirmation opens. */
    public void gift() {
        getRobot().clickOn(chooserButton(SALE_GIFT_BUTTON_ID));
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Haggle once; the chooser then reopens with the drawn result. */
    public void haggle() {
        getRobot().clickOn(chooserButton(SALE_HAGGLE_BUTTON_ID));
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Confirm the open sale or gift confirmation. */
    public void confirmDeal() {
        getRobot().clickOn(confirmationButton(CONFIRM_BUTTON_ID));
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Back out of the confirmation, which changes nothing. */
    public void backFromDeal() {
        getRobot().clickOn(confirmationButton(CONFIRM_BACK_ID));
        WaitForAsyncUtils.waitForFxEvents();
    }

    private Button confirmationButton(final String id) {
        return getNodeFinder().lookup("#" + CONFIRM_CONTENT_ID + " #" + id)
                .queryButton();
    }

    /** Gold the player holds now; a getter, nothing is asserted here. */
    public int gold() {
        return getModel().getPlayerByName("Dutch").getGold();
    }

    /**
     * The preview names the offered pieces and the gold the model quotes for a
     * sale, and the sale button is enabled.
     */
    public void verifySaleOffered(final int unitId, final Location settlement,
            final GoodsType goods, final int quantity) {
        final NativeSaleQuote sale = saleQuote(unitId, settlement, goods,
                quantity, false);
        assertTrue(getLabeledById(SALE_PREVIEW_ID).getText()
                .contains(String.format("Sell %d pieces of %s for %d gold.",
                        quantity, englishName(goods), sale.getGold())));
        assertFalse(isButtonDisabled(SALE_SELL_BUTTON_ID));
    }

    /** The preview names the gift without any gold, and gift is enabled. */
    public void verifyGiftOffered(final int unitId, final Location settlement,
            final GoodsType goods, final int quantity) {
        assertTrue(getLabeledById(SALE_PREVIEW_ID).getText()
                .contains(String.format(
                        "Give %d pieces of %s as a gift. No gold is paid.",
                        quantity, englishName(goods))));
        assertFalse(isButtonDisabled(SALE_GIFT_BUTTON_ID));
        assertEquals(0,
                saleQuote(unitId, settlement, goods, quantity, true).getGold());
    }

    /**
     * In Czech the preview names the sale and the gift with the model's pieces
     * and gold, and the goods keep their Czech name.
     */
    public void verifyCzechOffered(final int unitId, final Location settlement,
            final GoodsType goods, final int quantity) {
        final NativeSaleQuote sale = saleQuote(unitId, settlement, goods,
                quantity, false);
        final String preview = getLabeledById(SALE_PREVIEW_ID).getText();
        assertTrue(preview.contains(
                String.format("Prodáte %d kusů zboží Jídlo za %d zlata.",
                        quantity, sale.getGold())));
        assertTrue(preview.contains(String.format(
                "Darujete %d kusů zboží Jídlo. Žádné zlato se neplatí.",
                quantity)));
    }

    /** The confirmation names exactly the sale the model quotes. */
    public void verifySaleConfirmation(final int unitId,
            final Location settlement, final GoodsType goods,
            final int quantity) {
        final NativeSaleQuote sale = saleQuote(unitId, settlement, goods,
                quantity, false);
        assertEquals(
                String.format("Sell %d pieces of %s for %d gold.", quantity,
                        englishName(goods), sale.getGold()),
                getLabeledById(CONFIRM_LINE_ID).getText());
    }

    /** The confirmation names exactly the gift the model quotes. */
    public void verifyGiftConfirmation(final GoodsType goods,
            final int quantity) {
        assertEquals(
                String.format(
                        "Give %d pieces of %s as a gift. No gold is paid.",
                        quantity, englishName(goods)),
                getLabeledById(CONFIRM_LINE_ID).getText());
    }

    /**
     * After one haggle the chooser shows the result the model keeps, the offer
     * carries the matching terms, and the haggle button is spent.
     */
    public void verifyHaggleResultShown(final int unitId,
            final Location settlement, final GoodsType goods,
            final int quantity) {
        final NativeSaleQuote sale = saleQuote(unitId, settlement, goods,
                quantity, false);
        final String expected = sale
                .getHaggle() == NativeSaleQuote.Haggle.ACCEPTED
                        ? HAGGLE_ACCEPTED_EN
                        : HAGGLE_REFUSED_EN;
        assertEquals(expected, getLabeledById(SALE_HAGGLE_STATE_ID).getText());
        assertTrue(getLabeledById(SALE_PREVIEW_ID).getText()
                .contains(String.format("Sell %d pieces of %s for %d gold.",
                        quantity, englishName(goods), sale.getGold())));
        assertTrue(isButtonDisabled(SALE_HAGGLE_BUTTON_ID));
    }

    /** The chooser's refused or no-goods notice, exactly as shown. */
    public void verifyNoticeShown(final String expected) {
        assertEquals(expected, getLabeledById(SALE_NOTICE_ID).getText());
    }

    /** Model: the carrier holds this many pieces of the goods now. */
    public void verifyHeld(final int unitId, final GoodsType goods,
            final int expected) {
        assertEquals(expected, held(unitId, goods));
    }

    /** Model: the player holds exactly this much gold. */
    public void verifyGold(final int expected) {
        assertEquals(expected, gold());
    }

    /** Model: the settlement has bought this many pieces of the goods. */
    public void verifyReceived(final Location settlement, final GoodsType goods,
            final int expected) {
        assertEquals(expected, knownSettlement(settlement).getReceivedStock()
                .getOrDefault(goods, 0).intValue());
    }

    /** Model: the quoted gold of a sale, read without changing anything. */
    public int quotedGold(final int unitId, final Location settlement,
            final GoodsType goods, final int quantity) {
        return saleQuote(unitId, settlement, goods, quantity, false).getGold();
    }

    private NativeSaleQuote saleQuote(final int unitId,
            final Location settlement, final GoodsType goods,
            final int quantity, final boolean gift) {
        return getModel()
                .getNativeSaleQuote(getModel().getUnitById(unitId),
                        knownSettlement(settlement), goods, quantity, gift)
                .orElseThrow(() -> new AssertionError(
                        "No sale quote for unit " + unitId));
    }

    private int held(final int unitId, final GoodsType goods) {
        return ((UnitWithCargo) getModel().getUnitById(unitId)).getCargo()
                .getSlots().stream()
                .flatMap(slot -> slot.getGoods()
                        .filter(carried -> carried.getType().equals(goods))
                        .stream())
                .mapToInt(Goods::getAmount).sum();
    }

    private boolean isButtonDisabled(final String id) {
        return getNodeFinder().lookup("#" + CONTENT_ID + " #" + id)
                .queryButton().isDisabled();
    }

    private static String englishName(final GoodsType goods) {
        if (goods.equals(GoodsType.CORN)) {
            return "Corn";
        }
        if (goods.equals(GoodsType.TOBACCO)) {
            return "Tobacco";
        }
        return goods.name();
    }

    private NativeMarketView view(final int unitId, final Location at) {
        return getModel()
                .getNativeMarketView(getModel().getUnitById(unitId),
                        knownSettlement(at))
                .orElseThrow(() -> new AssertionError(
                        "No market view for unit " + unitId));
    }

    /**
     * Set the sale pieces with the keyboard: a click focuses the slider, the
     * left arrow drives it to its minimum, and each right arrow adds one piece.
     */
    public void chooseSalePieces(final int pieces) {
        selectPieces(SALE_QUANTITY_ID, pieces);
    }

    /** The same keyboard selection for the purchase pieces. */
    public void choosePurchasePieces(final int pieces) {
        selectPieces(PURCHASE_QUANTITY_ID, pieces);
    }

    private void selectPieces(final String sliderId, final int pieces) {
        getRobot().clickOn(chooserSlider(sliderId));
        for (int press = 0; press < SLIDER_TO_MINIMUM_PRESSES; press++) {
            getRobot().push(KeyCode.LEFT);
        }
        for (int press = 1; press < pieces; press++) {
            getRobot().push(KeyCode.RIGHT);
        }
        WaitForAsyncUtils.waitForFxEvents();
    }

    private Slider chooserSlider(final String id) {
        return getNodeFinder().lookup("#" + CONTENT_ID + " #" + id)
                .queryAs(Slider.class);
    }

    /**
     * Request the one discount of the second stage; the chooser then reopens
     * with the result the model keeps.
     */
    public void purchaseHaggle() {
        getRobot().clickOn(chooserButton(PURCHASE_HAGGLE_BUTTON_ID));
        WaitForAsyncUtils.waitForFxEvents();
    }

    /**
     * Decline the pending second stage with its own button; the result message
     * opens after the chooser closes, and buys nothing.
     */
    public void declinePurchase() {
        getRobot().clickOn(chooserButton(PURCHASE_DECLINE_BUTTON_ID));
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** The decline message says that nothing was bought. */
    public void verifyPurchaseDeclined() throws Exception {
        DialogMessagePage.of(getContext())
                .verifyDialogTextContains(PURCHASE_DECLINED_EN);
    }

    /**
     * Buy the shown purchase; its confirmation opens after the chooser closes.
     */
    public void buy() {
        getRobot().clickOn(chooserButton(PURCHASE_BUY_BUTTON_ID));
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Confirm the open purchase confirmation. */
    public void confirmPurchase() {
        getRobot().clickOn(
                purchaseConfirmationButton(PURCHASE_CONFIRM_BUTTON_ID));
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Back out of the purchase confirmation, which changes nothing. */
    public void backFromPurchase() {
        getRobot()
                .clickOn(purchaseConfirmationButton(PURCHASE_CONFIRM_BACK_ID));
        WaitForAsyncUtils.waitForFxEvents();
    }

    /** Close the result message that a confirmed purchase shows. */
    public void closePurchaseMessage() {
        DialogMessagePage.of(getContext()).close();
    }

    private Button purchaseConfirmationButton(final String id) {
        return getNodeFinder()
                .lookup("#" + PURCHASE_CONFIRM_CONTENT_ID + " #" + id)
                .queryButton();
    }

    /**
     * Model: pieces of one goods type the carrier holds; a getter, no
     * assertion.
     */
    public int carried(final int unitId, final GoodsType goods) {
        return held(unitId, goods);
    }

    /** Model: the stock of an offered good; a getter, no assertion. */
    public int stock(final Location settlement, final GoodsType goods) {
        return knownSettlement(settlement).getStock(goods);
    }

    /** Model: the treasury now; a getter, no assertion. */
    public int treasury(final Location settlement) {
        return knownSettlement(settlement).getTreasury();
    }

    /**
     * The goods the purchase selects first, as the chooser shows them; a
     * getter, nothing is asserted here.
     */
    public GoodsType purchaseGoods() {
        final ComboBox<GoodsType> goods = getNodeFinder()
                .lookup("#" + CONTENT_ID + " #" + PURCHASE_GOODS_ID)
                .queryAs(ComboBox.class);
        return goods.getValue();
    }

    /** Model: the gold the purchase quote asks now; a getter, no assertion. */
    public int purchaseGold(final int unitId, final Location settlement,
            final GoodsType goods, final int quantity) {
        return purchaseQuote(unitId, settlement, goods, quantity).getGold();
    }

    /**
     * The purchase section is shown exactly while a second stage is pending.
     */
    public void verifyPurchaseShown(final boolean expected) {
        // A missing chooser window must not pass as a hidden purchase section.
        assertTrue(isCssIdVisible(CONTENT_ID), "Chooser is not open");
        assertEquals(expected, isCssIdShown(PURCHASE_HEADER_ID));
    }

    /** The purchase slider names exactly these pieces. */
    public void verifyPurchaseQuantity(final int expected) {
        assertEquals(String.valueOf(expected),
                getLabeledById(PURCHASE_QUANTITY_VALUE_ID).getText());
    }

    /** The sale slider names exactly these pieces. */
    public void verifySaleQuantity(final int expected) {
        assertEquals(String.valueOf(expected),
                getLabeledById(SALE_QUANTITY_VALUE_ID).getText());
    }

    /**
     * The offer lines show each offered good with its stock and Europe ask, and
     * every slot without a good reads as unavailable.
     */
    public void verifyPurchaseOffers(final int unitId,
            final Location settlement) {
        final NativeMarketView.Purchase purchase = view(unitId, settlement)
                .getPurchase().orElseThrow();
        for (int slot = 0; slot < NativeMarketRules.OFFER_SLOTS; slot++) {
            final String expected = slot < purchase.getOffers().size()
                    ? purchaseOfferLine(purchase.getOffers().get(slot))
                    : UNAVAILABLE_SLOT_EN;
            assertEquals(expected,
                    getLabeledById(OFFER_ID_PREFIX + slot).getText());
        }
    }

    /**
     * The preview names the pieces and the gold the model quotes for the
     * purchase, and buy is enabled.
     */
    public void verifyPurchaseOffered(final int unitId,
            final Location settlement, final GoodsType goods,
            final int quantity) {
        final NativePurchaseQuote quote = purchaseQuote(unitId, settlement,
                goods, quantity);
        assertEquals(purchaseLine(goods, quantity, quote.getGold()),
                getLabeledById(PURCHASE_PREVIEW_ID).getText());
        assertFalse(isButtonDisabled(PURCHASE_BUY_BUTTON_ID));
    }

    /** The confirmation names exactly the purchase the model quotes. */
    public void verifyPurchaseConfirmation(final int unitId,
            final Location settlement, final GoodsType goods,
            final int quantity) {
        final NativePurchaseQuote quote = purchaseQuote(unitId, settlement,
                goods, quantity);
        assertEquals(purchaseLine(goods, quantity, quote.getGold()),
                getLabeledById(PURCHASE_CONFIRM_LINE_ID).getText());
    }

    /**
     * After the one request the purchase shows the result the model keeps, the
     * quote carries the matching gold, and the request button is spent.
     */
    public void verifyPurchaseHaggleResultShown(final int unitId,
            final Location settlement, final GoodsType goods,
            final int quantity) {
        final NativePurchaseQuote quote = purchaseQuote(unitId, settlement,
                goods, quantity);
        final String expected = quote
                .getHaggle() == NativeSaleQuote.Haggle.ACCEPTED
                        ? PURCHASE_HAGGLE_ACCEPTED_EN
                        : PURCHASE_HAGGLE_REFUSED_EN;
        assertEquals(expected,
                getLabeledById(PURCHASE_HAGGLE_STATE_ID).getText());
        assertEquals(purchaseLine(goods, quantity, quote.getGold()),
                getLabeledById(PURCHASE_PREVIEW_ID).getText());
        assertTrue(isButtonDisabled(PURCHASE_HAGGLE_BUTTON_ID));
    }

    /**
     * The quoted gold follows the accepted rule: the Europe ask times the
     * pieces with no tax, and after an accepted request ninety per cent of that
     * whole total rounded up once. A refused or unused request keeps the full
     * total.
     */
    public void verifyPurchasePriceRule(final int unitId,
            final Location settlement, final GoodsType goods,
            final int quantity) {
        final NativePurchaseQuote quote = purchaseQuote(unitId, settlement,
                goods, quantity);
        final long full = (long) quote.getAskPrice() * quantity;
        final long expected = quote
                .getHaggle() == NativeSaleQuote.Haggle.ACCEPTED
                        ? (full * 9 + 9) / 10
                        : full;
        assertEquals(expected, quote.getGold());
    }

    /** The result message of a confirmed purchase names the pieces and gold. */
    public void verifyPurchaseMessage(final GoodsType goods, final int quantity,
            final int gold) throws Exception {
        DialogMessagePage.of(getContext()).verifyDialogTextContains(
                String.format("You bought %d pieces of %s for %d gold.",
                        quantity, englishName(goods), gold));
    }

    /** The Czech chooser names the second stage with its own header. */
    public void verifyCzechPurchaseHeader() {
        assertEquals(
                "Druhý krok: po prodeji nebo daru můžete koupit jedno z těchto zboží.",
                getLabeledById(PURCHASE_HEADER_ID).getText());
    }

    private static String purchaseLine(final GoodsType goods,
            final int quantity, final int gold) {
        return String.format("Buy %d pieces of %s for %d gold.", quantity,
                englishName(goods), gold);
    }

    private static String purchaseOfferLine(
            final NativeMarketView.PurchaseOffer offer) {
        return String.format("Buy %s: %d in stock at %d gold each.",
                englishName(offer.getGoods()), offer.getStock(),
                offer.getAskPrice());
    }

    private NativePurchaseQuote purchaseQuote(final int unitId,
            final Location settlement, final GoodsType goods,
            final int quantity) {
        return getModel()
                .getNativePurchaseQuote(getModel().getUnitById(unitId),
                        knownSettlement(settlement), goods, quantity)
                .orElseThrow(() -> new AssertionError(
                        "No purchase quote for unit " + unitId));
    }

    private NativeSettlement knownSettlement(final Location settlement) {
        return getModel()
                .getNativeSettlementKnownTo(getModel().getPlayerByName("Dutch"),
                        settlement)
                .orElseThrow(() -> new AssertionError(
                        "No native settlement known at " + settlement));
    }

}
