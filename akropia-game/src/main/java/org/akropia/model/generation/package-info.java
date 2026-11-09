/**
 * Generated New World: validated player settings (seed, climate, land share),
 * deterministic terrain, rivers and resources, and the native nations placed in
 * the same schema as bundled worlds. The authored North America world reuses
 * the same assembly with geography, rivers and settlement squares read from its
 * editable source. {@code ScenarioDraft} holds an authored scenario of the
 * developer map editor (R-0435): bounded ocean creation, atomic edits under the
 * same geography, settlement and unit rules, structural checks for drafts and a
 * separate playability check for launch. It assembles unsaved model state only;
 * the caller replaces the active game after the candidate is complete. The GUI
 * gathers choices and shows errors without generating geography itself.
 *
 * @see <a href="https://jajir.github.io/microcol/architecture.html">Package
 *      responsibilities</a>
 */
package org.akropia.model.generation;
