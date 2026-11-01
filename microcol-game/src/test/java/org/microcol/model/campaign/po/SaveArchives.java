package org.microcol.model.campaign.po;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Builds saved-game archives: valid player saves through the production writer
 * and summary owners, and archives with chosen raw entries, independently of
 * the production writer, so tests can describe damaged or unexpected content.
 */
public final class SaveArchives {

    private SaveArchives() {
    }

    /**
     * Valid metadata of a persistent model through the production owners: the
     * summary describes a model made from a copy of the data. A model without a
     * playthrough identity receives one, as a new game does.
     *
     * @param data required valid persistent model
     * @param name required save name
     * @return metadata of a manual save of the data
     */
    public static SaveMetadataPo metadata(final GameModelPo data,
            final String name) throws IOException {
        return metadata(data, name, data);
    }

    /**
     * Metadata whose summary describes a valid state while the identity and
     * campaign keys are those of the saved data, for saves whose game data
     * tests make invalid.
     *
     * @param valid required valid persistent model the summary describes
     * @param name  required save name
     * @param saved required saved data; receives an identity when it has none
     * @return metadata of a manual save of the saved data
     */
    public static SaveMetadataPo metadata(final GameModelPo valid,
            final String name, final GameModelPo saved) throws IOException {
        final GameModelPo data = saved;
        if (data.getPlaythroughId() == null) {
            data.setPlaythroughId(java.util.UUID.randomUUID().toString());
        }
        final GameModelPoDao dao = new GameModelPoDao();
        final Path copy = Files.createTempFile("summary-", ".json");
        try {
            dao.saveScenarioToFile(copy.toString(), valid);
            final org.microcol.model.Model model = org.microcol.model.Model
                    .make(dao.loadScenarioFromFile(copy.toFile()).getModel(),
                            org.microcol.model.campaign.MissionImpl
                                    .getPredefinedGameOverEvaluators());
            return SaveMetadataPo.of(name, SavedGames.Kind.MANUAL,
                    java.time.Instant.now(), data.getPlaythroughId(),
                    SaveSummaryPo.describe(model, data.getCampaignName(),
                            data.getMissionName()));
        } finally {
            Files.deleteIfExists(copy);
        }
    }

    /**
     * Write a valid manual player save of the data with the production writer.
     */
    public static void playerSave(final Path file, final GameModelPo data)
            throws IOException {
        new GameModelPoDao().savePlayerGame(file.toString(), data,
                metadata(data, "Test save"), null);
    }

    /**
     * @param file required archive
     * @return every entry by name, in archive order
     */
    public static Map<String, byte[]> read(final Path file) throws IOException {
        final Map<String, byte[]> out = new LinkedHashMap<>();
        try (InputStream in = Files.newInputStream(file);
                ZipInputStream zip = new ZipInputStream(in)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                out.put(entry.getName(), zip.readAllBytes());
            }
        }
        return out;
    }

    /**
     * Write the entries as an archive replacing the file.
     *
     * @param file    required target
     * @param entries entries by name, written in map order
     */
    public static void write(final Path file, final Map<String, byte[]> entries)
            throws IOException {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            for (final Map.Entry<String, byte[]> entry : entries.entrySet()) {
                zip.putNextEntry(new ZipEntry(entry.getKey()));
                zip.write(entry.getValue());
                zip.closeEntry();
            }
        }
        Files.write(file, bytes.toByteArray());
    }

    /**
     * Replace one entry of an archive, keeping the others.
     */
    public static void replace(final Path file, final String name,
            final String text) throws IOException {
        final Map<String, byte[]> entries = read(file);
        entries.put(name, text.getBytes(StandardCharsets.UTF_8));
        write(file, entries);
    }

    /**
     * Write an archive whose second entry has the same name as the first:
     * ZipOutputStream refuses duplicates, so a same-length placeholder name is
     * renamed in the finished bytes.
     */
    public static void writeDuplicate(final Path file,
            final Map<String, byte[]> entries, final String duplicated,
            final byte[] duplicate) throws IOException {
        final String placeholder = duplicated.substring(0,
                duplicated.length() - 1) + "#";
        final Map<String, byte[]> all = new LinkedHashMap<>(entries);
        all.put(placeholder, duplicate);
        write(file, all);
        final byte[] bytes = Files.readAllBytes(file);
        final byte[] from = placeholder.getBytes(StandardCharsets.UTF_8);
        final byte[] to = duplicated.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i + from.length <= bytes.length; i++) {
            boolean same = true;
            for (int j = 0; j < from.length && same; j++) {
                same = bytes[i + j] == from[j];
            }
            if (same) {
                System.arraycopy(to, 0, bytes, i, to.length);
            }
        }
        Files.write(file, bytes);
    }

    /**
     * @return minimal PNG header bytes declaring the given size; enough for the
     *         archive limits, not a decodable image
     */
    public static byte[] pngHeader(final int width, final int height) {
        final java.nio.ByteBuffer out = java.nio.ByteBuffer.allocate(33);
        out.put(new byte[] { (byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1a, '\n',
                0, 0, 0, 13, 'I', 'H', 'D', 'R' });
        out.putInt(width).putInt(height);
        out.put(new byte[] { 8, 2, 0, 0, 0, 0, 0, 0, 0 });
        return out.array();
    }
}
