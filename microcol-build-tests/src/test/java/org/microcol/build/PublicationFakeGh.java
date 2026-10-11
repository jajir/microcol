package org.microcol.build;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Stateful GitHub CLI double used only by publication process tests. */
public final class PublicationFakeGh {
    private static Path state;

    private PublicationFakeGh() {
    }

    public static void main(final String[] arguments) throws Exception {
        state = Path.of(System.getenv("PUBLICATION_FAKE_STATE"));
        Files.createDirectories(state.resolve("releases"));
        final List<String> args = new ArrayList<>(Arrays.asList(arguments));
        log(String.join("\t", args));
        if (args.get(0).equals("api")) {
            api(args.get(1));
            return;
        }
        if (!args.get(0).equals("release")) {
            fail("unsupported fake gh command");
        }
        final String operation = args.get(1);
        final String tag = args.get(2);
        final Path release = state.resolve("releases").resolve(tag);
        if (operation.equals("view")) {
            if (!Files.isDirectory(release)) {
                fail("release not found");
            }
            System.out.println("{\"databaseId\":" + Files.readString(release.resolve("id")) + "}");
        } else if (operation.equals("create")) {
            Files.createDirectories(release.resolve("files"));
            Files.writeString(release.resolve("draft"), Boolean.toString(args.contains("--draft")));
            try (Stream<Path> releases = Files.list(state.resolve("releases"))) {
                Files.writeString(release.resolve("id"), Long.toString(100 + releases.count()));
            }
        } else if (operation.equals("upload")) {
            if (Files.exists(state.resolve("fail-upload"))) {
                fail("simulated interrupted upload");
            }
            for (int index = 3; index < args.size(); index++) {
                final String value = args.get(index);
                if (value.equals("--repo")) {
                    break;
                }
                if (!value.startsWith("--")) {
                    final Path file = Path.of(value);
                    Files.copy(file, release.resolve("files").resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                }
            }
            if (tag.equals("dev") && Files.exists(state.resolve("advance-main"))) {
                Files.writeString(state.resolve("main"), "b".repeat(40));
            }
            if (Files.exists(state.resolve("corrupt-upload")) && !tag.equals("dev")) {
                Files.writeString(release.resolve("files").resolve("MicroCol-Dev-macos.zip"), "damaged in transit");
            }
        } else if (operation.equals("edit")) {
            if (args.contains("--draft=false")) {
                Files.writeString(release.resolve("draft"), "false");
            }
        } else if (operation.equals("download")) {
            if (args.contains("--pattern")) {
                final String name = value(args, "--pattern");
                Files.copy(release.resolve("files").resolve(name), Path.of(value(args, "--output")), StandardCopyOption.REPLACE_EXISTING);
            } else {
                final Path destination = Path.of(value(args, "--dir"));
                Files.createDirectories(destination);
                for (Path file : files(release.resolve("files"))) {
                    Files.copy(file, destination.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } else {
            fail("unsupported fake release operation " + operation);
        }
    }

    private static void api(final String path) throws Exception {
        if (Files.exists(state.resolve("fail-api"))) {
            fail("gh: authentication failed (HTTP 403)");
        }
        if (path.endsWith("git/ref/heads/main")) {
            System.out.println("{\"object\":{\"sha\":" + quote(Files.readString(state.resolve("main"))) + "}}");
            return;
        }
        final String tagMarker = "/releases/tags/";
        if (path.contains(tagMarker)) {
            final String tag = path.substring(path.indexOf(tagMarker) + tagMarker.length());
            final Path release = state.resolve("releases").resolve(tag);
            // This deliberately models the API behavior that broke the first real publication.
            if (!Files.isDirectory(release) || Files.readString(release.resolve("draft")).equals("true")) {
                fail("gh: Not Found (HTTP 404)");
            }
            describe(release);
            return;
        }
        final String id = path.substring(path.lastIndexOf('/') + 1);
        for (Path release : files(state.resolve("releases"))) {
            if (Files.readString(release.resolve("id")).equals(id)) {
                describe(release);
                return;
            }
        }
        fail("gh: Not Found (HTTP 404)");
    }

    private static void describe(final Path release) throws Exception {
        final List<String> assets = new ArrayList<>();
        for (Path file : files(release.resolve("files"))) {
            final String digest = Files.exists(state.resolve("omit-digests")) ? "null" : quote("sha256:" + checksum(file));
            assets.add("{\"name\":" + quote(file.getFileName().toString()) + ",\"digest\":" + digest + "}");
        }
        System.out.println("{\"id\":" + Files.readString(release.resolve("id")) + ",\"tag_name\":"
                + quote(release.getFileName().toString()) + ",\"draft\":" + Files.readString(release.resolve("draft"))
                + ",\"assets\":[" + String.join(",", assets) + "]}");
    }

    static String checksum(final Path path) throws Exception {
        final byte[] digest = MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path));
        final StringBuilder result = new StringBuilder();
        for (byte value : digest) {
            result.append(String.format("%02x", value & 0xff));
        }
        return result.toString();
    }

    private static List<Path> files(final Path directory) throws IOException {
        try (Stream<Path> files = Files.list(directory)) {
            return files.sorted(Comparator.comparing(Path::toString)).collect(Collectors.toList());
        }
    }

    private static String value(final List<String> args, final String flag) {
        return args.get(args.indexOf(flag) + 1);
    }

    private static String quote(final String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
    }

    private static void log(final String line) throws IOException {
        Files.writeString(state.resolve("events"), line + "\n", StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    private static void fail(final String message) {
        System.err.println(message);
        System.exit(1);
    }
}
