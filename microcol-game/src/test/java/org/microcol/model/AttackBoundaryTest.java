package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.UnitActionType;
import org.microcol.model.unit.UnitFreeColonist;
import com.google.gson.Gson;

class AttackBoundaryTest {
    private static final Location ORIGIN = Location.of(18, 10);
    private static final Location TARGET = Location.of(18, 11);
    private final Gson gson = new Gson();
    private Model model;
    private Unit attacker;
    private Unit defender;

    private void prepare(final UnitType role, final UnitType targetType,
            final TerrainType terrain) {
        final ModelPo data = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.microcol").getModel();
        final UnitPo target = data.getUnits().stream()
                .filter(u -> u.getId() == 6).findFirst().orElseThrow();
        target.setType(targetType);
        target.setWorkUpdates(targetType == UnitType.COLONIST
                || targetType == UnitType.MASTER_BLACKSMITH ? 0 : null);
        target.getPlaceMap().setLocation(TARGET);
        final UnitPo force = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.microcol").getModel()
                .getUnits().stream().filter(u -> u.getId() == 6).findFirst()
                .orElseThrow();
        force.setId(20);
        force.setOwnerId("Dutch");
        force.setType(role);
        force.setWorkUpdates(
                role == UnitType.COLONIST || role == UnitType.MASTER_BLACKSMITH
                        ? 0
                        : null);
        force.setHoldingGuns(true);
        force.getPlaceMap().setLocation(ORIGIN);
        data.getUnits().add(force);
        final Map<Location, TerrainType> terrainMap = data.getMap()
                .getTerrainMap();
        terrainMap.put(ORIGIN, terrain);
        terrainMap.put(TARGET, terrain);
        data.getMap().setTerrainType(terrainMap);
        model = Model.make(data, MissionImpl.getPredefinedGameOverEvaluators());
        model.startGame();
        while (!model.getCurrentPlayer().getName().equals("Dutch")) {
            model.endTurn();
        }
        attacker = model.getUnitById(20);
        defender = model.getUnitById(6);
    }

    @BeforeEach
    void ordinaryArmedBorder() {
        prepare(UnitType.COLONIST, UnitType.COLONIST, TerrainType.GRASSLAND);
    }

    @ParameterizedTest
    @CsvSource({ "FRIGATE,GALLEON,OCEAN,true", "FRIGATE,FRIGATE,OCEAN,true",
            "GALLEON,FRIGATE,OCEAN,false", "WAGON,COLONIST,GRASSLAND,false",
            "COLONIST,FRIGATE,GRASSLAND,false",
            "MASTER_BLACKSMITH,FRIGATE,GRASSLAND,false",
            "FRIGATE,COLONIST,OCEAN,false", "FRIGATE,GALLEON,GRASSLAND,false",
            "COLONIST,COLONIST,OCEAN,false",
            "MASTER_BLACKSMITH,COLONIST,GRASSLAND,true" })
    void roleAndTargetDomainControlPreparation(final String role,
            final String targetType, final String terrain,
            final boolean allowed) {
        prepare(UnitType.valueOf(role), UnitType.valueOf(targetType),
                TerrainType.valueOf(terrain));
        final String before = gson.toJson(model.save());
        assertEquals(allowed, attacker.prepareAttack(TARGET, true).isPresent());
        assertEquals(before, gson.toJson(model.save()));
        if (!allowed) {
            assertThrows(IllegalStateException.class,
                    () -> attacker.attack(TARGET));
            assertEquals(before, gson.toJson(model.save()));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "null", "outside", "own-tile", "distant", "empty",
            "friendly", "no-movement", "tools-only", "working", "aboard",
            "unregistered", "unknown" })
    void unavailableProposalsAndCommandsDoNotMutate(final String boundary) {
        Location target = TARGET;
        switch (boundary) {
        case "null":
            target = null;
            break;
        case "outside":
            target = Location.of(0, 0);
            break;
        case "own-tile":
            target = ORIGIN;
            break;
        case "distant":
            defender.placeToLocation(Location.of(20, 11));
            target = defender.getLocation();
            break;
        case "empty":
            target = Location.of(19, 10);
            break;
        case "friendly":
            defender.takeOver(attacker.getOwner());
            break;
        case "no-movement":
            ((AbstractUnit) attacker).setActionPoints(0);
            break;
        case "tools-only":
            ((UnitFreeColonist) attacker).setHoldingGuns(false);
            ((UnitFreeColonist) attacker).setTools(100);
            break;
        case "working":
            ((AbstractUnit) attacker).setActionType(UnitActionType.plowField);
            break;
        case "aboard":
            attacker = model.getUnitById(0);
            ((UnitFreeColonist) attacker).setHoldingGuns(true);
            break;
        case "unregistered":
            model.destroyUnit(attacker);
            break;
        case "unknown":
            attacker.placeToLocation(Location.of(30, 28));
            defender.placeToLocation(Location.of(31, 28));
            target = defender.getLocation();
            assertFalse(attacker.getOwner().isVisible(target));
            break;
        default:
            fail(boundary);
        }
        final Location chosen = target;
        final String before = gson.toJson(model.save());
        assertTrue(attacker.prepareAttack(chosen, true).isEmpty());
        assertFalse(attacker.isPossibleToAttackAt(chosen));
        assertThrows(IllegalStateException.class,
                () -> attacker.attack(chosen));
        assertEquals(before, gson.toJson(model.save()));
    }

    @Test
    void aShipBattleExhaustsAllRemainingMovement() {
        prepare(UnitType.FRIGATE, UnitType.GALLEON, TerrainType.OCEAN);
        ((AbstractUnit) attacker).setActionPoints(attacker.getSpeed());
        ((AbstractUnit) defender).setActionPoints(6);
        attacker.prepareAttack(TARGET, true).orElseThrow().execute();
        assertEquals(0, attacker.getActionPoints());
        assertEquals(0, defender.getActionPoints());
    }
}
