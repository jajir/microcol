package org.microcol.build;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Runs only with an explicit native verification profile, after the native
 * module packages.
 */
class NativeDistributionIT {
    private static final Pattern WINDOW = Pattern.compile(
            "(?im)^\\s*(0x[0-9a-f]+)\\s+\"[^\"\\n]*microcol[^\"\\n]*\"[^\\n]*?\\b(\\d+)x(\\d+)\\+");

    @TempDir
    Path temporary;

    @Test
    void packagedDistributionHasVerifiedTestsRuntimeAndUsablePortableGame()
            throws Exception {
        Path evidence = Files.createDirectories(
                BuildTestSupport.OUTPUT.resolve("native-smoke"));
        Files.deleteIfExists(evidence.resolve("verified.txt"));
        String platform = platform(evidence);
        String build = System.getProperty("microcol.build.id", "local");
        assertTrue(
                build.equals("local")
                        || build.matches("dev-[1-9][0-9]*-[0-9a-f]{12}"),
                "Invalid build identifier: " + build);
        Files.writeString(evidence.resolve("game-tests.txt"),
                GameReportSupport.verify(BuildTestSupport.ROOT
                        .resolve("microcol-game/target/surefire-reports")));

        boolean mac = platform.startsWith("macos-");
        Path assets = BuildTestSupport.ROOT
                .resolve(mac ? "microcol-dist-macos/target/dev-assets"
                        : "microcol-dist-linux/target/dev-assets");
        String stem = "MicroCol-" + platform + "-" + build;
        Path archive = assets.resolve(stem + (mac ? ".zip" : ".tar.gz"));
        Path installer = assets.resolve(stem + (mac ? ".dmg" : ".deb"));
        verifyChecksum(archive);
        verifyChecksum(installer);
        assertInstallerSignature(installer, mac);
        List<ArchiveSupport.Member> members = ArchiveSupport.inspect(archive);
        String root = ArchiveSupport.singleRoot(members);
        if (mac) {
            assertEquals("MicroCol.app", root,
                    "The updater expects this application bundle name");
        }
        String launcher = mac ? root + "/Contents/MacOS/MicroCol"
                : root + "/bin/MicroCol";
        String java = mac ? root + "/Contents/runtime/Contents/Home/bin/java"
                : root + "/lib/runtime/bin/java";
        String modules = mac
                ? root + "/Contents/runtime/Contents/Home/lib/modules"
                : root + "/lib/runtime/lib/modules";
        assertTrue(ArchiveSupport.member(members, launcher).executable(),
                "Game launcher lost its Unix execute mode");
        assertTrue(ArchiveSupport.member(members, java).executable(),
                "Bundled Java lost its Unix execute mode");
        assertFalse(ArchiveSupport.member(members, modules).directory);
        Files.writeString(evidence.resolve("archive.txt"), "Archive: " + archive
                + "\nInstaller: " + installer + "\nSHA256 archive: "
                + BuildTestSupport.sha256(archive) + "\nSHA256 installer: "
                + BuildTestSupport.sha256(installer) + "\nRoot: " + root
                + "\nEntries: " + members.size() + "\n");

        Path extracted = temporary.resolve("portable application");
        ArchiveSupport.extract(archive, extracted);
        assertTrue(Files.isExecutable(extracted.resolve(java)));
        BuildTestSupport.Result runtime = BuildTestSupport.run(temporary,
                Map.of(), Duration.ofSeconds(30),
                evidence.resolve("runtime-modules.txt"),
                extracted.resolve(java).toString(), "--list-modules");
        assertEquals(0, runtime.exit, runtime.output);
        assertTrue(
                runtime.output.lines()
                        .anyMatch(line -> line.equals("microcol.game")
                                || line.startsWith("microcol.game@")),
                runtime.output);
        assertTrue(
                runtime.output.lines()
                        .anyMatch(line -> line.startsWith("java.desktop@")),
                runtime.output);
        if (!mac) {
            smokeLinux(extracted.resolve(launcher), evidence);
            smokeLinuxStartupFailure(extracted.resolve(launcher), evidence);
        }
        Files.writeString(evidence.resolve("verified.txt"), "Verified "
                + platform + " " + build
                + ": passing game tests, archive structure and modes, installer checksum, extracted runtime"
                + (mac ? ".\n" : ", and a live production MicroCol window.\n"));
    }

    private static String platform(Path evidence) throws Exception {
        BuildTestSupport.Result system = BuildTestSupport.run(
                BuildTestSupport.ROOT, Map.of(), Duration.ofSeconds(5),
                evidence.resolve("host-system.txt"), "uname", "-s");
        BuildTestSupport.Result architecture = BuildTestSupport.run(
                BuildTestSupport.ROOT, Map.of(), Duration.ofSeconds(5),
                evidence.resolve("host-architecture.txt"), "uname", "-m");
        assertEquals(0, system.exit, system.output);
        assertEquals(0, architecture.exit, architecture.output);
        String os = system.output.trim();
        String arch = architecture.output.trim();
        if (arch.equals("aarch64")) {
            arch = "arm64";
        } else if (arch.equals("amd64")) {
            arch = "x86_64";
        }
        String detected = (os.equals("Darwin") ? "macos-"
                : os.toLowerCase() + "-") + arch;
        assertTrue(
                Set.of("macos-arm64", "macos-x86_64", "linux-x86_64")
                        .contains(detected),
                "Unsupported native verification host: " + detected);
        String assigned = System.getProperty("microcol.platform", "").trim();
        if (!assigned.isEmpty()) {
            assertEquals(detected, assigned,
                    "Refusing to verify artifacts labelled for another host/architecture");
        }
        return detected;
    }

    private static void verifyChecksum(Path asset) throws Exception {
        assertTrue(Files.isRegularFile(asset),
                "Native build did not produce " + asset);
        assertTrue(Files.size(asset) > 0, "Empty asset: " + asset);
        Path sidecar = asset.resolveSibling(asset.getFileName() + ".sha256");
        assertTrue(Files.isRegularFile(sidecar),
                "Missing checksum: " + sidecar);
        String text = Files.readString(sidecar).trim();
        Matcher checksum = Pattern
                .compile("^([0-9a-f]{64})(?:[ \\t]+\\*?([^\\r\\n]+))?$")
                .matcher(text);
        assertTrue(checksum.matches(),
                "Malformed checksum sidecar: " + sidecar);
        assertEquals(BuildTestSupport.sha256(asset), checksum.group(1),
                "Checksum mismatch: " + asset);
        if (checksum.group(2) != null) {
            assertEquals(asset.getFileName().toString(), checksum.group(2),
                    "Checksum names another file");
        }
    }

    private static void assertInstallerSignature(Path installer, boolean mac)
            throws Exception {
        if (mac) {
            assertTrue(Files.size(installer) >= 512, "Truncated DMG");
            try (var channel = Files.newByteChannel(installer)) {
                channel.position(channel.size() - 512);
                java.nio.ByteBuffer signature = java.nio.ByteBuffer.allocate(4);
                assertEquals(4, channel.read(signature));
                assertEquals("koly", new String(signature.array(),
                        StandardCharsets.US_ASCII), "Not a UDIF DMG");
            }
        } else {
            try (InputStream input = Files.newInputStream(installer)) {
                assertEquals("!<arch>\n",
                        new String(input.readNBytes(8),
                                StandardCharsets.US_ASCII),
                        "Not a Debian ar package");
            }
        }
    }

    private void smokeLinux(Path launcher, Path evidence) throws Exception {
        assertNotNull(System.getenv("DISPLAY"),
                "Linux native verification needs Xvfb: wrap Maven with xvfb-run -a --server-args='-screen 0 1280x1024x24'");
        Path home = Files.createDirectory(temporary.resolve("isolated-home"));
        BuildTestSupport.Result initialWindows = BuildTestSupport.run(home,
                Map.of(), Duration.ofSeconds(10),
                evidence.resolve("windows-before.txt"), "xwininfo", "-root",
                "-tree");
        assertEquals(0, initialWindows.exit, initialWindows.output);
        Set<String> existing = usableWindows(initialWindows.output);
        Path log = evidence.resolve("game.log");
        ProcessBuilder builder = new ProcessBuilder(launcher.toString())
                .directory(home.toFile()).redirectErrorStream(true)
                .redirectOutput(log.toFile());
        builder.environment().remove("JAVA_HOME");
        builder.environment().remove("JDK_JAVA_OPTIONS");
        builder.environment().remove("_JAVA_OPTIONS");
        builder.environment().put("XDG_STATE_HOME",
                home.resolve("state").toString());
        builder.environment().put("HOME", home.toString());
        builder.environment().put("JAVA_TOOL_OPTIONS",
                "-DsoundEnabled=false -Duser.home=\"" + home
                        + "\" -Djava.util.prefs.userRoot=\""
                        + home.resolve("prefs") + "\"");
        Process process = builder.start();
        try {
            long deadline = System.nanoTime()
                    + Duration.ofSeconds(60).toNanos();
            while (System.nanoTime() < deadline) {
                assertTrue(process.isAlive(),
                        "Portable game exited before acceptance:\n"
                                + Files.readString(log));
                BuildTestSupport.Result windows = BuildTestSupport.run(home,
                        Map.of(), Duration.ofSeconds(10),
                        evidence.resolve("windows.txt"), "xwininfo", "-root",
                        "-tree");
                assertEquals(0, windows.exit, windows.output);
                Set<String> opened = usableWindows(windows.output);
                opened.removeAll(existing);
                if (!opened.isEmpty() && Files.readString(log)
                        .contains("started in production mode")) {
                    Thread.sleep(2000);
                    assertTrue(process.isAlive(),
                            "Game died immediately after opening its window:\n"
                                    + Files.readString(log));
                    Path diagnostics = home
                            .resolve("state/microcol/logs/microcol.log");
                    assertTrue(Files.exists(diagnostics),
                            "Packaged game wrote no diagnostic log: "
                                    + diagnostics);
                    assertTrue(
                            Files.readString(diagnostics)
                                    .contains("event=STARTED"),
                            "Diagnostic log lacks the start record");
                    assertSafeDiagnostics(diagnostics, home);
                    Files.copy(diagnostics, evidence.resolve("microcol.log"),
                            StandardCopyOption.REPLACE_EXISTING);
                    Files.writeString(evidence.resolve("linux-window.txt"),
                            "Owned launch PID: " + process.pid()
                                    + "\nNew usable MicroCol windows: " + opened
                                    + "\n");
                    return;
                }
                Thread.sleep(500);
            }
            fail("Packaged game did not open a usable production window within 60 seconds; see "
                    + evidence);
        } finally {
            BuildTestSupport.stopOwnedProcess(process);
        }
    }

    /**
     * Launches the packaged game with a home directory that is a regular file.
     * Startup then fails before any window opens; the failure must leave a safe
     * record and must not print a raw stack trace or touch the blocked path.
     */
    private void smokeLinuxStartupFailure(Path launcher, Path evidence)
            throws Exception {
        Path home = Files.createDirectory(temporary.resolve("unusable-home"));
        Path blockedHome = Files.writeString(home.resolve("home-is-a-file"),
                "not a directory");
        Path state = home.resolve("state");
        Path console = evidence.resolve("startup-failure.log");
        ProcessBuilder builder = new ProcessBuilder(launcher.toString())
                .directory(home.toFile()).redirectErrorStream(true)
                .redirectOutput(console.toFile());
        builder.environment().remove("JAVA_HOME");
        builder.environment().remove("JDK_JAVA_OPTIONS");
        builder.environment().remove("_JAVA_OPTIONS");
        builder.environment().put("XDG_STATE_HOME", state.toString());
        builder.environment().put("HOME", home.toString());
        builder.environment().put("JAVA_TOOL_OPTIONS",
                "-DsoundEnabled=false -Duser.home=\"" + blockedHome
                        + "\" -Djava.util.prefs.userRoot=\""
                        + home.resolve("prefs") + "\"");
        Process process = builder.start();
        try {
            assertTrue(process.waitFor(60, TimeUnit.SECONDS),
                    "Packaged game did not stop after a startup failure");
            assertNotEquals(0, process.exitValue(),
                    "Startup failure must exit unsuccessfully");
        } finally {
            BuildTestSupport.stopOwnedProcess(process);
        }
        Path diagnostics = state.resolve("microcol/logs/microcol.log");
        assertTrue(Files.exists(diagnostics),
                "Startup failure wrote no diagnostic log: " + diagnostics);
        assertTrue(
                Files.readString(diagnostics).contains("event=STARTUP_FAILED"),
                "Diagnostic log lacks the startup failure record");
        assertSafeDiagnostics(diagnostics, home, blockedHome);
        assertEquals("not a directory", Files.readString(blockedHome));
        assertFalse(Files.readString(console).contains("\tat "),
                "A raw stack trace reached the console");
        Files.copy(diagnostics,
                evidence.resolve("startup-failure-microcol.log"),
                StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * A packaged diagnostic record is bounded ASCII with build and platform
     * context, and has no JSON, message text or local path.
     */
    private static void assertSafeDiagnostics(Path diagnostics,
            Path... forbiddenPaths) throws Exception {
        byte[] bytes = Files.readAllBytes(diagnostics);
        assertTrue(bytes.length <= 5L * 1024 * 1024, "Log exceeds 5 MiB");
        for (byte value : bytes) {
            assertTrue(value >= 0, "Diagnostic log contains non-ASCII data");
        }
        String record = new String(bytes, StandardCharsets.US_ASCII);
        for (String field : new String[] { " build=", " os=\"", " arch=",
                " java=" }) {
            assertTrue(record.contains(field), "Record lacks " + field);
        }
        assertFalse(record.contains("{") || record.contains("}"),
                "Record contains JSON-like data");
        for (Path forbidden : forbiddenPaths) {
            assertFalse(record.contains(forbidden.toString()),
                    "Record contains a local path");
        }
        try (var files = Files.list(diagnostics.getParent())) {
            assertTrue(files.count() <= 3, "More than three log files");
        }
    }

    private static Set<String> usableWindows(String output) {
        Set<String> ids = new HashSet<>();
        Matcher matches = WINDOW.matcher(output);
        while (matches.find()) {
            if (Integer.parseInt(matches.group(2)) > 100
                    && Integer.parseInt(matches.group(3)) > 100) {
                ids.add(matches.group(1));
            }
        }
        return ids;
    }
}
