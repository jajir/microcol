package org.microcol.gui.util;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Provide methods to read scenarios and load and save game.
 */
public class PersistentSrevice {

	private final List<Scenario> scenarios = Lists.newArrayList(
			new Scenario("Simple tiny world", "/maps/map-small.json"),
			new Scenario("Declare Independence", "/maps/map-declare-independence.json"));

	public List<Scenario> getScenarios() {
		return scenarios;
	}
	
	//XXX add methods for save & load game model
}
