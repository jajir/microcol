package org.microcol.gui.preferences;

import java.awt.Rectangle;
import java.io.File;
import java.util.Locale;

import com.google.common.io.Files;

public abstract class AbstractPreferencesTest {

    protected final static File SETTING_FILE = new File(Files.createTempDir(), "setting.txt");

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
