package org.microcol.build;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.archivers.zip.ZipFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Requires the launcher's package phase; ordinary clean test needs no built archives. */
class LauncherPackageIT {
    @TempDir Path temporary;

    @Test
    void packagedBootstrapsRemainExecutableAndInstallWithoutRoot() throws Exception {
        Path assets = BuildTestSupport.ROOT.resolve("microcol-launcher/target/dev-assets");
        Path mac = assets.resolve("MicroCol-Dev-macos.zip");
        assertTrue(Files.isRegularFile(mac), "Build launcher packages with mvn -pl microcol-build-tests -am verify");
        List<ArchiveSupport.Member> entries = ArchiveSupport.inspect(mac);
        assertEquals(0755, ArchiveSupport.member(entries, "MicroCol Dev.app/Contents/MacOS/MicroCol Dev").mode & 0777);
        assertTrue(ArchiveSupport.member(entries, "MicroCol Dev.app/Contents/Resources/microcol-dev").executable());
        assertTrue(ArchiveSupport.member(entries, "MicroCol Dev.command").executable());
        try (ZipFile zip = ZipFile.builder().setPath(mac).get();
                var input = zip.getInputStream(zip.getEntry("MicroCol Dev.app/Contents/Info.plist"))) {
            String plist = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(plist.matches("(?s).*<key>CFBundleExecutable</key>\\s*<string>MicroCol Dev</string>.*"), plist);
        }
        Path linux = assets.resolve("MicroCol-Dev-linux.tar.gz");
        assertTrue(Files.isRegularFile(linux), "Missing packaged Linux launcher: " + linux);
        List<ArchiveSupport.Member> linuxEntries = ArchiveSupport.inspect(linux);
        assertEquals(0755, ArchiveSupport.member(linuxEntries, "microcol-dev/install.sh").mode & 0777);
        assertTrue(ArchiveSupport.member(linuxEntries, "microcol-dev/microcol-dev").executable());

        Path base = Files.createDirectory(temporary.resolve("bootstrap install with spaces"));
        Path extraction = base.resolve("linux-bootstrap");
        ArchiveSupport.extract(linux, extraction);
        Path cache = base.resolve("cache with spaces");
        Path data = base.resolve("xdg data");
        Path commands = Files.createDirectory(base.resolve("commands"));
        BuildTestSupport.executable(commands.resolve("curl"), "#!/bin/bash\nprintf unexpected > \"$UNEXPECTED_DOWNLOAD\"\nexit 101\n");
        Map<String, String> environment = new HashMap<>();
        environment.put("HOME", Files.createDirectory(base.resolve("home")).toString());
        environment.put("MICROCOL_DEV_ROOT", cache.toString());
        environment.put("XDG_DATA_HOME", data.toString());
        environment.put("PATH", commands + System.getProperty("path.separator") + System.getenv("PATH"));
        environment.put("UNEXPECTED_DOWNLOAD", base.resolve("unexpected-download").toString());
        BuildTestSupport.Result result = BuildTestSupport.run(base, environment, Duration.ofSeconds(15),
                base.resolve("install.log"), extraction.resolve("microcol-dev/install.sh").toString());
        assertEquals(0, result.exit, result.output);
        assertTrue(Files.isExecutable(cache.resolve("launcher/microcol-dev")));
        assertEquals(Files.readString(BuildTestSupport.ROOT.resolve("scripts/launcher/microcol-dev")),
                Files.readString(cache.resolve("launcher/microcol-dev")));
        String desktop = Files.readString(data.resolve("applications/microcol-dev.desktop"));
        assertTrue(desktop.contains("Terminal=true"), desktop);
        assertTrue(desktop.contains("Exec=\"" + cache.resolve("launcher/microcol-dev") + "\""), desktop);
        assertFalse(Files.exists(cache.resolve("state.tsv")), "Bootstrap installation must not install a game");
        assertFalse(Files.exists(base.resolve("unexpected-download")), "Bootstrap installation must not fetch a game");
    }
}
