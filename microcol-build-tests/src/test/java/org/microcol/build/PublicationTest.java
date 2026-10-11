package org.microcol.build;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Runs the actual publisher against an isolated GitHub CLI double. */
class PublicationTest {
    private static final String SHA = "a".repeat(40);
    private static final String BUILD = "dev-10-" + SHA.substring(0, 12);

    @TempDir
    Path temporary;
    private Path repository;
    private Path assets;
    private Path state;
    private Path commands;

    @BeforeEach
    void prepare() throws Exception {
        repository = Path.of(System.getProperty("microcol.root", "..")).toAbsolutePath().normalize();
        assets = Files.createDirectories(temporary.resolve("assets with spaces"));
        state = Files.createDirectories(temporary.resolve("github"));
        commands = Files.createDirectories(temporary.resolve("commands"));
        Files.writeString(state.resolve("main"), SHA);
        createAssets(BUILD);
        final String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        // A JVM helper makes the fake CLI portable without adding a scripting runtime.
        final Path gh = commands.resolve("gh");
        final String classpath = System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"));
        Files.writeString(gh, "#!/bin/sh\nexec " + shell(java) + " -cp " + shell(classpath)
                + " org.microcol.build.PublicationFakeGh \"$@\"\n");
        assertTrue(gh.toFile().setExecutable(true));
    }

    @Test
    void completePublicationResolvesDraftByIdAndPromotesManifestLast() throws Exception {
        final Result result = publish(BUILD);
        assertEquals(0, result.code, result.output);
        assertEquals("false", Files.readString(release(BUILD).resolve("draft")));
        assertEquals(10, names(release(BUILD).resolve("files")).size());
        assertFalse(names(release(BUILD).resolve("files")).stream().anyMatch(name -> name.endsWith(".sha256")));
        assertEquals(Files.readString(file(BUILD, "manifest.tsv")), Files.readString(file("dev", "manifest.tsv")));
        final List<String> events = Files.readAllLines(state.resolve("events"));
        assertTrue(events.stream().anyMatch(line -> line.startsWith("release\tview\t" + BUILD)), "Draft lookup must use the draft-aware CLI.");
        assertTrue(events.stream().anyMatch(line -> line.matches("api\\trepos/jajir/microcol/releases/[0-9]+")));
        final List<String> uploads = events.stream().filter(line -> line.startsWith("release\tupload\t")).collect(Collectors.toList());
        assertTrue(uploads.get(uploads.size() - 1).startsWith("release\tupload\tdev\t"));
        assertTrue(uploads.get(uploads.size() - 1).contains("/manifest.tsv\t"));
    }

    @Test
    void incompleteAssetsAndWrongSidecarFailBeforeAnyNetworkAccess() throws Exception {
        final Path missing = assets.resolve("MicroCol-Dev-linux.tar.gz");
        Files.delete(missing);
        assertFalse(publish(BUILD).code == 0);
        assertFalse(Files.exists(state.resolve("events")));
        Files.writeString(missing, "restored with different content");
        final Result result = publish(BUILD);
        assertFalse(result.code == 0);
        assertTrue(result.output.contains("Checksum sidecar does not match"), result.output);
        assertFalse(Files.exists(state.resolve("events")));
    }

    @Test
    void interruptedDraftUploadNeverPublishesChannelAndRetryRepairsIt() throws Exception {
        Files.writeString(state.resolve("fail-upload"), "yes");
        assertFalse(publish(BUILD).code == 0);
        assertEquals("true", Files.readString(release(BUILD).resolve("draft")));
        assertFalse(Files.exists(release("dev")));
        Files.delete(state.resolve("fail-upload"));
        final Result retry = publish(BUILD);
        assertEquals(0, retry.code, retry.output);
        assertTrue(Files.isRegularFile(file("dev", "manifest.tsv")));
        assertEquals(1, Files.readAllLines(state.resolve("events")).stream()
                .filter(line -> line.startsWith("release\tcreate\t" + BUILD + "\t")).count());
    }

    @Test
    void omittedRemoteDigestUsesDownloadAndCorruptUploadedBytesBlockPublication() throws Exception {
        Files.writeString(state.resolve("omit-digests"), "yes");
        Files.writeString(state.resolve("corrupt-upload"), "yes");
        final Result bad = publish(BUILD);
        assertFalse(bad.code == 0);
        assertTrue(bad.output.contains("Remote release checksum mismatch"), bad.output);
        assertFalse(Files.exists(release("dev")));
        Files.delete(state.resolve("corrupt-upload"));
        final Result good = publish(BUILD);
        assertEquals(0, good.code, good.output);
        assertTrue(Files.readAllLines(state.resolve("events")).stream()
                .anyMatch(line -> line.startsWith("release\tdownload\t" + BUILD + "\t--pattern\t")));
    }

    @Test
    void obsoleteSourceBuildIsRetainedWithoutMovingChannel() throws Exception {
        Files.writeString(state.resolve("main"), "b".repeat(40));
        final Result result = publish(BUILD);
        assertEquals(0, result.code, result.output);
        assertEquals("false", Files.readString(release(BUILD).resolve("draft")));
        assertFalse(Files.exists(release("dev")));
    }

    @Test
    void olderRunCannotReplaceNewerPromotedBuild() throws Exception {
        final String newer = "dev-11-" + SHA.substring(0, 12);
        replaceAssets(newer);
        assertEquals(0, publish(newer).code);
        final String before = Files.readString(file("dev", "manifest.tsv"));
        replaceAssets(BUILD);
        final Result result = publish(BUILD);
        assertEquals(0, result.code, result.output);
        assertEquals(before, Files.readString(file("dev", "manifest.tsv")));
    }

    @Test
    void mainAdvancingDuringBootstrapUploadLeavesManifestUntouched() throws Exception {
        Files.writeString(state.resolve("advance-main"), "yes");
        final Result result = publish(BUILD);
        assertEquals(0, result.code, result.output);
        assertFalse(Files.exists(file("dev", "manifest.tsv")));
    }

    @Test
    void rerunReusesPublishedBytesAndDoesNotOverwriteImmutableAssets() throws Exception {
        assertEquals(0, publish(BUILD).code);
        final Map<String, String> before = checksums(release(BUILD).resolve("files"));
        final String name = "MicroCol-macos-arm64-" + BUILD + ".zip";
        Files.writeString(assets.resolve(name), "a rebuild with different timestamps");
        sidecar(name);
        Files.writeString(state.resolve("events"), "");
        final Result result = publish(BUILD);
        assertEquals(0, result.code, result.output);
        assertEquals(before, checksums(release(BUILD).resolve("files")));
        assertFalse(Files.readAllLines(state.resolve("events")).stream()
                .anyMatch(line -> line.startsWith("release\tupload\t" + BUILD + "\t")));
    }

    @Test
    void corruptExistingPublishedArchiveCannotBePromoted() throws Exception {
        assertEquals(0, publish(BUILD).code);
        Files.writeString(file(BUILD, "MicroCol-macos-arm64-" + BUILD + ".zip"), "corrupted archive");
        Files.delete(file("dev", "manifest.tsv"));
        final Result result = publish(BUILD);
        assertFalse(result.code == 0);
        assertTrue(result.output.contains("Existing release manifest checksums"), result.output);
        assertFalse(Files.exists(file("dev", "manifest.tsv")));
    }

    @Test
    void malformedPublishedManifestAndAuthenticationFailureDoNotLookLikeMissingRelease() throws Exception {
        Files.writeString(state.resolve("fail-api"), "yes");
        Result result = publish(BUILD);
        assertFalse(result.code == 0);
        assertTrue(result.output.contains("authentication failed"), result.output);
        assertFalse(Files.exists(release(BUILD)));
        Files.delete(state.resolve("fail-api"));
        assertEquals(0, publish(BUILD).code);
        final Path manifest = file(BUILD, "manifest.tsv");
        Files.writeString(manifest, Files.readString(manifest).replace("asset\tmacos-x86_64\t", "asset\tmacos-arm64\t"));
        result = publish(BUILD);
        assertFalse(result.code == 0);
        assertTrue(result.output.contains("Duplicate platform"), result.output);
    }

    private Result publish(final String build) throws Exception {
        final Path log = temporary.resolve("publisher.log");
        final ProcessBuilder builder = new ProcessBuilder("/bin/bash", repository.resolve("scripts/distribution/publish.sh").toString(),
                assets.toString(), build, SHA, "--repository", "jajir/microcol");
        builder.environment().put("PATH", commands + File.pathSeparator + System.getenv("PATH"));
        builder.environment().put("PUBLICATION_FAKE_STATE", state.toString());
        builder.environment().remove("GH_TOKEN");
        builder.environment().remove("GITHUB_TOKEN");
        builder.environment().remove("GITHUB_STEP_SUMMARY");
        builder.redirectErrorStream(true).redirectOutput(log.toFile());
        final Process process = builder.start();
        if (!process.waitFor(90, TimeUnit.SECONDS)) {
            process.destroyForcibly().waitFor();
            throw new AssertionError("Publisher did not exit: " + Files.readString(log));
        }
        return new Result(process.exitValue(), Files.readString(log));
    }

    private void createAssets(final String build) throws Exception {
        final List<String> names = new ArrayList<>(Arrays.asList("MicroCol-Dev-macos.zip", "MicroCol-Dev-linux.tar.gz"));
        for (String platform : Arrays.asList("macos-arm64", "macos-x86_64", "linux-x86_64")) {
            final String stem = "MicroCol-" + platform + "-" + build;
            names.add(stem + (platform.equals("linux-x86_64") ? ".tar.gz" : ".zip"));
            names.add(stem + (platform.equals("linux-x86_64") ? ".deb" : ".dmg"));
        }
        for (String name : names) {
            Files.writeString(assets.resolve(name), "fixture bytes for " + name);
            sidecar(name);
        }
    }

    private void replaceAssets(final String build) throws Exception {
        try (Stream<Path> files = Files.list(assets)) {
            for (Path file : files.collect(Collectors.toList())) {
                Files.delete(file);
            }
        }
        createAssets(build);
    }

    private void sidecar(final String name) throws Exception {
        Files.writeString(assets.resolve(name + ".sha256"), PublicationFakeGh.checksum(assets.resolve(name)) + "\n");
    }

    private Path release(final String tag) {
        return state.resolve("releases").resolve(tag);
    }

    private Path file(final String tag, final String name) {
        return release(tag).resolve("files").resolve(name);
    }

    private static List<String> names(final Path directory) throws Exception {
        try (Stream<Path> files = Files.list(directory)) {
            return files.map(path -> path.getFileName().toString()).sorted().collect(Collectors.toList());
        }
    }

    private static Map<String, String> checksums(final Path directory) throws Exception {
        final Map<String, String> result = new java.util.TreeMap<>();
        for (String name : names(directory)) {
            result.put(name, PublicationFakeGh.checksum(directory.resolve(name)));
        }
        return result;
    }

    private static String shell(final String value) {
        return "'" + value.replace("'", "'\\''") + "'";
    }

    private static final class Result {
        private final int code;
        private final String output;

        private Result(final int code, final String output) {
            this.code = code;
            this.output = output;
        }
    }
}
