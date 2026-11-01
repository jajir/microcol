package org.microcol.gui.screen.market;

import org.microcol.gui.dialog.DialogEuropeGoldChanged;
import org.microcol.gui.dialog.DialogEuropePriceChanged;
import org.microcol.gui.dialog.DialogEuropeTradeChanged;
import org.microcol.gui.dialog.DialogGoodsTransferChanged;
import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;
import org.microcol.gui.screen.europe.ChooseGoods;
import org.microcol.gui.screen.europe.QuickBuyShortcut;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.microcol.gui.util.ButtonsBarYesNo;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.UpdatableLanguage;
import org.microcol.i18n.I18n;
import org.microcol.model.EuropePurchaseChangedException;
import org.microcol.model.EuropePurchaseLimit;
import org.microcol.model.EuropeTradeQuote;
import org.microcol.model.Goods;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Market component. This is main market component.
 * <p>
 * Every amount, price and limit comes from the model purchase snapshot; the
 * confirmation buys exactly the shown amount at the shown totals.
 */
public class MarketBuyPanel implements JavaFxComponent, UpdatableLanguage {

    final static int MIN_VALUE = 0;

    private final VBox mainPanel;

    private final Slider slider;

    private final I18n i18n;

    private final EventBus eventBus;

    private final QuickBuyShortcut quickBuyShortcut;

    private final DialogGoodsTransferChanged dialogGoodsTransferChanged;

    private final DialogEuropePriceChanged dialogEuropePriceChanged;

    private final DialogEuropeGoldChanged dialogEuropeGoldChanged;

    private final DialogEuropeTradeChanged dialogEuropeTradeChanged;

    private final Label labelHowMuch = new Label();

    private final Label labelLimit = new Label();

    private final Label labelPricePerPiece = new Label();

    private final Label labelPricePerPieceValue = new Label();

    private final Label labelSelected = new Label();

    private final Label labelSelectedValue = new Label();

    private final Label labelKingTax = new Label();

    private final Label labelKingTaxValue = new Label();

    private final Label labelTotal = new Label();

    private final Label labelTotalValue = new Label();

    private final Label labelRemaining = new Label();

    private final Label labelRemainingValue = new Label();

    private final Label labelQuickBuyHint = new Label();

    private final ButtonsBarYesNo buttonsBar;

    private Goods actualValue;

    private ScreenMarketBuyContext screenMarketContext;

    @Inject
    MarketBuyPanel(final I18n i18n, final EventBus eventBus,
            final QuickBuyShortcut quickBuyShortcut,
            final DialogGoodsTransferChanged dialogGoodsTransferChanged,
            final DialogEuropePriceChanged dialogEuropePriceChanged,
            final DialogEuropeGoldChanged dialogEuropeGoldChanged,
            final DialogEuropeTradeChanged dialogEuropeTradeChanged) {
        this.dialogEuropePriceChanged = Preconditions
                .checkNotNull(dialogEuropePriceChanged);
        this.dialogGoodsTransferChanged = Preconditions
                .checkNotNull(dialogGoodsTransferChanged);
        this.dialogEuropeGoldChanged = Preconditions
                .checkNotNull(dialogEuropeGoldChanged);
        this.dialogEuropeTradeChanged = Preconditions
                .checkNotNull(dialogEuropeTradeChanged);
        this.i18n = Preconditions.checkNotNull(i18n);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.quickBuyShortcut = Preconditions.checkNotNull(quickBuyShortcut);

        labelHowMuch.setId("marketHowMuch");
        labelLimit.setId("marketLimit");
        labelPricePerPiece.setId("marketUnitPriceCaption");
        labelPricePerPieceValue.setId("marketUnitPrice");
        labelSelected.setId("marketAmountCaption");
        labelSelectedValue.setId("marketGross");
        labelKingTax.setId("marketTaxCaption");
        labelKingTaxValue.setId("marketTax");
        labelTotal.setId("marketTotalCaption");
        labelTotalValue.setId("marketTotal");
        labelRemaining.setId("marketRemainingCaption");
        labelRemainingValue.setId("marketRemaining");
        labelQuickBuyHint.setId("marketQuickBuyHint");
        // Long translations wrap in a narrow window instead of being cut.
        for (final Label label : new Label[] { labelHowMuch, labelLimit,
                labelPricePerPiece, labelSelected, labelKingTax, labelTotal,
                labelRemaining, labelQuickBuyHint }) {
            label.setWrapText(true);
        }

        mainPanel = new VBox();
        mainPanel.getStyleClass().add("mainPanel");

        slider = new Slider();
        slider.setId("marketSlider");
        slider.setLabelFormatter(
                new SimpleStringConverter(this::converToString));
        slider.setMin(MIN_VALUE);
        slider.setSnapToTicks(false);
        slider.setBlockIncrement(1);
        slider.setMajorTickUnit(10);
        slider.setMinorTickCount(1);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(false);

        slider.valueProperty().addListener((obj, oldValue, newValue) -> {
            actualValue = Goods.of(actualValue.getType(), newValue.intValue());
            updateSelectedAmount();
        });

        buttonsBar = new ButtonsBarYesNo(i18n);
        buttonsBar.getButtonNo().setOnAction(this::onUserClickCancel);
        buttonsBar.getButtonYes().setOnAction(this::onUserClickBuy);
        buttonsBar.getButtonYes().requestFocus();

        final Row row1 = new Row(labelPricePerPiece, labelPricePerPieceValue);
        final Row row2 = new Row(labelSelected, labelSelectedValue);
        final Row row3 = new Row(labelKingTax, labelKingTaxValue);
        final Row row4 = new Row(labelTotal, labelTotalValue);
        final Row row5 = new Row(labelRemaining, labelRemainingValue);

        mainPanel.getChildren().addAll(labelHowMuch, labelLimit, slider,
                row1.getContent(), row2.getContent(), row3.getContent(),
                row4.getContent(), row5.getContent(), labelQuickBuyHint);
        // The quantity takes the keyboard focus whenever the choice is shown.
        mainPanel.sceneProperty()
                .addListener((property, oldScene, newScene) -> {
                    if (newScene != null) {
                        Platform.runLater(slider::requestFocus);
                    }
                });
    }

    @SuppressWarnings("unused")
    private void onUserClickCancel(final ActionEvent event) {
        eventBus.post(new ShowScreenEvent(Screen.EUROPE));
    }

    @SuppressWarnings("unused")
    private void onUserClickBuy(final ActionEvent event) {
        final int amount = getActualValue().getAmount();
        if (amount == MIN_VALUE) {
            // zero can't be confirmed, the Buy button is disabled
            return;
        }
        final EuropePurchaseLimit limit = screenMarketContext.getLimit();
        try {
            final Goods bought = screenMarketContext.getTargetCargoSlot()
                    .buyFromEuropePort(limit, amount);
            eventBus.post(new ShowScreenEvent(Screen.EUROPE));
            eventBus.post(
                    StatusBarMessageEvent.outcome(
                            MarketPurchaseText.bought(i18n, bought,
                                    limit.quote(amount).getTotal()),
                            Source.EUROPE));
            return;
        } catch (EuropePurchaseChangedException e) {
            // the shown offer is stale, nothing was charged
            showChange(e.getReason());
        } catch (IllegalArgumentException | IllegalStateException e) {
            // hold changed since the quantity was chosen, nothing was charged
            dialogGoodsTransferChanged.showAndWait();
        }
        eventBus.post(new ShowScreenEvent(Screen.EUROPE));
    }

    private void showChange(
            final EuropePurchaseChangedException.Reason reason) {
        switch (reason) {
        case TRADE:
            dialogEuropeTradeChanged.showAndWait();
            break;
        case HOLD:
            dialogGoodsTransferChanged.showAndWait();
            break;
        case PRICE:
            dialogEuropePriceChanged.showAndWait();
            break;
        case GOLD:
            dialogEuropeGoldChanged.showAndWait();
            break;
        default:
            throw new IllegalArgumentException("Unknown change " + reason);
        }
    }

    void init(final ScreenMarketBuyContext screenMarketContext) {
        this.screenMarketContext = Preconditions
                .checkNotNull(screenMarketContext);
        actualValue = screenMarketContext.getMaxPossibleGoodsToBuy();
        slider.setMax(actualValue.getAmount());
        slider.setValue(actualValue.getAmount());
        labelLimit.setText(MarketPurchaseText.limitReason(i18n,
                screenMarketContext.getLimit()));
        updateSelectedAmount();
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        labelHowMuch.setText(i18n.get(Market.howMuch));
        slider.setAccessibleText(i18n.get(Market.howMuch));
        labelQuickBuyHint
                .setText(i18n.get(Market.quickBuyHint, MarketPurchaseText
                        .quickBuyKey(i18n, quickBuyShortcut.isMac())));
        labelQuickBuyHint.setAccessibleText(
                i18n.get(Market.quickBuyHint, MarketPurchaseText
                        .quickBuyKeySpoken(i18n, quickBuyShortcut.isMac())));
        buttonsBar.getButtonYes().setText(i18n.get(Market.buttonBuy));
        buttonsBar.getButtonNo().setText(i18n.get(Market.buttonCancel));
        if (screenMarketContext != null) {
            labelLimit.setText(MarketPurchaseText.limitReason(i18n,
                    screenMarketContext.getLimit()));
            updateSelectedAmount();
        }
    }

    private void updateSelectedAmount() {
        final EuropePurchaseLimit limit = screenMarketContext.getLimit();
        final int amount = actualValue.getAmount();
        final EuropeTradeQuote quote = limit.quote(amount);
        labelPricePerPiece.setText(i18n.get(Market.pricePerPiece));
        labelPricePerPieceValue
                .setText(i18n.get(Market.price, quote.getUnitPrice()));
        labelSelected.setText(i18n.get(Market.selected, amount));
        labelSelectedValue.setText(i18n.get(Market.price, quote.getGross()));
        labelKingTax.setText(i18n.get(Market.tax, quote.getTaxPercentage()));
        labelKingTaxValue.setText(i18n.get(Market.price, quote.getTax()));
        labelTotal.setText(i18n.get(Market.total));
        labelTotalValue.setText(i18n.get(Market.price, quote.getTotal()));
        labelRemaining.setText(i18n.get(Market.remaining));
        labelRemainingValue
                .setText(i18n.get(Market.price, limit.getGoldAfter(amount)));
        buttonsBar.getButtonYes().setDisable(amount == MIN_VALUE);
    }

    Region getToolbar() {
        return buttonsBar.getContent();
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

    public Goods getActualValue() {
        return actualValue;
    }

    private String converToString(final Double value) {
        final int v = value.intValue();
        final int maximum = screenMarketContext
                .getAmountOfMaxPossibleGoodsToBuy();
        if (MIN_VALUE == value) {
            return i18n.get(ChooseGoods.zero);
        } else if (maximum == value) {
            return i18n.get(Market.maximum, maximum);
        } else if (v % 10 == 0 && (maximum - v) * 4 >= maximum) {
            // Round labels close to the end would overlap the maximum.
            return String.valueOf(v);
        }
        return null;
    }
}
