package org.microcol.gui.screen.statistics;

import org.microcol.gui.UnitTypes;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/** Review, postpone or explicitly settle one saved peace proposal. */
final class PeaceDialog extends AbstractMessageWindow {
    private final GameModelController game;
    private final Model model;
    private final PeaceOffer offer;
    private final I18n i18n;
    private final Label feedback = new Label();
    private final Button confirm = new Button();
    private boolean accepted;

    PeaceDialog(ViewUtil viewUtil, I18n i18n, GameModelController game,
            Model model, PeaceOffer offer) {
        super(viewUtil, i18n);
        this.game = game;
        this.model = model;
        this.offer = offer;
        this.i18n = i18n;
        setTitle(i18n.get(PeaceText.title));
        Label detail = new Label(offer.getActorGoldAfter() >= 0
                ? i18n.get(PeaceText.offer, offer.getRival().getDisplayName(),
                        offer.getPrice(), offer.getActorGold(),
                        offer.getActorGoldAfter())
                : i18n.get(PeaceText.offerUnaffordable,
                        offer.getRival().getDisplayName(), offer.getPrice(),
                        offer.getActorGold()));
        detail.setId("peaceDetail");
        detail.setWrapText(true);
        detail.setMinHeight(Region.USE_PREF_SIZE);
        Unit visitor = model.getAllUnits().stream()
                .filter(unit -> unit.getId() == offer.getVisitorId())
                .findFirst().orElse(null);
        Label contact = new Label(
                visitor != null && visitor.isAtPlaceLocation()
                        ? i18n.get(PeaceText.visitor,
                                i18n.get(UnitTypes
                                        .getUnitName(visitor.getType())),
                                visitor.getLocation().getX(),
                                visitor.getLocation().getY())
                        : i18n.get(PeaceText.visitorUnavailable));
        contact.setWrapText(true);
        contact.setMinHeight(Region.USE_PREF_SIZE);
        confirm.setId("peaceConfirm");
        confirm.setText(i18n.get(PeaceText.confirm));
        confirm.setDisable(offer.getActor().getGold() < offer.getPrice());
        confirm.setDefaultButton(true);
        confirm.setOnAction(event -> accept());
        Button cancel = new Button(i18n.get(PeaceText.cancel));
        cancel.setId("peaceCancel");
        cancel.setCancelButton(true);
        cancel.setOnAction(event -> cancel());
        Button later = new Button(i18n.get(PeaceText.later));
        later.setId("peaceLater");
        later.setOnAction(event -> close());
        feedback.setId("peaceFeedback");
        feedback.setWrapText(true);
        feedback.setMinHeight(Region.USE_PREF_SIZE);
        if (confirm.isDisabled())
            feedback.setText(i18n.get(PeaceText.insufficient));
        VBox root = new VBox(16, detail, contact, feedback,
                new HBox(12, confirm, cancel, later));
        root.setId("peaceDialog");
        root.setPadding(new Insets(20));
        root.setPrefWidth(620);
        init(root);
    }

    boolean review() {
        showAndWait();
        return accepted;
    }

    private boolean isCurrent() {
        return game.isGameModelReady() && game.getModel() == model
                && model.isGameRunning()
                && model.getCurrentPlayer() == offer.getActor();
    }

    private void accept() {
        try {
            if (!isCurrent())
                throw new IllegalStateException("Peace view changed");
            model.acceptPeaceOffer(offer);
            accepted = true;
            close();
        } catch (IllegalArgumentException | IllegalStateException
                | ArithmeticException rejected) {
            confirm.setDisable(true);
            feedback.setText(i18n.get(PeaceText.changed));
        }
    }

    private void cancel() {
        try {
            if (isCurrent())
                model.cancelPeaceOffer(offer);
            close();
        } catch (IllegalArgumentException | IllegalStateException rejected) {
            confirm.setDisable(true);
            feedback.setText(i18n.get(PeaceText.changed));
        }
    }
}
