package org.akropia.model.campaign;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Locale;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.akropia.gui.util.StreamReader;
import org.akropia.i18n.I18n;

public class MissionsTest {

    @ParameterizedTest
    @ValueSource(strings = { "en", "cs-CZ" })
    void outcomeMessagesKeepTheirTranslations(final String language) {
        final I18n i18n = I18n.builder().setStreamReader(new StreamReader())
                .setVerifyThatAllEnumKeysAreDefined(true)
                .setVerifyThatAllKeysInResourceBundleHaveConstant(true)
                .setDefaultLocale(Locale.forLanguageTag(language)).build();
        final boolean czech = language.equals("cs-CZ");
        assertEquals(
                czech ? "Ztratil jsi všechny kolonie."
                        : "You lost all colonies.",
                i18n.get(Missions.gameOver_allColoniesAreLost));
        assertEquals(
                czech ? "Čas vyhrazený pro tuto misi vypršel."
                        : "The time allowed for this mission has expired.",
                i18n.get(Missions.gameOver_timeIsUp));
        assertEquals(czech
                ? "Vyhlásil jsi samostatnou polis. Připrav se na útok vojska mateřského města."
                : "You declared an independent polis. Prepare for the army of the mother city.",
                i18n.get(Missions.independenceWasDeclared_caption));
    }

    @ParameterizedTest
    @ValueSource(strings = { "en", "cs-CZ" })
    void independenceMessagesCanBeLoaded(final String language) {
        final I18n i18n = I18n.builder().setStreamReader(new StreamReader())
                .setDefaultLocale(Locale.forLanguageTag(language)).build();

        assertFalse(i18n.get(Missions.default_m2_declareIndependence_done)
                .isBlank());
        assertFalse(i18n.get(Missions.default_m2_portIsClosed).isBlank());
    }
}
