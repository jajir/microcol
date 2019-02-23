package org.microcol.gui.preferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.microcol.gui.MicroColException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;

/**
 * Save setting to specific file and load setting object from specific file.
 */
public class SettingDao {

    private Logger logger = LoggerFactory.getLogger(SettingDao.class);

    private final Gson gson;

    @Inject
    SettingDao() {
        gson = new GsonBuilder().registerTypeAdapter(Locale.class, new GsonLocaleTypeAdapter())
                .setPrettyPrinting().create();
    }

    Setting loadFromFile(final File file) {
        Preconditions.checkNotNull(file, "File is null");
        Preconditions.checkArgument(file.exists(), "File '%s' doesn't exists.",
                file.getAbsolutePath());
        Preconditions.checkArgument(file.isFile());
        logger.debug("Starting to read from class path ({})", file.getAbsolutePath());
        try (final FileInputStream fis = new FileInputStream(file)) {
            return gson.fromJson(new InputStreamReader(fis, StandardCharsets.UTF_8), Setting.class);
        } catch (IOException e) {
            throw new MicroColException(e.getMessage(), e);
        }
    }

    void saveToFile(final Setting setting, final File file) {
        final String str = gson.toJson(setting);
        if (logger.isTraceEnabled()) {
            logger.trace(str);
        }
        try (final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8)) {
            outputStreamWriter.write(str);
        } catch (IOException e) {
            throw new MicroColException(e.getMessage(), e);
        }
    }

}
