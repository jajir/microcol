package org.microcol.gui.util;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Provide methods to read scenarios and load and save game.
 */
public class PersistentSrevice {

	private final List<Scenario> scenarios = Lists.newArrayList(
			new Scenario("Simple tiny world", "/maps/map-small.json"));

	public List<Scenario> getScenarios() {
		return scenarios;
	}

}
