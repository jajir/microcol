package org.akropia.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class GreekIdentityTest {
    @Test
    void cityNamesRetainTheirInternalRuleProfiles() {
        assertEquals("Corinth",
                EuropeanNation.fromPlayerName("Dutch").getDisplayName());
        assertEquals("Athens",
                EuropeanNation.fromPlayerName("England").getDisplayName());
        assertEquals("Phocaea",
                EuropeanNation.fromPlayerName("France").getDisplayName());
        assertEquals("Miletus",
                EuropeanNation.fromPlayerName("Spain").getDisplayName());
        assertThrows(IllegalArgumentException.class,
                () -> EuropeanNation.fromPlayerName(""));
    }

    @Test
    void localPeoplesKeepTheAgreedSettlementClasses() {
        assertEquals("Egyptians", NativeNation.AZTEC.getDisplayName());
        assertEquals("Etruscans", NativeNation.INCA.getDisplayName());
        assertEquals("Phoenicians", NativeNation.ARAWAK.getDisplayName());
        assertEquals("Thracians", NativeNation.CHEROKEE.getDisplayName());
        assertEquals("Illyrians", NativeNation.IROQUOIS.getDisplayName());
        assertEquals("Libyans", NativeNation.TUPI.getDisplayName());
        assertEquals("Scythians", NativeNation.SIOUX.getDisplayName());
        assertEquals("Arabs", NativeNation.APACHE.getDisplayName());
        for (final NativeNation nation : NativeNation.values()) {
            final NativeSettlementClass expected = nation == NativeNation.AZTEC
                    || nation == NativeNation.INCA
                            ? NativeSettlementClass.CITY
                            : nation == NativeNation.ARAWAK
                                    || nation == NativeNation.CHEROKEE
                                    || nation == NativeNation.IROQUOIS
                                            ? NativeSettlementClass.VILLAGE
                                            : NativeSettlementClass.CAMP;
            assertEquals(expected, nation.getSettlementClass());
        }
    }
}
