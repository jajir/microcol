package org.akropia.model;

import java.util.List;
import java.util.Optional;
import org.akropia.model.store.ObjectiveResultPo;
import com.google.common.base.Preconditions;

/** Frozen objectives and turn of a completed free game. */
public final class ObjectiveResult {
    private final List<FreePlayGoal> enabled;
    private final List<FreePlayGoal> achieved;
    private final String winner;
    private final int turn;

    private ObjectiveResult(List<FreePlayGoal> enabled,
            List<FreePlayGoal> achieved, String winner, int turn) {
        this.enabled = List.copyOf(enabled);
        this.achieved = List.copyOf(achieved);
        Preconditions.checkArgument(enabled.containsAll(achieved) && turn >= 0,
                "Invalid objective result");
        Preconditions.checkArgument(achieved.isEmpty() == (winner == null),
                "Winner and objectives must agree");
        this.winner = winner;
        this.turn = turn;
    }

    static ObjectiveResult freeze(Model model) {
        FreePlayObjectives goals = model.getFreePlayObjectives();
        Player winner = goals.nations().stream()
                .filter(p -> !goals.achieved(p).isEmpty()).findFirst()
                .orElse(null);
        return new ObjectiveResult(goals.getEnabled(),
                winner == null ? List.of() : goals.achieved(winner),
                winner == null ? null : winner.getName(),
                model.getCalendar().getNumberOfPlayedTurns());
    }

    public static ObjectiveResult load(ObjectiveResultPo po) {
        Preconditions.checkNotNull(po, "Missing objective result");
        return new ObjectiveResult(po.getEnabled(), po.getAchieved(),
                po.getWinner(), po.getTurn());
    }

    public List<FreePlayGoal> getEnabled() {
        return enabled;
    }

    public List<FreePlayGoal> getAchieved() {
        return achieved;
    }

    public Optional<String> getWinner() {
        return Optional.ofNullable(winner);
    }

    public int getTurn() {
        return turn;
    }

    public ObjectiveResultPo save() {
        ObjectiveResultPo out = new ObjectiveResultPo();
        out.setEnabled(enabled);
        out.setAchieved(achieved);
        out.setWinner(winner);
        out.setTurn(turn);
        return out;
    }
}
