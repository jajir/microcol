package org.microcol.model.campaign.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.microcol.gui.MicroColException;
import org.microcol.gui.dialog.PersistingService;
import org.microcol.gui.util.PersistingTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;

/**
 * DAO for loading and persisting information about campaigns. Currently is
 * stored:
 * <ul>
 * <li>if was mission finished</li>
 * </ul>
 */
public class CampaignsDao {

    private Logger logger = LoggerFactory.getLogger(CampaignsDao.class);

    private final String saveDir;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Inject
    CampaignsDao(final PersistingTool persistingTool) {
        this.saveDir = Preconditions
                .checkNotNull(persistingTool.getRootSaveDirectory().getAbsolutePath());
    }

    public void save(final CampaignsPo campaignsPo) {
        final String str = gson.toJson(campaignsPo);
        if (logger.isTraceEnabled()) {
            logger.trace(str);
        }
        try (final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                new FileOutputStream(getFile()), StandardCharsets.UTF_8);) {
            outputStreamWriter.write(str);
        } catch (IOException e) {
            throw new MicroColException(e.getMessage(), e);
        }
    }

    public CampaignsPo load() {
        if (getFile().exists()) {
            Preconditions.checkArgument(getFile().isFile());
            logger.debug("Starting to read from class path ({})", getFile().getAbsolutePath());
            try (final FileInputStream fis = new FileInputStream(getFile())) {
                Preconditions.checkNotNull(fis, "File input stream is null");
                return gson.fromJson(new InputStreamReader(fis, StandardCharsets.UTF_8),
                        CampaignsPo.class);
            } catch (IOException e) {
                throw new MicroColException(e.getMessage(), e);
            }
        } else {
            return new CampaignsPo();
        }
    }

    private File getFile() {
        return new File(
                saveDir + File.separator + ".campaigns." + PersistingService.SAVE_FILE_EXTENSION);
    }

}
