package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.EuropeanRelationPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.UnitActionNoAction;
import org.microcol.model.unit.UnitFreeColonist;
import org.microcol.model.unit.UnitWithCargo;

import com.google.gson.Gson;

class EuropeanHostilityTest {
    private final Gson gson = new Gson();
    private Model model;
    private Unit attacker;
    private Unit defender;
    private Colony colony;

    @BeforeEach
    void prepareBorder() {
        final GameModelPo saved = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.microcol");
        final List<PlayerPo> players = saved.getModel().getGameManager()
                .getPlayers();
        final PlayerPo spain = gson.fromJson(gson.toJson(
                players.stream().filter(p -> p.getName().equals("France"))
                        .findFirst().orElseThrow()),
                PlayerPo.class);
        spain.setName("Spain");
        players.add(spain);
        for (final String first : List.of("Dutch", "France")) {
            final EuropeanRelationPo pair = new EuropeanRelationPo();
            pair.setFirstPlayer(first);
            pair.setSecondPlayer("Spain");
            pair.setState(EuropeanRelationState.PEACE);
            saved.getModel().getEuropeanRelations().add(pair);
        }
        model = Model.make(saved.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        model.startGame();
        attacker = model.getUnitById(0);
        attacker.placeToLocation(Location.of(18, 10));
        ((UnitFreeColonist) attacker).setHoldingGuns(true);
        defender = model.getUnitById(6);
        defender.placeToLocation(Location.of(17, 11));
        attacker.getOwner().revealMapForUnit(attacker);
        colony = model.getColonyById(0).orElseThrow();
        assertEquals(Location.of(17, 10), colony.getLocation());
        assertEquals(1, attacker.getActionPoints());
    }

    private Player player(final String name) {
        return model.getPlayerByName(name);
    }

    private String snapshot() {
        return gson.toJson(model.save());
    }

    private void declareWar() {
        model.declareHostilities(player("Dutch"), player("France"));
    }

    @Test
    void hostilityIsSymmetricAndKeepsCrownOutsideEuropeanPairs() {
        assertFalse(model.areHostile(player("Dutch"), player("France")));
        assertFalse(model.areHostile(player("France"), player("Dutch")));
        assertFalse(model.areHostile(player("Dutch"), player("Dutch")));
        assertTrue(model.areHostile(player("Dutch"), player("Dutch's King")));
        assertTrue(model.areHostile(player("Dutch's King"), player("Dutch")));
        assertFalse(model.areHostile(player("Dutch's King"),
                player("Dutch's King")));
        final String before = snapshot();
        assertThrows(IllegalArgumentException.class,
                () -> model.areHostile(null, player("France")));
        final Model foreign = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertThrows(IllegalArgumentException.class,
                () -> model.areHostile(player("Dutch"),
                        foreign.getPlayerByName("France")));
        assertEquals(before, snapshot());
        declareWar();
        assertTrue(model.areHostile(player("Dutch"), player("France")));
        assertTrue(model.areHostile(player("France"), player("Dutch")));
        assertFalse(model.areHostile(player("Dutch"), player("Spain")));
        model.acceptPeace(player("Dutch"), player("France"));
        assertFalse(model.areHostile(player("Dutch"), player("France")));
    }

    @Test
    void peacefulForeignColonyBlocksBothOrdinaryAndIgnoreOccupancyRoutes() {
        final String before = snapshot();
        assertFalse(attacker.isPossibleToMoveAt(colony.getLocation()));
        assertFalse(attacker.isPossibleToMoveAt(colony.getLocation(), true));
        assertFalse(attacker.isPossibleToCaptureColonyAt(colony.getLocation()));
        assertTrue(attacker.getPath(colony.getLocation()).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> model
                .moveUnit(attacker, Path.of(List.of(colony.getLocation()))));
        assertThrows(IllegalStateException.class,
                () -> colony.captureColony(attacker));
        assertEquals(before, snapshot());
    }

    @Test
    void directCaptureOnTheColonyTileStillRejectsPeaceWithoutEffects() {
        attacker.placeToLocation(colony.getLocation());
        final String before = snapshot();
        assertThrows(IllegalStateException.class,
                () -> colony.captureColony(attacker));
        assertEquals(before, snapshot());
    }

    @Test
    void directCaptureCannotTeleportAnAdjacentForceEvenAtWar() {
        declareWar();
        final String before = snapshot();
        assertThrows(IllegalStateException.class,
                () -> colony.captureColony(attacker));
        assertEquals(before, snapshot());
    }

    @Test
    void warAllowsOneNormalStepToCaptureAnUnguardedColony() {
        declareWar();
        final int id = colony.getId();
        final String name = colony.getName();
        final Player formerOwner = colony.getOwner();
        final String stores = gson.toJson(colony.save().getColonyWarehouse());
        assertTrue(attacker.isPossibleToMoveAt(colony.getLocation()));
        assertTrue(attacker.isPossibleToCaptureColonyAt(colony.getLocation()));
        model.moveUnit(attacker, Path.of(List.of(colony.getLocation())));
        assertSame(player("Dutch"), colony.getOwner());
        assertEquals(id, colony.getId());
        assertEquals(name, colony.getName());
        assertEquals(0, attacker.getActionPoints());
        assertTrue(attacker.isAtPlaceLocation());
        assertEquals(colony.getLocation(), attacker.getLocation());
        assertTrue(((UnitFreeColonist) attacker).isHoldingGuns());
        assertSame(player("Dutch"), model.getUnitById(4).getOwner());
        assertSame(formerOwner, defender.getOwner());
        assertEquals(stores, gson.toJson(colony.save().getColonyWarehouse()));
        assertEquals(EuropeanRelationState.WAR,
                model.getEuropeanRelation(player("Dutch"), formerOwner));
    }

    @Test
    void acceptedPeaceClosesCaptureWithoutSpendingPointsOrChangingOwnership() {
        declareWar();
        assertTrue(attacker.isPossibleToCaptureColonyAt(colony.getLocation()));
        model.acceptPeace(player("Dutch"), player("France"));
        final String before = snapshot();
        assertFalse(attacker.isPossibleToCaptureColonyAt(colony.getLocation()));
        assertThrows(IllegalArgumentException.class,
                () -> attacker.moveOneStep(colony.getLocation()));
        assertThrows(IllegalStateException.class,
                () -> colony.captureColony(attacker));
        assertEquals(before, snapshot());
    }

    @Test
    void attackAndMoveQueriesOutsideTheMapAreReadOnlyRejections() {
        final String before = snapshot();
        assertFalse(attacker.isPossibleToAttackAt(Location.of(0, 0)));
        assertFalse(attacker.isPossibleToMoveAt(Location.of(0, 0)));
        assertEquals(before, snapshot());
    }

    @Test
    void peaceBlocksDirectAttackBeforeMovementOrCombatEffects() {
        final String before = snapshot();
        assertFalse(attacker.isPossibleToAttackAt(defender.getLocation()));
        assertThrows(IllegalStateException.class,
                () -> attacker.attack(defender.getLocation()));
        assertEquals(before, snapshot());
    }

    @Test
    void warAllowsExistingCombatAndExhaustsASurvivingAttacker() {
        declareWar();
        assertTrue(attacker.isPossibleToAttackAt(defender.getLocation()));
        final int count = model.getAllUnits().size();
        attacker.attack(defender.getLocation());
        assertEquals(count - 1, model.getAllUnits().size());
        assertNotEquals(model.tryGetUnitById(attacker.getId()).isPresent(),
                model.tryGetUnitById(defender.getId()).isPresent());
        model.tryGetUnitById(attacker.getId())
                .ifPresent(unit -> assertEquals(0, unit.getActionPoints()));
        assertEquals(EuropeanRelationState.WAR,
                model.getEuropeanRelation(player("Dutch"), player("France")));
    }

    @Test
    void acceptedPeaceClosesThePreviouslyAllowedAttack() {
        declareWar();
        assertTrue(attacker.isPossibleToAttackAt(defender.getLocation()));
        model.acceptPeace(player("Dutch"), player("France"));
        final String before = snapshot();
        assertFalse(attacker.isPossibleToAttackAt(defender.getLocation()));
        assertThrows(IllegalStateException.class,
                () -> attacker.attack(defender.getLocation()));
        assertEquals(before, snapshot());
    }

    @Test
    void peacefulColonyRejectsAttackEvenWhenItsOccupantIsAHostileThirdParty() {
        defender.takeOver(player("Spain"));
        defender.placeToLocation(colony.getLocation());
        model.declareHostilities(player("Dutch"), player("Spain"));
        final String before = snapshot();
        assertFalse(attacker.isPossibleToAttackAt(colony.getLocation()));
        assertTrue(
                attacker.prepareAttack(colony.getLocation(), true).isEmpty());
        assertFalse(attacker.isPossibleToCaptureColonyAt(colony.getLocation()));
        assertThrows(IllegalStateException.class,
                () -> attacker.attack(colony.getLocation()));
        assertEquals(before, snapshot());
    }

    @Test
    void mixedStackCombatNeverSelectsThePeacefulUnitAsDefender() {
        final Unit hostile = model.getUnitById(2);
        hostile.takeOver(player("Spain"));
        hostile.placeToLocation(defender.getLocation());
        ((UnitFreeColonist) hostile).setHoldingGuns(true);
        model.declareHostilities(player("Dutch"), player("Spain"));
        final String peacefulUnit = gson.toJson(defender.save());
        assertTrue(attacker.isPossibleToAttackAt(defender.getLocation()));
        final AttackOrder proposal = attacker
                .prepareAttack(defender.getLocation(), true).orElseThrow();
        assertSame(hostile, proposal.getDefender());
        assertFalse(proposal.isDeclarationRequired());
        proposal.execute();
        assertSame(defender, model.getUnitById(6));
        assertEquals(peacefulUnit, gson.toJson(defender.save()));
        assertEquals(EuropeanRelationState.PEACE,
                model.getEuropeanRelation(player("Dutch"), player("France")));
    }

    @Test
    void peaceDoesNotPermitStackingOrPassengerLandingOnForeignUnits() {
        final Location shore = Location.of(18, 9);
        final Unit ship = model.getUnitById(3);
        final Unit passenger = model.getUnitById(1);
        ship.placeToLocation(Location.of(19, 9));
        defender.placeToLocation(shore);
        final String before = snapshot();
        assertTrue(model.getMap().getTerrainTypeAt(ship.getLocation()).isSea());
        assertFalse(attacker.isPossibleToMoveAt(shore));
        assertTrue(attacker.isPossibleToMoveAt(shore, true));
        assertFalse(passenger.canUnitDisembarkAt(shore));
        assertThrows(IllegalArgumentException.class,
                () -> attacker.moveOneStep(shore));
        assertThrows(IllegalStateException.class,
                () -> passenger.disembarkToLocation(shore));
        assertEquals(before, snapshot());
    }

    private static final Location WEST_WATER = Location.of(16, 9);
    private static final Location EAST_WATER = Location.of(17, 9);

    private void prepareCoastalColony() {
        final ModelPo data = model.save();
        final Map<Location, TerrainType> terrain = data.getMap()
                .getTerrainMap();
        colony.getLocation().getNeighbors()
                .forEach(at -> terrain.put(at, TerrainType.GRASSLAND));
        terrain.put(WEST_WATER, TerrainType.OCEAN);
        terrain.put(EAST_WATER, TerrainType.OCEAN);
        data.getMap().setTerrainType(terrain);
        model = Model.make(data, MissionImpl.getPredefinedGameOverEvaluators());
        attacker = model.getUnitById(0);
        colony = model.getColonyById(0).orElseThrow();
        declareWar();
    }

    private Unit ship(final int id, final String owner, final Location at) {
        final UnitPo data = new UnitPo();
        data.setId(id);
        data.setType(UnitType.GALLEON);
        data.setOwnerId(owner);
        data.setAction(new UnitActionNoAction().save());
        data.setAvailableMoves(0);
        final PlaceMapPo place = new PlaceMapPo();
        place.setLocation(Location.of(1, 3));
        place.setOrientation(Direction.east);
        data.setPlaceMap(place);
        final Unit created = model.createUnit(model.save(), data);
        created.placeToLocation(at);
        return created;
    }

    @Test
    void wartimePortEntryEvacuatesTheShipWithoutCapturingItsCargo() {
        prepareCoastalColony();
        final Unit vessel = ship(100, "France", colony.getLocation());
        ((UnitWithCargo) vessel).getCargo().getSlotByIndex(0)
                .addGoods(Goods.of(GoodsType.ORE, 7));
        final String cargo = gson.toJson(vessel.save().getCargo());
        final String before = snapshot();
        assertTrue(attacker.isPossibleToMoveAt(colony.getLocation()));
        assertTrue(attacker.isPossibleToCaptureColonyAt(colony.getLocation()));
        assertEquals(before, snapshot());
        attacker.moveOneStep(colony.getLocation());
        assertSame(player("Dutch"), colony.getOwner());
        assertSame(player("France"), vessel.getOwner());
        assertTrue(
                List.of(WEST_WATER, EAST_WATER).contains(vessel.getLocation()));
        assertEquals(cargo, gson.toJson(vessel.save().getCargo()));
        assertEquals(0, vessel.getActionPoints());
        assertEquals(0, attacker.getActionPoints());
        assertEquals(colony.getLocation(), attacker.getLocation());
        assertTrue(((UnitFreeColonist) attacker).isHoldingGuns());
    }

    @Test
    void blockedShipRetreatRejectsBeforeMovementOwnershipOrStockChanges() {
        prepareCoastalColony();
        ship(100, "France", colony.getLocation());
        ship(101, "Dutch", WEST_WATER);
        ship(102, "Dutch", EAST_WATER);
        final String before = snapshot();
        assertFalse(attacker.isPossibleToCaptureColonyAt(colony.getLocation()));
        assertFalse(attacker.isPossibleToMoveAt(colony.getLocation()));
        assertThrows(IllegalArgumentException.class,
                () -> attacker.moveOneStep(colony.getLocation()));
        assertEquals(before, snapshot());
        attacker.placeToLocation(colony.getLocation());
        final String onTile = snapshot();
        assertThrows(IllegalStateException.class,
                () -> colony.captureColony(attacker));
        assertEquals(onTile, snapshot());
    }

    @Test
    void neutralShipPreventsWartimeColonyEntryWithoutBeingDisplaced() {
        prepareCoastalColony();
        ship(100, "Spain", colony.getLocation());
        final String before = snapshot();
        assertFalse(attacker.isPossibleToCaptureColonyAt(colony.getLocation()));
        assertFalse(attacker.isPossibleToMoveAt(colony.getLocation()));
        assertThrows(IllegalArgumentException.class,
                () -> attacker.moveOneStep(colony.getLocation()));
        assertEquals(before, snapshot());
    }

    @Test
    void shipsOfDifferentOwnersMustNotEscapeIntoTheSameEmptySquare() {
        prepareCoastalColony();
        model.declareHostilities(player("Dutch"), player("Spain"));
        final Unit french = ship(100, "France", colony.getLocation());
        final Unit spanish = ship(101, "Spain", colony.getLocation());
        attacker.moveOneStep(colony.getLocation());
        assertTrue(
                List.of(WEST_WATER, EAST_WATER).contains(french.getLocation()));
        assertTrue(List.of(WEST_WATER, EAST_WATER)
                .contains(spanish.getLocation()));
        assertNotEquals(french.getLocation(), spanish.getLocation());
        assertSame(player("Dutch"), colony.getOwner());
    }

    @Test
    void insufficientEscapeSquaresForTwoOwnersRejectsAllEscapesAtomically() {
        prepareCoastalColony();
        model.declareHostilities(player("Dutch"), player("Spain"));
        ship(102, "Dutch", WEST_WATER);
        ship(100, "France", colony.getLocation());
        ship(101, "Spain", colony.getLocation());
        attacker.placeToLocation(colony.getLocation());
        final String before = snapshot();
        assertFalse(attacker.isPossibleToCaptureColonyAt(colony.getLocation()));
        assertThrows(IllegalStateException.class,
                () -> colony.captureColony(attacker));
        assertEquals(before, snapshot());
    }

    @Test
    void ownOccupiedRetreatKeepsTheOnlySharedWaterFreeForTheOtherOwner() {
        prepareCoastalColony();
        model.declareHostilities(player("Dutch"), player("Spain"));
        ship(102, "France", EAST_WATER);
        final Unit french = ship(100, "France", colony.getLocation());
        final Unit spanish = ship(101, "Spain", colony.getLocation());
        attacker.moveOneStep(colony.getLocation());
        assertEquals(EAST_WATER, french.getLocation());
        assertEquals(WEST_WATER, spanish.getLocation());
        assertSame(player("Dutch"), colony.getOwner());
    }
}
