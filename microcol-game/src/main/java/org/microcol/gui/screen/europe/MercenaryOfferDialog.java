package org.microcol.gui.screen.europe;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.Colony;
import org.microcol.model.MercenaryOffer;
import org.microcol.model.Model;
import org.microcol.model.Player;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 * Shows a paid veteran mercenary offer with its issuer, full price and the
 * owned port that receives the soldiers. Accepting pays once. Declining and
 * closing never charge; closing keeps the offer for the rest of the turn.
 */
public final class MercenaryOfferDialog extends AbstractMessageWindow {
    private final GameModelController game;
    private final Model model;
    private final MercenaryOffer offer;
    private final I18n i18n;
    private final ComboBox<Colony> port = new ComboBox<>();
    private final Label hint = new Label();
    private final Label feedback = new Label();
    private final Button accept = new Button();
    private final Button decline = new Button();
    private final Button later = new Button();

    public MercenaryOfferDialog(final ViewUtil viewUtil, final I18n i18n,
            final GameModelController game, final Model model,
            final MercenaryOffer offer) {
        super(viewUtil, i18n);
        this.game = game;
        this.model = model;
        this.offer = offer;
        this.i18n = i18n;
        setTitle(i18n.get(MercenaryText.title));
        final Player owner = offer.getOwner();
        final Label detail = new Label(i18n.get(MercenaryText.offer,
                offer.getIssuer().getDisplayName(), offer.getPrice(),
                owner.getGold(), owner.getGold() - offer.getPrice()));
        detail.setId("mercenaryDetail");
        detail.setWrapText(true);
        detail.setMinHeight(Region.USE_PREF_SIZE);
        port.setId("mercenaryPort");
        port.setItems(FXCollections
                .observableArrayList(model.getMercenaryDeliveryPorts(owner)));
        port.setConverter(new StringConverter<Colony>() {
            @Override
            public String toString(final Colony colony) {
                return colony == null ? "" : colony.getName();
            }

            @Override
            public Colony fromString(final String text) {
                return null;
            }
        });
        port.getSelectionModel().selectFirst();
        hint.setId("mercenaryHint");
        hint.setWrapText(true);
        hint.setMinHeight(Region.USE_PREF_SIZE);
        port.valueProperty()
                .addListener((observed, before, now) -> updateHint());
        updateHint();
        accept.setId("mercenaryAccept");
        accept.setText(i18n.get(MercenaryText.accept));
        accept.setDefaultButton(true);
        accept.setOnAction(event -> onAccept());
        decline.setText(i18n.get(MercenaryText.decline));
        decline.setId("mercenaryDecline");
        decline.setOnAction(event -> onDecline());
        later.setText(i18n.get(MercenaryText.later));
        later.setId("mercenaryLater");
        later.setCancelButton(true);
        later.setOnAction(event -> close());
        feedback.setId("mercenaryFeedback");
        feedback.setWrapText(true);
        feedback.setMinHeight(Region.USE_PREF_SIZE);
        if (port.getItems().isEmpty()) {
            feedback.setText(i18n.get(MercenaryText.noPort));
        } else if (owner.getGold() < offer.getPrice()) {
            feedback.setText(i18n.get(MercenaryText.insufficient));
        }
        accept.setDisable(port.getItems().isEmpty()
                || owner.getGold() < offer.getPrice());
        final VBox root = new VBox(16, detail,
                new Label(i18n.get(MercenaryText.port)), port, hint, feedback,
                new HBox(12, accept, decline, later));
        root.setId("mercenaryDialog");
        root.setPadding(new Insets(20));
        root.setPrefWidth(620);
        init(root);
    }

    private boolean isCurrent() {
        return game.isGameModelReady() && game.getModel() == model
                && model.getPendingMercenaryOffer(offer.getOwner())
                        .orElse(null) == offer;
    }

    /** Name the blockade in advance, so a paid group is never a surprise. */
    private void updateHint() {
        final Colony chosen = port.getValue();
        try {
            hint.setText(chosen != null
                    && model.isMercenaryPortBlocked(offer.getOwner(), chosen)
                            ? i18n.get(MercenaryText.blocked)
                            : "");
        } catch (IllegalArgumentException rejected) {
            // The port stopped being an owned delivery port: accepting is
            // rejected by the model and explained then.
            hint.setText("");
        }
    }

    private void onAccept() {
        try {
            final Colony chosen = port.getValue();
            if (!isCurrent() || chosen == null) {
                throw new IllegalStateException("Mercenary view changed");
            }
            final boolean placed = model.acceptMercenaryOffer(offer, chosen);
            // The payment is final; the window stays open to report where the
            // soldiers are, and the only remaining action is to close it.
            accept.setDisable(true);
            decline.setDisable(true);
            later.setText(i18n.get(MercenaryText.close));
            feedback.setText(i18n.get(
                    placed ? MercenaryText.delivered : MercenaryText.pending));
        } catch (IllegalArgumentException | IllegalStateException
                | ArithmeticException rejected) {
            accept.setDisable(true);
            feedback.setText(i18n.get(MercenaryText.changed));
        }
    }

    private void onDecline() {
        try {
            if (isCurrent()) {
                model.declineMercenaryOffer(offer);
            }
            close();
        } catch (IllegalArgumentException | IllegalStateException rejected) {
            accept.setDisable(true);
            feedback.setText(i18n.get(MercenaryText.changed));
        }
    }
}
