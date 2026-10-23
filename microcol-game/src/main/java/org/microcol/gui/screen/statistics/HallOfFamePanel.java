package org.microcol.gui.screen.statistics;

import java.util.Optional;

import org.microcol.gui.LocalizationHelper;
import org.microcol.i18n.I18n;
import org.microcol.model.HallOfFame;
import org.microcol.model.HallOfFameEntry;
import org.microcol.model.MicroColException;
import org.microcol.model.campaign.po.HallOfFameDao;

import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

/**
 * The persistent top ten (R-0197) and, when a scored result is given, that
 * result with its complete breakdown whether or not it is ranked. The result is
 * a snapshot, so it stays visible after its game is left. Each showing records
 * the snapshot through the shared path, so an earlier failed record is retried.
 * A storage failure is explained while the saved ranking that could still be
 * read and the result's breakdown stay visible; the saved file is never
 * rewritten after a failure.
 */
final class HallOfFamePanel extends VBox {

    private final HallOfFameDao dao;
    private final I18n i18n;
    private final LocalizationHelper localization;
    private final VBox rows = new VBox(8);

    HallOfFamePanel(final HallOfFameDao dao, final I18n i18n,
            final LocalizationHelper localization) {
        super(12);
        this.dao = dao;
        this.i18n = i18n;
        this.localization = localization;
        setId("hallOfFame");
        getStyleClass().add("relations-report");
        final ScrollPane scroll = new ScrollPane(rows);
        scroll.setId("hallOfFameScroll");
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(320);
        scroll.setPrefViewportWidth(440);
        getChildren().add(scroll);
    }

    /**
     * Refreshes the panel from the saved ranking every time it is shown.
     *
     * @param current the scored result to record and show; empty for plain
     *                browsing from the menu
     */
    void showReport(final Optional<HallOfFameEntry> current) {
        rows.getChildren().clear();
        HallOfFame ranking = null;
        boolean stored = true;
        try {
            ranking = dao.record(current);
        } catch (final MicroColException failure) {
            stored = false;
            rows.getChildren().add(line("hallOfFameStorage",
                    i18n.get(HallOfFameText.storageFailed)));
            ranking = readRankingOrNull();
        }
        if (ranking != null) {
            showRanking(ranking);
        }
        if (current.isPresent()) {
            showCurrent(current.get(), stored ? ranking : null);
        }
    }

    /**
     * @return the saved ranking, or null when the saved file cannot be read
     */
    private HallOfFame readRankingOrNull() {
        try {
            return dao.load();
        } catch (final MicroColException unreadable) {
            return null;
        }
    }

    private void showRanking(final HallOfFame ranking) {
        if (ranking.getEntries().isEmpty()) {
            rows.getChildren().add(
                    line("hallOfFameEmpty", i18n.get(HallOfFameText.empty)));
            return;
        }
        for (int index = 0; index < ranking.getEntries().size(); index++) {
            final HallOfFameEntry entry = ranking.getEntries().get(index);
            final int rank = index + 1;
            final Label row = line("hallOfFameRank" + rank, i18n.get(
                    HallOfFameText.row, rank, entry.getPlayerDisplayName(),
                    entry.getTotal(),
                    localization.getDifficultyName(entry.getDifficulty())));
            row.getStyleClass().add("hall-of-fame-row");
            rows.getChildren().add(row);
        }
    }

    /**
     * @param result  the scored result, shown whether or not it is ranked
     * @param ranking the saved ranking that now holds the result, or null when
     *                the result could not be saved
     */
    private void showCurrent(final HallOfFameEntry result,
            final HallOfFame ranking) {
        rows.getChildren().add(line("hallOfFameIdentity", i18n
                .get(HallOfFameText.currentIdentity, result.getIdentity())));
        if (ranking == null) {
            rows.getChildren().add(line("hallOfFameCurrent",
                    i18n.get(HallOfFameText.currentUnsaved)));
        } else {
            final Optional<Integer> rank = ranking
                    .getRank(result.getIdentity());
            rows.getChildren()
                    .add(rank.isPresent()
                            ? line("hallOfFameCurrent",
                                    i18n.get(HallOfFameText.currentRank,
                                            rank.get()))
                            : line("hallOfFameCurrent",
                                    i18n.get(HallOfFameText.notRanked)));
        }
        rows.getChildren().addAll(BaseScorePanel.scoreLines(i18n,
                result.getBaseScore(), result.getScore(), true));
    }

    private static Label line(final String id, final String text) {
        final Label label = new Label(text);
        label.setId(id);
        label.setWrapText(true);
        label.setMaxWidth(400);
        return label;
    }
}
