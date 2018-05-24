package org.microcol.model.store;

import java.util.ArrayList;
import java.util.List;

public class StatisticsPo {

    private List<TurnPlayerStatisticsPo> turnStatistics;

    public StatisticsPo() {
        turnStatistics = new ArrayList<>();
    }

    /**
     * @return the turnStatistics
     */
    public List<TurnPlayerStatisticsPo> getTurnStatistics() {
        return turnStatistics;
    }

    /**
     * @param turnStatistics the turnStatistics to set
     */
    public void setTurnStatistics(List<TurnPlayerStatisticsPo> turnStatistics) {
        this.turnStatistics = turnStatistics;
    }
}
