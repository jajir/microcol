package org.microcol.model;

import java.util.List;
import java.util.Optional;

import com.google.common.base.Preconditions;

/**
 * Current place of one living person in the labor report (R-0198). A person
 * aboard a carrier takes the carrier's place and names the carrier by its
 * stable ID and type. A native course or mission names its settlement only when
 * the owner has learned that settlement; otherwise nothing about it is shown.
 * Only a colony, a map tile, Europe and a known settlement tile are
 * destinations.
 */
public final class LaborLocation {

    /** Kinds of place, each with its own destination rule. */
    public enum Kind {
        /** A colony field or building; the destination is that colony. */
        COLONY,
        /** A map tile; the destination is that tile. */
        MAP,
        /** The European port or pier; the destination is Europe. */
        EUROPE,
        /** A voyage on the high seas; no accessible destination. */
        AT_SEA,
        /** A course at a native settlement; the destination is a known tile. */
        NATIVE_COURSE,
        /**
         * A mission at a native settlement; the destination is a known tile.
         */
        NATIVE_MISSION,
        /** A ship under repair; no accessible destination. */
        REPAIR,
        /** Any other place; no accessible destination. */
        OTHER
    }

    private final Kind kind;
    private final Colony colony;
    private final Location location;
    private final GoodsType fieldGoods;
    private final ConstructionType construction;
    private final NativeSettlement nativeSettlement;
    private final Integer carrierId;
    private final UnitType carrierType;

    private LaborLocation(final Kind kind, final Colony colony,
            final Location location, final GoodsType fieldGoods,
            final ConstructionType construction,
            final NativeSettlement nativeSettlement, final Integer carrierId,
            final UnitType carrierType) {
        this.kind = Preconditions.checkNotNull(kind);
        this.colony = colony;
        this.location = location;
        this.fieldGoods = fieldGoods;
        this.construction = construction;
        this.nativeSettlement = nativeSettlement;
        this.carrierId = carrierId;
        this.carrierType = carrierType;
    }

    /**
     * Describe where a living person stands now. A person in a cargo slot is
     * described by the place of its carrier, which the description names.
     *
     * @param person     required living person of the report
     * @param knownSites required native settlements the owner has learned
     * @return current place, never null
     */
    static LaborLocation of(final Unit person,
            final List<NativeSettlement> knownSites) {
        Preconditions.checkNotNull(person, "Person is required");
        Preconditions.checkNotNull(knownSites,
                "Known settlements are required");
        if (person.isAtCargoSlot()) {
            final Unit carrier = person.getPlaceCargoSlot().getOwnerUnit();
            return placedAt(carrier, knownSites).aboard(carrier);
        }
        return placedAt(person, knownSites);
    }

    private static LaborLocation placedAt(final Unit unit,
            final List<NativeSettlement> knownSites) {
        if (unit.isAtPlaceColonyField()) {
            final PlaceColonyField field = unit.getPlaceColonyField();
            return new LaborLocation(Kind.COLONY, field.getColony(), null,
                    field.getProducedGoodsType(), null, null, null, null);
        }
        if (unit.isAtPlaceConstructionSlot()) {
            final PlaceConstructionSlot slot = unit.getPlaceConstruction();
            return new LaborLocation(Kind.COLONY, slot.getColony(), null, null,
                    slot.getConstructionSlot().getConstructionType(), null,
                    null, null);
        }
        if (unit.isAtPlaceLocation()) {
            return new LaborLocation(Kind.MAP, null, unit.getLocation(), null,
                    null, null, null, null);
        }
        if (unit.isAtEuropePort() || unit.isAtEuropePier()) {
            return new LaborLocation(Kind.EUROPE, null, null, null, null, null,
                    null, null);
        }
        if (unit.isAtHighSea()) {
            return new LaborLocation(Kind.AT_SEA, null, null, null, null, null,
                    null, null);
        }
        if (unit.isAtPlaceNativeCourse()) {
            return nativeSite(Kind.NATIVE_COURSE,
                    ((PlaceNativeCourse) unit.getPlace()).getSettlementId(),
                    knownSites);
        }
        if (unit.isAtPlaceNativeMission()) {
            return nativeSite(Kind.NATIVE_MISSION,
                    ((PlaceNativeMission) unit.getPlace()).getSettlementId(),
                    knownSites);
        }
        if (unit.getPlace() instanceof PlaceShipRepair) {
            return new LaborLocation(Kind.REPAIR, null, null, null, null, null,
                    null, null);
        }
        return new LaborLocation(Kind.OTHER, null, null, null, null, null, null,
                null);
    }

    /**
     * A native place is named only through a settlement the owner has learned.
     * An unlearned settlement gives no identity, location or destination.
     */
    private static LaborLocation nativeSite(final Kind kind,
            final int settlementId, final List<NativeSettlement> knownSites) {
        final NativeSettlement site = knownSites.stream()
                .filter(known -> known.getId() == settlementId).findFirst()
                .orElse(null);
        return new LaborLocation(kind, null,
                site == null ? null : site.getLocation(), null, null, site,
                null, null);
    }

    /** @return the same place, with the carrier the person is aboard */
    private LaborLocation aboard(final Unit carrier) {
        return new LaborLocation(kind, colony, location, fieldGoods,
                construction, nativeSettlement, carrier.getId(),
                carrier.getType());
    }

    /** @return kind of place, never null */
    public Kind getKind() {
        return kind;
    }

    /**
     * @return whether a destination of this place is accessible to the player
     */
    public boolean isNavigable() {
        return kind == Kind.COLONY || kind == Kind.EUROPE
                || location != null
                        && (kind == Kind.MAP || kind == Kind.NATIVE_COURSE
                                || kind == Kind.NATIVE_MISSION);
    }

    /** @return the colony of a colony place */
    public Optional<Colony> getColony() {
        return Optional.ofNullable(colony);
    }

    /**
     * @return the tile of a map place, or the tile of a learned native
     *         settlement
     */
    public Optional<Location> getLocation() {
        return Optional.ofNullable(location);
    }

    /** @return the goods a colony field produces, when the person works one */
    public Optional<GoodsType> getFieldGoods() {
        return Optional.ofNullable(fieldGoods);
    }

    /** @return the building a colony place works in, when there is one */
    public Optional<ConstructionType> getConstruction() {
        return Optional.ofNullable(construction);
    }

    /**
     * @return the native settlement of a course or mission, only when the owner
     *         has learned its identity
     */
    public Optional<NativeSettlement> getNativeSettlement() {
        return Optional.ofNullable(nativeSettlement);
    }

    /** @return stable ID of the carrier when the person is aboard a ship */
    public Optional<Integer> getCarrierId() {
        return Optional.ofNullable(carrierId);
    }

    /** @return the carrier type when the person is aboard a ship */
    public Optional<UnitType> getCarrierType() {
        return Optional.ofNullable(carrierType);
    }
}
