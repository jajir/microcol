package org.microcol.model;

import java.util.ArrayList;
import java.util.List;

import org.microcol.model.store.PlayerPo;

import com.google.common.base.Preconditions;

/**
 * Allow to easily create player.
 */
public class PlayerBuilder {

	private final ModelBuilder modelBuilder;
	
	private final PlayerPo playerPo;

	private final List<ColonyBuilder> colonyBuilders = new ArrayList<>();

	PlayerBuilder(final ModelBuilder modelBuilder, final String name) {
		this.modelBuilder = Preconditions.checkNotNull(modelBuilder);
		this.playerPo = new PlayerPo();
		playerPo.setName(Preconditions.checkNotNull(name));
	}

	public ModelBuilder build() {
		modelBuilder.getModelPo().getPlayers().add(playerPo);
		return modelBuilder;
	}
	
	public PlayerBuilder setGold(int gold) {
		playerPo.setGold(gold);
		return this;
	}

	public PlayerBuilder setComputerPlayer(boolean isComputerPlayer) {
		playerPo.setComputer(isComputerPlayer);
		return this;
	}

	public ColonyBuilder addColony(final String name) {
		final ColonyBuilder colonyBuilder = new ColonyBuilder(name, this, modelBuilder.getModelPo());
		colonyBuilders.add(colonyBuilder);
		return colonyBuilder;
	}

	/**
	 * @return the playerPo
	 */
	PlayerPo getPlayerPo() {
		return playerPo;
	}

}
