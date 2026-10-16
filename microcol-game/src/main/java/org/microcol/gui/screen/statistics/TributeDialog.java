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

/** Review, postpone or confirm one saved tribute demand. */
final class TributeDialog extends AbstractMessageWindow {
    private final GameModelController game;
    private final Model model;
    private final TributeDemand offer;
    private final I18n i18n;
    private final Label feedback = new Label();
    private final Button confirm = new Button();
    private boolean accepted;

    TributeDialog(ViewUtil viewUtil, I18n i18n, GameModelController game,
            Model model, TributeDemand offer) {
        super(viewUtil, i18n);
        this.game = game;
        this.model = model;
        this.offer = offer;
        this.i18n = i18n;
        setTitle(i18n.get(TributeText.title));
        Label detail = new Label(i18n.get(TributeText.offer,
                offer.getRival().getDisplayName(), offer.getAmount(),
                offer.getActorGold(), offer.getActorGoldAfter())
                + "\n"
                + i18n.get(!offer.isAccepted() ? TributeText.refusedPreview
                        : offer.getAmount() == 0 ? TributeText.emptyPreview
                                : TributeText.acceptedPreview));
        detail.setId("tributeDetail");
        detail.setWrapText(true);
        detail.setMinHeight(Region.USE_PREF_SIZE);
        Unit visitor = model.getAllUnits().stream()
                .filter(unit -> unit.getId() == offer.getVisitorId())
                .findFirst().orElse(null);
        Label contact = new Label(
                visitor != null && visitor.isAtPlaceLocation()
                        ? i18n.get(TributeText.visitor,
                                i18n.get(UnitTypes
                                        .getUnitName(visitor.getType())),
                                visitor.getLocation().getX(),
                                visitor.getLocation().getY())
                        : i18n.get(TributeText.visitorUnavailable));
        contact.setWrapText(true);
        contact.setMinHeight(Region.USE_PREF_SIZE);
        confirm.setId("tributeConfirm");
        confirm.setText(i18n.get(TributeText.confirm));
        confirm.setDefaultButton(true);
        confirm.setOnAction(event -> accept());
        Button cancel = new Button(i18n.get(TributeText.cancel));
        cancel.setId("tributeCancel");
        cancel.setCancelButton(true);
        cancel.setOnAction(event -> cancel());
        Button later = new Button(i18n.get(TributeText.later));
        later.setId("tributeLater");
        later.setOnAction(event -> close());
        feedback.setId("tributeFeedback");
        feedback.setWrapText(true);
        feedback.setMinHeight(Region.USE_PREF_SIZE);
        VBox root = new VBox(16, detail, contact, feedback,
                new HBox(12, confirm, cancel, later));
        root.setId("tributeDialog");
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
                throw new IllegalStateException("Tribute view changed");
            model.confirmTributeDemand(offer);
            accepted = true;
            close();
        } catch (IllegalArgumentException | IllegalStateException
                | ArithmeticException rejected) {
            confirm.setDisable(true);
            feedback.setText(i18n.get(TributeText.changed));
        }
    }

    private void cancel() {
        try {
            if (isCurrent())
                model.cancelTributeDemand(offer);
            close();
        } catch (IllegalArgumentException | IllegalStateException rejected) {
            confirm.setDisable(true);
            feedback.setText(i18n.get(TributeText.changed));
        }
    }
}
