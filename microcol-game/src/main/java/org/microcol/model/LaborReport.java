package org.microcol.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.microcol.model.unit.UnitFreeColonist;

import com.google.common.base.Preconditions;

/**
 * Labor report of one colonial nation (R-0198): its living people grouped by
 * unit type, each counted once by stable unit ID. Crown-seized people, other
 * owners' people, ships, braves and treasure are not labor. The report is a
 * snapshot of the live model; nothing is kept between calls.
 */
public final class LaborReport {

    private final Map<UnitType, List<LaborMember>> membersByType;
    private final int total;

    private LaborReport(final Map<UnitType, List<LaborMember>> membersByType,
            final int total) {
        this.membersByType = membersByType;
        this.total = total;
    }

    /**
     * Build the report of the current state of one colonial nation. Native
     * settlements are named only as far as the owner has learned them.
     *
     * @param model required model that owns the owner
     * @param owner required colonial nation whose people are reported
     * @return immutable report of the current state
     */
    static LaborReport calculate(final Model model, final Player owner) {
        Preconditions.checkNotNull(owner, "Owner is required");
        Preconditions.checkArgument(owner.isColonialNation(),
                "Labor report belongs to a colonial nation");
        final List<NativeSettlement> knownSites = model
                .getNativeSettlementsKnownTo(owner);
        final Map<Integer, LaborMember> people = new LinkedHashMap<>();
        for (final Unit unit : owner.getAllUnits()) {
            if (unit instanceof UnitFreeColonist && !unit.isCrownSeized()) {
                people.putIfAbsent(unit.getId(),
                        LaborMember.of((UnitFreeColonist) unit, knownSites));
            }
        }
        final Map<UnitType, List<LaborMember>> byType = new TreeMap<>(
                Comparator.comparing(UnitType::name));
        for (final LaborMember member : people.values()) {
            byType.computeIfAbsent(member.getType(), type -> new ArrayList<>())
                    .add(member);
        }
        byType.replaceAll((type, members) -> members.stream()
                .sorted(Comparator.comparingInt(LaborMember::getUnitId))
                .collect(Collectors.toUnmodifiableList()));
        return new LaborReport(Collections.unmodifiableMap(byType),
                people.size());
    }

    /** @return types that have at least one person, ordered by name */
    public List<UnitType> getTypes() {
        return List.copyOf(membersByType.keySet());
    }

    /**
     * @param type required unit type
     * @return people of the type, ordered by unit ID; empty for a type without
     *         people
     */
    public List<LaborMember> getMembers(final UnitType type) {
        Preconditions.checkNotNull(type, "Type is required");
        return membersByType.getOrDefault(type, List.of());
    }

    /**
     * @param type required unit type
     * @return number of people of the type
     */
    public int getCount(final UnitType type) {
        return getMembers(type).size();
    }

    /** @return number of people in the whole report, each counted once */
    public int getTotal() {
        return total;
    }

    /**
     * Find a person of the current report. A person that died, was captured,
     * was seized or belongs to another nation is not found.
     *
     * @param unitId stable unit ID
     * @return the person, empty when it is not part of this report
     */
    public Optional<LaborMember> findMember(final int unitId) {
        return membersByType.values().stream().flatMap(List::stream)
                .filter(member -> member.getUnitId() == unitId).findFirst();
    }
}
