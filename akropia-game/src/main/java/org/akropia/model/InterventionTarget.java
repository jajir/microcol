package org.akropia.model;

import org.akropia.model.store.InterventionPo;

import com.google.common.base.Preconditions;

/**
 * Foreign-intervention owl target of one declared colonial nation (R-0189). The
 * target is fixed when it starts, so a later difficulty change does not move
 * it. Owls counted from the first owner update accumulate without a cap; the
 * part above the target is exactly {@link #getOwlsBeyondTarget()}, and the
 * earning happens once. The delivery of the earned forces is kept apart from
 * the earning and happens once too (R-0190).
 */
public final class InterventionTarget {

    /** Owl base of the intervention rule, before the difficulty factor. */
    private static final int TARGET_OWLS = 500;

    private final int target;

    private long owls;

    private boolean earned;

    private boolean delivered;

    private InterventionTarget(final int target, final long owls,
            final boolean earned, final boolean delivered) {
        this.target = target;
        this.owls = owls;
        this.earned = earned;
        this.delivered = delivered;
    }

    /**
     * @param difficulty required selected difficulty
     * @return the target, {@code ceil(500 × k)} owls
     */
    public static int targetFor(final Difficulty difficulty) {
        Preconditions.checkNotNull(difficulty, "Difficulty is null");
        return (TARGET_OWLS * RecruitmentPool.factor(difficulty) + 99) / 100;
    }

    /**
     * Start the target at the first owner update after declaration.
     *
     * @param difficulty required selected difficulty
     * @return target with no owls counted yet
     */
    static InterventionTarget start(final Difficulty difficulty) {
        return new InterventionTarget(targetFor(difficulty), 0L, false, false);
    }

    /**
     * Load a saved target; an incomplete or incoherent state is rejected.
     *
     * @param po required saved target
     */
    InterventionTarget(final InterventionPo po) {
        Preconditions.checkArgument(po != null, "Missing intervention target");
        Preconditions.checkArgument(
                po.getTarget() != null && po.getTarget() > 0
                        && po.getOwls() != null && po.getOwls() >= 0
                        && po.getEarned() != null && po.getDelivered() != null,
                "Incomplete intervention target");
        Preconditions.checkArgument(
                po.getEarned() == (po.getOwls() >= po.getTarget()),
                "Intervention earning does not match its owls");
        Preconditions.checkArgument(!po.getDelivered() || po.getEarned(),
                "Intervention delivery needs an earned target");
        target = po.getTarget();
        owls = po.getOwls();
        earned = po.getEarned();
        delivered = po.getDelivered();
    }

    /**
     * Check a credit without changing anything.
     *
     * @param produced owls the colonial nation actually produced
     */
    void checkCredit(final int produced) {
        Preconditions.checkArgument(produced >= 0,
                "Produced owls must be nonnegative");
        Preconditions.checkState(owls <= Long.MAX_VALUE - produced,
                "Intervention owls are exhausted");
    }

    /**
     * Count newly produced owls. Only the first credit that reaches the target
     * earns it.
     *
     * @param produced owls the colonial nation actually produced
     * @return whether this credit earns the intervention
     */
    boolean credit(final int produced) {
        checkCredit(produced);
        owls += produced;
        if (earned || owls < target) {
            return false;
        }
        earned = true;
        return true;
    }

    /**
     * @return fixed target in owls
     */
    public int getTarget() {
        return target;
    }

    /**
     * @return owls counted since the first owner update, including any above
     *         the target
     */
    public long getOwls() {
        return owls;
    }

    /**
     * @return whether the cumulative owls reached the target; never reverts
     */
    public boolean isEarned() {
        return earned;
    }

    /**
     * @return owls counted above the target, which are the post-target part for
     *         later score accounting; zero before the target is reached
     */
    public long getOwlsBeyondTarget() {
        return Math.max(0L, owls - target);
    }

    /**
     * @return whether the earned forces were delivered to the map; never
     *         reverts
     */
    public boolean isDelivered() {
        return delivered;
    }

    /**
     * Record the delivery once the model has created every delivered unit. The
     * delivery is separate from the earning, so it needs an earned target.
     */
    void markDelivered() {
        Preconditions.checkState(earned && !delivered,
                "Only an earned, undelivered intervention is delivered");
        delivered = true;
    }

    InterventionPo save() {
        final InterventionPo po = new InterventionPo();
        po.setTarget(target);
        po.setOwls(owls);
        po.setEarned(earned);
        po.setDelivered(delivered);
        return po;
    }
}
