package org.akropia.gui.shortcut;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

import org.akropia.gui.preferences.GamePreferences;
import org.akropia.i18n.I18n;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Owns the one effective shortcut configuration: the running platform's
 * packaged defaults with the player's saved deviations. Saving persists first
 * and only then activates the new bindings and notifies listeners.
 */
@Singleton
public class ShortcutController implements KeyboardShortcuts {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ShortcutController.class);

    private final GamePreferences preferences;

    private final ShortcutPlatform platform;

    private final I18n i18n;

    private final ShortcutConfiguration defaults;

    private final List<Runnable> listeners = new CopyOnWriteArrayList<>();

    private ShortcutConfiguration active;

    @Inject
    ShortcutController(final GamePreferences preferences, final I18n i18n) {
        this(preferences, i18n, ShortcutPlatform.current());
    }

    /**
     * @param preferences saved deviations
     * @param i18n        localized key names
     * @param platform    platform selecting the default profile
     */
    public ShortcutController(final GamePreferences preferences,
            final I18n i18n, final ShortcutPlatform platform) {
        this.preferences = Preconditions.checkNotNull(preferences);
        this.i18n = Preconditions.checkNotNull(i18n);
        this.platform = Preconditions.checkNotNull(platform);
        defaults = loadProfile(platform);
        active = ShortcutConfiguration.withOverrides(defaults,
                preferences.getShortcutOverrides(platform.getId()),
                LOGGER::warn);
    }

    /**
     * @param platform required platform
     * @return its complete packaged default profile
     * @throws IllegalStateException when the profile is missing or invalid
     */
    public static ShortcutConfiguration loadProfile(
            final ShortcutPlatform platform) {
        try (InputStream in = ShortcutController.class
                .getResourceAsStream(platform.getResource())) {
            Preconditions.checkState(in != null, "Missing shortcut profile %s",
                    platform.getResource());
            final Properties profile = new Properties();
            profile.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            return ShortcutConfiguration.parseProfile(profile);
        } catch (IOException | IllegalArgumentException e) {
            throw new IllegalStateException(
                    "Invalid shortcut profile " + platform.getResource(), e);
        }
    }

    public ShortcutPlatform getPlatform() {
        return platform;
    }

    public ShortcutConfiguration getDefaults() {
        return defaults;
    }

    public ShortcutConfiguration getActive() {
        return active;
    }

    /**
     * Persist the deviations of the configuration and activate it.
     *
     * @param configuration edited configuration
     * @throws IllegalArgumentException when actions active together share a
     *                                  binding; nothing changes
     * @throws RuntimeException         when the setting cannot be saved; the
     *                                  previous configuration stays active
     */
    public void save(final ShortcutConfiguration configuration) {
        Preconditions.checkArgument(configuration.findConflicts().isEmpty(),
                "Conflicting shortcuts %s", configuration.findConflicts());
        preferences.setShortcutOverrides(platform.getId(),
                configuration.deviationsFrom(defaults));
        active = configuration;
        listeners.forEach(Runnable::run);
    }

    @Override
    public boolean matches(final ShortcutAction action, final KeyEvent event) {
        return active.matches(action, event);
    }

    @Override
    public Optional<ShortcutAction> find(final KeyEvent event,
            final ShortcutContext context) {
        return active.find(event, context);
    }

    @Override
    public String label(final ShortcutAction action) {
        final List<KeyBinding> bindings = active.getBindings(action);
        return bindings.isEmpty() ? "" : displayText(bindings.get(0));
    }

    @Override
    public String withHint(final String text, final ShortcutAction action) {
        final String label = label(action);
        return label.isEmpty() ? text : text + " (" + label + ")";
    }

    /**
     * @param binding required binding
     * @return text shown to the player on this platform
     */
    public String displayText(final KeyBinding binding) {
        return binding.toDisplayText(platform, this::keyName);
    }

    public List<String> displayKeys(final KeyBinding binding) {
        return binding.toDisplayKeys(platform, this::keyName);
    }

    private String keyName(final KeyCode code) {
        switch (code) {
        case PLUS:
            return "+";
        case SPACE:
            return i18n.get(ShortcutText.keySpace);
        case ENTER:
            return i18n.get(ShortcutText.keyEnter);
        case TAB:
            return i18n.get(ShortcutText.keyTab);
        case BACK_SPACE:
            return i18n.get(ShortcutText.keyBackSpace);
        case DELETE:
            return i18n.get(ShortcutText.keyDelete);
        default:
            return punctuation(code).orElse(code.getName());
        }
    }

    /**
     * Punctuation keys show their symbol, for example <code>,</code> for Comma;
     * saved bindings keep the key name.
     */
    static Optional<String> punctuation(final KeyCode code) {
        final String text = code.getChar();
        if (text.length() == 1) {
            final char symbol = text.charAt(0);
            if (symbol > ' ' && symbol < 0x7f
                    && !Character.isLetterOrDigit(symbol)) {
                return Optional.of(text);
            }
        }
        return Optional.empty();
    }

    @Override
    public void addListener(final Runnable listener) {
        listeners.add(Preconditions.checkNotNull(listener));
    }

}
