package org.akropia.model.unit;

import java.util.function.Function;

import org.akropia.model.Cargo;
import org.akropia.model.Model;
import org.akropia.model.Place;
import org.akropia.model.Player;
import org.akropia.model.Unit;
import org.akropia.model.UnitType;

public final class UnitMerchantman extends Ship {

    public UnitMerchantman(Function<Unit, Cargo> cargoBuilder, Model model,
            Integer id, Function<Unit, Place> placeBuilder, Player owner,
            int availableMoves, final UnitAction unitAction) {
        super(cargoBuilder, model, id, placeBuilder, owner, availableMoves,
                unitAction);
    }

    @Override
    public UnitType getType() {
        return UnitType.MERCHANTMAN;
    }

}
