package org.microcol.diagnostics;

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
        for (String name : List.of("microcol.log", "microcol.1.log",
                "microcol.2.log")) {
            assertTrue(Files.size(directory.resolve(name)) <= limit, name);
        }
    }

    @Test
    void recordHasBuildPlatformAndFramesButNoMessagesOrPlayerText()
            throws Exception {
        final DiagnosticLog log = new DiagnosticLog(directory, "test-build");
        final String home = System.getProperty("user.home");

        log.record(DiagnosticEvent.LOAD_FAILED, new IllegalArgumentException(
                "Colony 'New Amsterdam' in " + home + "/game.microcol"));

        final String record = Files.readString(log.getActiveFile());
        assertTrue(record.contains("event=LOAD_FAILED"));
        assertTrue(record.contains("build=test-build"));
        assertTrue(record.contains("os=\""));
        assertTrue(record.contains(" arch="));
        assertTrue(record.contains(" java="));
        assertTrue(record.contains("cause=java.lang.IllegalArgumentException"));
        assertTrue(record.contains(
                "\n    at org.microcol.diagnostics.DiagnosticLogTest."));
        assertFalse(record.contains("New Amsterdam"));
        assertFalse(record.contains(home));
        assertFalse(record.contains("game.microcol"));
    }

    @Test
    void stackIsBoundedToEightMicroColFramesAndSkipsOtherPackages()
            throws Exception {
        final IllegalStateException failure = new IllegalStateException();
        final StackTraceElement[] frames = Stream
                .concat(Stream.of(new StackTraceElement("java.lang.Thread",
                        "run", "Thread.java", 1)),
                        Stream.generate(() -> new StackTraceElement(
                                "org.microcol.Example", "run", "Example.java",
                                1)).limit(20))
                .toArray(StackTraceElement[]::new);
        failure.setStackTrace(frames);

        new DiagnosticLog(directory, "test-build")
                .record(DiagnosticEvent.STARTUP_FAILED, failure);

        final String record = Files
                .readString(directory.resolve(DiagnosticLog.FILE_NAME));
        assertEquals(8, record.lines()
                .filter(line -> line.startsWith("    at ")).count());
        assertFalse(record.contains("java.lang.Thread"));
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
    void hostileInputStaysAsciiSingleRecordAndWithinTheRecordBound()
            throws Exception {
        class Výjimka extends RuntimeException {
            private static final long serialVersionUID = 1L;
        }
        final Výjimka hostile = new Výjimka();
        final RuntimeException other = new RuntimeException();
        hostile.initCause(other);
        other.initCause(hostile);
        final StackTraceElement[] frames = Stream
                .generate(() -> new StackTraceElement(
                        "org.microcol.Přehled" + "x".repeat(500),
                        "metoda\nšest" + "y".repeat(500), "File.java", 7))
                .limit(30).toArray(StackTraceElement[]::new);
        hostile.setStackTrace(frames);
        final DiagnosticLog log = new DiagnosticLog(directory,
                "příliš\nlong" + "z".repeat(500));

        log.record(DiagnosticEvent.LOAD_FAILED, hostile);

        final byte[] bytes = Files.readAllBytes(log.getActiveFile());
        assertTrue(bytes.length <= DiagnosticLog.MAX_RECORD_CHARS,
                "Record is " + bytes.length + " bytes");
        for (final byte value : bytes) {
            assertTrue(value >= 0, "Record contains a non-ASCII byte");
        }
        final String record = new String(bytes,
                java.nio.charset.StandardCharsets.US_ASCII);
        assertEquals(9, record.lines().count());
        assertEquals(1,
                record.lines().filter(line -> line.contains("event=")).count());
        assertTrue(record.split(" <- ", -1).length <= 4);
        assertFalse(record.contains("příliš"));
        assertTrue(record.endsWith("\n"));
    }

    @Test
    void unexpectedInputNeverEscapesTheRecordCall() throws Exception {
        final RuntimeException broken = new RuntimeException() {
            private static final long serialVersionUID = 1L;

            @Override
            public StackTraceElement[] getStackTrace() {
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
                    .resolve("MicroCol"), location);
            assertFalse(location.startsWith(directory.resolve(".microcol")));
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
                            .resolve("microcol").resolve("logs"),
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
