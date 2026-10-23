package org.microcol.model;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Loss of a declared rebellion (R-0192). The surviving members are judged at a
 * completed boundary; the first condition in the priority order of
 * {@link GameOverEvaluator} that holds gives the single terminal reason.
 */
final class RebellionLoss {

    /** Crown-held share of all residents that ends the rebellion. */
    private static final int CROWN_RESIDENT_PERCENT = 90;

    private RebellionLoss() {
    }

    /**
     * @param model required game
     * @param rebel required declared colonial nation
     * @return the loss reason, or empty while the rebellion still holds
     */
    static Optional<GameOverResult> evaluate(final Model model,
            final Player rebel) {
        final Set<Integer> members = rebel.getRebellionColonyIds();
        final List<Colony> survivors = model.getColonies().stream()
                .filter(colony -> members.contains(colony.getId()))
                .collect(Collectors.toList());
        if (survivors.isEmpty()) {
            return reason(GameOverEvaluator.REASON_REBELLION_NO_COLONIES);
        }
        final Optional<PlayerKing> crown = model.getKingOf(rebel);
        if (survivors.stream().allMatch(colony -> isCrownHeld(crown, colony))) {
            return reason(
                    GameOverEvaluator.REASON_REBELLION_ALL_COLONIES_CROWN);
        }
        // An empty list of coastal members also satisfies this condition, so
        // inland-only survival is a loss.
        final List<Colony> coastal = survivors.stream()
                .filter(colony -> isCoastal(model, colony))
                .collect(Collectors.toList());
        if (coastal.stream().allMatch(colony -> isCrownHeld(crown, colony))) {
            return reason(GameOverEvaluator.REASON_REBELLION_COASTAL_CROWN);
        }
        // Current internal residents only; outside troops and declaration-time
        // population are not counted. Exact integer comparison.
        final long residents = survivors.stream()
                .mapToLong(Colony::getPopulation).sum();
        final long crownResidents = survivors.stream()
                .filter(colony -> isCrownHeld(crown, colony))
                .mapToLong(Colony::getPopulation).sum();
        if (isCrownResidentLoss(crownResidents, residents)) {
            return reason(GameOverEvaluator.REASON_REBELLION_RESIDENTS_CROWN);
        }
        return Optional.empty();
    }

    /**
     * Exact integer form of "the Crown holds at least 90 percent of all current
     * internal residents of the surviving members"; a zero denominator is a
     * loss. Ninety of 100 loses and 89 of 100 does not.
     *
     * @param crownResidents residents of the Crown-held surviving members
     * @param residents      residents of all surviving members
     * @return whether the population condition ends the rebellion
     */
    static boolean isCrownResidentLoss(final long crownResidents,
            final long residents) {
        return residents == 0
                || crownResidents * 100 >= residents * CROWN_RESIDENT_PERCENT;
    }

    private static boolean isCrownHeld(final Optional<PlayerKing> crown,
            final Colony colony) {
        return crown.isPresent() && colony.getOwner() == crown.get();
    }

    private static boolean isCoastal(final Model model, final Colony colony) {
        return colony.getLocation().getNeighbors().stream()
                .anyMatch(neighbour -> model.getMap().isValid(neighbour)
                        && model.getMap().getTerrainTypeAt(neighbour).isSea());
    }

    private static Optional<GameOverResult> reason(
            final String gameOverReason) {
        return Optional.of(new GameOverResult(gameOverReason));
    }
}
