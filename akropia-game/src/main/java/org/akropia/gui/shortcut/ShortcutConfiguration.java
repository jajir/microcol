package org.akropia.gui.shortcut;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;

import javafx.scene.input.KeyEvent;

/**
 * Immutable bindings of every {@link ShortcutAction}. An action may have one
 * binding or none.
 */
public final class ShortcutConfiguration {

    /**
     * Two actions active at the same time sharing one binding.
     */
    public static final class Conflict {
        private final ShortcutAction first;
        private final ShortcutAction second;
        private final KeyBinding binding;

        Conflict(final ShortcutAction first, final ShortcutAction second,
                final KeyBinding binding) {
            this.first = first;
            this.second = second;
            this.binding = binding;
        }

        public ShortcutAction getFirst() {
            return first;
        }

        public ShortcutAction getSecond() {
            return second;
        }

        public KeyBinding getBinding() {
            return binding;
        }

        @Override
        public String toString() {
            return binding + ": " + first.getKey() + ", " + second.getKey();
        }
    }

    private final Map<ShortcutAction, List<KeyBinding>> bindings;

    private ShortcutConfiguration(
            final Map<ShortcutAction, List<KeyBinding>> bindings) {
        final Map<ShortcutAction, List<KeyBinding>> copy = new EnumMap<>(
                ShortcutAction.class);
        for (final ShortcutAction action : ShortcutAction.values()) {
            Preconditions.checkArgument(
                    bindings.getOrDefault(action, Collections.emptyList())
                            .size() <= 1,
                    "Only one shortcut is allowed for '%s'", action.getKey());
            copy.put(action,
                    Collections.unmodifiableList(new ArrayList<>(
                            new LinkedHashSet<>(bindings.getOrDefault(action,
                                    Collections.emptyList())))));
        }
        this.bindings = Collections.unmodifiableMap(copy);
    }

    /**
     * Read a packaged default profile. The profile must name every action
     * exactly once, contain only valid bindings and have no conflicts.
     *
     * @param profile complete profile
     * @return default configuration
     * @throws IllegalArgumentException when the profile is incomplete or
     *                                  invalid
     */
    public static ShortcutConfiguration parseProfile(final Properties profile) {
        final Map<ShortcutAction, List<KeyBinding>> out = new EnumMap<>(
                ShortcutAction.class);
        for (final String key : profile.stringPropertyNames()) {
            Preconditions.checkArgument(ShortcutAction.byKey(key) != null,
                    "Unknown shortcut action '%s'", key);
        }
        for (final ShortcutAction action : ShortcutAction.values()) {
            final String value = profile.getProperty(action.getKey());
            Preconditions.checkArgument(value != null,
                    "Missing shortcut action '%s'", action.getKey());
            out.put(action, parseList(value));
        }
        final ShortcutConfiguration configuration = new ShortcutConfiguration(
                out);
        final List<Conflict> conflicts = configuration.findConflicts();
        Preconditions.checkArgument(conflicts.isEmpty(),
                "Conflicting default shortcuts %s", conflicts);
        return configuration;
    }

    /**
     * @param value comma separated bindings; empty means unbound
     * @return bindings in their written order
     * @throws IllegalArgumentException when any binding is invalid
     */
    public static List<KeyBinding> parseList(final String value) {
        Preconditions.checkNotNull(value);
        if (value.trim().isEmpty()) {
            return Collections.emptyList();
        }
        Preconditions.checkArgument(!value.contains(","),
                "Only one shortcut is allowed per action");
        return List.of(KeyBinding.parse(value.trim()));
    }

    static String formatList(final List<KeyBinding> list) {
        return list.stream().map(KeyBinding::toConfigText)
                .collect(Collectors.joining(", "));
    }

    /**
     * Apply the player's saved deviations to the defaults. Unknown actions and
     * unreadable values are reported and ignored, so the default remains. A
     * combination claimed by an explicit deviation is removed from an
     * overlapping action that still follows its default; between two deviations
     * the earlier action keeps it. The result never runs two actions for one
     * key.
     *
     * @param defaults  packaged defaults
     * @param overrides saved deviations by action key
     * @param problems  receives one message per ignored value or binding
     * @return effective configuration without conflicts
     */
    public static ShortcutConfiguration withOverrides(
            final ShortcutConfiguration defaults,
            final Map<String, String> overrides,
            final Consumer<String> problems) {
        final Map<ShortcutAction, List<KeyBinding>> wanted = new EnumMap<>(
                defaults.bindings);
        final Set<ShortcutAction> explicit = new LinkedHashSet<>();
        for (final Map.Entry<String, String> entry : overrides.entrySet()) {
            final ShortcutAction action = ShortcutAction.byKey(entry.getKey());
            if (action == null) {
                problems.accept("Ignoring unknown shortcut action '"
                        + entry.getKey() + "'");
                continue;
            }
            try {
                wanted.put(action, parseList(
                        Objects.requireNonNull(entry.getValue(), "value")));
                explicit.add(action);
            } catch (IllegalArgumentException | NullPointerException invalid) {
                problems.accept("Ignoring invalid shortcut '" + entry.getKey()
                        + "' = '" + entry.getValue() + "'");
            }
        }
        final List<ShortcutAction> order = new ArrayList<>();
        for (final ShortcutAction action : ShortcutAction.values()) {
            if (explicit.contains(action)) {
                order.add(action);
            }
        }
        for (final ShortcutAction action : ShortcutAction.values()) {
            if (!explicit.contains(action)) {
                order.add(action);
            }
        }
        final Map<ShortcutAction, List<KeyBinding>> out = new EnumMap<>(
                ShortcutAction.class);
        for (final ShortcutAction action : order) {
            final List<KeyBinding> kept = new ArrayList<>();
            for (final KeyBinding binding : wanted.get(action)) {
                final Optional<ShortcutAction> owner = findOwner(out, action,
                        binding);
                if (owner.isPresent()) {
                    problems.accept("Shortcut " + binding + " of '"
                            + action.getKey() + "' stays with '"
                            + owner.get().getKey() + "'");
                } else {
                    kept.add(binding);
                }
            }
            out.put(action, kept);
        }
        return new ShortcutConfiguration(out);
    }

    private static Optional<ShortcutAction> findOwner(
            final Map<ShortcutAction, List<KeyBinding>> map,
            final ShortcutAction action, final KeyBinding binding) {
        for (final Map.Entry<ShortcutAction, List<KeyBinding>> entry : map
                .entrySet()) {
            if (entry.getKey() != action
                    && entry.getKey().getContext().overlaps(action.getContext())
                    && entry.getValue().contains(binding)) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }

    /**
     * @param action required action
     * @return bindings in their order, possibly empty
     */
    public List<KeyBinding> getBindings(final ShortcutAction action) {
        return bindings.get(action);
    }

    /**
     * @param action      changed action
     * @param newBindings its new bindings
     * @return changed copy
     */
    public ShortcutConfiguration with(final ShortcutAction action,
            final List<KeyBinding> newBindings) {
        final Map<ShortcutAction, List<KeyBinding>> copy = new EnumMap<>(
                bindings);
        copy.put(action, newBindings);
        return new ShortcutConfiguration(copy);
    }

    /**
     * @param action  action receiving the binding
     * @param binding wanted binding
     * @return another action active at the same time that already uses it
     */
    public Optional<ShortcutAction> findOwner(final ShortcutAction action,
            final KeyBinding binding) {
        return findOwner(bindings, action, binding);
    }

    /**
     * @return all conflicts, each pair once, in action order
     */
    public List<Conflict> findConflicts() {
        final List<Conflict> out = new ArrayList<>();
        final ShortcutAction[] actions = ShortcutAction.values();
        for (int i = 0; i < actions.length; i++) {
            for (int j = i + 1; j < actions.length; j++) {
                if (!actions[i].getContext()
                        .overlaps(actions[j].getContext())) {
                    continue;
                }
                for (final KeyBinding binding : bindings.get(actions[i])) {
                    if (bindings.get(actions[j]).contains(binding)) {
                        out.add(new Conflict(actions[i], actions[j], binding));
                    }
                }
            }
        }
        return out;
    }

    /**
     * @param defaults platform defaults
     * @return action keys and written bindings that differ from the defaults;
     *         an empty value keeps the action explicitly unbound
     */
    public Map<String, String> deviationsFrom(
            final ShortcutConfiguration defaults) {
        final Map<String, String> out = new LinkedHashMap<>();
        for (final ShortcutAction action : ShortcutAction.values()) {
            if (!bindings.get(action).equals(defaults.bindings.get(action))) {
                out.put(action.getKey(), formatList(bindings.get(action)));
            }
        }
        return out;
    }

    /**
     * @param event   pressed or released key
     * @param context active context
     * @return the action of the exact binding in the context
     */
    public Optional<ShortcutAction> find(final KeyEvent event,
            final ShortcutContext context) {
        for (final ShortcutAction action : ShortcutAction.values()) {
            if (action.getContext() == context && matches(action, event)) {
                return Optional.of(action);
            }
        }
        return Optional.empty();
    }

    /**
     * @param action required action
     * @param event  key event
     * @return <code>true</code> when one binding matches exactly
     */
    public boolean matches(final ShortcutAction action, final KeyEvent event) {
        for (final KeyBinding binding : bindings.get(action)) {
            if (binding.matches(event)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof ShortcutConfiguration
                && bindings.equals(((ShortcutConfiguration) obj).bindings);
    }

    @Override
    public int hashCode() {
        return bindings.hashCode();
    }

}
