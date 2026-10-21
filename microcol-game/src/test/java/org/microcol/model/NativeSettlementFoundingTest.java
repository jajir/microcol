package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.NativeSettlementKnowledgePo;
import org.microcol.model.store.NativeSettlementPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.UnitPo;

/**
 * A native square is never a European colony site, and native identity is
 * learned only by sight and kept in the save.
 */
class NativeSettlementFoundingTest {

    /** France's colonist that the free-play map places at Quebec. */
    private static final int FOUNDER_ID = 6;

    private static NativeSettlementPo nativeAt(final int id,
            final NativeNation nation, final Location at) {
        final NativeSettlementPo po = new NativeSettlementPo();
        po.setId(id);
        po.setNation(nation);
        po.setSettlementClass(nation.getSettlementClass());
        po.setCapital(true);
        po.setLocation(at);
        po.setTreasury(2000);
        return po;
    }

    /**
     * Free play with France to move. The founder stands on a clear site with
     * grassland around it and a native settlement one square east, and a far
     * one three squares east. The site itself gets a settlement only on demand.
     */
    private static ModelPo founderSave(final boolean nativeOnSite) {
        final ModelPo po = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.microcol").getModel();
        po.getGameManager().setGameStarted(true);
        po.getGameManager().setCurrentPlayer("France");
        final Location site = clearSite(po);
        final Location near = Location.of(site.getX() + 1, site.getY());
        final Location far = Location.of(site.getX() + 3, site.getY());
        final Map<Location, TerrainType> terrain = po.getMap().getTerrainMap();
        site.getNeighbors()
                .forEach(at -> terrain.put(at, TerrainType.GRASSLAND));
        terrain.put(site, TerrainType.GRASSLAND);
        terrain.put(far, TerrainType.GRASSLAND);
        po.getMap().setTerrainType(terrain);
        po.getMap().setTrees(Collections.emptySet());
        final UnitPo founder = po.getUnits().stream()
                .filter(unit -> unit.getId() == FOUNDER_ID).findFirst()
                .orElseThrow();
        final PlaceMapPo place = new PlaceMapPo();
        place.setLocation(site);
        place.setOrientation(Direction.west);
        founder.setPlaceMap(place);
        founder.setAvailableMoves(1);
        final List<NativeSettlementPo> natives = new ArrayList<>();
        if (nativeOnSite) {
            natives.add(nativeAt(0, NativeNation.AZTEC, site));
        }
        natives.add(nativeAt(1, NativeNation.ARAWAK, near));
        natives.add(nativeAt(2, NativeNation.TUPI, far));
        po.setNativeSettlements(natives);
        // Every synthetic site needs its explicit zero alarm row for each
        // European; the saved table must match the settlement list exactly.
        po.setNativeLocalAlarm(NativeRelations.freshLocalAlarm(natives));
        po.setNativeLandRights(new ArrayList<>());
        po.setNativeLandTakings(new ArrayList<>());
        // Explicit empty knowledge: nothing is learned until a unit sees it.
        po.setNativeSettlementKnowledge(new ArrayList<>());
        return po;
    }

    /** First square at least three from every colony and free of units. */
    private static Location clearSite(final ModelPo po) {
        for (int x = 2; x <= po.getMap().getMaxX() - 3; x++) {
            for (int y = 2; y <= po.getMap().getMaxY() - 1; y++) {
                final Location at = Location.of(x, y);
                final boolean nearColony = po.getColonies().stream()
                        .anyMatch(colony -> colony.getLocation()
                                .getDistanceChebyshev(at) < 3);
                final boolean occupied = po.getUnits().stream()
                        .anyMatch(unit -> unit.getPlaceMap() != null
                                && at.equals(unit.getPlaceMap().getLocation()));
                if (!nearColony && !occupied) {
                    return at;
                }
            }
        }
        throw new IllegalStateException("No clear site on the free-play map");
    }

    private static Location founderSquare(final ModelPo po) {
        return po.getUnits().stream().filter(unit -> unit.getId() == FOUNDER_ID)
                .findFirst().orElseThrow().getPlaceMap().getLocation();
    }

    private static Set<Integer> ids(final List<NativeSettlement> settlements) {
        return settlements.stream().map(NativeSettlement::getId)
                .collect(Collectors.toCollection(HashSet::new));
    }

    @Test
    void foundingOnNativeSquareIsRefusedWithoutChangingState() {
        final ModelPo open = founderSave(false);
        final Location site = founderSquare(open);
        final Model baseline = Model.make(open,
                MissionImpl.getPredefinedGameOverEvaluators());
        // Without a settlement the same founder may found here, so the
        // refusal below is caused by the native square and nothing else.
        assertTrue(baseline.canFoundColonyAt(baseline.getUnitById(FOUNDER_ID),
                site));

        final Model model = Model.make(founderSave(true),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Unit founder = model.getUnitById(FOUNDER_ID);
        final Player france = model.getPlayerByName("France");
        assertFalse(model.canFoundColonyAt(founder, site));
        final int colonies = model.getColonies().size();
        final int settlements = model.getNativeSettlements().size();
        assertThrows(IllegalArgumentException.class,
                () -> model.buildColony(france, founder, "Native Square"));
        assertEquals(colonies, model.getColonies().size());
        assertEquals(settlements, model.getNativeSettlements().size());
        assertEquals(site, founder.getLocation());
    }

    @Test
    void identityIsLearnedOnlyBySightAndSurvivesSave() {
        final ModelPo po = founderSave(false);
        final Location site = founderSquare(po);
        final Location near = Location.of(site.getX() + 1, site.getY());
        final Location far = Location.of(site.getX() + 3, site.getY());
        final Model model = Model.make(po,
                MissionImpl.getPredefinedGameOverEvaluators());
        final Player france = model.getPlayerByName("France");
        // The founder's sight covers the neighbouring square at once.
        assertTrue(model.getNativeSettlementKnownTo(france, near).isPresent());
        // The far square is learned exactly when France can see it now.
        assertEquals(france.isCurrentlyVisible(far),
                model.getNativeSettlementKnownTo(france, far).isPresent());

        final Set<Integer> before = ids(
                model.getNativeSettlementsKnownTo(france));
        assertTrue(before.contains(1));
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertEquals(before, ids(restored.getNativeSettlementsKnownTo(
                restored.getPlayerByName("France"))));
    }

    @Test
    void explorationAloneDoesNotTeachIdentity() {
        final ModelPo po = founderSave(false);
        final Location site = founderSquare(po);
        final Location far = Location.of(site.getX() + 3, site.getY());
        final Model model = Model.make(po,
                MissionImpl.getPredefinedGameOverEvaluators());
        final Player france = model.getPlayerByName("France");
        // The far square is out of sight; a settlement there stays unknown.
        assertFalse(france.isCurrentlyVisible(far));
        assertTrue(model.getNativeSettlementKnownTo(france, far).isEmpty());
        assertFalse(ids(model.getNativeSettlementsKnownTo(france)).contains(2));
    }

    private static NativeSettlementKnowledgePo knowledge(final String player,
            final List<Integer> settlementIds) {
        final NativeSettlementKnowledgePo po = new NativeSettlementKnowledgePo();
        po.setPlayerName(player);
        po.setSettlementIds(settlementIds);
        return po;
    }

    private static void assertRejected(final ModelPo po) {
        assertThrows(IllegalArgumentException.class, () -> Model.make(po,
                MissionImpl.getPredefinedGameOverEvaluators()));
    }

    @Test
    void missingKnowledgeIsRejectedNotTreatedAsEmpty() {
        final ModelPo po = founderSave(false);
        po.setNativeSettlementKnowledge(null);
        assertRejected(po);
    }

    @Test
    void emptyKnowledgeIsAcceptedWhenExplicit() {
        final ModelPo po = founderSave(false);
        po.setNativeSettlementKnowledge(new ArrayList<>());
        assertNotNull(
                Model.make(po, MissionImpl.getPredefinedGameOverEvaluators()));
    }

    @Test
    void malformedKnowledgeEntriesAreRejected() {
        // Null entry, missing player and missing id list.
        final ModelPo nullEntry = founderSave(false);
        final List<NativeSettlementKnowledgePo> withNull = new ArrayList<>();
        withNull.add(null);
        nullEntry.setNativeSettlementKnowledge(withNull);
        assertRejected(nullEntry);

        final ModelPo noPlayer = founderSave(false);
        noPlayer.setNativeSettlementKnowledge(
                List.of(knowledge(null, List.of(1))));
        assertRejected(noPlayer);

        final ModelPo noIds = founderSave(false);
        noIds.setNativeSettlementKnowledge(List.of(knowledge("France", null)));
        assertRejected(noIds);

        // Unknown player and unknown settlement.
        final ModelPo unknownPlayer = founderSave(false);
        unknownPlayer.setNativeSettlementKnowledge(
                List.of(knowledge("Atlantis", List.of(1))));
        assertRejected(unknownPlayer);

        final ModelPo unknownSettlement = founderSave(false);
        unknownSettlement.setNativeSettlementKnowledge(
                List.of(knowledge("France", List.of(99))));
        assertRejected(unknownSettlement);

        // Null id, duplicate id and duplicate player entry.
        final ModelPo nullId = founderSave(false);
        final List<Integer> idsWithNull = new ArrayList<>();
        idsWithNull.add(null);
        nullId.setNativeSettlementKnowledge(
                List.of(knowledge("France", idsWithNull)));
        assertRejected(nullId);

        final ModelPo duplicateId = founderSave(false);
        duplicateId.setNativeSettlementKnowledge(
                List.of(knowledge("France", List.of(1, 1))));
        assertRejected(duplicateId);

        final ModelPo duplicatePlayer = founderSave(false);
        duplicatePlayer.setNativeSettlementKnowledge(
                List.of(knowledge("France", List.of(1)),
                        knowledge("France", List.of(2))));
        assertRejected(duplicatePlayer);
    }

    @Test
    void savedKnowledgeRoundTripsWithoutRelearning() {
        final ModelPo po = founderSave(false);
        po.setNativeSettlementKnowledge(
                List.of(knowledge("France", List.of(2))));
        final Model model = Model.make(po,
                MissionImpl.getPredefinedGameOverEvaluators());
        final Player france = model.getPlayerByName("France");
        // Remembered identity is kept even though the square is out of sight.
        assertFalse(france.isCurrentlyVisible(
                model.getNativeSettlements().get(1).getLocation()));
        assertTrue(ids(model.getNativeSettlementsKnownTo(france)).contains(2));
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertEquals(ids(model.getNativeSettlementsKnownTo(france)),
                ids(restored.getNativeSettlementsKnownTo(
                        restored.getPlayerByName("France"))));
        assertNotNull(restored.save().getNativeSettlementKnowledge());
    }

}
