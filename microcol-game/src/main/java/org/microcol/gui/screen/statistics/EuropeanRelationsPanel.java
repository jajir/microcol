package org.microcol.gui.screen.statistics;

import java.util.List;
import java.util.stream.Collectors;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.EuropeanRelationState;
import org.microcol.model.Model;
import org.microcol.model.Player;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

/** Basic political facts; game rules and transitions remain in the model. */
final class EuropeanRelationsPanel extends VBox {
    private final GameModelController game;
    private final ViewUtil viewUtil;
    private final I18n i18n;
    private final VBox rows = new VBox(12);
    private final Label feedback = new Label();

    EuropeanRelationsPanel(final GameModelController game,
            final ViewUtil viewUtil, final I18n i18n) {
        super(12);
        this.game = game;
        this.viewUtil = viewUtil;
        this.i18n = i18n;
        setId("europeanRelations");
        getStyleClass().add("relations-report");
        final ScrollPane scroll = new ScrollPane(rows);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(320);
        scroll.setPrefViewportWidth(400);
        feedback.setId("relationsFeedback");
        feedback.setWrapText(true);
        getChildren().addAll(scroll, feedback);
    }

    void showReport() {
        feedback.setText("");
        refresh();
    }

    private void refresh() {
        rows.getChildren().clear();
        if (!game.isGameModelReady()) {
            feedback.setText(i18n.get(Stats.relationsUnavailable));
            return;
        }
        final Model model = game.getModel();
        final Player actor = model.getCurrentPlayer();
        final List<Player> players = model.getPlayers().stream()
                .filter(player -> !player.isKing())
                .collect(Collectors.toList());
        for (int i = 0; i < players.size(); i++) {
            for (int j = i + 1; j < players.size(); j++) {
                final Player first = players.get(i);
                final Player second = players.get(j);
                final EuropeanRelationState state = model
                        .getEuropeanRelation(first, second);
                final VBox row = new VBox(4);
                row.setId("relation-" + i + "-" + j);
                row.getStyleClass().add("relation-row");
                row.setUserData(List.of(first.getName(), second.getName()));
                final Label names = new Label(
                        label(first) + " — " + label(second));
                names.setWrapText(true);
                names.setMaxWidth(380);
                names.getStyleClass().add("relation-names");
                final Label status = new Label(
                        i18n.get(state == EuropeanRelationState.PEACE
                                ? Stats.relationPeace
                                : Stats.relationWar));
                status.getStyleClass().add("relation-state");
                row.getChildren().addAll(names, status);
                if (actor.isHuman() && (actor == first || actor == second)
                        && state == EuropeanRelationState.PEACE) {
                    final Player target = actor == first ? second : first;
                    final Button declare = new Button(
                            i18n.get(Stats.declarationReview));
                    declare.setId("declare-" + i + "-" + j);
                    declare.setDisable(!model.isGameRunning());
                    declare.setOnAction(event -> review(model, actor, target));
                    row.getChildren().add(declare);
                }
                if (actor.isHuman() && (actor == first || actor == second)
                        && state == EuropeanRelationState.WAR) {
                    final Player target = actor == first ? second : first;
                    final boolean pending = model
                            .getPendingPeaceOffer(actor, target).isPresent();
                    final Button peace = new Button(i18n
                            .get(pending ? PeaceText.resume : PeaceText.open));
                    peace.setId("peace-" + i + "-" + j);
                    peace.setDisable(!model.isGameRunning() || (!pending
                            && model.getPeaceVisitors(actor, target)
                                    .isEmpty()));
                    peace.setTooltip(new javafx.scene.control.Tooltip(
                            i18n.get(PeaceText.contactRequired)));
                    peace.setOnAction(event -> negotiate(model, actor, target));
                    row.getChildren().add(peace);
                }
                if (actor.isHuman() && (actor == first || actor == second)) {
                    final Player target = actor == first ? second : first;
                    final boolean pending = model
                            .getPendingTributeDemand(actor, target).isPresent();
                    final int remaining = model.getTributeTurnsRemaining(actor,
                            target);
                    final Button tribute = new Button(i18n.get(
                            pending ? TributeText.resume : TributeText.open));
                    tribute.setId("tribute-" + i + "-" + j);
                    tribute.setDisable(!model.isGameRunning()
                            || (!pending && (remaining > 0
                                    || model.getTributeVisitors(actor, target)
                                            .isEmpty())));
                    tribute.setTooltip(
                            new javafx.scene.control.Tooltip(remaining > 0
                                    ? i18n.get(TributeText.cooldown, remaining)
                                    : i18n.get(TributeText.contactRequired)));
                    tribute.setOnAction(
                            event -> demandTribute(model, actor, target));
                    row.getChildren().add(tribute);
                }
                rows.getChildren().add(row);
            }
        }
        if (rows.getChildren().isEmpty()) {
            rows.getChildren().add(new Label(i18n.get(Stats.relationsEmpty)));
        }
        if (!model.isGameRunning() || !actor.isHuman()) {
            feedback.setText(i18n.get(Stats.relationsReadOnly));
        }
    }

    private String label(final Player player) {
        final String display = player.getDisplayName();
        return display.equals(player.getName()) ? display
                : display + " (" + player.getName() + ")";
    }

    private boolean isCurrent(final Model original, final Player actor,
            final Player target) {
        return game.isGameModelReady() && game.getModel() == original
                && original.isGameRunning()
                && original.getCurrentPlayer() == actor
                && original.getEuropeanRelation(actor,
                        target) == EuropeanRelationState.PEACE;
    }

    private void review(final Model original, final Player actor,
            final Player target) {
        if (!isCurrent(original, actor, target)) {
            stale();
            return;
        }
        final String actorLabel = label(actor);
        final String targetLabel = label(target);
        feedback.setText("");
        if (!new DeclarationDialog(viewUtil, i18n, actorLabel, targetLabel)
                .confirm()) {
            refresh();
            return;
        }
        if (!isCurrent(original, actor, target)
                || !actorLabel.equals(label(actor))
                || !targetLabel.equals(label(target))) {
            stale();
            return;
        }
        try {
            original.declareHostilities(actor, target);
        } catch (IllegalArgumentException | IllegalStateException rejected) {
            stale();
            return;
        }
        refresh();
        feedback.setText(
                i18n.get(Stats.declarationDone, actorLabel, targetLabel));
        feedback.requestFocus();
    }

    private void negotiate(Model original, Player actor, Player target) {
        try {
            if (!game.isGameModelReady() || game.getModel() != original
                    || original.getCurrentPlayer() != actor)
                throw new IllegalStateException("Relations view changed");
            final org.microcol.model.PeaceOffer offer = original
                    .getPendingPeaceOffer(actor, target)
                    .orElseGet(() -> original.preparePeaceOffer(
                            original.getPeaceVisitors(actor, target).stream()
                                    .findFirst().orElseThrow(
                                            () -> new IllegalStateException(
                                                    "Contact unavailable")),
                            target));
            boolean accepted = new PeaceDialog(viewUtil, i18n, game, original,
                    offer).review();
            refresh();
            if (accepted)
                feedback.setText(i18n.get(PeaceText.accepted, label(actor),
                        label(target)));
        } catch (IllegalArgumentException | IllegalStateException
                | ArithmeticException rejected) {
            refresh();
            feedback.setText(i18n.get(PeaceText.changed));
        }
    }

    private void demandTribute(Model original, Player actor, Player target) {
        try {
            if (!game.isGameModelReady() || game.getModel() != original
                    || original.getCurrentPlayer() != actor)
                throw new IllegalStateException("Relations view changed");
            final org.microcol.model.TributeDemand offer = original
                    .getPendingTributeDemand(actor, target)
                    .orElseGet(() -> original.prepareTributeDemand(
                            original.getTributeVisitors(actor, target).stream()
                                    .findFirst().orElseThrow(
                                            () -> new IllegalStateException(
                                                    "Contact unavailable")),
                            target));
            boolean confirmed = new TributeDialog(viewUtil, i18n, game,
                    original, offer).review();
            refresh();
            if (confirmed)
                feedback.setText(
                        offer.getAmount() > 0
                                ? i18n.get(TributeText.paid, label(target),
                                        offer.getAmount(), label(actor))
                                : i18n.get(
                                        offer.isAccepted() ? TributeText.empty
                                                : TributeText.refused,
                                        label(target)));
        } catch (IllegalArgumentException | IllegalStateException
                | ArithmeticException rejected) {
            refresh();
            feedback.setText(i18n.get(TributeText.changed));
        }
    }

    private void stale() {
        refresh();
        feedback.setText(i18n.get(Stats.declarationStale));
    }
}
