package org.microcol.model;

import java.util.ArrayList;
import java.util.List;

import org.microcol.model.store.ModelPo;
import org.microcol.model.store.StatisticsPo;

/**
 * Game statistics.
 */
public class Statistics {

    private final List<TurnPlayerStatistics> turnStatistics = new ArrayList<>();

    public Statistics(final ModelPo modelPo, final PlayerStore playerStore) {
        modelPo.getStatistics().getTurnStatistics().forEach(turnStatsPo -> {
            turnStatistics.add(TurnPlayerStatistics.make(turnStatsPo, playerStore));
        });
    }

    void countNextTurn(final Model model) {
        final int turnNo = model.getCalendar().getNumberOfPlayedTurns();
        model.getPlayers().forEach(player -> {
            final TurnPlayerStatistics stats = new TurnPlayerStatistics(player, turnNo);
            final PlayerStatistics playerStatistics = player.getPlayerStatistics();
            stats.setEcononyScore(playerStatistics.getGoodsStatistics().getEconomyValue());
            stats.setMilitaryScore(playerStatistics.getMilitaryStrength().getMilitaryStrength());
            turnStatistics.add(stats);
        });
    }

    StatisticsPo save() {
        final StatisticsPo out = new StatisticsPo();
        turnStatistics.forEach(stats -> {
            out.getTurnStatistics().add(stats.save());
        });
        return out;
    }

}
