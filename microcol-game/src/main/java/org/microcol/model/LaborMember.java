package org.microcol.model;

import java.util.List;
import java.util.Map;

import org.microcol.model.unit.UnitFreeColonist;

import com.google.common.base.Preconditions;

/**
 * One living person of the labor report (R-0198). The veteran flag, native
 * convert status and carried equipment describe the same person; none of them
 * is a second person in the counts.
 */
public final class LaborMember {

    private final int unitId;
    private final UnitType type;
    private final boolean veteran;
    private final boolean nativeConvert;
    private final Map<GoodsType, Integer> equipment;
    private final LaborLocation location;

    private LaborMember(final int unitId, final UnitType type,
            final boolean veteran, final boolean nativeConvert,
            final Map<GoodsType, Integer> equipment,
            final LaborLocation location) {
        this.unitId = unitId;
        this.type = Preconditions.checkNotNull(type);
        this.veteran = veteran;
        this.nativeConvert = nativeConvert;
        this.equipment = Map.copyOf(equipment);
        this.location = Preconditions.checkNotNull(location);
    }

    static LaborMember of(final UnitFreeColonist person,
            final List<NativeSettlement> knownSites) {
        Preconditions.checkNotNull(person, "Person is required");
        return new LaborMember(person.getId(), person.getType(),
                person.isVeteran(), person.isNativeConvert(),
                person.getCarriedEquipment(),
                LaborLocation.of(person, knownSites));
    }

    /** @return stable unit ID of this person */
    public int getUnitId() {
        return unitId;
    }

    /** @return unit type; the row of the report that counts this person */
    public UnitType getType() {
        return type;
    }

    /** @return whether this person has the veteran rank */
    public boolean isVeteran() {
        return veteran;
    }

    /** @return whether this person is a native convert */
    public boolean isNativeConvert() {
        return nativeConvert;
    }

    /** @return carried equipment, goods and quantity, never null */
    public Map<GoodsType, Integer> getEquipment() {
        return equipment;
    }

    /** @return current place of this person, never null */
    public LaborLocation getLocation() {
        return location;
    }
}
