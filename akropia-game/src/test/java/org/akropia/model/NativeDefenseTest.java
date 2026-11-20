package org.akropia.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.akropia.model.store.ModelPo;
import org.akropia.model.unit.UnitNativeBrave;

class NativeDefenseTest {
    private static Model scene(final int targetOffset, final boolean missionary,
            final boolean allied, final boolean war) {
        final ModelPo saved = NativeTurnScene.island()
                .nativePlayer(NativeNation.AZTEC)
                .settlement(0, NativeNation.AZTEC, true, -4, -4)
                .brave(NativeNation.AZTEC, 0, 0, 0)
                .dutchColonist(targetOffset, targetOffset)
                .favour(NativeNation.AZTEC, EuropeanNation.DUTCH,
                        allied ? 5 : 2)
                .po();
        saved.getUnits().stream()
                .filter(unit -> "Dutch".equals(unit.getOwnerId())
                        && unit.getPlaceMap() != null
                        && unit.getPlaceMap().getLocation().equals(
                                NativeTurnScene.at(targetOffset, targetOffset)))
                .forEach(unit -> unit.setMissionary(missionary));
        if (allied) {
            saved.getNativeFriendships().stream()
                    .filter(row -> row.getNation() == NativeNation.AZTEC)
                    .forEach(row -> {
                        row.getFavour().put(EuropeanNation.FRENCH, 8);
                        row.setFriend(EuropeanNation.FRENCH);
                    });
        }
        saved.getEuropeanRelations().forEach(row -> row.setState(
                war ? EuropeanRelationState.WAR : EuropeanRelationState.PEACE));
        return NativeTurnScene.make(saved);
    }

    private static boolean canAttack(final Model model,
            final int targetOffset) {
        final Unit brave = model.getAllUnits().stream()
                .filter(unit -> unit instanceof UnitNativeBrave).findFirst()
                .orElseThrow();
        final Unit target = model
                .getUnitsAt(NativeTurnScene.at(targetOffset, targetOffset))
                .stream()
                .filter(unit -> unit.getOwner().getName().equals("Dutch"))
                .findFirst().orElseThrow();
        return model.canNativeAttack(brave, target);
    }

    @Test
    void defenseIncludesSevenSquaresButNotEight() {
        assertTrue(canAttack(scene(3, false, false, false), 3));
        assertFalse(canAttack(scene(4, false, false, false), 4));
    }

    @Test
    void friendReceivesDefenseOnlyAgainstWartimeEnemies() {
        assertTrue(canAttack(scene(3, false, true, true), 3));
        assertFalse(canAttack(scene(3, false, true, false), 3));
    }

    @Test
    void missionariesAreProtectedFromHostileAndAlliedNativeArmies() {
        assertFalse(canAttack(scene(3, true, false, false), 3));
        assertFalse(canAttack(scene(3, true, true, true), 3));
    }
}
