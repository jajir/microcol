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

    private boolean showFightAdvisor;
    
    private String version;

    private String gameInProgressSaveFile;

    @Override
    public int hashCode() {
        return Objects.hashCode(mainFramePosition, mainFrameState, locale, volume, animationSpeed,
                showGrid, showFightAdvisor, version, gameInProgressSaveFile);
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
                && Objects.equal(this.showFightAdvisor, other.showFightAdvisor)
                && Objects.equal(this.version, other.version)
                && Objects.equal(this.gameInProgressSaveFile, other.gameInProgressSaveFile);

    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(Setting.class).add("mainFramePosition", mainFramePosition)
                .add("mainFrameState", mainFrameState).add("locale", locale).add("volume", volume)
                .add("animationSpeed", animationSpeed).add("showGrid", showGrid)
                .add("showFightAdvisor", showFightAdvisor).add("version", version)
                .add("gameInProgressSaveFile", gameInProgressSaveFile).toString();
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
