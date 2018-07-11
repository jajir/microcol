package org.microcol.model;

import org.microcol.model.store.ModelPo;
import org.microcol.model.store.UnitPo;

import com.google.common.base.Preconditions;

final class PlaceBuilderContext {

    private final Unit unit;
    private final UnitPo unitPo;
    private final ModelPo modelPo;
    private final Model model;

    PlaceBuilderContext(final Unit unit, final UnitPo unitPo, final ModelPo modelPo,
            final Model model) {
        this.unit = Preconditions.checkNotNull(unit);
        this.unitPo = Preconditions.checkNotNull(unitPo);
        this.modelPo = Preconditions.checkNotNull(modelPo);
        this.model = Preconditions.checkNotNull(model);
    }

    /**
     * @return the unit
     */
    public Unit getUnit() {
        return unit;
    }

    /**
     * @return the unitPo
     */
    public UnitPo getUnitPo() {
        return unitPo;
    }

    /**
     * @return the modelPo
     */
    public ModelPo getModelPo() {
        return modelPo;
    }

    /**
     * @return the model
     */
    public Model getModel() {
        return model;
    }
}