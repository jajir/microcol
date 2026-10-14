/**
 * Decisions and action sequencing for computer and royal players, including
 * continent target selection. Shared continent topology belongs to model. Use
 * shared model operations instead of duplicating game rules. Existing coupling
 * to GUI animations is a documented legacy exception; new strategies should not
 * add GUI dependencies. FirstColonyPlanner searches only known reachable cells
 * and ranks viable coastal sites; SimpleAiPlayer performs ordinary movement,
 * passenger landing and founding. The objective is reconstructed from live
 * units/knowledge and ends when the rival owns a colony. Stopped robots reject
 * queued turns and further actions after an animation wait.
 *
 * @see <a href="https://jajir.github.io/microcol/architecture.html">Package
 *      responsibilities</a>
 */
package org.microcol.ai;
