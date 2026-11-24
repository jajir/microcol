package org.akropia.gui;

import java.util.Optional;
import org.akropia.i18n.MessageKeyResource;
import org.akropia.model.ColonyJobChoice;
import org.akropia.model.ColonyField;
import org.akropia.model.RiverSize;
import org.akropia.model.Terrain;

/** Food sources share one stock; the source only selects its presentation. */
public enum FoodText implements MessageKeyResource {
    stock, farming, fishing;

    public static FoodText source(final Terrain terrain) {
        return terrain.getTerrainType().isSea()
                || terrain.getRiverSize() != RiverSize.NONE ? fishing : farming;
    }

    public static Optional<Terrain> terrain(final ColonyJobChoice choice) {
        return choice.getTargetLocation()
                .flatMap(location -> choice.getColony().getColonyFields()
                        .stream()
                        .filter(field -> field.isValid()
                                && field.getLocation().equals(location))
                        .map(ColonyField::getTerrain).findFirst());
    }
}
