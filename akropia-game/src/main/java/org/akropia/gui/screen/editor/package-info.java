/**
 * Developer map and scenario editor (R-0435).
 * <p>
 * Only in developer mode. A top menu creates, opens, saves and plays the draft;
 * the right panel holds the nation, the terrain, forest, field, river, bonus,
 * native settlement and unit tools, the selected square with its removal
 * controls and the editor feedback. {@code EditorActions} owns the flows and
 * the Save / Discard / Cancel guard; the model's
 * {@code model.generation.ScenarioDraft} owns every rule and validation. The
 * draft is separate from the running game.
 * </p>
 */
package org.akropia.gui.screen.editor;
