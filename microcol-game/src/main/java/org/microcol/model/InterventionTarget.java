package org.microcol.model;

import org.microcol.model.store.InterventionPo;

import com.google.common.base.Preconditions;

/**
 * Foreign-intervention bell target of one declared colonial nation (R-0189).
 * The target is fixed when it starts, so a later difficulty change does not
 * move it. Bells counted from the first owner update accumulate without a cap;
 * the part above the target is exactly {@link #getBellsBeyondTarget()}, and the
 * earning happens once. The delivery of the earned forces is kept apart from
 * the earning and happens once too (R-0190).
 */
public final class InterventionTarget {

    /** Bell base of the intervention rule, before the difficulty factor. */
    private static final int TARGET_BELLS = 500;

    private final int target;

    private long bells;

    private boolean earned;

    private boolean delivered;

    private InterventionTarget(final int target, final long bells,
            final boolean earned, final boolean delivered) {
        this.target = target;
        this.bells = bells;
        this.earned = earned;
        this.delivered = delivered;
    }

    /**
     * @param difficulty required selected difficulty
     * @return the target, {@code ceil(500 × k)} bells
     */
    public static int targetFor(final Difficulty difficulty) {
        Preconditions.checkNotNull(difficulty, "Difficulty is null");
        return (TARGET_BELLS * RecruitmentPool.factor(difficulty) + 99) / 100;
    }

    /**
     * Start the target at the first owner update after declaration.
     *
     * @param difficulty required selected difficulty
     * @return target with no bells counted yet
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
                        && po.getBells() != null && po.getBells() >= 0
                        && po.getEarned() != null && po.getDelivered() != null,
                "Incomplete intervention target");
        Preconditions.checkArgument(
                po.getEarned() == (po.getBells() >= po.getTarget()),
                "Intervention earning does not match its bells");
        Preconditions.checkArgument(!po.getDelivered() || po.getEarned(),
                "Intervention delivery needs an earned target");
        target = po.getTarget();
        bells = po.getBells();
        earned = po.getEarned();
        delivered = po.getDelivered();
    }

    /**
     * Check a credit without changing anything.
     *
     * @param produced bells the colonial nation actually produced
     */
    void checkCredit(final int produced) {
        Preconditions.checkArgument(produced >= 0,
                "Produced bells must be nonnegative");
        Preconditions.checkState(bells <= Long.MAX_VALUE - produced,
                "Intervention bells are exhausted");
    }

    /**
     * Count newly produced bells. Only the first credit that reaches the target
     * earns it.
     *
     * @param produced bells the colonial nation actually produced
     * @return whether this credit earns the intervention
     */
    boolean credit(final int produced) {
        checkCredit(produced);
        bells += produced;
        if (earned || bells < target) {
            return false;
        }
        earned = true;
        return true;
    }

    /**
     * @return fixed target in bells
     */
    public int getTarget() {
        return target;
    }

    /**
     * @return bells counted since the first owner update, including any above
     *         the target
     */
    public long getBells() {
        return bells;
    }

    /**
     * @return whether the cumulative bells reached the target; never reverts
     */
    public boolean isEarned() {
        return earned;
    }

    /**
     * @return bells counted above the target, which are the post-target part
     *         for later score accounting; zero before the target is reached
     */
    public long getBellsBeyondTarget() {
        return Math.max(0L, bells - target);
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
        po.setBells(bells);
        po.setEarned(earned);
        po.setDelivered(delivered);
        return po;
    }
}
