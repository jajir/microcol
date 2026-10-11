package org.microcol.build;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GameReportSupportTest {
    @TempDir
    Path temporary;

    private void report(String name, long tests, long failures, long errors,
            long skipped) throws Exception {
        Files.writeString(temporary.resolve("TEST-" + name + ".xml"),
                "<testsuite tests=\"" + tests + "\" failures=\"" + failures
                        + "\" errors=\"" + errors + "\" skipped=\"" + skipped
                        + "\"/>");
    }

    @Test
    void missingDirectoryOrMissingXmlCannotClaimVerification()
            throws Exception {
        assertThrows(IOException.class,
                () -> GameReportSupport.verify(temporary.resolve("missing")));
        Files.writeString(temporary.resolve("test.log"), "BUILD SUCCESS");
        assertThrows(IOException.class,
                () -> GameReportSupport.verify(temporary));
    }

    @Test
    void zeroOrEntirelySkippedTestsCannotClaimVerification() throws Exception {
        report("empty", 0, 0, 0, 0);
        assertThrows(IOException.class,
                () -> GameReportSupport.verify(temporary));
        report("empty", 3, 0, 0, 3);
        assertThrows(IOException.class,
                () -> GameReportSupport.verify(temporary));
    }

    @Test
    void oneFailedOrErroredSuiteRejectsOtherPassingSuites() throws Exception {
        report("passed", 100, 0, 0, 0);
        report("broken", 1, 1, 0, 0);
        assertThrows(IOException.class,
                () -> GameReportSupport.verify(temporary));
        report("broken", 1, 0, 1, 0);
        assertThrows(IOException.class,
                () -> GameReportSupport.verify(temporary));
    }

    @Test
    void malformedCountsAreNotSilentlyInterpretedAsSuccess() throws Exception {
        report("bad", 1, 0, 0, 2);
        assertThrows(IOException.class,
                () -> GameReportSupport.verify(temporary));
        report("bad", -1, 0, 0, 0);
        assertThrows(IOException.class,
                () -> GameReportSupport.verify(temporary));
        Files.writeString(temporary.resolve("TEST-bad.xml"),
                "<testsuite tests=\"1\"/>");
        assertThrows(IOException.class,
                () -> GameReportSupport.verify(temporary));
    }

    @Test
    void allSuitesContributeToTheRecordedCounts() throws Exception {
        report("first", 8, 0, 0, 2);
        report("second", 4, 0, 0, 1);
        String evidence = GameReportSupport.verify(temporary);
        assertTrue(evidence.contains("TEST-first.xml"));
        assertTrue(evidence.contains("TEST-second.xml"));
        assertTrue(
                evidence.contains(
                        "Totals: tests=12, skipped=3, failures=0, errors=0"),
                evidence);
    }
}
