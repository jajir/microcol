package org.microcol.model;

import java.util.HashSet;
import java.util.Set;

import org.microcol.model.store.CargoSlotPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.UnitPo;

/**
 * Saved games in which a nation has declared independence (R-0187). A declared
 * nation's units in the home port, on its pier or aboard a port ship are
 * Crown-seized, exactly as the real declaration leaves them; the strict loader
 * rejects any other combination. A fixture that sets the declared flag or moves
 * a unit into or out of the port in saved data calls this afterwards, so its
 * authored state is coherent.
 */
public final class ClosedEuropeFixtures {

    private ClosedEuropeFixtures() {
    }

    /**
     * Set the seizure of every unit from its saved place and owner: seized when
     * a declared nation's unit stands in home Europe, otherwise not seized.
     *
     * @param model required saved game, changed in place
     * @return the same saved game
     */
    public static ModelPo seizeHomeEurope(final ModelPo model) {
        final Set<String> declared = new HashSet<>();
        model.getGameManager().getPlayers().stream()
                .filter(player -> player.isDeclaredIndependence())
                .forEach(player -> declared.add(player.getName()));
        final Set<Integer> aboard = new HashSet<>();
        for (final UnitPo unit : model.getUnits()) {
            if (unit.getPlaceEuropePort() != null && unit.getType().isShip()
                    && unit.getCargo() != null) {
                for (final CargoSlotPo slot : unit.getCargo().getSlots()) {
                    if (slot.getUnitId() != null) {
                        aboard.add(slot.getUnitId());
                    }
                }
            }
        }
        for (final UnitPo unit : model.getUnits()) {
            final boolean inPort = unit.getPlaceEuropePort() != null
                    || aboard.contains(unit.getId());
            unit.setCrownSeized(inPort && declared.contains(unit.getOwnerId()));
        }
        return model;
    }
}
