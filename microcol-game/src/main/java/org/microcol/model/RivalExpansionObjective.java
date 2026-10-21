package org.microcol.model;

import java.util.Optional;

import org.microcol.model.store.RivalExpansionObjectivePo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * A rival colonial player's single second-colony expansion: the colony that
 * releases the founder, the founder, the chosen known site and, for a sea
 * journey, the carrier. All identities are stored as plain values. The owner
 * checks them against live ownership before every step, so a destroyed or
 * captured object retires instead of being used.
 */
public final class RivalExpansionObjective {

    /** Journey stage of the founder. */
    public enum Stage {
        /** Founder released onto the colony square, waiting to board. */
        BOARDING,
        /**
         * Founder walking, or aboard or landed from its carrier, travelling to
         * the site to found.
         */
        TRAVEL
    }

    private final int colonyId;

    private final int founderId;

    /** Null when the founder walks overland to the site. */
    private final Integer carrierId;

    private final Location site;

    private final Stage stage;

    /**
     * @param colonyId  required identity of the colony that releases the
     *                  founder
     * @param founderId required identity of the spare colonist that founds
     * @param carrierId identity of the ship that carries the founder, or null
     *                  for an overland walk; boarding needs a ship
     * @param site      required known legal site of the new colony
     * @param stage     required stage of the journey
     */
    public RivalExpansionObjective(final int colonyId, final int founderId,
            final Integer carrierId, final Location site, final Stage stage) {
        Preconditions.checkNotNull(site, "Site is null");
        Preconditions.checkNotNull(stage, "Stage is null");
        Preconditions.checkArgument(carrierId != null || stage == Stage.TRAVEL,
                "Boarding needs a carrier");
        this.colonyId = colonyId;
        this.founderId = founderId;
        this.carrierId = carrierId;
        this.site = site;
        this.stage = stage;
    }

    /**
     * Restore a saved expansion. Missing identities or an unknown stage are
     * rejected instead of guessed; stale identities are kept for retirement.
     *
     * @param po required saved expansion
     * @return the restored expansion
     */
    static RivalExpansionObjective load(final RivalExpansionObjectivePo po) {
        Preconditions.checkArgument(
                po.getColonyId() != null && po.getFounderId() != null
                        && po.getSite() != null && po.getStage() != null,
                "Rival expansion identity is missing");
        return new RivalExpansionObjective(po.getColonyId(), po.getFounderId(),
                po.getCarrierId(), po.getSite(), Stage.valueOf(po.getStage()));
    }

    RivalExpansionObjectivePo save() {
        final RivalExpansionObjectivePo out = new RivalExpansionObjectivePo();
        out.setColonyId(colonyId);
        out.setFounderId(founderId);
        out.setCarrierId(carrierId);
        out.setSite(site);
        out.setStage(stage.name());
        return out;
    }

    public int getColonyId() {
        return colonyId;
    }

    public int getFounderId() {
        return founderId;
    }

    /**
     * @return the carrier of a sea journey; empty for an overland walk
     */
    public Optional<Integer> getCarrierId() {
        return Optional.ofNullable(carrierId);
    }

    public Location getSite() {
        return site;
    }

    public Stage getStage() {
        return stage;
    }

    /**
     * @param next required stage of the journey, same identities and site
     * @return the expansion moved to that stage
     */
    public RivalExpansionObjective withStage(final Stage next) {
        return new RivalExpansionObjective(colonyId, founderId, carrierId, site,
                next);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("colonyId", colonyId)
                .add("founderId", founderId).add("carrierId", carrierId)
                .add("site", site).add("stage", stage).toString();
    }
}
