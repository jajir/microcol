package org.microcol.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;

/**
 * Test that verify that test are running.
 */
@ExtendWith(ApplicationExtension.class)
public class EnvironmentTest {

    @Tag("ci")
    @Test
    void test_this_test_always_shoudl_be_in_reports() throws Exception {
	assertTrue(true);
    }

}
