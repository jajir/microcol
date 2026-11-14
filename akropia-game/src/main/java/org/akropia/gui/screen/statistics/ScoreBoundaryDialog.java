package org.akropia.gui.screen.statistics;

import org.akropia.gui.util.ActionButtons;
import org.akropia.gui.image.ButtonIcon;
import org.akropia.gui.event.model.GameModelController;
import org.akropia.gui.util.AbstractMessageWindow;
import org.akropia.gui.util.ModalActions;
import org.akropia.gui.util.ViewUtil;
import org.akropia.i18n.I18n;
import org.akropia.model.BaseScore;
import org.akropia.model.GameResult;
import org.akropia.model.HallOfFame;
import org.akropia.model.AkropiaException;
import org.akropia.model.Model;
import org.akropia.model.Player;
import org.akropia.model.ResultScore;
import org.akropia.model.campaign.po.HallOfFameDao;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Shows the frozen result of spring 1800 with its named components, its place
 * in the Hall of Fame (R-0197) or the reason it could not be saved, and the one
 * unscored continuation. Continuing keeps the frozen score; deciding later
 * keeps the game waiting at the boundary, so nothing continues by default. A
 * storage failure is explained here and never blocks either choice.
 */
public final class ScoreBoundaryDialog extends AbstractMessageWindow {
    private final GameModelController game;
    private final Model model;
    private final Player player;
    private final I18n i18n;
    private final Label feedback = new Label();
    private final Button continueUnscored = ActionButtons
            .button(ButtonIcon.PLAY);
    private final Button later = ActionButtons.button(ButtonIcon.WAIT);

    public ScoreBoundaryDialog(final ViewUtil viewUtil, final I18n i18n,
            final GameModelController game, final Model model,
            final Player player, final GameResult result,
            final HallOfFameDao hallOfFameDao) {
        super(viewUtil, i18n);
        this.game = game;
        this.model = model;
        this.player = player;
        this.i18n = i18n;
        setTitle(i18n.get(ScoreBoundaryText.title));
        final BaseScore base = result.getBaseScore();
        final ResultScore score = result.getScore();
        final Label summary = wrapped(
                new Label(
                        i18n.get(ScoreBoundaryText.summary, score.getTotal())),
                "scoreBoundarySummary");
        final Label parts = wrapped(new Label(
                i18n.get(ScoreBoundaryText.parts, base.getPopulationPoints(),
                        base.getFatherPoints(), base.getGoldPoints(),
                        base.getSupportPoints(), base.getSubtotal())),
                "scoreBoundaryParts");
        final Label adjustments = wrapped(
                new Label(i18n.get(ScoreBoundaryText.adjustments,
                        score.getDestructionDeduction(),
                        score.getOwlsAfterTarget(),
                        score.getOrderBonusPercent(), score.getEarlyPoints(),
                        score.getDifficultyPercent())),
                "scoreBoundaryAdjustments");
        final Label ranking = rankingStatus(result, hallOfFameDao);
        final Label unscored = wrapped(
                new Label(i18n.get(ScoreBoundaryText.unscored)),
                "scoreBoundaryUnscored");
        continueUnscored.setText(i18n.get(ScoreBoundaryText.continueUnscored));
        continueUnscored.setId("scoreBoundaryContinue");
        continueUnscored.setDefaultButton(false);
        continueUnscored.setOnAction(event -> onContinue());
        later.setText(i18n.get(ScoreBoundaryText.decideLater));
        later.setId("scoreBoundaryLater");
        later.setCancelButton(true);
        later.setOnAction(event -> close());
        feedback.setId("scoreBoundaryFeedback");
        feedback.setWrapText(true);
        feedback.setMinHeight(Region.USE_PREF_SIZE);
        final VBox root = new VBox(16, summary, parts, adjustments, ranking,
                unscored, feedback, new ModalActions(continueUnscored, later));
        root.setId("scoreBoundaryDialog");
        root.setPadding(new Insets(20));
        root.setPrefWidth(620);
        init(root);
    }

    /**
     * The place of the frozen result in the Hall of Fame, or the storage
     * failure that kept it from being recorded. Reading the ranking does not
     * change the choice that the window offers.
     */
    private Label rankingStatus(final GameResult result,
            final HallOfFameDao hallOfFameDao) {
        final Label status = wrapped(new Label(), "scoreBoundaryRanking");
        try {
            final HallOfFame ranking = hallOfFameDao.recordFrozenResult(model);
            status.setText(ranking.getRank(result.getIdentity())
                    .map(rank -> i18n.get(HallOfFameText.currentRank, rank))
                    .orElseGet(() -> i18n.get(HallOfFameText.notRanked)));
        } catch (final AkropiaException failure) {
            status.setText(i18n.get(HallOfFameText.storageFailed));
        }
        return status;
    }

    private Label wrapped(final Label label, final String id) {
        label.setId(id);
        label.setWrapText(true);
        label.setMinHeight(Region.USE_PREF_SIZE);
        return label;
    }

    private boolean isCurrent() {
        return game.isGameModelReady() && game.getModel() == model
                && model.isAwaitingScoreChoice();
    }

    private void onContinue() {
        try {
            if (!isCurrent()) {
                throw new IllegalStateException("Score boundary changed");
            }
            model.continueWithoutScoring(player);
            close();
        } catch (IllegalArgumentException | IllegalStateException rejected) {
            continueUnscored.setDisable(true);
            feedback.setText(i18n.get(ScoreBoundaryText.changed));
        }
    }
}
