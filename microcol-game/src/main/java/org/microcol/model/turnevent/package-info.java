/**
 * This events are generated after each turn for some players. Turn events try
 * to drag player's attention to important events. Example of turn event's could
 * be:
 * <ul>
 * <li>Ship come to European port</li>
 * <li>Ship come from Europe to map</li>
 * <li>Colony was lost</li>
 * <li>Colonist died because of famine</li>
 * </ul>
 * <p>
 * Turn events should be stored into persistent model because turn events have
 * state and can't be reproduced when player perform some move.
 * </p>
 *
 */
package org.microcol.model.turnevent;
