package org.akropia.gui.preferences;

import java.awt.Rectangle;
import java.util.Locale;
import java.util.Optional;
import java.util.prefs.Preferences;

import org.akropia.gui.MusicPlayer;
import org.akropia.gui.util.Language;
import org.akropia.gui.util.PathPlanningService;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Manage game preferences.
 * <p>
 * Preferences are stored depending on {@link Preferences} platform
 * implementation.
 * </p>
 */
public class GamePreferences {

    /**
     * When there are no value in preferences than this value is used.
     */
    public static final Integer DEFAULT_VOLUME = 75;

    /**
     * When there are no value in preferences than this value is used.
     */
    public static final Integer DEFAULT_ANIMATION_SPEED = 1;

    public static final int DEFAULT_MAP_SCROLL_SPEED = 2;
    public static final int MAX_MAP_SCROLL_SPEED = 9;

    /**
     * When there are no value in preferences than this value is used.
     */
    public static final boolean DEFAULT_SHOW_GRID = false;

    /**
     * When there are no value in preferences than this value is used.
     */
    public static final boolean DEFAULT_SHOW_FIGHT_ADVISOR = true;

    /**
     * When there are no value in preferences than this value is used. Critical
     * turn notices are shown even when this is off.
     */
    public static final boolean DEFAULT_SHOW_TURN_SUMMARY = true;

    /**
     * When there are no value in preferences than this value is used. A Europe
     * arrival then only offers Open Europe in the report.
     */
    public static final boolean DEFAULT_OPEN_EUROPE_ON_ARRIVAL = false;

    public static final String SYSTEM_PROPERTY_DEVELOPMENT = "development";

    /**
     * Default value is true. When it's set to 'false' than music and effects
     * are silent whatever volumes are saved.
     */
    private static final String SYSTEM_PROPERTY_SOUND_ENABLED = "soundEnabled";

    public static final String SYSTEM_PROPERTY_CLEAN_SETTINGS = "clean";

    /**
     * When Akropia starts for first time it use this windows width.
     */
    public static final int DEFAULT_WINDOW_WIDTH = 1000;

    /**
     * When Akropia starts for first time it use this windows height.
     */
    public static final int DEFAULT_WINDOW_HEIGHT = 925;

    private final SettingService settingService;

    @Inject
    public GamePreferences(final SettingService settingService) {
        this.settingService = Preconditions.checkNotNull(settingService);
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
        return Boolean.valueOf(
                System.getProperty(SYSTEM_PROPERTY_DEVELOPMENT, "false"));
    }

    public boolean isSoundEnabled() {
        return Boolean.valueOf(
                System.getProperty(SYSTEM_PROPERTY_SOUND_ENABLED, "true"));
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

    /**
     * A setting without its own effects volume first keeps the effects at the
     * value they had through the music, so changing the music never moves them.
     *
     * @param volume music volume from 0 (silent) to 100
     */
    public void setMusicVolume(final int volume) {
        checkVolume(volume);
        settingService.update(setting -> {
            if (setting.getEffectsVolume() == null) {
                setting.setEffectsVolume(effectsVolume(setting));
            }
            setting.setVolume(volume);
        });
    }

    public int getMusicVolume() {
        return validVolume(settingService.load().getVolume());
    }

    public void setEffectsVolume(final int volume) {
        checkVolume(volume);
        settingService.update(setting -> setting.setEffectsVolume(volume));
    }

    /**
     * A setting saved before the separate effects volume has none; effects then
     * follow the saved music volume, so a player who chose silence stays
     * silent.
     *
     * @return effects volume from 0 (silent) to 100
     */
    public int getEffectsVolume() {
        return effectsVolume(settingService.load());
    }

    private static int effectsVolume(final Setting setting) {
        return validVolume(
                setting.getEffectsVolume() == null ? setting.getVolume()
                        : setting.getEffectsVolume());
    }

    private static void checkVolume(final int volume) {
        Preconditions.checkArgument(
                volume >= MusicPlayer.MIN_VOLUME
                        && volume <= MusicPlayer.MAX_VOLUME,
                "Volume %s is out of range", volume);
    }

    /**
     * A missing saved volume uses the default; an edited file outside the range
     * is clamped to it.
     */
    private static int validVolume(final Integer saved) {
        if (saved == null) {
            return DEFAULT_VOLUME;
        }
        return Math.max(MusicPlayer.MIN_VOLUME,
                Math.min(MusicPlayer.MAX_VOLUME, saved));
    }

    public void setShowFightAdvisor(final boolean showFightAdvisor) {
        settingService.update(
                setting -> setting.setShowFightAdvisor(showFightAdvisor));
    }

    public boolean isShowFightAdvisor() {
        return settingService.load().isShowFightAdvisor();
    }

    public void setAnimationSpeed(final int animationSpeed) {
        PathPlanningService.checkSpeed(animationSpeed);
        settingService
                .update(setting -> setting.setAnimationSpeed(animationSpeed));
    }

    public int getMapScrollSpeed() {
        final Integer speed = settingService.load().getMapScrollSpeed();
        return speed == null || speed < 1 || speed > MAX_MAP_SCROLL_SPEED
                ? DEFAULT_MAP_SCROLL_SPEED
                : speed;
    }

    public void setMapScrollSpeed(final int speed) {
        Preconditions.checkArgument(speed >= 1 && speed <= MAX_MAP_SCROLL_SPEED,
                "Map scroll speed must be between 1 and 9");
        settingService.update(setting -> setting.setMapScrollSpeed(speed));
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

    public void setAutosaveEnabled(final boolean enabled) {
        settingService.update(setting -> setting.setAutosaveEnabled(enabled));
    }

    public boolean isAutosaveEnabled() {
        return settingService.load().isAutosaveEnabled();
    }

    public void setShowTurnSummary(final boolean showTurnSummary) {
        settingService.update(
                setting -> setting.setTurnSummaryEnabled(showTurnSummary));
    }

    public boolean isShowTurnSummary() {
        return settingService.load().isTurnSummaryEnabled();
    }

    public void setOpenEuropeOnArrival(final boolean openEuropeOnArrival) {
        settingService.update(
                setting -> setting.setOpenEuropeOnArrival(openEuropeOnArrival));
    }

    public boolean isOpenEuropeOnArrival() {
        return settingService.load().isOpenEuropeOnArrival();
    }

    public void setGameInProgressSaveFile(final String gameInProgressSaveFile) {
        settingService.update(setting -> setting
                .setGameInProgressSaveFile(gameInProgressSaveFile));
    }

    public Optional<String> getGameInProgressSaveFile() {
        return Optional
                .ofNullable(settingService.load().getGameInProgressSaveFile());
    }

}
