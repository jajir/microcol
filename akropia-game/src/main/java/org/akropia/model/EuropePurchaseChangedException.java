package org.akropia.model;

import com.google.common.base.Preconditions;

/**
 * Exception raised when a shown European purchase choice is no longer current
 * at confirmation. Nothing was charged or stored.
 */
public final class EuropePurchaseChangedException extends AkropiaException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /** The first fact that changed since the choice was shown. */
    public enum Reason {
        /** Market access ended or the commodity became boycotted. */
        TRADE,
        /** The target hold changed. */
        HOLD,
        /** The unit price or the tax changed. */
        PRICE,
        /** The treasury changed. */
        GOLD
    }

    private final Reason reason;

    /**
     * Default constructor.
     *
     * @param reason required first changed fact
     */
    public EuropePurchaseChangedException(final Reason reason) {
        super("European purchase changed (" + reason + "); nothing was traded");
        this.reason = Preconditions.checkNotNull(reason);
    }

    /** @return the first fact that changed */
    public Reason getReason() {
        return reason;
    }

}
