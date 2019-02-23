package org.microcol.gui.screen.game.gamepanel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.microcol.model.Model;
import org.microcol.model.Unit;

/**
 * Helps work with all units in one turn.
 */
public final class TurnManager {

    private final List<Unit> units;

    private int pointer;

    TurnManager(final Model model) {
        units = new ArrayList<>(model.getCurrentPlayer().getUnits());
        pointer = 0;
    }

    /**
     * When user perform some action than this method check if focused unit have
     * some free action points.
     */
    public void tryNext() {
        if (getFocusedUnit().isPresent() && getFocusedUnit().get().getActionPoints() == 0) {
            pointer++;
            if (pointer >= units.size()) {
                pointer = 0;
            }
        }
    }

    private Optional<Unit> getFocusedUnit() {
        if (units.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(units.get(pointer));
    }

}
