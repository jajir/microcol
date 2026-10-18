package org.microcol.gui.screen.europe;

import java.util.Optional;

import org.microcol.gui.GoodsTypeName;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.Model;
import org.microcol.model.Player;
import org.microcol.model.TaxDemand;
import org.microcol.model.TaxProtest;

import com.google.inject.Inject;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Shows the Crown's tax demand with its exact consequences. Closing the window
 * defers the answer; the model keeps the demand and the paused turn.
 */
public final class TaxDemandDialog extends AbstractMessageWindow {

    private final Label rates = new Label();
    private final Label acceptExplain = new Label();
    private final Label protestDetail = new Label();
    private final Label feedback = new Label();
    private final Label laterExplain = new Label();
    private final Button accept = new Button();
    private final Button protest = new Button();
    private final Button later = new Button();
    private final GameModelController controller;
    private final EuropeCallback europe;
    private final I18n i18n;
    private Player player;
    private Model openedModel;
    private TaxDemand demand;
    private boolean open;

    @Inject
    public TaxDemandDialog(final ViewUtil viewUtil, final I18n i18n,
            final GameModelController controller, final EuropeCallback europe) {
        super(viewUtil, i18n);
        this.i18n = i18n;
        this.controller = controller;
        this.europe = europe;
        final VBox root = new VBox(12);
        root.setId("taxDemandDialog");
        root.setPadding(new Insets(20));
        root.setPrefWidth(560);
        rates.setId("taxDemandRates");
        acceptExplain.setId("taxDemandAcceptExplain");
        protestDetail.setId("taxDemandProtestDetail");
        feedback.setId("taxDemandFeedback");
        laterExplain.setId("taxDemandLaterExplain");
        for (final Label label : new Label[] { rates, acceptExplain,
                protestDetail, feedback, laterExplain }) {
            label.setWrapText(true);
        }
        accept.setId("taxDemandAccept");
        protest.setId("taxDemandProtest");
        later.setId("taxDemandLater");
        accept.setOnAction(event -> onAccept());
        protest.setOnAction(event -> onProtest());
        later.setCancelButton(true);
        later.setOnAction(event -> close());
        root.getChildren().addAll(rates, acceptExplain, protestDetail, feedback,
                laterExplain, new HBox(12, accept, protest, later));
        init(root);
    }

    @Override
    public void showAndWait() {
        player = controller.getHumanPlayer();
        openedModel = controller.getModel();
        feedback.setText("");
        refresh();
        if (demand == null) {
            return;
        }
        try {
            open = true;
            super.showAndWait();
        } finally {
            open = false;
            player = null;
            openedModel = null;
            demand = null;
        }
    }

    public boolean isOpen() {
        return open;
    }

    private void refresh() {
        demand = player.getTaxDemand().orElse(null);
        if (demand == null) {
            return;
        }
        setTitle(i18n.get(TaxText.title));
        rates.setText(i18n.get(TaxText.rates, demand.getCurrentRate(),
                demand.getProposedRate()));
        acceptExplain.setText(
                i18n.get(TaxText.acceptExplain, demand.getProposedRate()));
        final Optional<TaxProtest> consequence = demand.getProtest();
        protestDetail.setText(consequence.map(this::describe)
                .orElse(i18n.get(TaxText.protestNone)));
        laterExplain.setText(i18n.get(TaxText.laterExplain));
        accept.setText(i18n.get(TaxText.accept));
        protest.setText(i18n.get(TaxText.protest));
        later.setText(i18n.get(TaxText.later));
        accept.setDisable(!player.canAcceptTaxDemand(demand));
        protest.setDisable(
                consequence.isEmpty() || !player.canProtestTaxDemand(demand));
    }

    private String describe(final TaxProtest consequence) {
        return i18n.get(TaxText.protestDetail, consequence.getQuantity(),
                i18n.get(GoodsTypeName
                        .getNameForGoodsType(consequence.getGoods())),
                consequence.getColonyName(), consequence.getStock(),
                consequence.getLibertyGain(), consequence.getArrears(),
                demand.getCurrentRate());
    }

    private void onAccept() {
        try {
            checkSameGame();
            player.acceptTaxDemand(demand);
        } catch (final IllegalStateException rejected) {
            reject(TaxText.changed);
            return;
        }
        europe.repaint();
        close();
    }

    private void onProtest() {
        try {
            checkSameGame();
            player.protestTaxDemand(demand);
        } catch (final IllegalStateException rejected) {
            reject(TaxText.protestUnavailable);
            return;
        }
        europe.repaint();
        close();
    }

    private void checkSameGame() {
        if (controller.getModel() != openedModel) {
            throw new IllegalStateException(
                    "The demand belongs to a replaced game");
        }
    }

    private void reject(final TaxText text) {
        refresh();
        if (demand == null) {
            close();
            return;
        }
        feedback.setText(i18n.get(text));
    }
}
