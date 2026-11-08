package org.akropia.gui.shortcut;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import javafx.scene.input.KeyCode;

class KeyBindingTest {

    @Test
    void writtenBindingsRoundTripInModifierOrder() {
        assertEquals("Ctrl+Shift+O",
                KeyBinding.parse("shift + ctrl+O").toConfigText());
        assertEquals("Meta+Enter",
                KeyBinding.parse("Cmd+Enter").toConfigText());
        assertEquals(KeyCode.PAGE_UP, KeyBinding.parse("Page Up").getCode());
        assertEquals(KeyCode.PAGE_UP, KeyBinding.parse("PAGE_UP").getCode());
        assertEquals("⌃⇧O", KeyBinding.parse("Ctrl+Shift+O")
                .toDisplayText(ShortcutPlatform.MACOS, KeyCode::getName));
        assertEquals("Ctrl+Shift+O", KeyBinding.parse("Ctrl+Shift+O")
                .toDisplayText(ShortcutPlatform.WINDOWS, KeyCode::getName));
    }

    @Test
    void invalidBindingsAreRejected() {
        for (final String invalid : new String[] { "", "Ctrl+", "Foo",
                "Ctrl+Ctrl+A", "Hyper+A", "Escape", "Shift", "Ctrl+Alt" }) {
            assertThrows(IllegalArgumentException.class,
                    () -> KeyBinding.parse(invalid), invalid);
        }
    }

    @Test
    void listsAllowSeveralBindingsOrNone() {
        assertEquals(List.of(), ShortcutConfiguration.parseList(" "));
        assertEquals(List.of(KeyBinding.parse("R"), KeyBinding.parse("Ctrl+R")),
                ShortcutConfiguration.parseList("R, Ctrl+R"));
        assertThrows(IllegalArgumentException.class,
                () -> ShortcutConfiguration.parseList("R,"));
    }

    @Test
    void incompleteOrConflictingProfilesAreRejected() {
        final Properties profile = new Properties();
        for (final ShortcutAction action : ShortcutAction.values()) {
            profile.setProperty(action.getKey(), "");
        }
        assertTrue(ShortcutConfiguration.parseProfile(profile).findConflicts()
                .isEmpty());
        profile.setProperty("map.statistics", "K");
        profile.setProperty("map.congress", "K");
        assertThrows(IllegalArgumentException.class,
                () -> ShortcutConfiguration.parseProfile(profile));
        profile.setProperty("map.congress", "");
        profile.setProperty("map.unknown", "U");
        assertThrows(IllegalArgumentException.class,
                () -> ShortcutConfiguration.parseProfile(profile));
        profile.remove("map.unknown");
        profile.remove("map.help");
        assertThrows(IllegalArgumentException.class,
                () -> ShortcutConfiguration.parseProfile(profile));
    }

    @Test
    void contextsOverlapOnlyWhenActiveTogether() {
        assertTrue(ShortcutContext.GLOBAL.overlaps(ShortcutContext.EDITOR));
        assertTrue(ShortcutContext.SESSION.overlaps(ShortcutContext.MAP));
        assertTrue(ShortcutContext.SESSION.overlaps(ShortcutContext.MAIN_MENU));
        assertFalse(ShortcutContext.SESSION.overlaps(ShortcutContext.EDITOR));
        assertFalse(ShortcutContext.MAP.overlaps(ShortcutContext.MAIN_MENU));
    }

}
