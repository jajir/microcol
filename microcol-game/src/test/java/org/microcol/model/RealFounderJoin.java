package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.store.CongressPo;
import org.microcol.model.store.ModelPo;

/**
 * Joins a founder the way play does: the saved Congress holds more than the
 * first target, the owner chooses the offer, and the owner's own update joins
 * the founder. Tests use this helper instead of setting the final membership,
 * so the recruitment hook itself runs before the continuous effect is observed.
 */
final class RealFounderJoin {
    private RealFounderJoin() {
    }

    /** The Congress of the named nation in a saved game. */
    static CongressPo congressOf(final GameModelPo data, final String nation) {
        return congressOf(data.getModel(), nation);
    }

    /** The Congress of the named nation in a saved model. */
    static CongressPo congressOf(final ModelPo data, final String nation) {
        return data.getGameManager().getPlayers().stream()
                .filter(player -> player.getName().equals(nation)).findFirst()
                .orElseThrow().getCongress();
    }

    /**
     * Ready the Congress so that one owner update joins the founder: no member
     * yet, the target bells, and one offer per category including the founder.
     */
    static void readyFor(final CongressPo congress, final Founder founder) {
        congress.setActivated(true);
        congress.setGeneration(1);
        // Above any difficulty's first target; a surplus carries over.
        congress.setBells(1000);
        congress.setMembers(List.of());
        congress.setSelected(null);
        congress.setJonesDeliveryPending(false);
        final List<Founder> candidates = new ArrayList<>();
        for (final Founder.Category category : Founder.Category.values()) {
            candidates.add(category == founder.getCategory() ? founder
                    : Arrays.stream(Founder.values())
                            .filter(other -> other.getCategory() == category
                                    && other != founder)
                            .findFirst().orElseThrow());
        }
        congress.setCandidates(candidates);
    }

    /**
     * The current owner chooses the founder and runs its ordinary update, which
     * credits the bells and joins the founder once.
     */
    static void joinThroughOwnerUpdate(final Model model,
            final Founder founder) {
        final Player owner = model.getCurrentPlayer();
        owner.chooseFounder(owner.getFounderOffers().stream()
                .filter(offer -> offer.getFounder() == founder).findFirst()
                .orElseThrow());
        assertTrue(owner.startTurn());
        assertTrue(owner.getCongressMembers().contains(founder));
    }
}
