package org.microcol.gui.util;

import java.util.Properties;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for ApplicationInfo version handling.
 */
class ApplicationInfoTest {

    private Properties createProps(final String version, final String buildId) {
        final Properties props = new Properties();
        props.setProperty("version", version);
        if (buildId != null) {
            props.setProperty("build.id", buildId);
        }
        return props;
    }

    @Test
    void testMissingBuildId() {
        final Properties props = createProps("0.7.0", null);
        assertEquals("0.7.0", ApplicationInfo.formatBuildVersion(props));
    }

    @Test
    void testBlankBuildId() {
        final Properties props = createProps("0.7.0", "   ");
        assertEquals("0.7.0", ApplicationInfo.formatBuildVersion(props));
    }

    @Test
    void testLocalBuildId() {
        final Properties props = createProps("0.7.0", "local");
        assertEquals("0.7.0", ApplicationInfo.formatBuildVersion(props));
    }

    @Test
    void testUnresolvedPlaceholder() {
        final Properties props = createProps("0.7.0", "${microcol.build.id}");
        assertEquals("0.7.0", ApplicationInfo.formatBuildVersion(props));
    }

    @Test
    void testRealCIBuildId() {
        final Properties props = createProps("0.7.0", "dev-42-0123456789ab");
        assertEquals("0.7.0 (dev-42-0123456789ab)", ApplicationInfo.formatBuildVersion(props));
    }

}
