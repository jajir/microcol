package org.microcol.gui.screen.nativesettlements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.microcol.gui.GoodsTypeName;
import org.microcol.gui.Loc;
import org.microcol.gui.UnitTypes;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ButtonBarOk;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.GoodsType;
import org.microcol.model.NativeMarketRules;
import org.microcol.model.NativeMarketView;
import org.microcol.model.NativePurchaseQuote;
import org.microcol.model.NativeSaleQuote;
import org.microcol.model.NativeSettlement;
import org.microcol.model.NativeVisitAction;
import org.microcol.model.NativeVisitChoice;
import org.microcol.model.UnitType;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 * Chooser opened when a unit tries to enter a known native settlement. It lists
 * every visit action with the limit the model reports, then the land contact
 * and the market admission the model gives this unit. Only an eligible teaching
 * choice offers its button; that button confirms the course first and then runs
 * the model operation. Introduction, opening the market, and the sale, gift or
 * haggle buttons only close the chooser and report the chosen command; the
 * caller runs it after this window has closed, with a confirmation for a sale
 * or gift, and then opens a new chooser with the new state. The purchase of the
 * second stage (R-0161) works the same way: buy, request a discount, or
 * decline. Closing the window changes nothing: the unit, relations, cargo and
 * treasury stay as they were.
 */
public final class DialogNativeVisit extends AbstractMessageWindow {

    public static final String CONTENT_ID = "nativeVisitContent";

    public static final String SKILL_ID = "nativeVisitSkill";

    public static final String CHOICE_ID_PREFIX = "nativeVisitChoice_";

    public static final String TEACH_BUTTON_ID = "nativeVisitTeach";

    /** Explicit command chosen in the chooser, run after it has closed. */
    public enum FollowUp {

        /** The chooser was closed, cancelled or used for teaching. */
        NONE,

        /** Introduce the player's nation by land. */
        INTRODUCE,

        /** Open or reuse the carrier's market expedition. */
        OPEN_MARKET,

        /** Confirm and sell the shown goods; see {@link #getPendingSale()}. */
        SELL,

        /** Confirm and give the shown goods; see {@link #getPendingSale()}. */
        GIFT,

        /**
         * Haggle once over the shown offer; see {@link #getPendingSale()}.
         */
        HAGGLE,

        /**
         * Confirm and buy the shown purchase; see
         * {@link #getPendingPurchase()}.
         */
        BUY,

        /**
         * Request the one discount of the purchase stage; see
         * {@link #getPendingPurchase()}.
         */
        PURCHASE_HAGGLE,

        /** Decline the pending purchase; nothing is bought. */
        DECLINE_PURCHASE

    }

    public static final String MARKET_CONTACT_ID = "nativeMarketContact";

    public static final String MARKET_ADMISSION_ID = "nativeMarketAdmission";

    public static final String INTRODUCE_BUTTON_ID = "nativeMarketIntroduce";

    public static final String OPEN_MARKET_BUTTON_ID = "nativeMarketOpen";

    public static final String EXPEDITION_ID = "nativeMarketExpedition";

    public static final String OFFER_ID_PREFIX = "nativeMarketOffer_";

    public static final String WANTED_ID = "nativeMarketWanted";

    public static final String TREASURY_ID = "nativeMarketTreasury";

    public static final String SALE_GOODS_ID = "nativeSaleGoods";

    public static final String SALE_QUANTITY_ID = "nativeSaleQuantity";

    public static final String SALE_QUANTITY_VALUE_ID = "nativeSaleQuantityValue";

    public static final String SALE_PREVIEW_ID = "nativeSalePreview";

    public static final String SALE_HAGGLE_STATE_ID = "nativeSaleHaggleState";

    public static final String SALE_NOTICE_ID = "nativeSaleNotice";

    public static final String SALE_SELL_BUTTON_ID = "nativeSaleSell";

    public static final String SALE_GIFT_BUTTON_ID = "nativeSaleGift";

    public static final String SALE_HAGGLE_BUTTON_ID = "nativeSaleHaggle";

    public static final String PURCHASE_HEADER_ID = "nativePurchaseHeader";

    public static final String PURCHASE_GOODS_ID = "nativePurchaseGoods";

    public static final String PURCHASE_QUANTITY_ID = "nativePurchaseQuantity";

    public static final String PURCHASE_QUANTITY_VALUE_ID = "nativePurchaseQuantityValue";

    public static final String PURCHASE_PREVIEW_ID = "nativePurchasePreview";

    public static final String PURCHASE_HAGGLE_STATE_ID = "nativePurchaseHaggleState";

    public static final String PURCHASE_NOTICE_ID = "nativePurchaseNotice";

    public static final String PURCHASE_BUY_BUTTON_ID = "nativePurchaseBuy";

    public static final String PURCHASE_HAGGLE_BUTTON_ID = "nativePurchaseHaggle";

    public static final String PURCHASE_DECLINE_BUTTON_ID = "nativePurchaseDecline";

    /**
     * Model reads that shape the sale and purchase controls. All are pure:
     * asking them changes no goods, gold, relation, expedition or random state.
     */
    public interface SaleTerms {

        /**
         * @return most pieces of these goods that a sale or gift may name now
         */
        int maxQuantity(GoodsType goods, boolean gift);

        /**
         * @return terms of the sale or gift, empty when it is not legal now
         */
        Optional<NativeSaleQuote> quote(GoodsType goods, int quantity,
                boolean gift);

        /**
         * @return most pieces of an offered good that the purchase may buy now
         */
        int maxPurchaseQuantity(GoodsType goods);

        /**
         * @return terms of the purchase, empty when it is not legal now
         */
        Optional<NativePurchaseQuote> purchaseQuote(GoodsType goods,
                int quantity);

    }

    private final NativeSettlement settlement;

    private final List<NativeVisitChoice> choices;

    private final UnitType visitorType;

    private final NativeMarketView market;

    private final SaleTerms saleTerms;

    private final Runnable onTeach;

    private FollowUp followUp = FollowUp.NONE;

    private NativeSaleQuote pendingSale;

    private NativeSaleQuote shownSale;

    private NativeSaleQuote shownGift;

    private NativePurchaseQuote pendingPurchase;

    private NativePurchaseQuote shownPurchase;

    /** Language of the last refresh, used by the sale controls. */
    private I18n texts;

    private final ButtonBarOk buttonBar;

    private final Label intro = new Label();

    private final Label skill = new Label();

    private final List<Label> choiceLabels;

    private final Button teach = new Button();

    private final Label contact = new Label();

    private final Label admission = new Label();

    private final Button introduce = new Button();

    private final Button openMarket = new Button();

    private final Label expedition = new Label();

    private final List<Label> offerLabels = new ArrayList<>();

    private final Label wanted = new Label();

    private final Label treasury = new Label();

    private final Label saleGoodsLabel = new Label();

    private final ComboBox<GoodsType> saleGoods = new ComboBox<>();

    private final Label saleQuantityLabel = new Label();

    private final Slider saleQuantity = new Slider();

    private final Label saleQuantityValue = new Label();

    private final Label salePreview = new Label();

    private final Label saleHaggleState = new Label();

    private final Label saleNotice = new Label();

    private final Button sellButton = new Button();

    private final Button giftButton = new Button();

    private final Button haggleButton = new Button();

    private final Label purchaseHeader = new Label();

    private final Label purchaseGoodsLabel = new Label();

    private final ComboBox<GoodsType> purchaseGoods = new ComboBox<>();

    private final Label purchaseQuantityLabel = new Label();

    private final Slider purchaseQuantity = new Slider();

    private final Label purchaseQuantityValue = new Label();

    private final Label purchasePreview = new Label();

    private final Label purchaseHaggleState = new Label();

    private final Label purchaseNotice = new Label();

    private final Button buyButton = new Button();

    private final Button purchaseHaggleButton = new Button();

    private final Button declineButton = new Button();

    /** Pieces of each carried good, as the model reports them now. */
    private final Map<GoodsType, Integer> carriedAmounts = new HashMap<>();

    /**
     * Goods and pieces the player had chosen in a chooser. The caller carries
     * it to the next chooser only after a confirmation was backed out of, so
     * Back or Escape returns to the same offer. It is presentation only:
     * nothing in the model changes, the live limits clamp it, and every command
     * is quoted by the model again.
     */
    public static final class Selection {

        private final GoodsType saleGoods;

        private final int saleQuantity;

        private final GoodsType purchaseGoods;

        private final int purchaseQuantity;

        Selection(final GoodsType saleGoods, final int saleQuantity,
                final GoodsType purchaseGoods, final int purchaseQuantity) {
            this.saleGoods = saleGoods;
            this.saleQuantity = saleQuantity;
            this.purchaseGoods = purchaseGoods;
            this.purchaseQuantity = purchaseQuantity;
        }

    }

    /** Selection carried from the previous chooser, or null for none. */
    private final Selection kept;

    /**
     * @param saleTerms model reads for the sale controls, pure
     * @param onTeach   run once the player confirms learning in the
     *                  confirmation window; the model operation runs there, not
     *                  here
     * @param kept      selection of the previous chooser after a backed-out
     *                  confirmation, or null to start from the model's offer
     */
    public DialogNativeVisit(final ViewUtil viewUtil, final I18n i18n,
            final NativeSettlement settlement,
            final List<NativeVisitChoice> choices, final UnitType visitorType,
            final NativeMarketView market, final SaleTerms saleTerms,
            final Runnable onTeach, final Selection kept) {
        super(viewUtil, i18n);
        this.settlement = settlement;
        this.choices = List.copyOf(choices);
        this.visitorType = visitorType;
        this.market = market;
        this.saleTerms = saleTerms;
        this.onTeach = onTeach;
        this.kept = kept;
        buttonBar = new ButtonBarOk(i18n);
        buttonBar.getButtonOk().setOnAction(e -> {
            close();
        });

        intro.setWrapText(true);
        skill.setId(SKILL_ID);
        skill.setWrapText(true);
        choiceLabels = this.choices.stream().map(choice -> {
            final Label label = new Label();
            label.setId(CHOICE_ID_PREFIX + choice.getAction().name());
            label.setWrapText(true);
            return label;
        }).collect(Collectors.toList());
        teach.setId(TEACH_BUTTON_ID);
        teach.setVisible(isTeachingAvailable());
        teach.setManaged(isTeachingAvailable());
        teach.setOnAction(e -> new DialogNativeTeaching(viewUtil, i18n,
                visitorType, settlement.getTeachableSkill(), () -> {
                    close();
                    onTeach.run();
                }).showAndWait());
        final boolean admitted = isAdmitted();
        // The second stage shows the same offered goods, even with no goods
        // held.
        final boolean purchasing = market.getPurchase().isPresent();
        contact.setId(MARKET_CONTACT_ID);
        contact.setWrapText(true);
        admission.setId(MARKET_ADMISSION_ID);
        admission.setWrapText(true);
        introduce.setId(INTRODUCE_BUTTON_ID);
        introduce.setVisible(market.isIntroductionOffered());
        introduce.setManaged(market.isIntroductionOffered());
        introduce.setOnAction(e -> {
            followUp = FollowUp.INTRODUCE;
            close();
        });
        openMarket.setId(OPEN_MARKET_BUTTON_ID);
        openMarket.setVisible(admitted);
        openMarket.setManaged(admitted);
        openMarket.setOnAction(e -> {
            followUp = FollowUp.OPEN_MARKET;
            close();
        });
        expedition.setId(EXPEDITION_ID);
        expedition.setWrapText(true);
        expedition.setVisible(admitted);
        expedition.setManaged(admitted);
        // Stock, wanted goods and treasury exist only for an admitted visitor.
        for (int slot = 0; slot < NativeMarketRules.OFFER_SLOTS; slot++) {
            final Label offer = new Label();
            offer.setId(OFFER_ID_PREFIX + slot);
            offer.setWrapText(true);
            offer.setVisible(admitted || purchasing);
            offer.setManaged(admitted || purchasing);
            offerLabels.add(offer);
        }
        wanted.setId(WANTED_ID);
        wanted.setWrapText(true);
        wanted.setVisible(admitted);
        wanted.setManaged(admitted);
        treasury.setId(TREASURY_ID);
        treasury.setWrapText(true);
        treasury.setVisible(admitted);
        treasury.setManaged(admitted);
        final VBox saleBox = buildSaleBox(admitted);
        final VBox list = new VBox(6);
        list.getChildren().addAll(choiceLabels);
        // The left column holds the visit and the market status, the right one
        // the offered goods and the sale and purchase controls; split this way
        // the chooser keeps every control inside a 900x750 window, also when a
        // sale and a purchase are shown together.
        final VBox statusBox = new VBox(6, contact, admission, introduce,
                openMarket, expedition, wanted, treasury);
        final VBox visitBox = new VBox(10, skill, list, teach, statusBox);
        visitBox.setMinWidth(360);
        visitBox.setPrefWidth(360);
        final VBox marketBox = new VBox(6);
        marketBox.getChildren().addAll(offerLabels);
        marketBox.getChildren().add(saleBox);
        marketBox.getChildren().add(buildPurchaseBox(purchasing));
        marketBox.setMinWidth(450);
        marketBox.setPrefWidth(450);
        final HBox columns = new HBox(16, visitBox, marketBox);
        final VBox root = new VBox(10, intro, columns, buttonBar.getContent());
        root.setPrefWidth(846);
        root.setId(CONTENT_ID);
        root.setPadding(new Insets(10));
        // Handle Escape before the focused button consumes it.
        root.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                close();
                event.consume();
            }
        });
        init(root);
    }

    /**
     * Sale controls of an admitted visitor (R-0160): the carried goods, the
     * pieces, the exact terms of the current sale and gift, the haggle state
     * and one button for each command. Every value comes from the model through
     * {@link SaleTerms}, and a button runs only after this window has closed.
     */
    private VBox buildSaleBox(final boolean admitted) {
        final List<NativeMarketView.Carried> carried = market.getMarket()
                .map(NativeMarketView.Market::getCarried).orElse(List.of());
        for (final NativeMarketView.Carried good : carried) {
            carriedAmounts.put(good.getGoods(), good.getAmount());
        }
        saleGoodsLabel.setWrapText(true);
        saleGoods.setId(SALE_GOODS_ID);
        saleGoods.getItems()
                .setAll(carried.stream().map(NativeMarketView.Carried::getGoods)
                        .collect(Collectors.toList()));
        // A backed-out confirmation returns to the chosen offer; after a haggle
        // the haggled offer is shown again, also when the chooser is reopened
        // or the game continued; otherwise the first goods lead.
        final Optional<GoodsType> keptGoods = keptSaleGoods()
                .filter(carriedAmounts::containsKey);
        if (keptGoods.isPresent()) {
            saleGoods.getSelectionModel().select(keptGoods.get());
        } else if (!carried.isEmpty()) {
            saleGoods.getSelectionModel().selectFirst();
        }
        saleQuantityLabel.setWrapText(true);
        // The shared whole-number slider: arrow keys step by one piece, and
        // there is no text editor, so no invalid or uncommitted amount exists.
        saleQuantity.setId(SALE_QUANTITY_ID);
        saleQuantity.setSnapToTicks(true);
        saleQuantity.setMajorTickUnit(1);
        saleQuantity.setMinorTickCount(0);
        saleQuantity.setBlockIncrement(1);
        saleQuantity.setShowTickMarks(false);
        saleQuantity.setShowTickLabels(false);
        saleQuantityValue.setId(SALE_QUANTITY_VALUE_ID);
        resetQuantity(keptGoods.isPresent() ? keptSaleQuantity() : 0);
        salePreview.setId(SALE_PREVIEW_ID);
        salePreview.setWrapText(true);
        saleHaggleState.setId(SALE_HAGGLE_STATE_ID);
        saleHaggleState.setWrapText(true);
        saleNotice.setId(SALE_NOTICE_ID);
        saleNotice.setWrapText(true);
        sellButton.setId(SALE_SELL_BUTTON_ID);
        sellButton.setOnAction(e -> {
            pendingSale = shownSale;
            followUp = FollowUp.SELL;
            close();
        });
        giftButton.setId(SALE_GIFT_BUTTON_ID);
        giftButton.setOnAction(e -> {
            pendingSale = shownGift;
            followUp = FollowUp.GIFT;
            close();
        });
        haggleButton.setId(SALE_HAGGLE_BUTTON_ID);
        haggleButton.setOnAction(e -> {
            pendingSale = shownSale;
            followUp = FollowUp.HAGGLE;
            close();
        });
        saleGoods.valueProperty().addListener((observed, before, after) -> {
            resetQuantity(0);
            refreshSale();
        });
        saleQuantity.valueProperty().addListener((observed, before, after) -> {
            // Dragging passes fractions; only a new whole amount is quoted.
            if (Math.round(before.doubleValue()) != Math
                    .round(after.doubleValue())) {
                refreshSale();
            }
        });
        // Goods and pieces share one row, and the three commands share another,
        // to keep the chooser inside a 900x750 window.
        saleQuantity.setPrefWidth(160);
        final HBox chooseRow = new HBox(8, saleGoodsLabel, saleGoods,
                saleQuantityLabel, saleQuantity, saleQuantityValue);
        chooseRow.setAlignment(Pos.CENTER_LEFT);
        final HBox commandRow = new HBox(8, sellButton, giftButton,
                haggleButton);
        final VBox box = new VBox(6, chooseRow, salePreview, saleHaggleState,
                saleNotice, commandRow);
        box.setVisible(admitted);
        box.setManaged(admitted);
        return box;
    }

    /**
     * Start the pieces at the most the natives can pay for, or at the whole
     * carried stock when they can pay for none; the range is the carried stock.
     * A haggled offer is shown again at the pieces it was made for, clamped to
     * the carried stock.
     *
     * @param preferred pieces shown with the haggle, or zero for none
     */
    private void resetQuantity(final int preferred) {
        final GoodsType goods = saleGoods.getValue();
        final int held = goods == null ? 1
                : carriedAmounts.getOrDefault(goods, 1);
        final int payable = goods == null ? 0
                : saleTerms.maxQuantity(goods, false);
        // The minimum stays one; a lower maximum first clamps the value.
        saleQuantity.setMin(1);
        saleQuantity.setMax(held);
        saleQuantity.setValue(preferred > 0 ? Math.min(preferred, held)
                : Math.min(held, Math.max(1, payable)));
    }

    /**
     * @return whole pieces the slider names now, within its range
     */
    private int quantity() {
        return (int) Math.round(saleQuantity.getValue());
    }

    /**
     * Set a status text and leave its line out of the layout while empty, so
     * unused lines take no height.
     */
    private static void showText(final Label label, final String text) {
        label.setText(text);
        final boolean shown = !text.isEmpty();
        label.setVisible(shown);
        label.setManaged(shown);
    }

    /**
     * Recompute the shown sale and gift from the model and enable only the
     * buttons whose terms are legal now. Called from the controls and from the
     * language refresh, never from a model change.
     */
    private void refreshSale() {
        if (texts == null) {
            // Not shown yet; the first language refresh fills everything in.
            return;
        }
        final GoodsType goods = saleGoods.getValue();
        final boolean open = market.isExpeditionOpen();
        final int quantity = quantity();
        saleQuantityValue
                .setText(goods == null ? "" : String.valueOf(quantity));
        shownSale = goods == null || !open ? null
                : saleTerms.quote(goods, quantity, false).orElse(null);
        shownGift = goods == null || !open ? null
                : saleTerms.quote(goods, quantity, true).orElse(null);
        final StringBuilder preview = new StringBuilder();
        if (shownSale != null) {
            preview.append(texts.get(Loc.nativeSalePreviewSale, quantity,
                    texts.get(GoodsTypeName.getNameForGoodsType(goods)),
                    shownSale.getGold()));
        }
        if (shownGift != null) {
            if (preview.length() > 0) {
                preview.append('\n');
            }
            preview.append(texts.get(Loc.nativeSalePreviewGift, quantity,
                    texts.get(GoodsTypeName.getNameForGoodsType(goods))));
        }
        showText(salePreview, preview.toString());
        showText(saleHaggleState, shownSale == null ? ""
                : texts.get(haggleName(shownSale.getHaggle())));
        if (goods == null) {
            showText(saleNotice, texts.get(Loc.nativeSaleNoGoods));
        } else if (!open) {
            showText(saleNotice, texts.get(Loc.nativeSaleNeedsExpedition));
        } else if (shownSale == null && shownGift == null) {
            showText(saleNotice, texts.get(Loc.nativeSaleNoLimit));
        } else {
            showText(saleNotice, "");
        }
        sellButton.setDisable(shownSale == null);
        giftButton.setDisable(shownGift == null);
        haggleButton.setDisable(shownSale == null
                || shownSale.getHaggle() != NativeSaleQuote.Haggle.OFFERED);
        sellButton.setText(texts.get(Loc.nativeSaleSellButton));
        giftButton.setText(texts.get(Loc.nativeSaleGiftButton));
        haggleButton.setText(texts.get(Loc.nativeSaleHaggleButton));
    }

    /**
     * Second stage of an expedition (R-0161): the offered goods the model
     * reports with their stock and Europe ask, the pieces and gold of the exact
     * purchase, the one request, and the buttons. Each button runs only after
     * this window has closed. The box is shown only while a purchase is
     * pending.
     */
    private VBox buildPurchaseBox(final boolean purchasing) {
        final List<GoodsType> available = market.getPurchase()
                .map(purchase -> purchase.getOffers().stream()
                        .filter(offer -> offer.getStock() > 0)
                        .map(NativeMarketView.PurchaseOffer::getGoods)
                        .collect(Collectors.toList()))
                .orElse(List.of());
        purchaseHeader.setId(PURCHASE_HEADER_ID);
        purchaseHeader.setWrapText(true);
        purchaseGoodsLabel.setWrapText(true);
        purchaseGoods.setId(PURCHASE_GOODS_ID);
        purchaseGoods.getItems().setAll(available);
        // A backed-out confirmation returns to the chosen offer; after the
        // request its offer is shown again, also when the chooser is reopened
        // or the game continued; otherwise the first goods lead.
        final Optional<GoodsType> keptGoods = keptPurchaseGoods()
                .filter(available::contains);
        if (keptGoods.isPresent()) {
            purchaseGoods.getSelectionModel().select(keptGoods.get());
        } else if (!available.isEmpty()) {
            purchaseGoods.getSelectionModel().selectFirst();
        }
        purchaseQuantityLabel.setWrapText(true);
        // The same whole-number slider as the sale: arrow keys step by one
        // piece.
        purchaseQuantity.setId(PURCHASE_QUANTITY_ID);
        purchaseQuantity.setSnapToTicks(true);
        purchaseQuantity.setMajorTickUnit(1);
        purchaseQuantity.setMinorTickCount(0);
        purchaseQuantity.setBlockIncrement(1);
        purchaseQuantity.setShowTickMarks(false);
        purchaseQuantity.setShowTickLabels(false);
        purchaseQuantity.setPrefWidth(160);
        purchaseQuantityValue.setId(PURCHASE_QUANTITY_VALUE_ID);
        resetPurchaseQuantity(
                keptGoods.isPresent() ? keptPurchaseQuantity() : 0);
        purchasePreview.setId(PURCHASE_PREVIEW_ID);
        purchasePreview.setWrapText(true);
        purchaseHaggleState.setId(PURCHASE_HAGGLE_STATE_ID);
        purchaseHaggleState.setWrapText(true);
        purchaseNotice.setId(PURCHASE_NOTICE_ID);
        purchaseNotice.setWrapText(true);
        buyButton.setId(PURCHASE_BUY_BUTTON_ID);
        buyButton.setOnAction(e -> {
            pendingPurchase = shownPurchase;
            followUp = FollowUp.BUY;
            close();
        });
        purchaseHaggleButton.setId(PURCHASE_HAGGLE_BUTTON_ID);
        purchaseHaggleButton.setOnAction(e -> {
            pendingPurchase = shownPurchase;
            followUp = FollowUp.PURCHASE_HAGGLE;
            close();
        });
        declineButton.setId(PURCHASE_DECLINE_BUTTON_ID);
        declineButton.setOnAction(e -> {
            followUp = FollowUp.DECLINE_PURCHASE;
            close();
        });
        purchaseGoods.valueProperty().addListener((observed, before, after) -> {
            resetPurchaseQuantity(0);
            refreshPurchase();
        });
        purchaseQuantity.valueProperty()
                .addListener((observed, before, after) -> {
                    if (Math.round(before.doubleValue()) != Math
                            .round(after.doubleValue())) {
                        refreshPurchase();
                    }
                });
        final HBox chooseRow = new HBox(8, purchaseGoodsLabel, purchaseGoods,
                purchaseQuantityLabel, purchaseQuantity, purchaseQuantityValue);
        chooseRow.setAlignment(Pos.CENTER_LEFT);
        final HBox commandRow = new HBox(8, buyButton, declineButton);
        final VBox box = new VBox(6, purchaseHeader, chooseRow, purchasePreview,
                purchaseHaggleState, purchaseNotice, commandRow,
                purchaseHaggleButton);
        box.setVisible(purchasing);
        box.setManaged(purchasing);
        return box;
    }

    /**
     * Start the pieces at the offer the request was made for, clamped to what
     * the purchase may take now, or at the most it may take when there is no
     * such offer; the range is at least one piece. A quote stays the model's
     * word, so a clamped amount is still checked on confirmation.
     *
     * @param preferred pieces shown with the request, or zero for none
     */
    private void resetPurchaseQuantity(final int preferred) {
        final GoodsType goods = purchaseGoods.getValue();
        final int most = goods == null ? 0
                : saleTerms.maxPurchaseQuantity(goods);
        final int limit = Math.max(1, most);
        // The minimum stays one; a lower maximum first clamps the value.
        purchaseQuantity.setMin(1);
        purchaseQuantity.setMax(limit);
        purchaseQuantity
                .setValue(preferred > 0 ? Math.min(preferred, limit) : limit);
    }

    /**
     * @return whole pieces the purchase slider names now, within its range
     */
    private int purchaseQuantityNow() {
        return (int) Math.round(purchaseQuantity.getValue());
    }

    private boolean isPurchaseOpen() {
        return market.getPurchase().map(NativeMarketView.Purchase::isOpen)
                .orElse(false);
    }

    /**
     * Recompute the shown purchase from the model and enable only the buttons
     * whose terms are legal now. Called from the controls and from the language
     * refresh, never from a model change.
     */
    private void refreshPurchase() {
        if (texts == null || market.getPurchase().isEmpty()) {
            // Not shown yet, or no stage is pending; the refresh fills it in.
            return;
        }
        final GoodsType goods = purchaseGoods.getValue();
        final int quantity = purchaseQuantityNow();
        purchaseQuantityValue
                .setText(goods == null ? "" : String.valueOf(quantity));
        shownPurchase = goods == null || !isPurchaseOpen() ? null
                : saleTerms.purchaseQuote(goods, quantity).orElse(null);
        if (shownPurchase == null) {
            showText(purchasePreview, "");
        } else {
            showText(purchasePreview,
                    texts.get(Loc.nativePurchasePreview, quantity,
                            texts.get(GoodsTypeName.getNameForGoodsType(goods)),
                            shownPurchase.getGold()));
        }
        showText(purchaseHaggleState, texts.get(purchaseHaggleName(
                market.getPurchase().orElseThrow().getHaggle())));
        if (goods == null) {
            showText(purchaseNotice, texts.get(Loc.nativePurchaseNoStock));
        } else if (!isPurchaseOpen()) {
            showText(purchaseNotice, texts.get(Loc.nativePurchaseNotOpen));
        } else if (shownPurchase == null) {
            showText(purchaseNotice, texts.get(Loc.nativePurchaseNoLimit));
        } else {
            showText(purchaseNotice, "");
        }
        buyButton.setDisable(shownPurchase == null);
        purchaseHaggleButton.setDisable(shownPurchase == null
                || shownPurchase.getHaggle() != NativeSaleQuote.Haggle.OFFERED);
    }

    /**
     * @return the purchase the player chose to buy or to request a discount
     *         for, shown when the follow-up is {@link FollowUp#BUY} or
     *         {@link FollowUp#PURCHASE_HAGGLE}
     */
    public NativePurchaseQuote getPendingPurchase() {
        return pendingPurchase;
    }

    /**
     * @return goods and pieces chosen in this chooser when it closed, for the
     *         caller to carry over after a backed-out confirmation
     */
    public Selection getSelection() {
        return new Selection(saleGoods.getValue(), quantity(),
                purchaseGoods.getValue(), purchaseQuantityNow());
    }

    private boolean keepsSale() {
        return kept != null && kept.saleGoods != null && kept.saleQuantity > 0;
    }

    private boolean keepsPurchase() {
        return kept != null && kept.purchaseGoods != null
                && kept.purchaseQuantity > 0;
    }

    /**
     * @return sale goods to show first: the carried-over choice, else the
     *         haggled offer the model keeps
     */
    private Optional<GoodsType> keptSaleGoods() {
        if (keepsSale()) {
            return Optional.of(kept.saleGoods);
        }
        return market.getMarket()
                .flatMap(NativeMarketView.Market::getHaggledGoods);
    }

    /** @return pieces belonging to {@link #keptSaleGoods()} */
    private int keptSaleQuantity() {
        if (keepsSale()) {
            return kept.saleQuantity;
        }
        return market.getMarket()
                .map(NativeMarketView.Market::getHaggledQuantity).orElse(0);
    }

    /**
     * @return purchase goods to show first: the carried-over choice, else the
     *         offer of the request the model keeps
     */
    private Optional<GoodsType> keptPurchaseGoods() {
        if (keepsPurchase()) {
            return Optional.of(kept.purchaseGoods);
        }
        return market.getPurchase()
                .flatMap(NativeMarketView.Purchase::getSelectedGoods);
    }

    /** @return pieces belonging to {@link #keptPurchaseGoods()} */
    private int keptPurchaseQuantity() {
        if (keepsPurchase()) {
            return kept.purchaseQuantity;
        }
        return market.getPurchase()
                .map(NativeMarketView.Purchase::getSelectedQuantity).orElse(0);
    }

    private static Loc purchaseHaggleName(final NativeSaleQuote.Haggle haggle) {
        switch (haggle) {
        case OFFERED:
            return Loc.nativePurchaseHaggleOffered;
        case ACCEPTED:
            return Loc.nativePurchaseHaggleAccepted;
        case REFUSED:
            return Loc.nativePurchaseHaggleRefused;
        default:
            throw new IllegalStateException("Unknown native purchase request");
        }
    }

    /**
     * @return converter that names goods in the language of the chooser
     */
    private static StringConverter<GoodsType> goodsConverter(final I18n i18n) {
        return new StringConverter<GoodsType>() {

            @Override
            public String toString(final GoodsType goods) {
                return goods == null ? ""
                        : i18n.get(GoodsTypeName.getNameForGoodsType(goods));
            }

            @Override
            public GoodsType fromString(final String text) {
                return null;
            }

        };
    }

    private boolean isTeachingAvailable() {
        return choices.stream().anyMatch(choice -> choice
                .getAction() == NativeVisitAction.TEACHING
                && choice.getLimit() == NativeVisitChoice.Limit.AVAILABLE);
    }

    private boolean isAdmitted() {
        return market.getAdmission() == NativeMarketView.Admission.ADMITTED;
    }

    /**
     * @return the explicit command chosen before the chooser closed, or
     *         {@link FollowUp#NONE} after closing, Escape or teaching
     */
    public FollowUp getFollowUp() {
        return followUp;
    }

    /**
     * @return the sale or gift the player chose to confirm, shown when the
     *         follow-up is {@link FollowUp#SELL} or {@link FollowUp#GIFT}
     */
    public NativeSaleQuote getPendingSale() {
        return pendingSale;
    }

    /**
     * Keyboard focus starts on the close button, so Enter or Escape leaves the
     * chooser without a mouse.
     */
    @Override
    public void showAndWait() {
        Platform.runLater(buttonBar.getButtonOk()::requestFocus);
        super.showAndWait();
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        texts = i18n;
        setTitle(i18n.get(Loc.nativeVisitTitle));
        intro.setText(i18n.get(Loc.nativeVisitIntro));
        final String offered = i18n
                .get(UnitTypes.getUnitName(settlement.getTeachableSkill()));
        skill.setText(i18n.get(Loc.nativeVisitSkillLine, offered));
        teach.setText(i18n.get(Loc.nativeVisitTeachButton));
        buttonBar.setButtonText(i18n.get(Loc.ok));
        for (int i = 0; i < choices.size(); i++) {
            final NativeVisitChoice choice = choices.get(i);
            choiceLabels.get(i)
                    .setText(i18n.get(Loc.nativeVisitLine,
                            i18n.get(actionName(choice.getAction())),
                            i18n.get(limitName(choice))));
        }
        contact.setText(
                i18n.get(market.isContacted() ? Loc.nativeMarketContactKnown
                        : Loc.nativeMarketContactNone));
        admission.setText(i18n.get(admissionName(market.getAdmission())));
        introduce.setText(i18n.get(Loc.nativeMarketIntroduceButton));
        openMarket.setText(i18n.get(Loc.nativeMarketOpenButton));
        expedition.setText(i18n
                .get(market.isExpeditionOpen() ? Loc.nativeMarketExpeditionOpen
                        : Loc.nativeMarketExpeditionClosed));
        market.getMarket().ifPresent(details -> {
            for (int slot = 0; slot < offerLabels.size(); slot++) {
                final Label label = offerLabels.get(slot);
                if (slot < details.getOffers().size()) {
                    final NativeMarketView.Offer offer = details.getOffers()
                            .get(slot);
                    label.setText(i18n.get(Loc.nativeMarketOfferLine,
                            i18n.get(GoodsTypeName
                                    .getNameForGoodsType(offer.getGoods())),
                            offer.getStock()));
                } else {
                    label.setText(i18n.get(Loc.nativeMarketOfferUnavailable));
                }
            }
            wanted.setText(i18n.get(Loc.nativeMarketWantedLine,
                    details.getWanted().stream()
                            .map(goods -> i18n.get(
                                    GoodsTypeName.getNameForGoodsType(goods)))
                            .collect(Collectors.joining(", "))));
            treasury.setText(i18n.get(Loc.nativeMarketTreasuryLine,
                    details.getTreasury()));
        });
        // A pending purchase shows its own offer lines, with the Europe ask.
        market.getPurchase().ifPresent(purchase -> {
            for (int slot = 0; slot < offerLabels.size(); slot++) {
                final Label label = offerLabels.get(slot);
                if (slot < purchase.getOffers().size()) {
                    final NativeMarketView.PurchaseOffer offer = purchase
                            .getOffers().get(slot);
                    label.setText(i18n.get(Loc.nativePurchaseOfferLine,
                            i18n.get(GoodsTypeName
                                    .getNameForGoodsType(offer.getGoods())),
                            offer.getStock(), offer.getAskPrice()));
                } else {
                    label.setText(i18n.get(Loc.nativeMarketOfferUnavailable));
                }
            }
        });
        saleGoodsLabel.setText(i18n.get(Loc.nativeSaleGoodsLabel));
        saleQuantityLabel.setText(i18n.get(Loc.nativeSaleQuantityLabel));
        saleGoods.setConverter(goodsConverter(i18n));
        purchaseHeader.setText(i18n.get(Loc.nativePurchaseHeader));
        purchaseGoodsLabel.setText(i18n.get(Loc.nativePurchaseGoodsLabel));
        purchaseQuantityLabel
                .setText(i18n.get(Loc.nativePurchaseQuantityLabel));
        purchaseGoods.setConverter(goodsConverter(i18n));
        buyButton.setText(i18n.get(Loc.nativePurchaseBuyButton));
        purchaseHaggleButton.setText(i18n.get(Loc.nativePurchaseHaggleButton));
        declineButton.setText(i18n.get(Loc.nativePurchaseDeclineButton));
        refreshSale();
        refreshPurchase();
    }

    private static Loc haggleName(final NativeSaleQuote.Haggle haggle) {
        switch (haggle) {
        case OFFERED:
            return Loc.nativeSaleHaggleOffered;
        case ACCEPTED:
            return Loc.nativeSaleHaggleAccepted;
        case REFUSED:
            return Loc.nativeSaleHaggleRefused;
        case UNAVAILABLE:
            return Loc.nativeSaleHaggleUnavailable;
        default:
            throw new IllegalStateException("Unknown native haggle state");
        }
    }

    private static Loc actionName(final NativeVisitAction action) {
        switch (action) {
        case SCOUT_CHIEF:
            return Loc.nativeVisitActionScoutChief;
        case TEACHING:
            return Loc.nativeVisitActionTeaching;
        case TRADE:
            return Loc.nativeVisitActionTrade;
        case TRIBUTE:
            return Loc.nativeVisitActionTribute;
        case MISSION:
            return Loc.nativeVisitActionMission;
        case ATTACK:
            return Loc.nativeVisitActionAttack;
        default:
            throw new IllegalStateException("Unknown native visit action");
        }
    }

    private static Loc limitName(final NativeVisitChoice choice) {
        switch (choice.getLimit()) {
        case ROLE_REFUSED:
            return Loc.nativeVisitLimitRoleRefused;
        case RELATION_REFUSED:
            return Loc.nativeVisitLimitRelationRefused;
        case NOT_YET_AVAILABLE:
            return Loc.nativeVisitLimitNotYetAvailable;
        case SETTLEMENT_OCCUPIED:
            return Loc.nativeVisitLimitSettlementOccupied;
        case AVAILABLE:
            return Loc.nativeVisitLimitAvailable;
        default:
            throw new IllegalStateException("Unknown native visit limit");
        }
    }

    private static Loc admissionName(
            final NativeMarketView.Admission admission) {
        switch (admission) {
        case ADMITTED:
            return Loc.nativeMarketAdmitted;
        case NOT_A_TRADER:
            return Loc.nativeMarketNotTrader;
        case NO_CONTACT:
            return Loc.nativeMarketNoContact;
        case NO_GOODS:
            return Loc.nativeMarketNoGoods;
        case SHIP_NOT_COASTAL:
            return Loc.nativeMarketShipNotCoastal;
        case RELATION_TENSE:
            return Loc.nativeMarketRelationTense;
        case SHIP_RELATION_UNEASY:
            return Loc.nativeMarketShipUneasy;
        default:
            throw new IllegalStateException("Unknown native market admission");
        }
    }

}
