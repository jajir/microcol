package org.akropia.gui.shortcut;

import java.util.Locale;

/**
 * Platform with its own default shortcut profile and saved deviations.
 */
public enum ShortcutPlatform {

    WINDOWS("windows", ShortcutText.platformWindows),

    LINUX("linux", ShortcutText.platformLinux),

    MACOS("macos", ShortcutText.platformMacos);

    private final String id;
    private final ShortcutText label;

    ShortcutPlatform(final String id, final ShortcutText label) {
        this.id = id;
        this.label = label;
    }

    /**
     * @return stable identifier used in resources and saved settings
     */
    public String getId() {
        return id;
    }

    public ShortcutText getLabel() {
        return label;
    }

    /**
     * @return class path resource with the complete default profile
     */
    public String getResource() {
        return "/shortcuts-" + id + ".properties";
    }

    /**
     * @param osName value of the <code>os.name</code> system property
     * @return matching platform; unknown systems use the Linux profile
     */
    public static ShortcutPlatform fromOsName(final String osName) {
        final String name = osName == null ? ""
                : osName.toLowerCase(Locale.ROOT);
        if (name.startsWith("mac") || name.contains("os x")) {
            return MACOS;
        }
        if (name.startsWith("windows")) {
            return WINDOWS;
        }
        return LINUX;
    }

    public static ShortcutPlatform current() {
        return fromOsName(System.getProperty("os.name"));
    }

}
