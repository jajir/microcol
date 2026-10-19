package org.microcol;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.microcol.diagnostics.DiagnosticLog;

class MicroColApplicationStartupFailureTest {

    private static final String SECRET = "Colony 'Alpha' /Users/player/save.microcol {\"json\":1}";

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
    void startupFailureIsRecordedWithoutMessageOrRawStackTrace()
            throws Exception {
        final DiagnosticLog log = new DiagnosticLog(directory.resolve("logs"),
                "test-build");

        MicroColApplication
                .recordStartupFailure(new IllegalStateException(SECRET), log);

        final String record = Files.readString(log.getActiveFile());
        assertTrue(record.contains("event=STARTUP_FAILED"));
        assertTrue(record.contains("cause=java.lang.IllegalStateException"));
        assertFalse(record.contains("Alpha"));
        final String output = console.toString(UTF_8);
        assertFalse(output.contains("Alpha"), "Message reached the console");
        assertFalse(output.contains("\tat "), "Raw stack trace was printed");
        assertTrue(output.contains(log.getActiveFile().toString()),
                "The log location is shown to the player");
    }

    @Test
    void unavailableLogFolderStillReportsStartupFailureOnTheConsole()
            throws Exception {
        final Path blocked = Files.writeString(directory.resolve("blocked"),
                "not a directory");
        final DiagnosticLog log = new DiagnosticLog(blocked.resolve("logs"),
                "test-build");

        assertDoesNotThrow(() -> MicroColApplication
                .recordStartupFailure(new IllegalStateException(SECRET), log));

        final String output = console.toString(UTF_8);
        assertTrue(output.contains("event=STARTUP_FAILED"));
        assertTrue(output.contains("diagnostic log is unavailable"));
        assertFalse(output.contains("Alpha"));
        assertEquals("not a directory", Files.readString(blocked));
    }

    @Test
    void failureToPrepareTheLogFallsBackToTheExceptionClassOnly() {
        final String osName = System.getProperty("os.name");
        final String home = System.getProperty("user.home");
        try {
            System.setProperty("os.name", "Mac OS X");
            // A NUL is invalid on both Unix and Windows path providers.
            System.setProperty("user.home", "\u0000");

            assertDoesNotThrow(() -> MicroColApplication
                    .recordStartupFailure(new IllegalStateException(SECRET)));
        } finally {
            System.setProperty("os.name", osName);
            System.setProperty("user.home", home);
        }

        final String output = console.toString(UTF_8);
        assertTrue(output.contains("java.lang.IllegalStateException"));
        assertTrue(output.contains("diagnostic log is unavailable"));
        assertFalse(output.contains("Alpha"));
    }

}
