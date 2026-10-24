/**
 * Show dialog containing all turn events. At the start of a fresh human turn
 * the report opens when the routine summary is on or a saved notice is
 * critical; an automatic Europe opening waits behind it (EuropeOpeningRequest)
 * and never replaces it. Colony and Europe actions only navigate: they check
 * that the report still belongs to the current game and spend nothing.
 *
 */
package org.microcol.gui.screen.turnreport;
