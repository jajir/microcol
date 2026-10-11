package org.microcol.build;

import java.nio.file.Path;

/** Real hashing for both simulated operating systems, without external Perl/Python requirements. */
public final class LauncherChecksum {
    private LauncherChecksum() { }

    public static void main(String[] arguments) throws Exception {
        String filename = arguments[arguments.length - 1];
        System.out.println(BuildTestSupport.sha256(Path.of(filename)) + "  " + filename);
    }
}
