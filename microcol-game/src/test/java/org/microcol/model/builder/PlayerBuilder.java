package org.microcol.model.builder;

import java.util.ArrayList;
import java.util.List;

import org.microcol.model.IdManager;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.VisibilityPo;

import com.google.common.base.Preconditions;

/**
 * Allow to easily create player.
 */
public class PlayerBuilder {

    private final ModelBuilder modelBuilder;

    private final IdManager idManager;

    private final PlayerPo playerPo;

    private final List<ColonyBuilder> colonyBuilders = new ArrayList<>();

    PlayerBuilder(final ModelBuilder modelBuilder, final String name, final IdManager idManager) {
        this.modelBuilder = Preconditions.checkNotNull(modelBuilder);
        this.idManager = Preconditions.checkNotNull(idManager);
        this.playerPo = new PlayerPo();
        playerPo.setName(Preconditions.checkNotNull(name));
        playerPo.setVisible(new VisibilityPo());
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
        final ColonyBuilder colonyBuilder = new ColonyBuilder(name, this, idManager);
        colonyBuilders.add(colonyBuilder);
        return colonyBuilder;
    }

    /**
     * @return the playerPo
     */
    PlayerPo getPlayerPo() {
        return playerPo;
    }

    ModelPo getModelPo() {
        return modelBuilder.getModelPo();
    }

}
