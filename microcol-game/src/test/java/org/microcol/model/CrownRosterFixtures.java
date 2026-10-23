package org.microcol.model;

import org.microcol.model.store.ModelPo;

/**
 * Setup for saved models whose difficulty a test changes before loading. The
 * saved Crown rosters of a fixture are sized for its own difficulty, so each
 * Crown is given the fresh roster of the new one.
 */
final class CrownRosterFixtures {

    private CrownRosterFixtures() {
    }

    /**
     * @param po saved model whose difficulty was just set
     */
    static void freshCrownRosters(final ModelPo po) {
        po.getGameManager().getPlayers().stream()
                .filter(player -> player.getWhosKingThisPlayerIs() != null)
                .forEach(player -> player.setRoyalRoster(
                        RoyalRoster.initial(po.getDifficulty())));
    }
}
