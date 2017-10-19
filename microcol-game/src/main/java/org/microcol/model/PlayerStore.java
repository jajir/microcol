package org.microcol.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.microcol.gui.MicroColException;
import org.microcol.model.store.ModelPo;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class PlayerStore {

	private final List<Player> players;

	PlayerStore(final List<Player> players) {
		this.players = ImmutableList.copyOf(players);
		Preconditions.checkArgument(!this.players.isEmpty(), "There must be at least one player.");
		checkPlayersDuplicities(players);
	}
	
	static PlayerStore makePlayers(final Model model, final ModelPo modelPo){
		final List<Player> players = new ArrayList<>();
		modelPo.getPlayers().forEach(playerPo -> {
			players.add(Player.make(playerPo, model));
		});
		return new PlayerStore(players);
	}

	private void checkPlayersDuplicities(final List<Player> players) {
		Set<String> names = new HashSet<>();
		players.forEach(player -> {
			if (!names.add(player.getName())) {
				throw new IllegalArgumentException(String.format("Duplicate player name (%s).", player.getName()));
			}
		});
	}

	public List<Player> getPlayers() {
		return players;
	}

	public Player getPlayerByName(final String playerName) {
		return players.stream().filter(player -> player.getName().equals(playerName)).findAny()
				.orElseThrow(() -> new MicroColException(String.format("There is no such player (%s)", playerName)));
	}

}
