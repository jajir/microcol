package org.akropia.gui.shortcut;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.akropia.gui.preferences.GamePreferences;
import org.akropia.gui.util.StreamReader;
import org.akropia.i18n.I18n;
import org.akropia.model.AkropiaException;
import org.junit.jupiter.api.Test;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Platform profiles, saved deviations and the transactional save, with the
 * platform injected so the result never depends on the test machine.
 */
public class ShortcutControllerTest {

    private static I18n i18n(final Locale locale) {
        return I18n.builder().setStreamReader(new StreamReader())
                .setVerifyThatAllEnumKeysAreDefined(true)
                .setVerifyThatAllKeysInResourceBundleHaveConstant(true)
                .setDefaultLocale(locale).build();
    }

    private static GamePreferences preferences(
            final Map<String, String> overrides) {
        final GamePreferences preferences = mock(GamePreferences.class);
        when(preferences.getShortcutOverrides(anyString()))
                .thenReturn(overrides);
        return preferences;
    }

    /**
     * @param platform injected platform
     * @return controller with packaged defaults and no saved deviations
     */
    public static ShortcutController controller(
            final ShortcutPlatform platform) {
        return new ShortcutController(preferences(Map.of()), i18n(Locale.ROOT),
                platform);
    }

    static KeyEvent key(final KeyCode code, final KeyBinding.Modifier... down) {
        final List<KeyBinding.Modifier> list = List.of(down);
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code,
                list.contains(KeyBinding.Modifier.SHIFT),
                list.contains(KeyBinding.Modifier.CTRL),
                list.contains(KeyBinding.Modifier.ALT),
                list.contains(KeyBinding.Modifier.META));
    }

    @Test
    void everyPlatformProfileNamesEveryActionOnceWithoutConflicts()
            throws Exception {
        for (final ShortcutPlatform platform : ShortcutPlatform.values()) {
            final Properties profile = new Properties();
            try (InputStream in = ShortcutController.class
                    .getResourceAsStream(platform.getResource())) {
                profile.load(in);
            }
            assertEquals(ShortcutAction.values().length,
                    profile.stringPropertyNames().size(), platform.getId());
            for (final ShortcutAction action : ShortcutAction.values()) {
                assertTrue(profile.containsKey(action.getKey()),
                        platform + " " + action);
            }
            final ShortcutConfiguration defaults = ShortcutController
                    .loadProfile(platform);
            assertTrue(defaults.findConflicts().isEmpty(), platform.getId());
            for (final ShortcutAction action : ShortcutAction.values()) {
                assertFalse(defaults.getBindings(action).isEmpty(),
                        platform + " gives " + action + " a default");
            }
        }
    }

    @Test
    void profilesPreserveExistingKeysAndUsePlatformModifiers() {
        for (final ShortcutPlatform platform : new ShortcutPlatform[] {
                ShortcutPlatform.WINDOWS, ShortcutPlatform.LINUX }) {
            final ShortcutConfiguration defaults = ShortcutController
                    .loadProfile(platform);
            assertEquals(List.of(KeyBinding.parse("Ctrl+O")),
                    defaults.getBindings(ShortcutAction.LOAD_GAME));
            assertEquals(List.of(KeyBinding.parse("Ctrl+S")),
                    defaults.getBindings(ShortcutAction.EDITOR_SAVE));
        }
        final ShortcutConfiguration mac = ShortcutController
                .loadProfile(ShortcutPlatform.MACOS);
        assertEquals(List.of(KeyBinding.parse("Meta+O")),
                mac.getBindings(ShortcutAction.LOAD_GAME));
        // The editor keeps its former Control key beside Command.
        assertEquals(
                List.of(KeyBinding.parse("Meta+S"), KeyBinding.parse("Ctrl+S")),
                mac.getBindings(ShortcutAction.EDITOR_SAVE));
        for (final ShortcutPlatform platform : ShortcutPlatform.values()) {
            final ShortcutConfiguration defaults = ShortcutController
                    .loadProfile(platform);
            final Object[][] kept = { { ShortcutAction.DEVELOPER_MODE, "F3" },
                    { ShortcutAction.QUICK_SAVE, "F5" },
                    { ShortcutAction.QUICK_LOAD, "F9" },
                    { ShortcutAction.NEXT_UNIT, "Tab" },
                    { ShortcutAction.SKIP_UNIT, "Space" },
                    { ShortcutAction.CONFIRM_MOVE, "Enter" },
                    { ShortcutAction.STATISTICS, "R" },
                    { ShortcutAction.CONGRESS, "K" },
                    { ShortcutAction.CONTINUE_GAME, "C" },
                    { ShortcutAction.CENTER_VIEW, "C" },
                    { ShortcutAction.SCROLL_UP, "W" } };
            for (final Object[] pair : kept) {
                assertEquals(List.of(KeyBinding.parse((String) pair[1])),
                        defaults.getBindings((ShortcutAction) pair[0]),
                        platform + " " + pair[0]);
            }
        }
    }

    @Test
    void consequentialCommandsNeedAModifierOnEveryPlatform() {
        // Session exits and irreversible unit orders cannot start from one
        // stray letter; each still opens its own confirmation.
        final ShortcutAction[] consequential = { ShortcutAction.QUIT_GAME,
                ShortcutAction.EXIT_TO_MAIN_MENU,
                ShortcutAction.RESTART_MISSION,
                ShortcutAction.DECLARE_INDEPENDENCE, ShortcutAction.RETIRE,
                ShortcutAction.DISBAND, ShortcutAction.JETTISON,
                ShortcutAction.END_TURN };
        for (final ShortcutPlatform platform : ShortcutPlatform.values()) {
            final ShortcutConfiguration defaults = ShortcutController
                    .loadProfile(platform);
            for (final ShortcutAction action : consequential) {
                for (final KeyBinding binding : defaults.getBindings(action)) {
                    assertTrue(binding.toConfigText().contains("+"),
                            platform + " " + action + " " + binding);
                }
            }
        }
        // Command+Q stays the macOS system quit, which uses the same flow.
        assertFalse(ShortcutController.loadProfile(ShortcutPlatform.MACOS)
                .getBindings(ShortcutAction.QUIT_GAME)
                .contains(KeyBinding.parse("Meta+Q")));
    }

    @Test
    void screenCommandsReuseLettersOnlyOnScreensNeverShownTogether() {
        final ShortcutConfiguration defaults = ShortcutController
                .loadProfile(ShortcutPlatform.LINUX);
        // R is Statistics on the map, Recruit in Europe and Trade routes in
        // a colony.
        final KeyEvent r = key(KeyCode.R);
        assertEquals(Optional.of(ShortcutAction.STATISTICS),
                defaults.find(r, ShortcutContext.MAP));
        assertEquals(Optional.of(ShortcutAction.EUROPE_RECRUIT),
                defaults.find(r, ShortcutContext.EUROPE));
        assertEquals(Optional.of(ShortcutAction.COLONY_TRADE_ROUTE),
                defaults.find(r, ShortcutContext.COLONY));
        assertFalse(ShortcutContext.EUROPE.overlaps(ShortcutContext.COLONY));
        assertFalse(ShortcutContext.EUROPE.overlaps(ShortcutContext.MAP));
        assertTrue(ShortcutContext.SESSION.overlaps(ShortcutContext.EUROPE));
        assertTrue(ShortcutContext.SESSION.overlaps(ShortcutContext.COLONY));
        // A session key cannot also be a colony key.
        assertEquals(1,
                defaults.with(ShortcutAction.COLONY_TREASURE,
                        List.of(KeyBinding.parse("Ctrl+Q"))).findConflicts()
                        .size());
    }

    @Test
    void runningSystemSelectsItsPlatform() {
        assertEquals(ShortcutPlatform.MACOS,
                ShortcutPlatform.fromOsName("Mac OS X"));
        assertEquals(ShortcutPlatform.WINDOWS,
                ShortcutPlatform.fromOsName("Windows 11"));
        assertEquals(ShortcutPlatform.LINUX,
                ShortcutPlatform.fromOsName("Linux"));
        assertEquals(ShortcutPlatform.LINUX,
                ShortcutPlatform.fromOsName("FreeBSD"));
        assertEquals(ShortcutPlatform.LINUX, ShortcutPlatform.fromOsName(null));
    }

    @Test
    void labelsFollowPlatformAndLanguage() {
        assertEquals("Ctrl+O", controller(ShortcutPlatform.LINUX)
                .label(ShortcutAction.LOAD_GAME));
        assertEquals("⌘O", controller(ShortcutPlatform.MACOS)
                .label(ShortcutAction.LOAD_GAME));
        assertEquals("Load game (Ctrl+O)", controller(ShortcutPlatform.WINDOWS)
                .withHint("Load game", ShortcutAction.LOAD_GAME));
        final ShortcutController czech = new ShortcutController(
                preferences(Map.of()), i18n(new Locale("cs", "CZ")),
                ShortcutPlatform.LINUX);
        assertEquals("Mezerník", czech.label(ShortcutAction.SKIP_UNIT));
        // Punctuation shows its symbol; the saved text keeps the key name.
        assertEquals("Ctrl+,", czech.label(ShortcutAction.OPEN_SETTINGS));
        assertEquals("\u2318,", controller(ShortcutPlatform.MACOS)
                .label(ShortcutAction.OPEN_SETTINGS));
        assertEquals("Ctrl+Comma",
                czech.getDefaults().getBindings(ShortcutAction.OPEN_SETTINGS)
                        .get(0).toConfigText());
        assertEquals("Shift+R", czech.label(ShortcutAction.BUILD_ROAD));
    }

    @Test
    void bindingsMatchExactModifiers() {
        final ShortcutController shortcuts = controller(ShortcutPlatform.LINUX);
        assertTrue(
                shortcuts.matches(ShortcutAction.STATISTICS, key(KeyCode.R)));
        assertFalse(shortcuts.matches(ShortcutAction.STATISTICS,
                key(KeyCode.R, KeyBinding.Modifier.SHIFT)));
        assertEquals(Optional.of(ShortcutAction.BUILD_ROAD),
                shortcuts.find(key(KeyCode.R, KeyBinding.Modifier.SHIFT),
                        ShortcutContext.MAP));
        assertEquals(Optional.empty(), shortcuts.find(
                key(KeyCode.O, KeyBinding.Modifier.CTRL), ShortcutContext.MAP));
    }

    @Test
    void savePersistsOnlyDeviationsThenActivatesAndNotifies() {
        final GamePreferences preferences = preferences(Map.of());
        final ShortcutController shortcuts = new ShortcutController(preferences,
                i18n(Locale.ROOT), ShortcutPlatform.LINUX);
        final AtomicInteger notified = new AtomicInteger();
        shortcuts.addListener(notified::incrementAndGet);
        final ShortcutConfiguration draft = shortcuts.getActive()
                .with(ShortcutAction.STATISTICS,
                        List.of(KeyBinding.parse("Y"),
                                KeyBinding.parse("Ctrl+Y")))
                .with(ShortcutAction.HELP, List.of());
        shortcuts.save(draft);
        verify(preferences).setShortcutOverrides("linux",
                Map.of("map.statistics", "Y, Ctrl+Y", "map.help", ""));
        assertEquals(1, notified.get());
        assertTrue(shortcuts.matches(ShortcutAction.STATISTICS,
                key(KeyCode.Y, KeyBinding.Modifier.CTRL)));
        assertFalse(
                shortcuts.matches(ShortcutAction.STATISTICS, key(KeyCode.R)));
        assertEquals("Y", shortcuts.label(ShortcutAction.STATISTICS));
        assertEquals("Colonizopedia",
                shortcuts.withHint("Colonizopedia", ShortcutAction.HELP));
        // Restoring the defaults saves no deviation.
        shortcuts.save(shortcuts.getDefaults());
        verify(preferences).setShortcutOverrides("linux", Map.of());
    }

    @Test
    void conflictingDraftIsRejectedWithoutAnyChange() {
        final GamePreferences preferences = preferences(Map.of());
        final ShortcutController shortcuts = new ShortcutController(preferences,
                i18n(Locale.ROOT), ShortcutPlatform.LINUX);
        final ShortcutConfiguration before = shortcuts.getActive();
        final AtomicInteger notified = new AtomicInteger();
        shortcuts.addListener(notified::incrementAndGet);
        final ShortcutConfiguration draft = before.with(
                ShortcutAction.STATISTICS, List.of(KeyBinding.parse("K")));
        assertEquals(1, draft.findConflicts().size());
        assertEquals(ShortcutAction.CONGRESS,
                draft.findConflicts().get(0).getFirst());
        assertEquals(ShortcutAction.STATISTICS,
                draft.findConflicts().get(0).getSecond());
        assertThrows(IllegalArgumentException.class,
                () -> shortcuts.save(draft));
        verify(preferences, never()).setShortcutOverrides(anyString(), any());
        assertSame(before, shortcuts.getActive());
        assertEquals(0, notified.get());
        // A global action overlaps every screen: C is Continue in the main
        // menu, Center on the map and Trade route cargo in a colony.
        assertEquals(
                3, before
                        .with(ShortcutAction.DEVELOPER_MODE,
                                List.of(KeyBinding.parse("C")))
                        .findConflicts().size());
        // The main menu and the map are never active together.
        assertTrue(before.findConflicts().isEmpty());
    }

    @Test
    void persistenceFailureKeepsThePreviousConfiguration() {
        final GamePreferences preferences = preferences(Map.of());
        doThrow(new AkropiaException("disk full")).when(preferences)
                .setShortcutOverrides(anyString(), any());
        final ShortcutController shortcuts = new ShortcutController(preferences,
                i18n(Locale.ROOT), ShortcutPlatform.LINUX);
        final ShortcutConfiguration before = shortcuts.getActive();
        final AtomicInteger notified = new AtomicInteger();
        shortcuts.addListener(notified::incrementAndGet);
        assertThrows(AkropiaException.class,
                () -> shortcuts.save(before.with(ShortcutAction.STATISTICS,
                        List.of(KeyBinding.parse("Y")))));
        assertSame(before, shortcuts.getActive());
        assertEquals("R", shortcuts.label(ShortcutAction.STATISTICS));
        assertEquals(0, notified.get());
    }

    @Test
    void savedDeviationsKeepExplicitUnboundAndFollowOtherDefaults() {
        final ShortcutController shortcuts = new ShortcutController(
                preferences(Map.of("map.help", "", "session.load", "Ctrl+L")),
                i18n(Locale.ROOT), ShortcutPlatform.WINDOWS);
        assertTrue(shortcuts.getActive().getBindings(ShortcutAction.HELP)
                .isEmpty());
        assertEquals("", shortcuts.label(ShortcutAction.HELP));
        assertEquals("Ctrl+L", shortcuts.label(ShortcutAction.LOAD_GAME));
        assertEquals("R", shortcuts.label(ShortcutAction.STATISTICS));
        assertEquals(Map.of("map.help", "", "session.load", "Ctrl+L"),
                shortcuts.getActive().deviationsFrom(shortcuts.getDefaults()));
    }

    @Test
    void explicitDeviationKeepsAComboLaterGivenToADefault() {
        // A saved K for Statistics meets the packaged K of Congress.
        final ShortcutController shortcuts = new ShortcutController(
                preferences(Map.of("map.statistics", "K")), i18n(Locale.ROOT),
                ShortcutPlatform.LINUX);
        assertTrue(shortcuts.getActive().findConflicts().isEmpty());
        assertEquals(Optional.of(ShortcutAction.STATISTICS),
                shortcuts.find(key(KeyCode.K), ShortcutContext.MAP));
        assertTrue(shortcuts.getActive().getBindings(ShortcutAction.CONGRESS)
                .isEmpty());
        // Between two saved deviations the earlier action keeps the key.
        final ShortcutController both = new ShortcutController(
                preferences(Map.of("map.statistics", "U", "map.goals", "U")),
                i18n(Locale.ROOT), ShortcutPlatform.LINUX);
        assertEquals(Optional.of(ShortcutAction.STATISTICS),
                both.find(key(KeyCode.U), ShortcutContext.MAP));
        assertTrue(
                both.getActive().getBindings(ShortcutAction.GOALS).isEmpty());
    }

    @Test
    void malformedDeviationsKeepTheirDefaults() {
        final ShortcutController shortcuts = new ShortcutController(preferences(
                Map.of("map.goals", "Ctrl+?", "map.europe", "Ctrl+Ctrl+E",
                        "unknown.action", "X", "map.help", "Escape")),
                i18n(Locale.ROOT), ShortcutPlatform.LINUX);
        assertEquals("O", shortcuts.label(ShortcutAction.GOALS));
        assertEquals("E", shortcuts.label(ShortcutAction.EUROPE));
        assertEquals("H", shortcuts.label(ShortcutAction.HELP));
        assertTrue(shortcuts.getActive().deviationsFrom(shortcuts.getDefaults())
                .isEmpty());
    }

}
