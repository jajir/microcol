package org.akropia.model;

/**
 * General game exception.
 */
public class AkropiaException extends RuntimeException {

    /**
     * Default serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    public AkropiaException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public AkropiaException(final String message) {
        super(message);
    }

}
