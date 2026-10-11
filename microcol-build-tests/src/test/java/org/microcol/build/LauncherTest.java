package org.microcol.build;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarConstants;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LauncherTest {
    private static final Path LAUNCHER = BuildTestSupport.ROOT.resolve("scripts/launcher/microcol-dev");
    private static final String[] PLATFORMS = {"macos-arm64", "macos-x86_64", "linux-x86_64"};

    @TempDir Path temporary;
    private Path base;
    private Path fixtures;
    private Path cache;
    private Map<String, String> environment;
    private int invocation;

    @BeforeEach
    void prepareFixtures() throws Exception {
        base = Files.createDirectory(temporary.resolve("launcher test with spaces"));
        fixtures = Files.createDirectory(base.resolve("fixtures"));
        Path commands = Files.createDirectory(base.resolve("commands"));
        cache = base.resolve("cache with spaces");
        environment = new HashMap<>();
        environment.put("PATH", commands + System.getProperty("path.separator") + System.getenv("PATH"));
        environment.put("HOME", Files.createDirectory(base.resolve("home")).toString());
        environment.put("MICROCOL_DEV_ROOT", cache.toString());
        environment.put("FAKE_CURL_FIXTURES", fixtures.toString());
        environment.put("GAME_LOG", base.resolve("game.log").toString());
        environment.put("OPEN_LOG", base.resolve("open.log").toString());
        environment.put("XATTR_LOG", base.resolve("xattr.log").toString());
        environment.put("SIMULATED_OS", "Linux");
        environment.put("SIMULATED_ARCH", "x86_64");
        environment.put("TEST_JAVA", Path.of(System.getProperty("java.home"), "bin/java").toString());
        environment.put("TEST_CLASSPATH", System.getProperty("java.class.path"));
        command(commands, "uname", "if [ \"$1\" = -s ]; then printf '%s\\n' \"$SIMULATED_OS\"; else printf '%s\\n' \"$SIMULATED_ARCH\"; fi\n");
        String hash = "exec \"$TEST_JAVA\" -cp \"$TEST_CLASSPATH\" org.microcol.build.LauncherChecksum \"$@\"\n";
        command(commands, "sha256sum", hash);
        command(commands, "shasum", hash);
        command(commands, "open", "printf '%s\\n' \"$@\" > \"$OPEN_LOG\"\n");
        command(commands, "xattr", "printf '%s\\n' \"$@\" > \"$XATTR_LOG\"\n");
        command(commands, "curl",
                "previous=; output=; proto=; redirects=; deadline=; connect=; url=\n"
                + "for argument in \"$@\"; do\n"
                + "  case \"$previous\" in\n"
                + "    --output) output=$argument ;;\n"
                + "    --proto) proto=$argument ;;\n"
                + "    --proto-redir) redirects=$argument ;;\n"
                + "    --max-time) deadline=$argument ;;\n"
                + "    --connect-timeout) connect=$argument ;;\n"
                + "  esac\n"
                + "  previous=$argument; url=$argument\n"
                + "done\n"
                + "[ \"$proto\" = '=https' ] && [ \"$redirects\" = '=https' ] && [ -n \"$deadline\" ] && [ -n \"$connect\" ] || exit 101\n"
                + "case \"$url\" in https://github.com/jajir/microcol/releases/download/*) ;; *) exit 102 ;; esac\n"
                + "name=${url##*/}\n"
                + "if [ \"$name\" = manifest.tsv ] && [ \"${FAIL_MANIFEST:-0}\" = 1 ]; then exit 28; fi\n"
                + "source=$FAKE_CURL_FIXTURES/$name\n"
                + "[ -f \"$source\" ] || exit 22\n"
                + "if [ \"$name\" != manifest.tsv ]; then\n"
                + "  printf started > \"$FAKE_CURL_FIXTURES/asset-started\"\n"
                + "  if [ -n \"${PAUSE_ASSET:-}\" ]; then sleep \"$PAUSE_ASSET\"; fi\n"
                + "  if [ \"${PARTIAL_ASSET:-0}\" = 1 ]; then head -c 16 \"$source\" > \"$output\"; exit 18; fi\n"
                + "fi\n"
                + "cp \"$source\" \"$output\"\n");
    }

    private static void command(Path directory, String name, String body) throws Exception {
        BuildTestSupport.executable(directory.resolve(name), "#!/bin/bash\nset -eu\n" + body);
    }

    private BuildTestSupport.Result launcher(String... arguments) throws Exception {
        List<String> command = new ArrayList<>();
        command.add(LAUNCHER.toString());
        command.addAll(Arrays.asList(arguments));
        return BuildTestSupport.run(base, environment, Duration.ofSeconds(25),
                base.resolve("launcher-" + (++invocation) + ".log"), command.toArray(new String[0]));
    }

    private void succeeded(BuildTestSupport.Result result) {
        assertEquals(0, result.exit, result.output);
    }

    private List<String> state() throws Exception {
        return Arrays.asList(Files.readString(cache.resolve("state.tsv")).trim().split("\t"));
    }

    private String install(int number) throws Exception {
        String build = makeRelease(number, false, false);
        succeeded(launcher("--update-only"));
        return build;
    }

    private String makeRelease(int number, boolean traversal, boolean symlink) throws Exception {
        String commit = Integer.toHexString(number).repeat(40).substring(0, 40);
        String build = "dev-" + number + "-" + commit.substring(0, 12);
        List<String> rows = new ArrayList<>();
        rows.add("microcol-dev-v1");
        rows.add("build\t" + build + "\t" + commit);
        for (String platform : PLATFORMS) {
            String filename = "MicroCol-" + platform + "-" + build;
            if (platform.startsWith("macos")) {
                filename += ".zip";
                try (ZipArchiveOutputStream zip = new ZipArchiveOutputStream(fixtures.resolve(filename))) {
                    zipFile(zip, "MicroCol.app/Contents/MacOS/MicroCol", "#!/bin/sh\nexit 0\n", 0100755);
                    zipFile(zip, "MicroCol.app/Contents/runtime/Contents/Home/lib/modules", build, 0100644);
                    if (traversal) {
                        zipFile(zip, "../escaped", "unexpected", 0100644);
                    }
                    if (symlink) {
                        zipFile(zip, "MicroCol.app/escape", "/tmp", 0120777);
                    }
                }
            } else {
                filename += ".tar.gz";
                String root = "MicroCol-0.7.0-SNAPSHOT-linux-amd64";
                try (TarArchiveOutputStream tar = new TarArchiveOutputStream(
                        new GzipCompressorOutputStream(Files.newOutputStream(fixtures.resolve(filename))))) {
                    tarFile(tar, root + "/bin/MicroCol", "#!/bin/sh\nprintf '%s\\n' '" + build + "' >> \"$GAME_LOG\"\n", 0755);
                    tarFile(tar, root + "/lib/runtime/lib/modules", build, 0644);
                    if (traversal) {
                        tarFile(tar, "../escaped", "unexpected", 0644);
                    }
                    if (symlink) {
                        TarArchiveEntry entry = new TarArchiveEntry(root + "/escape", TarConstants.LF_SYMLINK);
                        entry.setLinkName("/tmp");
                        entry.setMode(0777);
                        tar.putArchiveEntry(entry);
                        tar.closeArchiveEntry();
                    }
                }
            }
            rows.add("asset\t" + platform + "\t" + filename + "\t" + BuildTestSupport.sha256(fixtures.resolve(filename)));
        }
        Files.writeString(fixtures.resolve("manifest.tsv"), String.join("\n", rows) + "\n");
        return build;
    }

    private static void zipFile(ZipArchiveOutputStream zip, String name, String contents, int mode) throws Exception {
        ZipArchiveEntry entry = new ZipArchiveEntry(name);
        entry.setUnixMode(mode);
        zip.putArchiveEntry(entry);
        zip.write(contents.getBytes(StandardCharsets.UTF_8));
        zip.closeArchiveEntry();
    }

    private static void tarFile(TarArchiveOutputStream tar, String name, String contents, int mode) throws Exception {
        byte[] data = contents.getBytes(StandardCharsets.UTF_8);
        TarArchiveEntry entry = new TarArchiveEntry(name);
        entry.setMode(mode);
        entry.setSize(data.length);
        tar.putArchiveEntry(entry);
        tar.write(data);
        tar.closeArchiveEntry();
    }

    @Test
    void checkDoesNotInstallThenValidUpdateAndLaunch() throws Exception {
        String build = makeRelease(1, false, false);
        BuildTestSupport.Result checked = launcher("--check");
        succeeded(checked);
        assertTrue(checked.output.contains(build), checked.output);
        assertFalse(Files.exists(cache.resolve("state.tsv")));
        succeeded(launcher());
        assertEquals(Arrays.asList(build, "-", "-"), state());
        assertEquals(build, Files.readString(base.resolve("game.log")).trim());
    }

    @Test
    void malformedManifestCannotInstallOrChooseAnArbitraryUrl() throws Exception {
        makeRelease(1, false, false);
        Path manifest = fixtures.resolve("manifest.tsv");
        String original = Files.readString(manifest);
        List<String> malformed = Arrays.asList(
                original.replace("asset\tmacos-x86_64\t", "asset\tmacos-arm64\t"),
                original.replace("MicroCol-linux-x86_64-", "https://attacker.invalid/"),
                original.replace("microcol-dev-v1", "microcol-dev-v2"),
                original.substring(0, original.lastIndexOf("asset\t")),
                original.replace("\n", "\r\n"),
                original.replace("build\tdev-1-111111111111", "build\tdev-1-222222222222"));
        for (String invalid : malformed) {
            Files.writeString(manifest, invalid);
            assertNotEquals(0, launcher("--update-only").exit, invalid);
            assertFalse(Files.exists(cache.resolve("state.tsv")));
        }
    }

    @Test
    void checksumMismatchPreservesCurrentVersion() throws Exception {
        String previous = install(1);
        makeRelease(2, false, false);
        Path manifest = fixtures.resolve("manifest.tsv");
        List<String> rows = Files.readAllLines(manifest);
        String[] fields = rows.get(rows.size() - 1).split("\t");
        fields[3] = "0".repeat(64);
        rows.set(rows.size() - 1, String.join("\t", fields));
        Files.write(manifest, rows);
        BuildTestSupport.Result result = launcher("--update-only");
        assertNotEquals(0, result.exit);
        assertTrue(result.output.contains("checksum"), result.output);
        assertEquals(previous, state().get(0));
        try (var versions = Files.list(cache.resolve("versions"))) {
            assertEquals(1, versions.count());
        }
    }

    @Test
    void partialDownloadPreservesCurrentAndCleansWork() throws Exception {
        String previous = install(1);
        makeRelease(2, false, false);
        environment.put("PARTIAL_ASSET", "1");
        assertNotEquals(0, launcher("--update-only").exit);
        assertEquals(previous, state().get(0));
        try (var files = Files.list(cache)) {
            assertFalse(files.anyMatch(path -> path.getFileName().toString().startsWith("work.")));
        }
        assertFalse(Files.exists(cache.resolve("update.lock")));
    }

    @Test
    void traversalAndSymlinksAreRejectedBeforeExtractionOnBothPlatforms() throws Exception {
        for (String system : Arrays.asList("Linux", "Darwin")) {
            environment.put("SIMULATED_OS", system);
            environment.put("SIMULATED_ARCH", system.equals("Darwin") ? "arm64" : "x86_64");
            for (boolean traversal : Arrays.asList(true, false)) {
                makeRelease(1, traversal, !traversal);
                BuildTestSupport.Result result = launcher("--update-only");
                assertNotEquals(0, result.exit, system + ": " + result.output);
                assertTrue(result.output.contains("Unsafe or damaged"), result.output);
                assertFalse(Files.exists(cache.resolve("state.tsv")));
                try (var paths = Files.walk(cache)) {
                    assertFalse(paths.anyMatch(path -> path.getFileName().toString().equals("escaped")));
                }
            }
        }
    }

    @Test
    void offlineFirstRunFailsButInstalledGameStillLaunches() throws Exception {
        environment.put("FAIL_MANIFEST", "1");
        BuildTestSupport.Result first = launcher();
        assertNotEquals(0, first.exit);
        assertTrue(first.output.contains("No usable cached game"), first.output);
        environment.remove("FAIL_MANIFEST");
        String current = install(1);
        environment.put("FAIL_MANIFEST", "1");
        succeeded(launcher());
        assertEquals(current, Files.readString(base.resolve("game.log")).trim());
        assertNotEquals(0, launcher("--update-only").exit);
    }

    @Test
    void rollbackHoldsRejectedBuildUntilNextRelease() throws Exception {
        String first = install(1);
        String second = install(2);
        assertEquals(Arrays.asList(second, first, "-"), state());
        succeeded(launcher("--rollback"));
        assertEquals(Arrays.asList(first, second, second), state());
        succeeded(launcher());
        assertEquals(first, state().get(0));
        String third = install(3);
        assertEquals(Arrays.asList(third, first, "-"), state());
        assertTrue(Files.isDirectory(cache.resolve("versions").resolve(second)));
    }

    @Test
    void staleManifestDoesNotDowngradeCurrentGame() throws Exception {
        String current = install(2);
        makeRelease(1, false, false);
        BuildTestSupport.Result result = launcher("--update-only");
        succeeded(result);
        assertTrue(result.output.contains("Ignoring stale release"), result.output);
        assertEquals(current, state().get(0));
    }

    @Test
    void concurrentUpdatesSerializeAndLeaveTheOldGameIntact() throws Exception {
        String first = install(1);
        Path oldGame = cache.resolve("versions").resolve(first).resolve("app/bin/MicroCol");
        byte[] original = Files.readAllBytes(oldGame);
        String second = makeRelease(2, false, false);
        Files.deleteIfExists(fixtures.resolve("asset-started"));
        Path firstLog = base.resolve("concurrent-first.log");
        ProcessBuilder builder = new ProcessBuilder(LAUNCHER.toString(), "--update-only")
                .directory(base.toFile()).redirectErrorStream(true).redirectOutput(firstLog.toFile());
        builder.environment().putAll(environment);
        builder.environment().put("PAUSE_ASSET", "1.5");
        Process process = builder.start();
        try {
            long deadline = System.nanoTime() + Duration.ofSeconds(8).toNanos();
            while (!Files.exists(fixtures.resolve("asset-started"))) {
                assertTrue(process.isAlive(), "First updater failed: " + Files.readString(firstLog));
                assertTrue(System.nanoTime() < deadline, "First updater never started its download");
                Thread.sleep(20);
            }
            assertEquals(first, state().get(0));
            BuildTestSupport.Result other = launcher("--update-only");
            assertTrue(process.waitFor(10, TimeUnit.SECONDS), "First updater deadlocked");
            assertEquals(0, process.exitValue(), Files.readString(firstLog));
            succeeded(other);
            assertEquals(Arrays.asList(second, first, "-"), state());
            assertArrayEquals(original, Files.readAllBytes(oldGame));
            assertFalse(Files.exists(cache.resolve("update.lock")));
        } finally {
            BuildTestSupport.stopOwnedProcess(process);
        }
    }

    @Test
    void deadOwnerLockRecovers() throws Exception {
        makeRelease(1, false, false);
        Path lock = Files.createDirectories(cache.resolve("update.lock"));
        Files.writeString(lock.resolve("pid"), "999999999\n");
        succeeded(launcher("--update-only"));
        assertFalse(Files.exists(lock));
    }

    @Test
    void macosKeepsQuarantineAndUsesOpenInsteadOfBypassingSecurity() throws Exception {
        environment.put("SIMULATED_OS", "Darwin");
        environment.put("SIMULATED_ARCH", "arm64");
        String build = install(1);
        List<String> quarantine = Files.readAllLines(base.resolve("xattr.log"));
        assertEquals(Arrays.asList("-w", "com.apple.quarantine"), quarantine.subList(0, 2));
        assertTrue(quarantine.get(2).startsWith("0083;"));
        succeeded(launcher());
        assertEquals(Arrays.asList(cache.resolve("versions").resolve(build).resolve("MicroCol.app").toString(), "--args"),
                Files.readAllLines(base.resolve("open.log")));
        assertFalse(Files.exists(base.resolve("game.log")));
    }

}
