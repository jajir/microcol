package org.microcol.gui.screen.congress;

import org.microcol.gui.util.ActionButtons;
import org.microcol.gui.image.ButtonIcon;
import com.google.inject.Inject;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.InGamePage;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.stream.Collectors;

/** Read-only Congress overview until an explicit candidate confirmation. */
public final class CongressDialog extends AbstractMessageWindow {
    private final GameModelController game;
    private final I18n i18n;
    private final InGamePage page;
    private final Label support = label("congressSupport");
    private final Label progress = label("congressProgress");
    private final Label objective = label("congressObjective");
    private final Label members = label("congressMembers");
    private final Label reward = label("congressReward");
    private final Label royalInfantry = label("congressRoyalInfantry");
    private final Label royalCavalry = label("congressRoyalCavalry");
    private final Label royalArtillery = label("congressRoyalArtillery");
    private final Label royalMenOfWar = label("congressRoyalMenOfWar");
    private final Label royalBells = label("congressRoyalBells");
    private final Label royalStopped = label("congressRoyalStopped");
    private final Label intervention = label("congressIntervention");
    private final Label interventionForces = label(
            "congressInterventionForces");
    private final Label feedback = label("congressFeedback");
    private final Label detail = label("congressDetail");
    private final ListView<FounderOffer> candidates = new ListView<>();
    private final Button choose = ActionButtons.button(ButtonIcon.CONFIRM);
    private final Button close = ActionButtons.button(ButtonIcon.CANCEL);
    private Model boundModel;
    private Player player;

    @Inject
    public CongressDialog(ViewUtil viewUtil, I18n i18n,
            GameModelController game, InGamePage page) {
        super(viewUtil, i18n);
        this.i18n = i18n;
        this.game = game;
        this.page = page;
        candidates.setId("congressCandidates");
        candidates.setPrefHeight(190);
        candidates.setMinHeight(Region.USE_PREF_SIZE);
        candidates.setCellFactory(view -> new ListCell<>() {
            @Override
            protected void updateItem(FounderOffer offer, boolean empty) {
                super.updateItem(offer, empty);
                setText(empty || offer == null ? null
                        : CongressText.name(i18n, offer.getFounder()) + " — "
                                + i18n.get(CongressText.valueOf(offer
                                        .getFounder().getCategory().name())));
                setId(empty || offer == null ? null
                        : "founder-" + offer.getFounder().name());
            }
        });
        candidates.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, value) -> {
                    choose.setDisable(value == null || !isCurrent()
                            || !player.canChooseFounder(value));
                    detail.setText(value != null
                            && value.getFounder() == Founder.JOHN_PAUL_JONES
                                    ? i18n.get(CongressText.jonesEffect)
                                    : "");
                });
        choose.setId("congressChoose");
        close.setId("congressClose");
        choose.setOnAction(event -> choose());
        close.setOnAction(event -> close());
        close.setCancelButton(true);
        VBox root = new VBox(12, support, progress, objective, candidates,
                detail, members, reward, royalInfantry, royalCavalry,
                royalArtillery, royalMenOfWar, royalBells, royalStopped,
                intervention, interventionForces, feedback);
        root.setId("congressDialog");
        root.setPadding(new Insets(20));
        root.setPrefWidth(580);
        final HBox actions = new HBox(12, choose, close);
        actions.setId("congressActions");
        actions.setPadding(new Insets(12, 20, 20, 20));
        // A focused candidate list consumes Escape before scene bubbling.
        page.getContent().addEventFilter(
                javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
                    if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                        close();
                        event.consume();
                    }
                });
        page.setToolbar(actions);
        page.setContent(root);
        page.getContent().lookup(".screen-scroll").setId("congressScroll");
        init(page.getContent());
    }

    private static Label label(String id) {
        Label label = new Label();
        label.setId(id);
        label.setWrapText(true);
        label.setMinHeight(Region.USE_PREF_SIZE);
        return label;
    }

    public void showCongress() {
        if (!game.isGameModelReady())
            return;
        boundModel = game.getModel();
        player = game.getHumanPlayer();
        setTitle(i18n.get(CongressText.title));
        page.setTitle(i18n.get(CongressText.title));
        page.getContent().setPrefSize(
                getViewUtil().getPrimaryStage().getScene().getWidth(),
                getViewUtil().getPrimaryStage().getScene().getHeight());
        page.beforeShow();
        choose.setText(i18n.get(CongressText.choose));
        close.setText(i18n.get(CongressText.close));
        feedback.setText("");
        refresh();
        showAndWait();
    }

    private boolean isCurrent() {
        return player != null && game.isGameModelReady()
                && game.getModel() == boundModel
                && game.getHumanPlayer() == player && boundModel.isGameRunning()
                && boundModel.getCurrentPlayer() == player;
    }

    /** Reads one Congress report of the saved state; nothing is cached. */
    private void refresh() {
        final CongressReport report = boundModel.getCongressReport(player);
        refreshSupport(report);
        refreshRoyal(report);
        refreshIntervention();
        refreshInterventionForces();
        progress.setText(i18n.get(CongressText.progress, report.getBells(),
                report.getTarget()));
        objective.setText(report.getSelectedFounder()
                .map(founder -> i18n.get(CongressText.objective,
                        CongressText.name(i18n, founder)))
                .orElseGet(() -> i18n.get(
                        report.getMembers().size() == Founder.values().length
                                ? CongressText.allMembers
                                : CongressText.chooseObjective)));
        candidates.getItems().setAll(player.getFounderOffers());
        choose.setDisable(true);
        members.setText(i18n.get(CongressText.members,
                report.getMembers().isEmpty() ? i18n.get(CongressText.none)
                        : report.getMembers().stream().map(
                                founder -> CongressText.name(i18n, founder))
                                .collect(Collectors.joining(", "))));
        reward.setText(report.isJonesDeliveryPending()
                ? i18n.get(CongressText.jonesPending)
                : report.getMembers().contains(Founder.JOHN_PAUL_JONES)
                        ? i18n.get(CongressText.jonesDelivered)
                        : "");
        if (player.getFounderOffers().isEmpty()
                && report.getSelectedFounder().isEmpty()
                && report.getMembers().isEmpty())
            feedback.setText(i18n.get(CongressText.firstColony));
    }

    /**
     * Shows the exact national support rounded down, with the Statistics label
     * (R-0100); zero residents read as zero percent.
     */
    private void refreshSupport(final CongressReport report) {
        support.setText(i18n.get(CongressText.support,
                report.getNationalSupport().getDisplayPercent(),
                report.getNationalSupport().getPopulation()));
    }

    /** Shows the Crown's real roster; blank when no Crown rules this nation. */
    private void refreshRoyal(final CongressReport report) {
        RoyalRoster crown = report.getCrownRoster().orElse(null);
        royalInfantry.setText(crown == null ? ""
                : familyLine(CongressText.royalInfantry, crown,
                        RoyalRoster.Family.INFANTRY));
        royalCavalry.setText(crown == null ? ""
                : familyLine(CongressText.royalCavalry, crown,
                        RoyalRoster.Family.CAVALRY));
        royalArtillery.setText(crown == null ? ""
                : familyLine(CongressText.royalArtillery, crown,
                        RoyalRoster.Family.ARTILLERY));
        royalMenOfWar.setText(crown == null ? ""
                : familyLine(CongressText.royalMenOfWar, crown,
                        RoyalRoster.Family.MAN_OF_WAR));
        royalBells.setText(crown == null ? ""
                : i18n.get(CongressText.royalBells, crown.getBellRemainder(),
                        crown.getBellThreshold()));
        royalStopped.setText(player.isDeclaredIndependence()
                ? i18n.get(CongressText.royalStopped)
                : "");
    }

    /**
     * Shows the stored intervention target and its progress or earning. Only
     * reads the model; the target is set by the rebel's own update.
     */
    private void refreshIntervention() {
        intervention.setText(player.getIntervention()
                .map(state -> state.isEarned()
                        ? i18n.get(CongressText.interventionEarned,
                                state.getTarget())
                        : i18n.get(CongressText.interventionProgress,
                                state.getBells(), state.getTarget()))
                .orElseGet(() -> player.isDeclaredIndependence()
                        ? i18n.get(CongressText.interventionAwaiting)
                        : ""));
    }

    /**
     * Shows whether the earned forces have arrived or still wait (R-0190). Only
     * reads the model.
     */
    private void refreshInterventionForces() {
        interventionForces.setText(
                player.getIntervention().filter(state -> state.isEarned())
                        .map(state -> i18n.get(state.isDelivered()
                                ? CongressText.interventionDelivered
                                : CongressText.interventionPending))
                        .orElse(""));
    }

    private String familyLine(CongressText label, RoyalRoster crown,
            RoyalRoster.Family family) {
        return i18n.get(CongressText.royalFamily, i18n.get(label),
                crown.getTotal(family),
                crown.getCount(family, RoyalRoster.State.RESERVE),
                crown.getCount(family, RoyalRoster.State.SHIPPED),
                crown.getCount(family, RoyalRoster.State.DESTROYED),
                crown.getCount(family, RoyalRoster.State.CAPTURED));
    }

    private void choose() {
        FounderOffer offer = candidates.getSelectionModel().getSelectedItem();
        if (offer == null)
            return;
        try {
            if (!isCurrent())
                throw new IllegalStateException("Congress view changed");
            player.chooseFounder(offer);
            refresh();
            feedback.setText(i18n.get(CongressText.chosen));
        } catch (IllegalArgumentException | IllegalStateException rejected) {
            choose.setDisable(true);
            feedback.setText(i18n.get(CongressText.changed));
        }
    }
}
