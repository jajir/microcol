package org.microcol.gui.screen.market;

import org.microcol.gui.dialog.DialogEuropePriceChanged;
import org.microcol.gui.dialog.DialogGoodsTransferChanged;
import org.microcol.gui.dialog.DialogNotEnoughGold;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;
import org.microcol.gui.screen.europe.ChooseGoods;
import org.microcol.gui.util.ButtonsBarYesNo;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.UpdatableLanguage;
import org.microcol.i18n.I18n;
import org.microcol.model.CargoSlot;
import org.microcol.model.EuropePriceChangedException;
import org.microcol.model.EuropeTradeQuote;
import org.microcol.model.Goods;
import org.microcol.model.NotEnoughtGoldException;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Market component. This is main market component.
 */
public class MarketBuyPanel implements JavaFxComponent, UpdatableLanguage {

    final static int MIN_VALUE = 0;

    private final VBox mainPanel;

    private final Slider slider;

    private final I18n i18n;

    private final EventBus eventBus;

    private final GameModelController gameModelController;

    private final DialogNotEnoughGold dialogNotEnoughGold;

    private final DialogGoodsTransferChanged dialogGoodsTransferChanged;

    private final DialogEuropePriceChanged dialogEuropePriceChanged;

    private final Label labelHowMuch = new Label();

    private final Label labelPricePerPiece = new Label();

    private final Label labelPricePerPieceValue = new Label();

    private final Label labelSelected = new Label();

    private final Label labelSelectedValue = new Label();

    private final Label labelKingTax = new Label();

    private final Label labelKingTaxValue = new Label();

    private final Label labelTotal = new Label();

    private final Label labelTotalValue = new Label();

    private final ButtonsBarYesNo buttonsBar;

    private Goods actualValue;

    /** Quote shown for the actual value; confirmation must match it. */
    private EuropeTradeQuote shownQuote;

    private ScreenMarketBuyContext screenMarketContext;

    @Inject
    MarketBuyPanel(final I18n i18n, final EventBus eventBus,
            final GameModelController gameModelController,
            final DialogNotEnoughGold dialogNotEnoughGold,
            final DialogGoodsTransferChanged dialogGoodsTransferChanged,
            final DialogEuropePriceChanged dialogEuropePriceChanged) {
        this.dialogEuropePriceChanged = Preconditions
                .checkNotNull(dialogEuropePriceChanged);
        this.dialogGoodsTransferChanged = Preconditions
                .checkNotNull(dialogGoodsTransferChanged);
        this.i18n = Preconditions.checkNotNull(i18n);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
        this.dialogNotEnoughGold = Preconditions
                .checkNotNull(dialogNotEnoughGold);

        labelPricePerPieceValue.setId("marketUnitPrice");
        labelTotalValue.setId("marketTotal");

        mainPanel = new VBox();
        mainPanel.getStyleClass().add("mainPanel");

        slider = new Slider();
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

        mainPanel.getChildren().addAll(labelHowMuch, slider, row1.getContent(),
                row2.getContent(), row3.getContent(), row4.getContent());
    }

    private int getKingsTaxPercentage() {
        return gameModelController.getKingsTaxPercentage();
    }

    @SuppressWarnings("unused")
    private void onUserClickCancel(final ActionEvent event) {
        eventBus.post(new ShowScreenEvent(Screen.EUROPE));
    }

    @SuppressWarnings("unused")
    private void onUserClickBuy(final ActionEvent event) {
        final CargoSlot targetCargoSlot = screenMarketContext
                .getTargetCargoSlot();
        try {
            if (getActualValue().isZero()) {
                // nothing selected, nothing to buy
                eventBus.post(new ShowScreenEvent(Screen.EUROPE));
                return;
            }
            if (isOfferStillValid(targetCargoSlot)) {
                targetCargoSlot.buyAndStoreFromEuropePort(getActualValue(),
                        shownQuote);
            } else {
                dialogGoodsTransferChanged.showAndWait();
            }
        } catch (NotEnoughtGoldException e) {
            dialogNotEnoughGold.showAndWait();
        } catch (EuropePriceChangedException e) {
            // the price moved after the quote was shown, nothing was charged
            dialogEuropePriceChanged.showAndWait();
        } catch (IllegalArgumentException | IllegalStateException e) {
            // hold changed since the quantity was chosen, nothing was charged
            dialogGoodsTransferChanged.showAndWait();
        }
        eventBus.post(new ShowScreenEvent(Screen.EUROPE));
    }

    /**
     * The chosen quantity must still fit exactly into the target hold; a
     * different (clamped) quantity is never bought silently.
     */
    private boolean isOfferStillValid(final CargoSlot targetCargoSlot) {
        final Goods chosen = getActualValue();
        return targetCargoSlot.getGoods()
                .map(stored -> stored.getType().equals(chosen.getType()))
                .orElse(true)
                && targetCargoSlot.getAvailableCapacity() >= chosen.getAmount();
    }

    void init(final ScreenMarketBuyContext screenMarketContext) {
        this.screenMarketContext = Preconditions
                .checkNotNull(screenMarketContext);
        actualValue = screenMarketContext.getMaxPossibleGoodsToBuy();
        slider.setMax(actualValue.getAmount());
        slider.setValue(actualValue.getAmount());
        updateSelectedAmount();
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        labelHowMuch.setText(i18n.get(Market.howMuch));
        buttonsBar.getButtonYes().setText(i18n.get(Market.buttonBuy));
        buttonsBar.getButtonNo().setText(i18n.get(Market.buttonCancel));
    }

    private void updateSelectedAmount() {
        shownQuote = gameModelController.getHumanPlayer()
                .quoteEuropeTrade(getActualValue(), true);
        labelPricePerPiece.setText(i18n.get(Market.pricePerPiece,
                getPricePerPiece(), getPriceWithoutTax()));
        labelPricePerPieceValue
                .setText(i18n.get(Market.price, getPricePerPiece()));
        labelSelected
                .setText(i18n.get(Market.selected, actualValue.getAmount()));
        labelSelectedValue
                .setText(i18n.get(Market.price, getPriceWithoutTax()));
        labelKingTax.setText(i18n.get(Market.tax, getKingsTaxPercentage()));
        labelKingTaxValue.setText(i18n.get(Market.price, getKingsTaxValue()));
        labelTotal.setText(i18n.get(Market.total));
        labelTotalValue.setText(i18n.get(Market.price, getTotal()));
    }

    private int getPricePerPiece() {
        return gameModelController
                .getEuropeGoodsTradeForType(getActualValue().getType())
                .getBuyPrice();
    }

    private int getPriceWithoutTax() {
        return gameModelController.getHumanPlayer()
                .quoteEuropeTrade(getActualValue(), true).getGross();
    }

    private int getKingsTaxValue() {
        return getTotal() - getPriceWithoutTax();
    }

    private int getTotal() {
        return gameModelController.getHumanPlayer()
                .quoteEuropeTrade(getActualValue(), true).getTotal();
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
        int v = value.intValue();
        if (MIN_VALUE == value) {
            return i18n.get(ChooseGoods.zero);
        } else if (screenMarketContext
                .getAmountOfMaxPossibleGoodsToBuy() == value) {
            return i18n.get(ChooseGoods.everything);
        } else if (v % 10 == 0) {
            return String.valueOf(v);
        }
        return null;
    }
}
