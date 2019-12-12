package org.microcol.gui.preferences;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.microcol.gui.MicroColException;
import org.microcol.gui.util.PersistingTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SettingService {

    /**
     * Default backup directory name. There will stored old saved files.
     */
    private final static String BACKUP_DIR = "backup-";

    private final static String FILE_NAME = ".setting.json";

    private final Logger logger = LoggerFactory.getLogger(SettingService.class);

    private final SettingDao settingDao;

    private final PersistingTool persistingTool;

    @Inject
    SettingService(final SettingDao settingDao, final PersistingTool persistingTool) {
        this.settingDao = Preconditions.checkNotNull(settingDao);
        this.persistingTool = Preconditions.checkNotNull(persistingTool);
    }

    public void save(final Setting setting) {
        settingDao.saveToFile(setting, getSettingFile());
    }

    public Setting load() {
        final Setting out = simpleLoad();
        if (Setting.CURRENT_VERSION.equals(out.getVersion())) {
            return out;
        }
        logger.info(
                "Saved saved data are from version {} this is version {}. Old data will be stored.",
                out.getVersion(), Setting.CURRENT_VERSION);

        /*
         * Saved setting is from different version. Create new directory
         * backup-000 and so on and move there content of setting directory and
         * final create new settings.
         */
        final File backupDir = makeBackupDirectory(persistingTool.getRootSaveDirectory());
        moveDataTobackupDir(persistingTool.getRootSaveDirectory(), backupDir);
        return simpleLoad();
    }

    private Setting simpleLoad() {
        final File settingFile = getSettingFile();
        if (!settingFile.exists()) {
            save(getDefaultSetting());
        }
        
        Optional<Setting> oSetting = settingDao.loadFromFile(getSettingFile());
        if (oSetting.isEmpty()) {
            save(getDefaultSetting());
            oSetting = settingDao.loadFromFile(getSettingFile());
            if (oSetting.isEmpty()) {
                throw new MicroColException(String.format("Unable to load setting from '%s'",
                        settingFile.getAbsolutePath()));
            } else {
                return oSetting.get();
            }
        } else {
            return oSetting.get();
        }
    }

    private void moveDataTobackupDir(final File homeDir, final File backupDir) {
        Preconditions.checkNotNull(homeDir);
        Preconditions.checkArgument(homeDir.exists(), "directory %s with saves doesn't exists",
                homeDir.getAbsolutePath());
        Preconditions.checkArgument(homeDir.isDirectory());
        Preconditions.checkNotNull(backupDir);
        Preconditions.checkArgument(backupDir.exists(), "backupDir %s doesn't exists",
                backupDir.getAbsolutePath());
        Preconditions.checkArgument(backupDir.isDirectory());
        try {
            final Path homePath = Path.of(homeDir.getAbsolutePath());
            final Path backupPath = Path.of(backupDir.getAbsolutePath());
            final List<String> filesToMove = Files.list(homePath)
                    .filter(path -> !path.getFileName().toString().startsWith(BACKUP_DIR))
                    .map(path -> path.getFileName().toString()).collect(Collectors.toList());
            filesToMove.forEach(path -> {
                final File from = homePath.resolve(path).toFile();
                final File to = backupPath.resolve(path).toFile();
                try {
                    com.google.common.io.Files.move(from, to);
                } catch (IOException e) {
                    throw new MicroColException(e.getMessage(), e);
                }
            });
        } catch (IOException e) {
            throw new MicroColException(e.getMessage(), e);
        }
    }

    private File makeBackupDirectory(final File homeDir) {
        Preconditions.checkNotNull(homeDir);
        Preconditions.checkArgument(homeDir.exists(), "directory %s with saves doesn't exists",
                homeDir.getAbsolutePath());
        Preconditions.checkArgument(homeDir.isDirectory());
        try {
            final Path homePath = Path.of(homeDir.getAbsolutePath());
            int backups = (int) Files.list(homePath)
                    .filter(path -> path.getFileName().toString().startsWith(BACKUP_DIR)).count();
            final File backup = homePath
                    .resolve(BACKUP_DIR + Strings.padStart(String.valueOf(backups), 4, '0'))
                    .toFile();
            backup.mkdir();
            return backup;
        } catch (IOException e) {
            throw new MicroColException(e.getMessage(), e);
        }
    }

    public void update(final Consumer<Setting> setter) {
        final Setting setting = load();
        setter.accept(setting);
        save(setting);
    }

    private File getSettingFile() {
        return new File(persistingTool.getRootSaveDirectory(), FILE_NAME);
    }

    private Setting getDefaultSetting() {
        final Setting out = new Setting();
        out.setVolume(GamePreferences.DEFAULT_VOLUME);
        out.setAnimationSpeed(GamePreferences.DEFAULT_ANIMATION_SPEED);
        out.setShowGrid(GamePreferences.DEFAULT_SHOW_GRID);
        out.setShowFightAdvisor(GamePreferences.DEFAULT_SHOW_FIGHT_ADVISOR);
        out.setVersion(Setting.CURRENT_VERSION);
        out.setLocale(Locale.getDefault());
        return out;
    }

}
