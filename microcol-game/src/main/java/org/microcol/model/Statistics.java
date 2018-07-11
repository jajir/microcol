package org.microcol.model;

import java.util.ArrayList;
import java.util.List;

import org.microcol.model.store.ModelPo;
import org.microcol.model.store.StatisticsPo;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Game statistics.
 */
public final class Statistics {

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
            stats.setGold(playerStatistics.getGold());
            stats.setMilitaryScore(playerStatistics.getMilitaryStrength().getMilitaryStrength());
            stats.setScore(stats.getEcononyScore() + stats.getMilitaryScore() + stats.getGold());
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

    public List<TurnPlayerStatistics> getStatsForPlayer(final Player player) {
        Preconditions.checkNotNull(player);
        return turnStatistics.stream().filter(stat -> stat.getPlayer().equals(player))
                .collect(ImmutableList.toImmutableList());
    }

}
