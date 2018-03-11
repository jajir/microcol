package org.microcol.gui;

/**
 * General game exception.
 */
public class MicroColException extends RuntimeException {

    /**
     * Default serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    public MicroColException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public MicroColException(final String message) {
        super(message);
    }

}
