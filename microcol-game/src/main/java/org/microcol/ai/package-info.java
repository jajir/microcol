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
 * takes a native nation's turn callback through the same robot lifecycle and
 * ends an actionless turn at once; raid and patrol policy are later
 * requirements, and native nations never use the European SimpleAiPlayer.
 * RivalTradeCycle runs one bounded colony-to-Europe supply cycle per computer
 * colony. Its objective is saved by the model and every step uses shared model
 * moves and Europe trade operations; human trade-route automation is separate.
 * RivalPlayerFactory creates the robot of each European computer rival; the
 * default StandardRivalPlayerFactory always returns the real SimpleAiPlayer,
 * and only a functional test's Guice override may bind another one.
 *
 * @see <a href="https://jajir.github.io/microcol/architecture.html">Package
 *      responsibilities</a>
 */
package org.microcol.ai;
