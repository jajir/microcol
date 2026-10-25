package org.microcol.gui.screen.colonizopedia;

import java.text.Normalizer;
import java.util.Arrays;
import org.microcol.gui.LocalizationHelper;
import org.microcol.model.TerrainType;
import org.microcol.model.UnitType;
import org.microcol.model.ResourceOverlay;
import java.util.Locale;

import org.microcol.i18n.I18n;

/**
 * Rule topics of the Colonizopedia. The texts describe supported rules only;
 * the owning model and the rule documents decide the behavior.
 */
public enum ColonizopediaTopic {

    MOVEMENT(Colonizopedia.topic_movement_title,
            Colonizopedia.topic_movement_body),
    CARGO(Colonizopedia.topic_cargo_title, Colonizopedia.topic_cargo_body),
    COLONIES(Colonizopedia.topic_colonies_title,
            Colonizopedia.topic_colonies_body),
    PRODUCTION(Colonizopedia.topic_production_title,
            Colonizopedia.topic_production_body),
    TRADE(Colonizopedia.topic_trade_title, Colonizopedia.topic_trade_body),
    DIPLOMACY(Colonizopedia.topic_diplomacy_title,
            Colonizopedia.topic_diplomacy_body),
    COMBAT(Colonizopedia.topic_combat_title, Colonizopedia.topic_combat_body),
    INDEPENDENCE(Colonizopedia.topic_independence_title,
            Colonizopedia.topic_independence_body),
    SCORING(Colonizopedia.topic_scoring_title,
            Colonizopedia.topic_scoring_body),
    TERRAIN(Colonizopedia.topic_terrain_title,
            Colonizopedia.topic_terrain_body),
    RESOURCES(Colonizopedia.topic_resources_title,
            Colonizopedia.topic_resources_body),
    UNITS(Colonizopedia.topic_units_title, Colonizopedia.topic_units_body);

    private final Colonizopedia title;

    private final Colonizopedia body;

    ColonizopediaTopic(final Colonizopedia title, final Colonizopedia body) {
        this.title = title;
        this.body = body;
    }

    public String getTitle(final I18n i18n) {
        return i18n.get(title);
    }

    public String getBody(final I18n i18n) {
        return i18n.get(body);
    }

    /**
     * Search the localized title and body. Letter case and accents are ignored;
     * a blank query matches every topic.
     *
     * @param i18n  localization used for the current language
     * @param query text typed by the player
     * @return true when the topic should stay in the list
     */
    public boolean matches(final I18n i18n, final String query) {
        final String needle = fold(query);
        return needle.isEmpty() || fold(getTitle(i18n)).contains(needle)
                || fold(getBody(i18n)).contains(needle)
                || referenceMatches(i18n, needle);
    }

    private boolean referenceMatches(final I18n i18n, final String needle) {
        final LocalizationHelper names = new LocalizationHelper(i18n);
        if (this == UNITS) {
            return UnitType.UNIT_TYPES.stream().anyMatch(
                    type -> fold(names.getUnitName(type)).contains(needle));
        }
        if (this == TERRAIN) {
            return TerrainType.TERRAINS.stream()
                    .anyMatch(terrain -> fold(names.getTerrainName(terrain))
                            .contains(needle));
        }
        return this == RESOURCES && Arrays.stream(ResourceOverlay.values())
                .filter(resource -> resource != ResourceOverlay.NONE).anyMatch(
                        resource -> fold(names.getResourceOverlayName(resource))
                                .contains(needle));
    }

    private static String fold(final String text) {
        return Normalizer.normalize(text.strip(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }

}
