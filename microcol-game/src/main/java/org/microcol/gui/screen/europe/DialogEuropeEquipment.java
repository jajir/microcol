package org.microcol.gui.screen.europe;

import com.google.inject.Inject;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.GoodsTypeName;
import org.microcol.i18n.I18n;
import org.microcol.model.GoodsType;
import org.microcol.model.unit.UnitFreeColonist;
import org.microcol.model.unit.UnitFreeColonist.EquipmentProblem;
import org.microcol.model.EuropeTradeQuote;
import org.microcol.gui.util.ViewUtil;

import java.util.Optional;

/**
 * Confirm model-provided equipment costs and revalidate after player input.
 */
public final class DialogEuropeEquipment extends AbstractMessageWindow {

    private UnitFreeColonist unit;
    private GoodsType type;
    private boolean buy;
    private EuropeTradeQuote quote;

    private final Label caption;
    private final Label cost;
    private final Label message;
    private final Button confirm;
    private final Button cancel;

    private final I18n i18n;

    @Inject
    public DialogEuropeEquipment(ViewUtil viewUtil, I18n i18n) {
        super(viewUtil, i18n);
        this.i18n = i18n;

        this.caption = new Label();
        this.cost = new Label();
        this.message = new Label();
        this.confirm = new Button();
        this.cancel = new Button();

        VBox root = new VBox(12);
        root.setPrefWidth(500);
        root.setId("europeEquipmentDialog");

        caption.setId("europeEquipmentCaption");
        cost.setId("europeEquipmentCost");
        message.setId("europeEquipmentMessage");

        confirm.setId("europeEquipmentConfirm");
        cancel.setId("europeEquipmentCancel");

        HBox buttons = new HBox(8, confirm, cancel);
        buttons.setId("europeEquipmentButtons");

        root.getChildren().addAll(caption, cost, message, buttons);

        caption.setWrapText(true);
        cost.setWrapText(true);
        cost.setMinHeight(80);
        message.setWrapText(true);
        message.setMinHeight(100);

        confirm.setDefaultButton(true);
        confirm.setOnAction(e -> handleConfirm());
        cancel.setCancelButton(true);
        cancel.setOnAction(e -> close());

        init(root);
    }

    public void show(UnitFreeColonist unit, GoodsType type, boolean buy) {
        this.unit = unit;
        this.type = type;
        this.buy = buy;

        caption.setText(i18n.get(actionKey(type, buy)));
        confirm.setText(i18n.get(Europe.equipmentConfirm));
        cancel.setText(i18n.get(Europe.equipmentCancel));
        message.setText("");

        refresh();

        javafx.application.Platform.runLater(() -> {
            if (confirm.isDisabled()) {
                cancel.requestFocus();
            } else {
                confirm.requestFocus();
            }
        });

        showAndWait();
    }

    private void refresh() {
        Optional<EquipmentProblem> problemOpt = unit
                .getEuropeEquipmentProblem(type, buy);

        if (problemOpt.isPresent()
                && (problemOpt.get() == EquipmentProblem.UNIT_UNAVAILABLE
                        || problemOpt.get() == EquipmentProblem.GOLD_LIMIT)) {
            quote = null;
            cost.setText("");
            confirm.setDisable(true);
        } else {
            try {
                quote = unit.getEuropeEquipmentQuote(type, buy);
                String amount = String.valueOf(quote.getGoods().getAmount());
                String name = i18n.get(GoodsTypeName.getKey(type, 2, false));
                int unitPrice = quote.getUnitPrice();
                int taxPercent = quote.getTaxPercentage();
                int tax = quote.getTax();
                int total = quote.getTotal();
                cost.setText(i18n.get(Europe.equipmentQuote, amount, name,
                        unitPrice, taxPercent, tax, total));
                confirm.setDisable(problemOpt.isPresent());
            } catch (ArithmeticException ae) {
                // Should only happen if problem is GOLD_LIMIT which is already
                // handled.
                quote = null;
                cost.setText("");
                confirm.setDisable(true);
            }
        }

        message.setText(problemOpt
                .map(p -> i18n.get(Europe.valueOf("equipment" + p.name())))
                .orElse(""));
    }

    private void handleConfirm() {
        Optional<EquipmentProblem> problemOpt = unit
                .getEuropeEquipmentProblem(type, buy);
        if (problemOpt.isPresent()) {
            refresh();
            return;
        }

        EuropeTradeQuote freshQuote = unit.getEuropeEquipmentQuote(type, buy);
        if (!freshQuote.equals(quote)) {
            refresh();
            message.setText(i18n.get(Europe.equipmentChanged));
            return;
        }

        try {
            unit.tradeEquipmentInEurope(type, buy, quote);
            close();
        } catch (IllegalStateException e) {
            refresh();
            Optional<EquipmentProblem> newProblem = unit
                    .getEuropeEquipmentProblem(type, buy);
            if (newProblem.isPresent()) {
                message.setText(i18n.get(
                        Europe.valueOf("equipment" + newProblem.get().name())));
            } else {
                message.setText(i18n.get(Europe.equipmentChanged));
            }
        }
    }

    public static Europe actionKey(GoodsType type, boolean buy) {
        if (type == GoodsType.MUSKET) {
            return buy ? Europe.buyMuskets : Europe.sellMuskets;
        } else if (type == GoodsType.HORSE) {
            return buy ? Europe.buyHorses : Europe.sellHorses;
        } else if (type == GoodsType.TOOLS) {
            return buy ? Europe.buyTools : Europe.sellTools;
        } else {
            throw new IllegalArgumentException("Unsupported GoodsType");
        }
    }
}