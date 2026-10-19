package org.microcol.diagnostics;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

import com.google.common.base.Preconditions;

/**
 * Bounded local record of recoverable failures. A record is plain ASCII and
 * holds the fixed event, build, operating system, architecture, Java version,
 * time, the exception class chain and at most eight MicroCol stack frames.
 * Exception messages, player text and save content are never written; every
 * value that is not a fixed label is reduced to a short ASCII form. Every
 * record is also printed to the console. Failures of the log itself, including
 * unexpected input, are reported on the console and never propagate into
 * gameplay, saving or loading.
 */
public final class DiagnosticLog {

    static final long MAX_FILE_BYTES = 5L * 1024 * 1024;

    static final int MAX_FILES = 3;

    static final String FILE_NAME = "microcol.log";

    /** Records are ASCII, so characters and bytes are the same count. */
    static final int MAX_RECORD_CHARS = 4096;

    private static final int MAX_FRAMES = 8;

    private static final int MAX_CAUSES = 4;

    private static final int MAX_NAME = 120;

    private static final int MAX_FIELD = 48;

    private static final String MICROCOL_PACKAGE = "org.microcol.";

    private final Path directory;

    private final String buildVersion;

    private final long maxFileBytes;

    private final int maxFiles;

    public DiagnosticLog(final Path directory, final String buildVersion) {
        this(directory, buildVersion, MAX_FILE_BYTES, MAX_FILES);
    }

    DiagnosticLog(final Path directory, final String buildVersion,
            final long maxFileBytes, final int maxFiles) {
        this.directory = Preconditions.checkNotNull(directory);
        this.buildVersion = Preconditions.checkNotNull(buildVersion);
        Preconditions.checkArgument(maxFileBytes >= MAX_RECORD_CHARS,
                "Each file must hold at least one record");
        Preconditions.checkArgument(maxFiles >= 2,
                "At least one archive is needed next to the active file");
        this.maxFileBytes = maxFileBytes;
        this.maxFiles = maxFiles;
    }

    /**
     * Platform directory for the diagnostic log. It is never inside the save
     * directory, so saved games and settings backups do not move it.
     *
     * @return directory for the log files
     */
    public static Path defaultDirectory() {
        final String home = System.getProperty("user.home");
        final String os = System.getProperty("os.name", "");
        if (os.contains("OS X")) {
            return Path.of(home, "Library", "Logs", "MicroCol");
        }
        if (os.startsWith("Windows")) {
            final String localAppData = System.getenv("LOCALAPPDATA");
            final Path base = localAppData == null || localAppData.isBlank()
                    ? Path.of(home, "AppData", "Local")
                    : Path.of(localAppData);
            return base.resolve("MicroCol").resolve("Logs");
        }
        final String stateHome = System.getenv("XDG_STATE_HOME");
        final Path base = stateHome != null && !stateHome.isBlank()
                && Path.of(stateHome).isAbsolute() ? Path.of(stateHome)
                        : Path.of(home, ".local", "state");
        return base.resolve("microcol").resolve("logs");
    }

    public Path getDirectory() {
        return directory;
    }

    /**
     * File that receives new records. Older records rotate into numbered
     * archives in the same directory.
     *
     * @return active log file
     */
    public Path getActiveFile() {
        return directory.resolve(FILE_NAME);
    }

    /**
     * Appends one record and prints it to the console. This method never
     * throws; a failed write is reported on the console instead.
     *
     * @param event   fixed event label
     * @param failure failure to describe, or <code>null</code> for a plain
     *                event
     */
    public synchronized void record(final DiagnosticEvent event,
            final Throwable failure) {
        try {
            final String record = format(event, failure);
            System.err.print(record);
            try {
                append(record);
            } catch (IOException | RuntimeException unavailable) {
                System.err.println("MicroCol diagnostic log is unavailable ("
                        + ascii(unavailable.getClass().getName(), MAX_NAME)
                        + "); the record above was not stored.");
            }
        } catch (RuntimeException unexpected) {
            System.err
                    .println("MicroCol diagnostic record could not be formed.");
        }
    }

    String format(final DiagnosticEvent event, final Throwable failure) {
        final StringBuilder out = new StringBuilder();
        out.append(Instant.now()).append(" build=")
                .append(ascii(buildVersion, MAX_FIELD)).append(" os=\"")
                .append(ascii(System.getProperty("os.name"), MAX_FIELD))
                .append(' ')
                .append(ascii(System.getProperty("os.version"), MAX_FIELD))
                .append("\" arch=")
                .append(ascii(System.getProperty("os.arch"), MAX_FIELD))
                .append(" java=")
                .append(ascii(System.getProperty("java.version"), MAX_FIELD))
                .append(" event=")
                .append(event == null ? "UNKNOWN" : event.name());
        if (failure != null) {
            out.append(" cause=").append(causeChain(failure));
            int frames = 0;
            for (final StackTraceElement frame : failure.getStackTrace()) {
                if (frames == MAX_FRAMES) {
                    break;
                }
                if (frame != null && String.valueOf(frame.getClassName())
                        .startsWith(MICROCOL_PACKAGE)) {
                    out.append("\n    at ")
                            .append(ascii(frame.getClassName(), MAX_NAME))
                            .append('.')
                            .append(ascii(frame.getMethodName(), MAX_NAME))
                            .append('(').append(frame.getLineNumber())
                            .append(')');
                    frames++;
                }
            }
        }
        if (out.length() > MAX_RECORD_CHARS - 1) {
            out.setLength(MAX_RECORD_CHARS - 1);
        }
        return out.append('\n').toString();
    }

    private static String causeChain(final Throwable failure) {
        final StringBuilder chain = new StringBuilder();
        Throwable current = failure;
        for (int depth = 0; current != null && depth < MAX_CAUSES; depth++) {
            if (depth > 0) {
                chain.append(" <- ");
            }
            chain.append(ascii(current.getClass().getName(), MAX_NAME));
            current = current.getCause();
        }
        return chain.toString();
    }

    /**
     * Reduces a value to a short, single-line ASCII form. Anything outside
     * letters, digits and a few separators becomes a question mark.
     */
    static String ascii(final String value, final int maxLength) {
        if (value == null) {
            return "unknown";
        }
        final int length = Math.min(value.length(), maxLength);
        final StringBuilder out = new StringBuilder(length);
        for (int index = 0; index < length; index++) {
            final char c = value.charAt(index);
            final boolean safe = c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z'
                    || c >= '0' && c <= '9' || ".-_+$ <>".indexOf(c) >= 0;
            out.append(safe ? c : '?');
        }
        return out.toString();
    }

    private void append(final String record) throws IOException {
        Files.createDirectories(directory);
        final byte[] bytes = record.getBytes(StandardCharsets.US_ASCII);
        if (Files.exists(getActiveFile())
                && Files.size(getActiveFile()) + bytes.length > maxFileBytes) {
            rotate();
        }
        Files.write(getActiveFile(), bytes, StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
    }

    /**
     * Keeps the active file plus {@code maxFiles - 1} archives. The oldest
     * archive is removed before the others move one step older.
     */
    private void rotate() throws IOException {
        final int archives = maxFiles - 1;
        Files.deleteIfExists(archive(archives));
        for (int index = archives - 1; index >= 1; index--) {
            if (Files.exists(archive(index))) {
                Files.move(archive(index), archive(index + 1),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        }
        Files.move(getActiveFile(), archive(1),
                StandardCopyOption.REPLACE_EXISTING);
    }

    private Path archive(final int index) {
        return directory.resolve("microcol." + index + ".log");
    }

}
