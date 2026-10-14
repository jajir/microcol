package org.microcol.gui.preferences;

import java.awt.Rectangle;
import java.io.File;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

public abstract class AbstractPreferencesTest {

    @TempDir
    protected File temporaryDirectory;

    protected File SETTING_FILE;

    @BeforeEach
    void prepareSettingFile() {
        SETTING_FILE = new File(temporaryDirectory, "setting.txt");
    }

    protected Setting createSetting() {
        final Setting out = new Setting();
        out.setAnimationSpeed(12);
        out.setLocale(new Locale("cs", "CZ"));
        out.setMainFramePosition(new Rectangle(111, 222, 100, 200));
        out.setMainFrameState(3);
        out.setShowFightAdvisor(true);
        out.setVolume(32);
        return out;
    }

}
