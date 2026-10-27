package org.microcol.gui.dialog;

import org.microcol.gui.util.ActionButtons;
import org.microcol.gui.image.ButtonIcon;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.Colony;
import org.microcol.model.Model;
import org.microcol.model.TreasureRedemptionQuote;
import org.microcol.model.Unit;
import org.microcol.model.unit.UnitTreasure;
import com.google.inject.Inject;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

/**
 * Explicit confirmation of model-provided treasure proceeds; closing pays
 * nothing.
 */
public final class TreasureDialog extends AbstractMessageWindow {
    private final GameModelController controller;
    private final I18n i18n;

    @Inject
    public TreasureDialog(final ViewUtil viewUtil, final I18n i18n,
            final GameModelController controller) {
        super(viewUtil, i18n);
        this.controller = controller;
        this.i18n = i18n;
    }

    /** A null colony selects own-galleon redemption in Europe. */
    public void showFor(final Colony colony) {
        final Model model = controller.getModel();
        final VBox rows = new VBox(12);
        rows.setId("treasureRows");
        for (final Unit unit : controller.getHumanPlayer().getAllUnits()) {
            if (!(unit instanceof UnitTreasure)) {
                continue;
            }
            final UnitTreasure train = (UnitTreasure) unit;
            if (colony != null && Integer.valueOf(colony.getId())
                    .equals(train.getPickupColonyId())) {
                final Label pending = label(i18n.get(TreasureText.pending,
                        train.getId(), train.getPickupNet()));
                pending.setId("treasurePending-" + train.getId());
                rows.getChildren().add(pending);
            }
            model.quoteTreasureRedemption(unit, colony)
                    .ifPresent(quote -> addOffer(rows, model, quote));
        }
        if (rows.getChildren().isEmpty()) {
            rows.getChildren().add(label(i18n.get(TreasureText.empty)));
        }
        final ScrollPane scroll = new ScrollPane(rows);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportWidth(540);
        scroll.setPrefViewportHeight(260);
        final Button cancel = ActionButtons.button(ButtonIcon.CANCEL,
                i18n.get(TreasureText.cancel));
        cancel.setId("treasureCancel");
        cancel.setCancelButton(true);
        cancel.setOnAction(event -> close());
        final VBox root = new VBox(12,
                label(i18n.get(colony == null ? TreasureText.ownRule
                        : TreasureText.royalRule)),
                scroll, cancel);
        root.setPadding(new Insets(20));
        root.setId("treasureDialog");
        init(root);
        setTitle(i18n.get(TreasureText.title));
        showAndWait();
    }

    private void addOffer(final VBox rows, final Model model,
            final TreasureRedemptionQuote quote) {
        final Label terms = label(i18n.get(TreasureText.terms,
                quote.getUnitId(), quote.getFaceValue(), quote.getTransport(),
                quote.getTax(), quote.getNet()));
        terms.setId("treasureTerms-" + quote.getUnitId());
        final Button confirm = ActionButtons.button(ButtonIcon.GOLD,
                i18n.get(TreasureText.confirm));
        confirm.setId("treasureConfirm-" + quote.getUnitId());
        final Label feedback = label("");
        feedback.setId("treasureFeedback-" + quote.getUnitId());
        confirm.setOnAction(event -> {
            try {
                if (controller.getModel() != model) {
                    throw new IllegalStateException("World changed");
                }
                model.redeemTreasure(quote);
                feedback.setText(
                        i18n.get(quote.isRoyalPickup() ? TreasureText.scheduled
                                : TreasureText.paid, quote.getNet()));
            } catch (IllegalStateException
                    | IllegalArgumentException rejected) {
                feedback.setText(i18n.get(TreasureText.changed));
            }
            confirm.setDisable(true);
        });
        rows.getChildren().add(new VBox(6, terms, confirm, feedback));
    }

    private Label label(final String text) {
        final Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(530);
        return label;
    }
}
