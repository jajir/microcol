package org.microcol.build;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

final class BuildTestSupport {
    static final Path ROOT = Paths
            .get(System.getProperty("microcol.root", "..")).toAbsolutePath()
            .normalize();
    static final Path OUTPUT = Paths
            .get(System.getProperty("microcol.test.output", "target"))
            .toAbsolutePath();

    private BuildTestSupport() {
    }

    static String sha256(Path path) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream input = Files.newInputStream(path)) {
            byte[] buffer = new byte[65536];
            int length;
            while ((length = input.read(buffer)) != -1) {
                digest.update(buffer, 0, length);
            }
        }
        StringBuilder hex = new StringBuilder();
        for (byte value : digest.digest()) {
            hex.append(String.format("%02x", value & 0xff));
        }
        return hex.toString();
    }

    static Result run(Path cwd, Map<String, String> environment,
            Duration timeout, Path output, String... command) throws Exception {
        ProcessBuilder builder = new ProcessBuilder(command)
                .directory(cwd.toFile()).redirectErrorStream(true)
                .redirectOutput(output.toFile());
        builder.environment().putAll(environment);
        Process process = builder.start();
        try {
            if (!process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                throw new AssertionError(
                        "Timed out: " + String.join(" ", command) + "\n"
                                + Files.readString(output));
            }
            return new Result(process.exitValue(), Files.readString(output));
        } finally {
            stopOwnedProcess(process);
        }
    }

    /**
     * No broad pkill: only this process and descendants observed while it is
     * owned.
     */
    static void stopOwnedProcess(Process process) throws InterruptedException {
        List<ProcessHandle> children = process.descendants()
                .collect(Collectors.toCollection(ArrayList::new));
        Collections.reverse(children);
        children.forEach(ProcessHandle::destroy);
        if (process.isAlive()) {
            process.destroy();
            if (!process.waitFor(3, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                process.waitFor(3, TimeUnit.SECONDS);
            }
        }
        children.stream().filter(ProcessHandle::isAlive)
                .forEach(ProcessHandle::destroyForcibly);
    }

    static void executable(Path path, String content) throws IOException {
        Files.writeString(path, content);
        Files.setPosixFilePermissions(path,
                java.nio.file.attribute.PosixFilePermissions
                        .fromString("rwxr-xr-x"));
    }

    static final class Result {
        final int exit;
        final String output;

        Result(int exit, String output) {
            this.exit = exit;
            this.output = output;
        }
    }
}
