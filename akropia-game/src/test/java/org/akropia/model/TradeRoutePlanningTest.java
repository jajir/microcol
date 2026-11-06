package org.akropia.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.akropia.model.campaign.MissionImpl;
import org.akropia.model.campaign.po.GameModelPo;
import org.akropia.model.campaign.po.GameModelPoDao;
import org.akropia.model.event.UnitMovedStepFinishedEvent;
import org.akropia.model.store.ModelPo;
import org.akropia.model.store.PlayerPo;
import org.akropia.model.store.UnitPo;

import com.google.common.collect.Lists;
import org.akropia.model.unit.Ship;
import org.akropia.model.unit.UnitWithCargo;

/**
 * Map previews of trade route paths (R-0395) on the shared F108 scenario.
 * Dutch's inland New Amsterdam (id 0) stands at (24,16) and the port Delft (id
 * 1) at (28,16); between them lies grassland costing one point a step and
 * arctic costing two. Galleon 3 waits in Delft's port and colonist 4, which the
 * tests turn into a wagon, stands in New Amsterdam. The sea lane to Europe is
 * the east edge. Dutch has explored columns 18 to 50 of rows 8 to 23.
 */
class TradeRoutePlanningTest {

    private static final File SCENARIO = new File(
            "../akropia-test/src/test/scenarios/F108-trade-route-cargo.akropia");

    private static final int NEW_AMSTERDAM = 0;
    private static final int DELFT = 1;
    private static final int SHIP_ID = 3;
    private static final int WAGON_ID = 4;
    private static final Location NEW_AMSTERDAM_AT = Location.of(24, 16);
    private static final Location DELFT_AT = Location.of(28, 16);
    private static final int MAX_ROUNDS = 30;

    /**
     * A road detour of seven one-third steps, longer than the four grassland
     * steps of three thirds each that the direct way needs.
     */
    private static final List<Location> ROAD = List.of(NEW_AMSTERDAM_AT,
            Location.of(24, 17), Location.of(24, 18), Location.of(25, 19),
            Location.of(26, 19), Location.of(27, 18), Location.of(28, 17),
            DELFT_AT);

    @TempDir
    Path directory;

    private static GameModelPo fixture(final Consumer<ModelPo> prepare) {
        final GameModelPo data = new GameModelPoDao().loadFromFile(SCENARIO);
        prepare.accept(data.getModel());
        return data;
    }

    private static Model load(final ModelPo data) {
        return Model.make(data, MissionImpl.getPredefinedGameOverEvaluators());
    }

    private static Player dutch(final Model model) {
        return model.getColonyById(DELFT).orElseThrow().getOwner();
    }

    private static Unit unitOf(final Model model, final int id) {
        return model.getAllUnits().stream().filter(unit -> unit.getId() == id)
                .findFirst().orElseThrow();
    }

    /** Colonist 4 becomes an empty wagon, as the navigation tests do. */
    private static void colonistToWagon(final ModelPo po) {
        final UnitPo wagon = po.getUnits().stream()
                .filter(unit -> unit.getId() == WAGON_ID).findFirst()
                .orElseThrow();
        wagon.setType(UnitType.WAGON);
        wagon.setTools(0);
        wagon.setHoldingWeapons(false);
        wagon.setMounted(false);
        wagon.setWorkUpdates(null);
        wagon.setWorkSkill(null);
        wagon.setMissionary(null);
    }

    /** Replaces one square's code in a saved map layer. */
    private static void setCode(final String[] rows, final Location at,
            final String code) {
        final String prefix = rows[at.getY()].substring(0,
                "row-0001:".length());
        final String[] parts = rows[at.getY()].substring(prefix.length())
                .split(",");
        parts[at.getX() - 1] = code;
        rows[at.getY()] = prefix + String.join(",", parts);
    }

    private static void ocean(final ModelPo po, final Location... squares) {
        for (final Location at : squares) {
            setCode(po.getMap().getTiles(), at, "_");
        }
    }

    private static void hideFromDutch(final ModelPo po,
            final Location... squares) {
        final PlayerPo player = po.getGameManager().getPlayers().stream()
                .filter(candidate -> candidate.getName().equals("Dutch"))
                .findFirst().orElseThrow();
        for (final Location at : squares) {
            setCode(player.getVisible().getVisible(), at, "#");
        }
    }

    private static void buildRoad(final Model model) {
        for (final Location at : ROAD) {
            if (!model.getMap().hasRoadAt(at)) {
                model.getMap().buildRoad(at);
            }
        }
    }

    private static TradeRoute landRoute(final Model model) {
        return model.createTradeRoute(dutch(model), "Wagon Road",
                TradeRouteKind.LAND, NEW_AMSTERDAM,
                List.of(TradeRouteStop.ofColony(DELFT)));
    }

    private static TradeRoute seaRoute(final Model model) {
        return model.createTradeRoute(dutch(model), "Ocean Run",
                TradeRouteKind.SEA, DELFT, List.of(TradeRouteStop.europe(),
                        TradeRouteStop.ofColony(DELFT)));
    }

    private static TradeRoutePreview preview(final Model model,
            final TradeRoute route) {
        return model.getTradeRoutePreview(dutch(model), route);
    }

    private static List<List<Location>> squares(
            final TradeRoutePreview preview) {
        return preview.getLegs().stream()
                .flatMap(leg -> leg.getPaths().stream())
                .map(TradeRoutePreview.LegPath::getSquares)
                .collect(Collectors.toList());
    }

    private static List<TradeRouteKind> kinds(final TradeRoutePreview.Leg leg) {
        return leg.getPaths().stream().map(TradeRoutePreview.LegPath::getKind)
                .collect(Collectors.toList());
    }

    /**
     * Dutch and then the King end their turns; Dutch is current again. A tax
     * demand is accepted through the player's own operation.
     */
    private static void playRound(final Model model) {
        final Player dutch = dutch(model);
        for (int i = 0; i < model.getPlayers().size(); i++) {
            model.endTurn();
            if (model.isAwaitingTaxDecision()) {
                dutch.acceptTaxDemand(dutch.getTaxDemand().orElseThrow());
            }
        }
        assertSame(dutch, model.getCurrentPlayer());
    }

    private String snapshot(final Model model, final GameModelPo data,
            final String name) throws Exception {
        final Path file = directory.resolve(name);
        data.setModel(model.save());
        new GameModelPoDao().saveToFile(file.toString(), data);
        return Files.readString(file);
    }

    @Test
    void longerCheaperRoadIsShownAndTheAssignedWagonWalksIt() {
        final Model model = load(
                fixture(TradeRoutePlanningTest::colonistToWagon).getModel());
        buildRoad(model);
        final TradeRoute route = landRoute(model);
        final Unit wagon = unitOf(model, WAGON_ID);
        model.assignTradeRoute(wagon, route, 0);

        final TradeRoutePreview preview = preview(model, route);
        assertSame(wagon, preview.getCarrier().orElseThrow());
        assertEquals(1, preview.getLegs().size());
        final TradeRoutePreview.LegPath path = preview.getLegs().get(0)
                .getPaths().get(0);
        // Seven road steps of one third beat four grassland steps of three.
        assertEquals(TradeRouteKind.LAND, path.getKind());
        assertEquals(ROAD, path.getSquares());
        assertEquals(7, path.getCostThirds());

        final List<Location> walked = new ArrayList<>();
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onUnitMovedStepFinished(
                    final UnitMovedStepFinishedEvent event) {
                if (event.getUnit() == wagon) {
                    walked.add(event.getEnd());
                }
            }
        });
        for (int round = 0; round < MAX_ROUNDS
                && !DELFT_AT.equals(wagon.getLocation()); round++) {
            playRound(model);
        }
        // Actual travel takes exactly the previewed squares.
        assertEquals(ROAD.subList(1, ROAD.size()), walked);
    }

    private static int cost(final Model model, final List<Location> squares) {
        int total = 0;
        for (int index = 1; index < squares.size(); index++) {
            total += MovementProfile.entryCostThirds(model, false,
                    squares.get(index - 1), squares.get(index));
        }
        return total;
    }

    /**
     * A trade route leg walks explored squares only. Where the way and the
     * squares around it are explored, it costs exactly as much as the ordinary
     * go-to route. Among the equal grassland routes it takes the one with the
     * fewest diagonal steps (R-0425), while go-to keeps its own choice.
     */
    @Test
    void exploredLegCostsTheSameAsTheOrdinaryGoToRouteOverExploredLand() {
        final Model model = load(
                fixture(TradeRoutePlanningTest::colonistToWagon).getModel());
        final AbstractUnit wagon = (AbstractUnit) unitOf(model, WAGON_ID);
        final List<Location> goTo = wagon.findGoToRoute(DELFT_AT).orElseThrow();
        final List<Location> leg = wagon.findKnownGoToRoute(DELFT_AT)
                .orElseThrow();
        assertEquals(4, goTo.size());
        assertEquals(4, leg.size());
        final List<Location> goToSquares = new ArrayList<>(
                List.of(NEW_AMSTERDAM_AT));
        goToSquares.addAll(goTo);
        final List<Location> legSquares = new ArrayList<>(
                List.of(NEW_AMSTERDAM_AT));
        legSquares.addAll(leg);
        assertEquals(12, cost(model, goToSquares));
        assertEquals(12, cost(model, legSquares));
        assertNotEquals(goTo, leg);
        assertEquals(List.of(Location.of(25, 15), Location.of(26, 15),
                Location.of(27, 15), DELFT_AT), leg);
        assertTrue(leg.stream().allMatch(dutch(model)::isVisible));
    }

    /**
     * New Amsterdam lies on arctic land, two points to enter, but a road on it
     * and on (24,17) makes the last step from (24,17) cost one third. From
     * (25,16) a full one-point allowance would allow the dear direct step.
     */
    private static final Location ROAD_TO_TOWN = Location.of(24, 17);

    private static void roadIntoNewAmsterdam(final Model model) {
        for (final Location at : List.of(NEW_AMSTERDAM_AT, ROAD_TO_TOWN)) {
            if (!model.getMap().hasRoadAt(at)) {
                model.getMap().buildRoad(at);
            }
        }
    }

    private static Consumer<ModelPo> wagonAt(final Location at) {
        return po -> {
            colonistToWagon(po);
            po.getUnits().stream().filter(unit -> unit.getId() == WAGON_ID)
                    .findFirst().orElseThrow().getPlaceMap().setLocation(at);
        };
    }

    @Test
    void ordinaryGoToKeepsItsDearLastStepButARouteLegTakesTheCheaperDetour() {
        final Location nextToTown = Location.of(25, 16);
        final Model model = load(fixture(wagonAt(nextToTown)).getModel());
        roadIntoNewAmsterdam(model);
        final AbstractUnit wagon = (AbstractUnit) unitOf(model, WAGON_ID);
        assertTrue(wagon.hasFullMovement());

        // A go-to order prefers what it can finish this turn: the full
        // allowance pays the six-third arctic step at once.
        assertEquals(List.of(NEW_AMSTERDAM_AT),
                wagon.findGoToRoute(NEW_AMSTERDAM_AT).orElseThrow());
        assertTrue(wagon.canAffordStepTo(NEW_AMSTERDAM_AT));

        // A trade route leg selects the lowest total cost, three plus one.
        assertEquals(List.of(ROAD_TO_TOWN, NEW_AMSTERDAM_AT),
                wagon.findKnownGoToRoute(NEW_AMSTERDAM_AT).orElseThrow());
    }

    @Test
    void assignedWagonWalksThePreviewedCheapestLegWithinItsAllowance() {
        final Model model = load(fixture(wagonAt(DELFT_AT)).getModel());
        roadIntoNewAmsterdam(model);
        final TradeRoute route = model.createTradeRoute(dutch(model),
                "Town Road", TradeRouteKind.LAND, DELFT,
                List.of(TradeRouteStop.ofColony(NEW_AMSTERDAM)));
        final Unit wagon = unitOf(model, WAGON_ID);
        model.assignTradeRoute(wagon, route, 0);

        final TradeRoutePreview.LegPath path = preview(model, route).getLegs()
                .get(0).getPaths().get(0);
        final List<Location> squares = path.getSquares();
        assertEquals(DELFT_AT, squares.get(0));
        // Four grassland steps to the road, then one third into the town:
        // cheaper than any way ending with the two-point arctic step.
        assertEquals(13, path.getCostThirds());
        assertEquals(List.of(ROAD_TO_TOWN, NEW_AMSTERDAM_AT),
                squares.subList(squares.size() - 2, squares.size()));

        final List<List<Location>> rounds = new ArrayList<>();
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onUnitMovedStepFinished(
                    final UnitMovedStepFinishedEvent event) {
                if (event.getUnit() == wagon) {
                    rounds.get(rounds.size() - 1).add(event.getEnd());
                }
            }
        });
        for (int round = 0; round < MAX_ROUNDS
                && !NEW_AMSTERDAM_AT.equals(wagon.getLocation()); round++) {
            rounds.add(new ArrayList<>());
            playRound(model);
        }
        assertEquals(NEW_AMSTERDAM_AT, wagon.getLocation());

        final List<Location> walked = rounds.stream().flatMap(List::stream)
                .collect(Collectors.toList());
        // The first real step is the previewed one, the way ends with the
        // cheap road step, and the total cost is the previewed minimum.
        assertEquals(squares.get(1), walked.get(0));
        assertEquals(List.of(ROAD_TO_TOWN, NEW_AMSTERDAM_AT),
                walked.subList(walked.size() - 2, walked.size()));
        Location from = DELFT_AT;
        int total = 0;
        for (final List<Location> turn : rounds) {
            int spent = 0;
            for (final Location step : turn) {
                spent += MovementProfile.entryCostThirds(model, false, from,
                        step);
                from = step;
            }
            // Selecting the cheapest way spends nothing extra: each turn the
            // wagon pays at most its one-point allowance.
            assertTrue(spent <= MovementPoints.THIRDS_PER_POINT,
                    "Spent " + spent + " thirds in one turn: " + turn);
            total += spent;
        }
        assertEquals(path.getCostThirds(), total);
    }

    @Test
    void changedMovementCostIsReadByTheNextPreview() {
        final Model model = load(fixture(po -> {
        }).getModel());
        final TradeRoute route = landRoute(model);
        final TradeRoutePreview.LegPath before = preview(model, route).getLegs()
                .get(0).getPaths().get(0);
        assertEquals(TradeRouteKind.LAND, before.getKind());
        assertEquals(12, before.getCostThirds());
        assertEquals(5, before.getSquares().size());

        buildRoad(model);
        final TradeRoutePreview.LegPath after = preview(model, route).getLegs()
                .get(0).getPaths().get(0);
        assertEquals(ROAD, after.getSquares());
        assertEquals(7, after.getCostThirds());
    }

    @Test
    void unexploredSquaresNeverShapeAPath() {
        // The road crosses two squares Dutch has never seen.
        final Location hiddenA = Location.of(25, 19);
        final Location hiddenB = Location.of(26, 19);
        final Model model = load(
                fixture(po -> hideFromDutch(po, hiddenA, hiddenB)).getModel());
        assertFalse(dutch(model).isVisible(hiddenA));
        buildRoad(model);
        final TradeRoute route = landRoute(model);

        // Without them the best way crosses two grassland squares to join
        // the road's eastern end: three, three, three, then two road thirds.
        final TradeRoutePreview.LegPath path = preview(model, route).getLegs()
                .get(0).getPaths().get(0);
        assertEquals(11, path.getCostThirds());
        assertFalse(path.getSquares().contains(hiddenA));
        assertFalse(path.getSquares().contains(hiddenB));
        assertTrue(
                path.getSquares().stream().allMatch(dutch(model)::isVisible));
    }

    @Test
    void assignedWagonShowsOnlyItsLandPath() {
        final Model model = load(
                fixture(TradeRoutePlanningTest::colonistToWagon).getModel());
        final TradeRoute route = landRoute(model);
        model.assignTradeRoute(unitOf(model, WAGON_ID), route, 0);

        final TradeRoutePreview preview = preview(model, route);
        assertEquals(WAGON_ID, preview.getCarrier().orElseThrow().getId());
        final TradeRoutePreview.Leg leg = preview.getLegs().get(0);
        assertEquals(TradeRouteStop.ofColony(NEW_AMSTERDAM), leg.getFrom());
        assertEquals(TradeRouteStop.ofColony(DELFT), leg.getTo());
        assertEquals(List.of(TradeRouteKind.LAND), kinds(leg));
        assertTrue(leg.getPaths().get(0).getSquares().stream()
                .noneMatch(at -> model.getMap().getTerrainTypeAt(at).isSea()));
        assertEquals(TradeRouteKind.LAND, route.getKind());
    }

    @Test
    void routeWithoutCarrierShowsBothReachableVariantsAndKeepsItsKind() {
        // A channel makes New Amsterdam a port that ships reach from Delft.
        final Model model = load(fixture(po -> ocean(po, Location.of(25, 17),
                Location.of(26, 17), Location.of(27, 17))).getModel());
        final TradeRoute route = landRoute(model);

        final TradeRoutePreview preview = preview(model, route);
        assertTrue(preview.getCarrier().isEmpty());
        final TradeRoutePreview.Leg leg = preview.getLegs().get(0);
        assertEquals(List.of(TradeRouteKind.LAND, TradeRouteKind.SEA),
                kinds(leg));
        assertTrue(leg.getProblem().isEmpty());
        assertEquals(
                List.of(NEW_AMSTERDAM_AT, Location.of(25, 17),
                        Location.of(26, 17), Location.of(27, 17), DELFT_AT),
                leg.getPaths().get(1).getSquares());
        assertEquals(TradeRouteKind.LAND,
                model.getTradeRoutes(dutch(model)).get(0).getKind());
    }

    @Test
    void routeWithoutCarrierShowsOnlyTheReachableVariant() {
        final Model model = load(fixture(po -> {
        }).getModel());
        // Inland New Amsterdam: only a wagon can make the leg.
        final TradeRoute land = landRoute(model);
        assertEquals(List.of(TradeRouteKind.LAND),
                kinds(preview(model, land).getLegs().get(0)));

        // Europe: only a ship can make either leg.
        final TradeRoute sea = seaRoute(model);
        final TradeRoutePreview preview = preview(model, sea);
        assertEquals(2, preview.getLegs().size());
        for (final TradeRoutePreview.Leg leg : preview.getLegs()) {
            assertEquals(List.of(TradeRouteKind.SEA), kinds(leg));
        }
    }

    @Test
    void routeWithoutAnyLegalPathExplainsItAndDrawsNothing() {
        final GameModelPo data = fixture(po -> {
        });
        final Model original = load(data.getModel());
        landRoute(original);
        // Water now rings New Amsterdam two squares out: no wagon torches it
        // and the inland colony has no port for a ship.
        final ModelPo saved = original.save();
        for (int x = 22; x <= 26; x++) {
            ocean(saved, Location.of(x, 14), Location.of(x, 18));
        }
        for (int y = 15; y <= 17; y++) {
            ocean(saved, Location.of(22, y), Location.of(26, y));
        }
        final Model model = load(saved);
        final TradeRoute route = model.getTradeRoutes(dutch(model)).get(0);

        final TradeRoutePreview.Leg leg = preview(model, route).getLegs()
                .get(0);
        assertTrue(leg.getPaths().isEmpty());
        assertEquals(TradeRoutePreviewProblem.NO_PATH,
                leg.getProblem().orElseThrow());
    }

    @Test
    void europeLegEndsAtTheActualCheapestDepartureAndReturnsToItsOrigin() {
        final Model model = load(fixture(po -> {
        }).getModel());
        final TradeRoute route = seaRoute(model);
        final Ship ship = (Ship) unitOf(model, SHIP_ID);
        model.assignTradeRoute(ship, route, 0);
        final TradeRouteRules rules = new TradeRouteRules(model);

        final TradeRoutePreview preview = preview(model, route);
        assertSame(ship, preview.getCarrier().orElseThrow());
        final TradeRoutePreview.Leg out = preview.getLegs().get(0);
        assertTrue(out.getTo().isEurope());
        final List<Location> outward = out.getPaths().get(0).getSquares();
        assertEquals(DELFT_AT, outward.get(0));
        // The same nearest departure the carrier itself would sail to.
        assertEquals(((AbstractUnit) ship)
                .findNearestRoute(rules::isDepartureSquare).orElseThrow(),
                outward.subList(1, outward.size()));
        assertTrue(rules.isDepartureSquare(outward.get(outward.size() - 1)));
        assertEquals((outward.size() - 1) * MovementPoints.THIRDS_PER_POINT,
                out.getPaths().get(0).getCostThirds());
        assertTrue(outward.subList(1, outward.size()).stream()
                .allMatch(at -> model.getMap().getTerrainTypeAt(at).isSea()));

        // A ship that never left returns to the default arrival square.
        final TradeRoutePreview.Leg back = preview.getLegs().get(1);
        assertTrue(back.getFrom().isEurope());
        assertEquals(
                model.getHighSea().getDefaultArrivalLocation().orElseThrow(),
                back.getPaths().get(0).getSquares().get(0));
        assertEquals(DELFT_AT, back.getPaths().get(0).getSquares()
                .get(back.getPaths().get(0).getSquares().size() - 1));

        // Once the ship has really departed, the return starts where it left.
        for (int round = 0; round < MAX_ROUNDS
                && ship.isAtPlaceLocation(); round++) {
            playRound(model);
        }
        assertFalse(ship.isAtPlaceLocation());
        final Location departure = ship.getDepartureLocation().orElseThrow();
        assertEquals(departure, preview(model, route).getLegs().get(1)
                .getPaths().get(0).getSquares().get(0));
    }

    @Test
    void equalCostPathStaysTheSameAcrossRepaintsAndContinue() {
        final GameModelPo data = fixture(po -> {
        });
        final Model model = load(data.getModel());
        final TradeRoute route = seaRoute(model);
        final List<List<Location>> first = squares(preview(model, route));
        assertEquals(first, squares(preview(model, route)));

        data.setModel(model.save());
        final Model restored = load(data.getModel());
        final TradeRoute again = restored.getTradeRoutes(dutch(restored))
                .get(0);
        assertEquals(route.getId(), again.getId());
        assertEquals(first, squares(preview(restored, again)));
    }

    private static TradeRoute loop(final Model model) {
        final TradeRoute route = model.createTradeRoute(dutch(model), "Loop",
                TradeRouteKind.LAND, NEW_AMSTERDAM,
                List.of(TradeRouteStop.ofColony(DELFT),
                        TradeRouteStop.ofColony(NEW_AMSTERDAM)));
        return model.configureTradeRouteStopCargo(dutch(model), route, 0,
                List.of(GoodsType.ORE), List.of());
    }

    private static TradeRoutePreview.LegPath path(
            final TradeRoutePreview.Leg leg, final TradeRouteKind kind) {
        return leg.getPaths().stream().filter(path -> path.getKind() == kind)
                .findFirst().orElseThrow();
    }

    /**
     * Several grassland ways from New Amsterdam to Delft cost the same. The way
     * there and the way back pick the same squares for each transport, so the
     * map draws one land and one sea connection. Each direction still costs its
     * own cheapest amount, and the choice survives repaints and Continue.
     */
    @Test
    void wayBackOverTheSameSquaresIsOneConnectionPerTransport() {
        // A channel makes New Amsterdam a port that ships reach from Delft.
        final GameModelPo data = fixture(po -> {
            colonistToWagon(po);
            ocean(po, Location.of(25, 17), Location.of(26, 17),
                    Location.of(27, 17));
        });
        final Model model = load(data.getModel());
        final TradeRoute route = loop(model);

        final TradeRoutePreview preview = preview(model, route);
        assertTrue(preview.getCarrier().isEmpty());
        assertEquals(2, preview.getLegs().size());
        final TradeRoutePreview.Leg there = preview.getLegs().get(0);
        final TradeRoutePreview.Leg back = preview.getLegs().get(1);
        assertEquals(TradeRouteStop.ofColony(DELFT), there.getTo());
        assertEquals(TradeRouteStop.ofColony(NEW_AMSTERDAM), back.getTo());
        for (final TradeRouteKind kind : List.of(TradeRouteKind.LAND,
                TradeRouteKind.SEA)) {
            assertEquals(Lists.reverse(path(there, kind).getSquares()),
                    path(back, kind).getSquares(), kind.name());
        }
        // Land enters grassland Delft one way and arctic New Amsterdam back.
        assertEquals(12, path(there, TradeRouteKind.LAND).getCostThirds());
        assertEquals(15, path(back, TradeRouteKind.LAND).getCostThirds());

        // The ordinary go-to of the wagon is an equally cheap other way.
        final AbstractUnit wagon = (AbstractUnit) unitOf(model, WAGON_ID);
        final List<Location> goTo = new ArrayList<>(List.of(NEW_AMSTERDAM_AT));
        goTo.addAll(wagon.findGoToRoute(DELFT_AT).orElseThrow());
        assertNotEquals(path(there, TradeRouteKind.LAND).getSquares(), goTo);
        assertEquals(12, cost(model, goTo));

        // One connection per transport, the same on every repaint.
        final List<TradeRoutePreview.LegPath> connections = preview
                .getConnections();
        assertEquals(List.of(TradeRouteKind.LAND, TradeRouteKind.SEA),
                connections.stream().map(TradeRoutePreview.LegPath::getKind)
                        .collect(Collectors.toList()));
        assertEquals(path(there, TradeRouteKind.LAND).getSquares(),
                connections.get(0).getSquares());
        assertEquals(path(there, TradeRouteKind.SEA).getSquares(),
                connections.get(1).getSquares());
        final List<List<Location>> drawn = connections.stream()
                .map(TradeRoutePreview.LegPath::getSquares)
                .collect(Collectors.toList());
        assertEquals(drawn,
                preview(model, route).getConnections().stream()
                        .map(TradeRoutePreview.LegPath::getSquares)
                        .collect(Collectors.toList()));

        data.setModel(model.save());
        final Model restored = load(data.getModel());
        final TradeRoute again = restored.getTradeRoutes(dutch(restored))
                .get(0);
        assertEquals(drawn,
                preview(restored, again).getConnections().stream()
                        .map(TradeRoutePreview.LegPath::getSquares)
                        .collect(Collectors.toList()));
    }

    /**
     * The assigned wagon's preview is one land connection, and the wagon really
     * walks it to Delft and back over the same squares.
     */
    @Test
    void assignedWagonWalksTheOneConnectionThereAndBack() {
        final Model model = load(
                fixture(TradeRoutePlanningTest::colonistToWagon).getModel());
        final TradeRoute route = loop(model);
        final Unit wagon = unitOf(model, WAGON_ID);
        model.assignTradeRoute(wagon, route, 0);

        final List<TradeRoutePreview.LegPath> connections = preview(model,
                route).getConnections();
        assertEquals(1, connections.size());
        assertEquals(TradeRouteKind.LAND, connections.get(0).getKind());
        final List<Location> squares = connections.get(0).getSquares();
        assertEquals(NEW_AMSTERDAM_AT, squares.get(0));
        assertEquals(DELFT_AT, squares.get(squares.size() - 1));

        final List<Location> walked = new ArrayList<>();
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onUnitMovedStepFinished(
                    final UnitMovedStepFinishedEvent event) {
                if (event.getUnit() == wagon) {
                    walked.add(event.getEnd());
                }
            }
        });
        for (int round = 0; round < MAX_ROUNDS && !(walked.contains(DELFT_AT)
                && NEW_AMSTERDAM_AT.equals(wagon.getLocation())); round++) {
            playRound(model);
        }
        final List<Location> expected = new ArrayList<>(
                squares.subList(1, squares.size()));
        expected.addAll(Lists.reverse(squares).subList(1, squares.size()));
        assertEquals(expected, walked);
    }

    /**
     * A road into New Amsterdam makes the way back cheaper over other squares
     * than the way there: each stays its own connection, at its own lowest
     * cost, instead of one direction taking the other's dearer squares.
     */
    @Test
    void cheaperWayBackOverOtherSquaresStaysItsOwnConnection() {
        final Model model = load(fixture(po -> {
        }).getModel());
        roadIntoNewAmsterdam(model);
        final TradeRoute route = loop(model);

        final TradeRoutePreview preview = preview(model, route);
        final TradeRoutePreview.LegPath there = path(preview.getLegs().get(0),
                TradeRouteKind.LAND);
        final TradeRoutePreview.LegPath back = path(preview.getLegs().get(1),
                TradeRouteKind.LAND);
        assertEquals(12, there.getCostThirds());
        assertEquals(13, back.getCostThirds());
        assertEquals(List.of(ROAD_TO_TOWN, NEW_AMSTERDAM_AT),
                back.getSquares().subList(back.getSquares().size() - 2,
                        back.getSquares().size()));
        // Either way taken in the other direction would cost more.
        assertEquals(15, cost(model, Lists.reverse(there.getSquares())));
        assertEquals(13, cost(model, Lists.reverse(back.getSquares())));
        assertFalse(there.isSameConnection(back));
        assertEquals(List.of(there, back), preview.getConnections());
    }

    /** Two grass squares in the open sea east of Delft. */
    private static final List<Location> ISLAND = List.of(Location.of(32, 14),
            Location.of(32, 15));

    private static void island(final ModelPo po) {
        for (final Location at : ISLAND) {
            setCode(po.getMap().getTiles(), at, "g");
        }
    }

    private static List<Location> route(final PathFinder finder,
            final Location from, final Location to) {
        final List<Location> squares = new ArrayList<>(List.of(from));
        squares.addAll(finder.find(from, to, false));
        return squares;
    }

    /**
     * North and south of the island lie equally cheap sea routes that are
     * mirror images of each other, so the diagonal steps cannot tell them
     * apart. The fixed order of steps chooses the western one both ways, and a
     * ship that searches again from a square on the way keeps to it.
     */
    @Test
    void equalSeaRoutesAroundAnIslandAreChosenAlikeBothWaysAndOnTheWay() {
        final Model model = load(
                fixture(TradeRoutePlanningTest::island).getModel());
        final PathFinder finder = new PathFinder(
                MovementProfile.of(model, UnitType.GALLEON, dutch(model)), true,
                dutch(model)::isVisible);
        final Location north = Location.of(32, 13);
        final Location south = Location.of(32, 16);

        final List<Location> down = route(finder, north, south);
        final List<Location> west = List.of(north, Location.of(31, 14),
                Location.of(31, 15), south);
        assertEquals(west, down);
        // The eastern mirror is just as legal and cheap.
        final List<Location> east = List.of(north, Location.of(33, 14),
                Location.of(33, 15), south);
        assertTrue(east.stream().allMatch(at -> model.getMap()
                .getTerrainTypeAt(at).equals(TerrainType.OCEAN)));
        assertEquals(Lists.reverse(down), route(finder, south, north));
        assertEquals(down.subList(1, down.size()),
                route(finder, down.get(1), south));
        assertEquals(Lists.reverse(down).subList(1, down.size()),
                route(finder, down.get(2), north));
    }

    /**
     * Every sea route between squares of the open sea around the island is the
     * way back of the route in the other direction, and from each of its
     * squares a new search takes the rest of it unchanged.
     */
    @Test
    void everySeaRouteNearTheIslandIsItsOwnWayBackAndKeepsItsRest() {
        final Model model = load(
                fixture(TradeRoutePlanningTest::island).getModel());
        final PathFinder finder = new PathFinder(
                MovementProfile.of(model, UnitType.GALLEON, dutch(model)), true,
                dutch(model)::isVisible);
        final List<Location> sea = new ArrayList<>();
        for (int x = 30; x <= 34; x++) {
            for (int y = 11; y <= 18; y++) {
                final Location at = Location.of(x, y);
                if (!ISLAND.contains(at)) {
                    assertEquals(TerrainType.OCEAN,
                            model.getMap().getTerrainTypeAt(at), at::toString);
                    sea.add(at);
                }
            }
        }
        for (final Location from : sea) {
            for (final Location to : sea) {
                if (from.equals(to)) {
                    continue;
                }
                final List<Location> there = route(finder, from, to);
                assertEquals(Lists.reverse(there), route(finder, to, from),
                        from + " to " + to);
                for (int index = 1; index + 1 < there.size(); index++) {
                    assertEquals(there.subList(index, there.size()),
                            route(finder, there.get(index), to),
                            from + " to " + to + " from " + there.get(index));
                }
            }
        }
    }

    @Test
    void previewChangesNothingAndIsOnlyForTheOwner() throws Exception {
        final GameModelPo data = fixture(
                TradeRoutePlanningTest::colonistToWagon);
        final Model model = load(data.getModel());
        final TradeRoute land = landRoute(model);
        final TradeRoute sea = seaRoute(model);
        model.assignTradeRoute(unitOf(model, WAGON_ID), land, 0);
        final int units = model.getAllUnits().size();
        final String before = snapshot(model, data, "before.akropia");

        for (int repaint = 0; repaint < 3; repaint++) {
            preview(model, land);
            preview(model, sea);
        }

        assertEquals(units, model.getAllUnits().size());
        assertEquals(NEW_AMSTERDAM_AT, unitOf(model, WAGON_ID).getLocation());
        assertEquals(DELFT_AT, unitOf(model, SHIP_ID).getLocation());
        assertEquals(before, snapshot(model, data, "after.akropia"));
        final Player king = model.getPlayers().stream()
                .filter(player -> player != dutch(model)).findFirst()
                .orElseThrow();
        assertThrows(IllegalArgumentException.class,
                () -> model.getTradeRoutePreview(king, land));
    }

    @Test
    void legsFollowTheRepeatingCycleOncePerStopPair() {
        final Model model = load(fixture(po -> {
        }).getModel());
        TradeRoute route = model.createTradeRoute(dutch(model), "Loop",
                TradeRouteKind.LAND, NEW_AMSTERDAM,
                List.of(TradeRouteStop.ofColony(DELFT),
                        TradeRouteStop.ofColony(NEW_AMSTERDAM)));
        route = model.configureTradeRouteStopCargo(dutch(model), route, 0,
                List.of(GoodsType.ORE), List.of());
        // Start to Delft, then back; the closing leg repeats the first pair.
        final List<TradeRoutePreview.Leg> legs = preview(model, route)
                .getLegs();
        assertEquals(2, legs.size());
        assertEquals(TradeRouteStop.ofColony(DELFT), legs.get(0).getTo());
        assertEquals(TradeRouteStop.ofColony(NEW_AMSTERDAM),
                legs.get(1).getTo());
        final UnitWithCargo none = preview(model, route).getCarrier()
                .orElse(null);
        assertNull(none);
    }
}
