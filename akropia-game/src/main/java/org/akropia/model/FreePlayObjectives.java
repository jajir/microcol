package org.akropia.model;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.akropia.model.store.ModelPo;
import com.google.common.base.Preconditions;

/** Model-owned objectives and persistent colony-loss history. */
public final class FreePlayObjectives {
    private final Model model;
    private final Set<FreePlayGoal> enabled;
    private final Set<String> established;
    private final Set<String> eliminated;

    FreePlayObjectives(Model model, ModelPo saved) {
        this.model = model;
        enabled = EnumSet.noneOf(FreePlayGoal.class);
        enabled.addAll(Preconditions.checkNotNull(saved.getFreePlayGoals()));
        established = new HashSet<>(
                Preconditions.checkNotNull(saved.getEstablishedNations()));
        eliminated = new HashSet<>(
                Preconditions.checkNotNull(saved.getEliminatedNations()));
        Set<String> names = nations().stream().map(Player::getName)
                .collect(Collectors.toSet());
        Preconditions.checkArgument(
                names.containsAll(established)
                        && established.containsAll(eliminated),
                "Invalid colony-loss history");
        for (Player nation : nations()) {
            if (!model.getColonies(nation).isEmpty()) {
                Preconditions.checkArgument(
                        !eliminated.contains(nation.getName()),
                        "An eliminated nation owns colonies");
                established.add(nation.getName());
            }
        }
    }

    public List<FreePlayGoal> getEnabled() {
        return List.copyOf(enabled);
    }

    public List<Player> nations() {
        return model.getPlayers().stream().filter(Player::isColonialNation)
                .sorted(java.util.Comparator
                        .comparingInt((Player player) -> GameManager
                                .nationRank(player.getName()))
                        .thenComparing(Player::getName))
                .collect(Collectors.toList());
    }

    public boolean isEliminated(Player player) {
        return eliminated.contains(player.getName());
    }

    public long remainingOpponents(Player player) {
        return nations().stream().filter(p -> p != player && !isEliminated(p))
                .count();
    }

    public boolean hasOpponents() {
        return nations().size() > 1;
    }

    public long remainingRoyalGround(Player player) {
        return model.getKingOf(player)
                .map(p -> (long) p.getRoyalRoster().getLivingGroundCount())
                .orElse(0L);
    }

    public long occupiedRebellionColonies(Player player) {
        return model.getColonies().stream()
                .filter(c -> player.getRebellionColonyIds().contains(c.getId())
                        && c.getOwner() != player)
                .count();
    }

    void updateColonyHistory() {
        for (Player nation : nations()) {
            if (!model.getColonies(nation).isEmpty())
                established.add(nation.getName());
            else if (established.contains(nation.getName()))
                eliminated.add(nation.getName());
        }
    }

    public List<FreePlayGoal> achieved(Player player) {
        if (isEliminated(player))
            return List.of();
        List<FreePlayGoal> out = new ArrayList<>();
        if (enabled.contains(FreePlayGoal.INDEPENDENCE)
                && player.getRebellionVictoryOrder().isPresent())
            out.add(FreePlayGoal.INDEPENDENCE);
        if (enabled.contains(FreePlayGoal.CONGRESS) && player
                .getCongressMembers().size() == Founder.values().length)
            out.add(FreePlayGoal.CONGRESS);
        if (enabled.contains(FreePlayGoal.DOMINATION) && hasOpponents()
                && remainingOpponents(player) == 0)
            out.add(FreePlayGoal.DOMINATION);
        return List.copyOf(out);
    }

    void save(ModelPo out) {
        out.setFreePlayGoals(getEnabled());
        out.setEstablishedNations(
                established.stream().sorted().collect(Collectors.toList()));
        out.setEliminatedNations(
                eliminated.stream().sorted().collect(Collectors.toList()));
    }
}
