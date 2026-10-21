package org.microcol.model;

/**
 * A native sale or gift was confirmed on terms that no longer hold: the goods,
 * the amount, the gold, the treasury, the Europe bid or the haggle state
 * changed after the player saw them. Nothing was traded, so the caller explains
 * the refusal and shows the current offer again.
 */
public final class NativeTermsChangedException extends IllegalStateException {

    private static final long serialVersionUID = 1L;

    NativeTermsChangedException(final String message) {
        super(message);
    }

}
