package org.akropia.gui.shortcut;

import java.util.ArrayList;
import java.util.List;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.google.common.base.Preconditions;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * One key with an exact set of modifiers, written for example as
 * <code>Ctrl+Shift+O</code>. A binding matches an event only when exactly its
 * modifiers are down.
 */
public final class KeyBinding {

    /**
     * Modifier keys in their written order.
     */
    public enum Modifier {
        CTRL("Ctrl", "⌃"), ALT("Alt", "⌥"), SHIFT("Shift", "⇧"),
        META("Meta", "⌘");

        private final String text;
        private final String macSymbol;

        Modifier(final String text, final String macSymbol) {
            this.text = text;
            this.macSymbol = macSymbol;
        }
    }

    private final KeyCode code;

    private final Set<Modifier> modifiers;

    public KeyBinding(final KeyCode code, final Set<Modifier> modifiers) {
        this.code = Preconditions.checkNotNull(code);
        Preconditions.checkArgument(isBindable(code),
                "Key '%s' cannot be bound", code);
        this.modifiers = modifiers.isEmpty() ? EnumSet.noneOf(Modifier.class)
                : EnumSet.copyOf(modifiers);
    }

    public static KeyBinding of(final KeyCode code,
            final Modifier... modifiers) {
        final Set<Modifier> set = EnumSet.noneOf(Modifier.class);
        for (final Modifier modifier : modifiers) {
            set.add(modifier);
        }
        return new KeyBinding(code, set);
    }

    /**
     * Escape always cancels capture and closes dialogs; modifier keys alone are
     * not combinations.
     */
    static boolean isBindable(final KeyCode code) {
        return code != KeyCode.UNDEFINED && code != KeyCode.ESCAPE
                && !code.isModifierKey();
    }

    /**
     * @param text written binding such as <code>Ctrl+O</code>
     * @return parsed binding
     * @throws IllegalArgumentException when the text is not a binding
     */
    public static KeyBinding parse(final String text) {
        Preconditions.checkNotNull(text);
        final String[] parts = text.split("\\+", -1);
        final Set<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
        for (int i = 0; i < parts.length - 1; i++) {
            final Modifier modifier = modifier(parts[i].trim());
            Preconditions.checkArgument(modifiers.add(modifier),
                    "Repeated modifier in '%s'", text);
        }
        final KeyCode code = keyCode(parts[parts.length - 1].trim());
        Preconditions.checkArgument(code != null && isBindable(code),
                "Unknown key in '%s'", text);
        return new KeyBinding(code, modifiers);
    }

    private static Modifier modifier(final String name) {
        switch (name.toLowerCase(Locale.ROOT)) {
        case "ctrl":
        case "control":
            return Modifier.CTRL;
        case "alt":
        case "option":
            return Modifier.ALT;
        case "shift":
            return Modifier.SHIFT;
        case "meta":
        case "cmd":
        case "command":
            return Modifier.META;
        default:
            throw new IllegalArgumentException(
                    String.format("Unknown modifier '%s'", name));
        }
    }

    private static KeyCode keyCode(final String name) {
        if (name.isEmpty()) {
            return null;
        }
        final KeyCode named = KeyCode.getKeyCode(name);
        if (named != null) {
            return named;
        }
        try {
            return KeyCode
                    .valueOf(name.toUpperCase(Locale.ROOT).replace(' ', '_'));
        } catch (IllegalArgumentException unknown) {
            return null;
        }
    }

    /**
     * @param event pressed key
     * @return binding of the pressed key, empty for modifier keys alone
     */
    public static Optional<KeyBinding> fromEvent(final KeyEvent event) {
        if (!isBindable(event.getCode())) {
            return Optional.empty();
        }
        final Set<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
        if (event.isControlDown()) {
            modifiers.add(Modifier.CTRL);
        }
        if (event.isAltDown()) {
            modifiers.add(Modifier.ALT);
        }
        if (event.isShiftDown()) {
            modifiers.add(Modifier.SHIFT);
        }
        if (event.isMetaDown()) {
            modifiers.add(Modifier.META);
        }
        return Optional.of(new KeyBinding(event.getCode(), modifiers));
    }

    /**
     * @param event key event
     * @return <code>true</code> for the same key with exactly these modifiers
     */
    public boolean matches(final KeyEvent event) {
        return code == event.getCode()
                && event.isControlDown() == modifiers.contains(Modifier.CTRL)
                && event.isAltDown() == modifiers.contains(Modifier.ALT)
                && event.isShiftDown() == modifiers.contains(Modifier.SHIFT)
                && event.isMetaDown() == modifiers.contains(Modifier.META);
    }

    public KeyCode getCode() {
        return code;
    }

    /**
     * @return text accepted by {@link #parse(String)}
     */
    public String toConfigText() {
        final StringBuilder out = new StringBuilder();
        for (final Modifier modifier : modifiers) {
            out.append(modifier.text).append('+');
        }
        return out.append(code.getName()).toString();
    }

    /**
     * @param platform platform naming the modifiers
     * @param keyName  localized name of the key
     * @return text shown to the player, for example <code>Ctrl+O</code> or
     *         <code>⌘O</code>
     */
    public String toDisplayText(final ShortcutPlatform platform,
            final Function<KeyCode, String> keyName) {
        final StringBuilder out = new StringBuilder();
        for (final Modifier modifier : modifiers) {
            if (platform == ShortcutPlatform.MACOS) {
                out.append(modifier.macSymbol);
            } else {
                out.append(modifier.text).append('+');
            }
        }
        return out.append(keyName.apply(code)).toString();
    }

    /**
     * Separate keycap captions; punctuation is a key, never a separator.
     */
    public List<String> toDisplayKeys(final ShortcutPlatform platform,
            final Function<KeyCode, String> keyName) {
        final List<String> keys = new ArrayList<>();
        for (final Modifier modifier : modifiers) {
            if (platform == ShortcutPlatform.MACOS) {
                switch (modifier) {
                case CTRL:
                    keys.add("control");
                    break;
                case ALT:
                    keys.add("option");
                    break;
                case SHIFT:
                    keys.add("shift");
                    break;
                case META:
                    keys.add("command");
                    break;
                default:
                    throw new IllegalStateException();
                }
            } else {
                keys.add(modifier.text);
            }
        }
        keys.add(keyName.apply(code));
        return keys;
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof KeyBinding)) {
            return false;
        }
        final KeyBinding other = (KeyBinding) obj;
        return code == other.code && modifiers.equals(other.modifiers);
    }

    @Override
    public int hashCode() {
        return code.hashCode() * 31 + modifiers.hashCode();
    }

    @Override
    public String toString() {
        return toConfigText();
    }

}
