package org.microcol.model.campaign;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Locale;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.microcol.gui.util.StreamReader;
import org.microcol.i18n.I18n;

public class MissionsTest {

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
