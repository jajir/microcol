package org.microcol.model;

/**
 * A foreign colony trade was confirmed on terms that no longer hold: the
 * carrier, the colony, the goods, the warehouse, the treasuries, the Europe
 * prices, the relation or the founder changed after the player saw them, or the
 * same offer was already used. Nothing was traded, so the caller explains the
 * refusal and shows the current state again.
 */
public final class ForeignColonyTermsChangedException
        extends IllegalStateException {

    private static final long serialVersionUID = 1L;

    ForeignColonyTermsChangedException(final String message) {
        super(message);
    }

}
