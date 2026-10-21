/**
 * Colony GUI, including draft-only naming dialogs for founding and renaming.
 * Custom-house controls confirm draft commodity selections; the model owns
 * export timing, prices and inventory accounting. Equipment choices show
 * model-provided costs and rejection reasons, and revalidate after modal input.
 * Education controls display model-approved assignments and saved progress; the
 * model owns staffing limits, graduation and productive-work learning. Model
 * operations validate identity, names, permissions and complete inventory
 * changes before committing. The trade route cargo dialog also assigns,
 * releases and edits routes: it lists every carrier that follows a route,
 * wherever it is, replaces a stop, and deletes a route after a second press
 * that names the carriers it releases; the model validates each operation, and
 * closing the dialog changes nothing.
 */
package org.microcol.gui.screen.colony;
