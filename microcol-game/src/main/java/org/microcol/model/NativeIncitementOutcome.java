package org.microcol.model;

/**
 * Actual result of one confirmed incitement (R-0166). A failure still costs the
 * price; only a success sets a tribe's target.
 */
public enum NativeIncitementOutcome {

    /** The tribe turns on the chosen European; its order is installed. */
    SUCCESS,

    /** The missionary failed; the previous order stays as it was. */
    FAILURE

}
