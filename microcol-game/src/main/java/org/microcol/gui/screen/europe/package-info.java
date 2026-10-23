/**
 * European port, recruitment, market and voyage presentation. Pier equipment
 * choices display model-owned quotes and rejection reasons; the model validates
 * and executes every confirmed trade. Voyage presentation reports model turn
 * counts and blocked arrivals; it does not choose return locations or advance
 * the crossing. Course reversal displays a model-owned offer and confirms it
 * through the model command; canceled or stale offers do not mutate the route.
 * The repair summary reads model destinations and remaining owner updates; it
 * never advances repairs or exposes damaged ships for cargo manipulation.
 * Brewster earned arrivals reuse the recruitment dialog with frozen model
 * offers; deferring preserves the pending choice and disables ordinary port
 * actions until the model completes turn preparation. TaxDemandDialog likewise
 * displays model-owned acceptance/protest consequences, preserves deferred
 * decisions and exposes reopening through Europe; trade controls read the model
 * boycott restrictions. MercenaryOfferDialog presents a model-owned paid
 * veteran offer (issuer, price, owned port choice, blockade hint) on the screen
 * the player is on, never forcing Europe, which a rebel nation cannot use; the
 * model validates and pays every confirmed answer, and closing keeps the offer.
 */
package org.microcol.gui.screen.europe;
