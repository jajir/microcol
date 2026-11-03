package org.akropia.gui;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.InputStream;
import java.util.Properties;

import org.junit.jupiter.api.Test;

class IdentityHelpTextTest {

    @Test
    void everyIdentityDescriptionSupportsLocalizedFormatting()
            throws Exception {
        for (final String locale : new String[] { "", "_cs_CZ" }) {
            final Properties messages = new Properties();
            try (InputStream input = getClass().getResourceAsStream(
                    "/org/akropia/gui/screen/colonizopedia/Colonizopedia"
                            + locale + ".xml")) {
                assertNotNull(input);
                messages.loadFromXML(input);
            }
            for (int index = 0; index < 12; index++) {
                final String text = messages.getProperty("identity" + index);
                assertNotNull(text);
                assertDoesNotThrow(() -> String.format(text));
            }
        }
    }
}
