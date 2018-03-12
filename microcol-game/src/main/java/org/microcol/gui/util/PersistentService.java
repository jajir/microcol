package org.microcol.gui.util;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Provide methods to read scenarios and load and save game.
 */
public class PersistentService {

    /**
     * Define testing game scenarios.
     */
    private final List<Scenario> scenarios = Lists.newArrayList(
            new Scenario("Simple tiny world", "/maps/01-map-small.json"),
            new Scenario("Declare Independence", "/maps/02-map-declare-independence.json"),
            new Scenario("Almost game over", "/maps/03-before-game-over.json"));

    public List<Scenario> getScenarios() {
        return scenarios;
    }

    /**
     * Return scenario for game start.
     * 
     * @return return game scenario
     */
    public Scenario getDefaultScenario() {
        return scenarios.get(0);
    }

}
