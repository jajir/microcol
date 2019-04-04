package org.microcol.gui.preferences;

import java.io.File;
import java.util.Locale;
import java.util.function.Consumer;

import org.microcol.gui.util.PersistingTool;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SettingService {

    private final static String FILE_NAME = ".setting.json";

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
        final File settingFile = getSettingFile();
        if (!settingFile.exists()) {
            save(getDefaultSetting());
        }
        return settingDao.loadFromFile(getSettingFile());
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
        out.setLocale(Locale.getDefault());
        return out;
    }

}
