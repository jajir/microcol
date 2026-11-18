package org.akropia.diagnostics;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DiagnosticLogTest {

    @TempDir
    Path directory;

    private final PrintStream originalErr = System.err;

    private final ByteArrayOutputStream console = new ByteArrayOutputStream();

    @BeforeEach
    void captureConsole() {
        System.setErr(new PrintStream(console, true, UTF_8));
    }

    @AfterEach
    void restoreConsole() {
        System.setErr(originalErr);
    }

    @Test
    void defaultLimitsAreFiveMegabytesAndThreeFilesIncludingActive() {
        assertEquals(5L * 1024 * 1024, DiagnosticLog.MAX_FILE_BYTES);
        assertEquals(3, DiagnosticLog.MAX_FILES);
    }

    @Test
    void rotationKeepsOnlyActiveFileAndTwoArchivesWithinTheLimit()
            throws Exception {
        final long limit = 8192;
        final DiagnosticLog log = new DiagnosticLog(directory, "test-build",
                limit, 3);

        for (int index = 0; index < 200; index++) {
            log.record(DiagnosticEvent.SAVE_FAILED, failure());
        }

        try (Stream<Path> files = Files.list(directory)) {
            assertEquals(3, files.count());
        }
        for (String name : List.of("akropia.log", "akropia.1.log",
                "akropia.2.log")) {
            assertTrue(Files.size(directory.resolve(name)) <= limit, name);
        }
    }

    @Test
    void recordPreservesStandardTraceIncludingMessagesCausesAndSuppressed()
            throws Exception {
        final DiagnosticLog log = new DiagnosticLog(directory, "test-build");
        final Exception root = new IllegalArgumentException("Poškozený save");
        root.setStackTrace(Stream
                .generate(() -> new StackTraceElement("external.Parser", "read",
                        "Parser.java", 42))
                .limit(30).toArray(StackTraceElement[]::new));
        final Exception failure = new IllegalStateException("Load failed",
                root);
        failure.addSuppressed(new java.io.IOException("Close failed"));
        final java.io.StringWriter expected = new java.io.StringWriter();
        failure.printStackTrace(new java.io.PrintWriter(expected));
        log.record(DiagnosticEvent.LOAD_FAILED, failure);
        final String record = Files.readString(log.getActiveFile());
        assertTrue(record.contains("event=LOAD_FAILED"));
        assertTrue(record.contains("build=test-build"));
        assertTrue(record.endsWith(expected.toString()));
        assertEquals(record, console.toString(UTF_8));
    }

    @Test
    void oversizedUnicodeTraceIsNotTruncatedAndNextRecordRotates()
            throws Exception {
        final DiagnosticLog log = new DiagnosticLog(directory, "test", 100, 3);
        final RuntimeException failure = new RuntimeException("ž".repeat(5000));
        failure.initCause(failureWithCycle());
        log.record(DiagnosticEvent.LOAD_FAILED, failure);
        final String record = Files.readString(log.getActiveFile());
        assertTrue(record.contains("ž".repeat(5000)));
        assertTrue(record.contains("CIRCULAR REFERENCE"));
        log.record(DiagnosticEvent.STARTED, null);
        assertEquals(record,
                Files.readString(directory.resolve("akropia.1.log")));
        assertTrue(Files.readString(log.getActiveFile())
                .contains("event=STARTED"));
    }

    private static Throwable failureWithCycle() {
        final RuntimeException first = new RuntimeException("first");
        final RuntimeException second = new RuntimeException("second", first);
        first.initCause(second);
        return first;
    }

    @Test
    void unavailableDirectoryFallsBackToConsoleWithoutThrowing()
            throws Exception {
        final Path blocked = Files.writeString(directory.resolve("blocked"),
                "not a directory");
        final DiagnosticLog log = new DiagnosticLog(blocked.resolve("logs"),
                "test-build");

        assertDoesNotThrow(() -> log.record(DiagnosticEvent.STARTED, null));

        final String output = console.toString(UTF_8);
        assertTrue(output.contains("event=STARTED"));
        assertTrue(output.contains("diagnostic log is unavailable"));
        assertFalse(Files.exists(log.getActiveFile()));
        assertEquals("not a directory", Files.readString(blocked));
    }

    @Test
    void unexpectedInputNeverEscapesTheRecordCall() throws Exception {
        final RuntimeException broken = new RuntimeException() {
            private static final long serialVersionUID = 1L;

            @Override
            public void printStackTrace(final java.io.PrintWriter writer) {
                throw new IllegalStateException("must not escape");
            }
        };
        final DiagnosticLog log = new DiagnosticLog(directory, "test-build");

        assertDoesNotThrow(
                () -> log.record(DiagnosticEvent.SAVE_FAILED, broken));
        assertDoesNotThrow(() -> log.record(null, null));

        assertTrue(console.toString(UTF_8).contains("could not be formed"));
        assertTrue(Files.readString(log.getActiveFile())
                .contains("event=UNKNOWN"));
    }

    @Test
    void macOsUsesUserLibraryLogsOutsideTheSaveDirectory() {
        final String osName = System.getProperty("os.name");
        final String home = System.getProperty("user.home");
        try {
            System.setProperty("os.name", "Mac OS X");
            System.setProperty("user.home", directory.toString());

            final Path location = DiagnosticLog.defaultDirectory();

            assertEquals(directory.resolve("Library").resolve("Logs")
                    .resolve("Akropia"), location);
            assertFalse(location.startsWith(directory.resolve(".akropia")));
        } finally {
            System.setProperty("os.name", osName);
            System.setProperty("user.home", home);
        }
    }

    @Test
    void linuxFollowsStateDirectoryConventionWhenNotOverridden() {
        Assumptions.assumeTrue(System.getenv("XDG_STATE_HOME") == null);
        final String osName = System.getProperty("os.name");
        final String home = System.getProperty("user.home");
        try {
            System.setProperty("os.name", "Linux");
            System.setProperty("user.home", directory.toString());

            assertEquals(
                    directory.resolve(".local").resolve("state")
                            .resolve("akropia").resolve("logs"),
                    DiagnosticLog.defaultDirectory());
        } finally {
            System.setProperty("os.name", osName);
            System.setProperty("user.home", home);
        }
    }

    private static Exception failure() {
        return new IllegalStateException("message that must not be stored");
    }

}
