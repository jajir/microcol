package org.microcol.build;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarConstants;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ArchiveSupportTest {
    @TempDir
    Path temporary;

    private Path zip(String secondName, int mode) throws Exception {
        Path archive = temporary.resolve("fixture.zip");
        try (ZipArchiveOutputStream zip = new ZipArchiveOutputStream(archive)) {
            for (String name : Arrays.asList("App/safe", secondName)) {
                // Set the Unix platform before assigning a path; a DOS entry
                // normalizes backslashes.
                ZipArchiveEntry entry = new ZipArchiveEntry("placeholder") {
                    {
                        setUnixMode(name.equals("App/safe") ? 0100755 : mode);
                        setName(name);
                    }
                };
                zip.putArchiveEntry(entry);
                zip.write(new byte[] { 1 });
                zip.closeArchiveEntry();
            }
        }
        return archive;
    }

    @Test
    void unsafePathsAndDuplicateEntriesFailBeforeAnyExtraction()
            throws Exception {
        for (String name : Arrays.asList("../escaped", "/absolute",
                "App/../escaped", "App/./alias", "C:\\escape",
                "App/back\\slash", "App//duplicate", "App/control\tname",
                "App/safe")) {
            Path archive = zip(name, 0100644);
            Path destination = temporary.resolve("extracted");
            assertThrows(IOException.class,
                    () -> ArchiveSupport.extract(archive, destination), name);
            assertFalse(Files.exists(destination),
                    "Even a preceding safe member must not be extracted: "
                            + name);
        }
    }

    @Test
    void zipLinksAndSpecialFilesAreNotAnApplicationArchive() throws Exception {
        for (int mode : new int[] { 0120777, 0060644, 0020644, 0010644 }) {
            Path archive = zip("App/special", mode);
            assertThrows(IOException.class,
                    () -> ArchiveSupport.inspect(archive));
        }
    }

    @Test
    void tarLinksAreRejectedRatherThanFollowed() throws Exception {
        for (byte type : new byte[] { TarConstants.LF_SYMLINK,
                TarConstants.LF_LINK }) {
            Path archive = temporary.resolve("fixture.tar.gz");
            try (TarArchiveOutputStream tar = new TarArchiveOutputStream(
                    new GzipCompressorOutputStream(
                            Files.newOutputStream(archive)))) {
                TarArchiveEntry entry = new TarArchiveEntry("App/link", type);
                entry.setLinkName("/tmp/elsewhere");
                tar.putArchiveEntry(entry);
                tar.closeArchiveEntry();
            }
            assertThrows(IOException.class, () -> ArchiveSupport
                    .extract(archive, temporary.resolve("extracted")));
            assertFalse(Files.exists(temporary.resolve("extracted")));
        }
    }

    @Test
    void separateTopLevelApplicationsCannotPassTheUpdaterContract()
            throws Exception {
        Path archive = zip("Other/application", 0100644);
        assertThrows(IOException.class, () -> ArchiveSupport
                .singleRoot(ArchiveSupport.inspect(archive)));
    }

    @Test
    void regularFilesPreserveExecutableModesOnExtraction() throws Exception {
        Path archive = zip("App/config", 0100644);
        assertEquals("App",
                ArchiveSupport.singleRoot(ArchiveSupport.inspect(archive)));
        Path destination = temporary.resolve("extracted");
        ArchiveSupport.extract(archive, destination);
        assertTrue(Files.isExecutable(destination.resolve("App/safe")));
        assertFalse(Files.isExecutable(destination.resolve("App/config")));
        assertArrayEquals(new byte[] { 1 },
                Files.readAllBytes(destination.resolve("App/safe")));
    }
}
