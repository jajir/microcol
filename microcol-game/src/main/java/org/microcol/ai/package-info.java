/**
 * Decisions and action sequencing for computer and royal players, including
 * continent target selection. Shared continent topology belongs to model. Use
 * shared model operations instead of duplicating game rules. Existing coupling
 * to GUI animations is a documented legacy exception; new strategies should not
 * add GUI dependencies. FirstColonyPlanner searches only known reachable cells
 * and ranks viable coastal sites; SimpleAiPlayer performs ordinary movement,
 * passenger landing and founding. The objective is reconstructed from live
 * units/knowledge and ends when the rival owns a colony. Stopped robots reject
 * queued turns and further actions after an animation wait. NativeRobotPlayer
 * takes a native nation's turn callback through the same robot lifecycle. A
 * brave of a hostile nation attacks the nearest known hostile colonial enemy
 * within two squares, otherwise returns toward and patrols its home; calmer
 * braves never raid but take the same home patrol. A brave of a tribe with a
 * paid incitement order (R-0166) first pursues the nearest known unit of the
 * selected European at any distance, nearest by the cost of the shared legal
 * route: it walks the part of that route its movement pays each turn and
 * attacks only when beside the target with movement left. It falls back to the
 * ordinary raid only when no known unit has a legal route. Model operations
 * validate every move and attack; native nations never use SimpleAiPlayer.
 * RivalTradeCycle runs one bounded colony-to-Europe supply cycle per computer
 * colony. Its objective is saved by the model and every step uses shared model
 * moves and Europe trade operations; human trade-route automation is separate.
 * RivalExpansionCycle runs at most one second-colony expansion for a computer
 * colony that has exactly one colony, using a spare founder, a carrier that is
 * not on a trade cycle, a known site and the saved reserves; its objective is
 * saved by the model and every step uses shared model operations.
 * RivalPlayerFactory creates the robot of each European computer rival; the
 * default StandardRivalPlayerFactory always returns the real SimpleAiPlayer,
 * and only a functional test's Guice override may bind another one.
 *
 * @see <a href="https://jajir.github.io/microcol/architecture.html">Package
 *      responsibilities</a>
 */
package org.microcol.ai;
