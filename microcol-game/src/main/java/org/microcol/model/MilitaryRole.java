package org.microcol.model;

/**
 * Persistent combat role of an armed colonist. {@link #COLONIAL} is every
 * ordinary colonist, whose strength comes from veteran status and mounting. The
 * other roles carry the revolution chart bases directly and are never inferred
 * from veteran status: only an explicit operation grants them.
 */
public enum MilitaryRole {

    /** Ordinary colonial soldier; ordinary veteran and mount rules apply. */
    COLONIAL(0, 0),

    /** Revolution muster of a rebel veteran: infantry 4, cavalry 5. */
    CONTINENTAL(4, 5),

    /** Crown regular: infantry 5, cavalry 6. */
    ROYAL(5, 6);

    private final int infantryBase;

    private final int cavalryBase;

    MilitaryRole(final int infantryBase, final int cavalryBase) {
        this.infantryBase = infantryBase;
        this.cavalryBase = cavalryBase;
    }

    /**
     * @return whether this role has its own chart bases and is destroyed with
     *         its equipment when it loses a battle
     */
    public boolean isSpecial() {
        return this != COLONIAL;
    }

    /**
     * @param mounted whether the armed unit rides, i.e. cavalry
     * @return chart base strength of the special role
     * @throws IllegalStateException for the ordinary colonial role
     */
    public int getBase(final boolean mounted) {
        if (!isSpecial()) {
            throw new IllegalStateException(
                    "Ordinary colonists have no role base");
        }
        return mounted ? cavalryBase : infantryBase;
    }
}
