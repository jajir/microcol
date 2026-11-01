/**
 * Main application screen container, event-based navigation and screen
 * lifecycle. Application shutdown hides the current screen so its animation
 * loop stops. Settings and Load remember the screen that opened them, choose
 * their menu or in-game host from it and return there; the main window's
 * shortcut+O/S keys are handled here as well.
 */
package org.microcol.gui.screen;
