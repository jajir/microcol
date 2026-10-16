package org.microcol.model;

/** Immutable military promotion result captured when combat resolves. */
public final class MilitaryPromotion {
    private final int unitId;
    private final UnitType fromType;
    private final UnitType toType;
    private final boolean wasVeteran;
    private final boolean veteran;

    public MilitaryPromotion(final int unitId, final UnitType fromType,
            final UnitType toType, final boolean wasVeteran,
            final boolean veteran) {
        this.unitId = unitId;
        this.fromType = fromType;
        this.toType = toType;
        this.wasVeteran = wasVeteran;
        this.veteran = veteran;
    }

    public int getUnitId() {
        return unitId;
    }

    public UnitType getFromType() {
        return fromType;
    }

    public UnitType getToType() {
        return toType;
    }

    public boolean wasVeteran() {
        return wasVeteran;
    }

    public boolean isVeteran() {
        return veteran;
    }
}
