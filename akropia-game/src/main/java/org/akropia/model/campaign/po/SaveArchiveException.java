package org.akropia.model.campaign.po;

import org.akropia.model.AkropiaException;

/**
 * A saved-game archive cannot be read. The reason distinguishes an unsupported
 * format version from damaged or unexpected content.
 */
public final class SaveArchiveException extends AkropiaException {

    private static final long serialVersionUID = 1L;

    public enum Reason {
        INVALID, UNSUPPORTED
    }

    private final Reason reason;

    SaveArchiveException(final Reason reason, final String message) {
        super(message);
        this.reason = reason;
    }

    SaveArchiveException(final Reason reason, final String message,
            final Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
