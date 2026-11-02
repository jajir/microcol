package org.akropia.model;

/**
 * Exception raised when there is not enough gold for operation.
 */
public final class NotEnoughtGoldException extends AkropiaException {

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
