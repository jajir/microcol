package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Type-level rows of the R-0211 unit ledger. Expected values come from the
 * player aid card (PDF 132) and the accepted rules, not from {@link UnitType}.
 * Instance-level equipment and strength rows are owned by the slice tests named
 * in the functional evidence.
 */
class UnitRoleLedgerTest {

    private static final List<UnitType> LAND_TYPES = List.of(UnitType.COLONIST,
            UnitType.BRAVE, UnitType.ARTILLERY, UnitType.WAGON,
            UnitType.TREASURE_TRAIN, UnitType.SEASONED_SCOUT,
            UnitType.VETERAN_SOLDIER);

    private static final List<UnitType> SHIP_TYPES = List.of(UnitType.CARAVEL,
            UnitType.MERCHANTMAN, UnitType.GALLEON, UnitType.PRIVATEER,
            UnitType.FRIGATE, UnitType.MAN_OF_WAR);

    @Test
    void landTypesWalkOnLandOnlyAndShipsSailOnSeaOnly() {
        for (final UnitType type : LAND_TYPES) {
            assertTrue(type.canMoveAtTerrain(TerrainType.GRASSLAND),
                    type.name());
            assertTrue(type.canMoveAtTerrain(TerrainType.ARCTIC), type.name());
            assertTrue(type.canMoveAtTerrain(TerrainType.MOUNTAIN),
                    type.name());
            assertFalse(type.canMoveAtTerrain(TerrainType.OCEAN), type.name());
            assertFalse(type.canMoveAtTerrain(TerrainType.HIGH_SEA),
                    type.name());
        }
        for (final UnitType type : SHIP_TYPES) {
            assertTrue(type.canMoveAtTerrain(TerrainType.OCEAN), type.name());
            assertTrue(type.canMoveAtTerrain(TerrainType.HIGH_SEA),
                    type.name());
            assertFalse(type.canMoveAtTerrain(TerrainType.GRASSLAND),
                    type.name());
            assertFalse(type.canMoveAtTerrain(TerrainType.MOUNTAIN),
                    type.name());
        }
    }

    @Test
    void typeLevelAttackPermissionFollowsTheAidCardRows() {
        assertTrue(UnitType.BRAVE.canAttack());
        assertTrue(UnitType.ARTILLERY.canAttack());
        assertTrue(UnitType.VETERAN_SOLDIER.canAttack());
        assertTrue(UnitType.SEASONED_SCOUT.canAttack());
        // Instance eligibility still needs muskets; see ColonistEquipmentTest
        // and AttackRoleTest.
        assertTrue(UnitType.COLONIST.canAttack());
        assertFalse(UnitType.WAGON.canAttack());
        assertFalse(UnitType.TREASURE_TRAIN.canAttack());
    }

    @Test
    void landAttackersNeverTargetShipsAndArmedShipsTargetOnlyShips() {
        for (final UnitType type : LAND_TYPES) {
            assertTrue(type.getAttackableUnitType().stream()
                    .noneMatch(UnitType::isShip), type.name());
        }
        for (final UnitType type : SHIP_TYPES) {
            final List<UnitType> targets = type.getAttackableUnitType();
            if (type.canAttack()) {
                assertFalse(targets.isEmpty(), type.name());
                assertTrue(targets.stream().allMatch(UnitType::isShip),
                        type.name());
            } else {
                assertTrue(targets.isEmpty(), type.name());
            }
        }
    }

    @Test
    void everyPassengerTakesOneHoldExceptTheSixHoldTreasureTrain() {
        for (final UnitType type : LAND_TYPES) {
            assertEquals(type == UnitType.TREASURE_TRAIN ? 6 : 1,
                    type.getRequiredHolds(), type.name());
        }
        assertFalse(UnitType.WAGON.isStorable());
        assertTrue(UnitType.ARTILLERY.isStorable());
    }
}
