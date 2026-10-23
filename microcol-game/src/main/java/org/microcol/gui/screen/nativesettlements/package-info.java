/**
 * Native settlement details GUI: the keyboard-reachable native advisor opened
 * by the N shortcut. It reads the model's native advisor report, which gives
 * the known nations and each learned settlement with its attitude, alarm
 * change, visible provoker and the chief facts already learned. It never
 * changes a relation; its map control centers the settlement only while the
 * model still knows it. It also holds the chooser opened when a unit tries to
 * enter a known adjacent settlement: it shows the offered skill and the model's
 * limit for each visit action and calls no model operation, so closing it
 * changes nothing.
 */
package org.microcol.gui.screen.nativesettlements;
