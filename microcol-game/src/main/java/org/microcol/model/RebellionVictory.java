package org.microcol.model;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Victory of a declared rebellion (R-0191). It is judged only after
 * {@link RebellionLoss} found no loss at the same boundary, so a loss always
 * wins. The rebels must hold every surviving member, and the Crown may keep at
 * most two living ground units in total; ships alone do not prevent victory.
 */
final class RebellionVictory {

    /** Living Crown ground units that still allow the rebels to win. */
    private static final int MAX_CROWN_GROUND = 2;

    private RebellionVictory() {
    }

    /**
     * @param model required game
     * @param rebel required declared colonial nation without a recorded loss
     * @return the victory result, or empty while the rebellion is not won
     */
    static Optional<GameOverResult> evaluate(final Model model,
            final Player rebel) {
        final Set<Integer> members = rebel.getRebellionColonyIds();
        final List<Colony> survivors = model.getColonies().stream()
                .filter(colony -> members.contains(colony.getId()))
                .collect(Collectors.toList());
        if (survivors.isEmpty() || survivors.stream()
                .anyMatch(colony -> colony.getOwner() != rebel)) {
            return Optional.empty();
        }
        return model.getKingOf(rebel)
                .filter(crown -> crown.getRoyalRoster()
                        .getLivingGroundCount() <= MAX_CROWN_GROUND)
                .map(crown -> new GameOverResult(
                        GameOverEvaluator.REASON_REBELLION_VICTORY));
    }
}
