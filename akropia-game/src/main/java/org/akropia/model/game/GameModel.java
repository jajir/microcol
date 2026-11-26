package org.akropia.model.game;

import org.akropia.model.Model;
import org.akropia.model.ModelListener;
import org.akropia.model.game.po.GameModelPo;
import org.akropia.model.game.po.SaveSummaryPo;
import com.google.common.base.Preconditions;

/** A running game and its save-library identity. */
public final class GameModel {
    private String playthroughId = java.util.UUID.randomUUID().toString();
    private final Model model;

    GameModel(final Model model) {
        this.model = Preconditions.checkNotNull(model);
    }

    public String getPlaythroughId() {
        return playthroughId;
    }

    void restorePlaythroughId(final String value) {
        if (value != null)
            playthroughId = java.util.UUID.fromString(value).toString();
    }

    public SaveSummaryPo getSaveSummary() {
        return SaveSummaryPo.describe(model);
    }

    GameModelPo getModelPo() {
        final GameModelPo out = new GameModelPo();
        out.setModel(model.save());
        out.setPlaythroughId(playthroughId);
        return out;
    }

    public Model getModel() {
        return model;
    }

    public void addListener(final ModelListener listener) {
        model.addListener(listener);
    }

    public void startGame() {
        model.startGame();
    }

    public void stop() {
        model.stop();
    }

    public static java.util.List<java.util.function.Function<Model, org.akropia.model.GameOverResult>> gameOverEvaluators() {
        return java.util.List.of(
                org.akropia.model.GameOverEvaluator.GAMEOVER_CONDITION_CALENDAR,
                org.akropia.model.GameOverEvaluator.GAMEOVER_CONDITION_HUMAN_LOST_ALL_COLONIES);
    }
}
