package org.microcol.gui.screen.europe;

import java.util.List;

import org.microcol.gui.GoodsTypeName;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.GoodsType;
import org.microcol.model.Model;
import org.microcol.model.Player;
import org.microcol.model.TaxArrearsQuote;

import com.google.inject.Inject;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 * Pays the frozen back taxes of one boycotted commodity. Cancelling or closing
 * the window charges nothing; the model confirms the exact quote before gold
 * moves.
 */
public final class BackTaxDialog extends AbstractMessageWindow {

    private final Label commodityLabel = new Label();
    private final ComboBox<GoodsType> commodity = new ComboBox<>();
    private final Label arrears = new Label();
    private final Label restore = new Label();
    private final Label treasury = new Label();
    private final Label insufficient = new Label();
    private final Label feedback = new Label();
    private final Button pay = new Button();
    private final Button cancel = new Button();
    private final GameModelController controller;
    private final EuropeCallback europe;
    private final I18n i18n;
    private Player player;
    private Model openedModel;
    private TaxArrearsQuote quote;
    private boolean open;

    @Inject
    public BackTaxDialog(final ViewUtil viewUtil, final I18n i18n,
            final GameModelController controller, final EuropeCallback europe) {
        super(viewUtil, i18n);
        this.i18n = i18n;
        this.controller = controller;
        this.europe = europe;
        final VBox root = new VBox(12);
        root.setId("backTaxDialog");
        root.setPadding(new Insets(20));
        root.setPrefWidth(560);
        commodity.setId("backTaxCommodity");
        commodity.setConverter(new StringConverter<GoodsType>() {
            @Override
            public String toString(final GoodsType type) {
                return type == null ? ""
                        : i18n.get(GoodsTypeName.getNameForGoodsType(type));
            }

            @Override
            public GoodsType fromString(final String text) {
                throw new UnsupportedOperationException(
                        "The commodity is chosen from the list");
            }
        });
        commodity.valueProperty()
                .addListener((observable, before, after) -> refreshQuote());
        arrears.setId("backTaxArrears");
        restore.setId("backTaxRestore");
        treasury.setId("backTaxTreasury");
        insufficient.setId("backTaxInsufficient");
        feedback.setId("backTaxFeedback");
        for (final Label label : new Label[] { commodityLabel, arrears, restore,
                treasury, insufficient, feedback }) {
            label.setWrapText(true);
        }
        pay.setId("backTaxPay");
        cancel.setId("backTaxCancel");
        pay.setOnAction(event -> onPay());
        cancel.setCancelButton(true);
        cancel.setOnAction(event -> close());
        root.getChildren().addAll(commodityLabel, commodity, arrears, restore,
                treasury, insufficient, feedback, new HBox(12, pay, cancel));
        init(root);
    }

    @Override
    public void showAndWait() {
        player = controller.getHumanPlayer();
        openedModel = controller.getModel();
        feedback.setText("");
        refresh();
        if (player.getTaxArrears().isEmpty()) {
            return;
        }
        try {
            open = true;
            super.showAndWait();
        } finally {
            open = false;
            player = null;
            openedModel = null;
            quote = null;
        }
    }

    public boolean isOpen() {
        return open;
    }

    private void refresh() {
        setTitle(i18n.get(BackTaxText.title));
        commodityLabel.setText(i18n.get(BackTaxText.commodity));
        pay.setText(i18n.get(BackTaxText.pay));
        cancel.setText(i18n.get(BackTaxText.cancel));
        final List<GoodsType> owed = List
                .copyOf(player.getTaxArrears().keySet());
        final GoodsType selected = commodity.getValue();
        commodity.getItems().setAll(owed);
        if (selected != null && owed.contains(selected)) {
            commodity.setValue(selected);
        } else {
            commodity.setValue(owed.isEmpty() ? null : owed.get(0));
        }
        refreshQuote();
    }

    private void refreshQuote() {
        quote = player == null || commodity.getValue() == null ? null
                : player.quoteTaxArrears(commodity.getValue()).orElse(null);
        if (quote == null) {
            arrears.setText("");
            restore.setText("");
            treasury.setText("");
            insufficient.setText("");
            pay.setDisable(true);
            return;
        }
        arrears.setText(i18n.get(BackTaxText.arrears, quote.getAmount()));
        restore.setText(i18n.get(BackTaxText.restore,
                i18n.get(GoodsTypeName.getNameForGoodsType(quote.getGoods()))));
        treasury.setText(i18n.get(BackTaxText.treasury, player.getGold(),
                quote.getAmount()));
        final boolean shortOfGold = player.getGold() < quote.getAmount();
        insufficient.setText(shortOfGold
                ? i18n.get(BackTaxText.insufficient, quote.getAmount())
                : "");
        pay.setDisable(!player.canPayTaxArrears(quote));
    }

    private void onPay() {
        try {
            checkSameGame();
            player.payTaxArrears(quote);
        } catch (final IllegalStateException rejected) {
            reject();
            return;
        }
        europe.repaint();
        close();
    }

    private void checkSameGame() {
        if (controller.getModel() != openedModel) {
            throw new IllegalStateException(
                    "The back taxes belong to a replaced game");
        }
    }

    private void reject() {
        refresh();
        // Another command may have changed the live debts; the port marks must
        // follow the model even when this dialog stays open.
        europe.repaint();
        if (player.getTaxArrears().isEmpty()) {
            close();
            return;
        }
        feedback.setText(i18n.get(BackTaxText.changed));
    }
}
