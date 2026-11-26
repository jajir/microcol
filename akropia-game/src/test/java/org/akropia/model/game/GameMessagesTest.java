package org.akropia.model.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Locale;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.akropia.gui.util.StreamReader;
import org.akropia.i18n.I18n;

public class GameMessagesTest {

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
                i18n.get(GameMessages.gameOver_allColoniesAreLost));
        assertEquals(
                czech ? "Čas vyhrazený pro tuto hru vypršel."
                        : "The time allowed for this game has expired.",
                i18n.get(GameMessages.gameOver_timeIsUp));
        assertEquals(czech
                ? "Vyhlásil jsi samostatnou polis. Připrav se na útok vojska mateřského města."
                : "You declared an independent polis. Prepare for the army of the mother city.",
                i18n.get(GameMessages.independenceWasDeclared_caption));
    }

}
