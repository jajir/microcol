package org.microcol.model.unit;

import com.google.common.base.Preconditions;

/**
 * Enumeration represents actions that could unit perform. Actions are unit
 * states usually lasting more than one turn.
 */
public enum UnitActionType {

    noAction("") {
        @Override
        public UnitActionNoAction make() {
            return new UnitActionNoAction();
        }
    },

    plowField("p") {
        @Override
        public UnitActionPlowField make() {
            return new UnitActionPlowField();
        }

    };

    private final String sign;

    /**
     * Private constructor. Just set fields.
     *
     * @param sign
     *            required sign string. That string will be shown at frontend.
     */
    UnitActionType(final String sign) {
        this.sign = Preconditions.checkNotNull(sign);
    }

    /**
     * Interface method that all unit action types have to implement.
     *
     * @return newly created unit action instance.
     */
    public abstract UnitAction make();

    /**
     * Get sign character. This character should be shown along unit. It helps
     * player to understand what is unit actually doing.
     *
     * @return Return sign character
     */
    public String getSign() {
        return sign;
    }

}
