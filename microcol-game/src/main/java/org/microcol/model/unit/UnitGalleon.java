package org.microcol.model.unit;

import java.util.function.Function;

import org.microcol.model.Cargo;
import org.microcol.model.Model;
import org.microcol.model.Place;
import org.microcol.model.Player;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;

public final class UnitGalleon extends Unit {

    public UnitGalleon(Function<Unit, Cargo> cargoBuilder, Model model, Integer id,
	    Function<Unit, Place> placeBuilder, UnitType unitType, Player owner, int availableMoves,
	    final UnitAction unitAction) {
	super(cargoBuilder, model, id, placeBuilder, unitType, owner, availableMoves, unitAction);
    }

}
