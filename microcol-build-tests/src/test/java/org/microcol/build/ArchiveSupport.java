package org.microcol.build;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

/** Inspect archives independently of the launcher before extracting any members. */
final class ArchiveSupport {
    private ArchiveSupport() { }

    static List<Member> inspect(Path archive) throws IOException {
        List<Member> members = new ArrayList<>();
        if (archive.toString().endsWith(".zip")) {
            try (ZipFile zip = ZipFile.builder().setPath(archive).get()) {
                Enumeration<ZipArchiveEntry> entries = zip.getEntries();
                while (entries.hasMoreElements()) {
                    ZipArchiveEntry entry = entries.nextElement();
                    int mode = entry.getUnixMode();
                    int type = mode & 0170000;
                    require(type == 0100000 || type == 0040000,
                            "ZIP entry has no regular Unix file/directory type: " + entry.getName());
                    require(!entry.isUnixSymlink() && zip.canReadEntryData(entry),
                            "Unsupported ZIP entry: " + entry.getName());
                    require(entry.isDirectory() == (type == 0040000),
                            "Inconsistent ZIP entry type: " + entry.getName());
                    members.add(new Member(entry.getName(), mode, entry.isDirectory()));
                }
            }
        } else {
            try (TarArchiveInputStream tar = tar(archive)) {
                TarArchiveEntry entry;
                while ((entry = tar.getNextEntry()) != null) {
                    // Commons Compress also classifies a TAR hard link as a file.
                    require((entry.isFile() || entry.isDirectory()) && !entry.isLink() && !entry.isSymbolicLink(),
                            "Unsupported TAR entry: " + entry.getName());
                    require(!entry.isSparse() && tar.canReadEntryData(entry), "Unsupported TAR encoding: " + entry.getName());
                    members.add(new Member(entry.getName(), entry.getMode(), entry.isDirectory()));
                }
            }
        }
        require(!members.isEmpty(), "Empty archive: " + archive);
        Set<String> names = new HashSet<>();
        Set<String> files = new HashSet<>();
        for (Member member : members) {
            String name = member.name;
            require(!name.isEmpty() && !name.startsWith("/") && !name.contains("\\")
                    && !name.matches("^[A-Za-z]:.*") && !name.contains("//")
                    && name.chars().noneMatch(c -> c < 32 || c == 127), "Unsafe archive path: " + name);
            String canonical = name.endsWith("/") ? name.substring(0, name.length() - 1) : name;
            for (String component : canonical.split("/")) {
                require(!component.isEmpty() && !component.equals(".") && !component.equals(".."),
                        "Unsafe archive path: " + name);
            }
            require(names.add(canonical), "Duplicate archive member: " + name);
            if (!member.directory) {
                files.add(canonical);
            }
        }
        for (String name : names) {
            int slash = name.indexOf('/');
            while (slash >= 0) {
                require(!files.contains(name.substring(0, slash)), "File is also an archive parent: " + name);
                slash = name.indexOf('/', slash + 1);
            }
        }
        return members;
    }

    static String singleRoot(List<Member> members) throws IOException {
        Set<String> roots = new HashSet<>();
        for (Member member : members) {
            roots.add(member.name.split("/", 2)[0]);
        }
        require(roots.size() == 1, "Game archive must have exactly one application directory: " + roots);
        String root = roots.iterator().next();
        require(members.stream().anyMatch(m -> m.name.startsWith(root + "/")), "Archive root is not a directory");
        return root;
    }

    static Member member(List<Member> members, String name) throws IOException {
        for (Member member : members) {
            if (member.name.equals(name)) {
                return member;
            }
        }
        throw new IOException("Missing archive member: " + name);
    }

    static void extract(Path archive, Path destination) throws IOException {
        // Validate the complete archive before creating even its first output file.
        inspect(archive);
        Files.createDirectories(destination);
        if (archive.toString().endsWith(".zip")) {
            try (ZipFile zip = ZipFile.builder().setPath(archive).get()) {
                Enumeration<ZipArchiveEntry> entries = zip.getEntries();
                while (entries.hasMoreElements()) {
                    ZipArchiveEntry entry = entries.nextElement();
                    try (InputStream data = zip.getInputStream(entry)) {
                        extractMember(destination, new Member(entry.getName(), entry.getUnixMode(), entry.isDirectory()), data);
                    }
                }
            }
        } else {
            try (TarArchiveInputStream tar = tar(archive)) {
                TarArchiveEntry entry;
                while ((entry = tar.getNextEntry()) != null) {
                    extractMember(destination, new Member(entry.getName(), entry.getMode(), entry.isDirectory()), tar);
                }
            }
        }
    }

    private static void extractMember(Path destination, Member member, InputStream data) throws IOException {
        Path output = destination.resolve(member.name).normalize();
        require(output.startsWith(destination.normalize()), "Archive path escaped extraction root");
        if (member.directory) {
            Files.createDirectories(output);
        } else {
            Files.createDirectories(output.getParent());
            try (OutputStream file = Files.newOutputStream(output)) {
                data.transferTo(file);
            }
            Files.setPosixFilePermissions(output, permissions(member.mode));
        }
    }

    private static Set<PosixFilePermission> permissions(int mode) {
        Set<PosixFilePermission> permissions = EnumSet.noneOf(PosixFilePermission.class);
        PosixFilePermission[] flags = PosixFilePermission.values();
        for (int index = 0; index < flags.length; index++) {
            if ((mode & (1 << (8 - index))) != 0) {
                permissions.add(flags[index]);
            }
        }
        return permissions;
    }

    private static TarArchiveInputStream tar(Path path) throws IOException {
        return new TarArchiveInputStream(GzipCompressorInputStream.builder().setPath(path).get());
    }

    private static void require(boolean condition, String message) throws IOException {
        if (!condition) {
            throw new IOException(message);
        }
    }

    static final class Member {
        final String name;
        final int mode;
        final boolean directory;

        Member(String name, int mode, boolean directory) {
            this.name = name;
            this.mode = mode;
            this.directory = directory;
        }

        boolean executable() {
            return !directory && (mode & 0111) != 0;
        }
    }
}
