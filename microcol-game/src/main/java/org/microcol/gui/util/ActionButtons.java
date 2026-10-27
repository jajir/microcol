package org.microcol.gui.util;

import java.util.Objects;
import org.microcol.gui.image.ButtonIcon;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.ToggleButton;

/** Native input and accessibility, with an explicit shared action icon. */
public final class ActionButtons {
    private ActionButtons() {
    }

    public static Button button(final ButtonIcon icon) {
        return button(icon, "");
    }

    public static Button button(final ButtonIcon icon, final String text) {
        return new Button(text, Objects.requireNonNull(icon).createView());
    }

    public static ToggleButton toggle(final ButtonIcon icon) {
        return toggle(icon, "");
    }

    public static ToggleButton toggle(final ButtonIcon icon,
            final String text) {
        return new ToggleButton(text,
                Objects.requireNonNull(icon).createView());
    }

    public static MenuButton menu(final ButtonIcon icon) {
        return menu(icon, "");
    }

    public static MenuButton menu(final ButtonIcon icon, final String text) {
        return new MenuButton(text, Objects.requireNonNull(icon).createView());
    }
}
