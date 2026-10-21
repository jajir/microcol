package org.microcol.model;

/**
 * Land status of one colonial European on one native-claimed square. Paid and
 * waived squares cause no unpaid-use alarm. A paid right is reported before a
 * waiver, so an existing payment stays visible even after Minuit joins.
 */
public enum NativeLandStatus {

    /** No right yet; using the square causes unpaid-use pressure. */
    UNPAID,

    /** Bought once for this European and square. */
    PAID,

    /** Future land payments are waived by Peter Minuit's membership. */
    WAIVED;

}
