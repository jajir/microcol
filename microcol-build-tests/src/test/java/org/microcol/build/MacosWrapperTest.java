package org.microcol.build;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Checks the Finder wrapper without starting the game or a native dialog. A
 * stub launcher replaces microcol-dev and a stub replaces osascript.
 */
class MacosWrapperTest {
    private static final Path WRAPPER = BuildTestSupport.ROOT
            .resolve("scripts/launcher/macos-wrapper.sh");
    private static final long LIMIT = 5L * 1024 * 1024;
    private static final String SECRET = "SECRET Colony Alpha {\"json\":1} /Users/player/save";

    @TempDir
    Path temporary;
    private Path launcher;
    private Path wrapper;
    private Path home;
    private Path root;
    private Path logs;
    private Path dialogs;

    @BeforeEach
    void prepare() throws Exception {
        Path macos = Files.createDirectories(
                temporary.resolve("MicroCol Dev.app/Contents/MacOS"));
        Path resources = Files.createDirectories(
                temporary.resolve("MicroCol Dev.app/Contents/Resources"));
        wrapper = Files.copy(WRAPPER, macos.resolve("macos-wrapper.sh"));
        launcher = resources.resolve("microcol-dev");
        home = Files.createDirectory(temporary.resolve("home"));
        root = temporary.resolve("MicroCol Dev root");
        logs = home.resolve("Library/Logs/MicroCol");
        dialogs = temporary.resolve("dialogs");
    }

    @Test
    void successDiscardsHighVolumeConsoleOutputAndWritesNothing()
            throws Exception {
        stub("for i in $(seq 1 60000); do printf '%0100d %s\\n' \"$i\" '"
                + SECRET + "'; done\nexit 0");
        Files.createDirectories(root);
        Files.writeString(root.resolve("launcher.log"), SECRET);

        Result result = run();

        assertEquals(0, result.status, result.output);
        assertFalse(Files.exists(logs.resolve("microcol.log")));
        assertFalse(Files.exists(root.resolve("launcher.log")),
                "The old raw console copy must be removed");
        assertFalse(Files.exists(dialogs));
        assertEquals("", result.output, "Console output must be discarded");
    }

    @Test
    void failureKeepsExitSemanticsAndRecordsOnlyFixedFields() throws Exception {
        stub("for i in $(seq 1 60000); do printf '%0100d %s\\n' \"$i\" '"
                + SECRET + "'; done\nexit 7");

        Result result = run();

        assertEquals(1, result.status, result.output);
        assertTrue(Files.exists(dialogs), "The failure dialog was not shown");
        String record = Files.readString(logs.resolve("microcol.log"));
        assertEquals(1, record.lines().count());
        assertTrue(
                record.matches("(?s)\\d{4}-\\d\\d-\\d\\dT\\d\\d:\\d\\d:\\d\\dZ"
                        + " os=macOS arch=\\S+ event=LAUNCHER_FAILED status=7\\n"),
                record);
        assertFalse(record.contains("SECRET"));
        assertFalse(record.contains(home.toString()));
        assertFalse(Files.exists(root.resolve("launcher.log")));
        assertTrue(Files.size(logs.resolve("microcol.log")) < 512);
    }

    @Test
    void recordsRotateWithinThreeFilesOfAtMostFiveMiB() throws Exception {
        stub("exit 3");
        Files.createDirectories(logs);
        byte[] filler = new byte[(int) LIMIT - 10];
        java.util.Arrays.fill(filler, (byte) 'x');
        Files.write(logs.resolve("microcol.log"), filler);
        Files.writeString(logs.resolve("microcol.1.log"), "one");
        Files.writeString(logs.resolve("microcol.2.log"), "two");

        Result result = run();

        assertEquals(1, result.status, result.output);
        try (Stream<Path> files = Files.list(logs)) {
            assertEquals(3, files.count());
        }
        for (String name : List.of("microcol.log", "microcol.1.log",
                "microcol.2.log")) {
            assertTrue(Files.size(logs.resolve(name)) <= LIMIT, name);
        }
        assertEquals("one", Files.readString(logs.resolve("microcol.2.log")));
        assertEquals(LIMIT - 10, Files.size(logs.resolve("microcol.1.log")));
        assertTrue(Files.readString(logs.resolve("microcol.log"))
                .contains("status=3"));
    }

    @Test
    void unwritableLogFolderStillShowsDialogAndFails() throws Exception {
        stub("exit 9");
        Files.createDirectories(home.resolve("Library/Logs"));
        Files.writeString(logs, "not a directory");

        Result result = run();

        assertEquals(1, result.status, result.output);
        assertTrue(Files.exists(dialogs));
        assertEquals("not a directory", Files.readString(logs));
    }

    private void stub(String body) throws Exception {
        Files.writeString(launcher, "#!/bin/bash\n" + body + "\n");
        assertTrue(launcher.toFile().setExecutable(true));
        Path dialog = temporary.resolve("osascript");
        Files.writeString(dialog, "#!/bin/bash\n: > \"" + dialogs + "\"\n");
        assertTrue(dialog.toFile().setExecutable(true));
    }

    private Result run() throws Exception {
        Path out = temporary.resolve("wrapper.out");
        ProcessBuilder builder = new ProcessBuilder("bash", wrapper.toString())
                .redirectErrorStream(true).redirectOutput(out.toFile());
        builder.environment().put("HOME", home.toString());
        builder.environment().put("MICROCOL_DEV_ROOT", root.toString());
        builder.environment().put("MICROCOL_OSASCRIPT",
                temporary.resolve("osascript").toString());
        Process process = builder.start();
        assertTrue(process.waitFor(60, TimeUnit.SECONDS),
                "Wrapper did not finish");
        return new Result(process.exitValue(), Files.readString(out));
    }

    private static final class Result {
        final int status;
        final String output;

        Result(int status, String output) {
            this.status = status;
            this.output = output;
        }
    }

}
