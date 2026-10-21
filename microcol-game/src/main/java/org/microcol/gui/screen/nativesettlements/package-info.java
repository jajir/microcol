/**
 * Native settlement details GUI: the keyboard-reachable list of learned
 * settlements opened by the N shortcut. It reads the model view of each
 * settlement, including its attitude, alarm change and visible provoker, and
 * never changes a relation. It also holds the chooser opened when a unit tries
 * to enter a known adjacent settlement: it shows the offered skill and the
 * model's limit for each visit action and calls no model operation, so closing
 * it changes nothing.
 */
package org.microcol.gui.screen.nativesettlements;
