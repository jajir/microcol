package org.microcol.ai;

import java.util.List;

import org.microcol.model.Location;
import org.microcol.model.Unit;

public class SimpleUnitBehavior {
	
	public void tryToFight(final Unit unit){
		if (unit.getType().canAttack() && unit.getAvailableMoves() > 0 && unit.isAtPlaceLocation()) {
			for (final Location location : unit.getLocation().getNeighbors()) {
				if(unit.isPossibleToAttackAt(location)){
					final List<Unit> enemies = unit.getOwner().getEnemyUnitsAt(location);
					if (!enemies.isEmpty()) {
						unit.attack(enemies.get(0).getLocation());
						break;
					}
				}
			}
		}		
	}
	
}
