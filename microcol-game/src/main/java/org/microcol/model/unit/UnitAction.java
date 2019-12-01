package org.microcol.model.unit;

import org.microcol.model.AbstractUnit;
import org.microcol.model.Model;
import org.microcol.model.store.UnitActionNoActionPo;
import org.microcol.model.store.UnitActionPo;

/**
 * Unit could be send to perform some activity that is longer than one turn. This
 * state will be stored in untit's activity. There are actions:
 * <ul>
 * <li>Fortify</li>
 * <li>Wait one turn</li>
 * <li>Plow field</li>
 * <li>Build road - unit build road</li>
 * <li>Sentry - Unit actively look for opponents. It looks within some limited
 * range.</li>
 * <li>Explore - unit will try to find new undiscovered areas</li>
 * </ul>
 */
public interface UnitAction {

    /**
     * Verify that unit doesn't execute any action.
     *
     * @return Return <code>true</code> when unit is waiting for user's command
     *         otherwise unit is executing action and <code>false</code> is
     *         returned.
     */
    default boolean isNoAction() {
        return false;
    }

    /**
     * Persist action state to persistent object.
     *
     * @return Return unit action persistent object.
     */
    default UnitActionPo save() {
        return new UnitActionNoActionPo();
    }

    /**
     * Get unit action type.
     *
     * @return Return unit action type.
     */
    UnitActionType getType();

    /**
     * This should be called when player start turn.
     *
     * @param model
     *            required model
     * @param unit
     *            required unit
     */
    default void startTurn(final Model model, final AbstractUnit unit) {
        // by default it do nothing.
    }

}
