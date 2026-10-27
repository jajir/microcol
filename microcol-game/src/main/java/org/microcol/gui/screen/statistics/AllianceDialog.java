package org.microcol.gui.screen.statistics;

import org.microcol.gui.util.ActionButtons;
import org.microcol.gui.image.ButtonIcon;
import org.microcol.gui.UnitTypes;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/** Review, postpone or explicitly settle one saved alliance proposal. */
final class AllianceDialog extends AbstractMessageWindow {
    private final GameModelController game;
    private final Model model;
    private final AllianceOffer offer;
    private final I18n i18n;
    private final Label feedback = new Label();
    private final Button confirm = ActionButtons.button(ButtonIcon.CONFIRM);
    private boolean accepted;

    AllianceDialog(ViewUtil viewUtil, I18n i18n, GameModelController game,
            Model model, AllianceOffer offer) {
        super(viewUtil, i18n);
        this.game = game;
        this.model = model;
        this.offer = offer;
        this.i18n = i18n;
        setTitle(i18n.get(Stats.allianceTitle));
        Label detail = new Label(offer.getActorGoldAfter() >= 0
                ? i18n.get(Stats.allianceOffer,
                        offer.getRival().getDisplayName(), offer.getPrice(),
                        offer.getActorGold(), offer.getActorGoldAfter())
                : i18n.get(Stats.allianceOfferUnaffordable,
                        offer.getRival().getDisplayName(), offer.getPrice(),
                        offer.getActorGold()));
        detail.setId("allianceDetail");
        detail.setWrapText(true);
        detail.setMinHeight(Region.USE_PREF_SIZE);
        Label obligations = new Label(i18n.get(Stats.allianceObligations));
        obligations.setId("allianceObligations");
        obligations.setWrapText(true);
        obligations.setMinHeight(Region.USE_PREF_SIZE);
        Unit visitor = model.getAllUnits().stream()
                .filter(unit -> unit.getId() == offer.getVisitorId())
                .findFirst().orElse(null);
        Label contact = new Label(
                visitor != null && visitor.isAtPlaceLocation()
                        ? i18n.get(Stats.allianceVisitor,
                                i18n.get(UnitTypes
                                        .getUnitName(visitor.getType())),
                                visitor.getLocation().getX(),
                                visitor.getLocation().getY())
                        : i18n.get(Stats.allianceVisitorUnavailable));
        contact.setWrapText(true);
        contact.setMinHeight(Region.USE_PREF_SIZE);
        confirm.setId("allianceConfirm");
        confirm.setText(i18n.get(Stats.allianceConfirm));
        confirm.setDisable(offer.getActor().getGold() < offer.getPrice());
        confirm.setDefaultButton(true);
        confirm.setOnAction(event -> accept());
        Button cancel = ActionButtons.button(ButtonIcon.CANCEL,
                i18n.get(Stats.allianceCancel));
        cancel.setId("allianceCancel");
        cancel.setCancelButton(true);
        cancel.setOnAction(event -> cancel());
        Button later = ActionButtons.button(ButtonIcon.WAIT,
                i18n.get(Stats.allianceLater));
        later.setId("allianceLater");
        later.setOnAction(event -> close());
        feedback.setId("allianceFeedback");
        feedback.setWrapText(true);
        feedback.setMinHeight(Region.USE_PREF_SIZE);
        if (confirm.isDisabled())
            feedback.setText(i18n.get(Stats.allianceInsufficient));
        VBox root = new VBox(16, detail, obligations, contact, feedback,
                new HBox(12, confirm, cancel, later));
        root.setId("allianceDialog");
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
                throw new IllegalStateException("Alliance view changed");
            model.acceptAllianceOffer(offer);
            accepted = true;
            close();
        } catch (IllegalArgumentException | IllegalStateException
                | ArithmeticException rejected) {
            confirm.setDisable(true);
            feedback.setText(i18n.get(Stats.allianceChanged));
        }
    }

    private void cancel() {
        try {
            if (isCurrent())
                model.cancelAllianceOffer(offer);
            close();
        } catch (IllegalArgumentException | IllegalStateException rejected) {
            confirm.setDisable(true);
            feedback.setText(i18n.get(Stats.allianceChanged));
        }
    }
}
