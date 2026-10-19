package org.microcol.model;

/**
 * Exception raised when the European price moved after the player confirmed a
 * quote. Nothing was charged or traded.
 */
public final class EuropePriceChangedException extends MicroColException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     *
     * @param message optional exception message
     */
    public EuropePriceChangedException(final String message) {
        super(message);
    }

}
