/**
 * Shared map layers also serve the non-interactive menu demo through
 * DemoMapPainter, with explicitly supplied state and private map caches.
 *
 * Map interaction, selection, movement preview and animation. Wait cycles the
 * model's eligible units; Skip uses its shared validated command. Selection
 * changes cancel unconfirmed movement previews without executing them. The
 * trade route planner keeps only presentation state and draws the model's route
 * previews; its clicks select routes and never order a unit.
 */
package org.akropia.gui.screen.game.gamepanel;
