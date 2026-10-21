package org.microcol.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.microcol.model.store.NativeContactPo;

import com.google.common.base.Preconditions;

/**
 * Land contact between colonial European nations and whole native tribes
 * (R-0159). A contact belongs to the tribe, not to one settlement, and to the
 * European nation, never to a player's display name. Introducing a nation again
 * reuses the same record.
 */
final class NativeContacts {

    private final Map<EuropeanNation, Set<NativeNation>> contacts;

    private NativeContacts(
            final Map<EuropeanNation, Set<NativeNation>> contacts) {
        this.contacts = contacts;
    }

    /**
     * Build the contacts from the saved list. An absent list means no contact
     * yet; every entry must name a European and a tribe, without duplicates.
     *
     * @param saved saved entries, or null when the save has none
     * @return validated contacts
     */
    static NativeContacts make(final List<NativeContactPo> saved) {
        final Map<EuropeanNation, Set<NativeNation>> out = new EnumMap<>(
                EuropeanNation.class);
        if (saved != null) {
            for (final NativeContactPo entry : saved) {
                Preconditions.checkArgument(
                        entry != null && entry.getEuropean() != null
                                && entry.getNation() != null,
                        "Unsupported save: incomplete native contact");
                Preconditions
                        .checkArgument(
                                out.computeIfAbsent(entry.getEuropean(),
                                        key -> EnumSet
                                                .noneOf(NativeNation.class))
                                        .add(entry.getNation()),
                                "Unsupported save: duplicate native contact");
            }
        }
        return new NativeContacts(out);
    }

    /**
     * @return saved form of every contact, in European and tribe order
     */
    List<NativeContactPo> save() {
        final List<NativeContactPo> out = new ArrayList<>();
        for (final EuropeanNation european : EuropeanNation.values()) {
            for (final NativeNation nation : NativeNation.values()) {
                if (isContacted(european, nation)) {
                    final NativeContactPo po = new NativeContactPo();
                    po.setEuropean(european);
                    po.setNation(nation);
                    out.add(po);
                }
            }
        }
        return out;
    }

    /**
     * @param european required colonial European nation
     * @param nation   required native tribe
     * @return whether a land introduction has made the tribe know the nation
     */
    boolean isContacted(final EuropeanNation european,
            final NativeNation nation) {
        return contacts.getOrDefault(european, Set.of()).contains(nation);
    }

    /**
     * Record the land introduction. Repeating it changes nothing.
     *
     * @param european required colonial European nation
     * @param nation   required native tribe
     */
    void introduce(final EuropeanNation european, final NativeNation nation) {
        contacts.computeIfAbsent(european,
                key -> EnumSet.noneOf(NativeNation.class)).add(nation);
    }

}
