package org.microcol.gui.screen.statistics;

import org.microcol.gui.util.ActionButtons;
import org.microcol.gui.image.ButtonIcon;
import java.util.List;
import java.util.Optional;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.BaseScore;
import org.microcol.model.GameResult;
import org.microcol.model.Model;
import org.microcol.model.ResultScore;
import org.microcol.model.campaign.po.HallOfFameDao;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

/**
 * Base score and calculated result of the human nation. Once a result is frozen
 * (R-0193) the panel shows that frozen result with its identity and never the
 * live values; before that it says the values are a live preview. While a
 * choice waits at spring 1800 the panel reopens it.
 */
final class BaseScorePanel extends VBox {
    private final GameModelController game;
    private final ViewUtil viewUtil;
    private final I18n i18n;
    private final HallOfFameDao hallOfFameDao;
    private final VBox rows = new VBox(12);

    BaseScorePanel(final GameModelController game, final ViewUtil viewUtil,
            final I18n i18n, final HallOfFameDao hallOfFameDao) {
        super(12);
        this.game = game;
        this.viewUtil = viewUtil;
        this.i18n = i18n;
        this.hallOfFameDao = hallOfFameDao;
        setId("baseScore");
        getStyleClass().add("relations-report");
        // The base and calculated rows together are taller than the panel.
        final ScrollPane scroll = new ScrollPane(rows);
        scroll.setId("baseScoreScroll");
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(320);
        scroll.setPrefViewportWidth(400);
        getChildren().add(scroll);
    }

    /** Refreshes the preview from the model every time it is shown. */
    void showReport() {
        rows.getChildren().clear();
        if (!game.isGameModelReady()) {
            rows.getChildren().add(line("baseScoreUnavailable",
                    i18n.get(Stats.baseScoreUnavailable)));
            return;
        }
        final Model model = game.getModel();
        final Optional<GameResult> frozen = model.getFrozenResult();
        if (frozen.isPresent()) {
            rows.getChildren().addAll(
                    line("frozenTitle", i18n.get(FrozenScoreText.frozenTitle)),
                    line("frozenIdentity",
                            i18n.get(FrozenScoreText.frozenIdentity,
                                    frozen.get().getIdentity())),
                    line("frozenStatus", i18n.get(statusText(frozen.get()))));
            showScore(frozen.get().getBaseScore(), frozen.get().getScore(),
                    true);
        } else {
            rows.getChildren().add(
                    line("liveTitle", i18n.get(FrozenScoreText.liveTitle)));
            showScore(game.getHumanPlayer().getBaseScore(),
                    game.getHumanPlayer().getResultScore(), false);
        }
        if (model.isAwaitingScoreChoice()) {
            final Button decide = ActionButtons.button(ButtonIcon.CONFIRM,
                    i18n.get(FrozenScoreText.decideFrozen));
            decide.setId("decideFrozenScore");
            decide.setOnAction(event -> openDecision());
            rows.getChildren().add(0, decide);
        }
    }

    /** Reopens the spring 1800 choice; closing it refreshes this report. */
    private void openDecision() {
        final Model model = game.getModel();
        model.getFrozenResult().filter(result -> model.isAwaitingScoreChoice())
                .ifPresent(result -> new ScoreBoundaryDialog(viewUtil, i18n,
                        game, model, game.getHumanPlayer(), result,
                        hallOfFameDao).showAndWait());
        showReport();
    }

    private static FrozenScoreText statusText(final GameResult result) {
        switch (result.getStatus()) {
        case AWAITING_CONTINUATION:
            return FrozenScoreText.statusAwaiting;
        case CONTINUED_UNSCORED:
            return FrozenScoreText.statusContinued;
        default:
            return FrozenScoreText.statusFinal;
        }
    }

    /**
     * Shows the components. A frozen result carries its own titles, so it is
     * never presented as the live preview that nothing records.
     */
    private void showScore(final BaseScore score, final ResultScore result,
            final boolean frozen) {
        rows.getChildren().addAll(scoreLines(i18n, score, result, frozen));
    }

    /**
     * The components and the calculated result as labels. The Hall of Fame
     * shows the breakdown of its frozen result with the same labels (R-0197).
     *
     * @param i18n   required translator
     * @param score  required base score
     * @param result required calculated result of the same nation
     * @param frozen whether the values are a frozen result rather than a
     *               preview
     * @return the labels in display order
     */
    static List<Label> scoreLines(final I18n i18n, final BaseScore score,
            final ResultScore result, final boolean frozen) {
        return List.of(
                line("baseScoreTitle",
                        pick(i18n, frozen, FrozenScoreText.frozenBaseTitle,
                                Stats.baseScoreTitle)),
                line("baseScoreNote",
                        pick(i18n, frozen, FrozenScoreText.frozenBaseNote,
                                Stats.baseScoreNote)),
                line("baseScorePopulation", i18n.get(Stats.baseScorePopulation,
                        score.getPopulationPoints(), score.getServants(),
                        score.getColonists(), score.getExperts())),
                line("baseScoreFathers",
                        i18n.get(Stats.baseScoreFathers,
                                score.getFatherPoints(), score.getFathers())),
                line("baseScoreGold",
                        i18n.get(Stats.baseScoreGold, score.getGoldPoints(),
                                score.getGold())),
                line("baseScoreSupport", i18n.get(Stats.baseScoreSupport,
                        score.getSupportPoints(), score.getSupportPercent())),
                line("baseScoreSubtotal",
                        i18n.get(Stats.baseScoreSubtotal, score.getSubtotal())),
                line("resultTitle",
                        pick(i18n, frozen, FrozenScoreText.frozenResultTitle,
                                Stats.resultTitle)),
                line("resultNote",
                        pick(i18n, frozen, FrozenScoreText.frozenResultNote,
                                Stats.resultNote)),
                line("resultDeduction",
                        i18n.get(Stats.resultDeduction,
                                result.getDestroyedSettlements(),
                                result.getPointsPerSettlement(),
                                result.getDestructionDeduction())),
                line("resultBells",
                        i18n.get(Stats.resultBells,
                                result.getBellsAfterTarget())),
                line("resultClampedBase",
                        i18n.get(Stats.resultClampedBase,
                                result.getClampedBase())),
                line("resultBonus",
                        i18n.get(Stats.resultBonus,
                                result.getOrderBonusPercent(),
                                result.getEarlyPoints())),
                line("resultDifficulty",
                        i18n.get(Stats.resultDifficulty,
                                result.getDifficultyPercent())),
                line("resultTotal",
                        i18n.get(Stats.resultTotal, result.getTotal())));
    }

    private static String pick(final I18n i18n, final boolean frozen,
            final FrozenScoreText frozenKey, final Stats liveKey) {
        return frozen ? i18n.get(frozenKey) : i18n.get(liveKey);
    }

    private static Label line(final String id, final String text) {
        final Label label = new Label(text);
        label.setId(id);
        label.setWrapText(true);
        label.setMaxWidth(380);
        return label;
    }
}
