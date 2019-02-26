package org.microcol.gui.preferences;

import java.awt.Rectangle;
import java.util.Locale;
import java.util.Optional;
import java.util.prefs.Preferences;

import org.microcol.gui.PathPlanning;
import org.microcol.gui.util.Language;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.beans.property.SimpleBooleanProperty;

/**
 * Manage game preferences.
 * <p>
 * Preferences are stored depending on {@link Preferences} platform
 * implementation.
 * </p>
 */
public final class GamePreferences {

    /**
     * When there are no value in preferences than this value is used.
     */
    public static final Integer DEFAULT_VOLUME = 75;

    /**
     * When there are no value in preferences than this value is used.
     */
    public static final Integer DEFAULT_ANIMATION_SPEED = 3;

    /**
     * When there are no value in preferences than this value is used.
     */
    public static final boolean DEFAULT_SHOW_GRID = false;

    /**
     * When there are no value in preferences than this value is used.
     */
    public static final boolean DEFAULT_SHOW_FIGHT_ADVISOR = true;

    public static final String SYSTEM_PROPERTY_DEVELOPMENT = "development";

    /**
     * Default value is false. When it's set to 'false' than sound in game is
     * disabled.
     */
    private static final String SYSTEM_PROPERTY_SOUND_ENABLED = "soundEnabled";

    public static final String SYSTEM_PROPERTY_CLEAN_SETTINGS = "clean";

    //TODO move it outside of this class.
    private final SimpleBooleanProperty showFightAdvisorProperty = new SimpleBooleanProperty();

    private final SettingService settingService;

    @Inject
    public GamePreferences(final SettingService settingService) {
        this.settingService = Preconditions.checkNotNull(settingService);
        showFightAdvisorProperty.setValue(settingService.load().isShowFightAdvisor());
        showFightAdvisorProperty.addListener((object, old, newValue) -> {
            settingService.update(setting -> setting.setShowFightAdvisor(newValue));
        });
    }

    /**
     * Determine if application runs on Apple system.
     * 
     * @return return <code>true</code> when it's Apple system otherwise return
     *         <code>false</code>
     */
    public boolean isOSX() {
        final String osName = System.getProperty("os.name");
        return osName.contains("OS X");
    }

    public boolean isDevelopment() {
        return Boolean.valueOf(System.getProperty(SYSTEM_PROPERTY_DEVELOPMENT, "false"));
    }

    public boolean isSoundEnabled() {
        return Boolean.valueOf(System.getProperty(SYSTEM_PROPERTY_SOUND_ENABLED, "true"));
    }

    public void setMainFramePosition(final Rectangle bounds) {
        settingService.update(setting -> setting.setMainFramePosition(bounds));
    }

    public Rectangle getMainFramePosition() {
        final Rectangle out = settingService.load().getMainFramePosition();
        if (out == null) {
            return new Rectangle(-1, -1, -1, -1);
        }
        return out;
    }

    public void setMainFrameState(final int state) {
        settingService.update(setting -> setting.setMainFrameState(state));
    }

    public int getMainFrameState() {
        return settingService.load().getMainFrameState();
    }

    public void setLanguage(final Locale locale) {
        Preconditions.checkNotNull(locale);
        settingService.update(setting -> setting.setLocale(locale));
    }

    public Locale getLocale() {
        return settingService.load().getLocale();
    }

    public Language getLanguage() {
        final Optional<Language> lang = Language.resolve(getLocale());
        return lang.orElse(Language.en);
    }

    public void setVolume(final int volume) {
        settingService.update(setting -> setting.setVolume(volume));
    }

    public int getVolume() {
        return settingService.load().getVolume();
    }

    public void setAnimationSpeed(final int animationSpeed) {
        PathPlanning.checkSpeed(animationSpeed);
        settingService.update(setting -> setting.setAnimationSpeed(animationSpeed));
    }

    public int getAnimationSpeed() {
        return settingService.load().getAnimationSpeed();
    }

    public void setShowGrid(final boolean isGridShown) {
        settingService.update(setting -> setting.setShowGrid(isGridShown));
    }

    public boolean isGridShown() {
        return settingService.load().isShowGrid();
    }

    public void setGameInProgressSaveFile(final String gameInProgressSaveFile) {
        settingService.update(setting -> setting.setGameInProgressSaveFile(gameInProgressSaveFile));
    }

    public Optional<String> getGameInProgressSaveFile() {
        return Optional.ofNullable(settingService.load().getGameInProgressSaveFile());
    }

    public SimpleBooleanProperty getShowFightAdvisorProperty() {
        return showFightAdvisorProperty;
    }

}