package org.microcol.gui.screen.congress;

import org.microcol.gui.util.ActionButtons;
import org.microcol.gui.image.ButtonIcon;
import com.google.inject.Inject;
import org.microcol.gui.event.model.GameModelController;
import com.google.common.eventbus.EventBus;
import org.microcol.gui.screen.GameScreen;
import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;
import org.microcol.gui.util.InGamePage;
import org.microcol.i18n.I18n;
import org.microcol.model.*;
import javafx.geometry.Pos;
import javafx.beans.binding.Bindings;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.stream.Collectors;

/** Read-only Congress overview until an explicit candidate confirmation. */
public final class ScreenCongress implements GameScreen {
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
    private final TilePane candidates = new TilePane(12, 12);
    private final ToggleGroup selection = new ToggleGroup();
    private final Label founderName = label("congressFounderName");
    private final ProgressBar bellProgress = new ProgressBar();
    private final Label royalSummary = label("congressRoyalSummary");
    private final TitledPane royalDetails = new TitledPane();
    private final Button choose = ActionButtons.button(ButtonIcon.CONFIRM);
    private final Button close = ActionButtons.button(ButtonIcon.BACK);
    private Model boundModel;
    private Player player;

    @Inject
    public ScreenCongress(EventBus eventBus, I18n i18n,
            GameModelController game, InGamePage page) {
        this.i18n = i18n;
        this.game = game;
        this.page = page;
        candidates.setId("congressCandidates");
        candidates.setPrefColumns(5);
        candidates.setTileAlignment(Pos.TOP_LEFT);
        candidates.setMinHeight(Region.USE_PREF_SIZE);
        candidates.setMaxHeight(Region.USE_PREF_SIZE);
        // TilePane otherwise measures wrapped button graphics without a width.
        candidates.prefTileHeightProperty()
                .bind(Bindings.createDoubleBinding(
                        () -> candidates.getChildren().stream()
                                .mapToDouble(node -> node.prefHeight(
                                        candidates.getPrefTileWidth()))
                                .max().orElse(0),
                        candidates.prefTileWidthProperty(),
                        candidates.getChildren(),
                        selection.selectedToggleProperty()));
        selection.selectedToggleProperty().addListener((obs, old, value) -> {
            final FounderOffer offer = value == null ? null
                    : (FounderOffer) value.getUserData();
            choose.setDisable(offer == null || !isCurrent()
                    || !player.canChooseFounder(offer));
            founderName.setText(offer == null ? ""
                    : CongressText.name(i18n, offer.getFounder()));
            detail.setText(offer == null ? ""
                    : CongressText.effect(i18n, offer.getFounder()));
        });
        choose.setId("congressChoose");
        close.setId("congressClose");
        choose.setOnAction(event -> choose());
        close.setOnAction(
                event -> eventBus.post(new ShowScreenEvent(Screen.GAME)));
        close.setCancelButton(true);
        bellProgress.setId("congressBellProgress");
        bellProgress.setMaxWidth(Double.MAX_VALUE);
        bellProgress.setMinHeight(Region.USE_PREF_SIZE);
        final VBox bells = new VBox(10, progress, bellProgress);
        final HBox metrics = new HBox(24, bells, support, members);
        metrics.getStyleClass().add("congress-metrics");
        for (Region metric : new Region[] { bells, support, members }) {
            metric.setMinWidth(0);
            metric.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(metric, Priority.ALWAYS);
            metric.prefWidthProperty()
                    .bind(metrics.widthProperty().subtract(48).divide(3));
        }
        founderName.getStyleClass().add("congress-founder-name");
        final VBox description = new VBox(8, founderName, detail);
        description.setMinWidth(0);
        HBox.setHgrow(description, Priority.ALWAYS);
        choose.setMinWidth(Region.USE_PREF_SIZE);
        final HBox decision = new HBox(24, description, choose);
        decision.setAlignment(Pos.CENTER_LEFT);
        decision.getStyleClass().add("congress-decision");
        royalDetails.setId("congressRoyalDetails");
        royalDetails.setAnimated(false);
        royalDetails.setExpanded(false);
        royalDetails.setContent(new VBox(8, royalInfantry, royalCavalry,
                royalArtillery, royalMenOfWar));
        final VBox royal = new VBox(8, royalSummary, royalDetails, royalBells,
                royalStopped, intervention, interventionForces);
        royal.getStyleClass().add("congress-royal");
        final VBox root = new VBox(18, metrics, objective, candidates, decision,
                feedback, reward, royal);
        root.setId("congressDialog");
        root.setMinWidth(0);
        candidates.prefTileWidthProperty()
                .bind(Bindings.createDoubleBinding(() -> {
                    final double width = Math.max(210, root.getWidth());
                    final int columns = Math.max(1,
                            Math.min(5, (int) ((width + 12) / 222)));
                    return Math.floor((width - (columns - 1) * 12) / columns);
                }, root.widthProperty()));
        final Label heading = new Label();
        heading.setId("congressHeading");
        heading.setGraphic(ButtonIcon.CONGRESS.createView());
        page.setTitle(heading);
        final HBox actions = new HBox(close);
        actions.setId("congressActions");
        page.getContent().addEventFilter(
                javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
                    if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                        close.fire();
                        event.consume();
                    }
                });
        page.setToolbar(actions);
        page.setContent(root);
        page.getContent().setId("congressPage");
        page.getContent().getStylesheets().add(ScreenCongress.class
                .getResource("/gui/Congress.css").toExternalForm());
        page.getContent().lookup(".screen-scroll").setId("congressScroll");
    }

    private ToggleButton candidate(final FounderOffer offer) {
        final Founder founder = offer.getFounder();
        final Label category = label("");
        category.setText(
                i18n.get(CongressText.valueOf(founder.getCategory().name())));
        category.getStyleClass().add("congress-category");
        final Label name = label("");
        name.setText(CongressText.name(i18n, founder));
        name.getStyleClass().add("congress-founder-name");
        final Label effect = label("");
        effect.setText(CongressText.effect(i18n, founder));
        final Label selected = label("");
        final ToggleButton card = ActionButtons.toggle(categoryIcon(founder));
        final VBox content = new VBox(10, card.getGraphic(), category, name,
                effect, selected);
        content.setMouseTransparent(true);
        card.setId("founder-" + founder.name());
        card.setUserData(offer);
        card.setToggleGroup(selection);
        card.getStyleClass().add("congress-card");
        card.setGraphic(content);
        card.setAccessibleText(CongressText.name(i18n, founder) + ". "
                + category.getText() + ". " + effect.getText());
        card.setMinWidth(0);
        card.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        content.prefWidthProperty()
                .bind(candidates.prefTileWidthProperty().subtract(32));
        selected.textProperty().bind(Bindings.when(card.selectedProperty())
                .then(i18n.get(CongressText.selected)).otherwise(""));
        return card;
    }

    private static ButtonIcon categoryIcon(final Founder founder) {
        switch (founder.getCategory()) {
        case MILITARY:
            return ButtonIcon.MILITARY;
        case POLITICAL:
            return ButtonIcon.DIPLOMACY;
        case TRADE:
            return ButtonIcon.TRADE;
        case RELIGIOUS:
            return ButtonIcon.RELIGION;
        case EXPLORATION:
            return ButtonIcon.CENTER;
        default:
            throw new IllegalArgumentException("Unknown founder category");
        }
    }

    private static Label label(String id) {
        Label label = new Label();
        label.setId(id);
        label.setWrapText(true);
        label.setMinHeight(Region.USE_PREF_SIZE);
        label.managedProperty().bind(label.textProperty().isNotEmpty());
        label.visibleProperty().bind(label.managedProperty());
        return label;
    }

    @Override
    public void beforeShow() {
        if (!game.isGameModelReady())
            return;
        boundModel = game.getModel();
        player = game.getHumanPlayer();
        royalDetails.setExpanded(false);
        ((ScrollPane) page.getContent().lookup("#congressScroll")).setVvalue(0);
        feedback.setText("");
        page.beforeShow();
        updateLanguage(i18n);
    }

    @Override
    public void beforeHide() {
        boundModel = null;
        player = null;
    }

    @Override
    public Region getContent() {
        return page.getContent();
    }

    @Override
    public void updateLanguage(final I18n texts) {
        ((Label) page.getContent().lookup("#congressHeading"))
                .setText(texts.get(CongressText.title));
        royalDetails.setText(texts.get(CongressText.deployment));
        choose.setText(texts.get(CongressText.choose));
        close.setText(texts.get(CongressText.back));
        page.updateLanguage(texts);
        if (boundModel != null)
            refresh();
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
        selection.getToggles().clear();
        candidates.getChildren().clear();
        player.getFounderOffers().forEach(
                offer -> candidates.getChildren().add(candidate(offer)));
        candidates.setManaged(!candidates.getChildren().isEmpty());
        founderName.setText(report.getSelectedFounder()
                .map(founder -> CongressText.name(i18n, founder)).orElse(""));
        detail.setText(report.getSelectedFounder()
                .map(founder -> CongressText.effect(i18n, founder)).orElse(""));
        bellProgress.setProgress(report.getTarget() == 0 ? 0
                : Math.min(1, (double) report.getBells() / report.getTarget()));
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
        royalSummary.setText(crown == null ? ""
                : i18n.get(CongressText.royalSummary,
                        crown.getTotal(RoyalRoster.Family.INFANTRY),
                        crown.getTotal(RoyalRoster.Family.CAVALRY),
                        crown.getTotal(RoyalRoster.Family.ARTILLERY),
                        crown.getTotal(RoyalRoster.Family.MAN_OF_WAR)));
        royalDetails.setManaged(crown != null);
        royalDetails.setVisible(crown != null);
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
        FounderOffer offer = selection.getSelectedToggle() == null ? null
                : (FounderOffer) selection.getSelectedToggle().getUserData();
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
