package org.microcol.gui.util;

import java.util.function.Consumer;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.MoreObjects.ToStringHelper;

/**
 * Wrap logic for conditional event called just one. Consumer is called just
 * when two conditions are triggered at least once.
 * <p>
 * It allow to trigger some event just when two conditions are true and it's not
 * sure order in which conditions will be passed.
 * </p>
 */
public final class OneTimeConditionalEvent<T> {

    private Consumer<T> onConditionsPassed;

    private boolean condition1;

    private boolean condition2;

    /**
     * Reset both conditions and wait again that they both pass.
     * 
     * @param onConditionsPassed
     *            required consumer that is called when conditions pass both
     *            again
     */
    public void setOnResetConditionsPassed(final Consumer<T> onConditionsPassed) {
        Preconditions.checkState(this.onConditionsPassed == null,
                "There is previous unprocessed event.");
        Preconditions.checkNotNull(onConditionsPassed);
        this.onConditionsPassed = onConditionsPassed;
        condition1 = false;
        condition2 = false;
    }

    /**
     * Call consumer when both conditions are passed. Conditions could already
     * be passes. In such case is consumer called immediately.
     * 
     * @param onConditionsPassed
     *            required condition to pass
     */
    public void setOnConditionsPassed(final Consumer<T> onConditionsPassed) {
        Preconditions.checkState(this.onConditionsPassed == null,
                "There is previous unprocessed event '%s'.", this.onConditionsPassed);
        Preconditions.checkNotNull(onConditionsPassed, "onConditionPassed consumer is null");
        executeIfNecessary(onConditionsPassed);
    }

    private void executeIfNecessary(final Consumer<T> onConditionsPassed) {
        if (condition1 && condition2 && onConditionsPassed != null) {
            onConditionsPassed.accept(null);
            this.onConditionsPassed = null;
        } else {
            this.onConditionsPassed = onConditionsPassed;
        }
    }

    public void setCondition1Passed() {
        condition1 = true;
        executeIfNecessary(onConditionsPassed);
    }

    private boolean isEventSet() {
        return onConditionsPassed != null;
    }

    public void setCondition2Passed() {
        condition2 = true;
        executeIfNecessary(onConditionsPassed);
    }

    @Override
    public String toString() {
        final ToStringHelper toStringHelper = MoreObjects.toStringHelper(getClass())
                .add("isEnabled", isEventSet());
        toStringHelper.add("condition1", condition1);
        toStringHelper.add("condition2", condition2);
        return toStringHelper.toString();
    }

}
