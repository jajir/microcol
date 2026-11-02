package org.akropia.model.unit;

import org.akropia.model.AbstractUnit;
import org.akropia.model.Model;
import org.akropia.model.store.UnitActionFortifyPo;
import org.akropia.model.store.UnitActionPo;

/** A fortifying order that becomes effective on its next owner update. */
public final class UnitActionFortify implements UnitAction {
    private boolean fortified;

    public UnitActionFortify(final boolean fortified) {
        this.fortified = fortified;
    }

    @Override
    public UnitActionType getType() {
        return UnitActionType.fortify;
    }

    public boolean isFortified() {
        return fortified;
    }

    @Override
    public boolean startTurn(final Model model, final AbstractUnit unit) {
        fortified = true;
        return false;
    }

    @Override
    public UnitActionPo save() {
        final UnitActionFortifyPo out = new UnitActionFortifyPo();
        out.setFortified(fortified);
        return out;
    }
}
