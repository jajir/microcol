package org.microcol.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.microcol.gui.MicroColException;
import org.microcol.model.store.ModelPo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public final class PlayerStore {

    private final List<Player> players;

    PlayerStore() {
        this.players = new ArrayList<>();
        checkPlayersDuplicities();
    }

    static PlayerStore makePlayers(final Model model, final ModelPo modelPo) {
        /*
         * First are created player whose are not kings. Next are created kings.
         * It's because kings in constructor required player who is under kings
         * rule.
         */
        final PlayerStore out = new PlayerStore();
        modelPo.getGameManager().getPlayers().stream()
                .filter(player -> player.getWhosKingThisPlayerIs() == null).forEach(playerPo -> {
                    out.players.add(Player.make(playerPo, model, out));
                });
        modelPo.getGameManager().getPlayers().stream()
                .filter(player -> player.getWhosKingThisPlayerIs() != null).forEach(playerPo -> {
                    out.players.add(Player.make(playerPo, model, out));
                });
        out.checkPlayersDuplicities();
        Preconditions.checkArgument(!out.players.isEmpty(), "There must be at least one player.");
        return out;
    }

    private void checkPlayersDuplicities() {
        Set<String> names = new HashSet<>();
        players.forEach(player -> {
            if (!names.add(player.getName())) {
                throw new IllegalArgumentException(
                        String.format("Duplicate player name (%s).", player.getName()));
            }
        });
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Player getPlayerByName(final String playerName) {
        return players.stream().filter(player -> player.getName().equals(playerName)).findAny()
                .orElseThrow(() -> new MicroColException(
                        String.format("There is no such player (%s)", playerName)));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("hashcode", hashCode())
                .add("players", players.size()).toString();
    }

}
