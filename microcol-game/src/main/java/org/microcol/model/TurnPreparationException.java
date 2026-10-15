package org.microcol.model;

/** The incomplete turn cannot be saved or accept player commands. */
public final class TurnPreparationException extends IllegalStateException {
    private static final long serialVersionUID = 1L;

    TurnPreparationException() {
        super("Turn preparation has not completed");
    }
}
