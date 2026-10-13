package org.microcol.model;

/**
 * Exception raised when there is not enough gold for operation.
 */
public final class NotEnoughtGoldException extends MicroColException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     *
     * @param message optional exception message
     */
    public NotEnoughtGoldException(final String message) {
        super(message);
    }

}
