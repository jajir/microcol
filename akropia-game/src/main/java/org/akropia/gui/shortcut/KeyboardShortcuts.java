package org.akropia.gui.shortcut;

import java.util.Optional;

import com.google.inject.ImplementedBy;

import javafx.scene.input.KeyEvent;

/**
 * Effective keyboard shortcuts shared by key handlers and displayed hints.
 * Handlers keep their own focus, dialog and availability guards.
 */
@ImplementedBy(ShortcutController.class)
public interface KeyboardShortcuts {

    /**
     * @param action required action
     * @param event  key event
     * @return <code>true</code> when the event is exactly one of its bindings
     */
    boolean matches(ShortcutAction action, KeyEvent event);

    /**
     * @param event   key event
     * @param context active context
     * @return action of the context bound exactly to the event
     */
    Optional<ShortcutAction> find(KeyEvent event, ShortcutContext context);

    /**
     * @param action required action
     * @return display text of its first binding, or empty when unbound
     */
    String label(ShortcutAction action);

    /**
     * @param text   action text
     * @param action action
     * @return the text followed by <code> (label)</code> when bound
     */
    String withHint(String text, ShortcutAction action);

    /**
     * @param listener called after a new configuration becomes active
     */
    void addListener(Runnable listener);

}
