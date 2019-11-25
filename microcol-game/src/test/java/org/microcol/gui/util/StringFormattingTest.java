package org.microcol.gui.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.microcol.gui.screen.turnreport.TurnEvents;
import org.microcol.i18n.I18n;

public class StringFormattingTest {

    /**
     * Verify that basic localization is working.
     * 
     * @throws Exception generic test exception
     */
    @Test
    public void test_simple() throws Exception {

        final I18n i18n = I18n.builder().setDefaultLocale(new Locale("CS", "cz"))
                .setVerifyThatAllEnumKeysAreDefined(true)
                .setVerifyThatAllKeysInResourceBundleHaveConstant(true).build();

        final String str = i18n.get(TurnEvents.goodsWasThrowsAway1, "Kladno", 23, "Doutniku");

        assertEquals("V kolonii Kladno došlo místo, proto se muselo 23 kusů Doutniku vyhodit.",
                str);

    }

}
