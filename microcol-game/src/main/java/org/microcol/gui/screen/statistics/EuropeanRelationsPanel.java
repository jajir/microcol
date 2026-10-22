package org.microcol.gui.screen.statistics;

import java.util.List;
import java.util.stream.Collectors;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.EuropeanRelationState;
import org.microcol.model.ForeignPowerComparison;
import org.microcol.model.Model;
import org.microcol.model.Player;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/** Basic political facts; game rules and transitions remain in the model. */
final class EuropeanRelationsPanel extends VBox {
    private final GameModelController game;
    private final ViewUtil viewUtil;
    private final I18n i18n;
    private final VBox rows = new VBox(12);
    private final VBox support = new VBox(4);
    private final Label comparisonsTitle = new Label();
    private final GridPane comparisons = new GridPane();
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
        support.setId("rebelSupport");
        comparisons.setId("foreignPowers");
        comparisons.setHgap(12);
        comparisons.setVgap(6);
        final ScrollPane comparisonScroll = new ScrollPane(comparisons);
        comparisonScroll.setId("foreignPowersScroll");
        comparisonScroll.setPrefViewportHeight(150);
        comparisonScroll.setPrefViewportWidth(520);
        comparisonsTitle.setId("foreignPowersTitle");
        getChildren().addAll(support, comparisonsTitle, comparisonScroll,
                scroll, feedback);
    }

    /** Read-only own support values; no rival or hidden colony is listed. */
    private void refreshSupport(final Model model) {
        support.getChildren().clear();
        final Player own = game.getHumanPlayer();
        final org.microcol.model.RebelSupport national = own
                .getNationalRebelSupport();
        final Label nationalLabel = new Label(i18n.get(Stats.supportNational,
                national.getDisplayPercent(), national.getPopulation()));
        nationalLabel.setId("nationalSupport");
        nationalLabel.setWrapText(true);
        nationalLabel.setMaxWidth(380);
        support.getChildren().add(nationalLabel);
        final List<org.microcol.model.Colony> colonies = model.getColonies(own);
        if (colonies.isEmpty()) {
            final Label none = new Label(i18n.get(Stats.supportNone));
            none.setId("supportNone");
            support.getChildren().add(none);
        }
        for (final org.microcol.model.Colony colony : colonies) {
            final Label row = new Label(
                    i18n.get(Stats.supportColonyRow, colony.getName(),
                            colony.getPopulation(), colony.getLiberty(),
                            colony.getRebelSupport().getDisplayPercent()));
            row.setId("supportColony-" + colony.getId());
            row.setWrapText(true);
            row.setMaxWidth(380);
            support.getChildren().add(row);
        }
    }

    /**
     * Read-only foreign comparisons. Each row shows exactly what the model
     * discloses; withheld powers say whether de Witt or contact is missing.
     */
    private void refreshComparisons(final Model model) {
        comparisonsTitle.setText(i18n.get(Stats.foreignTitle));
        comparisons.getChildren().clear();
        final List<Stats> headers = List.of(Stats.foreignPower,
                Stats.foreignRelation, Stats.foreignPopulation,
                Stats.foreignColonies, Stats.foreignAverageSize,
                Stats.foreignMilitary, Stats.foreignNaval, Stats.foreignTrade);
        for (int column = 0; column < headers.size(); column++) {
            final Label header = new Label(i18n.get(headers.get(column)));
            header.getStyleClass().add("relation-names");
            comparisons.add(header, column, 0);
        }
        int row = 1;
        for (final ForeignPowerComparison power : model
                .getForeignPowerComparisons(game.getHumanPlayer())) {
            final String key = power.getName();
            comparisons.add(cell(label(power.getDisplayName(), key),
                    "foreignPower-" + key), 0, row);
            comparisons.add(cell(relationText(power.getRelation()),
                    "foreignRelation-" + key), 1, row);
            if (power.getFigures().isPresent()) {
                final ForeignPowerComparison.Figures figures = power
                        .getFigures().get();
                final List<String> values = List.of(
                        String.valueOf(figures.getPopulation()),
                        String.valueOf(figures.getColonyCount()),
                        number(figures.getAverageColonySize()),
                        number(figures.getMilitaryStrength()),
                        number(figures.getNavalStrength()),
                        String.valueOf(figures.getTradeRevenue()));
                for (int column = 0; column < values.size(); column++) {
                    comparisons.add(
                            cell(values.get(column),
                                    "foreign-" + key + "-" + column),
                            column + 2, row);
                }
            } else {
                final boolean locked = power
                        .getDisclosure() == ForeignPowerComparison.Disclosure.LOCKED;
                final Label withheld = cell(
                        i18n.get(locked ? Stats.foreignLocked
                                : Stats.foreignUncontacted),
                        "foreignWithheld-" + key);
                comparisons.add(withheld, 2, row);
                GridPane.setColumnSpan(withheld, headers.size() - 2);
            }
            row++;
        }
    }

    private String relationText(final EuropeanRelationState state) {
        if (state == EuropeanRelationState.PEACE) {
            return i18n.get(Stats.relationPeace);
        }
        return i18n.get(
                state == EuropeanRelationState.ALLIANCE ? Stats.relationAlliance
                        : Stats.relationWar);
    }

    private static Label cell(final String text, final String id) {
        final Label label = new Label(text);
        label.setId(id);
        label.setWrapText(true);
        return label;
    }

    /**
     * Whole values stay whole; fractions show one decimal in the active UI
     * language, such as 4.5 in English and 4,5 in Czech.
     */
    private String number(final double value) {
        return value == Math.rint(value) ? String.valueOf((long) value)
                : String.format(i18n.getCurrentLocale(), "%.1f", value);
    }

    void showReport() {
        feedback.setText("");
        refresh();
    }

    private void refresh() {
        rows.getChildren().clear();
        support.getChildren().clear();
        comparisons.getChildren().clear();
        if (!game.isGameModelReady()) {
            feedback.setText(i18n.get(Stats.relationsUnavailable));
            return;
        }
        final Model model = game.getModel();
        refreshSupport(model);
        refreshComparisons(model);
        final Player actor = model.getCurrentPlayer();
        final List<Player> players = model.getPlayers().stream()
                .filter(Player::isColonialNation).collect(Collectors.toList());
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
                final int wait = model.getWarDeclarationWait(first, second);
                final Label status = new Label(
                        state == EuropeanRelationState.PEACE
                                ? (wait > 0
                                        ? i18n.get(Stats.relationWarWait, wait)
                                        : i18n.get(Stats.relationPeace))
                                : i18n.get(
                                        state == EuropeanRelationState.ALLIANCE
                                                ? Stats.relationAlliance
                                                : Stats.relationWar));
                status.getStyleClass().add("relation-state");
                row.getChildren().addAll(names, status);
                if (actor.isHuman() && (actor == first || actor == second)
                        && state == EuropeanRelationState.PEACE) {
                    final Player target = actor == first ? second : first;
                    final Button declare = new Button(
                            i18n.get(Stats.declarationReview));
                    declare.setId("declare-" + i + "-" + j);
                    declare.setDisable(!model.isGameRunning() || wait > 0);
                    if (wait > 0) {
                        declare.setTooltip(new javafx.scene.control.Tooltip(
                                i18n.get(Stats.declarationWaiting, wait)));
                    }
                    declare.setOnAction(event -> review(model, actor, target));
                    row.getChildren().add(declare);
                }
                if (actor.isHuman() && (actor == first || actor == second)
                        && state == EuropeanRelationState.PEACE) {
                    final Player target = actor == first ? second : first;
                    final boolean pending = model
                            .getPendingAllianceOffer(actor, target).isPresent();
                    final int blocked = model.getAllianceBlockedRounds(actor,
                            target);
                    final Button alliance = new Button(
                            i18n.get(pending ? Stats.allianceResume
                                    : Stats.allianceOpen));
                    alliance.setId("alliance-" + i + "-" + j);
                    alliance.setDisable(
                            !model.isGameRunning() || (!pending && (blocked > 0
                                    || model.getAllianceVisitors(actor, target)
                                            .isEmpty())));
                    alliance.setTooltip(
                            new javafx.scene.control.Tooltip(blocked > 0
                                    ? i18n.get(Stats.allianceBlocked, blocked)
                                    : i18n.get(Stats.allianceContactRequired)));
                    alliance.setOnAction(
                            event -> propose(model, actor, target));
                    row.getChildren().add(alliance);
                }
                if (actor.isHuman() && (actor == first || actor == second)
                        && state == EuropeanRelationState.ALLIANCE) {
                    final Player target = actor == first ? second : first;
                    final Button end = new Button(
                            i18n.get(Stats.allianceEndOpen));
                    end.setId("allianceEnd-" + i + "-" + j);
                    end.setDisable(!model.isGameRunning());
                    end.setOnAction(event -> endAlliance(model, actor, target));
                    row.getChildren().add(end);
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
        return label(player.getDisplayName(), player.getName());
    }

    private static String label(final String display, final String key) {
        return display.equals(key) ? display : display + " (" + key + ")";
    }

    private boolean isCurrent(final Model original, final Player actor,
            final Player target) {
        return game.isGameModelReady() && game.getModel() == original
                && original.isGameRunning()
                && original.getCurrentPlayer() == actor
                && original.getEuropeanRelation(actor,
                        target) == EuropeanRelationState.PEACE
                && original.getWarDeclarationWait(actor, target) == 0;
    }

    private void propose(Model original, Player actor, Player target) {
        try {
            if (!game.isGameModelReady() || game.getModel() != original
                    || original.getCurrentPlayer() != actor)
                throw new IllegalStateException("Relations view changed");
            final org.microcol.model.AllianceOffer offer = original
                    .getPendingAllianceOffer(actor, target)
                    .orElseGet(() -> original.prepareAllianceOffer(
                            original.getAllianceVisitors(actor, target).stream()
                                    .findFirst().orElseThrow(
                                            () -> new IllegalStateException(
                                                    "Contact unavailable")),
                            target));
            boolean accepted = new AllianceDialog(viewUtil, i18n, game,
                    original, offer).review();
            refresh();
            if (accepted)
                feedback.setText(i18n.get(Stats.allianceFormed, label(actor),
                        label(target)));
        } catch (IllegalArgumentException | IllegalStateException
                | ArithmeticException rejected) {
            refresh();
            feedback.setText(i18n.get(Stats.allianceChanged));
        }
    }

    private void endAlliance(Model original, Player actor, Player target) {
        final String actorLabel = label(actor);
        final String targetLabel = label(target);
        try {
            if (!game.isGameModelReady() || game.getModel() != original
                    || original.getCurrentPlayer() != actor
                    || !original.areAllied(actor, target))
                throw new IllegalStateException("Relations view changed");
            feedback.setText("");
            if (!new AllianceEndDialog(viewUtil, i18n, actorLabel, targetLabel,
                    Model.ALLIANCE_WAR_WAIT_ROUNDS).confirm()) {
                refresh();
                return;
            }
            if (!game.isGameModelReady() || game.getModel() != original
                    || original.getCurrentPlayer() != actor
                    || !actorLabel.equals(label(actor))
                    || !targetLabel.equals(label(target)))
                throw new IllegalStateException("Relations view changed");
            original.terminateAlliance(actor, target);
            refresh();
            feedback.setText(
                    i18n.get(Stats.allianceEnded, actorLabel, targetLabel,
                            original.getWarDeclarationWait(actor, target)));
            feedback.requestFocus();
        } catch (IllegalArgumentException | IllegalStateException rejected) {
            refresh();
            feedback.setText(i18n.get(Stats.allianceEndStale));
        }
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
