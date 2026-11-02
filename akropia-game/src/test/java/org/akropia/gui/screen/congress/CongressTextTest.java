package org.akropia.gui.screen.congress;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.akropia.gui.util.StreamReader;
import org.akropia.i18n.I18n;
import org.akropia.model.Founder;

class CongressTextTest {

    @Test
    void everyOfferedFounderHasLocalizedNameAndEffect() {
        for (Locale locale : new Locale[] { Locale.ENGLISH,
                new Locale("cs", "CZ") }) {
            final I18n i18n = I18n.builder().setStreamReader(new StreamReader())
                    .setDefaultLocale(locale)
                    .setVerifyThatAllEnumKeysAreDefined(true)
                    .setVerifyThatAllKeysInResourceBundleHaveConstant(true)
                    .build();
            for (Founder founder : Founder.values()) {
                assertFalse(CongressText.name(i18n, founder).isBlank());
                final String effect = CongressText.effect(i18n, founder);
                assertFalse(effect.isBlank(), founder.name());
                assertFalse(effect.contains("%%"), founder.name());
            }
            assertTrue(CongressText.effect(i18n, Founder.FRANCIS_DRAKE)
                    .contains("50"));
        }
    }
}
