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
 * rival at a time. With several colonies each completed round trip parks its
 * carrier in that port and the next owner turn serves the colony after it in ID
 * order. Its objective is saved by the model and every step uses shared model
 * moves and Europe trade operations; human trade-route automation is separate.
 * RivalExpansionCycle runs at most one new-colony expansion at a time for a
 * computer rival, using a spare founder, a carrier that is not on an active
 * trade cycle (a parked carrier may be taken), a known site and the saved
 * reserves. A second colony needs one colony; a further colony needs every
 * colony to keep a nonnegative food balance, five turns of food in stock and
 * its tool target met. Its objective is saved by the model and every step uses
 * shared model operations. RivalLandMilitaryCycle owns the land military of a
 * computer rival: defenders for threatened colonies first, then offence at the
 * shared 60 percent quote. Its defense half runs before the supply and
 * expansion cycles and its offence half after them, so an owner turn follows
 * famine prevention (a first workforce pass moves residents to food while a
 * colony's balance is negative), immediate defense, ongoing transport,
 * supplies, export, then expansion; offence, naval patrols, newcomers and spare
 * lumber work come last. A new colony needs every colony's balance nonnegative
 * after that repair; a blocked journey switches to another known legal site,
 * retires when its site became illegal, and otherwise waits.
 * RivalNavalMilitaryCycle owns the spare armed ships of a computer rival: it
 * prefers visible unarmed enemy carriers, then armed targets at the same quote,
 * and patrols known sea within five squares of an owned port, heading for a
 * band square the played-turn number selects, when no target is visible; a
 * rival without a port only pursues. It never claims a reserved trade or
 * expansion carrier or the first-colony transport, even an armed or loaded one,
 * and every battle and patrol interception is the shared model's.
 * RivalPlayerFactory creates the robot of each European computer rival; the
 * default StandardRivalPlayerFactory always returns the real SimpleAiPlayer,
 * and only a functional test's Guice override may bind another one.
 *
 * @see <a href="https://jajir.github.io/microcol/architecture.html">Package
 *      responsibilities</a>
 */
package org.microcol.ai;
