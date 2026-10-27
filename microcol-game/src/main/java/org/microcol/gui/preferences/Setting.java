package org.microcol.gui.preferences;

import java.awt.Rectangle;
import java.util.Locale;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 * Object representing MicroCol setting.
 */
public class Setting {

    /**
     * Current setting file version.
     */
    final static String CURRENT_VERSION = "1.0";

    private Rectangle mainFramePosition;

    private int mainFrameState;

    private Locale locale;

    private Integer volume;

    private Integer animationSpeed;

    private boolean showGrid;

    private boolean developerMode;

    private boolean showFightAdvisor;

    private Boolean autosaveEnabled;

    private Boolean turnSummaryEnabled;

    private Boolean openEuropeOnArrival;

    private String version;

    private String gameInProgressSaveFile;

    /**
     * Independent copy, so a remembered setting cannot be changed by callers.
     *
     * @return copy with the same values
     */
    Setting copy() {
        final Setting out = new Setting();
        out.mainFramePosition = mainFramePosition == null ? null
                : new Rectangle(mainFramePosition);
        out.mainFrameState = mainFrameState;
        out.locale = locale;
        out.volume = volume;
        out.animationSpeed = animationSpeed;
        out.showGrid = showGrid;
        out.developerMode = developerMode;
        out.showFightAdvisor = showFightAdvisor;
        out.autosaveEnabled = autosaveEnabled;
        out.turnSummaryEnabled = turnSummaryEnabled;
        out.openEuropeOnArrival = openEuropeOnArrival;
        out.version = version;
        out.gameInProgressSaveFile = gameInProgressSaveFile;
        return out;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mainFramePosition, mainFrameState, locale,
                volume, animationSpeed, showGrid, developerMode,
                showFightAdvisor, version, gameInProgressSaveFile,
                autosaveEnabled, turnSummaryEnabled, openEuropeOnArrival);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Setting other = (Setting) obj;
        return Objects.equal(this.mainFramePosition, other.mainFramePosition)
                && Objects.equal(this.mainFrameState, other.mainFrameState)
                && Objects.equal(this.locale, other.locale)
                && Objects.equal(this.volume, other.volume)
                && Objects.equal(this.animationSpeed, other.animationSpeed)
                && Objects.equal(this.showGrid, other.showGrid)
                && this.developerMode == other.developerMode
                && Objects.equal(this.showFightAdvisor, other.showFightAdvisor)
                && Objects.equal(this.autosaveEnabled, other.autosaveEnabled)
                && Objects.equal(this.turnSummaryEnabled,
                        other.turnSummaryEnabled)
                && Objects.equal(this.openEuropeOnArrival,
                        other.openEuropeOnArrival)
                && Objects.equal(this.version, other.version)
                && Objects.equal(this.gameInProgressSaveFile,
                        other.gameInProgressSaveFile);

    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(Setting.class)
                .add("mainFramePosition", mainFramePosition)
                .add("mainFrameState", mainFrameState).add("locale", locale)
                .add("volume", volume).add("animationSpeed", animationSpeed)
                .add("showGrid", showGrid).add("developerMode", developerMode)
                .add("showFightAdvisor", showFightAdvisor)
                .add("autosaveEnabled", autosaveEnabled)
                .add("turnSummaryEnabled", turnSummaryEnabled)
                .add("openEuropeOnArrival", openEuropeOnArrival)
                .add("version", version)
                .add("gameInProgressSaveFile", gameInProgressSaveFile)
                .toString();
    }

    public Rectangle getMainFramePosition() {
        return mainFramePosition;
    }

    public void setMainFramePosition(Rectangle mainFramePosition) {
        this.mainFramePosition = mainFramePosition;
    }

    public int getMainFrameState() {
        return mainFrameState;
    }

    public void setMainFrameState(int mainFrameState) {
        this.mainFrameState = mainFrameState;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public Integer getVolume() {
        return volume;
    }

    public void setVolume(Integer volume) {
        this.volume = volume;
    }

    public Integer getAnimationSpeed() {
        return animationSpeed;
    }

    public void setAnimationSpeed(Integer animationSpeed) {
        this.animationSpeed = animationSpeed;
    }

    public boolean isDeveloperMode() {
        return developerMode;
    }

    public void setDeveloperMode(final boolean enabled) {
        developerMode = enabled;
    }

    public boolean isShowGrid() {
        return showGrid;
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
    }

    public boolean isShowFightAdvisor() {
        return showFightAdvisor;
    }

    public void setShowFightAdvisor(boolean showFightAdvisor) {
        this.showFightAdvisor = showFightAdvisor;
    }

    public boolean isAutosaveEnabled() {
        return autosaveEnabled == null || autosaveEnabled;
    }

    public void setAutosaveEnabled(final boolean enabled) {
        autosaveEnabled = enabled;
    }

    /**
     * Missing value means the routine summary is shown, as in a new game.
     *
     * @return <code>true</code> when the routine turn summary is shown
     */
    public boolean isTurnSummaryEnabled() {
        return turnSummaryEnabled == null || turnSummaryEnabled;
    }

    public void setTurnSummaryEnabled(final boolean enabled) {
        turnSummaryEnabled = enabled;
    }

    /**
     * Missing value means the report does not open Europe by itself.
     *
     * @return <code>true</code> when a Europe arrival opens Europe
     *         automatically
     */
    public boolean isOpenEuropeOnArrival() {
        return openEuropeOnArrival != null && openEuropeOnArrival;
    }

    public void setOpenEuropeOnArrival(final boolean enabled) {
        openEuropeOnArrival = enabled;
    }

    public String getGameInProgressSaveFile() {
        return gameInProgressSaveFile;
    }

    public void setGameInProgressSaveFile(String gameInProgressSaveFile) {
        this.gameInProgressSaveFile = gameInProgressSaveFile;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
