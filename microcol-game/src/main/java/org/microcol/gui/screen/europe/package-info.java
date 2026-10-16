/**
 * European port, recruitment, market and voyage presentation. Pier equipment
 * choices display model-owned quotes and rejection reasons; the model validates
 * and executes every confirmed trade. Voyage presentation reports model turn
 * counts and blocked arrivals; it does not choose return locations or advance
 * the crossing. Course reversal displays a model-owned offer and confirms it
 * through the model command; canceled or stale offers do not mutate the route.
 * The repair summary reads model destinations and remaining owner updates; it
 * never advances repairs or exposes damaged ships for cargo manipulation.
 */
package org.microcol.gui.screen.europe;
