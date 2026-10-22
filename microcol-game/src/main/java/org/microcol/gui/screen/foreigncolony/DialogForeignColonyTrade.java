package org.microcol.gui.screen.foreigncolony;

import java.util.ArrayList;
import java.util.List;

import org.microcol.gui.GoodsTypeName;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.Colony;
import org.microcol.model.ForeignColonyTradeProblem;
import org.microcol.model.ForeignColonyTradeQuote;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Unit;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 * Sale or barter with an adjacent foreign colony (R-0167). The preview is the
 * model's quote for the chosen goods and quantities; confirming passes that
 * exact quote back, and the model refuses it if anything changed. Cancelling or
 * closing the window moves nothing, and the colony is never entered.
 * <p>
 * Every quantity limit comes from the model. The outgoing limit depends on the
 * goods given in the same barter, so it is recomputed whenever those change.
 */
public final class DialogForeignColonyTrade extends AbstractMessageWindow {

    private final GameModelController controller;
    private final I18n i18n;
    private final Unit visitor;
    private final Colony colony;
    private final CheckBox barter = new CheckBox();
    /** Ranges are kept for the whole life of the window and only resized. */
    private final SpinnerValueFactory.IntegerSpinnerValueFactory incomingRange = new SpinnerValueFactory.IntegerSpinnerValueFactory(
            1, 1, 1);
    private final SpinnerValueFactory.IntegerSpinnerValueFactory outgoingRange = new SpinnerValueFactory.IntegerSpinnerValueFactory(
            1, 1, 1);
    private final ComboBox<GoodsType> incomingGoods = new ComboBox<>();
    private final Spinner<Integer> incomingAmount = new Spinner<>(
            incomingRange);
    private final ComboBox<GoodsType> outgoingGoods = new ComboBox<>();
    private final Spinner<Integer> outgoingAmount = new Spinner<>(
            outgoingRange);
    private final Label giveCaption = new Label();
    private final Label receiveCaption = new Label();
    private final Label give = new Label();
    private final Label receive = new Label();
    private final Label prices = new Label();
    private final Label net = new Label();
    private final Label feedback = new Label();
    private final Button confirm = new Button();
    private final Button cancel = new Button();
    private ForeignColonyTradeQuote quote;
    /** Set while limits are resized, so their own value events do not nest. */
    private boolean limiting;
    private boolean traded;

    public DialogForeignColonyTrade(final ViewUtil viewUtil, final I18n i18n,
            final GameModelController controller, final Unit visitor,
            final Colony colony) {
        super(viewUtil, i18n);
        this.i18n = i18n;
        this.controller = controller;
        this.visitor = visitor;
        this.colony = colony;
        final VBox root = new VBox(10);
        root.setId("foreignTradeDialog");
        root.setPadding(new Insets(20));
        root.setPrefWidth(560);
        barter.setId("foreignTradeBarter");
        incomingGoods.setId("foreignTradeGiveGoods");
        incomingAmount.setId("foreignTradeGiveAmount");
        outgoingGoods.setId("foreignTradeReceiveGoods");
        outgoingAmount.setId("foreignTradeReceiveAmount");
        for (final ComboBox<GoodsType> box : List.of(incomingGoods,
                outgoingGoods)) {
            box.setConverter(goodsConverter());
        }
        barter.setOnAction(event -> onBarterChanged());
        incomingGoods.valueProperty()
                .addListener((observable, before, after) -> onGoodsChanged());
        outgoingGoods.valueProperty()
                .addListener((observable, before, after) -> onGoodsChanged());
        incomingAmount.valueProperty()
                .addListener((observable, before, after) -> onAmountChanged());
        outgoingAmount.valueProperty()
                .addListener((observable, before, after) -> refreshQuote());
        for (final Label label : List.of(give, receive, prices, net,
                feedback)) {
            label.setWrapText(true);
        }
        give.setId("foreignTradeGive");
        receive.setId("foreignTradeReceive");
        prices.setId("foreignTradePrices");
        net.setId("foreignTradeNet");
        feedback.setId("foreignTradeFeedback");
        confirm.setId("foreignTradeConfirm");
        cancel.setId("foreignTradeCancel");
        confirm.setOnAction(event -> onConfirm());
        cancel.setCancelButton(true);
        cancel.setOnAction(event -> close());
        root.getChildren().addAll(barter, giveCaption, incomingGoods,
                incomingAmount, receiveCaption, outgoingGoods, outgoingAmount,
                give, receive, prices, net, feedback,
                new HBox(12, confirm, cancel));
        init(root);
    }

    @Override
    public void showAndWait() {
        refresh();
        super.showAndWait();
    }

    /** @return whether the player confirmed a trade in this window */
    public boolean isTraded() {
        return traded;
    }

    private void refresh() {
        setTitle(i18n.get(ForeignColonyTradeText.title, colony.getName()));
        barter.setText(i18n.get(ForeignColonyTradeText.barter));
        giveCaption.setText(i18n.get(ForeignColonyTradeText.giveLabel));
        receiveCaption.setText(i18n.get(ForeignColonyTradeText.receiveLabel));
        confirm.setText(i18n.get(ForeignColonyTradeText.confirm));
        cancel.setText(i18n.get(ForeignColonyTradeText.cancel));
        feedback.setText("");
        outgoingGoods.setDisable(!barter.isSelected());
        outgoingAmount.setDisable(!barter.isSelected());
        final List<GoodsType> carried = new ArrayList<>();
        final List<GoodsType> stocked = new ArrayList<>();
        for (final GoodsType type : GoodsType.GOOD_TYPES) {
            if (controller.getModel().getForeignColonyTradeLimit(visitor,
                    colony, type, true) > 0) {
                carried.add(type);
            }
            // The stock alone decides: room may be freed by the given goods.
            if (controller.getModel().getForeignColonyStock(colony, type) > 0) {
                stocked.add(type);
            }
        }
        incomingGoods.getItems().setAll(carried);
        incomingGoods.setValue(carried.isEmpty() ? null : carried.get(0));
        outgoingGoods.getItems().setAll(stocked);
        outgoingGoods.setValue(stocked.isEmpty() ? null : stocked.get(0));
        updateLimits();
        refreshQuote();
    }

    private void onBarterChanged() {
        outgoingGoods.setDisable(!barter.isSelected());
        outgoingAmount.setDisable(!barter.isSelected());
        refreshQuote();
    }

    private void onGoodsChanged() {
        updateLimits();
        refreshQuote();
    }

    private void onAmountChanged() {
        if (!limiting) {
            updateLimits();
        }
        refreshQuote();
    }

    /**
     * Replace both spinner ranges with the model's limits, keeping each shown
     * amount when it still fits. The outgoing limit uses the amount given now.
     */
    private void updateLimits() {
        final GoodsType given = incomingGoods.getValue();
        final GoodsType received = outgoingGoods.getValue();
        limiting = true;
        try {
            limit(incomingRange,
                    given == null ? 0
                            : controller.getModel().getForeignColonyTradeLimit(
                                    visitor, colony, given, true));
            limit(outgoingRange, given == null || received == null ? 0
                    : controller.getModel().getForeignColonyReceiveLimit(
                            visitor, colony,
                            Goods.of(given, incomingAmount.getValue()),
                            received));
        } finally {
            limiting = false;
        }
    }

    /** Resize a range to the model's limit, keeping the amount when it fits. */
    private static void limit(
            final SpinnerValueFactory.IntegerSpinnerValueFactory range,
            final int max) {
        final int top = Math.max(1, max);
        range.setMax(top);
        if (range.getValue() > top) {
            range.setValue(top);
        }
    }

    private void refreshQuote() {
        final GoodsType given = incomingGoods.getValue();
        final GoodsType received = outgoingGoods.getValue();
        if (given == null || barter.isSelected() && received == null) {
            quote = null;
        } else if (barter.isSelected()) {
            quote = controller.getModel().quoteForeignColonyBarter(visitor,
                    colony, Goods.of(given, incomingAmount.getValue()),
                    Goods.of(received, outgoingAmount.getValue()));
        } else {
            quote = controller.getModel().quoteForeignColonySale(visitor,
                    colony, Goods.of(given, incomingAmount.getValue()));
        }
        showQuote();
    }

    private void showQuote() {
        if (quote == null) {
            give.setText("");
            receive.setText("");
            prices.setText("");
            net.setText("");
            // Refused access is explained first; the goods lists are then
            // empty.
            final ForeignColonyTradeProblem access = controller.getModel()
                    .getForeignColonyTradeAccess(visitor, colony);
            feedback.setText(access == ForeignColonyTradeProblem.NONE
                    ? i18n.get(ForeignColonyTradeText.noGoods)
                    : i18n.get(textFor(access)));
            confirm.setDisable(true);
            return;
        }
        give.setText(i18n.get(ForeignColonyTradeText.give,
                quote.getIncoming().getAmount(), i18n.get(GoodsTypeName
                        .getNameForGoodsType(quote.getIncoming().getType()))));
        if (quote.isSale()) {
            receive.setText(i18n.get(ForeignColonyTradeText.nothing));
            prices.setText("");
        } else {
            receive.setText(i18n.get(ForeignColonyTradeText.receive,
                    quote.getOutgoing().getAmount(),
                    i18n.get(GoodsTypeName.getNameForGoodsType(
                            quote.getOutgoing().getType()))));
            prices.setText(i18n.get(ForeignColonyTradeText.prices,
                    quote.getIncomingUnitPrice(),
                    quote.getOutgoingUnitPrice()));
        }
        // A refused quote carries no settled gold, so no balance is promised.
        if (!quote.isLegal()) {
            net.setText("");
        } else if (quote.getNetGold() > 0) {
            net.setText(i18n.get(ForeignColonyTradeText.netHost,
                    quote.getNetGold()));
        } else if (quote.getNetGold() < 0) {
            net.setText(i18n.get(ForeignColonyTradeText.netVisitor,
                    -quote.getNetGold()));
        } else {
            net.setText(i18n.get(ForeignColonyTradeText.netNone));
        }
        feedback.setText(
                quote.isLegal() ? "" : i18n.get(textFor(quote.getProblem())));
        confirm.setDisable(!quote.isLegal());
    }

    private static ForeignColonyTradeText textFor(
            final ForeignColonyTradeProblem problem) {
        if (problem == ForeignColonyTradeProblem.FOUNDER_ABSENT) {
            return ForeignColonyTradeText.founderAbsent;
        }
        if (problem == ForeignColonyTradeProblem.AT_WAR) {
            return ForeignColonyTradeText.atWar;
        }
        if (problem == ForeignColonyTradeProblem.EMPTY_CARGO) {
            return ForeignColonyTradeText.noGoods;
        }
        if (problem == ForeignColonyTradeProblem.HOST_GOLD) {
            return ForeignColonyTradeText.hostGold;
        }
        if (problem == ForeignColonyTradeProblem.VISITOR_GOLD) {
            return ForeignColonyTradeText.visitorGold;
        }
        if (problem == ForeignColonyTradeProblem.HOST_CAPACITY) {
            return ForeignColonyTradeText.hostCapacity;
        }
        if (problem == ForeignColonyTradeProblem.CARRIER_CAPACITY) {
            return ForeignColonyTradeText.carrierCapacity;
        }
        if (problem == ForeignColonyTradeProblem.HOST_STOCK) {
            return ForeignColonyTradeText.hostStock;
        }
        return ForeignColonyTradeText.refused;
    }

    private void onConfirm() {
        if (quote != null
                && controller.tradeWithForeignColony(visitor, colony, quote)) {
            traded = true;
            close();
            return;
        }
        // The model refused the offer as stale; show what is live now.
        refresh();
        feedback.setText(i18n.get(ForeignColonyTradeText.changed));
    }

    private StringConverter<GoodsType> goodsConverter() {
        return new StringConverter<GoodsType>() {
            @Override
            public String toString(final GoodsType type) {
                return type == null ? ""
                        : i18n.get(GoodsTypeName.getNameForGoodsType(type));
            }

            @Override
            public GoodsType fromString(final String text) {
                throw new UnsupportedOperationException(
                        "Goods are chosen from the list");
            }
        };
    }
}
