package org.microcol.model;

import java.util.List;
import java.util.Random;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class ColonyNames {

	private final String lexicon = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	private final Random rand = new Random();

	private final List<String> dutchNames = Lists.newArrayList("New Amsterdam", "Delft", "Haarlem", "Leiden",
			"Rotterdam", "Utrecht", "Eindhoven", "Tilburg", "Groningen", "Almere Stad", "Breda");

	private final Model model;

	ColonyNames(final Model model) {
		this.model = Preconditions.checkNotNull(model);
	}
	
	String getNewColonyName(final Player player){
		for(final String colonyName: dutchNames){
			if(isColonyNameAvailable(player, colonyName)){
				return colonyName;
			}
		}
		return randomColonyName(player);
	}

	private String randomColonyName(final Player player) {
		String colonyName = randomColonyName();
		while (!isColonyNameAvailable(player, colonyName)) {
			colonyName = randomColonyName();
		}
		return colonyName;
	}

	private boolean isColonyNameAvailable(final Player player, final String colonyName) {
		return !model.getColonies(player).stream().filter(colony -> colonyName.equals(colony.getName())).findAny()
				.isPresent();
	}

	private String randomColonyName() {
		StringBuilder builder = new StringBuilder();
		while (builder.toString().length() == 0) {
			int length = rand.nextInt(5) + 5;
			for (int i = 0; i < length; i++) {
				builder.append(lexicon.charAt(rand.nextInt(lexicon.length())));
			}
		}
		return builder.toString();
	}

}
