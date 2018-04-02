package org.microcol.model;

import org.microcol.model.store.PlaceColonyFieldPo;
import org.microcol.model.store.PlacePo;
import org.microcol.model.store.UnitPo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Unit is placed at field to work outside of colony.
 */
public class PlaceColonyField extends AbstractPlace {

    private final ColonyField colonyField;

    /**
     * What type of goods is produced on this field.
     */
    private GoodType producedGoodType;

    public PlaceColonyField(final Unit unit, final ColonyField colonyField,
            final GoodType producedGoodType) {
        super(unit);
        this.colonyField = Preconditions.checkNotNull(colonyField);
        // TODO call setProducedGoodType because of validations
        this.producedGoodType = producedGoodType;
    }

    @Override
    public String getName() {
        return "Construction";
    }

    @Override
    public void destroy() {
        colonyField.empty(true);
    }

    @Override
    public void destroySimple() {
        colonyField.empty(false);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(PlaceColonyField.class).add("unitId", getUnit().getId())
                .add("unitType", getUnit().getType()).add("colonyName", colonyField.getColonyName())
                .toString();
    }

    Colony getColony() {
        return colonyField.getColony();
    }

    public GoodType getProducedGoodType() {
        return producedGoodType;
    }

    public void setProducedGoodType(final GoodType producedGoodType) {
        Preconditions.checkNotNull(producedGoodType);
        Preconditions.checkArgument(colonyField.canProduce(producedGoodType),
                "Given good (%s) can't be produced at (%s)", producedGoodType.name(),
                colonyField.getTerrainType().name());

        this.producedGoodType = producedGoodType;
    }

    @Override
    public PlacePo save(final UnitPo unitPo) {
        final PlaceColonyFieldPo placeColonyFieldPo = new PlaceColonyFieldPo();
        unitPo.setPlaceColonyField(placeColonyFieldPo);
        return placeColonyFieldPo;
    }

}
